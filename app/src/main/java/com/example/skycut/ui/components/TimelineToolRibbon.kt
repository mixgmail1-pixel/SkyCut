package com.example.skycut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentRedCut
import com.example.ui.theme.SkyBluePrimary

enum class ToolRibbonAction(val label: String, val icon: ImageVector, val tag: String) {
    SPLIT("Split", Icons.Default.ContentCut, "tool_split"),
    SPEED("Speed", Icons.Default.Speed, "tool_speed"),
    VOLUME("Volume", Icons.Default.VolumeUp, "tool_volume"),
    FILTERS("Filters", Icons.Default.FilterVintage, "tool_filters"),
    ADJUST("Adjust", Icons.Default.Tune, "tool_adjust"),
    EFFECTS("Effects", Icons.Default.AutoAwesome, "tool_effects"),
    TRANSITION("Transition", Icons.Default.SwapHoriz, "tool_transition"),
    AUDIO("Audio", Icons.Default.MusicNote, "tool_audio"),
    TEXT("Text", Icons.Default.Title, "tool_text"),
    CAPTIONS("Captions", Icons.Default.ClosedCaption, "tool_captions"),
    KEYFRAME("Keyframe", Icons.Default.Diamond, "tool_keyframe"),
    CHROMA("Chroma Key", Icons.Default.ColorLens, "tool_chroma"),
    FREEZE("Freeze", Icons.Default.AcUnit, "tool_freeze"),
    ROTATE("Rotate", Icons.Default.RotateRight, "tool_rotate"),
    FLIP("Flip", Icons.Default.Flip, "tool_flip"),
    DUPLICATE("Duplicate", Icons.Default.ContentCopy, "tool_duplicate"),
    DELETE("Delete", Icons.Default.Delete, "tool_delete")
}

@Composable
fun TimelineToolRibbon(
    isClipSelected: Boolean,
    isAudioSelected: Boolean,
    isTextSelected: Boolean,
    onActionClick: (ToolRibbonAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = Color(0xFF090E17),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolRibbonAction.values().forEach { action ->
                val isDestructive = action == ToolRibbonAction.DELETE
                val isHighlighted = action == ToolRibbonAction.SPLIT || action == ToolRibbonAction.CAPTIONS

                Surface(
                    onClick = { onActionClick(action) },
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isHighlighted && isClipSelected -> Color(0xFF1E293B)
                        else -> Color.Transparent
                    },
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .testTag(action.tag)
                ) {
                    Column(
                        modifier = Modifier
                            .width(58.dp)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = when {
                                isDestructive -> AccentRedCut
                                isHighlighted -> SkyBluePrimary
                                else -> Color(0xFFE2E8F0)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = action.label,
                            color = when {
                                isDestructive -> AccentRedCut
                                isHighlighted -> SkyBluePrimary
                                else -> Color(0xFF94A3B8)
                            },
                            fontSize = 10.sp,
                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
