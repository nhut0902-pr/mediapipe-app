package com.example.ui.components

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewPanel(
    isUsingCamera: Boolean,
    onCameraActiveToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val infiniteTransition = rememberInfiniteTransition(label = "RadarAnim")
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "RadarAngle"
    )

    val sonarPulseRadio by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1424)
        ),
        modifier = modifier
            .border(1.dp, Color(0xFF1F2943), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Camera",
                        tint = Color(0xFF00FA9A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MẮT THẦN HỒNG NGOẠI (WEBCAM)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                // Toggle camera state
                Switch(
                    checked = isUsingCamera,
                    onCheckedChange = { active ->
                        if (active) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        onCameraActiveToggled(active)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00FA9A),
                        checkedTrackColor = Color(0xFF00FA9A).copy(alpha = 0.35f),
                        uncheckedThumbColor = Color(0xFF8B9FB4),
                        uncheckedTrackColor = Color(0xFF1E2640)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body rendering either actual camera view or simulated science-fiction radar scanner
            Box(
                modifier = Modifier
                    .height(260.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF050914))
                    .border(1.5.dp, Color(0xFF00FA9A).copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isUsingCamera && cameraPermissionState.status.isGranted) {
                    // Load actual CameraX preview
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val previewUseCase = Preview.Builder().build().apply {
                                        setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_FRONT_CAMERA,
                                        previewUseCase
                                    )
                                } catch (e: Exception) {
                                    // Fallback if camera provider or binding fails
                                    e.printStackTrace()
                                }
                            }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // AR Landmarker scanning filter on top of the live stream
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Draw virtual laser guide frame
                        drawRect(
                            color = Color(0xFF00FA9A).copy(alpha = 0.5f),
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )

                        // Draw moving computer vision analysis circles
                        drawCircle(
                            color = Color(0xFF00FA9A).copy(alpha = 0.25f),
                            radius = sonarPulseRadio.coerceAtMost(size.width / 2f),
                            center = Offset(cx, cy),
                            style = Stroke(width = 2f)
                        )

                        // Floating dynamic markers
                        val scanRad = 45f
                        val angleRad = Math.toRadians(radarSweepAngle.toDouble())
                        val sx = cx + (size.width / 3.5f) * cos(angleRad).toFloat()
                        val sy = cy + (size.height / 2.8f) * sin(angleRad).toFloat()

                        drawCircle(
                            color = Color(0xFFFF5252),
                            radius = 6f,
                            center = Offset(sx, sy)
                        )
                        drawLine(
                            color = Color(0xFFFF5252).copy(alpha = 0.5f),
                            start = Offset(cx, cy),
                            end = Offset(sx, sy),
                            strokeWidth = 2f
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📸 WEBCAM ĐANG KÀI LĂN QUÉT CHỈ...",
                            color = Color(0xFF00FA9A),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                } else {
                    // Radar Simulator View - incredibly cybernetic and gorgeous!
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxR = size.width.coerceAtMost(size.height) / 2.2f

                        // Target static concentric rings
                        drawCircle(
                            color = Color(0xFF00FA9A).copy(alpha = 0.08f),
                            radius = maxR,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFF00FA9A).copy(alpha = 0.15f),
                            radius = maxR * 0.7f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0xFF00FA9A).copy(alpha = 0.25f),
                            radius = maxR * 0.4f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )

                        // Radar sweep line
                        val angleRad = Math.toRadians(radarSweepAngle.toDouble())
                        val targetX = cx + maxR * cos(angleRad).toFloat()
                        val targetY = cy + maxR * sin(angleRad).toFloat()

                        drawLine(
                            color = Color(0xFF00FA9A).copy(alpha = 0.7f),
                            start = Offset(cx, cy),
                            end = Offset(targetX, targetY),
                            strokeWidth = 3f
                        )

                        // Floating simulated hand tracking lock target
                        val lockX = cx + maxR * 0.5f * cos(angleRad * 0.4).toFloat()
                        val lockY = cy + maxR * 0.5f * sin(angleRad * 0.4).toFloat()

                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.4f),
                            radius = 28f,
                            center = Offset(lockX, lockY),
                            style = Stroke(width = 2f)
                        )
                        drawRect(
                            color = Color(0xFFFFD700),
                            topLeft = Offset(lockX - 4f, lockY - 4f),
                            size = androidx.compose.ui.geometry.Size(8f, 8f)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF162035), RoundedCornerShape(50))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Sim Mode",
                                tint = Color(0xFF8B9FB4),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Webcam đang Tắt (Sử dụng Trackpad Simulator)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B9FB4)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mở Switch góc trên để kích hoạt mắt độc lập camera thật rảnh tay!",
                            color = Color(0xFF00FA9A),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
