package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.model.ChatMessage
import com.example.service.ChatRequest
import com.example.service.RetrofitClient
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

    fun toggleTheme() {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        prefs.edit().putBoolean("is_dark_theme", newVal).apply()
    }

    fun sendMessage(text: String) {
        if (text.trim().isEmpty() || _isGenerating.value) return
        lastSentMessageText = text.trim()

        activeJob = viewModelScope.launch {
            _isGenerating.value = true

            // 1. Persist user input to Room Db
            val userMsg = ChatMessage(text = lastSentMessageText, isUser = true)
            chatDao.insertMessage(userMsg)

            // 1b. Clear error flags on sending success
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
                val currentEndpoint = _apiUrl.value
                val request = ChatRequest(message = lastSentMessageText)

                val response = RetrofitClient.apiService.sendChat(currentEndpoint, request)

                // Remove the temporary AI thinking card
                _activeMessages.value = _activeMessages.value.filter { it !is ChatMessageItem.Temporary }

                if (response.isSuccessful) {
                    val rawAiResponseText = response.body()?.response ?: "Không nhận được phản hồi."
                    animateAndSaveResponse(rawAiResponseText)
                } else {
                    showErrorCard("Mã lỗi: ${response.code()}. Vui lòng kiểm tra lại cấu hình Endpoint.")
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

    data class Persisted(val message: ChatMessage) : ChatMessageItem {
        override val id: Long = message.id
        override val text: String = message.text
        override val isUser: Boolean = message.isUser
    }

    data class Temporary(
        override val id: Long,
        override val text: String,
        override val isUser: Boolean,
        val isThinking: Boolean = false,
        val isTyping: Boolean = false,
        val isError: Boolean = false
    ) : ChatMessageItem
}
