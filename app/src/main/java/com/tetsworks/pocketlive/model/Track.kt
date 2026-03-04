package com.tetsworks.pocketlive.model

import android.net.Uri
import com.tetsworks.pocketlive.audio.Synthesizer

data class Track(
    val id: Int,
    var name: String,
    var steps: BooleanArray = BooleanArray(16) { false },
    var volume: Float = 1.0f,
    var isMuted: Boolean = false,

    // Sintetizador embutido
    var instrumentType: Synthesizer.InstrumentType? = null,
    var pitch: Float = 1.0f,

    // Áudio customizado importado (Fase 2)
    var customAudioUri: Uri? = null,

    var color: Int = 0
) {
    val isCustom get() = customAudioUri != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Track) return false
        return id == other.id
    }
    override fun hashCode() = id
}
