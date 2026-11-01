package com.example.cse489assignment2.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.R
import java.util.Locale

class AudioFragment : Fragment() {
    private var media: MediaPlayer? = null
    private lateinit var seek: SeekBar
    private lateinit var elapsed: TextView
    private lateinit var durationLabel: TextView
    private lateinit var error: TextView
    private lateinit var toggle: ImageButton
    private val clock = Handler(Looper.getMainLooper())
    private var resumePosition = 0
    private var resumePlaying = false

    private val tick = object : Runnable {
        override fun run() {
            val mp = media ?: return
            if (view == null) return
            try {
                if (!seek.isPressed) seek.progress = mp.currentPosition
                elapsed.text = timestamp(mp.currentPosition)
                setIcon(mp.isPlaying)
                clock.postDelayed(this, 250)
            } catch (_: IllegalStateException) { showError() }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_audio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resumePosition = savedInstanceState?.getInt("audio_position") ?: 0
        resumePlaying = savedInstanceState?.getBoolean("audio_playing") ?: false
        seek = view.findViewById(R.id.audio_seek)
        elapsed = view.findViewById(R.id.audio_elapsed)
        durationLabel = view.findViewById(R.id.audio_duration)
        error = view.findViewById(R.id.audio_error)
        toggle = view.findViewById(R.id.audio_toggle)
        toggle.isEnabled = false
        seek.isEnabled = false
        setIcon(false)

        try {
            val mp = MediaPlayer.create(requireContext(), R.raw.sample_audio)
                ?: throw IllegalStateException("Audio file could not be decoded")
            media = mp
            seek.max = mp.duration.coerceAtLeast(1)
            durationLabel.text = timestamp(mp.duration)
            toggle.isEnabled = true
            seek.isEnabled = true
            if (resumePosition > 0) {
                mp.seekTo(resumePosition.coerceAtMost(mp.duration))
                seek.progress = resumePosition
                elapsed.text = timestamp(resumePosition)
            }
            if (resumePlaying) {
                mp.start()
                setIcon(true)
            }
            mp.setOnCompletionListener { completed ->
                // The music ends, and the playhead returns to the beginning.
                try { completed.seekTo(0) } catch (_: IllegalStateException) { }
                if (this.view != null) {
                    seek.progress = 0
                    elapsed.text = timestamp(0)
                    setIcon(false)
                }
            }
            mp.setOnErrorListener { _, _, _ -> showError(); true }
            toggle.setOnClickListener {
                try {
                    if (mp.isPlaying) mp.pause() else mp.start()
                    setIcon(mp.isPlaying)
                } catch (_: IllegalStateException) { showError() }
            }
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar, position: Int, fromUser: Boolean) {
                    if (fromUser) elapsed.text = timestamp(position)
                }
                override fun onStartTrackingTouch(bar: SeekBar) = Unit
                override fun onStopTrackingTouch(bar: SeekBar) {
                    try { mp.seekTo(bar.progress) }
                    catch (_: IllegalStateException) { showError() }
                }
            })
            clock.post(tick)
        } catch (_: Exception) { showError() }
    }

    private fun setIcon(playing: Boolean) {
        if (!::toggle.isInitialized) return
        toggle.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        toggle.contentDescription = getString(if (playing) R.string.pause else R.string.play)
    }

    private fun timestamp(ms: Int): String = String.format(
        Locale.US, "%02d:%02d", ms.coerceAtLeast(0) / 60000,
        (ms.coerceAtLeast(0) / 1000) % 60
    )

    private fun showError() {
        if (view == null) return
        error.visibility = View.VISIBLE
        clock.removeCallbacks(tick)
        toggle.isEnabled = false
        seek.isEnabled = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        media?.let { mp ->
            try {
                outState.putInt("audio_position", mp.currentPosition)
                outState.putBoolean("audio_playing", mp.isPlaying)
            } catch (_: IllegalStateException) { }
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        media?.let { mp ->
            try { if (mp.isPlaying) mp.pause() } catch (_: IllegalStateException) { }
        }
        if (::toggle.isInitialized) setIcon(false)
        super.onStop()
    }

    override fun onDestroyView() {
        clock.removeCallbacks(tick)
        media?.release()
        media = null
        super.onDestroyView()
    }
}
