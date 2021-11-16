package com.goodayapps.widget.media_viewer.screens.main

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.SavedStateRegistryOwner
import com.goodayapps.widget.media_viewer.core.MediaViewer
import com.goodayapps.widget.media_viewer.core.players.AppPlayer
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.models.ViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewerViewModel(private val handle: PlayerSavedStateHandle) : ViewModel() {

    // A job associated with listening to AppPlayer events. It's managed along the same lifecycle
    // as the appPlayer field.
    private var listening: Job? = null

    // A job associated with listening to AppPlayer events. It's managed along the same lifecycle
    // as the appPlayer field.
    private var buffering: Job? = null

    private var appPlayer: AppPlayer? = null

    // One-shot side-effects.
    private val viewEffects = MutableSharedFlow<PlayerEvent>()
    fun viewEffects(): SharedFlow<PlayerEvent> = viewEffects

    // State that's persisted in-memory.
    private val viewStates = MutableStateFlow(ViewState())
    fun viewStates(): StateFlow<ViewState> = viewStates

    override fun onCleared() {
        super.onCleared()
        tearDown()
    }

    fun setVideoData(context: Context, data: List<MediaModel>) {
        setupVideoWithFilteredData(context, appPlayer, data)
        viewStates.update { it.copy(data = data) }
    }

    private fun setupVideoWithFilteredData(
        context: Context,
        appPlayer: AppPlayer?,
        data: List<MediaModel>
    ) {
        val videoData = data.filter { media -> media.isVideo(context) }
        appPlayer?.setUpWith(videoData, handle.get())
    }

    fun processEvent(viewEvent: PlayerEvent) {
        when (viewEvent) {
            is PlayerEvent.Processing -> {
                onBuffering(viewEvent.isBuffering)
            }
            is PlayerEvent.SettledOnPage -> {
                onPageSettled(viewEvent.realPage, viewEvent.relativePage)
            }
            PlayerEvent.TappedPlayer -> {
                onPlayerTapped()
            }
        }
    }

    // Returns any active player instance or creates a new one.
    fun getPlayer(context: Context): AppPlayer {
        return appPlayer ?: MediaViewer.player.apply {
            listening = isPlayerRendering()
                .onEach { isPlayerRendering ->

                }
                .launchIn(viewModelScope)
            buffering = isPlayerBuffering()
                .onEach { isPlayerBuffering ->
                    processEvent(PlayerEvent.Processing(isPlayerBuffering))

                    if (isPlayerBuffering.not()) {
                        viewEffects.emit(PlayerEffect.ShowPlayer)
                    }
                }
                .launchIn(viewModelScope)
            viewStates.value.data?.let {
                setupVideoWithFilteredData(context, this, it)
            }
        }.also {
            appPlayer = it
        }
    }

    fun tearDown() {
        listening?.cancel()
        listening = null
        appPlayer?.run {
            // Keep track of player state so that it can be restored across player recreations.
            handle.set(currentPlayerState)
            release()
        }
        appPlayer = null
    }

    private fun onPlayerTapped() {
        val appPlayer = requireNotNull(appPlayer)
        val viewEffect = if (appPlayer.currentPlayerState.isPlaying) {
            appPlayer.pause()
            PlayerEffect.ShowPause
        } else {
            appPlayer.play()
            PlayerEffect.ShowPlay
        }
        viewModelScope.launch {
            viewEffects.emit(viewEffect)
        }
    }

    private fun onBuffering(buffering: Boolean) {
        viewModelScope.launch {
            if (buffering) {
                viewEffects.emit(PlayerEffect.ShowProgress)
            } else {
                viewEffects.emit(PlayerEffect.HideAny)
            }
        }
    }

    private fun onPageSettled(realPage: Int, relativePage: Int) {
        val appPlayer = requireNotNull(appPlayer)
        if (realPage == RecyclerView.NO_POSITION || relativePage == RecyclerView.NO_POSITION) {
            appPlayer.pause()
            return
        }

        val state = appPlayer.currentPlayerState
        handle.saveTime(state.currentMediaItemId.orEmpty(), state.seekPositionMillis)

        val didChangeMedia = playMediaAt(relativePage)
        viewModelScope.launch {
            if (didChangeMedia) {
                viewEffects.emit(PlayerEffect.HideAny)
            }
            viewEffects.emit(PlayerEvent.SettledOnPage(realPage, relativePage))
        }
    }

    private fun playMediaAt(position: Int): Boolean {
        val appPlayer = requireNotNull(appPlayer)
        if (appPlayer.currentPlayerState.currentMediaIndex == position) {
            /** Already playing the media at [position] */
            appPlayer.play()
            return false
        }

        /** Tell UI to hide player while player is loading content at [position]. */
        viewModelScope.launch {
            viewEffects.emit(PlayerEffect.HidePlayer)
        }

        val nextKey = appPlayer.currentPlayerState.currentMediaItems.getOrNull(position)

        val startPosition = handle.getTime(nextKey.orEmpty())

        appPlayer.playMediaAt(position, startPosition)
        appPlayer.play()
        return true
    }

    class Factory(
        savedStateRegistryOwner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            @Suppress("UNCHECKED_CAST")
            return MediaViewerViewModel(PlayerSavedStateHandle(handle)) as T
        }
    }
}