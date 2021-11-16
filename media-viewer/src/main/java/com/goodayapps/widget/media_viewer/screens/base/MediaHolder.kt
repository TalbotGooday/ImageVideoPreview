package com.goodayapps.widget.media_viewer.screens.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.goodayapps.widget.media_viewer.core.AppPlayerView
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.screens.main.adapters.MediaPreviewAdapter

abstract class MediaHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun enableFullScreen(enabled: Boolean) {
        // no-op
    }

    abstract fun bind(model: MediaModel, listener: MediaPreviewAdapter.Listener)
    open fun attach(appPlayerView: AppPlayerView) {
        // no-op
    }
}