package com.devingotaswitch.appsync;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;

import com.devingotaswitch.utils.Constants;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

class AppSyncActivity extends AppCompatActivity {

    String getPlayerId(String playerId) {
        return playerId + Constants.PLAYER_ID_DELIMITER + Constants.YEAR_KEY;
    }

    String getPosFromPlayerId(String playerId) {
        return playerId.split("\\" + Constants.PLAYER_ID_DELIMITER)[2];
    }

    long getCurrentTime() {
        return new Date().getTime();
    }

    String formatCurrentTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, getCurrentTime(), DateUtils.MINUTE_IN_MILLIS).toString();
    }
}
