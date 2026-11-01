package com.example.cse489assignment2.ui

import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.R
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class VideoFragment : Fragment(), TextureView.SurfaceTextureListener {
    private lateinit var texture: TextureView
    private lateinit var poster: ImageView
    private lateinit var loading: ProgressBar
    private lateinit var error: TextView
    private lateinit var toggle: ImageButton
    private lateinit var seek: SeekBar
    private lateinit var elapsed: TextView
    private lateinit var durationLabel: TextView

    private var player: MediaPlayer? = null
    private var surface: Surface? = null
    private var prepared = false
    private var videoWidth = 0
    private var videoHeight = 0
    private var resumePosition = 0
    private var resumePlaying = false
    private val clock = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            val mp = player ?: return
            if (prepared && view != null) {
                try {
                    val position = mp.currentPosition
                    if (!seek.isPressed) seek.progress = position
                    elapsed.text = timeLabel(position)
                    setIcon(mp.isPlaying)
                    clock.postDelayed(this, 250)
                } catch (_: IllegalStateException) {
                    displayError()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resumePosition = savedInstanceState?.getInt("video_position") ?: 0
        resumePlaying = savedInstanceState?.getBoolean("video_playing") ?: false

        texture = view.findViewById(R.id.video_texture)
        poster = view.findViewById(R.id.video_poster)
        loading = view.findViewById(R.id.video_loading_indicator)
        error = view.findViewById(R.id.video_error)
        toggle = view.findViewById(R.id.video_toggle)
        seek = view.findViewById(R.id.video_seek)
        elapsed = view.findViewById(R.id.video_elapsed)
        durationLabel = view.findViewById(R.id.video_duration)
        toggle.isEnabled = false
        seek.isEnabled = false
        setIcon(false)

        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        poster.scaleType = if (landscape) ImageView.ScaleType.CENTER_CROP
            else ImageView.ScaleType.FIT_CENTER

        toggle.setOnClickListener {
            val mp = player ?: return@setOnClickListener
            if (!prepared) return@setOnClickListener
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    texture.keepScreenOn = false
                } else {
                    mp.start()
                    texture.keepScreenOn = true
                }
                setIcon(mp.isPlaying)
            } catch (_: IllegalStateException) {
                displayError()
            }
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) elapsed.text = timeLabel(progress)
            }
            override fun onStartTrackingTouch(bar: SeekBar) = Unit
            override fun onStopTrackingTouch(bar: SeekBar) {
                if (prepared) {
                    try { player?.seekTo(bar.progress) }
                    catch (_: IllegalStateException) { displayError() }
                }
            }
        })
        texture.surfaceTextureListener = this
        if (texture.isAvailable) {
            texture.surfaceTexture?.let {
                onSurfaceTextureAvailable(it, texture.width, texture.height)
            }
        }
    }

    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
        releasePlayer()
        surface = Surface(st)
        try {
            val mp = MediaPlayer()
            player = mp
            resources.openRawResourceFd(R.raw.sample_video).use { afd ->
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            mp.setSurface(surface)
            mp.setOnPreparedListener { readyPlayer ->
                if (view == null) return@setOnPreparedListener
                prepared = true
                videoWidth = readyPlayer.videoWidth
                videoHeight = readyPlayer.videoHeight
                applyVideoTransform()
                loading.visibility = View.GONE
                seek.max = max(1, readyPlayer.duration)
                durationLabel.text = timeLabel(readyPlayer.duration)
                toggle.isEnabled = true
                seek.isEnabled = true
                if (resumePosition > 0) {
                    readyPlayer.seekTo(resumePosition.coerceAtMost(readyPlayer.duration))
                    elapsed.text = timeLabel(resumePosition)
                }
                if (resumePlaying) {
                    readyPlayer.start()
                    texture.keepScreenOn = true
                    setIcon(true)
                }
                resumePosition = 0
                resumePlaying = false
                clock.removeCallbacks(tick)
                clock.post(tick)
            }
            mp.setOnVideoSizeChangedListener { _, w, h ->
                videoWidth = w
                videoHeight = h
                applyVideoTransform()
            }
            mp.setOnInfoListener { _, code, _ ->
                if (code == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    if (view != null) poster.visibility = View.GONE
                }
                false
            }
            mp.setOnCompletionListener { completed ->
                if (view != null) {
                    completed.seekTo(0)
                    seek.progress = 0
                    elapsed.text = timeLabel(0)
                    poster.visibility = View.VISIBLE
                    texture.keepScreenOn = false
                    setIcon(false)
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                displayError()
                true
            }
            mp.prepareAsync()
        } catch (_: Exception) {
            displayError()
        }
    }

    private fun applyVideoTransform() {
        if (!::texture.isInitialized || videoWidth <= 0 || videoHeight <= 0 ||
            texture.width <= 0 || texture.height <= 0) return

        val vw = texture.width.toFloat()
        val vh = texture.height.toFloat()
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        // Portrait: show the whole movie. Landscape: fill the player surface,
        // cropping only the excess edges instead of leaving giant empty strips.
        val scale = if (landscape) max(vw / videoWidth, vh / videoHeight)
                    else min(vw / videoWidth, vh / videoHeight)
        val scaleX = videoWidth * scale / vw
        val scaleY = videoHeight * scale / vh
        texture.setTransform(Matrix().apply {
            setScale(scaleX, scaleY, vw / 2f, vh / 2f)
        })
    }

    private fun setIcon(playing: Boolean) {
        if (!::toggle.isInitialized) return
        toggle.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        toggle.contentDescription = getString(if (playing) R.string.pause else R.string.play)
    }

    private fun timeLabel(ms: Int): String = String.format(
        Locale.US, "%02d:%02d", ms.coerceAtLeast(0) / 60_000,
        (ms.coerceAtLeast(0) / 1000) % 60
    )

    private fun displayError() {
        if (view == null) return
        clock.removeCallbacks(tick)
        loading.visibility = View.GONE
        error.visibility = View.VISIBLE
        toggle.isEnabled = false
        seek.isEnabled = false
    }

    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
        applyVideoTransform()
    }
    override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
        releasePlayer()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (prepared) {
            try {
                val mp = player
                if (mp != null) {
                    outState.putInt("video_position", mp.currentPosition)
                    outState.putBoolean("video_playing", mp.isPlaying)
                }
            } catch (_: IllegalStateException) { }
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        player?.let { mp ->
            try { if (mp.isPlaying) mp.pause() } catch (_: IllegalStateException) { }
        }
        if (::texture.isInitialized) texture.keepScreenOn = false
        if (::toggle.isInitialized) setIcon(false)
        super.onStop()
    }

    private fun releasePlayer() {
        clock.removeCallbacks(tick)
        prepared = false
        player?.release()
        player = null
        surface?.release()
        surface = null
    }

    override fun onDestroyView() {
        clock.removeCallbacks(tick)
        texture.surfaceTextureListener = null
        releasePlayer()
        super.onDestroyView()
    }
}
