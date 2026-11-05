package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.skycut.data.ProjectJsonConverter
import com.example.skycut.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SkyCut", appName)
  }

  @Test
  fun `project serialization roundtrip preserves multi-track data`() {
    val project = SkyCutProject(
      id = UUID.randomUUID().toString(),
      title = "Cyberpunk Reel",
      aspectRatio = AspectRatio.RATIO_9_16,
      mainVideoClips = listOf(
        VideoClip(
          name = "Intro Scene",
          uri = "file://test.mp4",
          durationMs = 5000L,
          speed = 1.5f,
          filter = VideoFilter.CYBERPUNK,
          effect = VideoEffect.GLITCH,
          transitionIn = TransitionType.ZOOM_IN
        )
      ),
      audioClips = listOf(
        AudioClip(
          title = "Synth Beat",
          uri = "asset://synth.mp3",
          startTimelineMs = 0L,
          durationMs = 8000L,
          volume = 0.9f
        )
      ),
      textOverlays = listOf(
        TextOverlay(
          text = "SkyCut Pro 4K",
          startTimelineMs = 500L,
          durationMs = 3000L,
          stylePreset = TextStylePreset.BOLD_OUTLINE
        )
      )
    )

    val json = ProjectJsonConverter.toJson(project)
    val restored = ProjectJsonConverter.fromJson(json)

    assertEquals(project.id, restored.id)
    assertEquals(project.title, restored.title)
    assertEquals(project.aspectRatio, restored.aspectRatio)
    assertEquals(1, restored.mainVideoClips.size)
    assertEquals("Intro Scene", restored.mainVideoClips[0].name)
    assertEquals(1.5f, restored.mainVideoClips[0].speed, 0.01f)
    assertEquals(VideoFilter.CYBERPUNK, restored.mainVideoClips[0].filter)
    assertEquals(VideoEffect.GLITCH, restored.mainVideoClips[0].effect)
    assertEquals(TransitionType.ZOOM_IN, restored.mainVideoClips[0].transitionIn)
    assertEquals(1, restored.audioClips.size)
    assertEquals("Synth Beat", restored.audioClips[0].title)
    assertEquals(1, restored.textOverlays.size)
    assertEquals("SkyCut Pro 4K", restored.textOverlays[0].text)
  }

  @Test
  fun `clip effective duration accounts for speed and trim`() {
    val clip = VideoClip(
      name = "Speed Clip",
      uri = "file://clip.mp4",
      durationMs = 6000L,
      trimStartMs = 1000L,
      trimEndMs = 5000L,
      speed = 2.0f
    )
    // Trimmed span is 4000ms. At 2.0x speed, effective timeline duration is 2000ms
    assertEquals(2000L, clip.effectiveDurationMs)
  }

  @Test
  fun `aspect ratio calculation matches video standard`() {
    assertEquals(16f / 9f, AspectRatio.RATIO_16_9.ratio, 0.01f)
    assertEquals(9f / 16f, AspectRatio.RATIO_9_16.ratio, 0.01f)
    assertEquals(1.0f, AspectRatio.RATIO_1_1.ratio, 0.01f)
  }
}
