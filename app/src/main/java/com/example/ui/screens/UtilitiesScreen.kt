package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloadedStates by viewModel.downloadedUtilities.collectAsStateWithLifecycle()
    
    var activeUtilId by remember { mutableStateOf<String?>(null) } // null, "calendar", "calculator"

    // Simulating download progress states
    var isDownloadingCalendar by remember { mutableStateOf(false) }
    var calendarProgress by remember { mutableFloatStateOf(0f) }
    var calendarStatusText by remember { mutableStateOf("") }

    var isDownloadingCalculator by remember { mutableStateOf(false) }
    var calculatorProgress by remember { mutableFloatStateOf(0f) }
    var calculatorStatusText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    if (activeUtilId == "calendar") {
        CalendarView(onBack = { activeUtilId = null })
    } else if (activeUtilId == "calculator") {
        CalculatorView(onBack = { activeUtilId = null })
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Intro
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hệ thống Tiện ích Đa năng",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Các ứng dụng tiện ích hữu dụng tích hợp bổ sung. Tải về lưu trữ nếu cần hoặc gỡ cài đặt để giải phóng bộ nhớ tùy ý.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "CÁC TIỆN ÍCH HIỆN CÓ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Utility 1: Calculator
            val calcInstalled = downloadedStates["calculator"] ?: false
            UtilityItemCard(
                title = "Máy tính bỏ túi Đa năng",
                description = "Máy tính cá nhân xử lý các phép toán số học cộng, trừ, nhân, chia, lưu trữ lịch sử tính toán và giao diện phím bấm trực quan sắc nét.",
                icon = Icons.Default.Calculate,
                sizeLabel = "1.2 MB",
                isInstalled = calcInstalled,
                isDownloading = isDownloadingCalculator,
                progress = calculatorProgress,
                statusText = calculatorStatusText,
                onDownload = {
                    coroutineScope.launch {
                        isDownloadingCalculator = true
                        calculatorProgress = 0f
                        calculatorStatusText = "Đang kết nối Cloud..."
                        delay(600)
                        
                        calculatorStatusText = "Đang tải gói cài đặt (1.2 MB)..."
                        while (calculatorProgress < 0.7f) {
                            calculatorProgress += 0.1f + (Math.random() * 0.15f).toFloat()
                            if (calculatorProgress > 0.7f) calculatorProgress = 0.7f
                            delay(250)
                        }
                        
                        calculatorStatusText = "Đang giải nén tài nguyên..."
                        delay(500)
                        calculatorProgress = 0.85f
                        delay(400)
                        
                        calculatorStatusText = "Đang cài đặt thư viện..."
                        calculatorProgress = 1.0f
                        delay(500)
                        
                        viewModel.setUtilityDownloaded("calculator", true)
                        isDownloadingCalculator = false
                        Toast.makeText(context, "Đã cài đặt Máy tính thành công!", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpen = { activeUtilId = "calculator" },
                onUninstall = {
                    viewModel.setUtilityDownloaded("calculator", false)
                    Toast.makeText(context, "Đã gỡ bỏ Máy tính!", Toast.LENGTH_SHORT).show()
                }
            )

            // Utility 2: Calendar
            val calInstalled = downloadedStates["calendar"] ?: false
            UtilityItemCard(
                title = "Lịch vạn niên & Nhắc nhở",
                description = "Tra cứu ngày tháng dương lịch, ngày âm lịch Việt Nam, các ngày lễ tết, xem giờ hoàng đạo và hỗ trợ ghi chú nhắc nhở công việc trực tiếp cực kỳ tiện ích.",
                icon = Icons.Default.CalendarMonth,
                sizeLabel = "1.8 MB",
                isInstalled = calInstalled,
                isDownloading = isDownloadingCalendar,
                progress = calendarProgress,
                statusText = calendarStatusText,
                onDownload = {
                    coroutineScope.launch {
                        isDownloadingCalendar = true
                        calendarProgress = 0f
                        calendarStatusText = "Đang kết nối máy chủ..."
                        delay(700)
                        
                        calendarStatusText = "Đang tải dữ liệu lịch (1.8 MB)..."
                        while (calendarProgress < 0.65f) {
                            calendarProgress += 0.08f + (Math.random() * 0.12f).toFloat()
                            if (calendarProgress > 0.65f) calendarProgress = 0.65f
                            delay(250)
                        }
                        
                        calendarStatusText = "Bố cục lịch âm dương..."
                        delay(400)
                        calendarProgress = 0.82f
                        delay(400)
                        
                        calendarStatusText = "Hoàn tất cài đặt..."
                        calendarProgress = 1.0f
                        delay(500)
                        
                        viewModel.setUtilityDownloaded("calendar", true)
                        isDownloadingCalendar = false
                        Toast.makeText(context, "Đã cài đặt Lịch vạn niên thành công!", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpen = { activeUtilId = "calendar" },
                onUninstall = {
                    viewModel.setUtilityDownloaded("calendar", false)
                    Toast.makeText(context, "Đã gỡ bỏ Lịch vạn niên!", Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

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
        modifier = Modifier.fillMaxWidth(),
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
                            overflow = TextOverflow.Ellipsis
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
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
                    Text("Tải xuống & sử dụng miễn phí")
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
                // Calculator History summary (last 2 formulas)
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
        
        // Split with precedence or simple left-to-right evaluator
        // Support floating numbers
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
        
        // Multiplications and divisions first
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

        // Additions and subtractions
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

        // Standard integer display or decimal values
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

    // Month headers
    val vietnameseMonths = listOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )

    // Calendar generation
    val daysInMonth = remember(month, year) {
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(month, year) {
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        // Saturday/Sunday localized alignment
        // In Calendar, SUNDAY=1, MONDAY=2... We map Monday to start at 0, Sunday to 6
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
        // App bar back button
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
            
            // Current Year Label
            Text(
                text = "$year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Calendar Month Controller
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

        // Grid of weekdays headings (T2 -> CN)
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

        // Calendar days cells content
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
                                    // Simulated simple Lunar date subtext (beautiful touch)
                                    val lunarDay = (dayVal + 3) % 30 + 1
                                    Text(
                                        text = "$lunarDay",
                                        fontSize = 8.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else Color.Gray
                                    )
                                }
                            }
                        } else {
                            // Blank cell
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Details & Custom persistence Note
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header details
                Text(
                    text = "Ngày lễ & Ngày hoàng đạo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, sizeLabelTextSize(), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ngày $selectedDay tháng ${month + 1} năm $year",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Vietnamese Lunar zodiac day description simulation
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
                    color = Color(0xFF2E7D32), // Green label for Zodiac details
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Reminders / Note taking
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

@Composable
fun sizeLabelTextSize() = Modifier.size(20.dp)
