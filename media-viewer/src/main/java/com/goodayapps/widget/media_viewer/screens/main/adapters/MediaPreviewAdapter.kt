package com.goodayapps.widget.media_viewer.screens.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.goodayapps.widget.media_viewer.core.AppPlayerView
import com.goodayapps.widget.media_viewer.databinding.ItemImagePreviewBinding
import com.goodayapps.widget.media_viewer.databinding.ItemVideoPreviewBinding
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.screens.base.MediaHolder
import com.goodayapps.widget.media_viewer.screens.image.ImageViewViewHolder
import com.goodayapps.widget.media_viewer.screens.video.VideoViewViewHolder

class MediaPreviewAdapter(private val listener: Listener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val IMAGE_ITEM = 0
        private const val VIDEO_ITEM = 1
    }

    private var data: MutableList<MediaModel> = mutableListOf()

    private var _recyclerView: RecyclerView? = null

    val context
        get() = _recyclerView?.context!!

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isVideo(context)) {
            VIDEO_ITEM
        } else {
            IMAGE_ITEM
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        _recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        _recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIDEO_ITEM) {
            VideoViewViewHolder(ItemVideoPreviewBinding.inflate(inflater, parent, false))
        } else {
            ImageViewViewHolder(ItemImagePreviewBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MediaHolder) {
            val item = data[position]
            holder.bind(item, listener)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun swapData(data: List<MediaModel>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun indexOfCurrent(current: MediaModel?): Int {
        if (current == null) return -1

        return data.indexOf(current)
    }

    fun enableFullScreen(position: Int, enabled: Boolean) {
        val viewHolder = _recyclerView?.findViewHolderForAdapterPosition(position)
            as? MediaHolder

        if (viewHolder == null) {
            if (data.isNotEmpty()) {
                _recyclerView?.doOnLayout {
                    enableFullScreen(position, enabled)
                }
            } else {
                // Nothing to do here.
            }
        } else {
            viewHolder.enableFullScreen(enabled)
        }
    }

    fun attachPlayerView(appPlayerView: AppPlayerView, position: Int) {
        val viewHolder = _recyclerView?.findViewHolderForAdapterPosition(position)
            as? MediaHolder

        if (isVideoPosition(position).not()) return

        if (viewHolder == null) {
            if (data.isNotEmpty()) {
                _recyclerView?.doOnLayout {
                    attachPlayerView(appPlayerView, position)
                }
            } else {
                // Nothing to do here.
            }
        } else {
            viewHolder.attach(appPlayerView)
        }
    }

    fun bindData(position: Int) {
        val holder = _recyclerView?.findViewHolderForAdapterPosition(position)
            as? MediaHolder

        if (holder == null) {
            if (data.isNotEmpty()) {
                _recyclerView?.doOnLayout {
                    bindData(position)
                }
            } else {
                // Nothing to do here.
            }
        } else {
            val item = data[position]
            holder.bind(item, listener)
        }
    }

    fun isVideoPosition(currentItem: Int): Boolean {
        if (currentItem !in data.indices) return false

        return getItemViewType(currentItem) == VIDEO_ITEM
    }

    fun getRelativeVideoPosition(currentItem: Int): Int {
        if (isVideoPosition(currentItem).not()) return RecyclerView.NO_POSITION

        val videoItem = data[currentItem]

        val filtered = data.filterIndexed { index, _ -> isVideoPosition(index) }

        return filtered.indexOf(videoItem)
    }

    interface Listener
}
