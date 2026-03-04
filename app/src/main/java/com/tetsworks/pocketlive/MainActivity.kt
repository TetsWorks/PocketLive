package com.tetsworks.pocketlive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.tetsworks.pocketlive.audio.Synthesizer
import com.tetsworks.pocketlive.databinding.ActivityMainBinding
import com.tetsworks.pocketlive.model.Track
import com.tetsworks.pocketlive.ui.TrackRowView
import com.tetsworks.pocketlive.viewmodel.SequencerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vm: SequencerViewModel
    private val trackRows = mutableListOf<Pair<Int, TrackRowView>>()
    private var pendingTrackName: String? = null

    private val pickAudio = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val name = pendingTrackName ?: getFileName(uri)
            vm.addCustomTrack(name, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[SequencerViewModel::class.java]
        setupControls()
        observeViewModel()
    }

    private fun setupControls() {
        binding.playPauseButton.setOnClickListener {
            if (vm.isPlaying.value == true) vm.pause() else vm.play()
        }
        binding.stopButton.setOnClickListener { vm.stop() }

        binding.bpmSeekBar.max = 260
        binding.bpmSeekBar.progress = vm.bpm.value!! - 40
        binding.bpmSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) vm.setBpm(progress + 40)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        binding.addTrackButton.setOnClickListener { showAddTrackMenu() }
    }

    private fun observeViewModel() {
        vm.tracks.observe(this) { tracks ->
            tracks.forEach { track ->
                if (trackRows.none { it.first == track.id }) {
                    val row = TrackRowView(this).apply {
                        onStepToggled    = { step -> vm.toggleStep(track.id, step) }
                        onMuteToggled    = { vm.toggleMute(track.id) }
                        onVolumeChanged  = { vol -> vm.setVolume(track.id, vol) }
                        onLongPress      = { showTrackOptions(track.id) }
                    }
                    binding.tracksContainer.addView(row)
                    trackRows.add(Pair(track.id, row))
                }
                trackRows.find { it.first == track.id }?.second?.bind(track)
            }
            val ids = tracks.map { it.id }
            trackRows.filter { it.first !in ids }.forEach { (_, row) ->
                binding.tracksContainer.removeView(row)
            }
            trackRows.removeAll { it.first !in ids }
        }

        vm.currentStep.observe(this) { step ->
            trackRows.forEach { (_, row) -> row.highlightStep(step) }
        }

        vm.isPlaying.observe(this) { playing ->
            binding.playPauseButton.setIconResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        vm.bpm.observe(this) { bpm ->
            binding.bpmLabel.text = "BPM: $bpm"
        }
    }

    /** Menu: Instrumento Sintetizado OU Áudio customizado */
    private fun showAddTrackMenu() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Trilha")
            .setItems(arrayOf(
                "🎛️ Instrumento sintetizado",
                "🎵 Áudio do celular (remix/sample)"
            )) { _, which ->
                when (which) {
                    0 -> showSynthPicker()
                    1 -> showCustomAudioPicker()
                }
            }
            .show()
    }

    /** Lista todos os instrumentos do sintetizador */
    private fun showSynthPicker() {
        val types = Synthesizer.InstrumentType.values()
        val names = types.map { "${it.emoji} ${it.displayName}" }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Escolha o instrumento")
            .setItems(names) { _, which ->
                vm.addSynthTrack(types[which])
            }
            .show()
    }

    /** Importar áudio do celular */
    private fun showCustomAudioPicker() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_track, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.trackNameInput)
        MaterialAlertDialogBuilder(this)
            .setTitle("Importar Áudio")
            .setView(dialogView)
            .setPositiveButton("Escolher Arquivo") { _, _ ->
                pendingTrackName = nameInput.text?.toString()?.ifBlank { null }
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                }
                pickAudio.launch(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showTrackOptions(trackId: Int) {
        val track = vm.tracks.value?.find { it.id == trackId } ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(track.name)
            .setItems(arrayOf("Limpar steps", "Remover trilha")) { _, which ->
                when (which) {
                    0 -> vm.clearTrack(trackId)
                    1 -> vm.removeTrack(trackId)
                }
            }
            .show()
    }

    private fun getFileName(uri: Uri): String {
        var name = "Sample"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0)
                name = cursor.getString(idx).substringBeforeLast(".")
        }
        return name
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.stop()
    }
}
