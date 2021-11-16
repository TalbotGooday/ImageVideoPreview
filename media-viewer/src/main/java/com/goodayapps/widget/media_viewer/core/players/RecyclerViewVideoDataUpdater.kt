package com.goodayapps.widget.media_viewer.core.players

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.goodayapps.widget.media_viewer.core.MediaViewer
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

interface PreviewerModelUpdater {
    fun update(exoPlayer: ExoPlayer, incoming: List<MediaModel>)
}

/**
 * [androidx.recyclerview.widget] has out-of-the-box diffing algorithms that are useful for
 * updating/diffing any incoming PreviewerModel compared to current [PreviewerModel]. This class doesn't
 * actually have anything to do with RecyclerView or UI, it just uses RecyclerView APIs for
 * convenience.
 */
class RecyclerViewPreviewerModelUpdater : PreviewerModelUpdater {
    override fun update(exoPlayer: ExoPlayer, incoming: List<MediaModel>) {
        val newMediaItems = incoming.mapNotNull { it.content }.toMediaItems()
        val diffCallback = MediaItemDiffCallback(exoPlayer.currentMediaItems, newMediaItems)
        val updateCallback = ExoPlayerUpdateCallback(exoPlayer, newMediaItems)
        val result = DiffUtil.calculateDiff(diffCallback)
        result.dispatchUpdatesTo(updateCallback)
    }

    private fun List<Uri>.toMediaItems(): List<MediaItem> {
        return mapNotNull { content ->
            return@mapNotNull MediaItem.Builder()
                .setMediaId(content.toString())
                .setUri(content)
                .build()
        }
    }

    private class MediaItemDiffCallback(
        private val current: List<MediaItem>,
        private val incoming: List<MediaItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = current.size
        override fun getNewListSize() = incoming.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return current[oldItemPosition].mediaId == incoming[newItemPosition].mediaId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return current[oldItemPosition] == incoming[newItemPosition]
        }
    }

    private class ExoPlayerUpdateCallback(
        private val exoPlayer: ExoPlayer,
        private val incoming: List<MediaItem>
    ) : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            exoPlayer.addMediaItems(position, incoming.subList(position, position + count))
        }

        override fun onRemoved(position: Int, count: Int) {
            exoPlayer.removeMediaItems(position, position + count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            exoPlayer.moveMediaItem(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            exoPlayer.removeMediaItems(position, position + count)
            exoPlayer.addMediaItems(position, incoming.subList(position, position + count))
        }
    }
}
