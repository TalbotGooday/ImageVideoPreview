package com.goodayapps.widget.media_viewer.utils.widgets.swipe_back

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.goodayapps.widget.media_viewer.R

abstract class SwipeBackActivity : AppCompatActivity(), SwipeBackLayout.SwipeBackListener {

	companion object {
		private val DEFAULT_DRAG_EDGE = SwipeBackLayout.DragEdge.LEFT
	}

	private val swipeBackLayout by lazy {
		SwipeBackLayout(this).apply {
			setDragEdge(DEFAULT_DRAG_EDGE)
			setOnSwipeBackListener(this@SwipeBackActivity)
		}
	}

	private val ivShadow by lazy {
		ImageView(this@SwipeBackActivity).apply {
			setBackgroundColor(ContextCompat.getColor(this@SwipeBackActivity, R.color.hc_black_p50))
		}
	}

	private val container by lazy {
		RelativeLayout(this).apply {
			addView(ivShadow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
			addView(swipeBackLayout)
		}
	}

	override fun setContentView(layoutResID: Int) {
		super.setContentView(container)
		val view = LayoutInflater.from(this).inflate(layoutResID, null)
		swipeBackLayout.addView(view)
	}

	fun setEnableSwipe(enableSwipe: Boolean) {
		swipeBackLayout.setEnablePullToBack(enableSwipe)
	}

	fun setDragEdge(dragEdge: SwipeBackLayout.DragEdge) {
		swipeBackLayout.setDragEdge(dragEdge)
	}

	fun setDragDirectMode(dragDirectMode: SwipeBackLayout.DragDirectMode) {
		swipeBackLayout.setDragDirectMode(dragDirectMode)
	}

	override fun onViewPositionChanged(fractionAnchor: Float, fractionScreen: Float) {
		ivShadow.alpha = 1 - fractionScreen

		if (fractionScreen > 0) {
			onSwipeDetected()
		}
	}

	open fun onSwipeDetected() {}
}
