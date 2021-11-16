package com.goodayapps.widget.media_viewer.screens.main

import androidx.lifecycle.SavedStateHandle
import com.goodayapps.widget.media_viewer.core.players.PlayerState

class PlayerSavedStateHandle(
    private val handle: SavedStateHandle
) {
    fun get(): PlayerState? {
        return handle[KEY_PLAYER_STATE]
    }

    fun set(playerState: PlayerState) {
        handle[KEY_PLAYER_STATE] = playerState
    }

    fun saveTime(key: String, value: Long) {
        handle[key] = value
    }

     fun getTime(key: String): Long {
        return handle[key] ?: 0
    }

    companion object {
        private const val KEY_PLAYER_STATE = "player_state"
    }
}
