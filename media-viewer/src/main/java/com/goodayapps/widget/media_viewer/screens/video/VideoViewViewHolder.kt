package com.goodayapps.widget.media_viewer.screens.video

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
import com.goodayapps.widget.media_viewer.core.AppPlayerView
import com.goodayapps.widget.media_viewer.databinding.ItemVideoPreviewBinding
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.screens.base.ContentClickListener
import com.goodayapps.widget.media_viewer.screens.base.MediaHolder
import com.goodayapps.widget.media_viewer.screens.main.adapters.MediaPreviewAdapter
import com.goodayapps.widget.media_viewer.utils.animateGone
import com.goodayapps.widget.media_viewer.utils.animateVisible
import com.goodayapps.widget.media_viewer.utils.detachFromParent
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player

class VideoViewViewHolder(private val binding: ItemVideoPreviewBinding) :
    MediaHolder(binding.root) {
    companion object {
        private const val KEY_AUTO_PLAY = "auto_play"
        private const val KEY_POSITION = "position"
        private const val KEY_WINDOW = "window"
    }

    private var media: MediaModel? = null
    private var startPosition: Long = C.TIME_UNSET
    private var startAutoPlay: Boolean = true
    private var startWindow: Int = C.INDEX_UNSET

    private val videoProgressHandler = Handler(Looper.getMainLooper())
    private var listener: ContentClickListener? = null
    private var downloadUri: Uri? = null

    override fun bind(model: MediaModel, listener: MediaPreviewAdapter.Listener) {
    }

    override fun attach(appPlayerView: AppPlayerView) {
        if (binding.playerContainer == appPlayerView.view.parent) {
            // Already attached
            return
        }

        /**
         * Since effectively only one [AppPlayerView] instance is used in the app, it might currently
         * be attached to a View from a previous page. In that case, remove it from that parent
         * before adding it to this ViewHolder's View.
         */
        appPlayerView.view.detachFromParent()
        binding.playerContainer.addView(appPlayerView.view)
    }

    private fun handleSavedState(savedInstanceState: Bundle?) {
        startPosition = savedInstanceState?.getLong(KEY_POSITION) ?: C.TIME_UNSET
        startAutoPlay = savedInstanceState?.getBoolean(KEY_AUTO_PLAY) ?: true
        startWindow = savedInstanceState?.getInt(KEY_WINDOW) ?: C.INDEX_UNSET
    }
}