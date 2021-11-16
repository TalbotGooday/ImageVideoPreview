package com.goodayapps.widget.media_viewer.utils.widgets.swipe_back

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout.LayoutParams
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.goodayapps.widget.media_viewer.R

abstract class SwipeBackFragment(@LayoutRes private val contentLayoutId: Int) : Fragment(),
	SwipeBackLayout.SwipeBackListener {

	companion object {
		private val DEFAULT_DRAG_EDGE = SwipeBackLayout.DragEdge.LEFT
	}

	private val swipeBackLayout by lazy {
		SwipeBackLayout(this@SwipeBackFragment.requireContext()).apply {
			setDragEdge(DEFAULT_DRAG_EDGE)
			setOnSwipeBackListener(this@SwipeBackFragment)
		}
	}

	private val ivShadow by lazy {
		ImageView(this@SwipeBackFragment.context).apply {
			setBackgroundColor(ContextCompat.getColor(this@SwipeBackFragment.requireContext(), R.color.hc_black_p50))
		}
	}

	private val container by lazy {

	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (contentLayoutId != 0) {
			inflater.inflate(contentLayoutId, null, false)
		} else {
			null
		}?.also { swipeBackLayout.addView(it) }

		return FrameLayout(this@SwipeBackFragment.requireContext()).apply {
			addView(ivShadow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
			addView(swipeBackLayout)
		}
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
	}

	override fun finish(dragEdge: SwipeBackLayout.DragEdge) {
		onFinish(dragEdge)
	}

	open fun onFinish(dragEdge: SwipeBackLayout.DragEdge) {}
}
