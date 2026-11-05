package com.example.skycut.engine

import com.example.R
import com.example.skycut.model.*

object SampleMediaProvider {

    data class SampleMediaItem(
        val title: String,
        val durationMs: Long,
        val drawableRes: Int,
        val tag: String,
        val filter: VideoFilter = VideoFilter.ORIGINAL
    )

    data class SampleAudioItem(
        val title: String,
        val durationMs: Long,
        val genre: String,
        val uri: String
    )

    data class SoundEffectItem(
        val name: String,
        val icon: String,
        val category: String
    )

    val sampleVideos = listOf(
        SampleMediaItem("Pacific Ocean Waves", 5000L, R.drawable.template_travel, "Ocean & Nature", VideoFilter.SKY_VIVID),
        SampleMediaItem("Neo Tokyo Cyber Street", 5000L, R.drawable.template_urban, "Night & City", VideoFilter.CYBERPUNK),
        SampleMediaItem("Golden Sunset Drive", 4500L, R.drawable.template_travel, "Sunset & Vlog", VideoFilter.WARM_GOLD),
        SampleMediaItem("Cinematic Studio Production", 6000L, R.drawable.skycut_hero, "Studio & Tech", VideoFilter.CINEMATIC)
    )

    val sampleMusic = listOf(
        SampleAudioItem("Sky High Chill Vibes", 15000L, "Lo-Fi Beats", "asset://music/chill.mp3"),
        SampleAudioItem("Cyberpunk Horizon", 12000L, "Synthwave", "asset://music/synth.mp3"),
        SampleAudioItem("Summer Travel Vlog", 18000L, "Acoustic Pop", "asset://music/vlog.mp3"),
        SampleAudioItem("Cinematic Action Rise", 10000L, "Orchestral", "asset://music/action.mp3")
    )

    val soundEffects = listOf(
        SoundEffectItem("Whoosh", "💨", "Transitions"),
        SoundEffectItem("Cinematic Boom", "💥", "Impacts"),
        SoundEffectItem("Camera Click", "📸", "Tech"),
        SoundEffectItem("Pop", "✨", "UI"),
        SoundEffectItem("Bell", "🔔", "Notif")
    )

    val stickers = listOf(
        "🔥" to "Fire",
        "✨" to "Sparkles",
        "⚡" to "Lightning",
        "🎬" to "Clapper",
        "💯" to "100%",
        "❤️" to "Heart",
        "🚀" to "Rocket",
        "🌴" to "Tropical",
        "🕶️" to "Cool",
        "🎧" to "Music",
        "📸" to "Photo",
        "⭐" to "Star"
    )

    val autoCaptionStyles = listOf(
        "KARAOKE_BOUNCE" to "Karaoke Bounce (Sky Blue)",
        "SUBTITLE_BOX" to "Classic Dark Subtitle Box",
        "BOLD_OUTLINE" to "Bold Neon Outline",
        "MINIMAL_CLEAN" to "Minimal Modern Sans"
    )
}
