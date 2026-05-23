package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GestureType
import com.example.model.LifeAppType
import com.example.viewmodel.GestureViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LifeAppsScreen(
    viewModel: GestureViewModel,
    modifier: Modifier = Modifier
) {
    val selectedApp by viewModel.selectedLifeApp.collectAsState()
    val appsList = LifeAppType.values()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Horizontal apps scrollbar
        Text(
            text = "CHỌN TIỆN ÍCH ĐỜI SỐNG KHÔNG CHẠM (11+ ỨNG DỤNG):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("life_apps_tab_row")
        ) {
            items(appsList) { app ->
                val isSelected = selectedApp == app
                Card(
                    modifier = Modifier
                        .testTag("app_tab_${app.id}")
                        .clickable { viewModel.selectLifeApp(app) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF141A2D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF1E2843))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = app.iconEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = app.title,
                            color = if (isSelected) Color.Black else Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info details of active app
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = Color(0xFFFFD700), contentColor = Color.Black) {
                        Text(text = selectedApp.applicationField, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedApp.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedApp.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B9FB4)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large viewport viewport rendering of selected app
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF070B14))
                .border(1.5.dp, Color(0xFF1D2640), RoundedCornerShape(18.dp))
                .testTag("app_window_viewport")
        ) {
            AnimatedContent(
                targetState = selectedApp,
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "ActiveAppAnim"
            ) { target ->
                when (target) {
                    LifeAppType.APP_LOCK -> AppLockView(viewModel)
                    LifeAppType.SPORTS_TRACKER -> SportsGymTrackerView(viewModel)
                    LifeAppType.SLIDE_PRESENTER -> SlidePresenterProView(viewModel)
                    LifeAppType.SMART_HOME -> SmartHomeHubView(viewModel)
                    LifeAppType.SPATIAL_PAINT -> SpatialPaintView(viewModel)
                    LifeAppType.CLAY_SCULPT -> ClaySculptingView(viewModel)
                    LifeAppType.MUSIC_CONDUCTOR -> MusicConductorView(viewModel)
                    LifeAppType.G_CALCULATOR -> GestureCalculatorView(viewModel)
                    LifeAppType.SOS_EMERGENCY -> SOSEmergencyView(viewModel)
                    LifeAppType.CHEF_MASTER -> ChefMasterView(viewModel)
                    LifeAppType.CARPAL_STRETCH -> CarpalStretchView(viewModel)
                }
            }
        }
    }
}

// ================== APPLOCK VAULT ==================
@Composable
fun AppLockView(viewModel: GestureViewModel) {
    val lockStatus by viewModel.ls1Status.collectAsState()
    val secretMsg by viewModel.ls1SecretMessage.collectAsState()
    val passSeq by viewModel.ls1GesturePassword.collectAsState()
    val pathProg by viewModel.ls1ProgressSequence.collectAsState()
    val feedbackMsg by viewModel.ls1FeedbackMessage.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        if (lockStatus == "UNLOCKED") Color(0xFF00FA9A).copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f),
                        RoundedCornerShape(50)
                    )
                    .border(2.dp, if (lockStatus == "UNLOCKED") Color(0xFF00FA9A) else Color(0xFFFF5252), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (lockStatus == "UNLOCKED") Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = "Lock state",
                    tint = if (lockStatus == "UNLOCKED") Color(0xFF00FA9A) else Color(0xFFFF5252),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (lockStatus == "UNLOCKED") "🔓 KHÓA ĐÃ ĐƯỢC GIẢI PHÓNG" else "🔒 PHÒNG LƯU TRỮ ĐANG KHÓA CHẶT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (lockStatus == "UNLOCKED") Color(0xFF00FA9A) else Color(0xFFFF5252)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sequence required indicators
            Text(text = "Chuỗi hình mật khẩu sinh trắc cần gõ:", color = Color(0xFF8B9FB4), fontSize = 11.sp)
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                passSeq.forEach { gesture ->
                    Text(text = "${gesture.emoji} ${gesture.vietnameseName}", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic progress sequence entered
            Text(
                text = feedbackMsg,
                color = Color(0xFFFFD700),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hidden secret payload card which animates sliding open!
            AnimatedVisibility(
                visible = lockStatus == "UNLOCKED",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1A33)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MailOutline, contentDescription = "Mail", tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "TÀI LIỆU RÒ RỈ CẤP QUỐC GIA", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = secretMsg, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }

            // Quick bypass buttons
            if (lockStatus != "UNLOCKED") {
                Button(
                    onClick = {
                        viewModel.ls1Status.value = "UNLOCKED"
                        viewModel.ls1FeedbackMessage.value = "✅ Mở khóa thành công qua chế độ giả lập đặc quyền!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2640)),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("BỎ QUA CAMERA KHÓA NHANH (BYPASS)", fontSize = 11.sp, color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.ls1Status.value = "LOCKED"
                        viewModel.ls1ProgressSequence.value = emptyList()
                        viewModel.ls1FeedbackMessage.value = "Màn hình đã được khóa lại hoàn tất."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("BẮT ĐẦU KHÓA LẠI TÀI KHOẢN 🔒")
                }
            }
        }
    }
}

// ================== SPORTS GYM TRACKER ==================
@Composable
fun SportsGymTrackerView(viewModel: GestureViewModel) {
    val count by viewModel.ls2Count.collectAsState()
    val exe by viewModel.ls2Exercise.collectAsState()
    val repState by viewModel.ls2TrackCycleState.collectAsState()
    val infoMsg by viewModel.ls2StatusMessage.collectAsState()

    val workoutsDetails = listOf("Bicep Curl", "Squats", "Hít đất")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "HỆ THỐNG HUẤN LUYỆN GYM RẢNH TAY",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Switch exercises
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                workoutsDetails.forEach { w ->
                    val isS = exe == w
                    Card(
                        modifier = Modifier.clickable { viewModel.ls2Exercise.value = w },
                        colors = CardDefaults.cardColors(containerColor = if (isS) Color(0xFFFFD700) else Color(0xFF1E2843))
                    ) {
                        Text(
                            text = w,
                            color = if (isS) Color.Black else Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Reps Display
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2540)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(140.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = exe, color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$count", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(text = "Reps", color = Color(0xFF8B9FB4), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Trạng thái nhịp giãn: $repState",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = infoMsg,
                color = Color(0xFF00FA9A),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action simulation
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.updateFingers(1f, 1f, 1f, 1f, 1f) // Fist = closed
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2943))
                ) {
                    Text("1. CO TAY (FIST)")
                }
                Button(
                    onClick = {
                        viewModel.updateFingers(0f, 0f, 0f, 0f, 0f) // Palm = open
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2943))
                ) {
                    Text("2. DUỖI TAY (PALM)")
                }
            }
        }
    }
}

// ================== SLIDES PRESENTER ==================
@Composable
fun SlidePresenterProView(viewModel: GestureViewModel) {
    val slideNum by viewModel.ls3ActiveSlide.collectAsState()
    val laserActive by viewModel.ls3LaserActive.collectAsState()
    val laserPos by viewModel.ls3LaserPosition.collectAsState()
    val slideText = viewModel.ls3Slides[slideNum]
    val feedbackMsg by viewModel.ls3Feedback.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "VIRTUAL SMART SCREEN BOARD", color = Color(0xFF8B9FB4), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // White PowerPoint Slide Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = slideText,
                        color = Color(0xFF12141F),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Laser pointer red circle dot
                if (laserActive) {
                    Box(
                        modifier = Modifier
                            .offset(x = (laserPos.x * 240).dp, y = (laserPos.y * 180).dp)
                            .size(12.dp)
                            .background(Color.Red, RoundedCornerShape(50))
                            .border(1.5.dp, Color.White, RoundedCornerShape(50))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Slide ${slideNum + 1}/${viewModel.ls3Slides.size} | $feedbackMsg",
                color = Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Next Prev Button manual overrides
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (slideNum > 0) viewModel.ls3ActiveSlide.value = slideNum - 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2843))
                ) {
                    Text("TRƯỚC ⏪")
                }
                Button(
                    onClick = {
                        if (slideNum < viewModel.ls3Slides.size - 1) viewModel.ls3ActiveSlide.value = slideNum + 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2843))
                ) {
                    Text("SAU ⏩")
                }
            }
        }
    }
}

// ================== SMART HOME CONTROL ==================
@Composable
fun SmartHomeHubView(viewModel: GestureViewModel) {
    val devices by viewModel.ls4Appliances.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BẢNG ĐIỀU RẢNH THỦY SMART HOME INFRARED",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Vuốt cử chỉ không chạm được đồng khóa với thiết bị:", color = Color(0xFF8B9FB4), fontSize = 11.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Grid layout of 4 appliances
        devices.forEach { dev ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (dev.isOn) Color(0xFF162E4F) else Color(0xFF131828)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (dev.isOn) Color(0xFFFFD700) else Color(0xFF1F2943)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.toggleSmartDevice(dev.id) }
                    .testTag("appliance_${dev.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dev.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = dev.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Bằng cử chỉ: ${dev.gestureMapped.emoji} ${dev.gestureMapped.vietnameseName}", color = Color(0xFF8B9FB4), fontSize = 11.sp)
                        }
                    }

                    Badge(containerColor = if (dev.isOn) Color(0xFF00FA9A) else Color(0xFFFF5252)) {
                        Text(
                            text = dev.value,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================== SPATIAL AIR PAINT ==================
@Composable
fun SpatialPaintView(viewModel: GestureViewModel) {
    val strokes by viewModel.ls5Strokes.collectAsState()
    val activeColor by viewModel.ls5ActiveBrushColor.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw grid guide path
            drawRect(
                color = Color(0xFF111E3A).copy(alpha = 0.5f),
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f)))
            )

            strokes.forEach { stroke ->
                val pList = stroke.points
                if (pList.size > 1) {
                    for (i in 0 until pList.size - 1) {
                        val pStart = Offset(pList[i].x * size.width, pList[i].y * size.height)
                        val pEnd = Offset(pList[i + 1].x * size.width, pList[i + 1].y * size.height)
                        drawLine(
                            color = stroke.color,
                            start = pStart,
                            end = pEnd,
                            strokeWidth = stroke.size,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Overlay Paint brush selectors
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.ls5Strokes.value = emptyList() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("XÓA BẢNG VẼ OK👌", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            listOf(Color(0xFF00E5FF), Color(0xFFFF1744), Color(0xFF00E676), Color(0xFFFFEA00)).forEach { color ->
                val isSel = activeColor == color
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                        .border(2.dp, if (isSel) Color.White else Color.Transparent, RoundedCornerShape(50))
                        .clickable { viewModel.ls5ActiveBrushColor.value = color }
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "🖌️ AIR PAINT NEON CANVAS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "Đổi cử chỉ chỉ thẳng ngón INDEX_POINT để vẽ nét dạ quang", color = Color(0xFF8B9FB4), fontSize = 10.sp)
        }
    }
}

// ================== CLAY MODE SCULPTING ==================
@Composable
fun ClaySculptingView(viewModel: GestureViewModel) {
    val scaleX by viewModel.ls6ClayScaleX.collectAsState()
    val scaleY by viewModel.ls6ClayScaleY.collectAsState()
    val smooth by viewModel.ls6ClaySmoothness.collectAsState()
    val artworkName by viewModel.ls6ClayOutputName.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "3D GOM VIRTUAL SCULPTOR AMBIENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Nạp cử chỉ PINCH🤏 co kéo tọa độ để biến đổi đất sét", color = Color(0xFF8B9FB4), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful shaded Clay drawing using circular canvas scale
            Canvas(
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Clay material draw
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF8552), Color(0xFF813110)),
                        center = Offset(cx - 15f, cy - 15f),
                        radius = 120f
                    ),
                    topLeft = Offset(cx - (80f * scaleX), cy - (80f * scaleY)),
                    size = Size(160f * scaleX, 160f * scaleY)
                )

                // Shading highlights for clay texture realism
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = 20f * smooth,
                    center = Offset(cx - (30f * scaleX), cy - (30f * scaleY))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Badge(containerColor = Color(0xFFFFD700), contentColor = Color.Black) {
                Text(
                    text = "BẢN PHÂN TÍCH THỂ TÍCH: $artworkName",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ================== VIRTUAL MUSIC CONDUCTOR ==================
@Composable
fun MusicConductorView(viewModel: GestureViewModel) {
    val bpm by viewModel.ls7TempoBPM.collectAsState()
    val dbVolume by viewModel.ls7VolumeDB.collectAsState()
    val sectionStatus by viewModel.ls7OrchestraInstruments.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(text = "🎻 NHẠC TRƯỞNG GIAO HƯỞNG COOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Lắc tay trái/phải điều tempo BPM | Cao thấp đổi âm lượng", color = Color(0xFF8B9FB4), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Circular Tempo Dial
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TEMPO (Tốc độ)", color = Color.White, fontSize = 11.sp)
                    Text(text = "$bpm BPM", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00FA9A))
                }

                // Volume slider
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "VOLUME (Âm lượng)", color = Color.White, fontSize = 11.sp)
                    Text(text = "$dbVolume %", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF4500))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive band rows
            Text(text = "DÀN NHẠC CỤ ĐANG SẴN SÀNG:", color = Color.White, fontSize = 11.sp)
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sectionStatus.keys.forEach { inst ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B223D)),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = inst,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================== SMART CALCULATION GESTURE ==================
@Composable
fun GestureCalculatorView(viewModel: GestureViewModel) {
    val n1 by viewModel.ls8Number1.collectAsState()
    val n2 by viewModel.ls8Number2.collectAsState()
    val op by viewModel.ls8Operator.collectAsState()
    val result by viewModel.ls8Result.collectAsState()
    val display by viewModel.ls8Display.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(text = "MÁY TÍNH TRÍ TUỆ KHÔNG CHẠM SMART_CALC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Board output
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10162A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = display,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FA9A),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Số 1: ${n1 ?: "_"}", color = Color.White, fontSize = 15.sp)
                        Text(text = op ?: " ", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Số 2: ${n2 ?: "_"}", color = Color.White, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input helper shortcuts
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = {
                    viewModel.ls8Number1.value = 5
                    viewModel.ls8Operator.value = "+"
                    viewModel.ls8Number2.value = 10
                    viewModel.ls8Display.value = "Đã nạp lẹ phép toán giả lập: 5 + 10! Click FIST để nhận kết."
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2B4B))) {
                    Text("NẠP MẪU QUAY ĐẦU (5 + 10)", fontSize = 10.sp)
                }
            }
        }
    }
}

// ================== EMERGENCY SOS ==================
@Composable
fun SOSEmergencyView(viewModel: GestureViewModel) {
    val prog by viewModel.ls9CountdownProgress.collectAsState()
    val sosT by viewModel.ls9SOSTriggered.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "⚠️ CHỐT BẢO HỘ COOP SOS ALARM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Nắm chặt Fist✊ trong 3 giây liên tục để hú còi cấp cứu khẩn", color = Color(0xFF8B9FB4), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(24.dp))

            val progressRatio = (prog / 3.0f).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(50))
                    .border(3.dp, if (sosT) Color.Red else Color.Gray, RoundedCornerShape(50))
                    .background(if (sosT) Color.Red.copy(alpha = 0.2f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (sosT) "🚨 SOS!" else "✊ SUSTAIN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (sosT) Color.Red else Color.White
                    )
                    Text(
                        text = if (sosT) "ĐANG CỨU HỘ" else "${(progressRatio * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SOS warning progress bar
            LinearProgressIndicator(
                progress = progressRatio,
                color = Color.Red,
                trackColor = Color(0xFF1F253C),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (sosT) {
                Text(
                    text = "🚨 GPS COORDINATES TRANSMITTED:\nLat: 10.76269 | Long: 106.66017\nTín hiệu cứu nguy khẩn cấp đã truyền thành công tới trạm y tế!",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.ls9CountdownProgress.value = 0f
                        viewModel.ls9SOSTriggered.value = false
                        viewModel.ls9SOSSirenBeeping.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("HỦY KÍCH HOẠT SOS 🟢", color = Color.Black)
                }
            }
        }
    }
}

// ================== CHEF PAN MASTER ==================
@Composable
fun ChefMasterView(viewModel: GestureViewModel) {
    val ht by viewModel.ls10PancakeHeight.collectAsState()
    val chops by viewModel.ls10ChopProgress.collectAsState()
    val statusText by viewModel.ls10RecipeStatus.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "🍳 BẾP KHÔNG CHẠM CHEF_MASTER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Hất gạt tay để lật pancake | Gõ bàn tay thái lát cà rốt", color = Color(0xFF8B9FB4), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth(0.9f),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Pancakes rising based on height offset
                Box(
                    modifier = Modifier
                        .offset(y = (-ht * 6).dp)
                        .size(width = 95.dp, height = 18.dp)
                        .background(Color(0xFFCD853F), RoundedCornerShape(9.dp))
                        .border(1.5.dp, Color.White, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🥞 Fluffy", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Pan silhouette
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(140.dp)
                        .background(Color.DarkGray, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                color = Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.quickFlickPancake() }) {
                    Text("HẤT CHẢO (FLIP)")
                }
                Button(onClick = { viewModel.chopVegetables() }) {
                    Text("THÁI CÀ RỐT (CHOP): $chops")
                }
            }
        }
    }
}

// ================== STRETCH RECOVERY GUIDE ==================
@Composable
fun CarpalStretchView(viewModel: GestureViewModel) {
    val stretchText by viewModel.ls11StretchText.collectAsState()
    val index by viewModel.ls11StretchIndex.collectAsState()
    val points by viewModel.ls11StretchPoints.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(text = "🧘 CHỮA TRỊ CO KHỚP TAY VĂN PHÒNG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Hoàn thành các động tác co duỗi để phòng ngừa hội chứng ống cổ tay.", color = Color(0xFF8B9FB4), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Current step visual representation
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFF152A42), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF00FA9A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (index) {
                        0 -> "✊" // Fist
                        1 -> "✋" // Palm
                        2 -> "🤏" // Pinch
                        else -> "🖖" // Spock
                    },
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stretchText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Badge(containerColor = Color(0xFF00FA9A), contentColor = Color.Black) {
                Text(
                    text = "ĐIỂM LUYỆN KHỚP KHỎE MẠNH: $points PTS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
