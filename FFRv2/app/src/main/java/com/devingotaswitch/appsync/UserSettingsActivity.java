package com.devingotaswitch.appsync;

import android.app.Activity;
import android.provider.Settings;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.GetCommentsOnPlayerQuery;
import com.devingotaswitch.graphqlstuff.GetUserSettingsQuery;
import com.devingotaswitch.graphqlstuff.UpdateUserSettingsMutation;
import com.devingotaswitch.graphqlstuff.UpvoteCommentMutation;
import com.devingotaswitch.rankings.PlayerComparator;
import com.devingotaswitch.rankings.PlayerSorter;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.SettingsActivity;
import com.devingotaswitch.utils.AWSClientFactory;

import javax.annotation.Nonnull;

public class UserSettingsActivity extends AppSyncActivity {

    private static final String TAG = "UserSettingsActivity";

    public void updateUserSettings(Activity activity, boolean hideRanklessSearch, boolean hideRanklessSort,
                                     boolean hideRanklessComparator, boolean hideDraftedSearch, boolean hideDraftedSort,
                                     boolean hideDraftedComparator, boolean noteInRanks, boolean noteInSort,
                                     boolean refreshOnOverscroll) {
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
                .refreshOnOverscroll(refreshOnOverscroll)
                .hideDraftedComparator(hideDraftedComparator)
                .hideDraftedSearch(hideDraftedSearch)
                .hideDraftedSort(hideDraftedSort)
                .hideIrrelevantComparator(hideRanklessComparator)
                .hideIrrelevantSearch(hideRanklessSearch)
                .hideIrrelevantSort(hideRanklessSort)
                .showNoteOnRanks(noteInRanks)
                .showNoteOnSort(noteInSort)
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activity instanceof SettingsActivity) {
                                ((SettingsActivity) activity).updateUserSettings(settings.hideIrrelevantSearch(), settings.hideIrrelevantSort(),
                                        settings.hideIrrelevantComparator(), settings.hideDraftedSearch(), settings.hideDraftedSort(),
                                        settings.hideDraftedComparator(), settings.showNoteOnRanks(), settings.showNoteOnSort(),
                                        settings.refreshOnOverscroll());
                            } else if (activity instanceof PlayerComparator) {
                                ((PlayerComparator)activity).setUserSettings(settings.hideDraftedComparator(),
                                        settings.hideIrrelevantComparator());
                            } else if (activity instanceof PlayerSorter) {
                                ((PlayerSorter)activity).setUserSettings(settings.hideIrrelevantSort(), settings.hideDraftedSort(),
                                        settings.showNoteOnSort());
                            } else if (activity instanceof RankingsHome) {
                                ((RankingsHome)activity).setUserSettings(settings.hideIrrelevantSearch(), settings.hideDraftedSearch(),
                                        settings.refreshOnOverscroll(), settings.showNoteOnRanks());
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
