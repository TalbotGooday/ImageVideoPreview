package com.goodayapps.widget.media_viewer.screens.main

sealed class PlayerEvent {
    object TappedPlayer : PlayerEvent()
    data class Processing(val isBuffering: Boolean) : PlayerEvent()
    data class SettledOnPage(val realPage: Int, val relativePage: Int) : PlayerEvent()
}
