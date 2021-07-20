package com.goodayapps.widget.media_viewer.screens.main.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.screens.base.FragmentLifecycle
import com.goodayapps.widget.media_viewer.screens.image.ImageViewFragment
import com.goodayapps.widget.media_viewer.screens.video.VideoViewFragment

class MediaPreviewAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    private var data: MutableList<MediaModel> = mutableListOf()
    private var fragments: MutableMap<Int, Fragment> = mutableMapOf()

    private var currentPosition: Int = 0

    override fun getItemCount(): Int {
        return data.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = data[position]

        return if (item.isVideo) {
            VideoViewFragment.new(item)
        } else {
            ImageViewFragment.new(item)
        }
    }

    fun onPageSelected(position: Int = currentPosition) {
        if (currentPosition != position) {
            onPause()
        }

        (fragments[position] as? FragmentLifecycle)?.onResumeFragment()

        currentPosition = position
    }

    fun onPause() {
        (fragments[currentPosition] as? FragmentLifecycle)?.onPauseFragment()
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
}