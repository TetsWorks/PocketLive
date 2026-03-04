package com.tetsworks.pocketlive.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tetsworks.pocketlive.audio.AudioEngine
import com.tetsworks.pocketlive.audio.Sequencer
import com.tetsworks.pocketlive.audio.Synthesizer
import com.tetsworks.pocketlive.model.Track

class SequencerViewModel(app: Application) : AndroidViewModel(app) {

    val audioEngine = AudioEngine(app)
    val sequencer   = Sequencer(audioEngine)

    val tracks      = MutableLiveData<MutableList<Track>>()
    val currentStep = MutableLiveData<Int>(-1)
    val isPlaying   = MutableLiveData<Boolean>(false)
    val bpm         = MutableLiveData<Int>(120)

    private val trackColors = listOf(
        0xFFE53935.toInt(), 0xFF8E24AA.toInt(), 0xFF1E88E5.toInt(),
        0xFF00ACC1.toInt(), 0xFF43A047.toInt(), 0xFFFB8C00.toInt(),
        0xFFFFB300.toInt(), 0xFF6D4C41.toInt()
    )

    // Instrumentos padrão do sintetizador
    private val defaultInstruments = listOf(
        Synthesizer.InstrumentType.KICK,
        Synthesizer.InstrumentType.SNARE,
        Synthesizer.InstrumentType.HIHAT_CLOSED,
        Synthesizer.InstrumentType.CLAP,
    )

    init {
        val initialTracks = defaultInstruments.mapIndexed { i, type ->
            Track(
                id = i,
                name = "${type.emoji} ${type.displayName}",
                instrumentType = type,
                color = trackColors[i % trackColors.size]
            )
        }.toMutableList()

        tracks.value = initialTracks
        sequencer.setTracks(initialTracks)
        sequencer.onStepChanged = { step -> currentStep.value = step }
    }

    fun toggleStep(trackId: Int, step: Int) {
        tracks.value?.let { list ->
            list.find { it.id == trackId }?.steps?.let { steps ->
                steps[step] = !steps[step]
                tracks.value = list
            }
        }
    }

    fun toggleMute(trackId: Int) {
        tracks.value?.let { list ->
            list.find { it.id == trackId }?.let { it.isMuted = !it.isMuted }
            tracks.value = list
        }
    }

    fun setVolume(trackId: Int, volume: Float) {
        tracks.value?.let { list ->
            list.find { it.id == trackId }?.let {
                it.volume = volume
                audioEngine.updateVolume(it)
            }
        }
    }

    fun setPitch(trackId: Int, pitch: Float) {
        tracks.value?.let { list ->
            list.find { it.id == trackId }?.pitch = pitch
            tracks.value = list
        }
    }

    fun setBpm(value: Int) {
        sequencer.bpm = value
        bpm.value = sequencer.bpm
    }

    fun play()  { sequencer.play();  isPlaying.value = true  }
    fun pause() { sequencer.pause(); isPlaying.value = false }
    fun stop()  { sequencer.stop();  isPlaying.value = false }

    /** Adiciona instrumento sintetizado */
    fun addSynthTrack(type: Synthesizer.InstrumentType) {
        val list  = tracks.value ?: mutableListOf()
        val newId = (list.maxOfOrNull { it.id } ?: -1) + 1
        val track = Track(
            id = newId,
            name = "${type.emoji} ${type.displayName}",
            instrumentType = type,
            color = trackColors[newId % trackColors.size]
        )
        list.add(track)
        tracks.value = list
        sequencer.setTracks(list)
    }

    /** Adiciona áudio customizado importado (Fase 2) */
    fun addCustomTrack(name: String, uri: Uri) {
        val list  = tracks.value ?: mutableListOf()
        val newId = (list.maxOfOrNull { it.id } ?: -1) + 1
        val track = Track(
            id = newId,
            name = "🎵 $name",
            customAudioUri = uri,
            color = trackColors[newId % trackColors.size]
        )
        audioEngine.loadCustomAudio(newId, uri)
        list.add(track)
        tracks.value = list
        sequencer.setTracks(list)
    }

    fun removeTrack(trackId: Int) {
        val list = tracks.value ?: return
        list.removeAll { it.id == trackId }
        tracks.value = list
        sequencer.setTracks(list)
    }

    fun clearTrack(trackId: Int) {
        tracks.value?.let { list ->
            list.find { it.id == trackId }?.steps = BooleanArray(16) { false }
            tracks.value = list
        }
    }

    override fun onCleared() {
        super.onCleared()
        sequencer.stop()
        audioEngine.release()
    }
}
