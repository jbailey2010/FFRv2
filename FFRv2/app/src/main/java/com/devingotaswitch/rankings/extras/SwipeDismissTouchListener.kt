package com.devingotaswitch.rankings.extras

import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.SystemClock
import android.view.*
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.abs

class SwipeDismissTouchListener(listView: RecyclerView, doAnimate: Boolean, callbacks: DismissCallbacks,
                                onClickListener: RecyclerViewAdapter.OnItemClickListener,
                                onLongClickListener: RecyclerViewAdapter.OnItemLongClickListener) : View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private val mSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mAnimationTime: Long

    // Fixed properties
    private val mListView: RecyclerView
    private val mCallbacks: DismissCallbacks
    private var mViewWidth = 1 // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private val mPendingDismisses: MutableList<PendingDismissData> = ArrayList()
    private var mDismissAnimationRefCount = 0
    private var mDownX = 0f
    private var mDownY = 0f
    private var mSwiping = false
    private var mSwipingSlop = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var mDownPosition = 0
    private var mDownView: View? = null
    private var mPaused = false
    private val mDoDismiss: Boolean
    private val gestureDetector: GestureDetector
    private val mOnClickListener: RecyclerViewAdapter.OnItemClickListener
    private val mOnLongClickListener: RecyclerViewAdapter.OnItemLongClickListener

    interface DismissCallbacks {
        /**
         * Called to determine whether the given position can be dismissed.
         */
        fun canDismiss(view: View?): Boolean

        /**
         * Called when the user has indicated they she would like to dismiss one or more list item
         * positions.
         *
         * @param listView               The originating [ListView].
         * @param reverseSortedPositions An array of positions to dismiss, sorted in descending
         * order for convenience.
         */
        fun onDismiss(listView: RecyclerView?, reverseSortedPositions: IntArray?, rightDismiss: Boolean)
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given list view.
     *
     * @param listView  The list view whose items should be dismissable.
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     * dismiss one or more list items.
     */
    constructor(listView: RecyclerView, callbacks: DismissCallbacks,
                onClickListener: RecyclerViewAdapter.OnItemClickListener,
                onLongClickListener: RecyclerViewAdapter.OnItemLongClickListener) : this(listView,
            true, callbacks, onClickListener, onLongClickListener)

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    fun setEnabled(enabled: Boolean) {
        mPaused = !enabled
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (mViewWidth < 2) {
            mViewWidth = mListView.width
        }
        if (gestureDetector.onTouchEvent(motionEvent)) {
            val layoutManager = mListView.layoutManager as LinearLayoutManager?
            mOnClickListener.onItemClick(view, layoutManager!!.findFirstVisibleItemPosition())
            return true
        }
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (mPaused) {
                    return false
                }

                // TODO: ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                val rect = Rect()
                val childCount = mListView.childCount
                val listViewCoords = IntArray(2)
                mListView.getLocationOnScreen(listViewCoords)
                val x = motionEvent.rawX.toInt() - listViewCoords[0]
                val y = motionEvent.rawY.toInt() - listViewCoords[1]
                var child: View
                var i = 0
                while (i < childCount) {
                    child = mListView.getChildAt(i)
                    child.getHitRect(rect)
                    if (rect.contains(x, y)) {
                        mDownView = child
                        break
                    }
                    i++
                }
                if (mDownView != null) {
                    mDownX = motionEvent.rawX
                    mDownY = motionEvent.rawY
                    mDownPosition = mListView.getChildLayoutPosition(mDownView!!)
                    if (mCallbacks.canDismiss(mDownView)) {
                        mVelocityTracker = VelocityTracker.obtain()
                        mVelocityTracker!!.addMovement(motionEvent)
                    } else {
                        mDownView = null
                    }
                }
                return false
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mVelocityTracker != null) {
                    if (mDownView != null && mSwiping) {
                        // cancel
                        cancelSwipe()
                    }
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                    mDownX = 0f
                    mDownY = 0f
                    mDownView = null
                    mDownPosition = ListView.INVALID_POSITION
                    mSwiping = false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mVelocityTracker != null) {
                    val deltaX = motionEvent.rawX - mDownX
                    mVelocityTracker!!.addMovement(motionEvent)
                    mVelocityTracker!!.computeCurrentVelocity(1000)
                    val velocityX = mVelocityTracker!!.xVelocity
                    val absVelocityX = abs(velocityX)
                    val absVelocityY = abs(mVelocityTracker!!.yVelocity)
                    var dismiss = false
                    var dismissRight = false
                    if (abs(deltaX) > mViewWidth / 2.0 && mSwiping) {
                        dismiss = true
                        dismissRight = deltaX > 0
                    } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity && absVelocityY < absVelocityX && mSwiping) {
                        // dismiss only if flinging in the same direction as dragging
                        dismiss = velocityX < 0 == deltaX < 0
                        dismissRight = mVelocityTracker!!.xVelocity > 0
                    }
                    if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                        // dismiss
                        val downView = mDownView // mDownView gets null'd before animation ends
                        val downPosition = mDownPosition
                        ++mDismissAnimationRefCount
                        if (mDoDismiss) {
                            mDownView!!.animate()
                                    .translationX(if (dismissRight) mViewWidth.toFloat() else -mViewWidth.toFloat())
                                    .alpha(0f)
                                    .setDuration(mAnimationTime)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            performDismiss(downView, downPosition, deltaX > 0)
                                        }
                                    })
                        } else {
                            val positions = IntArray(1)
                            positions[0] = downPosition
                            mCallbacks.onDismiss(mListView, positions, deltaX > 0)
                            cancelSwipe()
                        }
                    } else {
                        // cancel
                        cancelSwipe()
                    }
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                    mDownX = 0f
                    mDownY = 0f
                    mDownView = null
                    mDownPosition = ListView.INVALID_POSITION
                    mSwiping = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mVelocityTracker != null && !mPaused) {
                    mVelocityTracker!!.addMovement(motionEvent)
                    val deltaX = motionEvent.rawX - mDownX
                    val deltaY = motionEvent.rawY - mDownY
                    if (abs(deltaX) > mSlop && abs(deltaY) < abs(deltaX) / 2) {
                        mSwiping = true
                        mSwipingSlop = if (deltaX > 0) mSlop else -mSlop
                        mListView.requestDisallowInterceptTouchEvent(true)

                        // Cancel ListView's touch (un-highlighting the item)
                        val cancelEvent = MotionEvent.obtain(motionEvent)
                        cancelEvent.action = MotionEvent.ACTION_CANCEL or
                                (motionEvent.actionIndex
                                        shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                        mListView.onTouchEvent(cancelEvent)
                        cancelEvent.recycle()
                    }
                    if (mSwiping) {
                        mDownView!!.translationX = deltaX - mSwipingSlop
                        if (mDoDismiss) {
                            mDownView!!.alpha = 0f.coerceAtLeast(
                                    1f.coerceAtMost(
                                    1f - 2f * abs(deltaX) / mViewWidth))
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun cancelSwipe() {
        mDownView!!.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(mAnimationTime)
                .setListener(null)
    }

    internal class PendingDismissData(val position: Int, val view: View?) : Comparable<PendingDismissData> {
        override fun compareTo(other: PendingDismissData): Int {
            // Sort by descending position
            return other.position - position
        }
    }

    private fun performDismiss(dismissView: View?, dismissPosition: Int, rightDismiss: Boolean) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.
        val lp = dismissView!!.layoutParams
        val originalHeight = dismissView.height
        val animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                --mDismissAnimationRefCount
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    mPendingDismisses.sort()
                    val dismissPositions = IntArray(mPendingDismisses.size)
                    for (i in mPendingDismisses.indices.reversed()) {
                        dismissPositions[i] = mPendingDismisses[i].position
                    }
                    mCallbacks.onDismiss(mListView, dismissPositions, rightDismiss)

                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                    // animation with a stale position
                    mDownPosition = ListView.INVALID_POSITION
                    var lp: ViewGroup.LayoutParams
                    for (pendingDismiss in mPendingDismisses) {
                        // Reset view presentation
                        pendingDismiss.view!!.alpha = 1f
                        pendingDismiss.view.translationX = 0f
                        lp = pendingDismiss.view.layoutParams
                        lp.height = originalHeight
                        pendingDismiss.view.layoutParams = lp
                    }

                    // Send a cancel event
                    val time = SystemClock.uptimeMillis()
                    val cancelEvent = MotionEvent.obtain(time, time,
                            MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
                    mListView.dispatchTouchEvent(cancelEvent)
                    mPendingDismisses.clear()
                }
            }
        })
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            lp.height = (valueAnimator.animatedValue as Int)
            dismissView.layoutParams = lp
        }
        mPendingDismisses.add(PendingDismissData(dismissPosition, dismissView))
        animator.start()
    }

    private inner class SingleTapConfirm : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(event: MotionEvent) {
            super.onLongPress(event)
            val layoutManager = mListView.layoutManager as LinearLayoutManager?
            mOnLongClickListener.onItemLongClick(mDownView, layoutManager!!.findFirstVisibleItemPosition())
        }
    }

    init {
        val vc = ViewConfiguration.get(listView.context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        mAnimationTime = listView.context.resources.getInteger(
                R.integer.config_shortAnimTime).toLong()
        mListView = listView
        mCallbacks = callbacks
        mDoDismiss = doAnimate
        gestureDetector = GestureDetector(listView.context, SingleTapConfirm())
        mOnClickListener = onClickListener
        mOnLongClickListener = onLongClickListener
    }
}