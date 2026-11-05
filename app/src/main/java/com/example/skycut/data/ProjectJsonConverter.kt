package com.example.skycut.data

import com.example.skycut.model.*
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonConverter {

    fun toJson(project: SkyCutProject): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("title", project.title)
        root.put("createdAt", project.createdAt)
        root.put("updatedAt", project.updatedAt)
        root.put("aspectRatio", project.aspectRatio.name)
        root.put("includeWatermark", project.includeWatermark)

        // Main clips
        val mainClipsArray = JSONArray()
        project.mainVideoClips.forEach { clip ->
            mainClipsArray.put(clipToJson(clip))
        }
        root.put("mainVideoClips", mainClipsArray)

        // Overlay clips
        val overlayClipsArray = JSONArray()
        project.overlayClips.forEach { clip ->
            overlayClipsArray.put(clipToJson(clip))
        }
        root.put("overlayClips", overlayClipsArray)

        // Audio clips
        val audioArray = JSONArray()
        project.audioClips.forEach { audio ->
            val obj = JSONObject()
            obj.put("id", audio.id)
            obj.put("title", audio.title)
            obj.put("uri", audio.uri)
            obj.put("startTimelineMs", audio.startTimelineMs)
            obj.put("durationMs", audio.durationMs)
            obj.put("trimStartMs", audio.trimStartMs)
            obj.put("trimEndMs", audio.trimEndMs)
            obj.put("volume", audio.volume.toDouble())
            obj.put("fadeInMs", audio.fadeInMs)
            obj.put("fadeOutMs", audio.fadeOutMs)
            obj.put("isVoiceover", audio.isVoiceover)
            obj.put("isSoundEffect", audio.isSoundEffect)
            audioArray.put(obj)
        }
        root.put("audioClips", audioArray)

        // Text overlays
        val textArray = JSONArray()
        project.textOverlays.forEach { text ->
            val obj = JSONObject()
            obj.put("id", text.id)
            obj.put("text", text.text)
            obj.put("startTimelineMs", text.startTimelineMs)
            obj.put("durationMs", text.durationMs)
            obj.put("stylePreset", text.stylePreset.name)
            obj.put("animation", text.animation.name)
            obj.put("fontSizeSp", text.fontSizeSp.toDouble())
            obj.put("colorHex", text.colorHex)
            obj.put("positionX", text.positionX.toDouble())
            obj.put("positionY", text.positionY.toDouble())
            obj.put("isCaption", text.isCaption)
            textArray.put(obj)
        }
        root.put("textOverlays", textArray)

        // Stickers
        val stickersArray = JSONArray()
        project.stickers.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("emojiOrIcon", s.emojiOrIcon)
            obj.put("label", s.label)
            obj.put("startTimelineMs", s.startTimelineMs)
            obj.put("durationMs", s.durationMs)
            obj.put("positionX", s.positionX.toDouble())
            obj.put("positionY", s.positionY.toDouble())
            obj.put("scale", s.scale.toDouble())
            obj.put("rotation", s.rotation.toDouble())
            stickersArray.put(obj)
        }
        root.put("stickers", stickersArray)

        return root.toString()
    }

    private fun clipToJson(clip: VideoClip): JSONObject {
        val obj = JSONObject()
        obj.put("id", clip.id)
        obj.put("name", clip.name)
        obj.put("uri", clip.uri)
        obj.put("durationMs", clip.durationMs)
        obj.put("trimStartMs", clip.trimStartMs)
        obj.put("trimEndMs", clip.trimEndMs)
        obj.put("speed", clip.speed.toDouble())
        obj.put("volume", clip.volume.toDouble())
        obj.put("rotationDegrees", clip.rotationDegrees)
        obj.put("isFlippedHorizontally", clip.isFlippedHorizontally)
        obj.put("filter", clip.filter.name)
        obj.put("effect", clip.effect.name)
        obj.put("transitionIn", clip.transitionIn.name)
        obj.put("transitionDurationMs", clip.transitionDurationMs)
        obj.put("isMuted", clip.isMuted)
        obj.put("isFrozen", clip.isFrozen)
        obj.put("chromaKeyEnabled", clip.chromaKeyEnabled)
        obj.put("chromaKeyColor", clip.chromaKeyColor)
        obj.put("chromaKeyThreshold", clip.chromaKeyThreshold.toDouble())
        obj.put("isSampleMedia", clip.isSampleMedia)
        obj.put("backgroundColorHex", clip.backgroundColorHex)

        // Adjustments
        val adj = JSONObject()
        adj.put("brightness", clip.adjustments.brightness.toDouble())
        adj.put("contrast", clip.adjustments.contrast.toDouble())
        adj.put("saturation", clip.adjustments.saturation.toDouble())
        adj.put("temperature", clip.adjustments.temperature.toDouble())
        adj.put("vignette", clip.adjustments.vignette.toDouble())
        adj.put("exposure", clip.adjustments.exposure.toDouble())
        obj.put("adjustments", adj)

        // Keyframes
        val kfArray = JSONArray()
        clip.keyframes.forEach { kf ->
            val kfObj = JSONObject()
            kfObj.put("timeMs", kf.timeMs)
            kfObj.put("scale", kf.scale.toDouble())
            kfObj.put("translationX", kf.translationX.toDouble())
            kfObj.put("translationY", kf.translationY.toDouble())
            kfObj.put("rotation", kf.rotation.toDouble())
            kfObj.put("opacity", kf.opacity.toDouble())
            kfArray.put(kfObj)
        }
        obj.put("keyframes", kfArray)

        return obj
    }

    fun fromJson(jsonStr: String): SkyCutProject {
        val root = JSONObject(jsonStr)
        val id = root.optString("id")
        val title = root.optString("title", "Untitled Project")
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
        val aspectRatioName = root.optString("aspectRatio", AspectRatio.RATIO_9_16.name)
        val aspectRatio = try { AspectRatio.valueOf(aspectRatioName) } catch (e: Exception) { AspectRatio.RATIO_9_16 }
        val includeWatermark = root.optBoolean("includeWatermark", false)

        val mainClips = mutableListOf<VideoClip>()
        val mainArray = root.optJSONArray("mainVideoClips")
        if (mainArray != null) {
            for (i in 0 until mainArray.length()) {
                mainClips.add(jsonToClip(mainArray.getJSONObject(i)))
            }
        }

        val overlayClips = mutableListOf<VideoClip>()
        val overlayArray = root.optJSONArray("overlayClips")
        if (overlayArray != null) {
            for (i in 0 until overlayArray.length()) {
                overlayClips.add(jsonToClip(overlayArray.getJSONObject(i)))
            }
        }

        val audioClips = mutableListOf<AudioClip>()
        val audioArray = root.optJSONArray("audioClips")
        if (audioArray != null) {
            for (i in 0 until audioArray.length()) {
                val a = audioArray.getJSONObject(i)
                audioClips.add(
                    AudioClip(
                        id = a.optString("id"),
                        title = a.optString("title", "Sound"),
                        uri = a.optString("uri"),
                        startTimelineMs = a.optLong("startTimelineMs", 0L),
                        durationMs = a.optLong("durationMs", 5000L),
                        trimStartMs = a.optLong("trimStartMs", 0L),
                        trimEndMs = a.optLong("trimEndMs", a.optLong("durationMs", 5000L)),
                        volume = a.optDouble("volume", 1.0).toFloat(),
                        fadeInMs = a.optLong("fadeInMs", 0L),
                        fadeOutMs = a.optLong("fadeOutMs", 0L),
                        isVoiceover = a.optBoolean("isVoiceover", false),
                        isSoundEffect = a.optBoolean("isSoundEffect", false)
                    )
                )
            }
        }

        val textOverlays = mutableListOf<TextOverlay>()
        val textArray = root.optJSONArray("textOverlays")
        if (textArray != null) {
            for (i in 0 until textArray.length()) {
                val t = textArray.getJSONObject(i)
                val preset = try { TextStylePreset.valueOf(t.optString("stylePreset")) } catch (e: Exception) { TextStylePreset.MODERN }
                val anim = try { TextAnimation.valueOf(t.optString("animation")) } catch (e: Exception) { TextAnimation.POP_IN }
                textOverlays.add(
                    TextOverlay(
                        id = t.optString("id"),
                        text = t.optString("text"),
                        startTimelineMs = t.optLong("startTimelineMs", 0L),
                        durationMs = t.optLong("durationMs", 3000L),
                        stylePreset = preset,
                        animation = anim,
                        fontSizeSp = t.optDouble("fontSizeSp", 22.0).toFloat(),
                        colorHex = t.optLong("colorHex", 0xFFFFFFFF),
                        positionX = t.optDouble("positionX", 0.5).toFloat(),
                        positionY = t.optDouble("positionY", 0.8).toFloat(),
                        isCaption = t.optBoolean("isCaption", false)
                    )
                )
            }
        }

        val stickers = mutableListOf<StickerOverlay>()
        val stickersArray = root.optJSONArray("stickers")
        if (stickersArray != null) {
            for (i in 0 until stickersArray.length()) {
                val s = stickersArray.getJSONObject(i)
                stickers.add(
                    StickerOverlay(
                        id = s.optString("id"),
                        emojiOrIcon = s.optString("emojiOrIcon"),
                        label = s.optString("label"),
                        startTimelineMs = s.optLong("startTimelineMs", 0L),
                        durationMs = s.optLong("durationMs", 2500L),
                        positionX = s.optDouble("positionX", 0.5).toFloat(),
                        positionY = s.optDouble("positionY", 0.5).toFloat(),
                        scale = s.optDouble("scale", 1.0).toFloat(),
                        rotation = s.optDouble("rotation", 0.0).toFloat()
                    )
                )
            }
        }

        return SkyCutProject(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            aspectRatio = aspectRatio,
            mainVideoClips = mainClips,
            overlayClips = overlayClips,
            audioClips = audioClips,
            textOverlays = textOverlays,
            stickers = stickers,
            includeWatermark = includeWatermark
        )
    }

    private fun jsonToClip(c: JSONObject): VideoClip {
        val filter = try { VideoFilter.valueOf(c.optString("filter")) } catch (e: Exception) { VideoFilter.ORIGINAL }
        val effect = try { VideoEffect.valueOf(c.optString("effect")) } catch (e: Exception) { VideoEffect.NONE }
        val transition = try { TransitionType.valueOf(c.optString("transitionIn")) } catch (e: Exception) { TransitionType.NONE }

        val adjObj = c.optJSONObject("adjustments")
        val adjustments = if (adjObj != null) {
            ColorAdjustments(
                brightness = adjObj.optDouble("brightness", 0.0).toFloat(),
                contrast = adjObj.optDouble("contrast", 1.0).toFloat(),
                saturation = adjObj.optDouble("saturation", 1.0).toFloat(),
                temperature = adjObj.optDouble("temperature", 0.0).toFloat(),
                vignette = adjObj.optDouble("vignette", 0.0).toFloat(),
                exposure = adjObj.optDouble("exposure", 0.0).toFloat()
            )
        } else {
            ColorAdjustments()
        }

        val keyframes = mutableListOf<Keyframe>()
        val kfArray = c.optJSONArray("keyframes")
        if (kfArray != null) {
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                keyframes.add(
                    Keyframe(
                        timeMs = kf.optLong("timeMs"),
                        scale = kf.optDouble("scale", 1.0).toFloat(),
                        translationX = kf.optDouble("translationX", 0.0).toFloat(),
                        translationY = kf.optDouble("translationY", 0.0).toFloat(),
                        rotation = kf.optDouble("rotation", 0.0).toFloat(),
                        opacity = kf.optDouble("opacity", 1.0).toFloat()
                    )
                )
            }
        }

        return VideoClip(
            id = c.optString("id"),
            name = c.optString("name", "Clip"),
            uri = c.optString("uri"),
            durationMs = c.optLong("durationMs", 5000L),
            trimStartMs = c.optLong("trimStartMs", 0L),
            trimEndMs = c.optLong("trimEndMs", c.optLong("durationMs", 5000L)),
            speed = c.optDouble("speed", 1.0).toFloat(),
            volume = c.optDouble("volume", 1.0).toFloat(),
            rotationDegrees = c.optInt("rotationDegrees", 0),
            isFlippedHorizontally = c.optBoolean("isFlippedHorizontally", false),
            filter = filter,
            adjustments = adjustments,
            effect = effect,
            transitionIn = transition,
            transitionDurationMs = c.optLong("transitionDurationMs", 600L),
            isMuted = c.optBoolean("isMuted", false),
            isFrozen = c.optBoolean("isFrozen", false),
            chromaKeyEnabled = c.optBoolean("chromaKeyEnabled", false),
            chromaKeyColor = c.optLong("chromaKeyColor", 0xFF00FF00),
            chromaKeyThreshold = c.optDouble("chromaKeyThreshold", 0.35).toFloat(),
            keyframes = keyframes,
            isSampleMedia = c.optBoolean("isSampleMedia", false),
            backgroundColorHex = c.optLong("backgroundColorHex", 0xFF1E293B)
        )
    }
}
