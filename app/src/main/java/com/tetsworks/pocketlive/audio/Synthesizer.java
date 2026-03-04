package com.tetsworks.pocketlive.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class Synthesizer {

    public static final int KICK         = 0;
    public static final int SNARE        = 1;
    public static final int HIHAT        = 2;
    public static final int CLAP         = 3;
    public static final int BASS         = 4;
    public static final int LEAD         = 5;
    public static final int PAD          = 6;
    public static final int PERC         = 7;

    public static final String[] NAMES   = {"Kick","Snare","Hi-Hat","Clap","Bass 808","Lead","Pad","Perc"};
    public static final String[] EMOJIS  = {"🥁","🪘","🎵","👏","🔈","🎹","🌊","🪃"};
    public static final int      COUNT   = 8;

    private static final int SAMPLE_RATE = 44100;

    public static short[] generateBuffer(int type, float pitch) {
        switch (type) {
            case KICK:  return generateKick(pitch);
            case SNARE: return generateSnare();
            case HIHAT: return generateHihat();
            case CLAP:  return generateClap();
            case BASS:  return generateBass(pitch);
            case LEAD:  return generateLead(pitch);
            case PAD:   return generatePad(pitch);
            case PERC:  return generatePerc(pitch);
            default:    return new short[0];
        }
    }

    // Kick: seno com pitch decay
    private static short[] generateKick(float pitch) {
        int samples = (int)(SAMPLE_RATE * 0.5);
        short[] buf = new short[samples];
        double startFreq = 150.0 * pitch;
        double endFreq   = 40.0;
        for (int i = 0; i < samples; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double freq = startFreq * Math.exp(-t * 18.0) + endFreq;
            double env  = Math.exp(-t * 8.0);
            double val  = Math.sin(2 * Math.PI * freq * t) * env;
            buf[i] = (short)(val * Short.MAX_VALUE * 0.95);
        }
        return buf;
    }

    // Snare: seno + ruído branco
    private static short[] generateSnare() {
        int samples = (int)(SAMPLE_RATE * 0.25);
        short[] buf = new short[samples];
        java.util.Random rand = new java.util.Random(42);
        for (int i = 0; i < samples; i++) {
            double t     = (double) i / SAMPLE_RATE;
            double env   = Math.exp(-t * 20.0);
            double tone  = Math.sin(2 * Math.PI * 200.0 * t) * 0.4;
            double noise = (rand.nextDouble() * 2 - 1) * 0.6;
            buf[i] = (short)((tone + noise) * env * Short.MAX_VALUE * 0.85);
        }
        return buf;
    }

    // Hi-Hat: ruído curto
    private static short[] generateHihat() {
        int samples = (int)(SAMPLE_RATE * 0.1);
        short[] buf = new short[samples];
        java.util.Random rand = new java.util.Random(7);
        for (int i = 0; i < samples; i++) {
            double t   = (double) i / SAMPLE_RATE;
            double env = Math.exp(-t * 60.0);
            buf[i] = (short)((rand.nextDouble() * 2 - 1) * env * Short.MAX_VALUE * 0.7);
        }
        return buf;
    }

    // Clap: 3 bursts de ruído
    private static short[] generateClap() {
        int samples = (int)(SAMPLE_RATE * 0.2);
        short[] buf = new short[samples];
        java.util.Random rand = new java.util.Random(13);
        double[] bursts = {0.0, 0.01, 0.02};
        for (int i = 0; i < samples; i++) {
            double t      = (double) i / SAMPLE_RATE;
            double sample = 0.0;
            for (double offset : bursts) {
                double dt = t - offset;
                if (dt >= 0) sample += (rand.nextDouble() * 2 - 1) * Math.exp(-dt * 40.0);
            }
            buf[i] = (short)(sample / bursts.length * Short.MAX_VALUE * 0.8);
        }
        return buf;
    }

    // Bass 808: seno com slide
    private static short[] generateBass(float pitch) {
        int samples = (int)(SAMPLE_RATE * 0.8);
        short[] buf = new short[samples];
        double startFreq = 80.0 * pitch;
        double endFreq   = 55.0 * pitch;
        double phase     = 0.0;
        for (int i = 0; i < samples; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double freq = startFreq + (endFreq - startFreq) * (1 - Math.exp(-t * 5.0));
            double env  = Math.exp(-t * 3.0);
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            buf[i] = (short)(Math.sin(phase) * env * Short.MAX_VALUE * 0.9);
        }
        return buf;
    }

    // Lead: onda quadrada com ADSR
    private static short[] generateLead(float pitch) {
        int samples = (int)(SAMPLE_RATE * 0.3);
        short[] buf = new short[samples];
        double freq    = 440.0 * pitch;
        int attack     = (int)(SAMPLE_RATE * 0.01);
        int release    = (int)(SAMPLE_RATE * 0.1);
        for (int i = 0; i < samples; i++) {
            double t      = (double) i / SAMPLE_RATE;
            double square = Math.sin(2 * Math.PI * freq * t) >= 0 ? 1.0 : -1.0;
            double env;
            if      (i < attack)              env = (double) i / attack;
            else if (i > samples - release)   env = (double)(samples - i) / release;
            else                              env = 1.0;
            buf[i] = (short)(square * env * Short.MAX_VALUE * 0.5);
        }
        return buf;
    }

    // Pad: múltiplos senos detuned
    private static short[] generatePad(float pitch) {
        int samples = (int)(SAMPLE_RATE * 1.0);
        short[] buf = new short[samples];
        double baseFreq  = 220.0 * pitch;
        double[] detunes = {1.0, 1.004, 0.996, 1.008};
        int attack       = (int)(SAMPLE_RATE * 0.2);
        int release      = (int)(SAMPLE_RATE * 0.3);
        for (int i = 0; i < samples; i++) {
            double t      = (double) i / SAMPLE_RATE;
            double env;
            if      (i < attack)             env = (double) i / attack;
            else if (i > samples - release)  env = (double)(samples - i) / release;
            else                             env = 1.0;
            double sample = 0.0;
            for (double d : detunes) sample += Math.sin(2 * Math.PI * baseFreq * d * t);
            buf[i] = (short)(sample / detunes.length * env * Short.MAX_VALUE * 0.6);
        }
        return buf;
    }

    // Perc: seno percussivo
    private static short[] generatePerc(float pitch) {
        int samples = (int)(SAMPLE_RATE * 0.15);
        short[] buf = new short[samples];
        double freq = 600.0 * pitch;
        for (int i = 0; i < samples; i++) {
            double t   = (double) i / SAMPLE_RATE;
            double env = Math.exp(-t * 30.0);
            buf[i] = (short)(Math.sin(2 * Math.PI * freq * t) * env * Short.MAX_VALUE * 0.8);
        }
        return buf;
    }

    public static void playBuffer(short[] buffer, float volume) {
        new Thread(() -> {
            AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(new AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(buffer.length * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

            track.write(buffer, 0, buffer.length);
            track.setVolume(volume);
            track.play();

            long durationMs = (long) buffer.length * 1000L / SAMPLE_RATE + 100L;
            try { Thread.sleep(durationMs); } catch (InterruptedException ignored) {}
            track.release();
        }).start();
    }
}
