package com.goodayapps.widget.media_viewer.core.players

import com.goodayapps.widget.media_viewer.core.MediaViewer
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ExoAppPlayer(
    val exoPlayer: ExoPlayer,
    private val updater: PreviewerModelUpdater
) : AppPlayer {
    override val currentPlayerState: PlayerState get() = exoPlayer.toPlayerState()

    override suspend fun setUpWith(data: List<MediaModel>, playerState: PlayerState?) {
        // A signal to restore any saved video state.
        val isInitializing = exoPlayer.currentMediaItem == null

        val resolvedData = MediaViewer.mediaUriResolver.resolve(data)

        updater.update(exoPlayer = exoPlayer, incoming = resolvedData)
        val currentMediaItems = exoPlayer.currentMediaItems

        val reconciledPlayerState = if (isInitializing) {
            /**
             * When restoring saved state, the saved media item might be unavailable, e.g. if
             * the saved media item before process death was from a data set different than [data].
             */
            val canRestoreSavedPlayerState = playerState != null
                && currentMediaItems.any { mediaItem -> mediaItem.mediaId == playerState.currentMediaItemId }

            if (canRestoreSavedPlayerState) {
                requireNotNull(playerState)
            } else {
                PlayerState.INITIAL
            }
        } else {
            exoPlayer.toPlayerState()
        }

        val windowIndex = currentMediaItems.indexOfFirst { mediaItem ->
            mediaItem.mediaId == reconciledPlayerState.currentMediaItemId
        }
        if (windowIndex != -1) {
            exoPlayer.seekTo(windowIndex, reconciledPlayerState.seekPositionMillis)
        }
        exoPlayer.playWhenReady = reconciledPlayerState.isPlaying
        exoPlayer.prepare()
    }

    // A signal that video content is immediately ready to play; any preview images
    // on top of the video can be hidden to reveal actual video playback underneath.
    override fun isPlayerRendering(): Flow<Boolean> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                trySend(true)
            }
        }

        exoPlayer.addListener(listener)

        awaitClose { exoPlayer.removeListener(listener) }
    }


    // A signal that video content is immediately ready to play; any preview images
    // on top of the video can be hidden to reveal actual video playback underneath.
    override fun isPlayerBuffering(): Flow<Boolean> = callbackFlow {
        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                when(playbackState){
                    Player.STATE_BUFFERING -> {
                        trySend(true)
                    }
                    else -> {
                        trySend(false)
                    }
                }
            }
        }

        exoPlayer.addListener(listener)

        awaitClose { exoPlayer.removeListener(listener) }
    }

    private fun ExoPlayer.toPlayerState(): PlayerState {
        return PlayerState(
            currentMediaItemId = currentMediaItem?.mediaId,
            currentMediaItems = currentMediaItems.map { it.mediaId },
            currentMediaIndex = currentWindowIndex,
            seekPositionMillis = currentPosition,
            isPlaying = isPlaying
        )
    }

    override fun playMediaAt(position: Int, startTime: Long) {
        exoPlayer.seekToDefaultPosition(position)
        exoPlayer.seekTo(startTime)
        exoPlayer.playWhenReady = true
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun release() {
        exoPlayer.release()
    }
}
