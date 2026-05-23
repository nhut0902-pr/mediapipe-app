package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameType
import com.example.model.GestureType
import com.example.viewmodel.GestureViewModel
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GamesPlayScreen(
    viewModel: GestureViewModel,
    modifier: Modifier = Modifier
) {
    val selectedGame by viewModel.selectedGame.collectAsState()
    val activeGesture by viewModel.activeGesture.collectAsState()

    val gamesList = GameType.values()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Horizontal list of 11 games as cyber tabs
        Text(
            text = "CHỌN GIAN HÀNG GAME CỬ CHỈ (11+ TRÒ CHƠI):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FA9A),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("games_tab_row")
        ) {
            items(gamesList) { game ->
                val isSelected = selectedGame == game
                Card(
                    modifier = Modifier
                        .testTag("game_tab_${game.id}")
                        .clickable { viewModel.selectGame(game) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF00FA9A) else Color(0xFF161F35)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00FA9A) else Color(0xFF1E2843))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = game.iconEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = game.title.split(" (")[0],
                            color = if (isSelected) Color.Black else Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active game detailed panel layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = selectedGame.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = selectedGame.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B9FB4)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Render selected game viewport simulator
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF080C16))
                .border(1.5.dp, Color(0xFF1D2640), RoundedCornerShape(18.dp))
                .testTag("game_window_viewport")
        ) {
            AnimatedContent(
                targetState = selectedGame,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() with slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "ActiveGameAnim"
            ) { target ->
                when (target) {
                    GameType.FRUIT_SLICER -> FruitSlicerGameView(viewModel)
                    GameType.GOLDEN_CATCH -> GoldenCatchGameView(viewModel)
                    GameType.FLAPPY_HAND -> FlappyHandGameView(viewModel)
                    GameType.BALANCE_BALL -> BalanceBallGameView(viewModel)
                    GameType.RHYTHM_PIANO -> RhythmPianoGameView(viewModel)
                    GameType.BUBBLE_POPPER -> BubblePopperGameView(viewModel)
                    GameType.VIRTUAL_BOXER -> VirtualBoxerGameView(viewModel)
                    GameType.ROCK_PAPER_SCISSORS -> RockPaperScissorsGameView(viewModel)
                    GameType.SPACE_SHOOTER -> SpaceShooterGameView(viewModel)
                    GameType.WHACK_A_MOLE -> WhackAMoleGameView(viewModel)
                    GameType.WAVE_RUNNER -> WaveRunnerGameView(viewModel)
                }
            }
        }
    }
}

// ================== FRUIT SLICER ==================
@Composable
fun FruitSlicerGameView(viewModel: GestureViewModel) {
    val fruits by viewModel.gs1Fruits.collectAsState()
    val score by viewModel.gs1Score.collectAsState()
    val lives by viewModel.gs1Lives.collectAsState()
    val isOver by viewModel.gs1IsGameOver.collectAsState()
    val px by viewModel.pointerX.collectAsState()
    val py by viewModel.pointerY.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw coordinate trace grid
            drawRect(
                color = Color(0xFF0F1A35).copy(alpha = 0.3f),
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
            )

            // Draw fruit pieces
            fruits.forEach { f ->
                val fx = f.x * size.width
                val fy = f.y * size.height
                if (f.isSliced) {
                    // Halved splash effect
                    drawCircle(
                        color = f.color.copy(alpha = 0.45f),
                        radius = 45f,
                        center = Offset(fx, fy)
                    )
                    // Slash cuts
                    drawLine(
                        color = Color.White,
                        start = Offset(fx - 50f, fy - 20f),
                        end = Offset(fx + 50f, fy + 20f),
                        strokeWidth = 6f
                    )
                } else {
                    // Glowing background
                    drawCircle(
                        color = f.color.copy(alpha = 0.15f),
                        radius = 48f,
                        center = Offset(fx, fy)
                    )
                }
            }
        }

        // Render overlay fruit emojis at landmarks
        fruits.forEach { f ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (f.x * 280).dp,
                        y = (f.y * 240).dp
                    )
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (f.isSliced) "💥" else f.type,
                    fontSize = 28.sp
                )
            }
        }

        // Blade trace lines representing the index finger pointer coordinates
        val activeG by viewModel.activeGesture.collectAsState()
        val isBlade = activeG == GestureType.INDEX_POINT || activeG == GestureType.PALM
        if (isBlade) {
            Box(
                modifier = Modifier
                    .offset(x = (px * 300).dp, y = (py * 250).dp)
                    .size(8.dp)
                    .background(Color.White, RoundedCornerShape(50))
            )
        }

        GameHUD(score = score, lives = lives, isGameOver = isOver, onRestart = {
            viewModel.selectGame(GameType.FRUIT_SLICER)
        })
    }
}

// ================== GOLDEN CATCH ==================
@Composable
fun GoldenCatchGameView(viewModel: GestureViewModel) {
    val items by viewModel.gs2Items.collectAsState()
    val score by viewModel.gs2Score.collectAsState()
    val lives by viewModel.gs2Lives.collectAsState()
    val isOver by viewModel.gs2IsGameOver.collectAsState()
    val basketX by viewModel.pointerX.collectAsState()
    val activeG by viewModel.activeGesture.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Falling items
        items.forEach { item ->
            if (!item.isCaught) {
                Box(
                    modifier = Modifier
                        .offset(x = (item.x * 280).dp, y = (item.y * 220).dp)
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.isApple) "🍎" else "💣",
                        fontSize = 26.sp
                    )
                }
            }
        }

        // Basket at bottom controlled by Pointer X
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (basketX * 260).dp, y = (-20).dp)
                .size(width = 80.dp, height = 50.dp)
                .background(
                    if (activeG == GestureType.PALM) Color(0xFF00FA9A).copy(alpha = 0.35f) else Color(0xFFFF5252).copy(alpha = 0.25f),
                    RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp, topStart = 6.dp, topEnd = 6.dp)
                )
                .border(
                    2.dp,
                    if (activeG == GestureType.PALM) Color(0xFF00FA9A) else Color(0xFFFF5252),
                    RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp, topStart = 6.dp, topEnd = 6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🧺 HỨNG", color = Color.White, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (activeG == GestureType.PALM) "XÒE TAY ✅" else "KHÉP FIST 🚫",
                    color = if (activeG == GestureType.PALM) Color(0xFF00FA9A) else Color(0xFFFF5252),
                    fontSize = 9.sp
                )
            }
        }

        GameHUD(score = score, lives = lives, isGameOver = isOver, onRestart = {
            viewModel.selectGame(GameType.GOLDEN_CATCH)
        })
    }
}

// ================== FLAPPY HAND ==================
@Composable
fun FlappyHandGameView(viewModel: GestureViewModel) {
    val birdY by viewModel.gs3BirdY.collectAsState()
    val pipes by viewModel.gs3Pipes.collectAsState()
    val score by viewModel.gs3Score.collectAsState()
    val isOver by viewModel.gs3IsGameOver.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw pipelines on custom Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            pipes.forEach { pipe ->
                val px = pipe.x * size.width
                val gapCenterY = pipe.gapY * size.height
                val gSize = pipe.gapSize * size.height
                val pipeWidth = 65f

                // Top Pipe
                drawRoundRect(
                    color = Color(0xFF00FA9A).copy(alpha = 0.85f),
                    topLeft = Offset(px - pipeWidth / 2f, 0f),
                    size = Size(pipeWidth, gapCenterY - gSize / 2f),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Bottom Pipe
                drawRoundRect(
                    color = Color(0xFF00FA9A).copy(alpha = 0.85f),
                    topLeft = Offset(px - pipeWidth / 2f, gapCenterY + gSize / 2f),
                    size = Size(pipeWidth, size.height - (gapCenterY + gSize / 2f)),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Grid lines inside pipes
                drawLine(
                    color = Color.Black.copy(alpha = 0.3f),
                    start = Offset(px, 0f),
                    end = Offset(px, size.height),
                    strokeWidth = 3f
                )
            }
        }

        // Render the cute flying bird flap
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = (birdY * 220).dp)
                .size(34.dp)
                .background(Color(0xFFFFD700), RoundedCornerShape(50))
                .border(2.dp, Color.Black, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🐦", fontSize = 18.sp)
        }

        GameHUD(score = score, lives = null, isGameOver = isOver, onRestart = {
            viewModel.selectGame(GameType.FLAPPY_HAND)
        })
    }
}

// ================== BALANCE BALL ==================
@Composable
fun BalanceBallGameView(viewModel: GestureViewModel) {
    val angle by viewModel.gs4BoardAngle.collectAsState()
    val ballX by viewModel.gs4BallX.collectAsState()
    val score by viewModel.gs4Score.collectAsState()
    val isOver by viewModel.gs4IsGameOver.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 1.5f
            val lineLen = size.width * 0.75f

            // Draw balance wooden platform angled
            val dx = (lineLen / 2f) * cos(angle)
            val dy = (lineLen / 2f) * sin(angle)

            val pStart = Offset(cx - dx, cy - dy)
            val pEnd = Offset(cx + dx, cy + dy)

            // Platform
            drawLine(
                color = Color(0xFFCD853F),
                start = pStart,
                end = pEnd,
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )

            // Fulcrum base triangle
            val baseP1 = Offset(cx, cy)
            val baseP2 = Offset(cx - 35f, cy + 50f)
            val baseP3 = Offset(cx + 35f, cy + 50f)
            drawLine(Color(0xFF8B9FB4), baseP1, baseP2, 4f)
            drawLine(Color(0xFF8B9FB4), baseP1, baseP3, 4f)
            drawLine(Color(0xFF8B9FB4), baseP2, baseP3, 4f)

            // Floating Ball position mapped on angled board
            val mx = pStart.x + ballX * (pEnd.x - pStart.x)
            val my = pStart.y + ballX * (pEnd.y - pStart.y) - 24f

            drawCircle(
                color = Color(0xFF00FA9A),
                radius = 20f,
                center = Offset(mx, my)
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(mx - 4f, my - 4f)
            )
        }

        // Tilt guidelines
        Text(
            text = "Góc nghiêng: ${Math.toDegrees(angle.toDouble()).toInt()}°",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        )

        GameHUD(score = score, lives = null, isGameOver = isOver, onRestart = {
            viewModel.selectGame(GameType.BALANCE_BALL)
        })
    }
}

// ================== RHYTHM PIANO ==================
@Composable
fun RhythmPianoView(viewModel: GestureViewModel) {
    // Already defined as sub component
}

@Composable
fun RhythmPianoGameView(viewModel: GestureViewModel) {
    val notes by viewModel.gs5Notes.collectAsState()
    val score by viewModel.gs5Score.collectAsState()
    val combo by viewModel.gs5Combo.collectAsState()
    val px by viewModel.pointerX.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Three Lanes
                for (lane in 0..2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color(0xFF1F2943))
                            .clickable { viewModel.tapPianoChord(lane) },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Lane falling notes
                        notes.filter { it.lane == lane && !it.isHit }.forEach { note ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(24.dp)
                                    .offset(y = (note.y * 180).dp)
                                    .background(Color(0xFF00FA9A), RoundedCornerShape(12.dp))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                            )
                        }

                        // Ideal trigger visual line at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color(0xFF00FA9A).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (lane) {
                                    0 -> "✌️ Kéo/Peace"
                                    1 -> "🖖 Spock"
                                    else -> "☝️ Chỉ chỉ"
                                },
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Rhythm Pad trigger controls clicking lanes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF131A2D))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { viewModel.tapPianoChord(0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2E4F)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text("ĐÁNH LÀN 1", fontSize = 10.sp)
                }
                Button(
                    onClick = { viewModel.tapPianoChord(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2E4F)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text("ĐÁNH LÀN 2", fontSize = 10.sp)
                }
                Button(
                    onClick = { viewModel.tapPianoChord(2) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2E4F)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text("ĐÁNH LÀN 3", fontSize = 10.sp)
                }
            }
        }

        // Overlay score & combo HUD
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "ĐIỂM: $score", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (combo > 0) {
                Text(text = "COMBO: $combo! 🔥", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ================== BUBBLE POPPER ==================
@Composable
fun BubblePopperGameView(viewModel: GestureViewModel) {
    val bubbles by viewModel.gs6Bubbles.collectAsState()
    val score by viewModel.gs6Score.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        bubbles.forEach { b ->
            Box(
                modifier = Modifier
                    .offset(x = (b.x * 280).dp, y = (b.y * 220).dp)
                    .size(b.size.dp)
                    .clip(RoundedCornerShape(50))
                    .background(b.color.copy(alpha = 0.25f))
                    .border(1.5.dp, b.color, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((b.size * 0.3f).dp)
                        .align(Alignment.TopStart)
                        .offset(x = (b.size * 0.15f).dp, y = (b.size * 0.15f).dp)
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp).align(Alignment.TopStart)) {
            Text(text = "Đã chọc nổ", color = Color(0xFF8B9FB4), fontSize = 10.sp)
            Text(text = "$score điểm", color = Color(0xFF00FA9A), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ================== VIRTUAL BOXER ==================
@Composable
fun VirtualBoxerGameView(viewModel: GestureViewModel) {
    val power by viewModel.gs7PowerMeter.collectAsState()
    val score by viewModel.gs7Score.collectAsState()
    val bags by viewModel.gs7BagsPopped.collectAsState()
    val msg by viewModel.gs7Message.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "PHÒNG ĐẤM BOX DI ĐỘNG",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Punching bag - shrinks and shakes based on power
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 150.dp)
                    .clickable { viewModel.punchBoxerTarget() }
                    .background(Color(0xFFFF1744), RoundedCornerShape(30.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🥊", fontSize = 48.sp)
                    Text(text = "ĐẤM FIST!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Bao cát: $bags", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = power / 100f,
                color = Color(0xFFFF1744),
                trackColor = Color(0xFF1E2843),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = Color(0xFF00FA9A), fontSize = 12.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.punchBoxerTarget() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                modifier = Modifier.testTag("punch_action_button")
            ) {
                Text("GIẢ LẬP ĐẤM BOX (TAP)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================== RPS INTERACTIVE ==================
@Composable
fun RockPaperScissorsGameView(viewModel: GestureViewModel) {
    val userC by viewModel.gs8UserChoice.collectAsState()
    val botC by viewModel.gs8AIChoice.collectAsState()
    val outcome by viewModel.gs8ResultText.collectAsState()
    val userScore by viewModel.gs8PlayerScore.collectAsState()
    val aiScore by viewModel.gs8AIScore.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User choice
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "NGƯỜI CHƠI", color = Color(0xFF00FA9A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFF1E2843), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userC?.emoji ?: "❓", fontSize = 34.sp)
                    }
                    Text(text = "Điểm: $userScore", color = Color.White, fontSize = 13.sp)
                }

                Text(text = "VS", color = Color(0xFFFF5252), fontSize = 28.sp, fontWeight = FontWeight.Black)

                // BOT Choice
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SIÊU TRÍ TUỆ BOT AI", color = Color(0xFFE040FB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFF1E2843), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = botC?.emoji ?: "❓", fontSize = 34.sp)
                    }
                    Text(text = "Điểm: $aiScore", color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = outcome,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Selectors
            Text(text = "Chọn đũa chiến của bạn:", color = Color(0xFF8B9FB4), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.playRPSGame(GestureType.FIST) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233052))
                ) {
                    Text("✊ BÚA")
                }
                Button(
                    onClick = { viewModel.playRPSGame(GestureType.PALM) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233052))
                ) {
                    Text("✋ BAO")
                }
                Button(
                    onClick = { viewModel.playRPSGame(GestureType.PEACE) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233052))
                ) {
                    Text("✌️ KÉO")
                }
            }
        }
    }
}

// ================== SPACE SHOOTER ==================
@Composable
fun SpaceShooterGameView(viewModel: GestureViewModel) {
    val playerX by viewModel.gs9PlayerX.collectAsState()
    val lasers by viewModel.gs9Lasers.collectAsState()
    val aliens by viewModel.gs9Aliens.collectAsState()
    val score by viewModel.gs9Score.collectAsState()
    val isOver by viewModel.gs9IsGameOver.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Aliens
        aliens.forEach { alien ->
            Box(
                modifier = Modifier
                    .offset(x = (alien.x * 280).dp, y = (alien.y * 220).dp)
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛸", fontSize = 22.sp)
            }
        }

        // Lasers
        lasers.forEach { laser ->
            Box(
                modifier = Modifier
                    .offset(x = (laser.x * 280 + 10).dp, y = (laser.y * 220).dp)
                    .size(width = 4.dp, height = 14.dp)
                    .background(Color(0xFFFF00FF), RoundedCornerShape(2.dp))
            )
        }

        // Player Ship
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (playerX * 270).dp, y = (-20).dp)
                .size(36.dp)
                .background(Color(0xFF00FA9A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🚀", fontSize = 20.sp)
        }

        GameHUD(score = score, lives = null, isGameOver = isOver, onRestart = {
            viewModel.selectGame(GameType.SPACE_SHOOTER)
        })
    }
}

// ================== WHACK A MOLE ==================
@Composable
fun WhackAMoleGameView(viewModel: GestureViewModel) {
    val moles by viewModel.gs10Moles.collectAsState()
    val score by viewModel.gs10Score.collectAsState()
    val timeLeft by viewModel.gs10TimeLeft.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(moles) { mole ->
                val isUp = mole.isUp
                Box(
                    modifier = Modifier
                        .height(65.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUp) Color(0xFF4E342E) else Color(0xFF162035))
                        .border(1.dp, Color(0xFF1D2640), RoundedCornerShape(12.dp))
                        .clickable {
                            if (isUp) {
                                mole.isUp = false
                                viewModel.gs10Score.value += 25
                                viewModel.updateGameScore(GameType.WHACK_A_MOLE.id, viewModel.gs10Score.value)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isUp) "🐹" else "🕳️",
                            fontSize = 32.sp
                        )
                        if (isUp) {
                            Text(text = "ĐẬP!", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Timing
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Timer, contentDescription = "Timer", tint = Color.Red, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Hạn: ${timeLeft}s", color = Color.White, fontSize = 11.sp)
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "ĐIỂM ĐẬP: $score", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ================== WAVE RUNNER ==================
@Composable
fun WaveRunnerGameView(viewModel: GestureViewModel) {
    val lane by viewModel.gs11PlayerLane.collectAsState()
    val obstacleX by viewModel.gs11ObstacleX.collectAsState()
    val obstacleLane by viewModel.gs11ObstacleLane.collectAsState()
    val score by viewModel.gs11Score.collectAsState()
    val isOver by viewModel.gs11IsGameOver.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGameLoop()
        onDispose { viewModel.stopGameLoop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw 3 lanes highways
            val laneWidthY = size.height / 4f
            for (i in 1..3) {
                val y = i * laneWidthY
                drawLine(
                    color = Color(0xFF00FA9A).copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
                )
            }
        }

        // Player Surfer avatar mapped to Lane
        Box(
            modifier = Modifier
                .offset(
                    x = 40.dp,
                    y = when (lane) {
                        0 -> 30.dp
                        2 -> 150.dp
                        else -> 90.dp
                    }
                )
                .size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🏄", fontSize = 28.sp)
        }

        // Advancing Obstacle brick
        Box(
            modifier = Modifier
                .offset(
                    x = (obstacleX * 280).dp,
                    y = when (obstacleLane) {
                        0 -> 30.dp
                        2 -> 150.dp
                        else -> 90.dp
                    }
                )
                .size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🧱", fontSize = 24.sp)
        }

        // Instructions for playing
        Text(
            text = "Thumbs UP = Nhảy cao | Thumbs DOWN = Trượt thấp",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        )

        GameHUD(score = score, lives = null, isGameOver = isOver, onRestart = {
            viewModel.selectGame(GameType.WAVE_RUNNER)
        })
    }
}


@Composable
fun GameHUD(
    score: Int,
    lives: Int?,
    isGameOver: Boolean,
    onRestart: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Status Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCORE: $score",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            if (lives != null) {
                Row {
                    for (i in 0 until 3) {
                        Text(
                            text = if (i < lives) "❤️" else "🖤",
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }

        // Game Over modal shade
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = "🎮 GAME OVER 🎮", color = Color.Red, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tổng điểm đạt được: $score", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FA9A))
                    ) {
                        Text(text = "CHƠI LẠI NGAY 🔄", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
