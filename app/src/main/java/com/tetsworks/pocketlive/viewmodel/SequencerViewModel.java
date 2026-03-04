package com.tetsworks.pocketlive.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.tetsworks.pocketlive.audio.AudioEngine;
import com.tetsworks.pocketlive.audio.Sequencer;
import com.tetsworks.pocketlive.audio.Synthesizer;
import com.tetsworks.pocketlive.model.Track;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SequencerViewModel extends AndroidViewModel {

    public final AudioEngine audioEngine;
    public final Sequencer   sequencer;

    public final MutableLiveData<List<Track>> tracks      = new MutableLiveData<>();
    public final MutableLiveData<Integer>     currentStep = new MutableLiveData<>(-1);
    public final MutableLiveData<Boolean>     isPlaying   = new MutableLiveData<>(false);
    public final MutableLiveData<Integer>     bpm         = new MutableLiveData<>(120);

    private static final int[] COLORS = {
        0xFFE53935, 0xFF8E24AA, 0xFF1E88E5, 0xFF00ACC1,
        0xFF43A047, 0xFFFB8C00, 0xFFFFB300, 0xFF6D4C41
    };

    private static final int[] DEFAULT_INSTRUMENTS = {
        Synthesizer.KICK,
        Synthesizer.SNARE,
        Synthesizer.HIHAT,
        Synthesizer.CLAP
    };

    public SequencerViewModel(@NonNull Application application) {
        super(application);
        audioEngine = new AudioEngine(application);
        sequencer   = new Sequencer(audioEngine);

        List<Track> initial = new ArrayList<>();
        for (int i = 0; i < DEFAULT_INSTRUMENTS.length; i++) {
            int type = DEFAULT_INSTRUMENTS[i];
            String name = Synthesizer.EMOJIS[type] + " " + Synthesizer.NAMES[type];
            initial.add(new Track(i, name, type, COLORS[i]));
        }
        tracks.setValue(initial);
        sequencer.setTracks(initial);
        sequencer.setOnStepChangedListener(step -> currentStep.setValue(step));
    }

    private int nextId() {
        List<Track> list = tracks.getValue();
        if (list == null || list.isEmpty()) return 0;
        int max = 0;
        for (Track t : list) if (t.getId() > max) max = t.getId();
        return max + 1;
    }

    private int colorFor(int id) { return COLORS[id % COLORS.length]; }

    public void toggleStep(int trackId, int step) {
        List<Track> list = tracks.getValue();
        if (list == null) return;
        for (Track t : list) {
            if (t.getId() == trackId) {
                t.getSteps()[step] = !t.getSteps()[step];
                break;
            }
        }
        tracks.setValue(list);
    }

    public void toggleMute(int trackId) {
        List<Track> list = tracks.getValue();
        if (list == null) return;
        for (Track t : list) {
            if (t.getId() == trackId) { t.setMuted(!t.isMuted()); break; }
        }
        tracks.setValue(list);
    }

    public void setVolume(int trackId, float volume) {
        List<Track> list = tracks.getValue();
        if (list == null) return;
        for (Track t : list) {
            if (t.getId() == trackId) {
                t.setVolume(volume);
                audioEngine.updateVolume(t);
                break;
            }
        }
    }

    public void setBpm(int value) {
        sequencer.setBpm(value);
        bpm.setValue(sequencer.getBpm());
    }

    public void play()  { sequencer.play();  isPlaying.setValue(true);  }
    public void pause() { sequencer.pause(); isPlaying.setValue(false); }
    public void stop()  { sequencer.stop();  isPlaying.setValue(false); }

    public void addSynthTrack(int instrumentType) {
        List<Track> list = new ArrayList<>(tracks.getValue() != null ? tracks.getValue() : new ArrayList<>());
        int id = nextId();
        String name = Synthesizer.EMOJIS[instrumentType] + " " + Synthesizer.NAMES[instrumentType];
        list.add(new Track(id, name, instrumentType, colorFor(id)));
        tracks.setValue(list);
        sequencer.setTracks(list);
    }

    public void addCustomTrack(String name, Uri uri) {
        List<Track> list = new ArrayList<>(tracks.getValue() != null ? tracks.getValue() : new ArrayList<>());
        int id = nextId();
        Track track = new Track(id, "🎵 " + name, uri, colorFor(id));
        audioEngine.loadCustomAudio(id, uri, 1.0f);
        list.add(track);
        tracks.setValue(list);
        sequencer.setTracks(list);
    }

    public void removeTrack(int trackId) {
        List<Track> list = new ArrayList<>(tracks.getValue() != null ? tracks.getValue() : new ArrayList<>());
        list.removeIf(t -> t.getId() == trackId);
        tracks.setValue(list);
        sequencer.setTracks(list);
    }

    public void clearTrack(int trackId) {
        List<Track> list = tracks.getValue();
        if (list == null) return;
        for (Track t : list) {
            if (t.getId() == trackId) { Arrays.fill(t.getSteps(), false); break; }
        }
        tracks.setValue(list);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        sequencer.stop();
        audioEngine.release();
    }
}
