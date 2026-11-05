package com.example.skycut.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentPurpleFX
import com.example.ui.theme.SkyBluePrimary

@Composable
fun AiToolsDialog(
    onSelectScriptToVideo: (String) -> Unit,
    onTriggerAutoCaptions: () -> Unit,
    onTriggerSmartCut: () -> Unit,
    onTriggerBeatSync: () -> Unit,
    onDismiss: () -> Unit
) {
    var promptInput by remember { mutableStateOf("Cinematic travel reel exploring futuristic Tokyo") }
    var isGeneratingScript by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131D2E),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SkyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SkyCut AI Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature 1: AI Script-to-Video
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AI Script & Storyboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AccentPurpleFX.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🌐 External AI Service",
                                    color = AccentPurpleFX,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generates multi-scene storyboard clips and title subtitles automatically.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("Topic for video...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onSelectScriptToVideo(promptInput)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            modifier = Modifier.fillMaxWidth().testTag("generate_ai_storyboard_button")
                        ) {
                            Text("Generate Storyboard Cut", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feature 2: Auto Captions
                Surface(
                    onClick = {
                        onTriggerAutoCaptions()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("ai_auto_captions_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Auto-Captions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ On-Device", color = SkyBluePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Generate karaoke pop-in subtitles synced to timeline", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkyBluePrimary)
                    }
                }

                // Feature 3: Smart Cut (Silence removal)
                Surface(
                    onClick = {
                        onTriggerSmartCut()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("ai_smart_cut_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Smart Silence Cut", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ On-Device", color = SkyBluePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Automatically trims silent pauses between takes", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkyBluePrimary)
                    }
                }

                // Feature 4: Beat Sync
                Surface(
                    onClick = {
                        onTriggerBeatSync()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("ai_beat_sync_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Audio Beat Sync", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ On-Device", color = SkyBluePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Snaps clip cuts directly to music tempo beats", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkyBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
