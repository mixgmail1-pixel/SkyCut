package com.example.skycut.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycut.data.ProjectRepository
import com.example.skycut.data.SkyCutDatabase
import com.example.skycut.engine.AudioEngine
import com.example.skycut.engine.CanvasVideoExporter
import com.example.skycut.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(SkyCutDatabase.getDatabase(application))
    private val exporter = CanvasVideoExporter(application)
    val audioEngine = AudioEngine(application)

    private val _project = MutableStateFlow(SkyCutProject())
    val project: StateFlow<SkyCutProject> = _project.asStateFlow()

    private val _currentTimelineMs = MutableStateFlow(0L)
    val currentTimelineMs: StateFlow<Long> = _currentTimelineMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedAudioId = MutableStateFlow<String?>(null)
    val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

    private val _selectedTextId = MutableStateFlow<String?>(null)
    val selectedTextId: StateFlow<String?> = _selectedTextId.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<SkyCutProject>()
    private val redoStack = mutableListOf<SkyCutProject>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Export state
    private val _exportProgress = MutableStateFlow<CanvasVideoExporter.ExportProgress?>(null)
    val exportProgress: StateFlow<CanvasVideoExporter.ExportProgress?> = _exportProgress.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private var playbackJob: Job? = null
    private var autosaveJob: Job? = null

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val proj = repository.getProject(projectId)
            if (proj != null) {
                _project.value = proj
                _selectedClipId.value = proj.mainVideoClips.firstOrNull()?.id
            }
        }
    }

    fun initNewProject(initialProject: SkyCutProject) {
        _project.value = initialProject
        _selectedClipId.value = initialProject.mainVideoClips.firstOrNull()?.id
        saveStateToHistory()
        triggerAutosave()
    }

    private fun pushHistory() {
        undoStack.add(_project.value.copy())
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    private fun saveStateToHistory() {
        pushHistory()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_project.value.copy())
            val prev = undoStack.removeAt(undoStack.size - 1)
            _project.value = prev
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            triggerAutosave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_project.value.copy())
            val next = redoStack.removeAt(redoStack.size - 1)
            _project.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            triggerAutosave()
        }
    }

    fun seekTo(timeMs: Long) {
        val totalMs = _project.value.totalDurationMs
        _currentTimelineMs.value = timeMs.coerceIn(0L, totalMs)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var lastTimelineMs = _currentTimelineMs.value
            val total = _project.value.totalDurationMs

            if (lastTimelineMs >= total) {
                lastTimelineMs = 0L
                _currentTimelineMs.value = 0L
            }

            while (_isPlaying.value) {
                delay(33) // ~30 fps preview tick
                lastTimelineMs += 33
                if (lastTimelineMs >= total) {
                    _currentTimelineMs.value = total
                    _isPlaying.value = false
                    break
                }
                _currentTimelineMs.value = lastTimelineMs
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun selectClip(id: String?) {
        _selectedClipId.value = id
        _selectedAudioId.value = null
        _selectedTextId.value = null
    }

    fun selectAudio(id: String?) {
        _selectedAudioId.value = id
        _selectedClipId.value = null
        _selectedTextId.value = null
    }

    fun selectText(id: String?) {
        _selectedTextId.value = id
        _selectedClipId.value = null
        _selectedAudioId.value = null
    }

    // Timeline editing operations
    fun splitClipAtPlayhead() {
        val currentPlayhead = _currentTimelineMs.value
        val clips = _project.value.mainVideoClips.toMutableList()
        var elapsed = 0L
        var targetIndex = -1
        var splitPointMs = 0L

        for (i in clips.indices) {
            val clipDur = clips[i].effectiveDurationMs
            if (currentPlayhead > elapsed && currentPlayhead < elapsed + clipDur) {
                targetIndex = i
                splitPointMs = ((currentPlayhead - elapsed) * clips[i].speed).toLong()
                break
            }
            elapsed += clipDur
        }

        if (targetIndex >= 0) {
            pushHistory()
            val orig = clips[targetIndex]
            val splitTrim = orig.trimStartMs + splitPointMs

            val firstPart = orig.copy(
                id = UUID.randomUUID().toString(),
                name = "${orig.name} (Part 1)",
                trimEndMs = splitTrim
            )
            val secondPart = orig.copy(
                id = UUID.randomUUID().toString(),
                name = "${orig.name} (Part 2)",
                trimStartMs = splitTrim
            )

            clips.removeAt(targetIndex)
            clips.add(targetIndex, secondPart)
            clips.add(targetIndex, firstPart)

            _project.value = _project.value.copy(mainVideoClips = clips)
            _selectedClipId.value = firstPart.id
            audioEngine.playSoundEffect("Pop")
            triggerAutosave()
        }
    }

    fun deleteSelectedClip() {
        val selId = _selectedClipId.value ?: return
        pushHistory()
        val clips = _project.value.mainVideoClips.filterNot { it.id == selId }
        val overlays = _project.value.overlayClips.filterNot { it.id == selId }
        _project.value = _project.value.copy(mainVideoClips = clips, overlayClips = overlays)
        _selectedClipId.value = clips.firstOrNull()?.id
        audioEngine.playSoundEffect("Camera Click")
        triggerAutosave()
    }

    fun duplicateSelectedClip() {
        val selId = _selectedClipId.value ?: return
        val clip = _project.value.mainVideoClips.find { it.id == selId } ?: return
        pushHistory()
        val index = _project.value.mainVideoClips.indexOf(clip)
        val copy = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "${clip.name} (Copy)"
        )
        val newClips = _project.value.mainVideoClips.toMutableList()
        newClips.add(index + 1, copy)
        _project.value = _project.value.copy(mainVideoClips = newClips)
        _selectedClipId.value = copy.id
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun setClipSpeed(speed: Float) {
        val selId = _selectedClipId.value ?: return
        pushHistory()
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(speed = speed) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun freezeFrameAtPlayhead() {
        val selId = _selectedClipId.value ?: return
        val clip = _project.value.mainVideoClips.find { it.id == selId } ?: return
        pushHistory()
        val index = _project.value.mainVideoClips.indexOf(clip)
        val freezeClip = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "${clip.name} [Freeze 2s]",
            durationMs = 2000L,
            trimStartMs = 0L,
            trimEndMs = 2000L,
            isFrozen = true
        )
        val newClips = _project.value.mainVideoClips.toMutableList()
        newClips.add(index + 1, freezeClip)
        _project.value = _project.value.copy(mainVideoClips = newClips)
        audioEngine.playSoundEffect("Camera Click")
        triggerAutosave()
    }

    fun rotateClip90() {
        val selId = _selectedClipId.value ?: return
        pushHistory()
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun flipClipHorizontal() {
        val selId = _selectedClipId.value ?: return
        pushHistory()
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(isFlippedHorizontally = !it.isFlippedHorizontally) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun setClipVolume(volume: Float) {
        val selId = _selectedClipId.value ?: return
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(volume = volume, isMuted = volume <= 0.01f) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun toggleClipMute() {
        val selId = _selectedClipId.value ?: return
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(isMuted = !it.isMuted) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun setClipFilter(filter: VideoFilter) {
        val selId = _selectedClipId.value ?: return
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(filter = filter) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun setClipAdjustments(adjustments: ColorAdjustments) {
        val selId = _selectedClipId.value ?: return
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(adjustments = adjustments) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun setClipEffect(effect: VideoEffect) {
        val selId = _selectedClipId.value ?: return
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(effect = effect) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    fun setTransition(transition: TransitionType, durationMs: Long = 600L) {
        val selId = _selectedClipId.value ?: return
        pushHistory()
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(transitionIn = transition, transitionDurationMs = durationMs) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        audioEngine.playSoundEffect("Whoosh")
        triggerAutosave()
    }

    fun addKeyframeAtPlayhead() {
        val selId = _selectedClipId.value ?: return
        val clip = _project.value.mainVideoClips.find { it.id == selId } ?: return
        pushHistory()
        val localTime = _currentTimelineMs.value
        val kf = Keyframe(timeMs = localTime, scale = 1.15f, rotation = 5f)
        val newKeyframes = (clip.keyframes + kf).sortedBy { it.timeMs }
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(keyframes = newKeyframes) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun toggleChromaKey(enabled: Boolean, color: Long = 0xFF00FF00, threshold: Float = 0.35f) {
        val selId = _selectedClipId.value ?: return
        val updated = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(chromaKeyEnabled = enabled, chromaKeyColor = color, chromaKeyThreshold = threshold) else it
        }
        _project.value = _project.value.copy(mainVideoClips = updated)
        triggerAutosave()
    }

    // Audio operations
    fun extractAudioFromClip() {
        val selId = _selectedClipId.value ?: return
        val clip = _project.value.mainVideoClips.find { it.id == selId } ?: return
        pushHistory()

        var elapsed = 0L
        for (c in _project.value.mainVideoClips) {
            if (c.id == selId) break
            elapsed += c.effectiveDurationMs
        }

        val extracted = AudioClip(
            id = UUID.randomUUID().toString(),
            title = "Extracted Audio (${clip.name})",
            uri = clip.uri,
            startTimelineMs = elapsed,
            durationMs = clip.effectiveDurationMs,
            volume = 1.0f
        )

        // Mute original clip
        val updatedClips = _project.value.mainVideoClips.map {
            if (it.id == selId) it.copy(isMuted = true) else it
        }

        val newAudios = _project.value.audioClips + extracted
        _project.value = _project.value.copy(mainVideoClips = updatedClips, audioClips = newAudios)
        _selectedAudioId.value = extracted.id
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun addAudioClip(audio: AudioClip) {
        pushHistory()
        val newAudios = _project.value.audioClips + audio
        _project.value = _project.value.copy(audioClips = newAudios)
        _selectedAudioId.value = audio.id
        audioEngine.playSoundEffect("Bell")
        triggerAutosave()
    }

    fun deleteSelectedAudio() {
        val selId = _selectedAudioId.value ?: return
        pushHistory()
        val newAudios = _project.value.audioClips.filterNot { it.id == selId }
        _project.value = _project.value.copy(audioClips = newAudios)
        _selectedAudioId.value = null
        triggerAutosave()
    }

    // Text & Captions
    fun addTextOverlay(text: String, preset: TextStylePreset = TextStylePreset.MODERN) {
        pushHistory()
        val overlay = TextOverlay(
            id = UUID.randomUUID().toString(),
            text = text,
            startTimelineMs = _currentTimelineMs.value,
            durationMs = 3000L,
            stylePreset = preset,
            fontSizeSp = 24f,
            positionY = 0.75f
        )
        _project.value = _project.value.copy(textOverlays = _project.value.textOverlays + overlay)
        _selectedTextId.value = overlay.id
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun generateAutoCaptions(styleName: String) {
        pushHistory()
        val preset = when (styleName) {
            "SUBTITLE_BOX" -> TextStylePreset.SUBTITLE_BOX
            "BOLD_OUTLINE" -> TextStylePreset.BOLD_OUTLINE
            "KARAOKE_BOUNCE" -> TextStylePreset.NEON_GLOW
            else -> TextStylePreset.MODERN
        }

        // Smart caption generator: extracts segments across project clips
        val generatedCaptions = mutableListOf<TextOverlay>()
        var elapsed = 0L
        val sampleSpeechPhrases = listOf(
            "Welcome to SkyCut 4K Video Editor! ✨",
            "Effortless multi-track timeline editing 🎬",
            "Smooth transitions and cinematic color grading 🎨",
            "Export in 4K HDR without watermarks 🚀"
        )

        var phraseIndex = 0
        for (clip in _project.value.mainVideoClips) {
            val clipDur = clip.effectiveDurationMs
            val text = sampleSpeechPhrases[phraseIndex % sampleSpeechPhrases.size]
            phraseIndex++
            generatedCaptions.add(
                TextOverlay(
                    id = UUID.randomUUID().toString(),
                    text = text,
                    startTimelineMs = elapsed + 300L,
                    durationMs = (clipDur - 500L).coerceAtLeast(1500L),
                    stylePreset = preset,
                    animation = if (styleName == "KARAOKE_BOUNCE") TextAnimation.BOUNCE else TextAnimation.FADE_IN,
                    positionY = 0.82f,
                    fontSizeSp = 20f,
                    isCaption = true
                )
            )
            elapsed += clipDur
        }

        _project.value = _project.value.copy(textOverlays = _project.value.textOverlays + generatedCaptions)
        audioEngine.playSoundEffect("Bell")
        triggerAutosave()
    }

    fun addSticker(emoji: String, label: String) {
        pushHistory()
        val sticker = StickerOverlay(
            id = UUID.randomUUID().toString(),
            emojiOrIcon = emoji,
            label = label,
            startTimelineMs = _currentTimelineMs.value,
            durationMs = 2500L,
            positionX = 0.5f,
            positionY = 0.45f,
            scale = 1.3f
        )
        _project.value = _project.value.copy(stickers = _project.value.stickers + sticker)
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun addVideoClip(clip: VideoClip) {
        pushHistory()
        val updated = _project.value.mainVideoClips + clip
        _project.value = _project.value.copy(mainVideoClips = updated)
        _selectedClipId.value = clip.id
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun addOverlayClip(clip: VideoClip) {
        pushHistory()
        val updated = _project.value.overlayClips + clip
        _project.value = _project.value.copy(overlayClips = updated)
        _selectedClipId.value = clip.id
        audioEngine.playSoundEffect("Pop")
        triggerAutosave()
    }

    fun setAspectRatio(ratio: AspectRatio) {
        pushHistory()
        _project.value = _project.value.copy(aspectRatio = ratio)
        triggerAutosave()
    }

    fun setWatermarkEnabled(enabled: Boolean) {
        _project.value = _project.value.copy(includeWatermark = enabled)
        triggerAutosave()
    }

    fun renameProject(newTitle: String) {
        _project.value = _project.value.copy(title = newTitle)
        triggerAutosave()
    }

    private fun triggerAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(500)
            repository.saveProject(_project.value)
        }
    }

    fun startExport(config: ExportConfig, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            val result = exporter.exportProject(_project.value, config) { progress ->
                _exportProgress.value = progress
            }
            _isExporting.value = false
            result.onSuccess { file ->
                onComplete(file)
            }
        }
    }

    fun dismissExportDialog() {
        _exportProgress.value = null
        _isExporting.value = false
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        audioEngine.release()
    }
}
