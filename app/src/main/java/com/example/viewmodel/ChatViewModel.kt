package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.model.ChatMessage
import com.example.service.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val chatDao = db.chatMessageDao

    // SharedPreferences to persist configurable API URL and custom settings
    private val prefs = application.getSharedPreferences("ai_chat_settings", Context.MODE_PRIVATE)
    private val defaultApiUrl = "https://nhut0902-chatbotai.hf.space/chat"

    private val _apiUrl = MutableStateFlow(prefs.getString("api_url", defaultApiUrl) ?: defaultApiUrl)
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    // Configurable service type: "huggingface" (HF) or "nvidia" (Nvidia NIM)
    private val _serviceType = MutableStateFlow(prefs.getString("service_type", "huggingface") ?: "huggingface")
    val serviceType: StateFlow<String> = _serviceType.asStateFlow()

    // Default Nvidia key requested by the user
    private val defaultNvidiaKey = "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"
    private val _nvidiaApiKey = MutableStateFlow(prefs.getString("nvidia_api_key", defaultNvidiaKey) ?: defaultNvidiaKey)
    val nvidiaApiKey: StateFlow<String> = _nvidiaApiKey.asStateFlow()

    private val _nvidiaModel = MutableStateFlow(prefs.getString("nvidia_model", "nvidia/llama-3.1-nemotron-70b-instruct") ?: "nvidia/llama-3.1-nemotron-70b-instruct")
    val nvidiaModel: StateFlow<String> = _nvidiaModel.asStateFlow()

    // Retrieve historical logs from local Room DB
    val savedMessages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current screen local active messages (includes temporary loading prompts / animated items)
    private val _activeMessages = MutableStateFlow<List<ChatMessageItem>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessageItem>> = _activeMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var activeJob: Job? = null
    private var lastSentMessageText: String = ""

    init {
        // Collect saved DB logs and update active messages list on launch
        viewModelScope.launch {
            savedMessages.collect { list ->
                // Avoid overriding active generating lists
                if (!_isGenerating.value) {
                    _activeMessages.value = list.map { ChatMessageItem.Persisted(it) }
                } else {
                    // Sync up history, keeping any active generator state
                    val currentList = _activeMessages.value
                    val freshHistory = list.map { ChatMessageItem.Persisted(it) }
                    val activeTempAndAi = currentList.filter { it is ChatMessageItem.Temporary || it.isUser == false && it.text.isEmpty() }
                    _activeMessages.value = freshHistory + activeTempAndAi
                }
            }
        }
    }

    fun updateApiUrl(newUrl: String) {
        val sanitized = newUrl.trim()
        _apiUrl.value = sanitized
        prefs.edit().putString("api_url", sanitized).apply()
    }

    fun updateServiceType(type: String) {
        _serviceType.value = type
        prefs.edit().putString("service_type", type).apply()
    }

    fun updateNvidiaApiKey(key: String) {
        val sanitized = key.trim()
        _nvidiaApiKey.value = sanitized
        prefs.edit().putString("nvidia_api_key", sanitized).apply()
    }

    fun updateNvidiaModel(model: String) {
        _nvidiaModel.value = model
        prefs.edit().putString("nvidia_model", model).apply()
    }

    fun toggleTheme() {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        prefs.edit().putBoolean("is_dark_theme", newVal).apply()
    }

    fun sendMessage(
        text: String,
        attachUri: String? = null,
        attachName: String? = null,
        attachType: String? = null
    ) {
        if (text.trim().isEmpty() && attachUri == null || _isGenerating.value) return
        val rawInput = text.trim()
        lastSentMessageText = rawInput

        activeJob = viewModelScope.launch {
            _isGenerating.value = true

            // Formulate prompt with attachment context
            var enhancedPrompt = rawInput
            if (attachUri != null) {
                if (attachType == "document") {
                    val contentText = extractUriText(attachUri)
                    if (contentText != null && contentText.trim().isNotEmpty()) {
                        enhancedPrompt = "[Tài liệu đính kèm: $attachName]\nNội dung văn bản:\n$contentText\n\nYêu cầu câu hỏi: $rawInput"
                    } else {
                        enhancedPrompt = "[Tài liệu đính kèm: $attachName]\n\nYêu cầu câu hỏi: $rawInput"
                    }
                } else if (attachType == "image") {
                    enhancedPrompt = "[Hình ảnh đính kèm: $attachName]\n\nYêu cầu câu hỏi: $rawInput"
                }
            }

            // 1. Persist user input with attachment metadata to Room Db
            val userMsg = ChatMessage(
                text = rawInput,
                isUser = true,
                attachmentUri = attachUri,
                attachmentName = attachName,
                attachmentType = attachType
            )
            chatDao.insertMessage(userMsg)

            // 1b. Create thinking slot
            val currentList = _activeMessages.value.toMutableList()
            // Append temporary AI thinking state
            val thinkingMsg = ChatMessageItem.Temporary(
                id = -System.currentTimeMillis(),
                text = "",
                isUser = false,
                isThinking = true
            )
            _activeMessages.value = currentList + thinkingMsg

            // 2. Query dynamic backend space
            try {
                val isNvidia = _serviceType.value == "nvidia"
                if (isNvidia) {
                    val authHeader = "Bearer " + _nvidiaApiKey.value.trim()
                    
                    // Construct complete chat context for conversational memory
                    val historyList = savedMessages.value.map { msg ->
                        var msgContent = msg.text
                        if (msg.attachmentUri != null) {
                            if (msg.attachmentType == "document") {
                                val contentText = extractUriText(msg.attachmentUri)
                                if (contentText != null && contentText.trim().isNotEmpty()) {
                                    msgContent = "[Tài liệu đính kèm: ${msg.attachmentName}]\nNội dung văn bản:\n$contentText\n\nYêu cầu: ${msg.text}"
                                } else {
                                    msgContent = "[Tài liệu đính kèm: ${msg.attachmentName}]\n\nYêu cầu: ${msg.text}"
                                }
                            } else if (msg.attachmentType == "image") {
                                msgContent = "[Hình ảnh đính kèm: ${msg.attachmentName}]\n\nYêu cầu: ${msg.text}"
                            }
                        }
                        NvidiaChatMessage(
                            role = if (msg.isUser) "user" else "assistant",
                            content = msgContent
                        )
                    }.toMutableList()

                    if (historyList.isEmpty() || historyList.last().content != enhancedPrompt) {
                        historyList.add(NvidiaChatMessage(role = "user", content = enhancedPrompt))
                    }

                    val nvidiaRequest = NvidiaChatRequest(
                        model = _nvidiaModel.value,
                        messages = historyList
                    )

                    val response = RetrofitClient.apiService.sendNvidiaChat(
                        url = "https://integrate.api.nvidia.com/v1/chat/completions",
                        authHeader = authHeader,
                        request = nvidiaRequest
                    )

                    // Remove thinking animation
                    _activeMessages.value = _activeMessages.value.filter { it !is ChatMessageItem.Temporary }

                    if (response.isSuccessful) {
                        val choices = response.body()?.choices
                        val aiResponseText = choices?.firstOrNull()?.message?.content ?: "Không thể trích xuất nội dung từ phản hồi của NIM AI."
                        animateAndSaveResponse(aiResponseText)
                    } else {
                        val errorDetail = response.errorBody()?.string() ?: ""
                        showErrorCard("Lỗi NVIDIA NIM (Mã: ${response.code()})\n$errorDetail")
                    }
                } else {
                    val currentEndpoint = _apiUrl.value
                    val request = ChatRequest(message = enhancedPrompt)

                    val response = RetrofitClient.apiService.sendChat(currentEndpoint, request)

                    // Remove the temporary AI thinking card
                    _activeMessages.value = _activeMessages.value.filter { it !is ChatMessageItem.Temporary }

                    if (response.isSuccessful) {
                        val body = response.body()
                        val rawAiResponseText = body?.response
                            ?: body?.text
                            ?: body?.message
                            ?: body?.output
                            ?: body?.generated_text
                            ?: "Không nhận được phản hồi."
                        animateAndSaveResponse(rawAiResponseText)
                    } else {
                        showErrorCard("Mã lỗi: ${response.code()}. Vui lòng kiểm tra lại cấu hình Endpoint.")
                    }
                }
            } catch (e: IOException) {
                // Remove thinking indicator
                _activeMessages.value = _activeMessages.value.filter { it !is ChatMessageItem.Temporary }
                showErrorCard("Lỗi kết nối mạng: Không thể kết nối với máy chủ AI. Hãy kiểm tra mạng Internet.")
            } catch (e: Exception) {
                _activeMessages.value = _activeMessages.value.filter { it !is ChatMessageItem.Temporary }
                showErrorCard("Lỗi không xác định: ${e.localizedMessage}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun extractUriText(uriStr: String): String? {
        try {
            val uri = android.net.Uri.parse(uriStr)
            val resolver = getApplication<Application>().contentResolver
            resolver.openInputStream(uri)?.use { stream ->
                return stream.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun retryLastMessage() {
        if (lastSentMessageText.isNotEmpty()) {
            // Remove previous error state
            _activeMessages.value = _activeMessages.value.filter { it !is ChatMessageItem.Temporary || !it.isError }
            sendMessage(lastSentMessageText)
        }
    }

    private suspend fun animateAndSaveResponse(fullText: String) {
        val tempId = -System.currentTimeMillis()
        val builder = java.lang.StringBuilder()

        // Create secondary dynamic typing card
        val typingCard = ChatMessageItem.Temporary(
            id = tempId,
            text = "",
            isUser = false,
            isThinking = false,
            isTyping = true
        )

        val currentList = _activeMessages.value.toMutableList()
        _activeMessages.value = currentList + typingCard

        // Speed-typing animation config
        val words = fullText.split(" ")
        for (i in words.indices) {
            // Check for cancelation signal
            if (activeJob?.isCancelled == true) break

            builder.append(words[i])
            if (i < words.size - 1) builder.append(" ")

            _activeMessages.value = _activeMessages.value.map {
                if (it is ChatMessageItem.Temporary && it.id == tempId) {
                    it.copy(text = builder.toString())
                } else {
                    it
                }
            }
            delay(40) // fast natural word chunk delays
        }

        // Save officially to local database
        val aiMsg = ChatMessage(text = builder.toString().ifEmpty { fullText }, isUser = false)
        chatDao.insertMessage(aiMsg)
    }

    private fun showErrorCard(errorMessage: String) {
        val currentList = _activeMessages.value.toMutableList()
        val errorCard = ChatMessageItem.Temporary(
            id = -System.currentTimeMillis(),
            text = errorMessage,
            isUser = false,
            isThinking = false,
            isError = true
        )
        _activeMessages.value = currentList + errorCard
    }

    fun stopGenerating() {
        activeJob?.cancel()
        _isGenerating.value = false
        // Purge any temporary states
        _activeMessages.value = _activeMessages.value.filter {
            if (it is ChatMessageItem.Temporary) {
                // Safe keep typed-so-far messages but make them permanent
                if (it.isTyping && it.text.isNotEmpty()) {
                    viewModelScope.launch {
                        chatDao.insertMessage(ChatMessage(text = it.text + " [Đã dừng]", isUser = false))
                    }
                    false
                } else {
                    false
                }
            } else {
                true
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
            _activeMessages.value = emptyList()
        }
    }
}

sealed interface ChatMessageItem {
    val id: Long
    val text: String
    val isUser: Boolean
    val attachmentUri: String?
    val attachmentName: String?
    val attachmentType: String?

    data class Persisted(val message: ChatMessage) : ChatMessageItem {
        override val id: Long = message.id
        override val text: String = message.text
        override val isUser: Boolean = message.isUser
        override val attachmentUri: String? = message.attachmentUri
        override val attachmentName: String? = message.attachmentName
        override val attachmentType: String? = message.attachmentType
    }

    data class Temporary(
        override val id: Long,
        override val text: String,
        override val isUser: Boolean,
        override val attachmentUri: String? = null,
        override val attachmentName: String? = null,
        override val attachmentType: String? = null,
        val isThinking: Boolean = false,
        val isTyping: Boolean = false,
        val isError: Boolean = false
    ) : ChatMessageItem
}

