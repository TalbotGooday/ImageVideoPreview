package com.goodayapps.widget.media_viewer.core

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isInvisible
import com.goodayapps.widget.media_viewer.R
import com.goodayapps.widget.media_viewer.core.players.AppPlayer
import com.goodayapps.widget.media_viewer.core.players.ExoAppPlayer
import com.goodayapps.widget.media_viewer.core.players.taps
import com.goodayapps.widget.media_viewer.databinding.PlayerViewBinding
import com.goodayapps.widget.media_viewer.screens.main.PlayerEffect
import com.goodayapps.widget.media_viewer.utils.animateGone
import com.goodayapps.widget.media_viewer.utils.animateVisible
import kotlinx.coroutines.flow.Flow

/**
 * An implementation of AppPlayerView that uses ExoPlayer APIs,
 * namely [com.google.android.exoplayer2.ui.PlayerView]
 */
class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    companion object{
        const val HIDE_TIME = 2_000L
    }
    override val view: View = layoutInflater.inflate(R.layout.player_view, null)
    private val binding = PlayerViewBinding.bind(view)

    private val videoProgressHandler = Handler(Looper.getMainLooper())

    private val showProcessRunnable = Runnable {
        renderEffect(PlayerEffect.HideAny)
    }

    override fun onStart(appPlayer: AppPlayer) {
        binding.playerView.player = (appPlayer as? ExoAppPlayer)?.exoPlayer
    }

    // ExoPlayer and PlayerView hold circular ref's to each other, so avoid leaking
    // Activity here by nulling it out.
    override fun onStop() {
        binding.playerView.player = null
    }

    override fun renderEffect(playerViewEffect: PlayerEffect) {
        videoProgressHandler.removeCallbacks(showProcessRunnable)
        when (playerViewEffect) {
            PlayerEffect.HideAny -> {
                binding.playPause.animateGone()
                binding.videoProgress.animateGone()
            }
            PlayerEffect.ShowPause -> {
                binding.playPause.apply {
                    setImageResource(R.drawable.ic_player_pause)
                    animateVisible()
                }
                scheduleViewsHide()
            }
            PlayerEffect.ShowPlay -> {
                binding.playPause.apply {
                    setImageResource(R.drawable.ic_player_play)
                    animateVisible()
                }
                scheduleViewsHide()
            }
            PlayerEffect.ShowProgress -> {
                binding.videoProgress.animateVisible()
            }
            PlayerEffect.HidePlayer -> {
                binding.playerView.isInvisible = true
            }
            PlayerEffect.ShowPlayer -> {
                binding.playerView.isInvisible = false
            }
        }
    }

    private fun scheduleViewsHide() {
        videoProgressHandler.removeCallbacks(showProcessRunnable)
        videoProgressHandler.postDelayed(showProcessRunnable, HIDE_TIME)
    }

    override fun taps(): Flow<Unit> = binding.playerView.taps()
}
