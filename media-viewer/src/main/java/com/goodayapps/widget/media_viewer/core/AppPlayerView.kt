package com.goodayapps.widget.media_viewer.core

import android.view.View
import com.goodayapps.widget.media_viewer.core.players.AppPlayer
import com.goodayapps.widget.media_viewer.screens.main.PlayerEffect
import kotlinx.coroutines.flow.Flow

// Abstraction over the player view. This facilitates testing and hides implementation details.
interface AppPlayerView {
    val view: View

    fun renderEffect(playerViewEffect: PlayerEffect)
    fun onStart(appPlayer: AppPlayer)
    fun onStop()
    fun taps(): Flow<Unit>
}
