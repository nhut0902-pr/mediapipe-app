package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.ChatMessageItem
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotAiView(
    onBack: () -> Unit
) {
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val activeMessages by chatViewModel.activeMessages.collectAsStateWithLifecycle()
    val isGenerating by chatViewModel.isGenerating.collectAsStateWithLifecycle()
    val apiUrl by chatViewModel.apiUrl.collectAsStateWithLifecycle()
    val isDarkTheme by chatViewModel.isDarkTheme.collectAsStateWithLifecycle()

    var showConfigDialog by remember { mutableStateOf(false) }
    var inputUrlText by remember { mutableStateOf(apiUrl) }
    var messageInput by remember { mutableStateOf("") }

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
        modifier = Modifier.fillMaxSize(),
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
                                    text = "Động cơ máy chủ nội bộ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
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
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardAiBg),
                                    modifier = Modifier.clickable {
                                        inputUrlText = apiUrl
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
                                                    MarkdownText(text = item.text, color = contentColor)
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
                                    .padding(horizontal = 8.dp),
                                maxLines = 4
                            )

                            // Animated send action button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (messageInput.trim().isNotEmpty() && !isGenerating) cardUserBg else Color.Gray.copy(alpha = 0.3f)
                                    )
                                    .clickable(enabled = messageInput.trim().isNotEmpty() && !isGenerating) {
                                        val textToSend = messageInput
                                        messageInput = ""
                                        chatViewModel.sendMessage(textToSend)
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

    // Dynamic Server URL settings pop up dialog
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    text = "Cấu hình AI Endpoint",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Vui lòng nhập Link API backend của bạn. Chatbot sẽ POST dữ liệu trực tiếp đến địa chỉ này.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputUrlText,
                        onValueChange = { inputUrlText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        label = { Text("Base URL hoặc Absolute URL") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { inputUrlText = "https://nhut0902-chatbotai.hf.space/chat" },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Khôi phục mặc định", fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrlText.trim().isNotEmpty()) {
                            chatViewModel.updateApiUrl(inputUrlText)
                            Toast.makeText(context, "Đã lưu cấu hình endpoint mới!", Toast.LENGTH_SHORT).show()
                        }
                        showConfigDialog = false
                    }
                ) {
                    Text("Lưu cấu hình")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Đóng")
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
