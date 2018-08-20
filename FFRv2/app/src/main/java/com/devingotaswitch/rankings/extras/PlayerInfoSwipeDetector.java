package com.devingotaswitch.rankings.extras;

import android.view.MotionEvent;
import android.view.View;

import com.devingotaswitch.rankings.PlayerInfo;

public class PlayerInfoSwipeDetector implements View.OnTouchListener {

    private static final int MIN_DISTANCE = 450;
    private float downX, upX;
    private PlayerInfo hold;

    public PlayerInfoSwipeDetector(PlayerInfo playerInfo) {
        hold = playerInfo;
    }

    private final void onRightToLeftSwipe() {
        hold.swipeRightToLeft();
    }

    private void onLeftToRightSwipe() {
        hold.swipeLeftToRight();
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                float deltaX = downX - upX;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (deltaX < 0) {
                        this.onLeftToRightSwipe();
                        return true;
                    }
                    if (deltaX > 0) {
                        this.onRightToLeftSwipe();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}