package com.devingotaswitch.utils;

import android.app.Activity;

import com.andrognito.flashbar.Flashbar;
import com.andrognito.flashbar.anim.FlashAnim;
import com.devingotaswitch.ffrv2.R;

public class FlashbarFactory {
    public static Flashbar generateTextOnlyFlashbar(Activity activity, String title, String message, Flashbar.Gravity gravity) {
        return new Flashbar.Builder(activity)
                .enableSwipeToDismiss()
                .gravity(gravity)
                .duration(Constants.FLASHBAR_DURATION)
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
                .build();
    }

    public static Flashbar generateFlashbarWithUndo(Activity activity, String title, String message, Flashbar.Gravity gravity,
                                                    Flashbar.OnActionTapListener undoListener) {
        return new Flashbar.Builder(activity)
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
                .primaryActionTapListener(undoListener)
                .build();
    }

    public static Flashbar generateInfiniteFlashbarWithAction(Activity activity, String title, String message, Flashbar.Gravity gravity,
                                                    Flashbar.OnActionTapListener listener, String actionText) {
        return new Flashbar.Builder(activity)
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
                .build();
    }
}
