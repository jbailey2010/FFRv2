package com.devingotaswitch.utils

import android.app.Activity
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.andrognito.flashbar.anim.FlashAnim
import com.devingotaswitch.ffrv2.R

object FlashbarFactory {
    @JvmStatic
    fun generateTextOnlyFlashbar(activity: Activity?, title: String?, message: String?, gravity: Flashbar.Gravity?): Flashbar {
        return Flashbar.Builder(activity!!)
                .enableSwipeToDismiss()
                .gravity(gravity!!)
                .duration(Constants.FLASHBAR_DURATION)
                .title(title!!)
                .message(message!!)
                .backgroundColorRes(R.color.player_info_buttons)
                .exitAnimation(FlashAnim.with(activity)
                        .animateBar()
                        .duration(Constants.FLASHBAR_ANIMATION_EXIT_DURATION)
                        .accelerateDecelerate())
                .enterAnimation(FlashAnim.with(activity)
                        .animateBar()
                        .duration(Constants.FLASHBAR_ANIMATION_ENTER_DURATION)
                        .alpha()
                        .overshoot())
                .build()
    }

    @JvmStatic
    fun generateFlashbarWithUndo(activity: Activity, title: String, message: String, gravity: Flashbar.Gravity,
                                 undoListener: OnActionTapListener?): Flashbar {
        return Flashbar.Builder(activity)
                .enableSwipeToDismiss()
                .gravity(gravity)
                .duration(Constants.FLASHBAR_WITH_RESPONSE_DURATION)
                .title(title)
                .message(message)
                .backgroundColorRes(R.color.player_info_buttons)
                .exitAnimation(FlashAnim.with(activity)
                        .animateBar()
                        .duration(Constants.FLASHBAR_ANIMATION_EXIT_DURATION)
                        .accelerateDecelerate())
                .enterAnimation(FlashAnim.with(activity)
                        .animateBar()
                        .duration(Constants.FLASHBAR_ANIMATION_ENTER_DURATION)
                        .alpha()
                        .overshoot())
                .primaryActionText("UNDO")
                .primaryActionTapListener(undoListener!!)
                .build()
    }

    @JvmStatic
    fun generateInfiniteFlashbarWithAction(activity: Activity?, title: String, message: String, gravity: Flashbar.Gravity,
                                           listener: OnActionTapListener, actionText: String): Flashbar {
        return Flashbar.Builder(activity!!)
                .enableSwipeToDismiss()
                .gravity(gravity)
                .title(title)
                .message(message)
                .backgroundColorRes(R.color.player_info_buttons)
                .exitAnimation(FlashAnim.with(activity)
                        .animateBar()
                        .duration(Constants.FLASHBAR_ANIMATION_EXIT_DURATION)
                        .accelerateDecelerate())
                .enterAnimation(FlashAnim.with(activity)
                        .animateBar()
                        .duration(Constants.FLASHBAR_ANIMATION_ENTER_DURATION)
                        .alpha()
                        .overshoot())
                .primaryActionText(actionText)
                .primaryActionTapListener(listener)
                .build()
    }
}