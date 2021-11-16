package com.goodayapps.widget.media_viewer.screens.main

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.consumer.TranslucentSlidingConsumer
import com.billy.android.swipe.listener.SimpleSwipeListener
import com.goodayapps.widget.media_viewer.R
import com.goodayapps.widget.media_viewer.core.AppPlayerView
import com.goodayapps.widget.media_viewer.core.MediaViewer
import com.goodayapps.widget.media_viewer.databinding.ActivityImageViewerBinding
import com.goodayapps.widget.media_viewer.models.PreviewerModel
import com.goodayapps.widget.media_viewer.screens.base.ContentClickListener
import com.goodayapps.widget.media_viewer.screens.main.adapters.MediaPreviewAdapter
import com.goodayapps.widget.media_viewer.utils.animateGone
import com.goodayapps.widget.media_viewer.utils.animateVisible
import com.goodayapps.widget.media_viewer.utils.colorAttribute
import com.goodayapps.widget.media_viewer.utils.pageScrollStateChanges
import com.goodayapps.widget.media_viewer.utils.setStatusBarColor
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

open class MediaViewerActivity : AppCompatActivity(), ContentClickListener {
    companion object {
        const val MODEL = "model"
        private const val ANIM_DURATION: Long = 150

        @JvmStatic
        fun open(context: Context, model: PreviewerModel, customOptions: Bundle? = null) {
            val intent = Intent(context, MediaViewerActivity::class.java)

            intent.putExtra(MODEL, model)

            val options = customOptions ?: ActivityOptions.makeCustomAnimation(
                context,
                R.anim.anim_mv_fade_in,
                R.anim.anim_mv_fade_out
            ).toBundle()

            context.startActivity(intent, options)
        }
    }

    private var areBarsVisible = true
    private val appPlayerView: AppPlayerView by lazy { MediaViewer.appPlayerView(layoutInflater) }
    private val viewModel: MediaViewerViewModel by viewModels { MediaViewerViewModel.Factory(this) }

    private var url: String? = null

    private var imagesCount = 1

    private var model: PreviewerModel? = null

    private lateinit var binding: ActivityImageViewerBinding

    private var mediaAdapter: MediaPreviewAdapter = createMediaAdapter()

    private fun createMediaAdapter(): MediaPreviewAdapter {
        return MediaPreviewAdapter(object : MediaPreviewAdapter.Listener {

        })
    }

    private var oldSystemUiVisibility: Int = -1

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            closeFullscreen()
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            openFullscreen()
        }
    }

    override fun onStart() {
        super.onStart()
        appPlayerView.onStart(viewModel.getPlayer(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MediaViewer.init(this)

        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        window.navigationBarColor = colorAttribute(R.attr.colorPrimary)

        with(binding) {
            photoViewPager.setOnClickListener { toggleBarsVisibility() }

            actionBack.setOnClickListener { onBackPressed() }

            actionShare.setOnClickListener {
                shareImage(url)
            }

            viewModel.viewEffects()
                .onEach { viewEffect ->
                    when (viewEffect) {
                        is PlayerEffect -> appPlayerView.renderEffect(viewEffect)
                        is PlayerEvent.SettledOnPage -> setCurrentItem(viewEffect.realPage)
                    }
                }
                .launchIn(lifecycleScope)
            viewModel.viewStates()
                .onEach { viewStates ->
                    setCurrentItem(0)
                }
                .launchIn(lifecycleScope)

            merge(
                // Idling on a page after a scroll is a signal to try and change player playlist positions
                binding.photoViewPager.pageScrollStateChanges()
                    .filter { state ->
                        state == ViewPager2.SCROLL_STATE_IDLE
                    }
                    .map {
                        PlayerEvent.SettledOnPage(
                            binding.photoViewPager.currentItem,
                            mediaAdapter.getRelativeVideoPosition(binding.photoViewPager.currentItem)
                        )
                    },
                // Taps on the player are signals to either play or pause the player, with animation side effects
                appPlayerView.taps().map { PlayerEvent.TappedPlayer }
            )
                .onEach(viewModel::processEvent)
                .launchIn(lifecycleScope)

            initSwipe()
        }

        initActivityWithData()
    }

    private fun initSwipe() = with(binding) {
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
                        colorAttribute(R.attr.colorPrimary),
                        (255 * alpha).toInt()
                    )
                    setStatusBarColor(alphaComponent)

                    window.navigationBarColor = alphaComponent
                }

                override fun onSwipeOpened(
                    wrapper: SmartSwipeWrapper?,
                    consumer: SwipeConsumer?,
                    direction: Int
                ) {
                    finish()
                }
            })
    }

    private fun setCurrentItem(position: Int) {
        mediaAdapter.bindData(position)
        mediaAdapter.attachPlayerView(appPlayerView, position)
    }

    override fun onStop() {
        super.onStop()
        appPlayerView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaViewer.release()
    }

    override fun onClick() {
        toggleBarsVisibility()
    }

    override fun setTopBottomBarsVisible(visible: Boolean) {
        areBarsVisible = visible.not()
        toggleBarsVisibility()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(0, R.anim.anim_mv_fade_out)
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
        val mediaAdapter1 = mediaAdapter
        imagesCount = model.previewData?.size ?: 1

        photoViewPager.apply {
            adapter = mediaAdapter1
            isSaveEnabled = true
        }

        model.previewData?.let {
            mediaAdapter1.swapData(it)
            viewModel.setVideoData(this@MediaViewerActivity, it)
        }

        photoViewPager.overScrollMode = if (model.previewData.isNullOrEmpty()) {
            View.OVER_SCROLL_NEVER
        } else {
            View.OVER_SCROLL_ALWAYS
        }

        if (model.showAuthor.not()) {
            binding.senderName.isVisible = false
        } else {
            binding.senderName.text = model.author
        }

        binding.bottomBar.isVisible = model.showAuthor

        binding.numeration.isVisible = model.showNumeration
    }

    private fun shareImage(url: String?) {
        // no-op
    }

    private fun openFullscreen() {
        oldSystemUiVisibility = window.decorView.systemUiVisibility
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun closeFullscreen() {
        window.decorView.systemUiVisibility = if (oldSystemUiVisibility != -1) {
            oldSystemUiVisibility
        } else {
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun setImageCounter(position: Int) {
        val countText = "${position + 1}/$imagesCount"

        binding.numeration.text = countText
    }
}
