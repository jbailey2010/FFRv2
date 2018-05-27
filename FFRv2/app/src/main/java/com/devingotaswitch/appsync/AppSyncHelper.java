package com.devingotaswitch.appsync;

import android.content.Context;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.devingotaswitch.utils.AWSClientFactory;

class AppSyncHelper {

    private static AWSAppSyncClient APPSYNC_CLIENT;

    public static void init(Context context) {
        if (APPSYNC_CLIENT != null) {
            return;
        }

        APPSYNC_CLIENT = AWSClientFactory.getAppSyncInstance(context);
    }

}
