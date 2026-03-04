package com.tetsworks.pocketlive.audio;

import android.os.Handler;
import android.os.Looper;
import com.tetsworks.pocketlive.model.Track;
import java.util.ArrayList;
import java.util.List;

public class Sequencer {

    public interface OnStepChangedListener {
        void onStepChanged(int step);
    }

    private final AudioEngine audioEngine;
    private List<Track> tracks = new ArrayList<>();
    private int bpm = 120;
    private int stepCount = 16;
    private int currentStep = -1;
    private boolean playing = false;
    private long stepIntervalMs = 60_000L / 120 / 2;
    private OnStepChangedListener listener;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!playing) return;
            currentStep = (currentStep + 1) % stepCount;
            if (listener != null) listener.onStepChanged(currentStep);
            for (Track t : tracks) {
                if (t.getSteps()[currentStep]) audioEngine.play(t);
            }
            handler.postDelayed(this, stepIntervalMs);
        }
    };

    public Sequencer(AudioEngine audioEngine) {
        this.audioEngine = audioEngine;
    }

    public void setTracks(List<Track> tracks) { this.tracks = tracks; }

    public void setBpm(int bpm) {
        this.bpm = Math.max(40, Math.min(300, bpm));
        this.stepIntervalMs = 60_000L / this.bpm / 2;
    }

    public int getBpm() { return bpm; }
    public int getCurrentStep() { return currentStep; }
    public boolean isPlaying() { return playing; }
    public void setOnStepChangedListener(OnStepChangedListener l) { this.listener = l; }

    public void play() {
        if (playing) return;
        playing = true;
        handler.post(tickRunnable);
    }

    public void pause() {
        playing = false;
        handler.removeCallbacks(tickRunnable);
    }

    public void stop() {
        playing = false;
        handler.removeCallbacks(tickRunnable);
        currentStep = -1;
        if (listener != null) listener.onStepChanged(-1);
    }
}
