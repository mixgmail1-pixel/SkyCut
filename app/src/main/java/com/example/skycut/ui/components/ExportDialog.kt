package com.example.skycut.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.skycut.engine.CanvasVideoExporter
import com.example.skycut.model.ExportConfig
import com.example.ui.theme.AccentRedCut
import com.example.ui.theme.SkyBluePrimary
import java.io.File

@Composable
fun ExportConfigDialog(
    initialConfig: ExportConfig = ExportConfig(),
    onConfirmExport: (ExportConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf(initialConfig.resolutionLabel) }
    var selectedFps by remember { mutableStateOf(initialConfig.fps) }
    var selectedBitrate by remember { mutableStateOf(initialConfig.bitrateMbps) }
    var includeWatermark by remember { mutableStateOf(initialConfig.includeWatermark) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131D2E),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resolution options
                Text("Resolution", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("720p (HD)" to (720 to 1280), "1080p (FHD)" to (1080 to 1920), "4K (UHD)" to (2160 to 3840)).forEach { (label, dims) ->
                        val isSel = selectedRes == label
                        Surface(
                            onClick = { selectedRes = label },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SkyBluePrimary else Color(0xFF1E293B),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f).testTag("res_${label.take(4)}")
                        ) {
                            Text(
                                text = label.takeWhile { it != ' ' },
                                color = if (isSel) Color(0xFF090E17) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frame Rate
                Text("Frame Rate", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(24, 30, 60).forEach { fps ->
                        val isSel = selectedFps == fps
                        Surface(
                            onClick = { selectedFps = fps },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SkyBluePrimary else Color(0xFF1E293B),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${fps}fps",
                                color = if (isSel) Color(0xFF090E17) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Watermark toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Add SkyCut Watermark", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Free - completely optional", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = includeWatermark,
                        onCheckedChange = { includeWatermark = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SkyBluePrimary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Export Button
                Button(
                    onClick = {
                        val (w, h) = when (selectedRes) {
                            "720p (HD)" -> 720 to 1280
                            "4K (UHD)" -> 2160 to 3840
                            else -> 1080 to 1920
                        }
                        val cfg = ExportConfig(
                            resolutionLabel = selectedRes,
                            width = w,
                            height = h,
                            fps = selectedFps,
                            bitrateMbps = selectedBitrate,
                            includeWatermark = includeWatermark
                        )
                        onConfirmExport(cfg)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_export_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF090E17))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export MP4 Video", color = Color(0xFF090E17), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ExportProgressDialog(
    progress: CanvasVideoExporter.ExportProgress?,
    onShare: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131D2E),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (progress?.error != null) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = AccentRedCut,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Export Error", color = AccentRedCut, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(progress.error, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onDismiss) { Text("Close") }
                } else if (progress?.isFinished == true && progress.outputFile != null) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Done",
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Video Exported Successfully!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saved at: ${progress.outputFile.name} (${progress.outputFile.length() / 1024} KB)",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onShare(progress.outputFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            modifier = Modifier.weight(1f).testTag("share_video_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF090E17))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).testTag("export_done_close_button"),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text("Done", color = Color.White)
                        }
                    }
                } else {
                    // In-progress rendering
                    CircularProgressIndicator(
                        progress = { (progress?.progressPercent ?: 0) / 100f },
                        modifier = Modifier.size(64.dp),
                        color = SkyBluePrimary,
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rendering Video...",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${progress?.progressPercent ?: 0}% • Frame ${progress?.currentFrame ?: 0} / ${progress?.totalFrames ?: 0}",
                        color = SkyBluePrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Encoding H.264 MP4 with effects, audio & text layers",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

fun shareVideoFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share SkyCut Video"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
