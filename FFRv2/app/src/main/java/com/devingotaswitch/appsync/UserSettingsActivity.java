package com.devingotaswitch.appsync;

import android.app.Activity;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.GetUserSettingsQuery;
import com.devingotaswitch.graphqlstuff.UpdateUserSettingsMutation;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.domain.UserSettings;
import com.devingotaswitch.utils.AWSClientFactory;

import javax.annotation.Nonnull;

public class UserSettingsActivity extends AppSyncActivity {

    private static final String TAG = "UserSettingsActivity";

    public void updateUserSettings(Activity activity, UserSettings userSettings) {
        GraphQLCall.Callback<UpdateUserSettingsMutation.Data> callback = new GraphQLCall
                .Callback<UpdateUserSettingsMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<UpdateUserSettingsMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Update user settings failed: " + error.message());
                    }
                } else {
                    Log.d(TAG, "Successfully updated user settings.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to update user settings.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                UpdateUserSettingsMutation.builder()
                .refreshOnOverscroll(userSettings.isRefreshOnOverscroll())
                .hideDraftedComparator(userSettings.isHideDraftedComparator())
                .hideDraftedSearch(userSettings.isHideDraftedSearch())
                .hideDraftedSort(userSettings.isHideDraftedSort())
                .hideIrrelevantComparator(userSettings.isHideRanklessComparator())
                .hideIrrelevantSearch(userSettings.isHideRanklessSearch())
                .hideIrrelevantSort(userSettings.isHideRanklessSort())
                .showNoteOnRanks(userSettings.isShowNoteRank())
                .showNoteOnSort(userSettings.isShowNoteSort())
                .sortWatchListByTime(userSettings.isSortWatchListByTime())
                .build()
        ).enqueue(callback);

    }

    public void getUserSettings(final Activity activity) {
        GraphQLCall.Callback<GetUserSettingsQuery.Data> callback = new GraphQLCall
                .Callback<GetUserSettingsQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<GetUserSettingsQuery.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Get user settings failed: " + error.message());
                    }
                } else if (response.data().getUserSettings() != null) {
                    Log.d(TAG, "Successfully retrieved user settings.");

                    final GetUserSettingsQuery.GetUserSettings settings = response.data().getUserSettings();
                    final UserSettings userSettings = new UserSettings();
                    userSettings.setShowNoteSort(settings.showNoteOnSort());
                    userSettings.setShowNoteRank(settings.showNoteOnRanks());
                    userSettings.setRefreshOnOverscroll(settings.refreshOnOverscroll());
                    userSettings.setHideDraftedComparator(settings.hideDraftedComparator());
                    userSettings.setHideDraftedSort(settings.hideDraftedSort());
                    userSettings.setHideDraftedSearch(settings.hideDraftedSearch());
                    userSettings.setHideRanklessComparator(settings.hideIrrelevantComparator());
                    userSettings.setHideRanklessSearch(settings.hideIrrelevantSearch());
                    userSettings.setHideRanklessSort(settings.hideIrrelevantSort());
                    userSettings.setSortWatchListByTime(settings.sortWatchListByTime());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activity instanceof RankingsHome) {
                                ((RankingsHome)activity).setUserSettings(userSettings);
                            }
                        }
                    });
                }


            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to get user settings.", e);
            }
        };


        AWSClientFactory.getAppSyncInstance(activity).query(
                GetUserSettingsQuery.builder()
                        .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(callback);
    }
}
