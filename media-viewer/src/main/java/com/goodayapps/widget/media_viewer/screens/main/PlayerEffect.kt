package com.goodayapps.widget.media_viewer.screens.main

sealed class PlayerEffect: PlayerEvent() {
    object ShowPause : PlayerEffect()
    object ShowPlay : PlayerEffect()
    object ShowProgress : PlayerEffect()
    object HideAny : PlayerEffect()
    object HidePlayer : PlayerEffect()
    object ShowPlayer : PlayerEffect()
}
