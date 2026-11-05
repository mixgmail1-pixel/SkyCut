package com.example.skycut.engine

import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import com.example.skycut.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.*

class CanvasVideoExporter(private val context: Context) {

    data class ExportProgress(
        val progressPercent: Int,
        val currentFrame: Int,
        val totalFrames: Int,
        val isFinished: Boolean = false,
        val outputFile: File? = null,
        val error: String? = null
    )

    suspend fun exportProject(
        project: SkyCutProject,
        config: ExportConfig,
        onProgress: (ExportProgress) -> Unit
    ): Result<File> = withContext(Dispatchers.Default) {
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null
        var outputFile: File? = null

        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val fileName = "SkyCut_${System.currentTimeMillis()}.mp4"
            outputFile = File(exportDir, fileName)

            val width = (config.width / 16) * 16 // Ensure 16-pixel alignment for H.264
            val height = (config.height / 16) * 16
            val fps = config.fps
            val bitRate = config.bitrateMbps * 1024 * 1024
            val totalDurationMs = project.totalDurationMs.coerceAtLeast(1000L)
            val totalFrames = ((totalDurationMs / 1000.0) * fps).toInt().coerceAtLeast(1)

            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe interval
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val frameDurationUs = (1_000_000L / fps)

            // Cache sample bitmaps if any
            val sampleBitmapCache = mutableMapOf<Int, Bitmap>()

            for (frameIndex in 0 until totalFrames) {
                val currentTimelineMs = (frameIndex * 1000L) / fps
                val presentationTimeUs = frameIndex * frameDurationUs

                // Lock Canvas on encoder surface
                val canvas = inputSurface.lockHardwareCanvas()
                try {
                    // 1. Draw base project background
                    canvas.drawColor(Color.BLACK)

                    // 2. Render Main Video Track clip at this timestamp
                    renderMainTrack(
                        canvas = canvas,
                        project = project,
                        timelineMs = currentTimelineMs,
                        width = width,
                        height = height,
                        sampleCache = sampleBitmapCache
                    )

                    // 3. Render Overlays / PIP
                    renderOverlays(
                        canvas = canvas,
                        project = project,
                        timelineMs = currentTimelineMs,
                        width = width,
                        height = height,
                        sampleCache = sampleBitmapCache
                    )

                    // 4. Render Text & Subtitles
                    renderTextOverlays(
                        canvas = canvas,
                        project = project,
                        timelineMs = currentTimelineMs,
                        width = width,
                        height = height
                    )

                    // 5. Render Stickers
                    renderStickers(
                        canvas = canvas,
                        project = project,
                        timelineMs = currentTimelineMs,
                        width = width,
                        height = height
                    )

                    // 6. Watermark if enabled
                    if (config.includeWatermark || project.includeWatermark) {
                        renderWatermark(canvas, width, height)
                    }

                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain encoder outputs
                while (true) {
                    val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw RuntimeException("Format changed after muxer started")
                        }
                        val newFormat = encoder.outputFormat
                        videoTrackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (outputBufferIndex >= 0) {
                        val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                        if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && bufferInfo.size > 0) {
                            if (muxerStarted) {
                                encodedData.position(bufferInfo.offset)
                                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                bufferInfo.presentationTimeUs = presentationTimeUs
                                muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                            }
                        }
                        encoder.releaseOutputBuffer(outputBufferIndex, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    }
                }

                val progress = ((frameIndex + 1) * 100) / totalFrames
                onProgress(
                    ExportProgress(
                        progressPercent = progress,
                        currentFrame = frameIndex + 1,
                        totalFrames = totalFrames
                    )
                )
            }

            // Signal End of Stream
            encoder.signalEndOfInputStream()
            var eos = false
            while (!eos) {
                val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                    if (encodedData != null && muxerStarted && bufferInfo.size > 0 && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eos = true
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            sampleBitmapCache.values.forEach { it.recycle() }
            sampleBitmapCache.clear()

            onProgress(
                ExportProgress(
                    progressPercent = 100,
                    currentFrame = totalFrames,
                    totalFrames = totalFrames,
                    isFinished = true,
                    outputFile = outputFile
                )
            )

            Result.success(outputFile)
        } catch (e: Exception) {
            outputFile?.delete()
            onProgress(
                ExportProgress(
                    progressPercent = 0,
                    currentFrame = 0,
                    totalFrames = 0,
                    error = e.localizedMessage ?: "Export failed"
                )
            )
            Result.failure(e)
        } finally {
            try {
                encoder?.stop()
                encoder?.release()
            } catch (e: Exception) { /* ignored */ }
            try {
                muxer?.stop()
                muxer?.release()
            } catch (e: Exception) { /* ignored */ }
            inputSurface?.release()
        }
    }

    private fun renderMainTrack(
        canvas: Canvas,
        project: SkyCutProject,
        timelineMs: Long,
        width: Int,
        height: Int,
        sampleCache: MutableMap<Int, Bitmap>
    ) {
        var elapsedMs = 0L
        var activeClip: VideoClip? = null
        var clipLocalTimeMs = 0L
        var isTransition = false
        var transitionFraction = 0f
        var nextClip: VideoClip? = null

        for (i in project.mainVideoClips.indices) {
            val clip = project.mainVideoClips[i]
            val clipDur = clip.effectiveDurationMs
            if (timelineMs >= elapsedMs && timelineMs < elapsedMs + clipDur) {
                activeClip = clip
                clipLocalTimeMs = ((timelineMs - elapsedMs) * clip.speed).toLong()

                // Check transition with previous or next
                if (clip.transitionIn != TransitionType.NONE && (timelineMs - elapsedMs) < clip.transitionDurationMs) {
                    isTransition = true
                    transitionFraction = (timelineMs - elapsedMs).toFloat() / clip.transitionDurationMs.toFloat()
                }
                break
            }
            elapsedMs += clipDur
        }

        if (activeClip == null) {
            // Draw default background
            val paint = Paint().apply {
                color = Color.parseColor("#0F172A")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            return
        }

        // Draw clip content
        drawClipFrame(
            canvas = canvas,
            clip = activeClip,
            localTimeMs = clipLocalTimeMs,
            width = width,
            height = height,
            sampleCache = sampleCache,
            transitionIn = if (isTransition) activeClip.transitionIn else TransitionType.NONE,
            transitionFraction = transitionFraction
        )
    }

    private fun renderOverlays(
        canvas: Canvas,
        project: SkyCutProject,
        timelineMs: Long,
        width: Int,
        height: Int,
        sampleCache: MutableMap<Int, Bitmap>
    ) {
        for (overlay in project.overlayClips) {
            if (timelineMs in 0L..overlay.effectiveDurationMs) {
                canvas.save()
                // Overlay default PIP rect: 50% size at bottom right
                val pipWidth = width * 0.45f
                val pipHeight = height * 0.45f
                val pipX = width * 0.5f
                val pipY = height * 0.5f
                canvas.translate(pipX, pipY)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                drawClipFrame(
                    canvas = canvas,
                    clip = overlay,
                    localTimeMs = timelineMs,
                    width = pipWidth.toInt(),
                    height = pipHeight.toInt(),
                    sampleCache = sampleCache,
                    transitionIn = TransitionType.NONE,
                    transitionFraction = 1f
                )
                canvas.restore()
            }
        }
    }

    private fun drawClipFrame(
        canvas: Canvas,
        clip: VideoClip,
        localTimeMs: Long,
        width: Int,
        height: Int,
        sampleCache: MutableMap<Int, Bitmap>,
        transitionIn: TransitionType,
        transitionFraction: Float
    ) {
        val saveCount = canvas.save()

        // 1. Apply keyframes if any
        if (clip.keyframes.isNotEmpty()) {
            val kf = interpolateKeyframe(clip.keyframes, localTimeMs)
            canvas.scale(kf.scale, kf.scale, width / 2f, height / 2f)
            canvas.translate(kf.translationX * width, kf.translationY * height)
            canvas.rotate(kf.rotation, width / 2f, height / 2f)
        }

        // 2. Rotation & Flip
        if (clip.rotationDegrees != 0) {
            canvas.rotate(clip.rotationDegrees.toFloat(), width / 2f, height / 2f)
        }
        if (clip.isFlippedHorizontally) {
            canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }

        // 3. Transitions
        when (transitionIn) {
            TransitionType.FADE_TO_BLACK -> {
                // Fade from black
                val alpha = (transitionFraction * 255).toInt().coerceIn(0, 255)
                canvas.drawColor(Color.argb(255 - alpha, 0, 0, 0))
            }
            TransitionType.WHITE_FLASH -> {
                val alpha = ((1f - transitionFraction) * 255).toInt().coerceIn(0, 255)
                canvas.drawColor(Color.argb(alpha, 255, 255, 255))
            }
            TransitionType.SLIDE_LEFT -> {
                val dx = (1f - transitionFraction) * width
                canvas.translate(dx, 0f)
            }
            TransitionType.SLIDE_RIGHT -> {
                val dx = -(1f - transitionFraction) * width
                canvas.translate(dx, 0f)
            }
            TransitionType.ZOOM_IN -> {
                val scale = 0.5f + (transitionFraction * 0.5f)
                canvas.scale(scale, scale, width / 2f, height / 2f)
            }
            else -> {}
        }

        // 4. Draw media or sample graphic
        if (clip.thumbnailRes != null) {
            var bmp = sampleCache[clip.thumbnailRes]
            if (bmp == null || bmp.isRecycled) {
                bmp = BitmapFactory.decodeResource(context.resources, clip.thumbnailRes)
                if (bmp != null) sampleCache[clip.thumbnailRes] = bmp
            }
            if (bmp != null && !bmp.isRecycled) {
                val srcRect = Rect(0, 0, bmp.width, bmp.height)
                val dstRect = Rect(0, 0, width, height)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                applyFilterAndAdjustments(paint, clip)
                canvas.drawBitmap(bmp, srcRect, dstRect, paint)
            }
        } else {
            // Draw solid/gradient background with time-based visual
            val bgPaint = Paint().apply {
                color = clip.backgroundColorHex.toInt()
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Draw animated abstract video bars or clip name
            val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#38BDF8")
                strokeWidth = 6f
                style = Paint.Style.STROKE
            }
            val path = Path()
            val waveY = height * 0.5f
            for (x in 0..width step 30) {
                val angle = (x + localTimeMs * 0.5) * 0.02
                val y = waveY + sin(angle).toFloat() * 60f
                if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
            }
            canvas.drawPath(path, wavePaint)
        }

        // 5. Effects (Glitch, RGB split, Grain)
        applyVideoEffect(canvas, clip.effect, width, height, localTimeMs)

        canvas.restoreToCount(saveCount)
    }

    private fun applyFilterAndAdjustments(paint: Paint, clip: VideoClip) {
        val filter = clip.filter
        val adj = clip.adjustments

        val totalBrightness = (filter.brightness + adj.brightness) * 255f
        val totalContrast = filter.contrast * adj.contrast
        val totalSat = filter.saturation * adj.saturation

        val cm = ColorMatrix()
        // Saturation
        cm.setSaturation(totalSat.coerceAtLeast(0f))

        // Contrast & Brightness
        val contrastScale = totalContrast
        val translate = (1f - contrastScale) * 0.5f * 255f + totalBrightness
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrastScale, 0f, 0f, 0f, translate,
                0f, contrastScale, 0f, 0f, translate,
                0f, 0f, contrastScale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        // Temperature (cool blue to warm orange)
        val temp = filter.warmTemp + adj.temperature
        if (temp != 0f) {
            val rShift = (temp * 30f).coerceIn(-50f, 50f)
            val bShift = (-temp * 30f).coerceIn(-50f, 50f)
            val tempMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, rShift,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(tempMatrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
    }

    private fun applyVideoEffect(canvas: Canvas, effect: VideoEffect, width: Int, height: Int, timeMs: Long) {
        when (effect) {
            VideoEffect.GLITCH -> {
                if ((timeMs / 100) % 4 == 0L) {
                    val glitchPaint = Paint().apply {
                        color = Color.argb(80, 56, 189, 248) // Sky blue glitch slice
                    }
                    val sliceY = ((timeMs * 3) % height).toFloat()
                    canvas.drawRect(0f, sliceY, width.toFloat(), sliceY + 30f, glitchPaint)
                }
            }
            VideoEffect.RGB_SPLIT -> {
                val shiftPaint = Paint().apply {
                    color = Color.argb(40, 255, 0, 100)
                }
                canvas.drawRect(10f, 0f, width.toFloat() + 10f, height.toFloat(), shiftPaint)
            }
            VideoEffect.CAMERA_SHAKE -> {
                val dx = (sin(timeMs * 0.05) * 8).toFloat()
                val dy = (cos(timeMs * 0.04) * 8).toFloat()
                canvas.translate(dx, dy)
            }
            VideoEffect.SPARKLES -> {
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
                for (i in 0..5) {
                    val px = ((timeMs * 47 + i * 180) % width).toFloat()
                    val py = ((timeMs * 31 + i * 220) % height).toFloat()
                    canvas.drawCircle(px, py, 4f, p)
                }
            }
            VideoEffect.FILM_GRAIN -> {
                val grainPaint = Paint().apply {
                    color = Color.argb(25, 255, 255, 255)
                }
                for (i in 0..20) {
                    val gx = (Math.random() * width).toFloat()
                    val gy = (Math.random() * height).toFloat()
                    canvas.drawRect(gx, gy, gx + 4f, gy + 4f, grainPaint)
                }
            }
            else -> {}
        }
    }

    private fun renderTextOverlays(canvas: Canvas, project: SkyCutProject, timelineMs: Long, width: Int, height: Int) {
        for (overlay in project.textOverlays) {
            val startMs = overlay.startTimelineMs
            val endMs = startMs + overlay.durationMs
            if (timelineMs in startMs..endMs) {
                val elapsed = timelineMs - startMs
                val text = overlay.text

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = overlay.colorHex.toInt()
                    textSize = overlay.fontSizeSp * (width / 360f)
                    textAlign = Paint.Align.CENTER
                    typeface = when (overlay.stylePreset) {
                        TextStylePreset.BOLD_OUTLINE -> Typeface.DEFAULT_BOLD
                        TextStylePreset.CINEMATIC_SERIF -> Typeface.SERIF
                        TextStylePreset.NEON_GLOW, TextStylePreset.TYPEWRITER -> Typeface.MONOSPACE
                        else -> Typeface.SANS_SERIF
                    }
                }

                val centerX = overlay.positionX * width
                var centerY = overlay.positionY * height

                // Text Animation
                when (overlay.animation) {
                    TextAnimation.FADE_IN -> {
                        val progress = (elapsed / 400f).coerceIn(0f, 1f)
                        textPaint.alpha = (progress * 255).toInt()
                    }
                    TextAnimation.SLIDE_UP -> {
                        val progress = (elapsed / 400f).coerceIn(0f, 1f)
                        centerY += (1f - progress) * 50f
                        textPaint.alpha = (progress * 255).toInt()
                    }
                    TextAnimation.BOUNCE -> {
                        val bounce = sin(elapsed * 0.015f) * 12f
                        centerY += bounce.toFloat()
                    }
                    else -> {}
                }

                // Subtitle background box
                if (overlay.stylePreset == TextStylePreset.SUBTITLE_BOX || overlay.isCaption) {
                    val bounds = Rect()
                    textPaint.getTextBounds(text, 0, text.length, bounds)
                    val bgPaint = Paint().apply {
                        color = Color.argb(160, 0, 0, 0)
                    }
                    val pad = 16f
                    val rect = RectF(
                        centerX - bounds.width() / 2f - pad,
                        centerY - bounds.height() - pad,
                        centerX + bounds.width() / 2f + pad,
                        centerY + pad
                    )
                    canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
                }

                // Outline if requested
                if (overlay.stylePreset.hasOutline) {
                    val strokePaint = Paint(textPaint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 6f
                        color = Color.BLACK
                    }
                    canvas.drawText(text, centerX, centerY, strokePaint)
                }

                canvas.drawText(text, centerX, centerY, textPaint)
            }
        }
    }

    private fun renderStickers(canvas: Canvas, project: SkyCutProject, timelineMs: Long, width: Int, height: Int) {
        for (sticker in project.stickers) {
            val startMs = sticker.startTimelineMs
            val endMs = startMs + sticker.durationMs
            if (timelineMs in startMs..endMs) {
                val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 54f * sticker.scale * (width / 360f)
                    textAlign = Paint.Align.CENTER
                }
                val sx = sticker.positionX * width
                val sy = sticker.positionY * height
                canvas.drawText(sticker.emojiOrIcon, sx, sy, emojiPaint)
            }
        }
    }

    private fun renderWatermark(canvas: Canvas, width: Int, height: Int) {
        val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 28f * (width / 720f).coerceAtLeast(1f)
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
        }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 15, 23, 42)
        }
        val text = "⚡ SkyCut"
        val margin = 32f
        val rect = RectF(width - 200f - margin, margin, width - margin, margin + 48f)
        canvas.drawRoundRect(rect, 24f, 24f, pillPaint)
        canvas.drawText(text, width - margin - 20f, margin + 35f, wmPaint)
    }

    private fun interpolateKeyframe(keyframes: List<Keyframe>, timeMs: Long): Keyframe {
        if (keyframes.isEmpty()) return Keyframe(timeMs)
        if (timeMs <= keyframes.first().timeMs) return keyframes.first()
        if (timeMs >= keyframes.last().timeMs) return keyframes.last()

        for (i in 0 until keyframes.size - 1) {
            val kf1 = keyframes[i]
            val kf2 = keyframes[i + 1]
            if (timeMs in kf1.timeMs..kf2.timeMs) {
                val span = (kf2.timeMs - kf1.timeMs).toFloat().coerceAtLeast(1f)
                val fraction = (timeMs - kf1.timeMs) / span
                return Keyframe(
                    timeMs = timeMs,
                    scale = kf1.scale + (kf2.scale - kf1.scale) * fraction,
                    translationX = kf1.translationX + (kf2.translationX - kf1.translationX) * fraction,
                    translationY = kf1.translationY + (kf2.translationY - kf1.translationY) * fraction,
                    rotation = kf1.rotation + (kf2.rotation - kf1.rotation) * fraction,
                    opacity = kf1.opacity + (kf2.opacity - kf1.opacity) * fraction
                )
            }
        }
        return keyframes.first()
    }
}
