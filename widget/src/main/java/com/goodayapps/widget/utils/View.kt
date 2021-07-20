package com.goodayapps.widget.utils

import android.view.View

fun View.animateGone(duration: Long = 220, action: (() -> Unit)? = null) {
    if (visibility != View.GONE) {
        animate().setDuration(duration)
            .alpha(0f)
            .withEndAction {
                visibility = View.GONE
                action?.invoke()
            }
    }
}

fun View.animateVisible(duration: Long = 220) {
    if (visibility != View.VISIBLE) {
        alpha = 0f
        visibility = View.VISIBLE
        animate().setDuration(duration)
            .alpha(1f)
    }
}
