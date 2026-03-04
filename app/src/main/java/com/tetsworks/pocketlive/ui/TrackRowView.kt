package com.tetsworks.pocketlive.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.tetsworks.pocketlive.databinding.ViewTrackRowBinding
import com.tetsworks.pocketlive.model.Track

class TrackRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewTrackRowBinding.inflate(LayoutInflater.from(context), this)
    private val stepButtons = mutableListOf<StepButton>()

    var onStepToggled: ((step: Int) -> Unit)? = null
    var onMuteToggled: (() -> Unit)? = null
    var onVolumeChanged: ((Float) -> Unit)? = null
    var onLongPress: (() -> Unit)? = null

    init {
        orientation = VERTICAL
    }

    fun bind(track: Track) {
        binding.trackName.text = track.name
        binding.trackName.setTextColor(track.color)
        binding.muteButton.isSelected = track.isMuted
        binding.volumeSlider.value = track.volume

        // Cria os step buttons se ainda não existem
        if (stepButtons.isEmpty()) {
            buildStepButtons(track)
        } else {
            updateStepButtons(track)
        }

        binding.muteButton.setOnClickListener { onMuteToggled?.invoke() }
        binding.volumeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onVolumeChanged?.invoke(value)
        }
        setOnLongClickListener { onLongPress?.invoke(); true }
    }

    private fun buildStepButtons(track: Track) {
        binding.stepsContainer.removeAllViews()
        stepButtons.clear()

        for (i in track.steps.indices) {
            val btn = StepButton(context).apply {
                isActive = track.steps[i]
                trackColor = track.color
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(3, 3, 3, 3)
                }
                setOnClickListener { onStepToggled?.invoke(i) }
            }
            stepButtons.add(btn)
            binding.stepsContainer.addView(btn)
        }
    }

    private fun updateStepButtons(track: Track) {
        track.steps.forEachIndexed { i, active ->
            stepButtons.getOrNull(i)?.apply {
                isActive = active
                trackColor = track.color
            }
        }
    }

    fun highlightStep(step: Int) {
        stepButtons.forEachIndexed { i, btn ->
            btn.isCurrent = (i == step)
        }
    }
}
