package com.tetsworks.pocketlive.audio

import android.os.Handler
import android.os.Looper
import com.tetsworks.pocketlive.model.Track

class Sequencer(private val audioEngine: AudioEngine) {

    var bpm: Int = 120
        set(value) {
            field = value.coerceIn(40, 300)
            stepIntervalMs = (60_000L / field / 2)
        }

    var stepCount: Int = 16
    var currentStep: Int = -1
        private set

    var isPlaying: Boolean = false
        private set

    var onStepChanged: ((step: Int) -> Unit)? = null

    private var stepIntervalMs: Long = (60_000L / 120 / 2)
    private val handler = Handler(Looper.getMainLooper())
    private var tracks: List<Track> = emptyList()

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isPlaying) return
            currentStep = (currentStep + 1) % stepCount
            onStepChanged?.invoke(currentStep)
            tracks.forEach { track ->
                if (track.steps[currentStep]) {
                    audioEngine.play(track)
                }
            }
            handler.postDelayed(this, stepIntervalMs)
        }
    }

    fun setTracks(tracks: List<Track>) {
        this.tracks = tracks
    }

    fun play() {
        if (isPlaying) return
        isPlaying = true
        handler.post(tickRunnable)
    }

    fun pause() {
        isPlaying = false
        handler.removeCallbacks(tickRunnable)
    }

    fun stop() {
        isPlaying = false
        handler.removeCallbacks(tickRunnable)
        currentStep = -1
        onStepChanged?.invoke(-1)
    }
}
