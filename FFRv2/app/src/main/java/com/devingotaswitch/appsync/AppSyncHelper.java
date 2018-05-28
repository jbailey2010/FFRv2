package com.devingotaswitch.appsync;

import android.content.Context;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.utils.AWSClientFactory;

public class AppSyncHelper {

    private static AWSAppSyncClient APPSYNC_CLIENT;

    public static void init(Context context) {
        if (APPSYNC_CLIENT != null) {
            return;
        }

        APPSYNC_CLIENT = AWSClientFactory.getAppSyncInstance(context);
    }
}
