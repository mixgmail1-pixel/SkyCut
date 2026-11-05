package com.example.skycut.engine

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private var activeTrack: AudioTrack? = null
    private var isRecording = false
    private var recordingFile: File? = null

    // Generates sound effect synthesis for built-in effects (Whoosh, Pop, Boom, Bell, Click)
    fun playSoundEffect(effectName: String) {
        val sampleRate = 44100
        val durationSeconds = when (effectName) {
            "Cinematic Boom" -> 1.2
            "Whoosh" -> 0.45
            "Bell" -> 0.8
            "Camera Click" -> 0.15
            else -> 0.3 // Pop
        }
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        when (effectName) {
            "Cinematic Boom" -> {
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 120.0 * (1.0 - t / durationSeconds) + 40.0
                    val envelope = (1.0 - t / durationSeconds)
                    buffer[i] = (sin(2.0 * Math.PI * freq * t) * 30000 * envelope).toInt().toShort()
                }
            }
            "Whoosh" -> {
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 200.0 + 800.0 * sin(Math.PI * t / durationSeconds)
                    val envelope = sin(Math.PI * t / durationSeconds)
                    buffer[i] = (sin(2.0 * Math.PI * freq * t) * 22000 * envelope).toInt().toShort()
                }
            }
            "Bell" -> {
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq1 = 880.0
                    val freq2 = 1760.0
                    val envelope = Math.exp(-4.0 * t)
                    val s = (sin(2.0 * Math.PI * freq1 * t) * 0.7 + sin(2.0 * Math.PI * freq2 * t) * 0.3)
                    buffer[i] = (s * 28000 * envelope).toInt().toShort()
                }
            }
            "Camera Click" -> {
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    buffer[i] = ((Math.random() * 2 - 1) * 25000 * Math.exp(-25.0 * t)).toInt().toShort()
                }
            }
            else -> { // Pop
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 600.0 + 400.0 * (1.0 - t / durationSeconds)
                    val envelope = Math.exp(-12.0 * t)
                    buffer[i] = (sin(2.0 * Math.PI * freq * t) * 26000 * envelope).toInt().toShort()
                }
            }
        }

        try {
            activeTrack?.stop()
            activeTrack?.release()
            activeTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                numSamples * 2,
                AudioTrack.MODE_STATIC
            )
            activeTrack?.write(buffer, 0, numSamples)
            activeTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Voiceover recorder
    suspend fun startVoiceoverRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val audioDir = File(context.cacheDir, "voiceovers")
            if (!audioDir.exists()) audioDir.mkdirs()
            val file = File(audioDir, "vo_${System.currentTimeMillis()}.pcm")
            recordingFile = file
            isRecording = true

            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(4096)

            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            record.startRecording()
            val fos = FileOutputStream(file)
            val audioBuffer = ByteArray(bufferSize)

            Thread {
                try {
                    while (isRecording) {
                        val read = record.read(audioBuffer, 0, bufferSize)
                        if (read > 0) {
                            fos.write(audioBuffer, 0, read)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        record.stop()
                        record.release()
                        fos.close()
                    } catch (e: Exception) { /* ignored */ }
                }
            }.start()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopVoiceoverRecording(): File? {
        isRecording = false
        return recordingFile
    }

    fun release() {
        try {
            activeTrack?.stop()
            activeTrack?.release()
            activeTrack = null
        } catch (e: Exception) { /* ignored */ }
    }
}
