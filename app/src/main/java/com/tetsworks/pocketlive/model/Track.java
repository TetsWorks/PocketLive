package com.tetsworks.pocketlive.model;

import android.net.Uri;
import com.tetsworks.pocketlive.audio.Synthesizer;

public class Track {

    private int id;
    private String name;
    private boolean[] steps;
    private float volume;
    private boolean muted;
    private int instrumentType; // constantes de Synthesizer (KICK, SNARE, etc.)
    private float pitch;
    private Uri customAudioUri;
    private int color;

    /** Construtor para instrumento sintetizado */
    public Track(int id, String name, int instrumentType, int color) {
        this.id             = id;
        this.name           = name;
        this.steps          = new boolean[16];
        this.volume         = 1.0f;
        this.muted          = false;
        this.instrumentType = instrumentType;
        this.pitch          = 1.0f;
        this.customAudioUri = null;
        this.color          = color;
    }

    /** Construtor para áudio customizado */
    public Track(int id, String name, Uri customAudioUri, int color) {
        this.id             = id;
        this.name           = name;
        this.steps          = new boolean[16];
        this.volume         = 1.0f;
        this.muted          = false;
        this.instrumentType = -1;
        this.pitch          = 1.0f;
        this.customAudioUri = customAudioUri;
        this.color          = color;
    }

    public boolean isCustom() { return customAudioUri != null; }

    public int     getId()             { return id; }
    public String  getName()           { return name; }
    public void    setName(String n)   { name = n; }
    public boolean[] getSteps()        { return steps; }
    public float   getVolume()         { return volume; }
    public void    setVolume(float v)  { volume = v; }
    public boolean isMuted()           { return muted; }
    public void    setMuted(boolean m) { muted = m; }
    public int     getInstrumentType() { return instrumentType; }
    public float   getPitch()          { return pitch; }
    public void    setPitch(float p)   { pitch = p; }
    public Uri     getCustomAudioUri() { return customAudioUri; }
    public int     getColor()          { return color; }
}
