package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.GameType
import com.example.model.GestureType
import com.example.model.HandLandmark
import com.example.model.LifeAppType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Fruit class for Game 1
data class Fruit(
    val id: Int,
    val type: String, // "🍉", "🍊", "🥥", "💣"
    var x: Float,     // Fraction 0f..1f
    var y: Float,     // Fraction 0f..1f
    val speedX: Float,
    var speedY: Float,
    var isSliced: Boolean = false,
    val color: Color
)

// Apple/Bomb class for Game 2
data class FallingItem(
    val id: Int,
    val isApple: Boolean, // True = Gold Apple, False = Bomb
    var x: Float,
    var y: Float,
    val speedY: Float,
    var isCaught: Boolean = false
)

// Obstacle Pipe for Game 3
data class FlappyPipe(
    val id: Int,
    var x: Float, // Fraction 0f..1f
    val gapY: Float, // Fraction 0.3f..0.7f
    val gapSize: Float = 0.22f
)

// Music Note for Game 5
data class MusicNote(
    val id: Int,
    val lane: Int, // 0..2
    var y: Float,  // 0f to 1f
    var isHit: Boolean = false,
    var isMissed: Boolean = false
)

// Bubble for Game 6
data class PopBubble(
    val id: Int,
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    val speed: Float
)

// Boxing target for Game 7
data class BoxingTarget(
    val id: Int,
    val x: Float,
    val y: Float,
    val term: Long,
    val type: String // "UPPERCUT", "HOOK", "JAB"
)

// Laser for Game 9
data class LaserBeam(
    val id: Int,
    var x: Float,
    var y: Float,
    val speedY: Float
)

data class AlienEnemy(
    val id: Int,
    var x: Float,
    var y: Float,
    val speedX: Float,
    var hp: Int = 1
)

// Mole for Game 10
data class WhackMole(
    val id: Int,
    val x: Float,
    val y: Float,
    var isUp: Boolean,
    var upDuration: Int
)

// Neon Stroke for Paint LifeApp 5
data class PaintStroke(
    val points: List<Offset>,
    val color: Color,
    val size: Float
)

// Home Appliance for SmartHome LifeApp 4
data class HomeAppliance(
    val name: String,
    val id: String,
    val emoji: String,
    val isOn: Boolean,
    val value: String,
    val gestureMapped: GestureType
)

class GestureViewModel : ViewModel() {

    // --- SHARED GESTURE & CAMERA TRACKER STATES ---
    private val _pointerX = MutableStateFlow(0.5f)
    val pointerX: StateFlow<Float> = _pointerX.asStateFlow()

    private val _pointerY = MutableStateFlow(0.5f)
    val pointerY: StateFlow<Float> = _pointerY.asStateFlow()

    private val _activeGesture = MutableStateFlow(GestureType.PALM)
    val activeGesture: StateFlow<GestureType> = _activeGesture.asStateFlow()

    // Hand flex values (0.0f is fully extended, 1.0f is fully closed/flexed)
    private val _thumbCurl = MutableStateFlow(0.0f)
    val thumbCurl = _thumbCurl.asStateFlow()

    private val _indexCurl = MutableStateFlow(0.0f)
    val indexCurl = _indexCurl.asStateFlow()

    private val _middleCurl = MutableStateFlow(0.0f)
    val middleCurl = _middleCurl.asStateFlow()

    private val _ringCurl = MutableStateFlow(0.0f)
    val ringCurl = _ringCurl.asStateFlow()

    private val _pinkyCurl = MutableStateFlow(0.0f)
    val pinkyCurl = _pinkyCurl.asStateFlow()

    private val _handLandmarks = MutableStateFlow<List<HandLandmark>>(emptyList())
    val handLandmarks: StateFlow<List<HandLandmark>> = _handLandmarks.asStateFlow()

    // Detection mode: SIMULATOR or PHYSICAL_CAMERA
    private val _isCameraConnected = MutableStateFlow(false)
    val isCameraConnected = _isCameraConnected.asStateFlow()

    private val _isUsingCamera = MutableStateFlow(false)
    val isUsingCamera = _isUsingCamera.asStateFlow()

    // Navigation and tabs
    private val _selectedGame = MutableStateFlow(GameType.FRUIT_SLICER)
    val selectedGame = _selectedGame.asStateFlow()

    private val _selectedLifeApp = MutableStateFlow(LifeAppType.APP_LOCK)
    val selectedLifeApp = _selectedLifeApp.asStateFlow()

    private val _appScore = MutableStateFlow(0)
    val appScore = _appScore.asStateFlow()

    // Master list of 21 landmarks
    init {
        updateSkeletonLandmarks()
        startGameLoop() // Tự động khởi chạy vòng lặp trò chơi để cập nhật trạng thái tiện ích đời sống liên tục
    }

    // Update coordinates derived from the 5 finger flexion states to show a beautiful interactive hand
    fun updateSkeletonLandmarks() {
        val list = ArrayList<HandLandmark>()
        val px = _pointerX.value
        val py = _pointerY.value

        // Wrist (Landmark 0)
        list.add(HandLandmark(0, "WRIST", px, py + 0.15f))

        // Thumb: Landmark 1..4
        val tc = _thumbCurl.value
        list.add(HandLandmark(1, "THUMB_CMC", px - 0.04f, py + 0.10f))
        list.add(HandLandmark(2, "THUMB_MCP", px - 0.08f, py + 0.06f))
        list.add(HandLandmark(3, "THUMB_IP", px - 0.09f - (0.03f * (1f - tc)), py + 0.02f - (0.02f * tc)))
        list.add(HandLandmark(4, "THUMB_TIP", px - 0.08f - (0.06f * (1f - tc)), py - 0.02f - (0.04f * tc)))

        // Index: Landmark 5..8
        val ic = _indexCurl.value
        list.add(HandLandmark(5, "INDEX_FINGER_MCP", px - 0.04f, py + 0.02f))
        list.add(HandLandmark(6, "INDEX_FINGER_PIP", px - 0.04f, py - 0.04f + (0.03f * ic)))
        list.add(HandLandmark(7, "INDEX_FINGER_DIP", px - 0.04f, py - 0.10f + (0.07f * ic)))
        list.add(HandLandmark(8, "INDEX_FINGER_TIP", px - 0.04f, py - 0.16f + (0.12f * ic)))

        // Middle: Landmark 9..12
        val mc = _middleCurl.value
        list.add(HandLandmark(9, "MIDDLE_FINGER_MCP", px - 0.01f, py + 0.01f))
        list.add(HandLandmark(10, "MIDDLE_FINGER_PIP", px - 0.01f, py - 0.06f + (0.04f * mc)))
        list.add(HandLandmark(11, "MIDDLE_FINGER_DIP", px - 0.01f, py - 0.12f + (0.08f * mc)))
        list.add(HandLandmark(12, "MIDDLE_FINGER_TIP", px - 0.01f, py - 0.18f + (0.14f * mc)))

        // Ring: Landmark 13..16
        val rc = _ringCurl.value
        list.add(HandLandmark(13, "RING_FINGER_MCP", px + 0.02f, py + 0.02f))
        list.add(HandLandmark(14, "RING_FINGER_PIP", px + 0.02f, py - 0.04f + (0.03f * rc)))
        list.add(HandLandmark(15, "RING_FINGER_DIP", px + 0.02f, py - 0.10f + (0.07f * rc)))
        list.add(HandLandmark(16, "RING_FINGER_TIP", px + 0.02f, py - 0.15f + (0.11f * rc)))

        // Pinky: Landmark 17..20
        val pc = _pinkyCurl.value
        list.add(HandLandmark(17, "PINKY_MCP", px + 0.05f, py + 0.03f))
        list.add(HandLandmark(18, "PINKY_PIP", px + 0.06f, py - 0.02f + (0.02f * pc)))
        list.add(HandLandmark(19, "PINKY_DIP", px + 0.07f, py - 0.06f + (0.04f * pc)))
        list.add(HandLandmark(20, "PINKY_TIP", px + 0.07f, py - 0.11f + (0.08f * pc)))

        _handLandmarks.value = list

        // Run detection heuristic
        evaluateGestureHeuristic()
    }

    private fun evaluateGestureHeuristic() {
        val t = _thumbCurl.value
        val i = _indexCurl.value
        val m = _middleCurl.value
        val r = _ringCurl.value
        val p = _pinkyCurl.value

        val newG = when {
            // Palm: All open (low curl)
            t < 0.25f && i < 0.25f && m < 0.25f && r < 0.25f && p < 0.25f -> GestureType.PALM
            // Fist: All closed (high curl)
            t > 0.65f && i > 0.65f && m > 0.65f && r > 0.65f && p > 0.65f -> GestureType.FIST
            // Index Point: Only index open
            t > 0.5f && i < 0.25f && m > 0.65f && r > 0.65f && p > 0.65f -> GestureType.INDEX_POINT
            // Peace: Index & Middle open, rest closed
            t > 0.4f && i < 0.3f && m < 0.3f && r > 0.65f && p > 0.65f -> GestureType.PEACE
            // Thumbs Up: Thumb fully open, rest closed, pointing upwards (simulated)
            t < 0.2f && i > 0.65f && m > 0.65f && r > 0.65f && p > 0.65f -> GestureType.THUMBS_UP
            // Rock sign: Index and Pinky open, thumb curled or open, ring/middle closed
            i < 0.3f && p < 0.3f && m > 0.65f && r > 0.65f -> GestureType.ROCK_ON
            // Pinch: Thumb and Index almost closed or slightly flexed, rest curled
            t > 0.2f && t < 0.7f && i > 0.2f && i < 0.7f && m > 0.7f && r > 0.7f && p > 0.7f -> GestureType.PINCH
            // Spock: Index & Middle open, Ring & Pinky open, split (represented by curl)
            t < 0.3f && i < 0.25f && m < 0.25f && r < 0.25f && p < 0.25f -> GestureType.SPOCK
            // OK sign: Index and Thumb curled (meeting), rest open
            t > 0.4f && t < 0.8f && i > 0.4f && i < 0.8f && m < 0.3f && r < 0.3f && p < 0.3f -> GestureType.OK_SIGN
            // Hand Gun: Thumb open, Index open, rest closed
            t < 0.3f && i < 0.3f && m > 0.6f && r > 0.6f && p > 0.6f -> GestureType.GUN
            // Fallback
            else -> _activeGesture.value
        }

        if (newG != _activeGesture.value) {
            _activeGesture.value = newG
            onGestureChanged(newG)
        }
    }

    // Set finger parameters corresponding to predetermined gesture presets
    fun setGesturePreset(preset: GestureType) {
        when (preset) {
            GestureType.PALM -> {
                _thumbCurl.value = 0f
                _indexCurl.value = 0f
                _middleCurl.value = 0f
                _ringCurl.value = 0f
                _pinkyCurl.value = 0f
            }
            GestureType.FIST -> {
                _thumbCurl.value = 0.9f
                _indexCurl.value = 0.9f
                _middleCurl.value = 0.9f
                _ringCurl.value = 0.9f
                _pinkyCurl.value = 0.8f
            }
            GestureType.INDEX_POINT -> {
                _thumbCurl.value = 0.8f
                _indexCurl.value = 0f
                _middleCurl.value = 0.9f
                _ringCurl.value = 0.9f
                _pinkyCurl.value = 0.9f
            }
            GestureType.PEACE -> {
                _thumbCurl.value = 0.8f
                _indexCurl.value = 0f
                _middleCurl.value = 0f
                _ringCurl.value = 0.9f
                _pinkyCurl.value = 0.9f
            }
            GestureType.SPOCK -> {
                // Fingers open, slightly customized Spock representation
                _thumbCurl.value = 0.1f
                _indexCurl.value = 0.1f
                _middleCurl.value = 0.1f
                _ringCurl.value = 0.1f
                _pinkyCurl.value = 0.1f
            }
            GestureType.THUMBS_UP -> {
                _thumbCurl.value = 0f
                _indexCurl.value = 0.9f
                _middleCurl.value = 0.9f
                _ringCurl.value = 0.9f
                _pinkyCurl.value = 0.9f
            }
            GestureType.THUMBS_DOWN -> {
                _thumbCurl.value = 0.05f
                _indexCurl.value = 0.95f
                _middleCurl.value = 0.95f
                _ringCurl.value = 0.95f
                _pinkyCurl.value = 0.95f
            }
            GestureType.OK_SIGN -> {
                _thumbCurl.value = 0.5f
                _indexCurl.value = 0.5f
                _middleCurl.value = 0.1f
                _ringCurl.value = 0.1f
                _pinkyCurl.value = 0.1f
            }
            GestureType.ROCK_ON -> {
                _thumbCurl.value = 0.7f
                _indexCurl.value = 0.1f
                _middleCurl.value = 0.8f
                _ringCurl.value = 0.8f
                _pinkyCurl.value = 0.1f
            }
            GestureType.PINCH -> {
                _thumbCurl.value = 0.45f
                _indexCurl.value = 0.45f
                _middleCurl.value = 0.9f
                _ringCurl.value = 0.9f
                _pinkyCurl.value = 0.9f
            }
            GestureType.GUN -> {
                _thumbCurl.value = 0.1f
                _indexCurl.value = 0.0f
                _middleCurl.value = 0.85f
                _ringCurl.value = 0.85f
                _pinkyCurl.value = 0.85f
            }
        }
        _activeGesture.value = preset
        updateSkeletonLandmarks()
        onGestureChanged(preset)
    }

    fun setPointer(x: Float, y: Float) {
        _pointerX.value = x.coerceIn(0f, 1f)
        _pointerY.value = y.coerceIn(0f, 1f)
        updateSkeletonLandmarks()
        onPointerMoved(_pointerX.value, _pointerY.value)
    }

    fun updateFingers(thumb: Float, index: Float, middle: Float, ring: Float, pinky: Float) {
        _thumbCurl.value = thumb.coerceIn(0f, 1f)
        _indexCurl.value = index.coerceIn(0f, 1f)
        _middleCurl.value = middle.coerceIn(0f, 1f)
        _ringCurl.value = ring.coerceIn(0f, 1f)
        _pinkyCurl.value = pinky.coerceIn(0f, 1f)
        updateSkeletonLandmarks()
    }

    fun setUsingCamera(use: Boolean) {
        _isUsingCamera.value = use
    }

    fun selectGame(game: GameType) {
        _selectedGame.value = game
        resetGameState(game)
    }

    fun selectLifeApp(app: LifeAppType) {
        _selectedLifeApp.value = app
        resetLifeAppState(app)
    }

    // --- GAMES STATE PERSISTENCE ---

    // Game scores
    private val _gameScores = MutableStateFlow<Map<String, Int>>(emptyMap())
    val gameScores = _gameScores.asStateFlow()

    fun updateGameScore(gameId: String, score: Int) {
        val current = _gameScores.value.toMutableMap()
        val currentHigh = current[gameId] ?: 0
        if (score > currentHigh) {
            current[gameId] = score
            _gameScores.value = current
            _appScore.value += (score - currentHigh)
        }
    }

    // Dynamic gameloop ticking job
    private var gameLoopJob: Job? = null

    fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                delay(30) // ~30 fps tick
                tickActiveGame()
            }
        }
    }

    fun stopGameLoop() {
        gameLoopJob?.cancel()
    }

    // 🍉 Game 1: Fruit Slicer State
    val gs1Fruits = MutableStateFlow<List<Fruit>>(emptyList())
    val gs1Score = MutableStateFlow(0)
    val gs1Lives = MutableStateFlow(3)
    val gs1IsGameOver = MutableStateFlow(false)
    private var fruitIdCounter = 0

    // 🧺 Game 2: Golden Catch State
    val gs2Items = MutableStateFlow<List<FallingItem>>(emptyList())
    val gs2Score = MutableStateFlow(0)
    val gs2Lives = MutableStateFlow(3)
    val gs2IsGameOver = MutableStateFlow(false)
    private var itemIdCounter = 0

    // 🐦 Game 3: Flappy Hand State
    val gs3BirdY = MutableStateFlow(0.5f)
    val gs3BirdVelocity = MutableStateFlow(0f)
    val gs3Pipes = MutableStateFlow<List<FlappyPipe>>(emptyList())
    val gs3Score = MutableStateFlow(0)
    val gs3IsGameOver = MutableStateFlow(false)
    private var pipeIdCounter = 0

    // 🥎 Game 4: Balance Ball State
    val gs4BoardAngle = MutableStateFlow(0f) // Radians
    val gs4BallX = MutableStateFlow(0.5f) // Fraction 0f..1f
    val gs4BallVelocity = MutableStateFlow(0f)
    val gs4Score = MutableStateFlow(0)
    val gs4TimeLive = MutableStateFlow(0)
    val gs4IsGameOver = MutableStateFlow(false)

    // 🎹 Game 5: Rhythm Piano State
    val gs5Notes = MutableStateFlow<List<MusicNote>>(emptyList())
    val gs5Score = MutableStateFlow(0)
    val gs5Combo = MutableStateFlow(0)
    val gs5RequiredGestures = listOf(GestureType.PEACE, GestureType.SPOCK, GestureType.INDEX_POINT)
    private var noteIdCounter = 0

    // 🫧 Game 6: Bubble Popper State
    val gs6Bubbles = MutableStateFlow<List<PopBubble>>(emptyList())
    val gs6Score = MutableStateFlow(0)
    private var bubbleIdCounter = 0

    // 🥊 Game 7: Boxing Puncher State
    val gs7Score = MutableStateFlow(0)
    val gs7PowerMeter = MutableStateFlow(0f) // 0..100
    val gs7Message = MutableStateFlow("Đấm bóng để mài dũa thể lực!")
    val gs7BagsPopped = MutableStateFlow(0)

    // ✂️ Game 8: Rock Paper Scissors State
    val gs8UserChoice = MutableStateFlow<GestureType?>(null)
    val gs8AIChoice = MutableStateFlow<GestureType?>(null)
    val gs8ResultText = MutableStateFlow("Đợi lựa chọn...")
    val gs8PlayerScore = MutableStateFlow(0)
    val gs8AIScore = MutableStateFlow(0)

    // 🚀 Game 9: Space Shooter State
    val gs9PlayerX = MutableStateFlow(0.5f)
    val gs9Lasers = MutableStateFlow<List<LaserBeam>>(emptyList())
    val gs9Aliens = MutableStateFlow<List<AlienEnemy>>(emptyList())
    val gs9Score = MutableStateFlow(0)
    val gs9IsGameOver = MutableStateFlow(false)
    private var laserIdCounter = 0
    private var alienIdCounter = 0

    // 🔨 Game 10: Whack Mole State
    val gs10Moles = MutableStateFlow<List<WhackMole>>(
        listOf(
            WhackMole(0, 0.25f, 0.3f, false, 0),
            WhackMole(1, 0.5f, 0.3f, false, 0),
            WhackMole(2, 0.75f, 0.3f, false, 0),
            WhackMole(3, 0.25f, 0.6f, false, 0),
            WhackMole(4, 0.5f, 0.6f, false, 0),
            WhackMole(5, 0.75f, 0.6f, false, 0),
            WhackMole(6, 0.35f, 0.85f, false, 0),
            WhackMole(7, 0.65f, 0.85f, false, 0)
        )
    )
    val gs10Score = MutableStateFlow(0)
    val gs10TimeLeft = MutableStateFlow(30) // 30 seconds count

    // 🏄 Game 11: Wave Runner State
    val gs11PlayerLane = MutableStateFlow(1) // 0: Top, 1: Middle, 2: Bottom
    val gs11LaneYValues = listOf(0.25f, 0.50f, 0.75f)
    val gs11ObstacleX = MutableStateFlow(1.0f)
    val gs11ObstacleLane = MutableStateFlow(1)
    val gs11Score = MutableStateFlow(0)
    val gs11IsGameOver = MutableStateFlow(false)


    // --- EVERYDAY LIFE APPLICATIONS STATE VARIABLES ---

    // 🔐 LifeApp 1: Privacy App Lock State
    val ls1Status = MutableStateFlow("LOCKED") // "LOCKED", "UNLOCKED", "SETTING_PIN"
    val ls1SecretMessage = MutableStateFlow("HỒ SƠ KHẨN: Dự án AI Meta-Gesture đã thành công 100%. Mở khóa để nhận dữ liệu bí mật.")
    val ls1GesturePassword = MutableStateFlow(listOf(GestureType.THUMBS_UP, GestureType.PEACE, GestureType.FIST))
    val ls1ProgressSequence = MutableStateFlow<List<GestureType>>(emptyList())
    val ls1FeedbackMessage = MutableStateFlow("Thực hiện chuỗi mật mã bằng cử chỉ tay.")

    // 🏋️ LifeApp 2: Sports Tracker & Rep Counter
    val ls2Exercise = MutableStateFlow("Bicep Curl") // "Bicep Curl", "Squats", "Hít đất"
    val ls2Count = MutableStateFlow(0)
    val ls2TrackCycleState = MutableStateFlow("EXTENDED") // "FLEXED", "EXTENDED"
    val ls2StatusMessage = MutableStateFlow("Giữ tư thế thẳng trước webcam và tập luyện.")

    // 📊 LifeApp 3: Slide Presenter Pro
    val ls3ActiveSlide = MutableStateFlow(0)
    val ls3LaserActive = MutableStateFlow(false)
    val ls3LaserPosition = MutableStateFlow(Offset(0.5f, 0.5f))
    val ls3Feedback = MutableStateFlow("Gạt tay TRÁI / PHẢI hoặc dựng ngón trỏ kích Laser")
    val ls3Slides = listOf(
        "🔮 GESTUREQUEST ARENA - TƯƠNG LAI KHÔNG CHẠM\nKhai phá kỷ nguyên tương tác cử chỉ tay (MediaPipe)\nTự động hiệu chỉnh, chính xác tuyệt đỉnh.",
        "📱 CÔNG NGHỆ NHẬN DIỆN 21 LANDMARKS\nTheo dõi các đốt ngón tay từ 0 - 20.\nPhân tích góc gập, suy luận thời gian thực trạng thái co khớp.",
        "📈 TIỀM NĂNG ỨNG DỤNG THỰC TẾ\n- Trị liệu phục hồi khớp tay văn phòng\n- Trình diễn Slide không dây chuyên nghiệp\n- Kiểm soát nhà thông minh IoT rảnh tay",
        "💡 BẢO MẬT SINH TRẮC HỌC CỬ CHỈ CHIỀU CAO\nMã hóa chuỗi chu kỳ chuyển động sinh học tinh giản.\nTăng độ bảo mật vật lý lên hơn 240% so với mã PIN thông thường."
    )

    // 🏠 LifeApp 4: Smart Home Hub Config
    val ls4Appliances = MutableStateFlow(
        listOf(
            HomeAppliance("Đèn trần chính", "light", "💡", false, "Off", GestureType.THUMBS_UP),
            HomeAppliance("Điều hòa không khí", "ac", "❄️", false, "24°C", GestureType.SPOCK),
            HomeAppliance("Dàn âm thanh rạp hát", "stereo", "🎵", false, "Muted", GestureType.ROCK_ON),
            HomeAppliance("Tivi 8K Smart", "tv", "📺", false, "Chamber Live", GestureType.OK_SIGN)
        )
    )

    // 🎨 LifeApp 5: Air Paint Canvas Data
    val ls5Strokes = MutableStateFlow<List<PaintStroke>>(emptyList())
    val ls5ActiveBrushColor = MutableStateFlow(Color(0xFF00E5FF))
    val ls5ActiveBrushSize = MutableStateFlow(12f)

    // 🏺 LifeApp 6: 3D Clay Sculpting Data
    val ls6ClayScaleX = MutableStateFlow(1.0f)
    val ls6ClayScaleY = MutableStateFlow(1.0f)
    val ls6ClaySmoothness = MutableStateFlow(0.5f)
    val ls6ClayOutputName = MutableStateFlow("Khối Đất Sét Tròn")

    // 🎼 LifeApp 7: Music Conductor States
    val ls7TempoBPM = MutableStateFlow(100)
    val ls7VolumeDB = MutableStateFlow(75) // 0..100
    val ls7OrchestraInstruments = MutableStateFlow(
        mapOf("Violin 🎻" to true, "Pháo Brass 🎺" to true, "Sáo Flute 🪈" to true, "Trống Gõ 🥁" to true)
    )

    // 🧮 LifeApp 8: Smart Finger Counter & Calc
    val ls8Display = MutableStateFlow("Lòng bàn tay: xòe ngón để chọn con số (1-5)")
    val ls8Number1 = MutableStateFlow<Int?>(null)
    val ls8Number2 = MutableStateFlow<Int?>(null)
    val ls8Operator = MutableStateFlow<String?>(null) // "+", "-", "*", "/"
    val ls8Result = MutableStateFlow<Int?>(null)

    // 🚨 LifeApp 9: SOS Emergency Sequence
    val ls9CountdownProgress = MutableStateFlow(0f) // 0f to 3.0f seconds
    val ls9SOSTriggered = MutableStateFlow(false)
    val ls9SOSSirenBeeping = MutableStateFlow(false)

    // 🍳 LifeApp 10: Chef Pancake Master
    val ls10PancakeHeight = MutableStateFlow(0f) // 0..100
    val ls10PancakeVelocity = MutableStateFlow(0f)
    val ls10ChopProgress = MutableStateFlow(0) // target is 10 chops
    val ls10RecipeStatus = MutableStateFlow("Đang chuẩn bị: hất Pancakes!")

    // 🧘 LifeApp 11: Carpal Stretch Wellness Guide
    val ls11StretchText = MutableStateFlow("Bài 1: Nắm chặt năm ngón tay rướn thẳng (Nhấn chọn cử chỉ FIST)")
    val ls11StretchIndex = MutableStateFlow(0)
    val ls11StretchPoints = MutableStateFlow(0)


    // --- RESET MANAGEMENT ---

    private fun resetGameState(game: GameType) {
        val random = Random(System.currentTimeMillis())
        when (game) {
            GameType.FRUIT_SLICER -> {
                gs1Fruits.value = emptyList()
                gs1Score.value = 0
                gs1Lives.value = 3
                gs1IsGameOver.value = false
                fruitIdCounter = 0
            }
            GameType.GOLDEN_CATCH -> {
                gs2Items.value = emptyList()
                gs2Score.value = 0
                gs2Lives.value = 3
                gs2IsGameOver.value = false
                itemIdCounter = 0
            }
            GameType.FLAPPY_HAND -> {
                gs3BirdY.value = 0.5f
                gs3BirdVelocity.value = 0f
                gs3Pipes.value = emptyList()
                gs3Score.value = 0
                gs3IsGameOver.value = false
                pipeIdCounter = 0
            }
            GameType.BALANCE_BALL -> {
                gs4BoardAngle.value = 0f
                gs4BallX.value = 0.5f
                gs4BallVelocity.value = 0f
                gs4Score.value = 0
                gs4TimeLive.value = 0
                gs4IsGameOver.value = false
            }
            GameType.RHYTHM_PIANO -> {
                gs5Notes.value = emptyList()
                gs5Score.value = 0
                gs5Combo.value = 0
                noteIdCounter = 0
            }
            GameType.BUBBLE_POPPER -> {
                gs6Bubbles.value = emptyList()
                gs6Score.value = 0
                bubbleIdCounter = 0
            }
            GameType.VIRTUAL_BOXER -> {
                gs7Score.value = 0
                gs7PowerMeter.value = 0f
                gs7BagsPopped.value = 0
                gs7Message.value = "Nắm chặt Fist đấm tan bao cát!"
            }
            GameType.ROCK_PAPER_SCISSORS -> {
                gs8UserChoice.value = null
                gs8AIChoice.value = null
                gs8ResultText.value = "Đợi cử chỉ của bạn..."
            }
            GameType.SPACE_SHOOTER -> {
                gs9PlayerX.value = 0.5f
                gs9Lasers.value = emptyList()
                gs9Aliens.value = emptyList()
                gs9Score.value = 0
                gs9IsGameOver.value = false
                laserIdCounter = 0
                alienIdCounter = 0
            }
            GameType.WHACK_A_MOLE -> {
                gs10Score.value = 0
                gs10TimeLeft.value = 30
            }
            GameType.WAVE_RUNNER -> {
                gs11PlayerLane.value = 1
                gs11ObstacleX.value = 1.0f
                gs11ObstacleLane.value = random.nextInt(3)
                gs11Score.value = 0
                gs11IsGameOver.value = false
            }
        }
    }

    private fun resetLifeAppState(app: LifeAppType) {
        when (app) {
            LifeAppType.APP_LOCK -> {
                ls1Status.value = "LOCKED"
                ls1ProgressSequence.value = emptyList()
                ls1FeedbackMessage.value = "Hãy thực hiện cử chỉ ĐÚNG để mở phòng bí mật."
            }
            LifeAppType.SPORTS_TRACKER -> {
                ls2Count.value = 0
                ls2TrackCycleState.value = "EXTENDED"
                ls2StatusMessage.value = "Hệ thống đang hoạt động. Nắm đấm co lại rồi duỗi mở tay để đếm."
            }
            LifeAppType.SLIDE_PRESENTER -> {
                ls3ActiveSlide.value = 0
                ls3LaserActive.value = false
                ls3Feedback.value = "Thuyết trình chuyên nghiệp không cầm chuột!"
            }
            LifeAppType.SPATIAL_PAINT -> {
                ls5Strokes.value = emptyList()
            }
            LifeAppType.CLAY_SCULPT -> {
                ls6ClayScaleX.value = 1.0f
                ls6ClayScaleY.value = 1.0f
                ls6ClaySmoothness.value = 0.5f
                ls6ClayOutputName.value = "Đất Sét Sơ Khai"
            }
            LifeAppType.SOS_EMERGENCY -> {
                ls9CountdownProgress.value = 0f
                ls9SOSTriggered.value = false
                ls9SOSSirenBeeping.value = false
            }
            LifeAppType.CHEF_MASTER -> {
                ls10PancakeHeight.value = 0f
                ls10ChopProgress.value = 0
                ls10RecipeStatus.value = "Đang chuẩn bị: Sôi nổi!"
            }
            LifeAppType.CARPAL_STRETCH -> {
                ls11StretchIndex.value = 0
                ls11StretchPoints.value = 0
                ls11StretchText.value = "Bài 1: Khép chặt Nắm Đấm rốn cơ xương tay (FIST)"
            }
            else -> {}
        }
    }

    // --- RECTIFY GESTURE TRIGGERS ---

    private fun onGestureChanged(gesture: GestureType) {
        // App Lock checks
        if (_selectedLifeApp.value == LifeAppType.APP_LOCK) {
            val pass = ls1GesturePassword.value
            val currentProg = ls1ProgressSequence.value.toMutableList()

            // Only append different gesture
            if (ls1Status.value != "UNLOCKED") {
                if (currentProg.isEmpty() || currentProg.last() != gesture) {
                    currentProg.add(gesture)
                    ls1ProgressSequence.value = currentProg
                    ls1FeedbackMessage.value = "Khớp cử danh sách khóa: " + currentProg.joinToString(" ➔ ") { it.emoji }

                    // Check PIN length
                    if (currentProg.size >= pass.size) {
                        var matched = true
                        for (idx in pass.indices) {
                            if (currentProg[idx] != pass[idx]) matched = false
                        }
                        if (matched) {
                            ls1Status.value = "UNLOCKED"
                            ls1FeedbackMessage.value = "✅ XÁC THỰC SINH TRẮC HỌC THÀNH CÔNG! ĐÃ MỞ KHÓA."
                            _appScore.value += 100
                        } else {
                            ls1ProgressSequence.value = emptyList()
                            ls1FeedbackMessage.value = "❌ Sai mã cử chỉ sinh trắc! Quay lại khóa ban đầu."
                        }
                    }
                }
            }
        }

        // Sports Tracker rep counting
        if (_selectedLifeApp.value == LifeAppType.SPORTS_TRACKER) {
            val cState = ls2TrackCycleState.value
            if (gesture == GestureType.FIST && cState == "EXTENDED") {
                ls2TrackCycleState.value = "FLEXED"
                ls2StatusMessage.value = "Đang co cơ nâng lực! Duỗi tay xòe ra để tính 1 nhịp."
            } else if (gesture == GestureType.PALM && cState == "FLEXED") {
                ls2TrackCycleState.value = "EXTENDED"
                val newCount = ls2Count.value + 1
                ls2Count.value = newCount
                ls2StatusMessage.value = "💪 Đã thực hiện chính xác $newCount nhịp tập xịn mịn! Rất tốt!"
                _appScore.value += 10
            }
        }

        // SOS sustained check
        if (_selectedLifeApp.value == LifeAppType.SOS_EMERGENCY) {
            if (gesture != GestureType.FIST) {
                ls9CountdownProgress.value = 0f
                ls9SOSSirenBeeping.value = false
            }
        }

        // Space Invaders laser fire
        if (_selectedGame.value == GameType.SPACE_SHOOTER && !gs9IsGameOver.value) {
            if (gesture == GestureType.PINCH) {
                fireSpaceLaser()
            }
        }

        // Flappy Hand Flap action
        if (_selectedGame.value == GameType.FLAPPY_HAND && !gs3IsGameOver.value) {
            if (gesture == GestureType.PINCH || gesture == GestureType.PALM) {
                gs3BirdVelocity.value = -0.05f // Move up
            }
        }

        // Carpal stretch wellness cycle
        if (_selectedLifeApp.value == LifeAppType.CARPAL_STRETCH) {
            val sIdx = ls11StretchIndex.value
            if (sIdx == 0 && gesture == GestureType.FIST) {
                ls11StretchIndex.value = 1
                ls11StretchPoints.value += 20
                _appScore.value += 5
                ls11StretchText.value = "Bài 2: Tỏa rộng cả 5 ngón tay mát xa mô (Chọn cử chỉ PALM)"
            } else if (sIdx == 1 && gesture == GestureType.PALM) {
                ls11StretchIndex.value = 2
                ls11StretchPoints.value += 20
                _appScore.value += 5
                ls11StretchText.value = "Bài 3: Nhéo kéo duỗi dẻo ngón cái - ngón trỏ (Chọn cử chỉ PINCH)"
            } else if (sIdx == 2 && gesture == GestureType.PINCH) {
                ls11StretchIndex.value = 3
                ls11StretchPoints.value += 20
                _appScore.value += 5
                ls11StretchText.value = "Bài 4: Chào Spock liên kết hệ thần kinh vận động (Chọn cử chỉ SPOCK)"
            } else if (sIdx == 3 && gesture == GestureType.SPOCK) {
                ls11StretchIndex.value = 0
                ls11StretchPoints.value += 40
                _appScore.value += 10
                ls11StretchText.value = "Thành công hoàn chỉnh 1 chu kỳ! Bài 1: Co chặt Nắm Đấm (FIST)"
            }
        }

        // Intelligent Calculator Operator setup
        if (_selectedLifeApp.value == LifeAppType.G_CALCULATOR) {
            when (gesture) {
                GestureType.OK_SIGN -> {
                    ls8Operator.value = "+"
                    ls8Display.value = "Phép toán: CỘNG (+). Xòe tay nhập số hạng thứ 2"
                    ls8Result.value = null
                }
                GestureType.PEACE -> {
                    ls8Operator.value = "-"
                    ls8Display.value = "Phép toán: TRỪ (-). Xòe tay nhập số hạng thứ 2"
                    ls8Result.value = null
                }
                GestureType.ROCK_ON -> {
                    ls8Operator.value = "*"
                    ls8Display.value = "Phép toán: NHÂN (*). Xòe tay nhập số hạng thứ 2"
                    ls8Result.value = null
                }
                GestureType.FIST -> {
                    // Trigger Evaluation "="
                    val op = ls8Operator.value
                    val n1 = ls8Number1.value
                    val n2 = ls8Number2.value
                    if (op != null && n1 != null && n2 != null) {
                        val res = when (op) {
                            "+" -> n1 + n2
                            "-" -> n1 - n2
                            "*" -> n1 * n2
                            else -> 0
                        }
                        ls8Result.value = res
                        ls8Display.value = "KẾT QUẢ: $n1 $op $n2 = $res 🎉 (Chọn PALM để xóa nháp!)"
                        _appScore.value += 10
                    } else {
                        ls8Display.value = "Cần nạp đủ 2 số hạng và phép tính trước khi chọn FIST để lấy két quả!"
                    }
                }
                GestureType.PALM -> {
                    // Clear state
                    ls8Number1.value = null
                    ls8Number2.value = null
                    ls8Operator.value = null
                    ls8Result.value = null
                    ls8Display.value = "Khởi tạo máy tính! Xòe 1-5 ngón để nhập số hạng 1."
                }
                else -> {}
            }
        }

        // Smart Home gesture trigger
        if (_selectedLifeApp.value == LifeAppType.SMART_HOME) {
            val updatedAppliances = ls4Appliances.value.map { item ->
                if (item.gestureMapped == gesture) {
                    val flag = !item.isOn
                    _appScore.value += 5
                    item.copy(
                        isOn = flag,
                        value = if (flag) "Hoạt Động 🟢" else "Đang Tắt 🔴"
                    )
                } else {
                    item
                }
            }
            ls4Appliances.value = updatedAppliances
        }

        // Music Conductor gesture trigger
        if (_selectedLifeApp.value == LifeAppType.MUSIC_CONDUCTOR) {
            if (gesture == GestureType.THUMBS_UP) {
                ls7VolumeDB.value = (ls7VolumeDB.value + 10).coerceIn(0, 100)
            } else if (gesture == GestureType.THUMBS_DOWN) {
                ls7VolumeDB.value = (ls7VolumeDB.value - 10).coerceIn(0, 100)
            }
        }

        // Chef Master gesture trigger
        if (_selectedLifeApp.value == LifeAppType.CHEF_MASTER) {
            if (gesture == GestureType.INDEX_POINT || gesture == GestureType.GUN || gesture == GestureType.THUMBS_UP) {
                quickFlickPancake()
            } else if (gesture == GestureType.PALM || gesture == GestureType.PINCH || gesture == GestureType.FIST) {
                chopVegetables()
            }
        }
    }

    private fun onPointerMoved(x: Float, y: Float) {
        // Line trace in paint strokes
        if (_selectedLifeApp.value == LifeAppType.SPATIAL_PAINT) {
            if (_activeGesture.value == GestureType.INDEX_POINT) {
                val currentStrokes = ls5Strokes.value.toMutableList()
                if (currentStrokes.isNotEmpty() && currentStrokes.last().points.size < 50) {
                    val last = currentStrokes.removeAt(currentStrokes.size - 1)
                    val pts = last.points.toMutableList()
                    pts.add(Offset(x, y))
                    currentStrokes.add(PaintStroke(pts, last.color, last.size))
                    ls5Strokes.value = currentStrokes
                } else {
                    ls5Strokes.value = currentStrokes + PaintStroke(listOf(Offset(x, y)), ls5BrushColor(ls5Strokes.value.size), ls5ActiveBrushSize.value)
                }
            } else if (_activeGesture.value == GestureType.OK_SIGN) {
                ls5Strokes.value = emptyList() // clear air painting
            }
        }

        // Sculpting stretch calculations
        if (_selectedLifeApp.value == LifeAppType.CLAY_SCULPT) {
            val dist = sqrt((x - 0.5f) * (x - 0.5f) + (y - 0.5f) * (y - 0.5f))
            if (_activeGesture.value == GestureType.PINCH) {
                ls6ClayScaleX.value = (0.5f + dist * 1.5f).coerceIn(0.3f, 2.5f)
                ls6ClayScaleY.value = (1.5f - dist * 1.1f).coerceIn(0.3f, 2.5f)
                ls6ClaySmoothness.value = (dist * 2f).coerceIn(0f, 1f)
                ls6ClayOutputName.value = when {
                    ls6ClayScaleX.value > 1.6f && ls6ClayScaleY.value < 0.6f -> "Đĩa Sứ Thượng Uyển 🍽️"
                    ls6ClayScaleX.value < 0.7f && ls6ClayScaleY.value > 1.6f -> "Bình Cổ Tràng Tiền 🏺"
                    ls6ClayScaleX.value > 1.2f && ls6ClayScaleY.value > 1.2f -> "Bánh Donut Gốm 🍩"
                    else -> "Bát Sứ Men Ngọc 🥣"
                }
            }
        }

        // Live slide cursor tracker
        if (_selectedLifeApp.value == LifeAppType.SLIDE_PRESENTER) {
            ls3LaserPosition.value = Offset(x, y)
            ls3LaserActive.value = (_activeGesture.value == GestureType.INDEX_POINT)
        }

        // Smart Calculator finger numeric input
        if (_selectedLifeApp.value == LifeAppType.G_CALCULATOR && ls8Result.value == null) {
            val fingersInput = when {
                y < 0.25f -> 5
                y < 0.45f -> 4
                y < 0.65f -> 3
                y < 0.85f -> 2
                else -> 1
            }
            if (ls8Operator.value == null) {
                ls8Number1.value = fingersInput
                ls8Display.value = "Số thứ nhất: $fingersInput. Chọn cử chỉ OK (+), PEACE (-), ROCK (*) để chọn phép tính."
            } else {
                ls8Number2.value = fingersInput
                ls8Display.value = "Số thứ hai: $fingersInput. Nhấn nắm tay FIST để kết xuất kết quả!"
            }
        }
    }

    private fun ls5BrushColor(index: Int): Color {
        val list = listOf(Color(0xFF00E5FF), Color(0xFFFF1744), Color(0xFF00E676), Color(0xFFFFEA00), Color(0xFFD500F9))
        return list[index % list.size]
    }

    // Dynamic Swipe Detection
    private var lastPointerX = 0.5f
    fun evaluateSwipeDetection(newX: Float) {
        val diff = newX - lastPointerX
        if (abs(diff) > 0.17f) {
            if (diff > 0) {
                // Swipe R
                onSwipeRight()
            } else {
                // Swipe L
                onSwipeLeft()
            }
        }
        lastPointerX = newX
    }

    private fun onSwipeLeft() {
        if (_selectedLifeApp.value == LifeAppType.SLIDE_PRESENTER) {
            val idx = ls3ActiveSlide.value
            if (idx > 0) {
                ls3ActiveSlide.value = idx - 1
                ls3Feedback.value = "⏪ Trượt sang slide lúc trước!"
            }
        }
        if (_selectedLifeApp.value == LifeAppType.MUSIC_CONDUCTOR) {
            ls7TempoBPM.value = (ls7TempoBPM.value - 10).coerceIn(60, 200)
        }
    }

    private fun onSwipeRight() {
        if (_selectedLifeApp.value == LifeAppType.SLIDE_PRESENTER) {
            val idx = ls3ActiveSlide.value
            if (idx < ls3Slides.size - 1) {
                ls3ActiveSlide.value = idx + 1
                ls3Feedback.value = "⏩ Tiến hành cuộn sang slide kế tiếp!"
            }
        }
        if (_selectedLifeApp.value == LifeAppType.MUSIC_CONDUCTOR) {
            ls7TempoBPM.value = (ls7TempoBPM.value + 10).coerceIn(60, 200)
        }
    }


    // --- GAME ENGINE PROCESS TACTICS ---

    private fun tickActiveGame() {
        val random = Random(System.currentTimeMillis())

        when (_selectedGame.value) {
            GameType.FRUIT_SLICER -> {
                if (gs1IsGameOver.value) return
                // Move fruits
                val current = gs1Fruits.value.map { fruit ->
                    fruit.y += fruit.speedY
                    fruit.x += fruit.speedX
                    // Gravity
                    fruit.speedY += 0.003f
                    fruit
                }.filter { fruit ->
                    val out = fruit.y > 1.1f
                    if (out && !fruit.isSliced && fruit.type != "💣") {
                        // Missed fruit! Lose a life
                        val lv = gs1Lives.value - 1
                        gs1Lives.value = lv
                        if (lv <= 0) {
                            gs1IsGameOver.value = true
                            updateGameScore(GameType.FRUIT_SLICER.id, gs1Score.value)
                        }
                    }
                    fruit.y <= 1.1f && fruit.x in -0.1f..1.1f
                }.toMutableList()

                // Spawn fruits
                if (random.nextFloat() < 0.04f && current.size < 6) {
                    val listTypes = listOf("🍉", "🍊", "🥥", "💣")
                    val type = listTypes[random.nextInt(listTypes.size)]
                    val color = when (type) {
                        "🍉" -> Color(0xFF4CAF50)
                        "🍊" -> Color(0xFFFF9800)
                        "🥥" -> Color(0xFF795548)
                        else -> Color(0xFFE91E63)
                    }
                    current.add(
                        Fruit(
                            id = fruitIdCounter++,
                            type = type,
                            x = random.nextFloat() * 0.8f + 0.1f,
                            y = 1.0f,
                            speedX = (random.nextFloat() - 0.5f) * 0.015f,
                            speedY = -0.07f - random.nextFloat() * 0.03f,
                            color = color
                        )
                    )
                }

                // Check slices using PointerX and PointerY
                val px = _pointerX.value
                val py = _pointerY.value
                val indexPointer = _activeGesture.value == GestureType.INDEX_POINT || _activeGesture.value == GestureType.PALM

                current.forEach { fruit ->
                    if (!fruit.isSliced && indexPointer) {
                        val dx = fruit.x - px
                        val dy = fruit.y - py
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 0.11f) {
                            fruit.isSliced = true
                            if (fruit.type == "💣") {
                                // Boom! Instant Game Over
                                gs1IsGameOver.value = true
                                updateGameScore(GameType.FRUIT_SLICER.id, gs1Score.value)
                            } else {
                                gs1Score.value += 10
                            }
                        }
                    }
                }

                gs1Fruits.value = current
            }

            GameType.GOLDEN_CATCH -> {
                if (gs2IsGameOver.value) return
                // Check items falling
                val basketX = _pointerX.value
                val items = gs2Items.value.map { item ->
                    item.y += item.speedY
                    item
                }.filter { item ->
                    val out = item.y > 0.95f
                    if (out && !item.isCaught) {
                        if (item.isApple) {
                            val lv = gs2Lives.value - 1
                            gs2Lives.value = lv
                            if (lv <= 0) {
                                gs2IsGameOver.value = true
                                updateGameScore(GameType.GOLDEN_CATCH.id, gs2Score.value)
                            }
                        }
                    }
                    item.y <= 1.0f
                }.toMutableList()

                // Spawn item
                if (random.nextFloat() < 0.05f && items.size < 5) {
                    val isApple = random.nextFloat() < 0.75f
                    items.add(
                        FallingItem(
                            id = itemIdCounter++,
                            isApple = isApple,
                            x = random.nextFloat() * 0.8f + 0.1f,
                            y = 0.0f,
                            speedY = 0.02f + random.nextFloat() * 0.02f
                        )
                    )
                }

                // Catch detection with Basket!
                val gesture = _activeGesture.value
                items.forEach { item ->
                    if (!item.isCaught && item.y in 0.80f..0.92f) {
                        val dx = abs(item.x - basketX)
                        if (dx < 0.16f) {
                            // Caught! Check gesture rule
                            item.isCaught = true
                            if (gesture == GestureType.PALM) {
                                if (item.isApple) {
                                    gs2Score.value += 15
                                } else {
                                    // Bomb caught in open palm!
                                    val lv = gs2Lives.value - 1
                                    gs2Lives.value = lv
                                    if (lv <= 0) {
                                        gs2IsGameOver.value = true
                                        updateGameScore(GameType.GOLDEN_CATCH.id, gs2Score.value)
                                    }
                                }
                            } else {
                                // If it is FIST or others, let's treat closed fist as avoiding bomb capture safely
                                item.isCaught = false
                            }
                        }
                    }
                }

                gs2Items.value = items
            }

            GameType.FLAPPY_HAND -> {
                if (gs3IsGameOver.value) return
                // Gravity on bird
                gs3BirdVelocity.value += 0.002f
                val py = (gs3BirdY.value + gs3BirdVelocity.value).coerceIn(0.01f, 0.99f)
                gs3BirdY.value = py

                // Move pipes
                val pipes = gs3Pipes.value.map { pipe ->
                    pipe.x -= 0.015f
                    pipe
                }.filter { pipe ->
                    val passX = pipe.x in 0.18f..0.21f
                    if (passX) {
                        // Add score when crossing
                        gs3Score.value += 1
                    }
                    pipe.x > -0.1f
                }.toMutableList()

                // Spawn pipe
                if (pipes.isEmpty() || (pipes.last().x < 0.55f)) {
                    pipes.add(
                        FlappyPipe(
                            id = pipeIdCounter++,
                            x = 1.0f,
                            gapY = 0.35f + random.nextFloat() * 0.3f
                        )
                    )
                }

                // Check collision
                pipes.forEach { pipe ->
                    if (abs(pipe.x - 0.2f) < 0.08f) {
                        // check Y collision
                        val inGap = py > (pipe.gapY - pipe.gapSize / 2f) && py < (pipe.gapY + pipe.gapSize / 2f)
                        if (!inGap) {
                            gs3IsGameOver.value = true
                            updateGameScore(GameType.FLAPPY_HAND.id, gs3Score.value)
                        }
                    }
                }

                gs3Pipes.value = pipes
            }

            GameType.BALANCE_BALL -> {
                if (gs4IsGameOver.value) return
                val angle = gs4BoardAngle.value
                // Tilt board using hand pose relative
                val handTilt = (_pointerX.value - 0.5f) * 1.5f
                gs4BoardAngle.value = handTilt

                // Physics of ball
                gs4BallVelocity.value += sin(angle) * 0.005f
                val bx = gs4BallX.value + gs4BallVelocity.value
                gs4BallX.value = bx

                // Check boundaries
                if (bx < 0.0f || bx > 1.0f) {
                    gs4IsGameOver.value = true
                    updateGameScore(GameType.BALANCE_BALL.id, gs4Score.value)
                } else {
                    gs4TimeLive.value += 1
                    if (gs4TimeLive.value % 20 == 0) {
                        gs4Score.value += 5
                    }
                }
            }

            GameType.RHYTHM_PIANO -> {
                // Ticking piano falling notes
                val notes = gs5Notes.value.map { note ->
                    note.y += 0.012f
                    note
                }.filter { note ->
                    if (note.y > 1.0f && !note.isHit) {
                        note.isMissed = true
                        gs5Combo.value = 0
                    }
                    note.y <= 1.0f
                }.toMutableList()

                // Spawn notes
                if (random.nextFloat() < 0.035f && notes.size < 4) {
                    notes.add(
                        MusicNote(
                            id = noteIdCounter++,
                            lane = random.nextInt(3),
                            y = 0.0f
                        )
                    )
                }

                // Hit trigger is done in click/auto
                gs5Notes.value = notes
            }

            GameType.BUBBLE_POPPER -> {
                // Ticking bubbles
                val bubbles = gs6Bubbles.value.map { bubble ->
                    bubble.y -= bubble.speed
                    bubble
                }.filter { bubble ->
                    bubble.y > -0.1f
                }.toMutableList()

                // Spawn bubbles
                if (random.nextFloat() < 0.08f && bubbles.size < 12) {
                    val listColors = listOf(Color(0xFF00E5FF), Color(0xFFFF1744), Color(0xFF00E676), Color(0xFFFFEA00), Color(0xFFE040FB))
                    bubbles.add(
                        PopBubble(
                            id = bubbleIdCounter++,
                            x = random.nextFloat() * 0.85f + 0.07f,
                            y = 1.05f,
                            size = 35f + random.nextFloat() * 40f,
                            color = listColors[random.nextInt(listColors.size)],
                            speed = 0.004f + random.nextFloat() * 0.01f
                        )
                    )
                }

                // Pop detection using Pointer
                val px = _pointerX.value
                val py = _pointerY.value
                val indexPointer = _activeGesture.value == GestureType.INDEX_POINT || _activeGesture.value == GestureType.PALM

                if (indexPointer) {
                    bubbles.filter { !it.isYOut(py) }.forEach { b ->
                        val dx = b.x - px
                        val dy = b.y - py
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 0.12f) {
                            bubbles.remove(b)
                            gs6Score.value += 12
                            updateGameScore(GameType.BUBBLE_POPPER.id, gs6Score.value)
                        }
                    }
                }

                gs6Bubbles.value = bubbles
            }

            GameType.SPACE_SHOOTER -> {
                if (gs9IsGameOver.value) return
                // Player ship moves with pointer
                gs9PlayerX.value = _pointerX.value

                // Move lasers
                val lasers = gs9Lasers.value.map { laser ->
                    laser.y -= laser.speedY
                    laser
                }.filter { laser -> laser.y > -0.05f }

                // Move aliens
                val aliens = gs9Aliens.value.map { alien ->
                    alien.y += 0.006f
                    alien
                }.filter { alien ->
                    val hitPlayer = alien.y > 0.85f && abs(alien.x - gs9PlayerX.value) < 0.12f
                    if (hitPlayer || alien.y > 1.0f) {
                        gs9IsGameOver.value = true
                        updateGameScore(GameType.SPACE_SHOOTER.id, gs9Score.value)
                    }
                    alien.y <= 1.0f
                }.toMutableList()

                // Spawn aliens
                if (random.nextFloat() < 0.035f && aliens.size < 5) {
                    aliens.add(
                        AlienEnemy(
                            id = alienIdCounter++,
                            x = random.nextFloat() * 0.8f + 0.1f,
                            y = 0.0f,
                            speedX = 0f
                        )
                    )
                }

                // Collision Laser <-> Alien
                val updatedLasers = lasers.toMutableList()
                val updatedAliens = aliens.toMutableList()

                val lasersToDel = mutableListOf<LaserBeam>()
                val aliensToDel = mutableListOf<AlienEnemy>()

                updatedLasers.forEach { laser ->
                    updatedAliens.forEach { alien ->
                        val dx = abs(laser.x - alien.x)
                        val dy = abs(laser.y - alien.y)
                        if (dx < 0.08f && dy < 0.08f) {
                            lasersToDel.add(laser)
                            aliensToDel.add(alien)
                            gs9Score.value += 20
                            updateGameScore(GameType.SPACE_SHOOTER.id, gs9Score.value)
                        }
                    }
                }

                updatedLasers.removeAll(lasersToDel)
                updatedAliens.removeAll(aliensToDel)

                gs9Lasers.value = updatedLasers
                gs9Aliens.value = updatedAliens
            }

            GameType.WHACK_A_MOLE -> {
                // Ticking remaining time
                val currentMoles = gs10Moles.value.map {
                    val trigger = random.nextFloat() < 0.02f
                    if (trigger && !it.isUp) {
                        it.isUp = true
                        it.upDuration = 30 + random.nextInt(40)
                    }

                    if (it.isUp) {
                        it.upDuration--
                        if (it.upDuration <= 0) {
                            it.isUp = false
                        }
                    }
                    it
                }
                gs10Moles.value = currentMoles

                // Point Check index pointwhack
                val px = _pointerX.value
                val py = _pointerY.value
                val indexPointer = _activeGesture.value == GestureType.INDEX_POINT || _activeGesture.value == GestureType.PALM

                if (indexPointer) {
                    currentMoles.forEach { mole ->
                        if (mole.isUp) {
                            val dx = abs(mole.x - px)
                            val dy = abs(mole.y - py)
                            if (dx < 0.12f && dy < 0.12f) {
                                mole.isUp = false
                                gs10Score.value += 25
                                updateGameScore(GameType.WHACK_A_MOLE.id, gs10Score.value)
                            }
                        }
                    }
                }
            }

            GameType.WAVE_RUNNER -> {
                if (gs11IsGameOver.value) return
                // Lane controller with ThumbsUp / ThumbsDown
                val gesture = _activeGesture.value
                if (gesture == GestureType.THUMBS_UP) {
                    gs11PlayerLane.value = 0 // jump to Top
                } else if (gesture == GestureType.THUMBS_DOWN) {
                    gs11PlayerLane.value = 2 // slide to Bottom
                } else {
                    gs11PlayerLane.value = 1 // stay in Middle
                }

                // Move obstacles
                val ox = gs11ObstacleX.value - 0.024f
                if (ox < -0.1f) {
                    gs11ObstacleX.value = 1.1f
                    gs11ObstacleLane.value = random.nextInt(3)
                    gs11Score.value += 10
                    updateGameScore(GameType.WAVE_RUNNER.id, gs11Score.value)
                } else {
                    gs11ObstacleX.value = ox
                }

                // Collision
                if (abs(gs11ObstacleX.value - 0.25f) < 0.08f) {
                    if (gs11ObstacleLane.value == gs11PlayerLane.value) {
                        gs11IsGameOver.value = true
                        updateGameScore(GameType.WAVE_RUNNER.id, gs11Score.value)
                    }
                }
            }

            else -> {}
        }


        // Smart Life App 9: SOS Emergency ticking
        if (_selectedLifeApp.value == LifeAppType.SOS_EMERGENCY && !ls9SOSTriggered.value) {
            if (_activeGesture.value == GestureType.FIST) {
                val current = ls9CountdownProgress.value + 0.033f // add ~33ms
                ls9CountdownProgress.value = current
                if (current >= 3.0f) {
                    ls9SOSTriggered.value = true
                    ls9SOSSirenBeeping.value = true
                    _appScore.value += 200
                }
            }
        }

        // Conductor Speed ticking
        if (_selectedLifeApp.value == LifeAppType.MUSIC_CONDUCTOR) {
            // Volume modulated with thumbs up/down gestures.
            // The ticking loop can animate the conductor baton movement.
        }

        // Live App 10: Chef pancakes gravity
        if (_selectedLifeApp.value == LifeAppType.CHEF_MASTER) {
            val ht = ls10PancakeHeight.value
            val vY = ls10PancakeVelocity.value
            if (ht > 0f) {
                ls10PancakeVelocity.value = vY - 0.8f // fall down gravity
                ls10PancakeHeight.value = (ht + ls10PancakeVelocity.value).coerceAtLeast(0f)
                if (ls10PancakeHeight.value == 0f) {
                    // landed pancake back down!
                    ls10RecipeStatus.value = "Chạm chảo phẳng tốt! Hãy gạt hất tiếp."
                }
            }
        }
    }

    private fun PopBubble.isYOut(py: Float): Boolean {
        return abs(this.y - py) > 0.15f
    }

    private fun fireSpaceLaser() {
        val lasers = gs9Lasers.value.toMutableList()
        lasers.add(LaserBeam(laserIdCounter++, gs9PlayerX.value, 0.8f, 0.04f))
        gs9Lasers.value = lasers
    }


    // Trigger rapid punch in Boxer Arena
    fun punchBoxerTarget() {
        if (_selectedGame.value == GameType.VIRTUAL_BOXER) {
            val currentPower = gs7PowerMeter.value
            if (currentPower < 100f) {
                val add = 15f
                gs7PowerMeter.value = (currentPower + add).coerceAtMost(100f)
                if (currentPower + add >= 100f) {
                    gs7BagsPopped.value += 1
                    gs7Score.value += 50
                    updateGameScore(GameType.VIRTUAL_BOXER.id, gs7Score.value)
                    gs7Message.value = "CHÚC MỪNG! Bao cát đã nổ tung rực rỡ! 💥 (+50 điểm)"
                    gs7PowerMeter.value = 0f
                } else {
                    gs7Message.value = "Đang tích nén kình lực đấm! " + (currentPower + add).toInt() + "%"
                }
            }
        }
    }

    // Rock Paper Scissors fighter duel
    fun playRPSGame(userMove: GestureType) {
        if (_selectedGame.value == GameType.ROCK_PAPER_SCISSORS) {
            gs8UserChoice.value = userMove
            val list = listOf(GestureType.FIST, GestureType.PALM, GestureType.PEACE)
            val random = Random(System.currentTimeMillis())
            val botMove = list[random.nextInt(list.size)]
            gs8AIChoice.value = botMove

            val result = evaluateRPSLocal(userMove, botMove)
            gs8ResultText.value = result

            if (result.contains("BẠN THẮNG")) {
                gs8PlayerScore.value += 1
                updateGameScore(GameType.ROCK_PAPER_SCISSORS.id, gs8PlayerScore.value)
            } else if (result.contains("AI THẮNG")) {
                gs8AIScore.value += 1
            }
        }
    }

    private fun evaluateRPSLocal(user: GestureType, bot: GestureType): String {
        if (user == bot) return "HÒA NHAU! Cả hai chọn " + user.vietnameseName
        return when {
            user == GestureType.FIST && bot == GestureType.PEACE -> "🏆 BẠN THẮNG! Đá nghiền nát Kéo."
            user == GestureType.PALM && bot == GestureType.FIST -> "🏆 BẠN THẮNG! Bao gói trọn Đá."
            user == GestureType.PEACE && bot == GestureType.PALM -> "🏆 BẠN THẮNG! Kéo rọc ngọt ngào Bao."
            bot == GestureType.FIST && user == GestureType.PEACE -> "🤖 AI THẮNG! Đá huỷ diệt Kéo."
            bot == GestureType.PALM && user == GestureType.FIST -> "🤖 AI THẮNG! Bao bọc gọn Đá."
            bot == GestureType.PEACE && user == GestureType.PALM -> "🤖 AI THẮNG! Kéo rọc nát Bao."
            else -> "Hòa cùng vui vẻ!"
        }
    }

    // Trigger hit on Rhythmic notes
    fun tapPianoChord(lane: Int) {
        if (_selectedGame.value == GameType.RHYTHM_PIANO) {
            var hit = false
            val updatedNotes = gs5Notes.value.map { note ->
                if (note.lane == lane && abs(note.y - 0.85f) < 0.15f && !note.isHit) {
                    note.isHit = true
                    hit = true
                }
                note
            }
            if (hit) {
                gs5Score.value += 20
                gs5Combo.value += 1
                updateGameScore(GameType.RHYTHM_PIANO.id, gs5Score.value)
            } else {
                gs5Combo.value = 0
            }
            gs5Notes.value = updatedNotes
        }
    }

    // Swipe slide controls
    fun selectSlide(index: Int) {
        if (index in gs3Pipes.value.indices) {
            ls3ActiveSlide.value = index
        }
    }

    // Smart Home hub toggles
    fun toggleSmartDevice(applianceId: String) {
        val list = ls4Appliances.value.map { item ->
            if (item.id == applianceId) {
                val flag = !item.isOn
                _appScore.value += 10
                item.copy(
                    isOn = flag,
                    value = if (flag) "Hoạt Động 🟢" else "Đang Tắt 🔴"
                )
            } else {
                item
            }
        }
        ls4Appliances.value = list
    }

    fun quickFlickPancake() {
        if (ls10PancakeHeight.value == 0f) {
            ls10PancakeHeight.value = 1f // trigger jump animation
            ls10PancakeVelocity.value = 12f // speed flip
            ls10RecipeStatus.value = "🔥 Lật bánh Pancakes thành công mĩ mãn!"
            _appScore.value += 15
        }
    }

    fun chopVegetables() {
        val chopped = ls10ChopProgress.value + 1
        ls10ChopProgress.value = chopped
        if (chopped >= 10) {
            ls10ChopProgress.value = 0
            ls10RecipeStatus.value = "🥕 Đã cắt nhỏ 1 đĩa Carrot đầy chất xơ!"
            _appScore.value += 25
        } else {
            ls10RecipeStatus.value = "Chop: " + chopped + "/10 lát"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
