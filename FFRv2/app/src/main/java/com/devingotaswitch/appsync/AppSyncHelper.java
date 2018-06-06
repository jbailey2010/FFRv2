package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.devingotaswitch.rankings.asynctasks.RankingsLoader;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.utils.AWSClientFactory;
import com.devingotaswitch.youruserpools.CUPHelper;

public class AppSyncHelper {

    public static final PlayerMetadataActivity metadataActivity = new PlayerMetadataActivity();

    /**
     * If the query to get the metadata returns null, create is called internally.
     * If it's successful, it will also trigger a view count increment, as this is
     * only to be called from the player info page. Meaning, if it's created, it will be with
     * one view. If it's retrieved, it will update to be x+1 views.
     *
     * @param activity the activity calling the query.
     * @param playerId the player for which metadata is wanted.
     */
    public static void getOrCreatePlayerMetadataAndIncrementViewCount(Activity activity, String playerId) {
        AWSAppSyncClient appSync = AWSClientFactory.getAppSyncInstance(activity);
        metadataActivity.getPlayerMetadata(activity, appSync, playerId);
    }
}
