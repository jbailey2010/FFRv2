package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;

public class AppSyncHelper {

    public static final PlayerMetadataActivity metadataActivity = new PlayerMetadataActivity();

    public static void getOrCreatePlayerMetadataAndIncrementViewCount(Activity activity, String playerId) {
        metadataActivity.incrementViewCount(activity, playerId);
    }

    public static void incrementPlayerWatchedCount(Context context, String playerId) {
        metadataActivity.incrementWatchCount(context, playerId);
    }

    public static void decrementPlayerWatchedCount(Context context, String playerId) {
        metadataActivity.decrementWatchCount(context, playerId);
    }

    public static void incrementPlayerDraftCount(Context context, String playerId) {
        metadataActivity.incrementDraftCount(context, playerId);
    }

    public static void decrementPlayerDraftCount(Context context, String playerId) {
        metadataActivity.decrementDraftCount(context, playerId);
    }
}
