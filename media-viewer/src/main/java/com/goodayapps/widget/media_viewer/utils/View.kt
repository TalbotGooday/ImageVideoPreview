package com.goodayapps.widget.media_viewer.utils

import android.view.View

internal fun View.animateGone(duration: Long = 220, action: (() -> Unit)? = null) {
    if (visibility != View.GONE) {
        animate().setDuration(duration)
            .alpha(0f)
            .withEndAction {
                visibility = View.GONE
                action?.invoke()
            }
    }
}

internal fun View.animateVisible(duration: Long = 220) {
    if (visibility != View.VISIBLE) {
        alpha = 0f
        visibility = View.VISIBLE
        animate().setDuration(duration)
            .alpha(1f)
    }
}
