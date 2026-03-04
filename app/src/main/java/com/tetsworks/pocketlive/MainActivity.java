package com.tetsworks.pocketlive;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.tetsworks.pocketlive.audio.Synthesizer;
import com.tetsworks.pocketlive.databinding.ActivityMainBinding;
import com.tetsworks.pocketlive.model.Track;
import com.tetsworks.pocketlive.ui.TrackRowView;
import com.tetsworks.pocketlive.viewmodel.SequencerViewModel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SequencerViewModel vm;

    // Cada entrada: [trackId]  índice = posição na lista de views
    private final List<Integer>      trackIds  = new ArrayList<>();
    private final List<TrackRowView> trackViews = new ArrayList<>();

    private String pendingTrackName = null;

    private final ActivityResultLauncher<Intent> pickAudio =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri  = result.getData().getData();
                String name = pendingTrackName != null ? pendingTrackName : getFileName(uri);
                vm.addCustomTrack(name, uri);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider(this).get(SequencerViewModel.class);
        setupControls();
        observeViewModel();
    }

    private void setupControls() {
        binding.playPauseButton.setOnClickListener(v -> {
            Boolean playing = vm.isPlaying.getValue();
            if (playing != null && playing) vm.pause(); else vm.play();
        });

        binding.stopButton.setOnClickListener(v -> vm.stop());

        binding.bpmSeekBar.setMax(260);
        Integer currentBpm = vm.bpm.getValue();
        binding.bpmSeekBar.setProgress(currentBpm != null ? currentBpm - 40 : 80);
        binding.bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) vm.setBpm(progress + 40);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        binding.addTrackButton.setOnClickListener(v -> showAddTrackMenu());
    }

    private void observeViewModel() {
        vm.tracks.observe(this, tracks -> {
            // Adicionar novas trilhas
            for (Track track : tracks) {
                if (!trackIds.contains(track.getId())) {
                    TrackRowView row = new TrackRowView(this);
                    final int tid = track.getId();
                    row.setOnStepToggledListener(step -> vm.toggleStep(tid, step));
                    row.setOnMuteToggledListener(() -> vm.toggleMute(tid));
                    row.setOnVolumeChangedListener(vol -> vm.setVolume(tid, vol));
                    row.setOnLongPressListener(() -> showTrackOptions(tid));
                    binding.tracksContainer.addView(row);
                    trackIds.add(track.getId());
                    trackViews.add(row);
                }
                // Atualizar view
                int idx = trackIds.indexOf(track.getId());
                if (idx >= 0) trackViews.get(idx).bind(track);
            }

            // Remover trilhas deletadas
            List<Integer> activeIds = new ArrayList<>();
            for (Track t : tracks) activeIds.add(t.getId());

            for (int i = trackIds.size() - 1; i >= 0; i--) {
                if (!activeIds.contains(trackIds.get(i))) {
                    binding.tracksContainer.removeView(trackViews.get(i));
                    trackIds.remove(i);
                    trackViews.remove(i);
                }
            }
        });

        vm.currentStep.observe(this, step -> {
            for (TrackRowView row : trackViews) row.highlightStep(step);
        });

        vm.isPlaying.observe(this, playing -> {
            binding.playPauseButton.setIconResource(
                playing ? R.drawable.ic_pause : R.drawable.ic_play
            );
        });

        vm.bpm.observe(this, bpmVal ->
            binding.bpmLabel.setText("BPM: " + bpmVal)
        );
    }

    private void showAddTrackMenu() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Trilha")
            .setItems(new String[]{
                "🎛️ Instrumento sintetizado",
                "🎵 Áudio do celular (remix/sample)"
            }, (dialog, which) -> {
                if (which == 0) showSynthPicker();
                else            showCustomAudioPicker();
            })
            .show();
    }

    private void showSynthPicker() {
        String[] names = new String[Synthesizer.COUNT];
        for (int i = 0; i < Synthesizer.COUNT; i++)
            names[i] = Synthesizer.EMOJIS[i] + " " + Synthesizer.NAMES[i];

        new MaterialAlertDialogBuilder(this)
            .setTitle("Escolha o instrumento")
            .setItems(names, (dialog, which) -> vm.addSynthTrack(which))
            .show();
    }

    private void showCustomAudioPicker() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_track, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.trackNameInput);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Importar Áudio")
            .setView(dialogView)
            .setPositiveButton("Escolher Arquivo", (dialog, which) -> {
                String typed = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                pendingTrackName = typed.isEmpty() ? null : typed;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                pickAudio.launch(intent);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showTrackOptions(int trackId) {
        List<Track> list = vm.tracks.getValue();
        String trackName = "Trilha";
        if (list != null) {
            for (Track t : list) {
                if (t.getId() == trackId) { trackName = t.getName(); break; }
            }
        }
        new MaterialAlertDialogBuilder(this)
            .setTitle(trackName)
            .setItems(new String[]{"Limpar steps", "Remover trilha"}, (dialog, which) -> {
                if (which == 0) vm.clearTrack(trackId);
                else            vm.removeTrack(trackId);
            })
            .show();
    }

    private String getFileName(Uri uri) {
        String name = "Sample";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    String full = cursor.getString(idx);
                    int dot = full.lastIndexOf('.');
                    name = dot > 0 ? full.substring(0, dot) : full;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vm.stop();
    }
}
