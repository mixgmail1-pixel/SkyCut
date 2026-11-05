package com.example.skycut.model

import java.util.UUID

enum class AspectRatio(val label: String, val ratio: Float, val widthDp: Int, val heightDp: Int) {
    RATIO_16_9("16:9", 16f / 9f, 320, 180),
    RATIO_9_16("9:16 (Shorts/TikTok)", 9f / 16f, 180, 320),
    RATIO_1_1("1:1 (Square)", 1f, 240, 240),
    RATIO_4_5("4:5 (Instagram)", 4f / 5f, 200, 250),
    RATIO_21_9("21:9 (Cinema)", 21f / 9f, 336, 144)
}

enum class TransitionType(val displayName: String) {
    NONE("None"),
    CROSS_DISSOLVE("Dissolve"),
    FADE_TO_BLACK("Fade Black"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    ZOOM_IN("Zoom In"),
    WHITE_FLASH("Flash"),
    WIPE("Wipe")
}

enum class VideoFilter(val displayName: String, val brightness: Float, val contrast: Float, val saturation: Float, val warmTemp: Float) {
    ORIGINAL("Original", 0f, 1f, 1f, 0f),
    SKY_VIVID("Sky Vivid", 0.05f, 1.25f, 1.4f, -0.05f),
    CINEMATIC("Cinematic", -0.05f, 1.3f, 0.85f, 0.1f),
    TEAL_ORANGE("Teal & Orange", 0f, 1.35f, 1.2f, 0.15f),
    VINTAGE_90S("Vintage 90s", 0.08f, 0.95f, 0.75f, 0.25f),
    NOIR_BW("B&W Noir", 0f, 1.4f, 0f, 0f),
    CYBERPUNK("Cyberpunk", 0.05f, 1.4f, 1.5f, -0.2f),
    WARM_GOLD("Warm Sunset", 0.05f, 1.15f, 1.2f, 0.35f),
    DREAMY("Dreamy Soft", 0.12f, 0.9f, 0.9f, 0.08f)
}

enum class VideoEffect(val displayName: String) {
    NONE("None"),
    GLITCH("Glitch"),
    RGB_SPLIT("RGB Split"),
    CAMERA_SHAKE("Camera Shake"),
    SPARKLES("Sparkles"),
    DREAM_GLOW("Dream Glow"),
    ZOOM_BLUR("Zoom Blur"),
    FILM_GRAIN("Film Grain")
}

data class ColorAdjustments(
    val brightness: Float = 0f,    // -1f to 1f
    val contrast: Float = 1f,      // 0.5f to 2f
    val saturation: Float = 1f,    // 0f to 2f
    val temperature: Float = 0f,   // -1f (cool blue) to 1f (warm orange)
    val vignette: Float = 0f,      // 0f to 1f
    val exposure: Float = 0f       // -1f to 1f
)

data class Keyframe(
    val timeMs: Long,
    val scale: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
)

data class VideoClip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: String, // local file path, content uri, or asset name
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val rotationDegrees: Int = 0,
    val isFlippedHorizontally: Boolean = false,
    val filter: VideoFilter = VideoFilter.ORIGINAL,
    val adjustments: ColorAdjustments = ColorAdjustments(),
    val effect: VideoEffect = VideoEffect.NONE,
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 600L,
    val isMuted: Boolean = false,
    val isFrozen: Boolean = false,
    val chromaKeyEnabled: Boolean = false,
    val chromaKeyColor: Long = 0xFF00FF00, // Green
    val chromaKeyThreshold: Float = 0.35f,
    val keyframes: List<Keyframe> = emptyList(),
    val isSampleMedia: Boolean = false,
    val thumbnailRes: Int? = null,
    val backgroundColorHex: Long = 0xFF1E293B
) {
    val effectiveDurationMs: Long
        get() {
            val trimmed = (trimEndMs - trimStartMs).coerceAtLeast(100L)
            return (trimmed / speed).toLong()
        }
}

data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uri: String,
    val startTimelineMs: Long,
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val isVoiceover: Boolean = false,
    val isSoundEffect: Boolean = false
) {
    val effectiveDurationMs: Long
        get() = (trimEndMs - trimStartMs).coerceAtLeast(100L)
}

enum class TextStylePreset(val displayName: String, val fontStyle: String, val hasBackground: Boolean, val hasOutline: Boolean) {
    MODERN("Sky Modern", "SansSerif", false, false),
    BOLD_OUTLINE("Bold Outline", "SansSerifBlack", false, true),
    SUBTITLE_BOX("Subtitle Box", "SansSerif", true, false),
    NEON_GLOW("Neon Glow", "Monospace", false, true),
    CINEMATIC_SERIF("Cinematic", "Serif", false, false),
    TYPEWRITER("Typewriter", "Monospace", true, false)
}

enum class TextAnimation(val displayName: String) {
    NONE("None"),
    FADE_IN("Fade In"),
    POP_IN("Pop In"),
    SLIDE_UP("Slide Up"),
    TYPEWRITER("Typewriter"),
    BOUNCE("Bounce")
}

data class TextOverlay(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startTimelineMs: Long,
    val durationMs: Long = 3000L,
    val stylePreset: TextStylePreset = TextStylePreset.MODERN,
    val animation: TextAnimation = TextAnimation.POP_IN,
    val fontSizeSp: Float = 22f,
    val colorHex: Long = 0xFFFFFFFF,
    val positionX: Float = 0.5f, // Normalized 0..1
    val positionY: Float = 0.8f,
    val isCaption: Boolean = false
)

data class StickerOverlay(
    val id: String = UUID.randomUUID().toString(),
    val emojiOrIcon: String,
    val label: String,
    val startTimelineMs: Long,
    val durationMs: Long = 2500L,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

data class SkyCutProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Project",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val mainVideoClips: List<VideoClip> = emptyList(),
    val overlayClips: List<VideoClip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickers: List<StickerOverlay> = emptyList(),
    val includeWatermark: Boolean = false
) {
    val totalDurationMs: Long
        get() {
            var maxMs = 0L
            var currentMainMs = 0L
            for (clip in mainVideoClips) {
                currentMainMs += clip.effectiveDurationMs
            }
            if (currentMainMs > maxMs) maxMs = currentMainMs

            for (clip in overlayClips) {
                val end = clip.effectiveDurationMs
                if (end > maxMs) maxMs = end
            }
            for (audio in audioClips) {
                val end = audio.startTimelineMs + audio.effectiveDurationMs
                if (end > maxMs) maxMs = end
            }
            for (text in textOverlays) {
                val end = text.startTimelineMs + text.durationMs
                if (end > maxMs) maxMs = end
            }
            return maxMs.coerceAtLeast(3000L) // Minimum 3s project duration
        }
}

data class ExportConfig(
    val resolutionLabel: String = "1080p (FHD)",
    val width: Int = 1080,
    val height: Int = 1920,
    val fps: Int = 30,
    val bitrateMbps: Int = 12,
    val includeWatermark: Boolean = false
)
