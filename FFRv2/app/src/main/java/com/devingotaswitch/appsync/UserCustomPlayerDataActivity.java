package com.devingotaswitch.appsync;

import android.app.Activity;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.GetUserCustomPlayerDataQuery;
import com.devingotaswitch.graphqlstuff.UpdateUserCustomPlayerDataMutation;
import com.devingotaswitch.graphqlstuff.UpdateUserSettingsMutation;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.AWSClientFactory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class UserCustomPlayerDataActivity extends AppSyncActivity {

    private static final String TAG = "UserCustomPlayerDataActivity";

    public void updateCustomPlayerData(Activity activity, List<String> watchList, Map<String, String> notes) {

        GraphQLCall.Callback<UpdateUserCustomPlayerDataMutation.Data> callback = new GraphQLCall
                .Callback<UpdateUserCustomPlayerDataMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<UpdateUserCustomPlayerDataMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Update user custom data failed: " + error.message());
                    }
                } else {
                    Log.d(TAG, "Successfully updated custom data settings.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to update user custom data.", e);
            }
        };

        String notesString = GSON.toJson(notes).replaceAll("\"", "\\\\\"");
        String watchListString = GSON.toJson(watchList).replaceAll("\"", "\\\\\"");

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                UpdateUserCustomPlayerDataMutation.builder()
                        .watchList(watchListString)
                        .notes(notesString)
                        .build()
        ).enqueue(callback);

    }

    public void getCustomPlayerData(final Activity activity) {
        GraphQLCall.Callback<GetUserCustomPlayerDataQuery.Data> callback = new GraphQLCall
                .Callback<GetUserCustomPlayerDataQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<GetUserCustomPlayerDataQuery.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Get user custom data failed: " + error.message());
                    }
                } else {
                    Log.d(TAG, "Successfully got custom data settings.");

                    final List<String> watchList = new ArrayList<>();
                    final Map<String, String> notes = new HashMap<>();
                    GetUserCustomPlayerDataQuery.GetUserCustomPlayerData data = response.data().getUserCustomPlayerData();
                    if (data != null && data.watchList() != null && data.watchList().length() > 0) {
                        watchList.addAll(GSON.fromJson(data.watchList(), List.class));
                    }
                    if (data != null && data.notes() != null && data.notes().length() > 0) {
                        notes.putAll(GSON.fromJson(data.notes(), Map.class));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RankingsHome)activity).setUserCustomData(watchList, notes);
                        }
                    });

                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to get user custom data.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).query(
                GetUserCustomPlayerDataQuery.builder()
                        .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(callback);
    }
}
