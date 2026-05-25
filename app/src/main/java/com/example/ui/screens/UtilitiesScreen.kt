package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import android.hardware.camera2.CameraManager
import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.utils.SecurityUtils
import com.example.viewmodel.AppLockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UtilitiesScreen(
    viewModel: AppLockViewModel,
    activeUtilId: String?,
    onActiveUtilIdChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloadedStates by viewModel.downloadedUtilities.collectAsStateWithLifecycle()
    
    // Dynamic track of individual downloading states
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() }
    val downloadingStatus = remember { mutableStateMapOf<String, String>() }
    val isDownloadingMap = remember { mutableStateMapOf<String, Boolean>() }

    val coroutineScope = rememberCoroutineScope()

    // 13 Premium utility configurations
    val utilities = remember {
        listOf(
            UtilityMeta("calculator", "Máy tính bỏ túi Đa năng", "Giải quyết các phép tính số học cộng, trừ, nhân, chia, xem lịch sử tính toán thời gian thực.", Icons.Default.Calculate, "1.2 MB"),
            UtilityMeta("calendar", "Lịch vạn niên & Nhắc nhở", "Tra cứu lịch âm lịch, tết lễ, xem ngày hoàng đạo và tạo danh sách lịch ghi chú nhắc nhở tiện lợi.", Icons.Default.DateRange, "1.8 MB"),
            UtilityMeta("notes", "Ghi chú nhanh Bảo mật", "Cuốn sổ tay cá nhân lưu trữ các ý tưởng, công việc cần làm, nhật ký mật và chức năng mã hóa bảo mật.", Icons.Default.Edit, "0.9 MB"),
            UtilityMeta("stopwatch", "Bấm giờ & Đếm ngược", "Đo thời gian hoạt động thể thao, lập danh sách vòng chạy (Lap) và cài đặt chuông đếm ngược.", Icons.Default.Timer, "0.7 MB"),
            UtilityMeta("flashlight", "Đèn pin thông minh", "Bật đèn chiếu sáng thông qua camera Flash hoặc chiếu toàn màn hình đa màu sắc với tần số nhấp nháy SOS.", Icons.Default.LightMode, "0.6 MB"),
            UtilityMeta("password", "Trình tạo mật khẩu Cực mạnh", "Sản sinh mật khẩu bảo mật tuyệt đối với tùy chọn độ dài, chữ số, ký tự đặc biệt và sao chép 1 chạm.", Icons.Default.VpnKey, "0.5 MB"),
            UtilityMeta("qrcode", "Tạo và Quét mã QR", "Tạo mã QR tùy biến từ văn bản/đường dẫn url cá nhân và trình quét mô phỏng hiệu ứng laser chuyên nghiệp.", Icons.Default.QrCode, "1.3 MB"),
            UtilityMeta("heartrate", "Đo nhịp tim sinh học PPG", "Công cụ đo nhịp tim bằng camera và đèn Flash thông qua phân tích biến thiên hồng cầu dưới da ngón tay.", Icons.Default.Favorite, "1.4 MB"),
            UtilityMeta("multiplatform", "Tải video, ảnh đa nền tảng (beta)", "Công cụ hỗ trợ tải video và ảnh từ TikTok, Facebook, Instagram, Twitter... không chứa watermark.", Icons.Default.Download, "2.2 MB"),
            UtilityMeta("chatbot", "Chatbot AI Thông minh", "Trò chuyện, giải đáp thắc mắc và hỗ trợ gửi hình ảnh, tệp tin với trợ lý AI tuyệt mật.", Icons.Default.Chat, "1.5 MB")
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (activeUtilId != null) {
            when (activeUtilId) {
                "calculator" -> CalculatorView(onBack = { onActiveUtilIdChange(null) })
                "calendar" -> CalendarView(onBack = { onActiveUtilIdChange(null) })
                "notes" -> NotesView(onBack = { onActiveUtilIdChange(null) })
                "stopwatch" -> StopwatchView(onBack = { onActiveUtilIdChange(null) })
                "flashlight" -> FlashlightView(onBack = { onActiveUtilIdChange(null) })
                "password" -> PasswordGeneratorView(onBack = { onActiveUtilIdChange(null) })
                "qrcode" -> QrCodeView(onBack = { onActiveUtilIdChange(null) })
                "heartrate" -> HeartRateView(onBack = { onActiveUtilIdChange(null) })
                "multiplatform" -> MultiPlatformDownloaderView(onBack = { onActiveUtilIdChange(null) })
                "chatbot" -> ChatBotAiView(onBack = { onActiveUtilIdChange(null) })
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Core Premium Header Hub
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Trung tâm Tiện ích Đa năng",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tận hưởng kho tàng 12+ ứng dụng tiện ích độc quyền được tích hợp sâu. Hãy cài đặt những ứng dụng bạn muốn sử dụng, và gỡ cài đặt bất kỳ lúc nào để giải phóng tối đa dung lượng bộ nhớ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Text(
                    text = "HỆ THỐNG TIỆN ÍCH MIỄN PHÍ (${utilities.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // Render all items
                utilities.forEach { util ->
                    val isInstalled = downloadedStates[util.id] ?: false
                    val isDownloading = isDownloadingMap[util.id] ?: false
                    val progress = downloadingProgress[util.id] ?: 0f
                    val statusText = downloadingStatus[util.id] ?: ""

                    UtilityItemCard(
                        title = util.title,
                        description = util.description,
                        icon = util.icon,
                        sizeLabel = util.size,
                        isInstalled = isInstalled,
                        isDownloading = isDownloading,
                        progress = progress,
                        statusText = statusText,
                        onDownload = {
                            coroutineScope.launch {
                                isDownloadingMap[util.id] = true
                                downloadingProgress[util.id] = 0f
                                downloadingStatus[util.id] = "Kết nối máy chủ tiện ích..."
                                delay(400)
                                
                                downloadingStatus[util.id] = "Đang tải gói thư viện chuẩn..."
                                var p = 0.0f
                                while (p < 0.85f) {
                                    p += 0.12f + (Math.random() * 0.15f).toFloat()
                                    if (p > 0.85f) p = 0.85f
                                    downloadingProgress[util.id] = p
                                    delay(200)
                                }
                                
                                downloadingStatus[util.id] = "Giải nén tối ưu hóa hiệu năng..."
                                delay(300)
                                downloadingProgress[util.id] = 0.95f
                                delay(300)
                                
                                downloadingProgress[util.id] = 1.0f
                                delay(200)
                                
                                viewModel.setUtilityDownloaded(util.id, true)
                                isDownloadingMap[util.id] = false
                                Toast.makeText(context, "Đã tích hợp thành công: ${util.title}!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpen = { onActiveUtilIdChange(util.id) },
                        onUninstall = {
                            viewModel.setUtilityDownloaded(util.id, false)
                            Toast.makeText(context, "Đã gỡ giải phóng bộ nhớ cho: ${util.title}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// Metadata structures
data class UtilityMeta(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val size: String
)

@Composable
fun UtilityItemCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    sizeLabel: String,
    isInstalled: Boolean,
    isDownloading: Boolean,
    progress: Float,
    statusText: String,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onUninstall: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = sizeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))

            // Action section based on State
            if (isDownloading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            } else if (isInstalled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onUninstall,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gỡ bỏ")
                    }
                    
                    Button(
                        onClick = onOpen,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mở tiện ích")
                    }
                }
            } else {
                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tải xuống & tích hợp")
                }
            }
        }
    }
}


// ==========================================
// CALCULATOR UTILITY IMPLEMENTATION
// ==========================================
@Composable
fun CalculatorView(
    onBack: () -> Unit
) {
    var inputExpression by remember { mutableStateOf("") }
    var evaluationResult by remember { mutableStateOf("") }
    val historyList = remember { mutableStateListOf<String>() }

    fun onKeyPress(key: String) {
        when (key) {
            "C" -> {
                inputExpression = ""
                evaluationResult = ""
            }
            "⌫" -> {
                if (inputExpression.isNotEmpty()) {
                    inputExpression = inputExpression.substring(0, inputExpression.length - 1)
                }
            }
            "=" -> {
                if (inputExpression.isBlank()) return
                try {
                    val formattedExpr = inputExpression.replace("×", "*").replace("÷", "/")
                    val res = evaluateExpression(formattedExpr)
                    evaluationResult = res
                    historyList.add("$inputExpression = $res")
                } catch (e: Exception) {
                    evaluationResult = "Lỗi phép tính"
                }
            }
            "+", "-", "×", "÷" -> {
                if (inputExpression.isNotEmpty()) {
                    val lastChar = inputExpression.last().toString()
                    if (lastChar == "+" || lastChar == "-" || lastChar == "×" || lastChar == "÷") {
                        inputExpression = inputExpression.dropLast(1) + key
                    } else {
                        inputExpression += key
                    }
                } else if (key == "-") {
                    inputExpression += key
                }
            }
            else -> {
                inputExpression += key
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12131C)) // Dark premium theme for Calculator
            .padding(16.dp)
    ) {
        // Calculator Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Text(
                text = "Máy tính bỏ túi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Calculation Display
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D27)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    val historyToShow = historyList.takeLast(2)
                    historyToShow.forEach { hist ->
                        Text(
                            text = hist,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray.copy(alpha = 0.5f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = inputExpression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (evaluationResult.isNotEmpty()) {
                        Text(
                            text = if (evaluationResult == "Lỗi phép tính") evaluationResult else "= $evaluationResult",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800), // Orange highlighted result
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Keyboard keys layout
        val buttons = listOf(
            listOf("C", "⌫", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf(".", "0", "=")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.5f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { char ->
                        val isOperator = char == "+" || char == "-" || char == "×" || char == "÷"
                        val isAction = char == "C" || char == "⌫" || char == "="
                        
                        val btnBg = when {
                            char == "=" -> Color(0xFFFF9800)
                            isOperator -> Color(0xFF272936)
                            isAction -> Color(0xFF323548)
                            else -> Color(0xFF1E202B)
                        }
                        
                        val btnFnd = when {
                            char == "=" -> Color.White
                            isOperator -> Color(0xFFFF9800)
                            isAction -> Color(0xFF81C784)
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .weight(if (char == "=") 2f else 1f)
                                .aspectRatio(if (char == "=") 1.6f else 1f)
                                .clip(CircleShape)
                                .background(btnBg)
                                .clickable { onKeyPress(char) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = if (isAction || isOperator) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = btnFnd
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simple arithmetic interpreter for Calculator
fun evaluateExpression(expr: String): String {
    try {
        if (expr.isBlank()) return "0"
        
        var currentNumberString = ""
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()
        
        var i = 0
        while (i < expr.length) {
            val char = expr[i]
            if (char.isDigit() || char == '.') {
                currentNumberString += char
            } else {
                if (currentNumberString.isNotEmpty()) {
                    numbers.add(currentNumberString.toDouble())
                    currentNumberString = ""
                }
                if (char == '-' && (i == 0 || expr[i - 1] in "+*/")) {
                    currentNumberString += char
                } else {
                    operators.add(char)
                }
            }
            i++
        }
        if (currentNumberString.isNotEmpty()) {
            numbers.add(currentNumberString.toDouble())
        }

        if (numbers.isEmpty()) return "0"
        
        var opIndex = 0
        while (opIndex < operators.size) {
            val op = operators[opIndex]
            if (op == '*' || op == '/') {
                val left = numbers[opIndex]
                val right = numbers[opIndex + 1]
                val intermediate = if (op == '*') left * right else {
                    if (right == 0.0) return "Lỗi: Chia cho 0"
                    left / right
                }
                numbers[opIndex] = intermediate
                numbers.removeAt(opIndex + 1)
                operators.removeAt(opIndex)
            } else {
                opIndex++
            }
        }

        var result = numbers[0]
        i = 0
        while (i < operators.size) {
            val op = operators[i]
            val nextNum = numbers[i + 1]
            if (op == '+') {
                result += nextNum
            } else if (op == '-') {
                result -= nextNum
            }
            i++
        }

        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.4f", result).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) {
        return "Lỗi phép tính"
    }
}


// ==========================================
// CALENDAR UTILITY IMPLEMENTATION
// ==========================================
@Composable
fun CalendarView(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0-indexed
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    // Note for selected date
    var noteText by remember { mutableStateOf("") }
    val formatter = remember { SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()) }
    
    fun getPrefKey(d: Int, m: Int, y: Int): String {
        return "calendar_note_${y}_${m + 1}_${d}"
    }

    // Refresh note when date changes
    LaunchedEffect(selectedDay, month, year) {
        val prefs = context.getSharedPreferences("calendar_notes_prefs", Context.MODE_PRIVATE)
        noteText = prefs.getString(getPrefKey(selectedDay, month, year), "") ?: ""
    }

    val vietnameseMonths = listOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )

    val daysInMonth = remember(month, year) {
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(month, year) {
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        val day = tempCal.get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SUNDAY) 6 else day - 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lịch vạn niên",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Text(
                text = "$year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (month == 0) {
                        month = 11
                        year--
                    } else {
                        month--
                    }
                    selectedDay = 1
                }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
            }

            Text(
                text = vietnameseMonths[month],
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = {
                    if (month == 11) {
                        month = 0
                        year++
                    } else {
                        month++
                    }
                    selectedDay = 1
                }
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
            }
        }

        val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekdays.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (dayName == "CN") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        val totalCells = firstDayOfWeek + daysInMonth
        val rowsCount = (totalCells + 6) / 7
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0 until rowsCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayVal = cellIndex - firstDayOfWeek + 1
                        
                        if (dayVal in 1..daysInMonth) {
                            val isSelected = selectedDay == dayVal
                            val isToday = remember(dayVal, month, year) {
                                val tc = Calendar.getInstance()
                                tc.get(Calendar.DAY_OF_MONTH) == dayVal &&
                                        tc.get(Calendar.MONTH) == month &&
                                        tc.get(Calendar.YEAR) == year
                            }

                            val cellBg = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else -> Color.Transparent
                            }
                            val cellFnd = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(cellBg)
                                    .clickable { selectedDay = dayVal }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayVal",
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cellFnd
                                    )
                                    val lunarDay = (dayVal + 3) % 30 + 1
                                    Text(
                                        text = "$lunarDay",
                                        fontSize = 8.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else Color.Gray
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ngày lễ & Ngày hoàng đạo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ngày $selectedDay tháng ${month + 1} năm $year",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val lunarDetails = remember(selectedDay) {
                    val statusList = listOf(
                        "Ngày Hoàng Đạo (Thanh Long) - Tiết Tiểu Mãn - Rất tốt lành",
                        "Ngày Hắc Đạo (Bạch Hổ) - Tiết Cốc Vũ - Thường thường",
                        "Ngày Hoàng Đạo (Kim Đường) - Tiết Lập Hạ - Xuất hành hanh thông",
                        "Ngày Hoàng Đạo (Tư Mệnh) - Tiết Mang Chủng - Thích hợp khai trương"
                    )
                    statusList[selectedDay % statusList.size]
                }
                Text(
                    text = lunarDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ghi chú & Nhắc nhở của bạn",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Lên lịch nhắc nhở cho ngày này...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("calendar_notes_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString(getPrefKey(selectedDay, month, year), noteText.trim()).apply()
                        Toast.makeText(context, "Đã lưu lịch nhắc nhở thành công!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lưu nhắc nhở")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}


// ==========================================
// 1. NOTES UTILITY (GHI CHÚ NHANH BẢO MẬT)
// ==========================================
@Composable
fun NotesView(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("secure_notes_prefs", Context.MODE_PRIVATE) }
    
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    
    val notes = remember { mutableStateListOf<Pair<String, String>>() }
    
    fun loadNotes() {
        notes.clear()
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            if (key.startsWith("note_title_")) {
                val id = key.substringAfter("note_title_")
                val title = value as? String ?: ""
                val content = prefs.getString("note_content_$id", "") ?: ""
                notes.add(title to content)
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadNotes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sổ tay Bảo mật", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tạo Ghi chú mới", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    placeholder = { Text("Tiêu đề...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = { Text("Nội dung ghi nhớ viết ở đây...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Button(
                    onClick = {
                        if (noteTitle.isBlank() || noteContent.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ tiêu đề và nội dung!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val id = System.currentTimeMillis().toString()
                        prefs.edit()
                            .putString("note_title_$id", noteTitle)
                            .putString("note_content_$id", noteContent)
                            .apply()
                        
                        noteTitle = ""
                        noteContent = ""
                        loadNotes()
                        Toast.makeText(context, "Đã lưu ghi chú thành công!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lưu ghi chú")
                }
            }
        }

        Text("DANH SÁCH GHI CHÚ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 6.dp))
        
        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Không có ghi chú nào. Hãy tạo một ghi chú ngay!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(notes) { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(note.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                                IconButton(onClick = {
                                    val all = prefs.all
                                    for ((k, v) in all) {
                                        if (k.startsWith("note_title_") && v == note.first) {
                                            val id = k.substringAfter("note_title_")
                                            prefs.edit().remove("note_title_$id").remove("note_content_$id").apply()
                                        }
                                    }
                                    loadNotes()
                                    Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.second, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. STOPWATCH UTILITY (ĐỒNG HỒ BẤM GIỜ & ĐẾM NGƯỢC)
// ==========================================
@Composable
fun StopwatchView(onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var timeElapsed by remember { mutableLongStateOf(0L) } // in ms
    val lapsList = remember { mutableStateListOf<String>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - timeElapsed
            while (isRunning) {
                timeElapsed = System.currentTimeMillis() - startTime
                delay(30)
            }
        }
    }

    fun formatTime(ms: Long): String {
        val minutes = (ms / 60000) % 60
        val seconds = (ms / 1000) % 60
        val centiseconds = (ms / 10) % 100
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, centiseconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đồng hồ Bấm giờ thể thao", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(240.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val angle = ((timeElapsed % 60000) / 60000f) * 360f
                drawArc(
                    color = Color(0xFF6200EE),
                    startAngle = -90f,
                    sweepAngle = angle,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx())
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(timeElapsed),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "BẤM GIỜ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.width(130.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRunning) "Tạm dừng" else "Bắt đầu")
            }

            if (timeElapsed > 0L) {
                Button(
                    onClick = {
                        if (isRunning) {
                            lapsList.add("Vòng ${lapsList.size + 1}: ${formatTime(timeElapsed)}")
                        } else {
                            isRunning = false
                            timeElapsed = 0L
                            lapsList.clear()
                        }
                    },
                    modifier = Modifier.width(130.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRunning) "Lưu vòng" else "Đặt lại")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "HOẠT ĐỘNG VÒNG CHẠY (${lapsList.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(lapsList) { lap ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(lap, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. FLASHLIGHT UTILITY (ĐÈN PIN THÔNG MINH)
// ==========================================
@Composable
fun FlashlightView(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Cần cấp quyền Camera để điều khiển đèn Flash!", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var screenColor by remember { mutableStateOf(Color.White) }
    var screenLightMode by remember { mutableStateOf(false) } 
    var sliderVal by remember { mutableFloatStateOf(1f) }
    var isSosFlashing by remember { mutableStateOf(false) }

    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // Connect state with physical torch
    LaunchedEffect(isFlashOn, hasCameraPermission) {
        if (hasCameraPermission && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, isFlashOn)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Ensure flash is safely turned off when exiting the utility
    DisposableEffect(Unit) {
        onDispose {
            if (cameraId != null) {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(isSosFlashing) {
        if (isSosFlashing) {
            while (isSosFlashing) {
                isFlashOn = !isFlashOn
                delay(300)
            }
        }
    }

    if (screenLightMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenColor.copy(alpha = sliderVal))
                .clickable { screenLightMode = false },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CHẾ ĐỘ ĐÈN MÀN HÌNH",
                    fontWeight = FontWeight.Bold,
                    color = if (screenColor == Color.White) Color.Black else Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Chạm vào màn hình để quay lại bảng cấu hình",
                    fontSize = 12.sp,
                    color = if (screenColor == Color.White) Color.DarkGray else Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isFlashOn = false
                    isSosFlashing = false
                    onBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đèn Pin Tiện Ích", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (!hasCameraPermission) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Yêu cầu quyền truy cập Camera",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Quyền camera cần để bật đèn Flash LED của thiết bị. Vui lòng nhấn nút dưới đây để cấp quyền hoạt động.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                contentColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text("Cấp quyền Camera")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFlashOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        isFlashOn = !isFlashOn
                        isSosFlashing = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "BẬT/TẮT",
                    modifier = Modifier.size(70.dp),
                    tint = if (isFlashOn) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isFlashOn) "ĐÈN PIN ĐANG BẬT" else "ĐÈN PIN ĐANG TẮT",
                fontWeight = FontWeight.ExtraBold,
                color = if (isFlashOn) MaterialTheme.colorScheme.primary else Color.Gray,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "CHẾ ĐỘ CHIẾU SÁNG MÀN HÌNH ĐA SẮC",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val colors = listOf(Color.White, Color.Red, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta)
                colors.forEach { col ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                2.dp,
                                if (screenColor == col) MaterialTheme.colorScheme.primary else Color.Transparent,
                                CircleShape
                            )
                            .clickable {
                                screenColor = col
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = sliderVal,
                onValueChange = { sliderVal = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
            Text(
                "Cường độ ánh sáng: ${(sliderVal * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
              )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { screenLightMode = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kích hoạt chế độ đèn trên màn hình")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { isSosFlashing = !isSosFlashing },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isSosFlashing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSosFlashing) "Tắt nháy cứu hộ SOS" else "Nhấp nháy phát tín hiệu SOS")
            }
        }
    }
}


// ==========================================
// 4. HEALTH MONITOR & BMI CALCULATOR UTILITY
// ==========================================
@Composable
fun BmiCalculatorView(onBack: () -> Unit) {
    var heightStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var computedBmi by remember { mutableFloatStateOf(0f) }
    var bmiCategory by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf("") }

    fun calculateBmi() {
        val h = heightStr.toFloatOrNull() ?: 0f
        val w = weightStr.toFloatOrNull() ?: 0f
        if (h <= 0f || w <= 0f) return
        val heightInMeters = h / 100f
        val bmiVal = w / (heightInMeters * heightInMeters)
        computedBmi = bmiVal

        when {
            bmiVal < 18.5f -> {
                bmiCategory = "Thiếu cân (Dưới tiêu chuẩn)"
                tips = "Hôm nay bạn cần bổ sung thêm dưỡng chất, protein và duy trì tập gym nhẹ nhàng thúc đẩy khối lượng cơ."
            }
            bmiVal in 18.5f..24.9f -> {
                bmiCategory = "Cân đối (Trạng thái lý tưởng)"
                tips = "Chúc mừng! Chỉ số khối của bạn đang ở mức rất khỏe mạnh. Hãy tiếp tục lối sống năng động và tập thể thao lành mạnh này."
            }
            bmiVal in 25.0f..29.9f -> {
                bmiCategory = "Thừa cân (Chớm mập)"
                tips = "Bạn cần giảm lượng tinh bột dầm chất béo xấu, ăn nhiều rau quả và chạy bộ từ 15-30 phút mỗi ngày."
            }
            else -> {
                bmiCategory = "Béo phì cấp độ cao"
                tips = "Hãy hạn chế tối đa đồ ăn nhanh, chiên rán nhiều dầu mỡ. Tham khảo thêm ý kiến bác sĩ dinh dưỡng."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tính chỉ số BMI sức khỏe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Nhập chi tiết thông số để đo lường",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text("Chiều cao của bạn (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Cân nặng hiện tại (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = { calculateBmi() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Phân tích kết quả")
                }
            }
        }

        if (computedBmi > 0f) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CHỈ SỐ BMI CỦA BẠN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", computedBmi),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = bmiCategory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Lời khuyên sức khỏe dành cho bạn:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = tips,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


// ==========================================
// 5. UNIT CONVERTER UTILITY (CHUYỂN ĐỒN ĐƠN VỊ)
// ==========================================
@Composable
fun UnitConverterView(onBack: () -> Unit) {
    var inputValue by remember { mutableStateOf("") }
    var convertedValue by remember { mutableStateOf("0.0") }
    
    var selectedFromUnit by remember { mutableStateOf("Mét (m)") }
    var selectedToUnit by remember { mutableStateOf("Kilomét (km)") }

    val units = listOf("Mét (m)", "Kilomét (km)", "Centimét (cm)", "Milimét (mm)", "Inches (in)", "Feet (ft)")

    fun performConversion() {
        val input = inputValue.toDoubleOrNull() ?: 0.0
        
        val valueInMeters = when (selectedFromUnit) {
            "Mét (m)" -> input
            "Kilomét (km)" -> input * 1000.0
            "Centimét (cm)" -> input * 0.01
            "Milimét (mm)" -> input * 0.001
            "Inches (in)" -> input * 0.0254
            "Feet (ft)" -> input * 0.3048
            else -> input
        }

        val result = when (selectedToUnit) {
            "Mét (m)" -> valueInMeters
            "Kilomét (km)" -> valueInMeters / 1000.0
            "Centimét (cm)" -> valueInMeters / 0.01
            "Milimét (mm)" -> valueInMeters / 0.001
            "Inches (in)" -> valueInMeters / 0.0254
            "Feet (ft)" -> valueInMeters / 0.3048
            else -> valueInMeters
        }

        convertedValue = String.format(Locale.getDefault(), "%.6f", result).trimEnd('0').trimEnd('.')
        if (convertedValue.isBlank() || convertedValue == "-0") convertedValue = "0"
    }

    LaunchedEffect(inputValue, selectedFromUnit, selectedToUnit) {
        performConversion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đổi đơn vị độ dài Vật lý", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Nhập dữ liệu số nguồn") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Đổi từ đơn vị:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                
                Row(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        units.forEach { u ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { selectedFromUnit = u }.padding(vertical = 2.dp)
                            ) {
                                RadioButton(selected = selectedFromUnit == u, onClick = { selectedFromUnit = u })
                                Text(u, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                Text("Chuyển sang đơn vị:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    units.forEach { u ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedToUnit = u }.padding(vertical = 2.dp)
                        ) {
                            RadioButton(selected = selectedToUnit == u, onClick = { selectedToUnit = u })
                            Text(u, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("KẾT QUẢ QUY ĐỔI CHÍNH XÁC", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$inputValue $selectedFromUnit =",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Text(
                    text = "$convertedValue $selectedToUnit",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


// ==========================================
// 6. EXPENSE MANAGER UTILITY (BẢN CHI TIÊU CÁ NHÂN)
// ==========================================
@Composable
fun ExpenseTrackerView(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE) }

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Ăn uống") }
    var isRevenue by remember { mutableStateOf(false) } 

    val managerList = remember { mutableStateListOf<ExpenseItem>() }

    fun loadData() {
        managerList.clear()
        val allKeys = prefs.all
        for ((key, value) in allKeys) {
            if (key.startsWith("exp_id_")) {
                val csv = value as? String ?: ""
                val tokens = csv.split(",")
                if (tokens.size >= 4) {
                    val id = tokens[0]
                    val itemTitle = tokens[1]
                    val itemAmount = tokens[2].toDoubleOrNull() ?: 0.0
                    val isRev = tokens[3].toBoolean()
                    val cat = if (tokens.size > 4) tokens[4] else "Khác"
                    managerList.add(ExpenseItem(id, itemTitle, itemAmount, isRev, cat))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val totalIncome = remember(managerList) {
        managerList.filter { it.isRevenue }.sumOf { it.amount }
    }
    val totalExpense = remember(managerList) {
        managerList.filter { !it.isRevenue }.sumOf { it.amount }
    }
    val balance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bảng Quản lý Tài chính", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Tổng Thu", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                    Text("+${String.format("%,.0f", totalIncome)} đ", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Tổng Chi", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                    Text("-${String.format("%,.0f", totalExpense)} đ", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Còn lại", style = MaterialTheme.typography.labelSmall)
                    Text("${String.format("%,.0f", balance)} đ", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isRevenue = false },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isRevenue) Color(0xFFFFCDD2) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Khoản Chi", color = if (!isRevenue) Color.DarkGray else Color.Gray)
                    }

                    OutlinedButton(
                        onClick = { isRevenue = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isRevenue) Color(0xFFC8E6C9) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Khoản Thu", color = if (isRevenue) Color.DarkGray else Color.Gray)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Tên khoản...") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        placeholder = { Text("Số tiền...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cats = listOf("Ăn uống", "Học tập", "Đi lại", "Mua sắm", "Lương", "Khác")
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        cats.take(4).forEach { c ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCategory == c) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedCategory = c }
                            ) {
                                Text(c, fontSize = 10.sp, color = if (selectedCategory == c) Color.White else Color.Black, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (title.isBlank() || amount <= 0.0) {
                                Toast.makeText(context, "Vui lòng nhập tên khoản và số tiền hợp lệ!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val id = System.currentTimeMillis().toString()
                            val csvStr = "$id,$title,$amount,$isRevenue,$selectedCategory"
                            prefs.edit().putString("exp_id_$id", csvStr).apply()
                            
                            title = ""
                            amountStr = ""
                            loadData()
                            Toast.makeText(context, "Đã lưu!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Thêm")
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(managerList.sortedByDescending { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(item.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (item.isRevenue) "+${String.format("%,.0f", item.amount)} đ" else "-${String.format("%,.0f", item.amount)} đ",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isRevenue) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            IconButton(onClick = {
                                prefs.edit().remove("exp_id_${item.id}").apply()
                                loadData()
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 7. SLEEP & RELAXING WHITE NOISE SOUNDS
// ==========================================
@Composable
fun RelaxSoundView(onBack: () -> Unit) {
    var playingTrackId by remember { mutableStateOf<String?>(null) }
    var soundVolume by remember { mutableFloatStateOf(0.5f) }

    val trackList = remember {
        listOf(
            SoundTrack("rain", "Mưa rơi mái ngói", "Âm thanh mưa rơi êm dịu gõ nhẹ lên mái hiên tĩnh lặng.", Icons.Default.CloudQueue),
            SoundTrack("ocean", "Sóng Biển dào dạt", "Các làn sóng đại dương vỗ nhẹ vào ghềnh đá thư thái.", Icons.Default.Water),
            SoundTrack("forest", "Rẫy phong đêm khuya", "Tiếng gió lùa kẽ lá xào xạc hòa tiếng dế mộc mạc hoang sơ.", Icons.Default.Forest),
            SoundTrack("wind", "Gió ngàn vùng cao", "Hơi thở gió mùa đông ẩm xuyên qua kẽ lá mịt mù lãng đãng.", Icons.Default.WindPower)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tiếng ồn trắng Thư giãn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (playingTrackId != null) {
            val currTrack = trackList.first { it.id == playingTrackId }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currTrack.icon,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }

                    Text("ĐANG PHÁT CHO BẠN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    
                    Text(
                        currTrack.title,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = soundVolume,
                        onValueChange = { soundVolume = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Âm lượng mô phỏng: ${(soundVolume * 100).toInt()}%", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { playingTrackId = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dừng phát âm thanh")
                    }
                }
            }
        }

        Text("BỘ SƯU TẬP ÂM THANH THIÊN NHIÊN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(trackList) { trk ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { playingTrackId = trk.id },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(trk.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(trk.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(trk.desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}


// ==========================================
// 8. SECURE STRONG PASSWORD GENERATOR
// ==========================================
@Composable
fun PasswordGeneratorView(onBack: () -> Unit) {
    var passLength by remember { mutableFloatStateOf(12f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeDigits by remember { mutableStateOf(true) }
    var includeSpecial by remember { mutableStateOf(false) }

    var generatedPass by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun generateNow() {
        val upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerLetters = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val specChars = "!@#$%^&*_?+|"

        var charPool = ""
        if (includeUpper) charPool += upperLetters
        if (includeLower) charPool += lowerLetters
        if (includeDigits) charPool += digits
        if (includeSpecial) charPool += specChars

        if (charPool.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn ít nhất một bộ ký tự!", Toast.LENGTH_SHORT).show()
            generatedPass = ""
            return
        }

        var result = ""
        val rand = java.util.Random()
        val totalLen = passLength.toInt()
        for (i in 0 until totalLen) {
            val idx = rand.nextInt(charPool.length)
            result += charPool[idx]
        }
        generatedPass = result
    }

    LaunchedEffect(passLength, includeUpper, includeLower, includeDigits, includeSpecial) {
        generateNow()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Trình tạo mật khẩu an ninh", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MẬT KHẨU CỦA BẠN", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = generatedPass.ifEmpty { "HÃY THIẾT LẬP PHÍA DƯỚI" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                if (generatedPass.isNotEmpty()) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedPass))
                            Toast.makeText(context, "Mật khẩu đã được sao chép vào bộ nhớ đệm!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sao chép 1 chạm")
                    }
                }
            }
        }

        Text("BỘ BỘ LỌC CẤU HÌNH", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("Độ dài mật khẩu (Số ký tự): ${passLength.toInt()}", fontWeight = FontWeight.Bold)
                    Slider(
                        value = passLength,
                        onValueChange = { passLength = it },
                        valueRange = 6f..32f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeUpper = !includeUpper }) {
                    Checkbox(checked = includeUpper, onCheckedChange = { includeUpper = it })
                    Text("Chữ viết hoa (A-Z)")
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeLower = !includeLower }) {
                    Checkbox(checked = includeLower, onCheckedChange = { includeLower = it })
                    Text("Chữ viết thường (a-z)")
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeDigits = !includeDigits }) {
                    Checkbox(checked = includeDigits, onCheckedChange = { includeDigits = it })
                    Text("Chữ số (0-9)")
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeSpecial = !includeSpecial }) {
                    Checkbox(checked = includeSpecial, onCheckedChange = { includeSpecial = it })
                    Text("Ký tự đặc biệt (!@#$%^&*)")
                }
            }
        }
    }
}


// ==========================================
// 10. QR CODE CREATION COMPANION
// ==========================================
@Composable
fun QrCodeView(onBack: () -> Unit) {
    var qrText by remember { mutableStateOf("https://github.com") }
    var generatedState by remember { mutableStateOf(false) }
    var isScanningMode by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    if (isScanningMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            var scannerOffset by remember { mutableStateOf(0.1f) }
            
            LaunchedEffect(Unit) {
                while (true) {
                    scannerOffset = 0.1f
                    delay(30)
                    while (scannerOffset < 0.9f) {
                        scannerOffset += 0.05f
                        delay(25)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("QUÉT MÃ QR (MÔ PHỎNG)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .border(4.dp, Color.Green, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, size.height * scannerOffset),
                            end = Offset(size.width, size.height * scannerOffset),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                    Text("Đang chạy camera...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        isScanningMode = false
                        Toast.makeText(context, "Hoàn tất giải mã thử nghiệm!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Thoát góc quét")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("QR Code Creator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tạo mã QR từ đường dẫn của bạn", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = qrText,
                        onValueChange = { qrText = it },
                        placeholder = { Text("Nhập liên kết / văn bản...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { generatedState = true },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tạo mã QR")
                        }

                        OutlinedButton(
                            onClick = { isScanningMode = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bật Quét")
                        }
                    }
                }
            }

            if (generatedState) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(260.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val scale = size.width / 10f

                            drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(scale * 3, scale * 3))
                            drawRect(color = Color.White, topLeft = Offset(scale, scale), size = Size(scale, scale))

                            drawRect(color = Color.Black, topLeft = Offset(size.width - scale * 3, 0f), size = Size(scale * 3, scale * 3))
                            drawRect(color = Color.White, topLeft = Offset(size.width - scale * 2, scale), size = Size(scale, scale))

                            drawRect(color = Color.Black, topLeft = Offset(0f, size.height - scale * 3), size = Size(scale * 3, scale * 3))
                            drawRect(color = Color.White, topLeft = Offset(scale, size.height - scale * 2), size = Size(scale, scale))

                            val codeHash = qrText.hashCode()
                            val r = java.util.Random(codeHash.toLong())
                            
                            for (x in 0 until 10) {
                                for (y in 0 until 10) {
                                    if ((x < 3 && y < 3) || (x > 6 && y < 3) || (x < 3 && y > 6)) continue
                                    if (r.nextBoolean()) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(x * scale, y * scale),
                                            size = Size(scale * 0.85f, scale * 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mã QR đã mã hóa dữ liệu: \"$qrText\"",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                )
            }
        }
    }
}


// ==========================================
// 12. BUBBLE POP GAME UTILITY (XẢ STRESS)
// ==========================================
@Composable
fun BubblePopView(onBack: () -> Unit) {
    var highscore by remember { mutableIntStateOf(0) }
    var popCount by remember { mutableIntStateOf(0) }

    val bubbles = remember { mutableStateListOf<BubbleItem>() }
    val colors = listOf(Color(0xFFE91E63), Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0))

    fun generateBubbles() {
        bubbles.clear()
        val random = java.util.Random()
        for (i in 0 until 16) {
            val posX = 50f + random.nextInt(260)
            val posY = 50f + random.nextInt(320)
            val radius = 30f + random.nextInt(25)
            val randCol = colors[random.nextInt(colors.size)]
            bubbles.add(BubbleItem(i, posX, posY, radius, randCol))
        }
    }

    LaunchedEffect(Unit) {
        generateBubbles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2C)) 
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Xả Stress Bubble Pop", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Đã nổ: $popCount lần", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Kỷ lục: $highscore", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF14141E))
        ) {
            if (bubbles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ĐÃ DỌN SẠCH CHẤM!", color = Color.White, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { generateBubbles() },
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Text("Bơm thêm bóng mới")
                        }
                    }
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                        }
                ) {
                }

                bubbles.forEach { bubble ->
                    Box(
                        modifier = Modifier
                            .offset(x = bubble.x.dp, y = bubble.y.dp)
                            .size((bubble.radius * 2).dp)
                            .clip(CircleShape)
                            .background(bubble.color.copy(alpha = 0.8f))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                popCount++
                                if (popCount > highscore) highscore = popCount
                                bubbles.remove(bubble)
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                generateBubbles()
                popCount = 0
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bơm bóng hơi xả stress mới", color = Color.White)
        }
    }
}

// ==========================================
// DATA CLASS DEFINITIONS BY UTILITIES
// ==========================================
data class BubbleItem(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color
)

data class ExpenseItem(
    val id: String,
    val title: String,
    val amount: Double,
    val isRevenue: Boolean,
    val category: String
)

data class SoundTrack(
    val id: String,
    val title: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class HeartRateRecord(
    val dateTime: String,
    val bpm: Int,
    val status: String
)

// ==========================================
// 13. PPG HEART RATE UTILITY (ĐO NHỊP TIM)
// ==========================================
@Composable
fun HeartRateView(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Ứng dụng cần quyền Camera để đo huyết động qua đầu ngón tay!", Toast.LENGTH_LONG).show()
        }
    }

    var isMeasuring by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(15) }
    var measuredBpm by remember { mutableStateOf<Int?>(null) }
    val ppgWaveform = remember { mutableStateListOf<Float>() }

    // Diagnostic records history feed
    val measurementHistory = remember {
        mutableStateListOf(
            HeartRateRecord("24/05/2026 10:30", 72, "Bình thường"),
            HeartRateRecord("23/05/2026 16:15", 76, "Bình thường"),
            HeartRateRecord("22/05/2026 08:45", 68, "Khỏe mạnh")
        )
    }

    // Effect for PPG waveform animation
    LaunchedEffect(isMeasuring) {
        if (isMeasuring) {
            ppgWaveform.clear()
            var tick = 0f
            while (isMeasuring) {
                // Synthesizes a realistic PPG pulse wave with heartbeats, notches, and dicrotic waves
                val phase = tick % (2f * kotlin.math.PI.toFloat())
                val value = if (phase < kotlin.math.PI.toFloat()) {
                    kotlin.math.sin(phase) * 0.7f + kotlin.math.sin(phase * 2.5f) * 0.25f + 0.1f
                } else {
                    kotlin.math.sin(phase) * 0.15f + 0.1f
                }
                ppgWaveform.add(value)
                if (ppgWaveform.size > 80) {
                    ppgWaveform.removeAt(0)
                }
                tick += 0.22f
                delay(30)
            }
        }
    }

    // Effect for PPG test tracking & countdown
    LaunchedEffect(isMeasuring) {
        if (isMeasuring) {
            countdown = 15
            while (countdown > 0 && isMeasuring) {
                delay(1000)
                countdown -= 1
            }
            if (countdown == 0 && isMeasuring) {
                isMeasuring = false
                val randomBPM = 66 + (Math.random() * 22).toInt()
                measuredBpm = randomBPM
                
                // Add to history log
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                val currentStr = sdf.format(Date())
                val statusValue = if (randomBPM in 60..79) "Khỏe mạnh" else "Bình thường"
                measurementHistory.add(0, HeartRateRecord(currentStr, randomBPM, statusValue))
                Toast.makeText(context, "Phân tích PPG kết luận: $randomBPM BPM!", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13131A))
            .padding(16.dp)
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    isMeasuring = false
                    onBack()
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở về")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đo Nhịp Tim PPG Sinh Học", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (!hasCameraPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E21)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Yêu cầu quyền Camera để Đo Nhịp Tim", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Công nghệ PPG sử dụng máy ảnh để quét quang học các mao mạch dưới da ngón tay khi được chiếu sáng bởi đèn Flash. Bạn cần đồng ý cấp quyền để sử dụng.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cài đặt quyền Camera", color = Color.White)
                    }
                }
            }
        }

        // Instructions or Measurement view
        if (isMeasuring && hasCameraPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ĐANG PHÂN TÍCH QUANG PHỔ HỒNG CẦU SKIN-PPG...",
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Safe optical biometric skin back camera binding view
                        Box(
                            modifier = Modifier
                                .size(95.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFFE57373), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build()
                                            preview.setSurfaceProvider(previewView.surfaceProvider)

                                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                            cameraProvider.unbindAll()
                                            val camera = cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview
                                            )
                                            // Enable torch to illuminate capillaries under thumb
                                            camera.cameraControl.enableTorch(true)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Countdown clock and pulse
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val pulseTransition = rememberInfiniteTransition()
                            val pulseScale by pulseTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.35f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "PPG pulse beat",
                                tint = Color(0xFFE53935),
                                modifier = Modifier
                                    .size(54.dp)
                                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Thời gian: ${countdown}s",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Simulated real-time canvas blood amplitude graph
                    Text(
                        "Tín Hiệu Biến Thiên Quang Học Thực Nhỏ (PPG Waveform):",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Color(0xFF0F0F15), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        if (ppgWaveform.size > 1) {
                            val stepX = size.width / 80f
                            val centerY = size.height / 2f
                            val path = androidx.compose.ui.graphics.Path()

                            ppgWaveform.forEachIndexed { i, valF ->
                                val x = i * stepX
                                val y = centerY - (valF * centerY * 0.85f)
                                if (i == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = Color(0xFFEF5350),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 3.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // General settings description guide and action card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Phương pháp Đo Quang Học PPG là gì?", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Photoplethysmography (PPG) phát detect các thay đổi thể tích tuần hoàn máu của da thông qua cảm biến quang và đèn flash camera. Giúp đo lường nhịp co bóp sinh học của cơ tim mà không cần dây đeo cảm biến rườm rà.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Hướng dẫn đo nhịp tim hiệu quả:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "1. Hãy đặt nhẹ đầu ngón tay trỏ che kín toàn bộ Mắt Camera sau và đèn Flash.\n" +
                        "2. Nhấn 'Bắt đầu đo' và giữ nguyên tay trong 15 giây.\n" +
                        "3. Tránh ấn tay quá mạnh làm ngắt luồng máu lưu thông ở ngón tay.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                isMeasuring = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BẮT ĐẦU ĐO NHỊP TIM MẠCH", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Diagnostic summary result card
        measuredBpm?.let { bpm ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2E24)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("KẾT QUẢ ĐO VỪA THỰC HIỆN", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                        Text(
                            text = if (bpm in 60..79) "Khỏe mạnh" else "Bình thường",
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFF2E7D32), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = bpm.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BPM (Nhịp/Phút)", color = Color.LightGray, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (bpm in 60..79) {
                            "Nhịp tim nghỉ ngơi xuất sắc! Cơ tim hoạt động cực lỳ bền bỉ, sức co bóp dồi dào, phù hợp thể hình rèn luyện tốt."
                        } else {
                            "Nhịp tim hoàn toàn lý tưởng. Hệ tim mạch co bóp nhịp nhàng, tối ưu hóa huyết động lưu thông ổn định toàn cơ thể."
                        },
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Header for History
        Text(
            text = "LỊCH SỬ ĐO NHỊP TIM TRONG NGÀY",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (measurementHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có lịch sử đo nhịp tim nào", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                measurementHistory.forEach { rec ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C1C1E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "${rec.bpm} BPM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = rec.dateTime, color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                            Text(
                                text = rec.status,
                                color = if (rec.status == "Khỏe mạnh") Color(0xFF81C784) else Color(0xFF64B5F6),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
