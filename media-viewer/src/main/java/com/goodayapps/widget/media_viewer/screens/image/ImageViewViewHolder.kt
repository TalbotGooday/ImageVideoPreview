package com.goodayapps.widget.media_viewer.screens.image

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
import coil.clear
import coil.load
import com.goodayapps.widget.media_viewer.core.MediaViewer
import com.goodayapps.widget.media_viewer.databinding.ItemImagePreviewBinding
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.screens.base.ContentClickListener
import com.goodayapps.widget.media_viewer.screens.base.MediaHolder
import com.goodayapps.widget.media_viewer.screens.main.adapters.MediaPreviewAdapter
import com.goodayapps.widget.media_viewer.utils.animateGone
import com.goodayapps.widget.media_viewer.utils.animateVisible

class ImageViewViewHolder(private val binding: ItemImagePreviewBinding) :
    MediaHolder(binding.root) {
    private var listener: ContentClickListener? = null

    private val videoProgressHandler = Handler(Looper.getMainLooper())

    private val showProcessRunnable = Runnable {
        binding.videoProgress.animateVisible()
    }

    override fun bind(model: MediaModel, listener: MediaPreviewAdapter.Listener) {
        initMediaData(model)
    }

    private fun initMediaData(media: MediaModel) = with(binding) {
        videoProgressHandler.postDelayed(showProcessRunnable, 0)

        val uri = media.uri ?: media.url?.let { Uri.parse(it) }

        if (uri != null) {
            MediaViewer.imageUriResolver.resolve(uri) {
                if (it != null) {
                    loadUri(this, it, this@ImageViewViewHolder)
                } else {
                    clear()
                }
            }
        } else {
            clear()
        }

        photoView.setOnClickListener { listener?.onClick() }
    }

    private fun clear() = with(binding) {
        photoView.clear()
        photoView.setImageDrawable(null)
    }

    private fun loadUri(
        itemImagePreviewBinding: ItemImagePreviewBinding,
        it: Uri?,
        imageViewViewHolder: ImageViewViewHolder
    ) {
        itemImagePreviewBinding.photoView.load(it) {
            listener { _, _ ->
                imageViewViewHolder.hideProgress()
            }
        }
    }

    private fun hideProgress(animate: Boolean = true) {
        videoProgressHandler.removeCallbacks(showProcessRunnable)

        if (animate) {
            binding.videoProgress.animateGone()
        } else {
            binding.videoProgress.isVisible = false
        }
    }
}