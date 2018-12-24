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
}
