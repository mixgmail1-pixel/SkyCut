package com.example.skycut.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycut.model.*
import com.example.ui.theme.AccentPurpleFX
import com.example.ui.theme.AccentRedCut
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary

@Composable
fun MultiTrackTimeline(
    project: SkyCutProject,
    currentTimelineMs: Long,
    selectedClipId: String?,
    selectedAudioId: String?,
    selectedTextId: String?,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectText: (String) -> Unit,
    onAddClip: () -> Unit,
    onOpenTransitionDialog: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val totalMs = project.totalDurationMs.coerceAtLeast(4000L)

    // Pixels per millisecond scale (60dp per second)
    val dpPerSecond = 70.dp
    val pxPerMs = with(density) { (dpPerSecond.toPx() / 1000f) }
    val totalTimelineWidthDp = with(density) { ((totalMs * pxPerMs) + 600f).toDp() }

    val horizontalScrollState = rememberScrollState()

    // Auto-scroll timeline to follow playhead when near bounds
    LaunchedEffect(currentTimelineMs) {
        val playheadPx = currentTimelineMs * pxPerMs
        val viewportWidth = with(density) { 360.dp.toPx() }
        val centerTarget = (playheadPx - viewportWidth / 2).toInt().coerceAtLeast(0)
        if (kotlin.math.abs(horizontalScrollState.value - centerTarget) > 300) {
            horizontalScrollState.animateScrollTo(centerTarget)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1524))
    ) {
        // Scrollable Multi-Track Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val clickedMs = (offset.x / pxPerMs).toLong().coerceIn(0L, totalMs)
                        onSeek(clickedMs)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newPlayheadPx = (currentTimelineMs * pxPerMs) + dragAmount.x
                        val newMs = (newPlayheadPx / pxPerMs).toLong().coerceIn(0L, totalMs)
                        onSeek(newMs)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .width(totalTimelineWidthDp)
                    .padding(vertical = 4.dp)
            ) {
                // 1. Time Ruler
                TimeRulerView(
                    totalDurationMs = totalMs,
                    pxPerMs = pxPerMs,
                    totalWidthDp = totalTimelineWidthDp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Main Video Track
                MainVideoTrackView(
                    clips = project.mainVideoClips,
                    selectedClipId = selectedClipId,
                    pxPerMs = pxPerMs,
                    onSelectClip = onSelectClip,
                    onAddClip = onAddClip,
                    onOpenTransition = onOpenTransitionDialog
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 3. PIP / Overlay Track (if any overlays present or subtle placeholder)
                if (project.overlayClips.isNotEmpty()) {
                    OverlayTrackView(
                        overlays = project.overlayClips,
                        selectedClipId = selectedClipId,
                        pxPerMs = pxPerMs,
                        onSelectOverlay = onSelectClip
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 4. Text & Subtitle Track
                if (project.textOverlays.isNotEmpty()) {
                    TextTrackView(
                        texts = project.textOverlays,
                        selectedTextId = selectedTextId,
                        pxPerMs = pxPerMs,
                        onSelectText = onSelectText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 5. Audio & Voiceover Track
                if (project.audioClips.isNotEmpty()) {
                    AudioTrackView(
                        audios = project.audioClips,
                        selectedAudioId = selectedAudioId,
                        pxPerMs = pxPerMs,
                        onSelectAudio = onSelectAudio
                    )
                }

                // Bottom padding
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Scrubbing Playhead Line across all tracks
            val playheadOffsetDp = with(density) { (currentTimelineMs * pxPerMs).toDp() }
            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp - 1.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(SkyBluePrimary)
            )

            // Playhead Cursor Pin at top of ruler
            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp - 7.dp, y = 0.dp)
                    .size(14.dp)
                    .background(SkyBluePrimary, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun TimeRulerView(
    totalDurationMs: Long,
    pxPerMs: Float,
    totalWidthDp: androidx.compose.ui.unit.Dp
) {
    val density = LocalDensity.current
    Canvas(
        modifier = Modifier
            .width(totalWidthDp)
            .height(24.dp)
    ) {
        val totalSec = (totalDurationMs / 1000).toInt() + 4
        for (sec in 0..totalSec) {
            val secMs = sec * 1000L
            val x = secMs * pxPerMs

            // Major tick
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(x, 10f),
                end = Offset(x, size.height),
                strokeWidth = 2f
            )

            // Minor half-second tick
            val halfX = (secMs + 500L) * pxPerMs
            drawLine(
                color = Color(0xFF475569),
                start = Offset(halfX, 16f),
                end = Offset(halfX, size.height),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun MainVideoTrackView(
    clips: List<VideoClip>,
    selectedClipId: String?,
    pxPerMs: Float,
    onSelectClip: (String) -> Unit,
    onAddClip: () -> Unit,
    onOpenTransition: (String) -> Unit
) {
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF131D2E), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Header Badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text("V1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Clip strips
        clips.forEachIndexed { index, clip ->
            val clipWidthDp = with(density) { (clip.effectiveDurationMs * pxPerMs).toDp().coerceAtLeast(48.dp) }
            val isSelected = clip.id == selectedClipId

            // Transition node button before clip (if not first clip)
            if (index > 0) {
                Surface(
                    onClick = { onOpenTransition(clip.id) },
                    shape = RoundedCornerShape(4.dp),
                    color = if (clip.transitionIn != TransitionType.NONE) SkyBluePrimary else Color(0xFF334155),
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("transition_button_${clip.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (clip.transitionIn != TransitionType.NONE) "⧗" else "|",
                            color = if (clip.transitionIn != TransitionType.NONE) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
            }

            // Clip Item Card
            Surface(
                onClick = { onSelectClip(clip.id) },
                shape = RoundedCornerShape(6.dp),
                color = Color(clip.backgroundColorHex),
                border = if (isSelected) BorderStroke(2.dp, SkyBluePrimary) else BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .width(clipWidthDp)
                    .fillMaxHeight()
                    .testTag("clip_card_${clip.id}")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Clip thumbnail / stripes
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = clip.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            if (clip.speed != 1.0f) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = "${clip.speed}x",
                                        color = SkyBluePrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${clip.effectiveDurationMs / 1000f}s",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (clip.filter != VideoFilter.ORIGINAL) {
                                Text(
                                    text = "FX",
                                    color = AccentPurpleFX,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Trim drag handle hints when selected
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(SkyBluePrimary, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(SkyBluePrimary, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(3.dp))
        }

        // Add Clip Plus Button
        Surface(
            onClick = onAddClip,
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .size(48.dp)
                .testTag("add_clip_timeline_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Clip",
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun OverlayTrackView(
    overlays: List<VideoClip>,
    selectedClipId: String?,
    pxPerMs: Float,
    onSelectOverlay: (String) -> Unit
) {
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF131D2E), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PictureInPicture,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text("PIP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        overlays.forEach { overlay ->
            val w = with(density) { (overlay.effectiveDurationMs * pxPerMs).toDp().coerceAtLeast(50.dp) }
            val isSelected = overlay.id == selectedClipId
            Surface(
                onClick = { onSelectOverlay(overlay.id) },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E3A8A),
                border = if (isSelected) BorderStroke(2.dp, SkyBluePrimary) else null,
                modifier = Modifier
                    .width(w)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "PIP: ${overlay.name}",
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(3.dp))
        }
    }
}

@Composable
private fun TextTrackView(
    texts: List<TextOverlay>,
    selectedTextId: String?,
    pxPerMs: Float,
    onSelectText: (String) -> Unit
) {
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Color(0xFF131D2E), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text("TEXT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        texts.forEach { item ->
            val offsetDp = with(density) { (item.startTimelineMs * pxPerMs).toDp() }
            val w = with(density) { (item.durationMs * pxPerMs).toDp().coerceAtLeast(44.dp) }
            val isSelected = item.id == selectedTextId

            Surface(
                onClick = { onSelectText(item.id) },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF3B0764),
                border = if (isSelected) BorderStroke(2.dp, SkyBluePrimary) else null,
                modifier = Modifier
                    .width(w)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = item.text,
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(3.dp))
        }
    }
}

@Composable
private fun AudioTrackView(
    audios: List<AudioClip>,
    selectedAudioId: String?,
    pxPerMs: Float,
    onSelectAudio: (String) -> Unit
) {
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF131D2E), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text("AUDIO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        audios.forEach { audio ->
            val w = with(density) { (audio.effectiveDurationMs * pxPerMs).toDp().coerceAtLeast(60.dp) }
            val isSelected = audio.id == selectedAudioId

            Surface(
                onClick = { onSelectAudio(audio.id) },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF064E3B),
                border = if (isSelected) BorderStroke(2.dp, SkyBluePrimary) else null,
                modifier = Modifier
                    .width(w)
                    .fillMaxHeight()
                    .testTag("audio_card_${audio.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎵 ${audio.title}",
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "${audio.effectiveDurationMs / 1000f}s",
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.width(3.dp))
        }
    }
}
