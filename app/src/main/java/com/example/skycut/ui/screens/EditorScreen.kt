package com.example.skycut.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
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
import com.example.skycut.model.*
import com.example.skycut.ui.components.*
import com.example.skycut.viewmodel.EditorViewModel
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    projectId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe ViewModel state
    val project by viewModel.project.collectAsState()
    val currentTimelineMs by viewModel.currentTimelineMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val selectedAudioId by viewModel.selectedAudioId.collectAsState()
    val selectedTextId by viewModel.selectedTextId.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()

    // Sheet states
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showVolumeSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showEffectsSheet by remember { mutableStateOf(false) }
    var showTransitionsSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }
    var showCaptionsSheet by remember { mutableStateOf(false) }
    var showChromaSheet by remember { mutableStateOf(false) }
    var showAspectRatioSheet by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showExportConfigDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    // Active selected clip
    val selectedClip = remember(project, selectedClipId) {
        project.mainVideoClips.find { it.id == selectedClipId }
            ?: project.overlayClips.find { it.id == selectedClipId }
    }

    LaunchedEffect(projectId) {
        if (projectId.isNotBlank()) {
            viewModel.loadProject(projectId)
        }
    }

    BackHandler {
        viewModel.pause()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.pause()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    TextButton(onClick = { showRenameDialog = true }) {
                        Text(
                            text = project.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("editor_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) SkyBluePrimary else Color(0xFF475569)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("editor_redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) SkyBluePrimary else Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Prominent Export Button
                    Button(
                        onClick = {
                            viewModel.pause()
                            showExportConfigDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("editor_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Color(0xFF090E17),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export",
                            color = Color(0xFF090E17),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090E17))
            )
        },
        containerColor = Color(0xFF090E17)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Real Video Preview Viewport
            VideoPreviewCanvas(
                project = project,
                currentTimelineMs = currentTimelineMs,
                isPlaying = isPlaying,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSelectAspectRatio = { showAspectRatioSheet = true },
                modifier = Modifier.weight(1f)
            )

            // 2. Multi-Track Timeline
            MultiTrackTimeline(
                project = project,
                currentTimelineMs = currentTimelineMs,
                selectedClipId = selectedClipId,
                selectedAudioId = selectedAudioId,
                selectedTextId = selectedTextId,
                onSeek = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onSelectAudio = { viewModel.selectAudio(it) },
                onSelectText = { viewModel.selectText(it) },
                onAddClip = { showImportSheet = true },
                onOpenTransitionDialog = {
                    viewModel.selectClip(it)
                    showTransitionsSheet = true
                },
                modifier = Modifier.weight(0.95f)
            )

            // 3. CapCut Bottom Action Ribbon
            TimelineToolRibbon(
                isClipSelected = selectedClipId != null,
                isAudioSelected = selectedAudioId != null,
                isTextSelected = selectedTextId != null,
                onActionClick = { action ->
                    when (action) {
                        ToolRibbonAction.SPLIT -> viewModel.splitClipAtPlayhead()
                        ToolRibbonAction.SPEED -> showSpeedSheet = true
                        ToolRibbonAction.VOLUME -> showVolumeSheet = true
                        ToolRibbonAction.FILTERS -> showFilterSheet = true
                        ToolRibbonAction.ADJUST -> showFilterSheet = true
                        ToolRibbonAction.EFFECTS -> showEffectsSheet = true
                        ToolRibbonAction.TRANSITION -> showTransitionsSheet = true
                        ToolRibbonAction.AUDIO -> showAudioSheet = true
                        ToolRibbonAction.TEXT -> showTextSheet = true
                        ToolRibbonAction.CAPTIONS -> showCaptionsSheet = true
                        ToolRibbonAction.KEYFRAME -> viewModel.addKeyframeAtPlayhead()
                        ToolRibbonAction.CHROMA -> showChromaSheet = true
                        ToolRibbonAction.FREEZE -> viewModel.freezeFrameAtPlayhead()
                        ToolRibbonAction.ROTATE -> viewModel.rotateClip90()
                        ToolRibbonAction.FLIP -> viewModel.flipClipHorizontal()
                        ToolRibbonAction.DUPLICATE -> viewModel.duplicateSelectedClip()
                        ToolRibbonAction.DELETE -> {
                            if (selectedClipId != null) {
                                viewModel.deleteSelectedClip()
                            } else if (selectedAudioId != null) {
                                viewModel.deleteSelectedAudio()
                            }
                        }
                    }
                }
            )
        }
    }

    // Modal Bottom Sheets
    if (showSpeedSheet) {
        SpeedBottomSheet(
            currentSpeed = selectedClip?.speed ?: 1.0f,
            onSpeedSelected = { viewModel.setClipSpeed(it) },
            onDismiss = { showSpeedSheet = false }
        )
    }

    if (showVolumeSheet) {
        VolumeBottomSheet(
            currentVolume = selectedClip?.volume ?: 1.0f,
            isMuted = selectedClip?.isMuted ?: false,
            onVolumeChanged = { viewModel.setClipVolume(it) },
            onToggleMute = { viewModel.toggleClipMute() },
            onExtractAudio = { viewModel.extractAudioFromClip() },
            onDismiss = { showVolumeSheet = false }
        )
    }

    if (showFilterSheet) {
        FilterAdjustBottomSheet(
            currentFilter = selectedClip?.filter ?: VideoFilter.ORIGINAL,
            currentAdjustments = selectedClip?.adjustments ?: ColorAdjustments(),
            onFilterSelected = { viewModel.setClipFilter(it) },
            onAdjustmentsChanged = { viewModel.setClipAdjustments(it) },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showEffectsSheet) {
        EffectsBottomSheet(
            currentEffect = selectedClip?.effect ?: VideoEffect.NONE,
            onEffectSelected = { viewModel.setClipEffect(it) },
            onDismiss = { showEffectsSheet = false }
        )
    }

    if (showTransitionsSheet) {
        TransitionsBottomSheet(
            currentTransition = selectedClip?.transitionIn ?: TransitionType.NONE,
            onTransitionSelected = { viewModel.setTransition(it) },
            onDismiss = { showTransitionsSheet = false }
        )
    }

    if (showAudioSheet) {
        AudioBottomSheet(
            onAddMusic = { title, durationMs ->
                val newAudio = AudioClip(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    uri = "asset://music/$title",
                    startTimelineMs = currentTimelineMs,
                    durationMs = durationMs
                )
                viewModel.addAudioClip(newAudio)
            },
            onPlaySoundEffect = { viewModel.audioEngine.playSoundEffect(it) },
            onAddSoundEffect = { name ->
                val sfx = AudioClip(
                    id = UUID.randomUUID().toString(),
                    title = "SFX: $name",
                    uri = "sfx://$name",
                    startTimelineMs = currentTimelineMs,
                    durationMs = 1500L,
                    isSoundEffect = true
                )
                viewModel.addAudioClip(sfx)
            },
            onDismiss = { showAudioSheet = false }
        )
    }

    if (showTextSheet) {
        TextBottomSheet(
            onAddText = { text, preset -> viewModel.addTextOverlay(text, preset) },
            onAddSticker = { emoji, label -> viewModel.addSticker(emoji, label) },
            onDismiss = { showTextSheet = false }
        )
    }

    if (showCaptionsSheet) {
        AutoCaptionsBottomSheet(
            onGenerate = { style -> viewModel.generateAutoCaptions(style) },
            onDismiss = { showCaptionsSheet = false }
        )
    }

    if (showChromaSheet) {
        ChromaKeyBottomSheet(
            enabled = selectedClip?.chromaKeyEnabled ?: false,
            threshold = selectedClip?.chromaKeyThreshold ?: 0.35f,
            onToggle = { viewModel.toggleChromaKey(it) },
            onThresholdChange = { viewModel.toggleChromaKey(true, threshold = it) },
            onDismiss = { showChromaSheet = false }
        )
    }

    if (showAspectRatioSheet) {
        AspectRatioBottomSheet(
            currentRatio = project.aspectRatio,
            onRatioSelected = { viewModel.setAspectRatio(it) },
            onDismiss = { showAspectRatioSheet = false }
        )
    }

    if (showImportSheet) {
        MediaImportSheet(
            onImportLocalMedia = { uri: Uri ->
                val newClip = VideoClip(
                    name = "Imported Video",
                    uri = uri.toString(),
                    durationMs = 6000L
                )
                viewModel.addVideoClip(newClip)
            },
            onImportSampleMedia = { sample ->
                val newClip = VideoClip(
                    name = sample.title,
                    uri = sample.title,
                    durationMs = sample.durationMs,
                    thumbnailRes = sample.drawableRes,
                    filter = sample.filter,
                    isSampleMedia = true
                )
                viewModel.addVideoClip(newClip)
            },
            onDismiss = { showImportSheet = false }
        )
    }

    // Export Dialogs
    if (showExportConfigDialog) {
        ExportConfigDialog(
            initialConfig = ExportConfig(includeWatermark = project.includeWatermark),
            onConfirmExport = { config ->
                showExportConfigDialog = false
                viewModel.startExport(config) { /* completed */ }
            },
            onDismiss = { showExportConfigDialog = false }
        )
    }

    if (isExporting || exportProgress != null) {
        ExportProgressDialog(
            progress = exportProgress,
            onShare = { file -> shareVideoFile(context, file) },
            onDismiss = { viewModel.dismissExportDialog() }
        )
    }

    // Project Rename Dialog
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(project.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = Color(0xFF131D2E),
            title = { Text("Rename Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBluePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.renameProject(newTitle)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                ) {
                    Text("Save", color = Color(0xFF090E17), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
