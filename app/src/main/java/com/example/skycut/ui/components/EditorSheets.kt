package com.example.skycut.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycut.engine.SampleMediaProvider
import com.example.skycut.model.*
import com.example.ui.theme.AccentRedCut
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedBottomSheet(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Video Speed Control",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${currentSpeed}x Speed",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Speed Presets
            val speeds = listOf(0.2f, 0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 5.0f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                speeds.forEach { speed ->
                    val isSelected = kotlin.math.abs(currentSpeed - speed) < 0.05f
                    Surface(
                        onClick = { onSpeedSelected(speed) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SkyBluePrimary else Color(0xFF1E293B),
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.testTag("speed_${speed}x")
                    ) {
                        Text(
                            text = "${speed}x",
                            color = if (isSelected) Color(0xFF090E17) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth().testTag("speed_done_button")
            ) {
                Text("Apply Speed", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeBottomSheet(
    currentVolume: Float,
    isMuted: Boolean,
    onVolumeChanged: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onExtractAudio: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clip Volume & Audio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                TextButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isMuted) AccentRedCut else SkyBluePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isMuted) "Unmute" else "Mute", color = if (isMuted) AccentRedCut else SkyBluePrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Volume: ${(currentVolume * 100).toInt()}%",
                fontSize = 14.sp,
                color = Color(0xFFCBD5E1)
            )

            Slider(
                value = currentVolume,
                onValueChange = onVolumeChanged,
                valueRange = 0f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = SkyBluePrimary,
                    activeTrackColor = SkyBluePrimary,
                    inactiveTrackColor = Color(0xFF334155)
                ),
                modifier = Modifier.testTag("volume_slider")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Audio Extraction Feature
            OutlinedButton(
                onClick = {
                    onExtractAudio()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("extract_audio_button"),
                border = BorderStroke(1.dp, SkyBluePrimary)
            ) {
                Icon(Icons.Default.CallSplit, contentDescription = null, tint = SkyBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extract Audio to Separate Track", color = SkyBluePrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterAdjustBottomSheet(
    currentFilter: VideoFilter,
    currentAdjustments: ColorAdjustments,
    onFilterSelected: (VideoFilter) -> Unit,
    onAdjustmentsChanged: (ColorAdjustments) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Filters, 1 = Adjustments

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = SkyBluePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Filters & LUTs", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Color Adjust", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Filters Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(VideoFilter.values()) { filter ->
                        val isSelected = filter == currentFilter
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onFilterSelected(filter) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        when (filter) {
                                            VideoFilter.SKY_VIVID -> Color(0xFF0284C7)
                                            VideoFilter.CINEMATIC -> Color(0xFF1E3A8A)
                                            VideoFilter.TEAL_ORANGE -> Color(0xFFEA580C)
                                            VideoFilter.VINTAGE_90S -> Color(0xFFB45309)
                                            VideoFilter.NOIR_BW -> Color(0xFF475569)
                                            VideoFilter.CYBERPUNK -> Color(0xFF9333EA)
                                            VideoFilter.WARM_GOLD -> Color(0xFFD97706)
                                            VideoFilter.DREAMY -> Color(0xFF059669)
                                            else -> Color(0xFF334155)
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) SkyBluePrimary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.displayName.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = filter.displayName,
                                fontSize = 11.sp,
                                color = if (isSelected) SkyBluePrimary else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // Adjustments Sliders
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AdjustmentSliderItem("Brightness", currentAdjustments.brightness, -0.5f..0.5f) {
                        onAdjustmentsChanged(currentAdjustments.copy(brightness = it))
                    }
                    AdjustmentSliderItem("Contrast", currentAdjustments.contrast, 0.5f..1.8f) {
                        onAdjustmentsChanged(currentAdjustments.copy(contrast = it))
                    }
                    AdjustmentSliderItem("Saturation", currentAdjustments.saturation, 0f..2.0f) {
                        onAdjustmentsChanged(currentAdjustments.copy(saturation = it))
                    }
                    AdjustmentSliderItem("Temperature", currentAdjustments.temperature, -0.8f..0.8f) {
                        onAdjustmentsChanged(currentAdjustments.copy(temperature = it))
                    }
                    AdjustmentSliderItem("Vignette", currentAdjustments.vignette, 0f..1f) {
                        onAdjustmentsChanged(currentAdjustments.copy(vignette = it))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth().testTag("filter_done_button")
            ) {
                Text("Done", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdjustmentSliderItem(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(String.format("%.2f", value), color = SkyBluePrimary, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = SkyBluePrimary,
                activeTrackColor = SkyBluePrimary,
                inactiveTrackColor = Color(0xFF334155)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsBottomSheet(
    currentEffect: VideoEffect,
    onEffectSelected: (VideoEffect) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Special Visual Effects",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VideoEffect.values()) { effect ->
                    val isSelected = effect == currentEffect
                    Surface(
                        onClick = { onEffectSelected(effect) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SkyBluePrimary else Color(0xFF1E293B),
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.testTag("effect_${effect.name}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = when (effect) {
                                    VideoEffect.GLITCH -> Icons.Default.Bolt
                                    VideoEffect.RGB_SPLIT -> Icons.Default.BlurOn
                                    VideoEffect.CAMERA_SHAKE -> Icons.Default.Vibration
                                    VideoEffect.SPARKLES -> Icons.Default.Star
                                    else -> Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF090E17) else SkyBluePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = effect.displayName,
                                color = if (isSelected) Color(0xFF090E17) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Effect", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitionsBottomSheet(
    currentTransition: TransitionType,
    onTransitionSelected: (TransitionType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Scene Transitions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TransitionType.values()) { trans ->
                    val isSelected = trans == currentTransition
                    Surface(
                        onClick = { onTransitionSelected(trans) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SkyBluePrimary else Color(0xFF1E293B),
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF090E17) else SkyBluePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = trans.displayName,
                                color = if (isSelected) Color(0xFF090E17) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioBottomSheet(
    onAddMusic: (String, Long) -> Unit,
    onPlaySoundEffect: (String) -> Unit,
    onAddSoundEffect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Royalty Free Music, 1 = Sound Effects

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = SkyBluePrimary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Music Library", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Sound Effects", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SampleMediaProvider.sampleMusic.forEach { track ->
                        Surface(
                            onClick = {
                                onAddMusic(track.title, track.durationMs)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(track.genre, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                                Button(
                                    onClick = {
                                        onAddMusic(track.title, track.durationMs)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                                ) {
                                    Text("Use", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SampleMediaProvider.soundEffects.forEach { sfx ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(sfx.icon, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(sfx.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(sfx.category, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { onPlaySoundEffect(sfx.name) }) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Preview", tint = SkyBluePrimary)
                                    }
                                    Button(
                                        onClick = {
                                            onAddSoundEffect(sfx.name)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                                    ) {
                                        Text("Add", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBottomSheet(
    onAddText: (String, TextStylePreset) -> Unit,
    onAddSticker: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(TextStylePreset.MODERN) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Add Text & Titles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Enter title or caption...", color = Color(0xFF64748B)) },
                modifier = Modifier.fillMaxWidth().testTag("text_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text("Typography Style Preset", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TextStylePreset.values()) { preset ->
                    val isSel = preset == selectedPreset
                    Surface(
                        onClick = { selectedPreset = preset },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) SkyBluePrimary else Color(0xFF1E293B),
                        border = if (isSel) null else BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = preset.displayName,
                            color = if (isSel) Color(0xFF090E17) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onAddText(textInput, selectedPreset)
                        onDismiss()
                    }
                },
                enabled = textInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth().testTag("add_text_submit_button")
            ) {
                Text("Add Title to Timeline", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text("Stickers & Emojis", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SampleMediaProvider.stickers) { (emoji, label) ->
                    Surface(
                        onClick = {
                            onAddSticker(emoji, label)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCaptionsBottomSheet(
    onGenerate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SkyBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Auto-Captions Generator",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Automatically transcribe and synchronize stylish subtitle animations across your video clips.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            SampleMediaProvider.autoCaptionStyles.forEach { (styleKey, title) ->
                Surface(
                    onClick = {
                        onGenerate(styleKey)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("caption_style_$styleKey")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkyBluePrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromaKeyBottomSheet(
    enabled: Boolean,
    threshold: Float,
    onToggle: (Boolean) -> Unit,
    onThresholdChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Green Screen / Chroma Key",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = SkyBluePrimary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Key color: Green (0x00FF00). Removes green background from overlay clips.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Color Threshold: ${(threshold * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
            Slider(
                value = threshold,
                onValueChange = onThresholdChange,
                valueRange = 0.1f..0.8f,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = SkyBluePrimary,
                    activeTrackColor = SkyBluePrimary,
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AspectRatioBottomSheet(
    currentRatio: AspectRatio,
    onRatioSelected: (AspectRatio) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D2E),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Canvas Aspect Ratio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            AspectRatio.values().forEach { ratio ->
                val isSelected = ratio == currentRatio
                Surface(
                    onClick = {
                        onRatioSelected(ratio)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) SkyBluePrimary else Color(0xFF1E293B),
                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("aspect_${ratio.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ratio.label,
                            color = if (isSelected) Color(0xFF090E17) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF090E17))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
