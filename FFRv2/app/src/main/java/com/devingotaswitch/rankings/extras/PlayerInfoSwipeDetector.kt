package com.devingotaswitch.rankings.extras

import android.view.MotionEvent
import android.view.View
import com.devingotaswitch.rankings.PlayerInfo
import kotlin.math.abs

class PlayerInfoSwipeDetector(private val hold: PlayerInfo) : View.OnTouchListener {
    private var downX = 0f
    private fun onRightToLeftSwipe() {
        hold.swipeRightToLeft()
    }

    private fun onLeftToRightSwipe() {
        hold.swipeLeftToRight()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                run { downX = event.x }
                run {
                    val upX = event.x
                    val deltaX = downX - upX
                    if (abs(deltaX) > MIN_DISTANCE) {
                        if (deltaX < 0) {
                            this.onLeftToRightSwipe()
                            return true
                        }
                        if (deltaX > 0) {
                            this.onRightToLeftSwipe()
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val upX = event.x
                val deltaX = downX - upX
                if (abs(deltaX) > MIN_DISTANCE) {
                    if (deltaX < 0) {
                        onLeftToRightSwipe()
                        return true
                    }
                    if (deltaX > 0) {
                        onRightToLeftSwipe()
                        return true
                    }
                }
            }
        }
        return false
    }

    companion object {
        private const val MIN_DISTANCE = 450
    }
}