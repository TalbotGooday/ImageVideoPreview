package com.goodayapps.widget.media_viewer.screens.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import coil.load
import com.goodayapps.widget.media_viewer.databinding.FragmentImagePreviewBinding
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.screens.base.FragmentLifecycle

class ImageViewFragment : Fragment(), FragmentLifecycle {
    companion object {
        fun new(item: MediaModel) = ImageViewFragment().apply {
            arguments = bundleOf(
                "media" to item
            )
        }
    }

    private var _binding: FragmentImagePreviewBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        val media = arguments?.getParcelable<MediaModel>("media")

        if (media == null) {
            // suka blyat
            showError()
            return
        }

        initMediaData(media)
    }

    private fun initMediaData(media: MediaModel) = with(binding.root as ImageView) {
        if (media.url != null) {
            load(media.url)
        }

        if (media.uri != null) {
            load(media.uri)
        }
    }

    private fun showError() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
    }
}