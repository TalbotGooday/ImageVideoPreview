package com.goodayapps.widget.media_viewer.utils.widgets.swipe_back

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class SwipeBackLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

	companion object {
		private const val TAG = "HCSwipeBackLayout"
		private const val AUTO_FINISHED_SPEED_LIMIT = 2000.0
		private const val BACK_FACTOR = 0.5f
	}

	private var dragDirectMode = DragDirectMode.EDGE

	var dragEdge = DragEdge.TOP
		private set

	private val viewDragHelper: ViewDragHelper

	private var target: View? = null

	private var scrollChild: View? = null

	private var verticalDragRange = 0

	private var horizontalDragRange = 0

	private var draggingState = 0

	private var draggingOffset: Int = 0

	/**
	 * Whether allow to pull this layout.
	 */
	private var enablePullToBack = true

	/**
	 * the anchor of calling finish.
	 */
	private var finishAnchor = 0f

	private var enableFlingBack = true

	private var swipeBackListener: SwipeBackListener? = null

	private var lastY = 0f
	private var newY = 0f
	private var offsetY = 0f

	private var lastX = 0f
	private var newX = 0f
	private var offsetX = 0f

	private val dragRange: Int
		get() {
			return when (dragEdge) {
				DragEdge.TOP, DragEdge.BOTTOM -> verticalDragRange
				DragEdge.LEFT, DragEdge.RIGHT -> horizontalDragRange
			}
		}

	init {
		viewDragHelper = ViewDragHelper.create(this, 1.0f, ViewDragHelperCallBack())
		cheDraggable()
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
		val width = measuredWidth
		val height = measuredHeight
		if (childCount == 0) return

		val child = getChildAt(0)

		val childWidth = width - paddingLeft - paddingRight
		val childHeight = height - paddingTop - paddingBottom
		val childLeft = paddingLeft
		val childTop = paddingTop
		val childRight = childLeft + childWidth
		val childBottom = childTop + childHeight
		child.layout(childLeft, childTop, childRight, childBottom)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		if (childCount > 1) {
			throw IllegalStateException("SwipeBackLayout must contains only one direct child.")
		}

		if (childCount > 0) {
			val measureWidth = MeasureSpec.makeMeasureSpec(measuredWidth - paddingLeft - paddingRight, MeasureSpec.EXACTLY)
			val measureHeight = MeasureSpec.makeMeasureSpec(measuredHeight - paddingTop - paddingBottom, MeasureSpec.EXACTLY)
			getChildAt(0).measure(measureWidth, measureHeight)
		}
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		verticalDragRange = h
		horizontalDragRange = w

		finishAnchor = when (dragEdge) {
			DragEdge.TOP, DragEdge.BOTTOM -> if (finishAnchor > 0) finishAnchor else verticalDragRange * BACK_FACTOR
			DragEdge.LEFT, DragEdge.RIGHT -> if (finishAnchor > 0) finishAnchor else horizontalDragRange * BACK_FACTOR
		}
	}

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		var handled = false
		ensureTarget()
		if (isEnabled) {
			handled = viewDragHelper.shouldInterceptTouchEvent(ev)
		} else {
			viewDragHelper.cancel()
		}
		return if (!handled) super.onInterceptTouchEvent(ev) else handled
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		viewDragHelper.processTouchEvent(event)
		return true
	}

	override fun computeScroll() {
		if (viewDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this)
		}
	}

	fun setDragEdge(dragEdge: DragEdge) {
		this.dragEdge = dragEdge
	}

	fun setDragDirectMode(dragDirectMode: DragDirectMode) {
		this.dragDirectMode = dragDirectMode
		if (dragDirectMode == DragDirectMode.VERTICAL) {
			this.dragEdge = DragEdge.TOP
		} else if (dragDirectMode == DragDirectMode.HORIZONTAL) {
			this.dragEdge = DragEdge.LEFT
		}
	}

	/**
	 * Set the anchor of calling finish.
	 *
	 * @param offset
	 */
	fun setFinishAnchor(offset: Float) {
		finishAnchor = offset
	}

	/**
	 * Whether allow to finish activity by fling the layout.
	 *
	 * @param b
	 */
	fun setEnableFlingBack(b: Boolean) {
		enableFlingBack = b
	}

	fun setScrollChild(view: View) {
		scrollChild = view
	}

	@Deprecated("")
	fun setOnPullToBackListener(listener: SwipeBackListener) {
		swipeBackListener = listener
	}

	fun setOnSwipeBackListener(listener: SwipeBackListener) {
		swipeBackListener = listener
	}


	fun setEnablePullToBack(b: Boolean) {
		enablePullToBack = b
	}

	fun canChildScrollUp(): Boolean {
		return scrollChild?.canScrollVertically(-1) ?: false
	}

	fun canChildScrollDown(): Boolean {
		return scrollChild?.canScrollVertically(1) ?: false
	}

	private fun cheDraggable() {
		setOnTouchListener { _, motionEvent ->
			if (motionEvent.action == MotionEvent.ACTION_DOWN) {
				lastY = motionEvent.rawY
				lastX = motionEvent.rawX
			} else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
				newY = motionEvent.rawY
				lastX = motionEvent.rawX

				offsetY = Math.abs(newY - lastY)
				lastY = newY

				offsetX = Math.abs(newX - lastX)
				lastX = newX

				when (dragEdge) {
					DragEdge.TOP, DragEdge.BOTTOM -> {
						setEnablePullToBack(offsetY > offsetX)
						setEnablePullToBack(offsetY < offsetX)
					}
					DragEdge.LEFT, DragEdge.RIGHT -> setEnablePullToBack(offsetY < offsetX)
				}
			}

			false
		}
	}

	private fun ensureTarget() {
		if (target == null) {
			if (childCount > 1) {
				throw IllegalStateException("SwipeBackLayout must contains only one direct child")
			}
			target = getChildAt(0)

			if (scrollChild == null && target != null) {
				if (target is ViewGroup) {
					findScrollView((target as ViewGroup?)!!)
				} else {
					scrollChild = target
				}

			}
		}
	}

	/**
	 * Find out the scrollable child view from a ViewGroup.
	 *
	 * @param viewGroup
	 */
	private fun findScrollView(viewGroup: ViewGroup) {
		scrollChild = viewGroup
		if (viewGroup.childCount > 0) {
			val count = viewGroup.childCount
			var child: View
			for (i in 0 until count) {
				child = viewGroup.getChildAt(i)
				if (child is AbsListView || child is ScrollView || child is ViewPager || child is WebView) {
					scrollChild = child
					return
				}
			}
		}
	}

	private fun canChildScrollRight(): Boolean {
		return scrollChild?.canScrollHorizontally(-1) ?: false
	}

	private fun canChildScrollLeft(): Boolean {
		return scrollChild?.canScrollHorizontally(1) ?: false
	}

	private fun finish() {
		if (swipeBackListener == null) {
			val act = context as Activity
			act.finish()
			act.overridePendingTransition(0, android.R.anim.fade_out)
		} else {
			swipeBackListener?.finish(dragEdge)
		}
	}

	private inner class ViewDragHelperCallBack : ViewDragHelper.Callback() {

		override fun tryCaptureView(child: View, pointerId: Int): Boolean {
			return child === target && enablePullToBack
		}

		override fun getViewVerticalDragRange(child: View): Int {
			return verticalDragRange
		}

		override fun getViewHorizontalDragRange(child: View): Int {
			return horizontalDragRange
		}

		override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {

			var result = 0

			if (dragDirectMode == DragDirectMode.VERTICAL) {
				if (!canChildScrollUp() && top > 0) {
					dragEdge = DragEdge.TOP
				} else if (!canChildScrollDown() && top < 0) {
					dragEdge = DragEdge.BOTTOM
				}
			}

			if (dragEdge == DragEdge.TOP && !canChildScrollUp() && top > 0) {
				val topBound = paddingTop
				val bottomBound = verticalDragRange
				result = top.coerceAtLeast(topBound).coerceAtMost(bottomBound)
			} else if (dragEdge == DragEdge.BOTTOM && !canChildScrollDown() && top < 0) {
				val topBound = -verticalDragRange
				val bottomBound = paddingTop
				result = top.coerceAtLeast(topBound).coerceAtMost(bottomBound)
			}

			return result
		}

		override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {

			var result = 0

			if (dragDirectMode == DragDirectMode.HORIZONTAL) {
				if (!canChildScrollRight() && left > 0) {
					dragEdge = DragEdge.LEFT
				} else if (!canChildScrollLeft() && left < 0) {
					dragEdge = DragEdge.RIGHT
				}
			}

			if (dragEdge == DragEdge.LEFT && !canChildScrollRight() && left > 0) {
				val leftBound = paddingLeft
				val rightBound = horizontalDragRange
				result = left.coerceAtLeast(leftBound).coerceAtMost(rightBound)
			} else if (dragEdge == DragEdge.RIGHT && !canChildScrollLeft() && left < 0) {
				val leftBound = -horizontalDragRange
				val rightBound = paddingLeft
				result = left.coerceAtLeast(leftBound).coerceAtMost(rightBound)
			}

			return result
		}

		override fun onViewDragStateChanged(state: Int) {
			if (state == draggingState) return

			if ((draggingState == ViewDragHelper.STATE_DRAGGING || draggingState == ViewDragHelper.STATE_SETTLING) && state == ViewDragHelper.STATE_IDLE) {
				// the view stopped from moving.
				if (draggingOffset == dragRange) {
					finish()
				}
			}

			draggingState = state
		}

		override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
			draggingOffset = when (dragEdge) {
				DragEdge.TOP, DragEdge.BOTTOM -> abs(top)
				DragEdge.LEFT, DragEdge.RIGHT -> abs(left)
			}

			//The proportion of the sliding.
			var fractionAnchor = draggingOffset.toFloat() / finishAnchor
			if (fractionAnchor >= 1) fractionAnchor = 1f

			var fractionScreen = draggingOffset.toFloat() / dragRange.toFloat()
			if (fractionScreen >= 1) fractionScreen = 1f

			swipeBackListener?.onViewPositionChanged(fractionAnchor, fractionScreen)
		}

		override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
			if (draggingOffset == 0) return

			if (draggingOffset == dragRange) return

			var isBack = false

			if (enableFlingBack && backBySpeed(xvel, yvel)) {
				isBack = !canChildScrollUp()
			} else if (draggingOffset >= finishAnchor) {
				isBack = true
			} else if (draggingOffset < finishAnchor) {
				isBack = false
			}

			val finalLeft: Int
			val finalTop: Int
			when (dragEdge) {
				DragEdge.LEFT -> {
					finalLeft = if (isBack) horizontalDragRange else 0
					smoothScrollToX(finalLeft)
				}
				DragEdge.RIGHT -> {
					finalLeft = if (isBack) -horizontalDragRange else 0
					smoothScrollToX(finalLeft)
				}
				DragEdge.TOP -> {
					finalTop = if (isBack) verticalDragRange else 0
					smoothScrollToY(finalTop)
				}
				DragEdge.BOTTOM -> {
					finalTop = if (isBack) -verticalDragRange else 0
					smoothScrollToY(finalTop)
				}
			}

		}
	}

	private fun backBySpeed(xvel: Float, yvel: Float): Boolean {
		when (dragEdge) {
			DragEdge.TOP, DragEdge.BOTTOM -> if (abs(yvel) > abs(xvel) && abs(yvel) > AUTO_FINISHED_SPEED_LIMIT) {
				return if (dragEdge == DragEdge.TOP) !canChildScrollUp() else !canChildScrollDown()
			}
			DragEdge.LEFT, DragEdge.RIGHT -> if (abs(xvel) > abs(yvel) && abs(xvel) > AUTO_FINISHED_SPEED_LIMIT) {
				return if (dragEdge == DragEdge.LEFT) !canChildScrollLeft() else !canChildScrollRight()
			}
		}
		return false
	}

	private fun smoothScrollToX(finalLeft: Int) {
		if (viewDragHelper.settleCapturedViewAt(finalLeft, 0)) {
			ViewCompat.postInvalidateOnAnimation(this@SwipeBackLayout)
		}
	}

	private fun smoothScrollToY(finalTop: Int) {
		if (viewDragHelper.settleCapturedViewAt(0, finalTop)) {
			ViewCompat.postInvalidateOnAnimation(this@SwipeBackLayout)
		}
	}

	interface SwipeBackListener {

		/**
		 * Return scrolled fraction of the layout.
		 *
		 * @param fractionAnchor relative to the anchor.
		 * @param fractionScreen relative to the screen.
		 */
		fun onViewPositionChanged(fractionAnchor: Float, fractionScreen: Float)

		fun finish(dragEdge: DragEdge)
	}

	enum class DragDirectMode {
		EDGE,
		VERTICAL,
		HORIZONTAL
	}

	enum class DragEdge {
		LEFT,
		TOP,
		RIGHT,
		BOTTOM
	}

}
