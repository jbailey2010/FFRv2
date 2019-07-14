package com.devingotaswitch.appsync;

import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;

import com.devingotaswitch.utils.Constants;
import com.google.gson.Gson;

import java.util.Date;

class AppSyncActivity extends AppCompatActivity {

    static final Gson GSON = new Gson();

    String getPlayerId(String playerId) {
        return playerId + Constants.PLAYER_ID_DELIMITER + Constants.YEAR_KEY;
    }

    String getPosFromPlayerId(String playerId) {
        int totalDots = playerId.split("\\" + Constants.PLAYER_ID_DELIMITER).length - 1;
        return playerId.split("\\" + Constants.PLAYER_ID_DELIMITER)[totalDots];
    }

    long getCurrentTime() {
        return new Date().getTime();
    }

    String formatCurrentTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, getCurrentTime(), DateUtils.MINUTE_IN_MILLIS).toString();
    }
}
