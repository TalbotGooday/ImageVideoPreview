package com.goodayapps.widget.media_viewer.core.players

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerState(
    val currentMediaItemId: String?,
    val currentMediaItems: List<String>,
    val currentMediaIndex: Int? = null,
    val seekPositionMillis: Long,
    val isPlaying: Boolean
) : Parcelable {
    companion object {
        val INITIAL = PlayerState(
            currentMediaItemId = null,
            currentMediaItems = emptyList(),
            currentMediaIndex = null,
            seekPositionMillis = 0L,
            isPlaying = false
        )
    }
}
