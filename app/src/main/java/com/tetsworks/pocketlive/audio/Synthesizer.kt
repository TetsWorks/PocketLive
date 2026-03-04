package com.tetsworks.pocketlive.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.*

/**
 * Sintetizador de sons por geração matemática (sem arquivos de áudio).
 * Cada tipo de instrumento é gerado via ondas senoidais, quadradas, ruído, etc.
 */
object Synthesizer {

    private val sampleRate = 44100

    enum class InstrumentType(val displayName: String, val emoji: String) {
        KICK("Kick", "🥁"),
        SNARE("Snare", "🪘"),
        HIHAT_CLOSED("Hi-Hat", "🎵"),
        CLAP("Clap", "👏"),
        BASS("Bass 808", "🔈"),
        LEAD("Lead Synth", "🎹"),
        PAD("Pad", "🌊"),
        PERC("Perc", "🪃")
    }

    /** Gera o buffer PCM para um instrumento específico */
    fun generateBuffer(type: InstrumentType, pitch: Float = 1.0f): ShortArray {
        return when (type) {
            InstrumentType.KICK        -> generateKick(pitch)
            InstrumentType.SNARE       -> generateSnare()
            InstrumentType.HIHAT_CLOSED-> generateHihat()
            InstrumentType.CLAP        -> generateClap()
            InstrumentType.BASS        -> generateBass(pitch)
            InstrumentType.LEAD        -> generateLead(pitch)
            InstrumentType.PAD         -> generatePad(pitch)
            InstrumentType.PERC        -> generatePerc(pitch)
        }
    }

    // --- Kick: seno com pitch decay (frequência cai rapidamente) ---
    private fun generateKick(pitch: Float): ShortArray {
        val duration = 0.5 // segundos
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val startFreq = 150.0 * pitch
        val endFreq   = 40.0
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = startFreq * exp(-t * 18.0) + endFreq
            val env  = exp(-t * 8.0)
            val sample = sin(2 * PI * freq * t) * env
            buf[i] = (sample * Short.MAX_VALUE * 0.95).toInt().toShort()
        }
        return buf
    }

    // --- Snare: seno + ruído branco ---
    private fun generateSnare(): ShortArray {
        val duration = 0.25
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val rand = java.util.Random(42)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val env   = exp(-t * 20.0)
            val tone  = sin(2 * PI * 200.0 * t) * 0.4
            val noise = (rand.nextDouble() * 2 - 1) * 0.6
            buf[i] = ((tone + noise) * env * Short.MAX_VALUE * 0.85).toInt().toShort()
        }
        return buf
    }

    // --- Hi-Hat: ruído branco filtrado com decay curto ---
    private fun generateHihat(): ShortArray {
        val duration = 0.1
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val rand = java.util.Random(7)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 60.0)
            val noise = (rand.nextDouble() * 2 - 1)
            buf[i] = (noise * env * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return buf
    }

    // --- Clap: burst de ruído em camadas ---
    private fun generateClap(): ShortArray {
        val duration = 0.2
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val rand = java.util.Random(13)
        // 3 bursts de ruído sobrepostos
        val bursts = listOf(0.0, 0.01, 0.02)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            var sample = 0.0
            for (offset in bursts) {
                val dt = t - offset
                if (dt >= 0) {
                    val env = exp(-dt * 40.0)
                    sample += (rand.nextDouble() * 2 - 1) * env
                }
            }
            buf[i] = (sample / bursts.size * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return buf
    }

    // --- Bass 808: seno com pitch slide e sustain longo ---
    private fun generateBass(pitch: Float): ShortArray {
        val duration = 0.8
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val startFreq = 80.0 * pitch
        val endFreq   = 55.0 * pitch
        var phase = 0.0
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = startFreq + (endFreq - startFreq) * (1 - exp(-t * 5.0))
            val env  = exp(-t * 3.0)
            phase += 2 * PI * freq / sampleRate
            buf[i] = (sin(phase) * env * Short.MAX_VALUE * 0.9).toInt().toShort()
        }
        return buf
    }

    // --- Lead: onda quadrada com envelope ADSR simples ---
    private fun generateLead(pitch: Float): ShortArray {
        val duration = 0.3
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val freq = 440.0 * pitch
        val attack  = (sampleRate * 0.01).toInt()
        val release = (sampleRate * 0.1).toInt()
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            // Onda quadrada
            val square = if (sin(2 * PI * freq * t) >= 0) 1.0 else -1.0
            // Envelope
            val env = when {
                i < attack                       -> i.toDouble() / attack
                i > samples - release            -> (samples - i).toDouble() / release
                else                             -> 1.0
            }
            buf[i] = (square * env * Short.MAX_VALUE * 0.5).toInt().toShort()
        }
        return buf
    }

    // --- Pad: múltiplos senos detuned para som largo ---
    private fun generatePad(pitch: Float): ShortArray {
        val duration = 1.0
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val baseFreq = 220.0 * pitch
        val detunes  = listOf(1.0, 1.004, 0.996, 1.008)
        val attack   = (sampleRate * 0.2).toInt()
        val release  = (sampleRate * 0.3).toInt()
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val env = when {
                i < attack            -> i.toDouble() / attack
                i > samples - release -> (samples - i).toDouble() / release
                else                  -> 1.0
            }
            var sample = 0.0
            for (d in detunes) sample += sin(2 * PI * baseFreq * d * t)
            buf[i] = (sample / detunes.size * env * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return buf
    }

    // --- Perc: seno curto com pitch alto ---
    private fun generatePerc(pitch: Float): ShortArray {
        val duration = 0.15
        val samples = (sampleRate * duration).toInt()
        val buf = ShortArray(samples)
        val freq = 600.0 * pitch
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 30.0)
            buf[i] = (sin(2 * PI * freq * t) * env * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return buf
    }

    /** Toca um buffer PCM direto no AudioTrack (non-blocking) */
    fun playBuffer(buffer: ShortArray, volume: Float = 1.0f) {
        Thread {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(44100)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.setVolume(volume)
            track.play()

            // Libera após tocar
            val durationMs = (buffer.size.toLong() * 1000L / 44100L) + 100L
            Thread.sleep(durationMs)
            track.release()
        }.start()
    }
}
