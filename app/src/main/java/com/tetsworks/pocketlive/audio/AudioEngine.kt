package com.tetsworks.pocketlive.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.tetsworks.pocketlive.model.Track

class AudioEngine(private val context: Context) {

    // Cache de buffers sintetizados (evita regerar toda vez)
    private val synthCache = mutableMapOf<String, ShortArray>()

    // MediaPlayers para áudios customizados
    private val mediaPlayers = mutableMapOf<Int, MediaPlayer>()

    fun play(track: Track) {
        if (track.isMuted) return

        if (track.isCustom) {
            playCustom(track)
        } else {
            playSynth(track)
        }
    }

    private fun playSynth(track: Track) {
        val type = track.instrumentType ?: return
        val key  = "${type.name}_${track.pitch}"
        val buffer = synthCache.getOrPut(key) {
            Synthesizer.generateBuffer(type, track.pitch)
        }
        Synthesizer.playBuffer(buffer, track.volume)
    }

    private fun playCustom(track: Track) {
        mediaPlayers[track.id]?.let { mp ->
            if (mp.isPlaying) { mp.pause(); mp.seekTo(0) }
            mp.setVolume(track.volume, track.volume)
            mp.start()
        }
    }

    fun loadCustomAudio(trackId: Int, uri: Uri, volume: Float = 1f) {
        mediaPlayers[trackId]?.release()
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                setVolume(volume, volume)
            }
            mediaPlayers[trackId] = mp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateVolume(track: Track) {
        mediaPlayers[track.id]?.setVolume(track.volume, track.volume)
    }

    fun release() {
        mediaPlayers.values.forEach { it.release() }
        mediaPlayers.clear()
        synthCache.clear()
    }
}
