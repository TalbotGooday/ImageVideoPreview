package com.goodayapps.widget.media_viewer.core.players

import com.goodayapps.widget.media_viewer.models.MediaModel
import kotlinx.coroutines.flow.Flow

// Abstract the underlying player to facilitate testing and hide player implementation details.
interface AppPlayer {
    val currentPlayerState: PlayerState

    suspend fun setUpWith(data: List<MediaModel>, playerState: PlayerState?)
    fun isPlayerRendering(): Flow<Boolean>
    fun isPlayerBuffering(): Flow<Boolean>
    fun playMediaAt(position: Int, startTime: Long)
    fun play()
    fun pause()
    fun release()
}
