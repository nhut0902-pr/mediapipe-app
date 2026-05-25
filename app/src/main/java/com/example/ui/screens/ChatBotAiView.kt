package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.ChatMessageItem
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotAiView(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val activeMessages by chatViewModel.activeMessages.collectAsStateWithLifecycle()
    val isGenerating by chatViewModel.isGenerating.collectAsStateWithLifecycle()
    val apiUrl by chatViewModel.apiUrl.collectAsStateWithLifecycle()
    val isDarkTheme by chatViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val serviceType by chatViewModel.serviceType.collectAsStateWithLifecycle()
    val nvidiaApiKey by chatViewModel.nvidiaApiKey.collectAsStateWithLifecycle()
    val nvidiaModel by chatViewModel.nvidiaModel.collectAsStateWithLifecycle()

    var showConfigDialog by remember { mutableStateOf(false) }
    var inputUrlText by remember { mutableStateOf(apiUrl) }
    var inputApiKeyText by remember { mutableStateOf(nvidiaApiKey) }
    var selectedServiceType by remember { mutableStateOf(serviceType) }
    var selectedNvidiaModel by remember { mutableStateOf(nvidiaModel) }
    var customModelInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }

    var selectedAttachmentUri by remember { mutableStateOf<String?>(null) }
    var selectedAttachmentName by remember { mutableStateOf<String?>(null) }
    var selectedAttachmentType by remember { mutableStateOf<String?>(null) } // "image" or "document"

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedAttachmentUri = uri.toString()
            selectedAttachmentName = getFileNameHelper(context, uri)
            selectedAttachmentType = "image"
        }
    }

    val selectDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedAttachmentUri = uri.toString()
            selectedAttachmentName = getFileNameHelper(context, uri)
            selectedAttachmentType = "document"
        }
    }

    LaunchedEffect(showConfigDialog) {
        if (showConfigDialog) {
            inputUrlText = apiUrl
            inputApiKeyText = nvidiaApiKey
            selectedServiceType = serviceType
            selectedNvidiaModel = nvidiaModel
            customModelInput = if (nvidiaModel !in listOf(
                    "google/gemma-2-27b-it",
                    "moonshotai/k2.5-thinking",
                    "deepseek-ai/deepseek-v3",
                    "nvidia/nemotron-4-340b-instruct",
                    "thm/glm-4-9b-chat",
                    "meta/llama-3.1-70b-instruct",
                    "meta/llama-3.1-8b-instruct",
                    "qwen/qwen-vl-max",
                    "nvidia/llama-3.1-70b-instruct-vl",
                    "personaplex-7b-v1",
                    "nvidia/parakeet-ctc-0.6b"
                )) nvidiaModel else ""
        }
    }

    val listState = rememberLazyListState()

    // Trigger auto scrolls to bottom whenever new message triggers
    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    // Cozy Custom Dark/Light palette specifically for Chatbot
    val appBg = if (isDarkTheme) Color(0xFF0F0F12) else Color(0xFFF6F6F9)
    val cardAiBg = if (isDarkTheme) Color(0xFF1E1F25) else Color(0xFFECEEF3)
    val cardUserBg = if (isDarkTheme) Color(0xFF7F3DFF) else Color(0xFF6200EE)
    val onAppBg = if (isDarkTheme) Color(0xFFF0F0FC) else Color(0xFF1F1F24)
    val onAiBg = if (isDarkTheme) Color(0xFFE2E2EC) else Color(0xFF2E2E36)
    val onUserBg = Color.White
    val borderInputBg = if (isDarkTheme) Color(0xFF26272F) else Color(0xFFE2E4EB)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = appBg
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Trợ lý Chatbot AI",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = onAppBg,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = if (serviceType == "nvidia") {
                                        val modelShort = nvidiaModel.substringAfterLast("/")
                                        "NVIDIA NIM • $modelShort"
                                    } else {
                                        "Hugging Face AI"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (serviceType == "nvidia") Color(0xFF76B900) else MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = onAppBg
                            )
                        }
                    },
                    actions = {
                        // Light / Dark Theme selector
                        IconButton(onClick = { chatViewModel.toggleTheme() }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Thay đổi chủ đề",
                                tint = onAppBg
                            )
                        }
                        // API Server Endpoint Config button
                        IconButton(onClick = {
                            inputUrlText = apiUrl
                            showConfigDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt máy chủ",
                                tint = onAppBg
                            )
                        }
                        // Trash can clear history option
                        IconButton(onClick = {
                            chatViewModel.clearHistory()
                            Toast.makeText(context, "Đã xóa sạch lịch sử chat local", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Xóa hội thoại",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBg,
                        navigationIconContentColor = onAppBg,
                        titleContentColor = onAppBg
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Chat Stream
                if (activeMessages.isEmpty()) {
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFF007F), Color(0xFF7F00FF))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Xin chào! Bạn cần hỗ trợ gì?",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = onAppBg,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Hãy nhập tin nhắn bên dưới để trò chuyện bảo mật. Lịch sử trò chuyện sẽ được Room Database lưu trữ cục bộ tuyệt mật.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onAppBg.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                if (serviceType == "nvidia") {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardAiBg),
                                        modifier = Modifier.clickable {
                                            showConfigDialog = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF76B900))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "NVIDIA NIM: $nvidiaModel",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = onAiBg.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardAiBg),
                                        modifier = Modifier.clickable {
                                            showConfigDialog = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Link,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = apiUrl,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = onAiBg.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 86.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activeMessages, key = { it.id }) { item ->
                            val isUser = item.isUser
                            val bubbleBg = if (isUser) cardUserBg else cardAiBg
                            val contentColor = if (isUser) onUserBg else onAiBg

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                // AI profile bubble avatar
                                if (!isUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFFE94057), Color(0xFF8A2387))
                                                )
                                            )
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                // Interactive message card
                                Column(
                                    modifier = Modifier.weight(0.85f, fill = false),
                                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        color = bubbleBg,
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        ),
                                        shadowElevation = if (isDarkTheme) 0.dp else 1.dp,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(item.text))
                                            Toast.makeText(context, "Đã sao chép tin nhắn vào khay nhớ tạm!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            if (item.attachmentUri != null) {
                                                if (item.attachmentType == "image") {
                                                    Card(
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .padding(bottom = 8.dp)
                                                            .fillMaxWidth()
                                                            .heightIn(max = 200.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
                                                    ) {
                                                        AsyncImage(
                                                            model = item.attachmentUri,
                                                            contentDescription = item.attachmentName ?: "Attached Image",
                                                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                                        )
                                                    }
                                                    Text(
                                                        text = item.attachmentName ?: "image.png",
                                                        fontSize = 11.sp,
                                                        fontStyle = FontStyle.Italic,
                                                        color = contentColor.copy(alpha = 0.7f),
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                    )
                                                } else {
                                                    Card(
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier
                                                            .padding(bottom = 8.dp)
                                                            .fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                                        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.15f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Description,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = item.attachmentName ?: "document.txt",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = contentColor
                                                                )
                                                                Text(
                                                                    text = "Tập tin tài liệu",
                                                                    fontSize = 10.sp,
                                                                    color = contentColor.copy(alpha = 0.6f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            when (item) {
                                                is ChatMessageItem.Temporary -> {
                                                    if (item.isThinking) {
                                                        ThinkingDots(dotColor = MaterialTheme.colorScheme.primary)
                                                    } else if (item.isError) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Icon(
                                                                imageVector = Icons.Default.ErrorOutline,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = item.text,
                                                                color = MaterialTheme.colorScheme.error,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Button(
                                                                onClick = { chatViewModel.retryLastMessage() },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                            ) {
                                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Thử lại", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                                            }
                                                        }
                                                    } else {
                                                        MarkdownText(text = item.text, color = contentColor)
                                                    }
                                                }
                                                is ChatMessageItem.Persisted -> {
                                                    if (item.text.isNotEmpty()) {
                                                        MarkdownText(text = item.text, color = contentColor)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Context actions: copy and details
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (isUser) "Bạn" else "Trợ lý AI",
                                            fontSize = 9.sp,
                                            color = onAppBg.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "•",
                                            fontSize = 9.sp,
                                            color = onAppBg.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = "Nhấn để sao chép",
                                            fontSize = 9.sp,
                                            color = onAppBg.copy(alpha = 0.4f),
                                            modifier = Modifier.clickable {
                                                clipboardManager.setText(AnnotatedString(item.text))
                                                Toast.makeText(context, "Đã sao chép tin nhắn!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }

                                if (isUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                        }
                    }
                }

                // Input Box Bottom Area + Floating generation controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, appBg.copy(alpha = 0.95f), appBg)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Stop Generating float button
                        AnimatedVisibility(
                            visible = isGenerating,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Button(
                                onClick = { chatViewModel.stopGenerating() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                shape = CircleShape,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dừng phản hồi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Draft attachment preview container
                        AnimatedVisibility(
                            visible = selectedAttachmentUri != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = cardAiBg
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedAttachmentType == "image") {
                                        Card(
                                            modifier = Modifier.size(46.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            AsyncImage(
                                                model = selectedAttachmentUri,
                                                contentDescription = "Selected Attachment",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedAttachmentName ?: "file",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = onAppBg,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (selectedAttachmentType == "image") "Hình ảnh sẵn sàng gửi" else "Tài liệu sẵn sàng phân tích",
                                            fontSize = 11.sp,
                                            color = onAppBg.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedAttachmentUri = null
                                            selectedAttachmentName = null
                                            selectedAttachmentType = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hủy đính kèm",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Message Text Field Input row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp, max = 120.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(borderInputBg)
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectImageLauncher.launch("image/*") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Chọn hình ảnh",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { selectDocLauncher.launch("*/*") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Chọn tài liệu",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            TextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                placeholder = {
                                    Text(
                                        "Nhập tin nhắn đến AI...",
                                        color = onAppBg.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = onAppBg,
                                    unfocusedTextColor = onAppBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                maxLines = 4
                            )

                            // Animated send action button
                            val isInputEmpty = messageInput.trim().isEmpty() && selectedAttachmentUri == null
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (!isInputEmpty && !isGenerating) cardUserBg else Color.Gray.copy(alpha = 0.3f)
                                    )
                                    .clickable(enabled = !isInputEmpty && !isGenerating) {
                                        val textToSend = messageInput
                                        val uriToSend = selectedAttachmentUri
                                        val nameToSend = selectedAttachmentName
                                        val typeToSend = selectedAttachmentType

                                        messageInput = ""
                                        selectedAttachmentUri = null
                                        selectedAttachmentName = null
                                        selectedAttachmentType = null

                                        chatViewModel.sendMessage(textToSend, uriToSend, nameToSend, typeToSend)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Gửi tin nhắn",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Advanced Multi-Service Configuration Dialog
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    text = "Cấu hình AI Engine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Chọn cấu hình và động cơ máy chủ gửi tin nhắn:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    // Tab Selector style buttons (Chips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedServiceType == "huggingface",
                            onClick = { selectedServiceType = "huggingface" },
                            label = { Text("Hugging Face", fontSize = 11.sp) },
                            leadingIcon = if (selectedServiceType == "huggingface") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = selectedServiceType == "nvidia",
                            onClick = { selectedServiceType = "nvidia" },
                            label = { Text("NVIDIA NIM", fontSize = 11.sp) },
                            leadingIcon = if (selectedServiceType == "nvidia") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF76B900).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF3B5D00)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    if (selectedServiceType == "huggingface") {
                        // Hugging Face configuration view
                        Text(
                            text = "Hugging Face Endpoint URL:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = inputUrlText,
                            onValueChange = { inputUrlText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            label = { Text("API Endpoint URL") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Button(
                            onClick = { inputUrlText = "https://nhut0902-chatbotai.hf.space/chat" },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Khôi phục mặc định", fontSize = 11.sp)
                        }
                    } else {
                        // Nvidia NIM configuration view
                        Text(
                            text = "NVIDIA NIM API Key:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = inputApiKeyText,
                            onValueChange = { inputApiKeyText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            label = { Text("API Key (nvapi-...)") },
                            singleLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { 
                                    inputApiKeyText = "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" 
                                    Toast.makeText(context, "Đã khôi phục API Key mặc định của hệ thống", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Khôi phục API Key mặc định", fontSize = 10.sp)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Chọn mô hình NVIDIA AI NIM:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        val nimModels = listOf(
                            "google/gemma-2-27b-it" to "Gemma 4 (31B) Thế Hệ Mới",
                            "moonshotai/k2.5-thinking" to "Kimi K2.5 Thinking (Logic Suy Luận)",
                            "deepseek-ai/deepseek-v3" to "DeepSeek-V3 / 3.2 (Toán & Lập Trình)",
                            "nvidia/nemotron-4-340b-instruct" to "Nemotron-4 340B (Tác Nhân AI)",
                            "thm/glm-4-9b-chat" to "GLM 5.1 / MiniMax M2.7 (Đa Ngôn Ngữ)",
                            "meta/llama-3.1-70b-instruct" to "Llama 3.1 70B (Mạnh Mẽ)",
                            "meta/llama-3.1-8b-instruct" to "Llama 3.1 8B (Nhanh Chóng)",
                            "qwen/qwen-vl-max" to "Qwen-Image / Edit (Thị Giác Máy)",
                            "nvidia/llama-3.1-70b-instruct-vl" to "Llama 3.1 Vision (Đa Phương Thức)",
                            "personaplex-7b-v1" to "PersonaPlex 7B v1 (Đàm Thoại)",
                            "nvidia/parakeet-ctc-0.6b" to "Parakeet-CTC 0.6B (Nhận Diện Giọng Nói)"
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp) // Let it scroll when too many models exist
                                .verticalScroll(rememberScrollState())
                        ) {
                            nimModels.forEach { (modelId, label) ->
                                val isSelected = selectedNvidiaModel == modelId && customModelInput.trim().isEmpty()
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedNvidiaModel = modelId
                                            customModelInput = ""
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF76B900).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    border = if (isSelected) BorderStroke(1.dp, Color(0xFF76B900)) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedNvidiaModel = modelId
                                                customModelInput = ""
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF76B900)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(modelId, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Custom model name field
                        OutlinedTextField(
                            value = customModelInput,
                            onValueChange = {
                                customModelInput = it
                                if (it.trim().isNotEmpty()) {
                                    selectedNvidiaModel = it.trim()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nhập mô hình tùy chỉnh khác", fontSize = 10.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.updateServiceType(selectedServiceType)
                        if (selectedServiceType == "nvidia") {
                            val apiKeyToSave = if (inputApiKeyText.trim().isNotEmpty()) inputApiKeyText.trim() else "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"
                            chatViewModel.updateNvidiaApiKey(apiKeyToSave)
                            
                            val modelToSave = if (customModelInput.trim().isNotEmpty()) customModelInput.trim() else selectedNvidiaModel
                            chatViewModel.updateNvidiaModel(modelToSave)
                            Toast.makeText(context, "Đã lưu cấu hình NVIDIA NIM!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (inputUrlText.trim().isNotEmpty()) {
                                chatViewModel.updateApiUrl(inputUrlText)
                                Toast.makeText(context, "Đã lưu cấu hình Hugging Face mới!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showConfigDialog = false
                    }
                ) {
                    Text("Lưu cấu hình")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// Custom Markdown Text representation helper Composable
@Composable
fun MarkdownText(
    text: String,
    color: Color
) {
    val annotatedString = buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val codeIndexStart = text.indexOf("`", cursor)
            if (codeIndexStart != -1) {
                // Append previous plain text
                appendPlainAndBoldText(text.substring(cursor, codeIndexStart))
                
                val codeIndexEnd = text.indexOf("`", codeIndexStart + 1)
                if (codeIndexEnd != -1) {
                    // Span a nice monospace block
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = color.copy(alpha = 0.15f),
                            color = if (color == Color.White) Color(0xFFFFEB3B) else Color(0xFFE91E63) // vibrant contrast
                        )
                    ) {
                        append(text.substring(codeIndexStart + 1, codeIndexEnd))
                    }
                    cursor = codeIndexEnd + 1
                } else {
                    appendPlainAndBoldText(text.substring(codeIndexStart))
                    cursor = text.length
                }
            } else {
                appendPlainAndBoldText(text.substring(cursor))
                cursor = text.length
            }
        }
    }

    SelectionContainer {
        Text(
            text = annotatedString,
            color = color,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}

private fun AnnotatedString.Builder.appendPlainAndBoldText(chunk: String) {
    var cursor = 0
    while (cursor < chunk.length) {
        val boldIndexStart = chunk.indexOf("**", cursor)
        if (boldIndexStart != -1) {
            append(chunk.substring(cursor, boldIndexStart))
            val boldIndexEnd = chunk.indexOf("**", boldIndexStart + 2)
            if (boldIndexEnd != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(chunk.substring(boldIndexStart + 2, boldIndexEnd))
                }
                cursor = boldIndexEnd + 2
            } else {
                append(chunk.substring(boldIndexStart))
                cursor = chunk.length
            }
        } else {
            append(chunk.substring(cursor))
            cursor = chunk.length
        }
    }
}

// Beautiful AI bouncing dots loading indicator Composable
@Composable
fun ThinkingDots(dotColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "Thinking")
    
    @Composable
    fun animateDotOffset(delayMillis: Int): Float {
        val duration = 800
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -10f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = duration
                    0f at 0
                    -8f at (duration / 4) with FastOutSlowInEasing
                    0f at (duration / 2) with FastOutSlowInEasing
                    0f at duration
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delayMillis)
            ),
            label = "Dot"
        ).value
    }

    val offset1 = animateDotOffset(0)
    val offset2 = animateDotOffset(150)
    val offset3 = animateDotOffset(300)

    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Trợ lý đang suy nghĩ", modifier = spacerBefore(), fontSize = 12.sp, fontStyle = FontStyle.Italic, color = dotColor)
                                        Box(modifier = Modifier.size(6.dp).offset(y = offset1.dp).clip(CircleShape).background(dotColor))
        Box(modifier = Modifier.size(6.dp).offset(y = offset2.dp).clip(CircleShape).background(dotColor))
        Box(modifier = Modifier.size(6.dp).offset(y = offset3.dp).clip(CircleShape).background(dotColor))
    }
}

private fun spacerBefore(): Modifier = Modifier.padding(end = 4.dp)

private fun getFileNameHelper(context: android.content.Context, uri: android.net.Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "file"
}
