package com.devingotaswitch.appsync;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.CreatePlayerMetadataMutation;
import com.devingotaswitch.graphqlstuff.GetPlayerMetadataQuery;
import com.devingotaswitch.graphqlstuff.IncrementPlayerViewCountMutation;
import com.devingotaswitch.utils.Constants;

import javax.annotation.Nonnull;

class PlayerMetadataActivity extends AppCompatActivity {

    private static final String TAG = "PlayerMetadataActivity";

    void createPlayerMetadata(Activity activity, AWSAppSyncClient appSync, String playerId) {
        GraphQLCall.Callback<CreatePlayerMetadataMutation.Data> createCallback = new GraphQLCall.Callback <CreatePlayerMetadataMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<CreatePlayerMetadataMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Create player metadata failed: " + error.message());
                    }
                } else if (response.data() == null) {
                    Log.d(TAG, "Mutation had an empty response. This should never happen.");
                } else {
                    CreatePlayerMetadataMutation.CreatePlayerMetadata metadata = response.data().createPlayerMetadata();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: display on player info
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform create player metadata", e);
            }
        };
        appSync.mutate(CreatePlayerMetadataMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(createCallback);
    }

    void getPlayerMetadata(final Activity activity, final AWSAppSyncClient appSync, final String playerId) {
        GraphQLCall.Callback<GetPlayerMetadataQuery.Data> getCallback = new GraphQLCall.Callback <GetPlayerMetadataQuery.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<GetPlayerMetadataQuery.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Get player metadata failed: " + error.message());
                    }
                } else if (response.data() == null || response.data().getPlayerMetadata() == null) {
                    createPlayerMetadata(activity, appSync, playerId);
                } else {
                    GetPlayerMetadataQuery.GetPlayerMetadata metadata = response.data().getPlayerMetadata();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: display on player info
                        }
                    });
                    incrementViewCount(appSync, playerId);
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform get player metadata", e);
            }
        };

        appSync.query(GetPlayerMetadataQuery.builder().playerId(getPlayerId(playerId)).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(getCallback);
    }

    private void incrementViewCount(final AWSAppSyncClient appSync, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerViewCountMutation.Data> incrementWatchCallback = new GraphQLCall
                .Callback <IncrementPlayerViewCountMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<IncrementPlayerViewCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player watch count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Increment player watch count succeeded.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform increment player watch count.", e);
            }
        };

        appSync.mutate(IncrementPlayerViewCountMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(incrementWatchCallback);
    }

    private String getPlayerId(String playerId) {
        return playerId + Constants.YEAR_KEY;
    }
}
