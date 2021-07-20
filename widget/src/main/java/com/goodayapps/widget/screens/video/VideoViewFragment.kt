package com.goodayapps.widget.screens.video

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.vkay94.dtpv.youtube.YouTubeOverlay.PerformListener
import com.goodayapps.widget.R
import com.goodayapps.widget.databinding.FragmentVideoPreviewBinding
import com.goodayapps.widget.models.MediaModel
import com.goodayapps.widget.screens.base.FragmentLifecycle
import com.goodayapps.widget.utils.animateGone
import com.goodayapps.widget.utils.animateVisible
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer.Builder

class VideoViewFragment : Fragment(), FragmentLifecycle {
    companion object {
        private const val KEY_AUTO_PLAY = "auto_play"
        private const val KEY_POSITION = "position"
        private const val KEY_WINDOW = "window"

        fun new(item: MediaModel) = VideoViewFragment().apply {
            arguments = bundleOf(
                "media" to item
            )
        }
    }

    private var _binding: FragmentVideoPreviewBinding? = null

    private val binding get() = _binding!!

    private var player: SimpleExoPlayer? = null

    private var media: MediaModel? = null
    private var startPosition: Long = C.TIME_UNSET
    private var startAutoPlay: Boolean = true
    private var startWindow: Int = C.INDEX_UNSET

    private val handler = Handler(Looper.getMainLooper())
    private val showProcessRunnable = Runnable {
        binding.videoProgress.animateVisible()
        binding.playerView.hideController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPreviewBinding.inflate(inflater, container, false)
        handleSavedState(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        media = arguments?.getParcelable("media")

        startPosition = savedInstanceState?.getLong(KEY_POSITION, 0L) ?: 0L
        if (media == null) {
            // suka blyat
            showError()
            return
        }

        handleSavedState(savedInstanceState)

        initMediaData(media!!)
    }

    override fun onResume() {
        super.onResume()
        initPlayer(requireContext())
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onPauseFragment() {
        updateStartPosition()
        releasePlayer()
    }

    override fun onResumeFragment() {
        initPlayer(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSavedState(savedInstanceState)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateStartPosition()
        outState.putBoolean(KEY_AUTO_PLAY, true)
        outState.putLong(KEY_POSITION, startPosition)
        outState.putInt(KEY_WINDOW, startWindow)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        handleSavedState(savedInstanceState)
    }

    private fun handleSavedState(savedInstanceState: Bundle?) {
        startPosition = savedInstanceState?.getLong(KEY_POSITION) ?: C.TIME_UNSET
        startAutoPlay = savedInstanceState?.getBoolean(KEY_AUTO_PLAY) ?: true
        startWindow = savedInstanceState?.getInt(KEY_WINDOW) ?: C.INDEX_UNSET

    }

    private fun updateStartPosition() {
        if (player != null) {
            startAutoPlay = player!!.playWhenReady
            startPosition = 0L.coerceAtLeast(player!!.contentPosition)
            startWindow = player!!.currentWindowIndex
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    private fun initMediaData(media: MediaModel) = with(binding) {
        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (media.url != null) {
            player?.setMediaItem(MediaItem.fromUri(media.url), !haveStartPosition)
        }

        if (media.uri != null) {
            player?.setMediaItem(MediaItem.fromUri(media.uri), !haveStartPosition)
        }

        player?.prepare()
        binding.playerView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }

    private fun showError() {
    }

    private fun initViews() {
        initPlayer(requireContext())
    }

    private fun initPlayer(context: Context) = with(binding) {
        if (player != null) {
            player?.play()

            return@with
        }

        player = Builder(context).build().also { player ->
            playerView.setErrorMessageProvider { Pair.create(0, "Something is wrong") }
            playerView.requestFocus()

            player.playWhenReady = startAutoPlay
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_BUFFERING) {
                        handler.postDelayed(showProcessRunnable, 500)
                    } else {
                        handler.removeCallbacks(showProcessRunnable)

                        videoProgress.animateGone()
                        playerView.showController()
                    }
                }
            })
            playerView.player = player
            val haveStartPosition = startWindow != C.INDEX_UNSET
            if (haveStartPosition) {
                player.seekTo(startWindow, startPosition)
            }

            youtubeOverlay.performListener(object : PerformListener {
                override fun onAnimationStart() {
                    youtubeOverlay.animateVisible()
                }

                override fun onAnimationEnd() {
                    youtubeOverlay.animateGone()
                }
            })

            youtubeOverlay.player(player)
        }

        media?.let { initMediaData(it) }

    }
}