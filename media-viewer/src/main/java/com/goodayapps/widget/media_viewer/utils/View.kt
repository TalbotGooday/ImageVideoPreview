package com.goodayapps.widget.media_viewer.utils

import android.view.View
import android.view.ViewManager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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


fun View.detachFromParent() {
    val parent = parent as? ViewManager ?: return
    parent.removeView(this)
}

fun ViewPager2.pageScrollStateChanges(): Flow<Int> = callbackFlow {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            trySend(state)
        }
    }

    registerOnPageChangeCallback(callback)

    awaitClose { unregisterOnPageChangeCallback(callback) }
}
