package com.devingotaswitch.appsync;

import android.support.v7.app.AppCompatActivity;

import com.devingotaswitch.utils.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class AppSyncActivity extends AppCompatActivity {

    String getPlayerId(String playerId) {
        return playerId + Constants.YEAR_KEY;
    }

    String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

}
