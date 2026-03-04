package com.tetsworks.pocketlive.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import com.tetsworks.pocketlive.model.Track;
import java.util.HashMap;
import java.util.Map;

public class AudioEngine {

    private final Context context;
    private final Map<String, short[]> synthCache = new HashMap<>();
    private final Map<Integer, MediaPlayer> mediaPlayers = new HashMap<>();

    public AudioEngine(Context context) {
        this.context = context;
    }

    public void play(Track track) {
        if (track.isMuted()) return;
        if (track.isCustom()) {
            playCustom(track);
        } else {
            playSynth(track);
        }
    }

    private void playSynth(Track track) {
        String key = track.getInstrumentType() + "_" + track.getPitch();
        short[] buffer = synthCache.get(key);
        if (buffer == null) {
            buffer = Synthesizer.generateBuffer(track.getInstrumentType(), track.getPitch());
            synthCache.put(key, buffer);
        }
        Synthesizer.playBuffer(buffer, track.getVolume());
    }

    private void playCustom(Track track) {
        MediaPlayer mp = mediaPlayers.get(track.getId());
        if (mp != null) {
            if (mp.isPlaying()) { mp.pause(); mp.seekTo(0); }
            mp.setVolume(track.getVolume(), track.getVolume());
            mp.start();
        }
    }

    public void loadCustomAudio(int trackId, Uri uri, float volume) {
        MediaPlayer old = mediaPlayers.get(trackId);
        if (old != null) old.release();
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
            mp.setDataSource(context, uri);
            mp.prepare();
            mp.setVolume(volume, volume);
            mediaPlayers.put(trackId, mp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateVolume(Track track) {
        MediaPlayer mp = mediaPlayers.get(track.getId());
        if (mp != null) mp.setVolume(track.getVolume(), track.getVolume());
    }

    public void release() {
        for (MediaPlayer mp : mediaPlayers.values()) mp.release();
        mediaPlayers.clear();
        synthCache.clear();
    }
}
