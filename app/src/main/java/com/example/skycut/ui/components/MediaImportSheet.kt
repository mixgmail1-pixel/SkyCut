package com.example.skycut.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycut.engine.SampleMediaProvider
import com.example.skycut.model.VideoClip
import com.example.ui.theme.SkyBluePrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaImportSheet(
    onImportLocalMedia: (Uri) -> Unit,
    onImportSampleMedia: (SampleMediaProvider.SampleMediaItem) -> Unit,
    onDismiss: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportLocalMedia(uri)
            onDismiss()
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Import Media",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Device Storage Button
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                modifier = Modifier.fillMaxWidth().testTag("pick_device_storage_button")
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF090E17))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select from Device Storage", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Or choose Sample 4K Footage:", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(260.dp)
            ) {
                items(SampleMediaProvider.sampleVideos) { sample ->
                    Surface(
                        onClick = {
                            onImportSampleMedia(sample)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.testTag("sample_${sample.title.take(5)}")
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = sample.drawableRes),
                                contentDescription = sample.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = sample.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${sample.durationMs / 1000}s", color = SkyBluePrimary, fontSize = 10.sp)
                                    Text("4K HDR", color = Color(0xFF64748B), fontSize = 9.sp)
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
