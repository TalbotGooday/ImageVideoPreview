package com.goodayapps.widget.screens.main

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.consumer.TranslucentSlidingConsumer
import com.billy.android.swipe.listener.SimpleSwipeListener
import com.goodayapps.widget.R.anim
import com.goodayapps.widget.R.attr
import com.goodayapps.widget.databinding.ActivityHcImageViewerBinding
import com.goodayapps.widget.models.PreviewerModel
import com.goodayapps.widget.screens.main.adapters.MediaPreviewAdapter
import com.goodayapps.widget.utils.animateGone
import com.goodayapps.widget.utils.animateVisible
import com.goodayapps.widget.utils.colorAttribute
import com.goodayapps.widget.utils.setStatusBarColor

open class MediaPreviewerActivity : AppCompatActivity() {
    companion object {
        const val MODEL = "model"
        private const val ANIM_DURATION: Long = 250

        @JvmStatic
        fun open(context: Context, model: PreviewerModel) {
            val intent = Intent(context, MediaPreviewerActivity::class.java)

            intent.putExtra(MODEL, model)

            val options = ActivityOptions.makeCustomAnimation(
                context,
                anim.anim_hc_fade_in,
                anim.anim_hc_fade_out
            )
            context.startActivity(intent, options.toBundle())
        }
    }

    private var areBarsVisible = true

    private var url: String? = null

    private var imagesCount = 1

    private var model: PreviewerModel? = null

    private lateinit var binding: ActivityHcImageViewerBinding

    private var mediaAdapter: MediaPreviewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHcImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = colorAttribute(attr.colorPrimary)
        }

        with(binding) {
            photoViewPager.setOnClickListener { toggleBarsVisibility() }

            actionBack.setOnClickListener { onBackPressed() }

            actionShare.setOnClickListener {
                shareImage(url)
            }

            SmartSwipe.wrap(photoViewPager)
                .removeAllConsumers()
                .addConsumer(TranslucentSlidingConsumer())
                .enableVertical()
                .addListener(object : SimpleSwipeListener() {
                    override fun onSwipeProcess(
                        wrapper: SmartSwipeWrapper?,
                        consumer: SwipeConsumer?,
                        direction: Int,
                        settling: Boolean,
                        progress: Float
                    ) {
                        val alpha = 1 - progress

                        container.alpha = alpha

                        val alphaComponent = ColorUtils.setAlphaComponent(
                            colorAttribute(attr.colorPrimary),
                            (255 * alpha).toInt()
                        )

                        setStatusBarColor(alphaComponent)

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            window.navigationBarColor = alphaComponent
                        }
                    }

                    override fun onSwipeOpened(
                        wrapper: SmartSwipeWrapper?,
                        consumer: SwipeConsumer?,
                        direction: Int
                    ) {
                        finish()
                    }
                })

            photoViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setImageCounter(position)
                    mediaAdapter?.onPageSelected(position)
                }
            })
        }

        initActivityWithData()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    private fun initActivityWithData() {
        val extras = intent.extras ?: return

        val model = extras.getParcelable<PreviewerModel>(MODEL)

        if (model == null) {
            finish()
            return
        }

        this.model = model

        initWithData(model)
    }

    private fun toggleBarsVisibility() {
        val isFooterVisible = model?.showAuthor == true

        if (areBarsVisible) {
            binding.toolbar.animateGone(ANIM_DURATION)
            if (isFooterVisible) {
                binding.bottomBar.animateGone(ANIM_DURATION)
            }
        } else {
            binding.toolbar.animateVisible(ANIM_DURATION)
            if (isFooterVisible) {
                binding.bottomBar.animateVisible(ANIM_DURATION)
            }
        }

        areBarsVisible = !areBarsVisible
    }

    private fun initWithData(model: PreviewerModel) = with(binding) {
        if (mediaAdapter == null) {
            mediaAdapter = MediaPreviewAdapter(this@MediaPreviewerActivity)
        }

        val mediaAdapter1 = mediaAdapter ?: return@with
        imagesCount = model.previewData?.size ?: 1

        photoViewPager.apply {
            adapter = mediaAdapter1
            isSaveEnabled = true
        }

        model.previewData?.let { mediaAdapter1.swapData(it) }

        val index = mediaAdapter1.indexOfCurrent(model.current)

        setImageCounter(index)

        photoViewPager.setCurrentItem(index, false)

        if (model.showAuthor.not()) {
            binding.senderName.isVisible = false
        } else {
            binding.senderName.text = model.author
        }

        binding.bottomBar.isVisible = model.showAuthor

        binding.numeration.isVisible = model.showNumeration
    }

    private fun shareImage(url: String?) {
        var fileName = url?.substringAfterLast("/")

        if (fileName.isNullOrBlank()) {
            fileName = "${System.currentTimeMillis()}.jpeg"
        }
    }

    private fun setImageCounter(position: Int) {
        val countText = "${position + 1}/$imagesCount"

        binding.numeration.text = countText
    }
}
