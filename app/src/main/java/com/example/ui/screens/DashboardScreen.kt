package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CameraPreviewPanel
import com.example.ui.components.HandSkeletonView
import com.example.ui.components.InteractiveSimulatorPanel
import com.example.viewmodel.GestureViewModel

@Composable
fun DashboardScreen(
    viewModel: GestureViewModel,
    modifier: Modifier = Modifier
) {
    val activeGesture by viewModel.activeGesture.collectAsState()
    val landmarks by viewModel.handLandmarks.collectAsState()
    val totalAppScore by viewModel.appScore.collectAsState()
    val isUsingCamera by viewModel.isUsingCamera.collectAsState()

    var activeCategoryTab by remember { mutableStateOf(0) } // 0: Games, 1: Everyday Apps

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030610))
    ) {
        val isWide = maxWidth > 840.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header Cyber Title Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "GESTUREQUEST ARENA 🔮",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Trung tâm thực tế ảo cử chỉ tay 21 điểm của MediaPipe",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B9FB4)
                    )
                }

                // Global Stats Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F35)),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Reps",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HẠNG ĐIỂM SỐ: $totalAppScore",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs to shift between Games & Life Apps
            TabRow(
                selectedTabIndex = activeCategoryTab,
                containerColor = Color(0xFF0C101F),
                contentColor = Color(0xFF00FA9A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeCategoryTab]),
                        color = if (activeCategoryTab == 0) Color(0xFF00FA9A) else Color(0xFFFFD700)
                    )
                },
                modifier = Modifier.testTag("category_tab_row")
            ) {
                Tab(
                    selected = activeCategoryTab == 0,
                    onClick = { activeCategoryTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = "Games")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("11+ TRÒ CHƠI CỬ CHỈ 🎮", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_games")
                )
                Tab(
                    selected = activeCategoryTab == 1,
                    onClick = { activeCategoryTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderShared, contentDescription = "Apps")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("11+ TIỆN ÍCH ĐỜI SỐNG 🧘", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_apps")
                )
            }

            if (isWide) {
                // LANDSCAPE ADAPTIVE GRID
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT FRAME: Camera & Hand skeletal glow monitor
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxWidth()
                                .border(1.ddpBoundary(), Color(0xFF1A2645), RoundedCornerShape(20.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                HandSkeletonView(
                                    landmarks = landmarks,
                                    activeColor = if (activeCategoryTab == 0) Color(0xFF00FA9A) else Color(0xFFFFD700),
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Overlay Detected Gesture Token Chip
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "NHẬN DIỆN CỬ CHỈ: ${activeGesture.emoji} ${activeGesture.vietnameseName}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        CameraPreviewPanel(
                            isUsingCamera = isUsingCamera,
                            onCameraActiveToggled = { viewModel.setUsingCamera(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // RIGHT FRAME: Playable Viewport Games / LifeApps + Trackpad Controls
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.4f)
                                .fillMaxWidth()
                        ) {
                            if (activeCategoryTab == 0) {
                                GamesPlayScreen(viewModel)
                            } else {
                                LifeAppsScreen(viewModel)
                            }
                        }

                        InteractiveSimulatorPanel(
                            viewModel = viewModel,
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            } else {
                // PORTRAIT SCROLLING STACK for Mobiles
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick gesture skeleton view height limited to look beautiful
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(260.dp)
                            .fillMaxWidth()
                            .border(1.ddpBoundary(), Color(0xFF1E2843), RoundedCornerShape(20.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            HandSkeletonView(
                                landmarks = landmarks,
                                activeColor = if (activeCategoryTab == 0) Color(0xFF00FA9A) else Color(0xFFFFD700),
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay detected sign
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cử chỉ đang kết nối: ${activeGesture.emoji} ${activeGesture.vietnameseName}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Selected Viewport (Games or Apps)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101F)),
                        modifier = Modifier
                            .height(440.dp)
                            .fillMaxWidth()
                            .border(1.ddpBoundary(), Color(0xFF19223D), RoundedCornerShape(20.dp))
                    ) {
                        if (activeCategoryTab == 0) {
                            GamesPlayScreen(viewModel)
                        } else {
                            LifeAppsScreen(viewModel)
                        }
                    }

                    // Visual Simulator Controllers
                    InteractiveSimulatorPanel(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Unified Camera Panel
                    CameraPreviewPanel(
                        isUsingCamera = isUsingCamera,
                        onCameraActiveToggled = { viewModel.setUsingCamera(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun Int.ddpBoundary() = 1.dp
