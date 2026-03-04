package com.tetsworks.pocketlive.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.google.android.material.slider.Slider;
import com.tetsworks.pocketlive.R;
import com.tetsworks.pocketlive.databinding.ViewTrackRowBinding;
import com.tetsworks.pocketlive.model.Track;
import java.util.ArrayList;
import java.util.List;

public class TrackRowView extends LinearLayout {

    public interface OnStepToggledListener   { void onStepToggled(int step); }
    public interface OnMuteToggledListener   { void onMuteToggled(); }
    public interface OnVolumeChangedListener { void onVolumeChanged(float volume); }
    public interface OnLongPressListener     { void onLongPress(); }

    private final ViewTrackRowBinding binding;
    private final List<StepButton> stepButtons = new ArrayList<>();

    private OnStepToggledListener   onStepToggled;
    private OnMuteToggledListener   onMuteToggled;
    private OnVolumeChangedListener onVolumeChanged;
    private OnLongPressListener     onLongPress;

    public TrackRowView(Context context) {
        super(context);
        binding = ViewTrackRowBinding.inflate(LayoutInflater.from(context), this, true);
        setOrientation(VERTICAL);
    }

    public TrackRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = ViewTrackRowBinding.inflate(LayoutInflater.from(context), this, true);
        setOrientation(VERTICAL);
    }

    public void setOnStepToggledListener(OnStepToggledListener l)     { onStepToggled   = l; }
    public void setOnMuteToggledListener(OnMuteToggledListener l)     { onMuteToggled   = l; }
    public void setOnVolumeChangedListener(OnVolumeChangedListener l) { onVolumeChanged = l; }
    public void setOnLongPressListener(OnLongPressListener l)         { onLongPress     = l; }

    public void bind(Track track) {
        binding.trackName.setText(track.getName());
        binding.trackName.setTextColor(track.getColor());
        binding.muteButton.setSelected(track.isMuted());
        binding.volumeSlider.setValue(track.getVolume());

        if (stepButtons.isEmpty()) {
            buildStepButtons(track);
        } else {
            updateStepButtons(track);
        }

        binding.muteButton.setOnClickListener(v -> { if (onMuteToggled != null) onMuteToggled.onMuteToggled(); });
        binding.volumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && onVolumeChanged != null) onVolumeChanged.onVolumeChanged(value);
        });
        setOnLongClickListener(v -> {
            if (onLongPress != null) onLongPress.onLongPress();
            return true;
        });
    }

    private void buildStepButtons(Track track) {
        binding.stepsContainer.removeAllViews();
        stepButtons.clear();
        boolean[] steps = track.getSteps();
        for (int i = 0; i < steps.length; i++) {
            final int idx = i;
            StepButton btn = new StepButton(getContext());
            btn.setActive(steps[i]);
            btn.setTrackColor(track.getColor());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
            lp.setMargins(3, 3, 3, 3);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> { if (onStepToggled != null) onStepToggled.onStepToggled(idx); });
            stepButtons.add(btn);
            binding.stepsContainer.addView(btn);
        }
    }

    private void updateStepButtons(Track track) {
        boolean[] steps = track.getSteps();
        for (int i = 0; i < stepButtons.size() && i < steps.length; i++) {
            stepButtons.get(i).setActive(steps[i]);
            stepButtons.get(i).setTrackColor(track.getColor());
        }
    }

    public void highlightStep(int step) {
        for (int i = 0; i < stepButtons.size(); i++) {
            stepButtons.get(i).setCurrent(i == step);
        }
    }
}
