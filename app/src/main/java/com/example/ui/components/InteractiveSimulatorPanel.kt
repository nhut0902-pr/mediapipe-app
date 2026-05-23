package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.GestureType
import com.example.viewmodel.GestureViewModel

@Composable
fun InteractiveSimulatorPanel(
    viewModel: GestureViewModel,
    modifier: Modifier = Modifier
) {
    val activeGesture by viewModel.activeGesture.collectAsState()
    val px by viewModel.pointerX.collectAsState()
    val py by viewModel.pointerY.collectAsState()

    val tCurl by viewModel.thumbCurl.collectAsState()
    val iCurl by viewModel.indexCurl.collectAsState()
    val mCurl by viewModel.middleCurl.collectAsState()
    val rCurl by viewModel.ringCurl.collectAsState()
    val pCurl by viewModel.pinkyCurl.collectAsState()

    var trackSize by remember { mutableStateOf(Offset(300f, 300f)) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111625)
        ),
        modifier = modifier
            .border(1.dp, Color(0xFF1E2640), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = "Simulator",
                        tint = Color(0xFF00FA9A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIRTUAL DISK CHẤP (TRACKPAD)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Badge(
                    containerColor = Color(0xFF00FA9A).copy(alpha = 0.15f),
                    contentColor = Color(0xFF00FA9A)
                ) {
                    Text(
                        text = "MediaPipe Sim v1.2",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fast Hand Gesture Presets Selectors
            Text(
                text = "Bộ cử chỉ mẫu nhanh (Nạp nhanh):",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B9FB4)
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preset_gesture_row")
            ) {
                items(GestureType.values()) { preset ->
                    val isSelected = activeGesture == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGesturePreset(preset) },
                        label = {
                            Text("${preset.emoji} ${preset.vietnameseName}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00FA9A),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1A1F35),
                            labelColor = Color(0xFF8B9FB4)
                        ),
                        modifier = Modifier.testTag("preset_${preset.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trackpad and Sliders Layout side-by-side on wide views, stacked on vertical mobile views
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val isWide = maxWidth > 560.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.2f)) {
                            TrackPadView(px, py, onMove = { nx, ny ->
                                viewModel.setPointer(nx, ny)
                                viewModel.evaluateSwipeDetection(nx)
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SlidersView(tCurl, iCurl, mCurl, rCurl, pCurl, onValueChange = { fIdx, sliderVal ->
                                when (fIdx) {
                                    0 -> viewModel.updateFingers(sliderVal, iCurl, mCurl, rCurl, pCurl)
                                    1 -> viewModel.updateFingers(tCurl, sliderVal, mCurl, rCurl, pCurl)
                                    2 -> viewModel.updateFingers(tCurl, iCurl, sliderVal, rCurl, pCurl)
                                    3 -> viewModel.updateFingers(tCurl, iCurl, mCurl, sliderVal, pCurl)
                                    4 -> viewModel.updateFingers(tCurl, iCurl, mCurl, rCurl, sliderVal)
                                }
                            })
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TrackPadView(px, py, onMove = { nx, ny ->
                            viewModel.setPointer(nx, ny)
                            viewModel.evaluateSwipeDetection(nx)
                        })
                        SlidersView(tCurl, iCurl, mCurl, rCurl, pCurl, onValueChange = { fIdx, sliderVal ->
                            when (fIdx) {
                                0 -> viewModel.updateFingers(sliderVal, iCurl, mCurl, rCurl, pCurl)
                                1 -> viewModel.updateFingers(tCurl, sliderVal, mCurl, rCurl, pCurl)
                                2 -> viewModel.updateFingers(tCurl, iCurl, sliderVal, rCurl, pCurl)
                                3 -> viewModel.updateFingers(tCurl, iCurl, mCurl, sliderVal, pCurl)
                                4 -> viewModel.updateFingers(tCurl, iCurl, mCurl, rCurl, sliderVal)
                            }
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hint panel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF0F1422), RoundedCornerShape(10.dp))
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tips",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dùng ngón trỏ quét làm chuột vẽ, kẹo pinch, búa oẳn tù tì. Các game sẽ tự động phản ánh cử chỉ tay của bộ giả lập này đấy!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TrackPadView(
    px: Float,
    py: Float,
    onMove: (Float, Float) -> Unit
) {
    Column {
        Text(
            text = "Bàn cảm ứng điều phối (Kéo thả chuột di chuyển tay):",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B9FB4)
        )
        Spacer(modifier = Modifier.height(4.dp))
        BoxWithConstraints(
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F1322), Color(0xFF131A30))
                    )
                )
                .border(1.5.dp, Color(0xFF00FA9A).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .pointerInput(px, py) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val currentX = px + dragAmount.x / size.width
                        val currentY = py + dragAmount.y / size.height
                        onMove(currentX.coerceIn(0f, 1f), currentY.coerceIn(0f, 1f))
                    }
                }
                .testTag("trackpad_area")
        ) {
            val containerWidth = maxWidth
            val containerHeight = maxHeight

            // Draw crosshair or marker mapped to actual device container dimensions minus the marker size
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (px * (containerWidth.value - 24)).dp.coerceAtLeast(0.dp),
                        y = (py * (containerHeight.value - 24)).dp.coerceAtLeast(0.dp)
                    )
                    .size(24.dp)
                    .background(Color(0xFF00FA9A).copy(alpha = 0.35f), RoundedCornerShape(50))
                    .border(2.dp, Color(0xFF00FA9A), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Pos pointer",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Coordinates Display
            Text(
                text = "X: ${(px * 100).toInt()}% | Y: ${(py * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF00FA9A),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun SlidersView(
    tCurl: Float,
    iCurl: Float,
    mCurl: Float,
    rCurl: Float,
    pCurl: Float,
    onValueChange: (Int, Float) -> Unit
) {
    val sliders = listOf(
        Triple("Ngón cái (Thumb)", tCurl, 0),
        Triple("Ngón trỏ (Index)", iCurl, 1),
        Triple("Ngón giữa (Middle)", mCurl, 2),
        Triple("Ngón áp út (Ring)", rCurl, 3),
        Triple("Ngón út (Pinky)", pCurl, 4)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Độ gập gân ngón tay (Flexion):",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B9FB4)
        )

        sliders.forEach { (label, value, index) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White)
                    Text(
                        text = "${(value * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00FA9A)
                    )
                }
                Slider(
                    value = value,
                    onValueChange = { onValueChange(index, it) },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00FA9A),
                        activeTrackColor = Color(0xFF00FA9A),
                        inactiveTrackColor = Color(0xFF1E2640)
                    ),
                    modifier = Modifier
                        .height(24.dp)
                        .testTag("slider_$index")
                )
            }
        }
    }
}
