package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.example.model.HandLandmark

@Composable
fun HandSkeletonView(
    landmarks: List<HandLandmark>,
    activeColor: Color = Color(0xFF00FA9A), // Medium Spring Green
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0A1128), Color(0xFF02040C)),
                    radius = 800f
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (landmarks.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height

            // Helper to get raw coordinates
            fun getPos(id: Int): Offset {
                if (id >= landmarks.size) return Offset.Zero
                val landmark = landmarks[id]
                // Landmarks are 0 to 1, we map to canvas size
                return Offset(landmark.x * w, landmark.y * h)
            }

            // Bone connection paths (MediaPipe Hands standard skeletal layout)
            val boneConnections = listOf(
                // Palm base
                0 to 1, 0 to 5, 0 to 17,
                // Thumb
                1 to 2, 2 to 3, 3 to 4,
                // Index finger
                5 to 6, 6 to 7, 7 to 8,
                // Middle finger
                9 to 10, 10 to 11, 11 to 12,
                // Ring finger
                13 to 14, 14 to 15, 15 to 16,
                // Pinky finger
                17 to 18, 18 to 19, 19 to 20,
                // Knuckles bridging
                5 to 9, 9 to 13, 13 to 17
            )

            // Draw glowing bone lines
            boneConnections.forEach { (startId, endId) ->
                val pStart = getPos(startId)
                val pEnd = getPos(endId)
                if (pStart != Offset.Zero && pEnd != Offset.Zero) {
                    // Shadow/glowing bloom effect
                    drawLine(
                        color = activeColor.copy(alpha = 0.3f),
                        start = pStart,
                        end = pEnd,
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = activeColor,
                        start = pStart,
                        end = pEnd,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw joint nodes (colored pins)
            landmarks.forEach { lm ->
                val center = Offset(lm.x * w, lm.y * h)
                val isTip = lm.id in listOf(4, 8, 12, 16, 20)
                val nodeColor = if (isTip) Color(0xFFFF4500) else activeColor

                // Outer halo
                drawCircle(
                    color = nodeColor.copy(alpha = 0.45f),
                    radius = if (isTip) 14f else 9f,
                    center = center
                )
                // Inner core
                drawCircle(
                    color = Color.White,
                    radius = if (isTip) 6f else 4f,
                    center = center
                )
            }
        }
    }
}
