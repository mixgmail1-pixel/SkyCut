package com.example.skycut.data

import android.content.Context
import com.example.R
import com.example.skycut.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val database: SkyCutDatabase) {

    val allProjects: Flow<List<SkyCutProject>> =
        database.projectDao().getAllProjects().map { entities ->
            entities.map { entity ->
                try {
                    ProjectJsonConverter.fromJson(entity.projectJson)
                } catch (e: Exception) {
                    SkyCutProject(
                        id = entity.id,
                        title = entity.title,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
        }

    suspend fun getProject(id: String): SkyCutProject? {
        val entity = database.projectDao().getProjectById(id) ?: return null
        return try {
            ProjectJsonConverter.fromJson(entity.projectJson)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProject(project: SkyCutProject) {
        val json = ProjectJsonConverter.toJson(project)
        val entity = ProjectEntity(
            id = project.id,
            title = project.title,
            createdAt = project.createdAt,
            updatedAt = System.currentTimeMillis(),
            durationMs = project.totalDurationMs,
            aspectRatio = project.aspectRatio.name,
            projectJson = json
        )
        database.projectDao().insertOrUpdate(entity)
    }

    suspend fun deleteProject(id: String) {
        database.projectDao().deleteProjectById(id)
    }

    companion object {
        fun createSampleTravelProject(): SkyCutProject {
            return SkyCutProject(
                title = "Pacific Coast Sunset Vlog",
                aspectRatio = AspectRatio.RATIO_9_16,
                mainVideoClips = listOf(
                    VideoClip(
                        name = "Ocean Waves Cliffs",
                        uri = "sample_travel",
                        durationMs = 4500L,
                        filter = VideoFilter.SKY_VIVID,
                        thumbnailRes = R.drawable.template_travel,
                        isSampleMedia = true
                    ),
                    VideoClip(
                        name = "Golden Hour Highway",
                        uri = "sample_travel_2",
                        durationMs = 4000L,
                        filter = VideoFilter.WARM_GOLD,
                        transitionIn = TransitionType.CROSS_DISSOLVE,
                        thumbnailRes = R.drawable.template_travel,
                        isSampleMedia = true
                    ),
                    VideoClip(
                        name = "Night Cyber City",
                        uri = "sample_urban",
                        durationMs = 5000L,
                        filter = VideoFilter.CYBERPUNK,
                        transitionIn = TransitionType.ZOOM_IN,
                        thumbnailRes = R.drawable.template_urban,
                        isSampleMedia = true
                    )
                ),
                audioClips = listOf(
                    AudioClip(
                        title = "Sunset Chill BGM",
                        uri = "asset://audio/chill_sunset.mp3",
                        startTimelineMs = 0L,
                        durationMs = 12000L,
                        volume = 0.85f,
                        fadeInMs = 1000L,
                        fadeOutMs = 1500L
                    )
                ),
                textOverlays = listOf(
                    TextOverlay(
                        text = "GOLDEN ESCAPE ☀️",
                        startTimelineMs = 500L,
                        durationMs = 3500L,
                        stylePreset = TextStylePreset.BOLD_OUTLINE,
                        animation = TextAnimation.POP_IN,
                        positionY = 0.25f,
                        fontSizeSp = 28f
                    ),
                    TextOverlay(
                        text = "Shot on SkyCut 4K HDR",
                        startTimelineMs = 4600L,
                        durationMs = 3000L,
                        stylePreset = TextStylePreset.SUBTITLE_BOX,
                        animation = TextAnimation.FADE_IN,
                        positionY = 0.85f,
                        fontSizeSp = 18f
                    )
                ),
                stickers = listOf(
                    StickerOverlay(
                        emojiOrIcon = "✈️",
                        label = "Travel",
                        startTimelineMs = 800L,
                        durationMs = 2800L,
                        positionX = 0.82f,
                        positionY = 0.25f,
                        scale = 1.2f
                    )
                )
            )
        }

        fun createSampleCyberpunkProject(): SkyCutProject {
            return SkyCutProject(
                title = "Neo Tokyo Night Run",
                aspectRatio = AspectRatio.RATIO_9_16,
                mainVideoClips = listOf(
                    VideoClip(
                        name = "Tokyo Neon Street",
                        uri = "sample_urban",
                        durationMs = 4000L,
                        filter = VideoFilter.CYBERPUNK,
                        effect = VideoEffect.GLITCH,
                        thumbnailRes = R.drawable.template_urban,
                        isSampleMedia = true
                    ),
                    VideoClip(
                        name = "Rain Reflection Blur",
                        uri = "sample_urban_2",
                        durationMs = 4500L,
                        filter = VideoFilter.CINEMATIC,
                        effect = VideoEffect.RGB_SPLIT,
                        transitionIn = TransitionType.WHITE_FLASH,
                        thumbnailRes = R.drawable.template_urban,
                        isSampleMedia = true
                    )
                ),
                audioClips = listOf(
                    AudioClip(
                        title = "Synthwave Pulse",
                        uri = "asset://audio/synthwave.mp3",
                        startTimelineMs = 0L,
                        durationMs = 8500L,
                        volume = 0.95f
                    )
                ),
                textOverlays = listOf(
                    TextOverlay(
                        text = "NEO SPEED 2088",
                        startTimelineMs = 300L,
                        durationMs = 3000L,
                        stylePreset = TextStylePreset.NEON_GLOW,
                        animation = TextAnimation.BOUNCE,
                        positionY = 0.3f,
                        fontSizeSp = 26f
                    )
                )
            )
        }
    }
}
