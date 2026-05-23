package com.example.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class GestureType(
    val id: String,
    val displayName: String,
    val vietnameseName: String,
    val emoji: String,
    val description: String
) {
    PALM("palm", "Open Palm", "Bàn tay mở", "✋", "Căng rộng cả 5 ngón tay"),
    FIST("fist", "Closed Fist", "Nắm đấm", "✊", "Búi chặt tất cả các ngón tay"),
    INDEX_POINT("index_point", "Index Point", "Chỉ ngón trỏ", "☝️", "Chỉ thẳng ngón trỏ, các ngón khác co"),
    PEACE("peace", "Peace Sign", "Cử chỉ chữ V/Kéo", "✌️", "Mở ngón trỏ và ngón giữa"),
    SPOCK("spock", "Spock Sign", "Cử chỉ Spock", "🖖", "Chia tách ngón trỏ-giữa và áp út-út"),
    THUMBS_UP("thumbs_up", "Thumbs Up", "Thích (Thumbs Up)", "👍", "Dựng thẳng ngón cái hướng lên"),
    THUMBS_DOWN("thumbs_down", "Thumbs Down", "Không thích", "👎", "Dựng thẳng ngón cái hướng xuống"),
    OK_SIGN("ok_sign", "OK Sign", "Cử chỉ OK", "👌", "Ngón cái chạm ngón trỏ thành vòng tròn"),
    ROCK_ON("rock_on", "Rock Sign", "Cử chỉ Rock", "🤘", "Ngón trỏ và út đứng thẳng"),
    PINCH("pinch", "Pinch Gesture", "Cử chỉ Kẹp/Nhéo", "🤏", "Ngón cái và ngón trỏ ép sát lại"),
    GUN("gun", "Hand Gun", "Hình súng cầm tay", "👉", "Ngón trỏ chỉ ngang và ngón cái dựng lên")
}

data class HandLandmark(
    val id: Int,
    val name: String,
    var x: Float, // 0f to 1f
    var y: Float, // 0f to 1f
    var z: Float = 0f
)

enum class GameType(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val requiredGesture: GestureType,
    val gestureHint: String
) {
    FRUIT_SLICER(
        "fruit_slicer",
        "Chém Hoa Quả (Fruit Slicer)",
        "🍉",
        "Sử dụng ngón trỏ để chém các loại quả bay trên màn hình. Tránh chém trúng bom!",
        GestureType.INDEX_POINT,
        "Dùng cử chỉ 'Chỉ ví trí ngón trỏ' di chuyển làm gươm sắc bén."
    ),
    GOLDEN_CATCH(
        "golden_catch",
        "Hứng Trái Cây (Golden Catch)",
        "🧺",
        "Mở bàn tay (Palm) để hứng hoa quả vàng, nắm bàn tay (Fist) lại để tránh hứng vỏ chuối hoặc bom!",
        GestureType.PALM,
        "Mở rộng lòng bàn tay hứng quả, nắm đấm để dọn rác."
    ),
    FLAPPY_HAND(
        "flappy_hand",
        "Cánh Chim Cử Chỉ (Flappy Wave)",
        "🐦",
        "Liên tục xòe bàn tay và nắm đấm (Palm <-> Fist) hoặc dùng cử chỉ Pinch để bổ cánh lái chim qua cống xanh.",
        GestureType.PINCH,
        "Thay đổi liên tục giữa Kẹp/Xòe để điều chỉnh độ bay cao."
    ),
    BALANCE_BALL(
        "balance_ball",
        "Cân Bằng Bóng (Balance Ball)",
        "🥎",
        "Giữ lòng bàn tay phẳng. Nghiêng tay trái/phải (sử dụng Thumbs Up / Thumbs Down hoặc gạt trái phải) để cân bằng quả bóng trên thanh gỗ.",
        GestureType.PALM,
        "Dùng lòng bàn tay thăng bằng, bẻ lái khéo léo."
    ),
    RHYTHM_PIANO(
        "rhythm_piano",
        "Nhạc Trưởng Piano (Rhythm Conductor)",
        "🎹",
        "Đánh đúng nhịp các phím nhạc rơi bằng cách thay đổi cử chỉ sang Peace, Spock hoặc Index Point khi đi đúng ô.",
        GestureType.PEACE,
        "Thay đổi cử chỉ sang phím nhạc tương ứng khi nốt nhạc chạm vạch."
    ),
    BUBBLE_POPPER(
        "bubble_popper",
        "Bắn Bóng Bong Bóng (Bubble Popper)",
        "🫧",
        "Chạm trực tiếp ngón trỏ vào các bong bóng khí đa sắc màu bay lên để kích hoạt bùng nổ điểm số.",
        GestureType.INDEX_POINT,
        "Chỉ thẳng ngón trỏ di chuyển để chọc nổ bong bóng."
    ),
    VIRTUAL_BOXER(
        "virtual_boxer",
        "Quyền Anh Thực Tế Ảo (Virtual Boxer)",
        "🥊",
        "Khi bao cát hoặc đối thủ xuất hiện, thực hiện đấm thật nhanh bằng cách khép chặt Nắm đấm (Fist)!",
        GestureType.FIST,
        "Hình thành 'Nắm đấm' đấm sập bao cát huấn luyện."
    ),
    ROCK_PAPER_SCISSORS(
        "rps",
        "Oẳn Tù Tì AI (Rock Paper Scissors)",
        "✂️",
        "Chọn Fist (Búa), Palm (Bao) hoặc Peace (Kéo) để đọ sức trực tiếp với siêu trí tuệ nhân tạo AI.",
        GestureType.PALM,
        "Nắm đấm = Búa, Bàn tay mở = Bao, Tay chữ V = Kéo."
    ),
    SPACE_SHOOTER(
        "space_shooter",
        "Bắn Ruồi Thiên Hà (Gesture Galaxy)",
        "🚀",
        "Di chuyển tàu chiến bằng chỉ tay trái/phải, nén ngón tay (Pinch) để bắn loạt đạn la-ze tiêu diệt tàu địch.",
        GestureType.PINCH,
        "Di chuyển tay để lái tàu, Kẹp Pinch ngón tay để bắn laser."
    ),
    WHACK_A_MOLE(
        "whack_a_mole",
        "Đập Chuột Chũi (Gesture Whack)",
        "🔨",
        "Dùng ngón trỏ làm búa gỗ đập bôm bốp lên đầu các con chuột chũi tinh nghịch đang chui khỏi hang.",
        GestureType.INDEX_POINT,
        "Di chuyển đầu ngón trỏ đè lên chuột chũi trong hang."
    ),
    WAVE_RUNNER(
        "wave_runner",
        "Lướt Sóng Cử Chỉ (Wave Runner)",
        "🏄",
        "Sử dụng Thumbs Up để nhảy lên, Thumbs Down để trượt xuống thấp né chướng ngại vật dồn dập.",
        GestureType.THUMBS_UP,
        "Thay đổi thích (Thumbs Up) / không thích (Thumbs Down) để nhảy/cúi."
    )
}

enum class LifeAppType(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val primaryGesture: GestureType,
    val applicationField: String
) {
    APP_LOCK(
        "app_lock",
        "Khóa Sinh Trắc Học Cử Chỉ",
        "🔐",
        "Đặt chuỗi khóa mật khẩu bảo mật riêng tư bằng cử chỉ. Hãy lặp lại đúng mật khẩu tay để mở khóa ứng dụng bí mật.",
        GestureType.OK_SIGN,
        "Bảo mật thông minh"
    ),
    SPORTS_TRACKER(
        "sports_tracker",
        "Gym & Tracker Body-Weight",
        "🏋️",
        "Bộ đếm nhịp hít đất, bicep curl tự động bằng mắt thần cử chỉ. Co rửa tay liên tục (Fist <-> Palm) để đếm số hiệp tập.",
        GestureType.FIST,
        "Y tế & Sức khỏe"
    ),
    SLIDE_PRESENTER(
        "slide_presenter",
        "Thuyết Trình Không Chạm (Smart Slides)",
        "📊",
        "Vuốt tay ngang sang trái/phải để chuyển slide PowerPoint. Dùng ngón trỏ làm tia laser đỏ rực chiếu thẳng lên bảng biểu.",
        GestureType.INDEX_POINT,
        "Công tác văn phòng"
    ),
    SMART_HOME(
        "smart_home",
        "Bảng Điều Khiển Nhà Thông Minh",
        "🏠",
        "Nhà thông minh không chạm. Gán OK để bật TV, Spock để mở điều hòa, Rock On bật nhạc, Thumbs Up tắt đèn ngủ.",
        GestureType.SPOCK,
        "Tiện ích nhà ở"
    ),
    SPATIAL_PAINT(
        "spatial_paint",
        "Vẽ Neon Trong Không Trung (Air Paint)",
        "🎨",
        "Sử dụng ngón trỏ dẫn đường để vẽ các bức tranh 3D đầy màu sắc neon rực rỡ, OK để xóa nét vẽ.",
        GestureType.INDEX_POINT,
        "Nghệ thuật & Sáng tạo"
    ),
    CLAY_SCULPT(
        "clay_sculpt",
        "Nặn Đất Sét Tương Tác (3D Clay)",
        "🏺",
        "Kẹp ngón tay (Pinch) co kéo đẩy để biến khối đất sét thô thành bình gốm, bánh donut nghệ thuật tinh tế.",
        GestureType.PINCH,
        "Thiết kế 3D"
    ),
    MUSIC_CONDUCTOR(
        "music_conductor",
        "Nhạc Trưởng Thính Phòng (Conductor)",
        "🎼",
        "Làm chỉ huy dàn nhạc. Điều tiết tốc độ tempo bằng nhịp tay nhanh chậm, Thumbs Up/Down điều khiển dải âm lượng.",
        GestureType.PALM,
        "Âm nhạc giải trí"
    ),
    G_CALCULATOR(
        "gesture_calc",
        "Máy Tính Cử Chỉ Ngón Tay (Smart Calc)",
        "🧮",
        "Xòe số ngón tay để nạp số tương ứng (1-5), dùng cử chỉ OK để tính cộng, Peace để trừ, Fist cứu bằng dấu bằng.",
        GestureType.OK_SIGN,
        "Giáo dục & Học thuật"
    ),
    SOS_EMERGENCY(
        "sos_emergency",
        "Kích Hoạt Bảo Vệ SOS Khẩn Cấp",
        "🚨",
        "Cơ chế an sinh xã hội. Giữ chặt nắm cốt Fist vững chắc 3 giây để kích hoạt âm còi báo động khẩn và gửi tin cứu nạn.",
        GestureType.FIST,
        "Bảo hộ & An toàn"
    ),
    CHEF_MASTER(
        "chef_master",
        "Đầu Bếp Siêu Cấp (Pan & Chop)",
        "🍳",
        "Nấu ăn không chạm! Thực hiện hất cổ tay (gạt nhanh) để chiên lật bánh xèo, dập phẳng Palm để thái củ cải.",
        GestureType.PALM,
        "Đời sống nấu nướng"
    ),
    CARPAL_STRETCH(
        "carpal_stretch",
        "Tập Khớp Ngừa Đau Cổ Tay",
        "🧘",
        "Chương trình thư giãn cổ tay cho lập trình viên. Hướng dẫn co duỗi năm ngón tay nhịp nhàng giảm hội chứng ống cổ tay.",
        GestureType.PALM,
        "Sức khỏe văn phòng"
    )
}
