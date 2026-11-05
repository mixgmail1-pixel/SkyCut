package com.example.skycut.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycut.model.*
import com.example.ui.theme.SkyBluePrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VideoPreviewCanvas(
    project: SkyCutProject,
    currentTimelineMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSelectAspectRatio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalMs = project.totalDurationMs

    // Bitmaps cache for sample previews
    val bitmapCache = remember { mutableMapOf<Int, ImageBitmap>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF090E17)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Info Bar: Aspect Ratio Pill + Timecode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onSelectAspectRatio,
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.testTag("aspect_ratio_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Aspect Ratio",
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = project.aspectRatio.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Timecode display
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B)
            ) {
                Text(
                    text = "${formatTimecode(currentTimelineMs)} / ${formatTimecode(totalMs)}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Viewport Container with selected Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(min = 220.dp, max = 320.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val aspect = project.aspectRatio.ratio
            Box(
                modifier = Modifier
                    .aspectRatio(aspect, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .testTag("video_preview_box")
            ) {
                // Real-time Canvas Rendering
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Find active main clip
                    var elapsed = 0L
                    var activeClip: VideoClip? = null
                    var localTime = 0L
                    var transitionFraction = 0f
                    var activeTransition = TransitionType.NONE

                    for (clip in project.mainVideoClips) {
                        val dur = clip.effectiveDurationMs
                        if (currentTimelineMs >= elapsed && currentTimelineMs < elapsed + dur) {
                            activeClip = clip
                            localTime = ((currentTimelineMs - elapsed) * clip.speed).toLong()
                            if (clip.transitionIn != TransitionType.NONE && (currentTimelineMs - elapsed) < clip.transitionDurationMs) {
                                activeTransition = clip.transitionIn
                                transitionFraction = (currentTimelineMs - elapsed).toFloat() / clip.transitionDurationMs.toFloat()
                            }
                            break
                        }
                        elapsed += dur
                    }

                    if (activeClip != null) {
                        drawPreviewClip(
                            clip = activeClip,
                            localTimeMs = localTime,
                            w = w,
                            h = h,
                            transition = activeTransition,
                            transitionFraction = transitionFraction,
                            context = context,
                            bitmapCache = bitmapCache
                        )
                    } else {
                        // Background placeholder
                        drawRect(Color(0xFF0F172A), size = size)
                    }

                    // 2. Overlays / PIP
                    for (overlay in project.overlayClips) {
                        if (currentTimelineMs in 0L..overlay.effectiveDurationMs) {
                            drawIntoCanvas { canvas ->
                                canvas.save()
                                canvas.translate(w * 0.5f, h * 0.5f)
                                drawPreviewClip(
                                    clip = overlay,
                                    localTimeMs = currentTimelineMs,
                                    w = w * 0.45f,
                                    h = h * 0.45f,
                                    transition = TransitionType.NONE,
                                    transitionFraction = 1f,
                                    context = context,
                                    bitmapCache = bitmapCache
                                )
                                canvas.restore()
                            }
                        }
                    }

                    // 3. Text Overlays & Subtitles
                    for (text in project.textOverlays) {
                        val start = text.startTimelineMs
                        val end = start + text.durationMs
                        if (currentTimelineMs in start..end) {
                            val tx = text.positionX * w
                            val ty = text.positionY * h
                            val isBox = text.stylePreset == TextStylePreset.SUBTITLE_BOX || text.isCaption

                            // Text background box
                            if (isBox) {
                                drawRoundRect(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    topLeft = Offset(w * 0.1f, ty - 26f),
                                    size = Size(w * 0.8f, 52f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                                )
                            }

                            // Outline if style requires
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                    this.color = text.colorHex.toInt()
                                    this.textSize = text.fontSizeSp * (w / 360f)
                                    this.textAlign = android.graphics.Paint.Align.CENTER
                                    this.typeface = when (text.stylePreset) {
                                        TextStylePreset.BOLD_OUTLINE -> android.graphics.Typeface.DEFAULT_BOLD
                                        TextStylePreset.CINEMATIC_SERIF -> android.graphics.Typeface.SERIF
                                        TextStylePreset.NEON_GLOW, TextStylePreset.TYPEWRITER -> android.graphics.Typeface.MONOSPACE
                                        else -> android.graphics.Typeface.SANS_SERIF
                                    }
                                }

                                if (text.stylePreset.hasOutline) {
                                    val stroke = android.graphics.Paint(paint).apply {
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 6f
                                        this.color = android.graphics.Color.BLACK
                                    }
                                    canvas.nativeCanvas.drawText(text.text, tx, ty, stroke)
                                }
                                canvas.nativeCanvas.drawText(text.text, tx, ty, paint)
                            }
                        }
                    }

                    // 4. Stickers
                    for (sticker in project.stickers) {
                        if (currentTimelineMs in sticker.startTimelineMs..(sticker.startTimelineMs + sticker.durationMs)) {
                            drawIntoCanvas { canvas ->
                                val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                    textSize = 42f * sticker.scale * (w / 360f)
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                canvas.nativeCanvas.drawText(sticker.emojiOrIcon, sticker.positionX * w, sticker.positionY * h, p)
                            }
                        }
                    }

                    // 5. Watermark badge
                    if (project.includeWatermark) {
                        drawRoundRect(
                            color = Color(0xFF0F172A).copy(alpha = 0.65f),
                            topLeft = Offset(w - 120f, 16f),
                            size = Size(104f, 32f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                        )
                        drawIntoCanvas { canvas ->
                            val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                color = android.graphics.Color.WHITE
                                textSize = 18f
                                textAlign = android.graphics.Paint.Align.CENTER
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                            canvas.nativeCanvas.drawText("⚡ SkyCut", w - 68f, 38f, p)
                        }
                    }
                }

                // Center Play/Pause Button Overlay (subtle tap to play)
                if (!isPlaying) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(52.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.5.dp, SkyBluePrimary, CircleShape)
                            .testTag("preview_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Viewport Control Strip: Jump Start, Frame Step Back, Play/Pause, Frame Step Forward, Jump End
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSeek(0L) },
                modifier = Modifier.size(36.dp).testTag("seek_start_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Start",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { onSeek((currentTimelineMs - 100L).coerceAtLeast(0L)) },
                modifier = Modifier.size(36.dp).testTag("step_backward_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Replay5,
                    contentDescription = "Step Back",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Play/Pause Pill
            Surface(
                onClick = onTogglePlay,
                shape = CircleShape,
                color = SkyBluePrimary,
                modifier = Modifier.size(42.dp).testTag("main_play_pause_pill")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF090E17),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { onSeek((currentTimelineMs + 100L).coerceAtMost(totalMs)) },
                modifier = Modifier.size(36.dp).testTag("step_forward_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Forward5,
                    contentDescription = "Step Forward",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { onSeek(totalMs) },
                modifier = Modifier.size(36.dp).testTag("seek_end_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "End",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawPreviewClip(
    clip: VideoClip,
    localTimeMs: Long,
    w: Float,
    h: Float,
    transition: TransitionType,
    transitionFraction: Float,
    context: android.content.Context,
    bitmapCache: MutableMap<Int, ImageBitmap>
) {
    drawIntoCanvas { canvas ->
        canvas.save()

        // Transitions
        when (transition) {
            TransitionType.SLIDE_LEFT -> {
                canvas.translate((1f - transitionFraction) * w, 0f)
            }
            TransitionType.SLIDE_RIGHT -> {
                canvas.translate(-(1f - transitionFraction) * w, 0f)
            }
            TransitionType.ZOOM_IN -> {
                val scale = 0.5f + (transitionFraction * 0.5f)
                canvas.translate(w / 2f, h / 2f)
                canvas.scale(scale, scale)
                canvas.translate(-w / 2f, -h / 2f)
            }
            else -> {}
        }

        // Clip rotation & flip
        if (clip.rotationDegrees != 0) {
            canvas.translate(w / 2f, h / 2f)
            canvas.rotate(clip.rotationDegrees.toFloat())
            canvas.translate(-w / 2f, -h / 2f)
        }
        if (clip.isFlippedHorizontally) {
            canvas.translate(w / 2f, h / 2f)
            canvas.scale(-1f, 1f)
            canvas.translate(-w / 2f, -h / 2f)
        }

        // Draw image bitmap or background graphic
        if (clip.thumbnailRes != null) {
            var img = bitmapCache[clip.thumbnailRes]
            if (img == null) {
                val bmp = BitmapFactory.decodeResource(context.resources, clip.thumbnailRes)
                if (bmp != null) {
                    img = bmp.asImageBitmap()
                    bitmapCache[clip.thumbnailRes] = img
                }
            }
            if (img != null) {
                // Apply color tint / filter simulation
                val colorFilter = when (clip.filter) {
                    VideoFilter.SKY_VIVID -> ColorFilter.tint(Color(0xFF38BDF8), BlendMode.Overlay)
                    VideoFilter.CINEMATIC -> ColorFilter.tint(Color(0xFF1E3A8A), BlendMode.ColorBurn)
                    VideoFilter.TEAL_ORANGE -> ColorFilter.tint(Color(0xFFF97316), BlendMode.Hue)
                    VideoFilter.WARM_GOLD -> ColorFilter.tint(Color(0xFFF59E0B), BlendMode.Softlight)
                    VideoFilter.CYBERPUNK -> ColorFilter.tint(Color(0xFFA855F7), BlendMode.Overlay)
                    VideoFilter.NOIR_BW -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    else -> null
                }

                drawImage(
                    image = img,
                    dstSize = androidx.compose.ui.unit.IntSize(w.toInt(), h.toInt()),
                    colorFilter = colorFilter
                )
            }
        } else {
            // Draw gradient clip placeholder
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(clip.backgroundColorHex), Color(0xFF0F172A)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                size = Size(w, h)
            )

            // Dynamic wave preview
            val path = Path()
            val waveY = h * 0.5f
            for (x in 0..w.toInt() step 20) {
                val angle = (x + localTimeMs * 0.4) * 0.03
                val y = waveY + sin(angle).toFloat() * (h * 0.15f)
                if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
            }
            drawPath(path, SkyBluePrimary, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        }

        // Glitch or Shake effects
        when (clip.effect) {
            VideoEffect.GLITCH -> {
                if ((localTimeMs / 100) % 4 == 0L) {
                    val sliceY = ((localTimeMs * 3) % h.toInt()).toFloat()
                    drawRect(
                        color = SkyBluePrimary.copy(alpha = 0.35f),
                        topLeft = Offset(0f, sliceY),
                        size = Size(w, 24f)
                    )
                }
            }
            VideoEffect.SPARKLES -> {
                for (i in 0..4) {
                    val px = ((localTimeMs * 37 + i * 160) % w.toInt()).toFloat()
                    val py = ((localTimeMs * 29 + i * 200) % h.toInt()).toFloat()
                    drawCircle(Color.White, radius = 5f, center = Offset(px, py))
                }
            }
            else -> {}
        }

        // Fade to black / flash transition overlays
        when (transition) {
            TransitionType.FADE_TO_BLACK -> {
                drawRect(Color.Black.copy(alpha = (1f - transitionFraction).coerceIn(0f, 1f)), size = Size(w, h))
            }
            TransitionType.WHITE_FLASH -> {
                drawRect(Color.White.copy(alpha = (1f - transitionFraction).coerceIn(0f, 1f)), size = Size(w, h))
            }
            else -> {}
        }

        canvas.restore()
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val centis = (ms % 1000) / 10
    return String.format("%02d:%02d.%02d", min, sec, centis)
}
