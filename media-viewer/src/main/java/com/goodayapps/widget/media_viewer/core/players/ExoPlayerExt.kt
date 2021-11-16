package com.goodayapps.widget.media_viewer.core.players

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

val ExoPlayer.currentMediaItems: List<MediaItem> get() {
    val mediaItems = mutableListOf<MediaItem>()

    for (i in 0 until mediaItemCount) {
        mediaItems += getMediaItemAt(i)
    }

    return mediaItems
}

fun ExoPlayer.loopVideos() {
    repeatMode = Player.REPEAT_MODE_ONE
}


@SuppressLint("ClickableViewAccessibility")
fun View.taps(): Flow<Unit> = callbackFlow {
    val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
            trySend(Unit)
            return true
        }
    }
    val gestureDetector = GestureDetector(context, gestureListener)

    setOnTouchListener { _, motionEvent ->
        gestureDetector.onTouchEvent(motionEvent)
        true
    }

    awaitClose { setOnTouchListener(null) }
}
