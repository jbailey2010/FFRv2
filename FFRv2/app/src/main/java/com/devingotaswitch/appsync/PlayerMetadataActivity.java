package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.DecrementPlayerDraftedCountMutation;
import com.devingotaswitch.graphqlstuff.DecrementPlayerWatchedCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementPlayerDraftedCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementPlayerViewCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementPlayerWatchedCountMutation;
import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.utils.AWSClientFactory;
import com.devingotaswitch.utils.Constants;

import javax.annotation.Nonnull;

class PlayerMetadataActivity extends AppCompatActivity {

    private static final String TAG = "PlayerMetadataActivity";

    void incrementWatchCount(final Context context, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerWatchedCountMutation.Data> callback = new GraphQLCall
                .Callback<IncrementPlayerWatchedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<IncrementPlayerWatchedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player watch count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully incremented player watched count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to increment watched count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(IncrementPlayerWatchedCountMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(callback);
    }

    void incrementDraftCount(final Context context, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerDraftedCountMutation.Data> callback = new GraphQLCall
                .Callback<IncrementPlayerDraftedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<IncrementPlayerDraftedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player draft count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully incremented player draft count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to increment draft count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(IncrementPlayerDraftedCountMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(callback);
    }

    void decrementWatchCount(final Context context, final String playerId) {
        GraphQLCall.Callback<DecrementPlayerWatchedCountMutation.Data> callback = new GraphQLCall
                .Callback<DecrementPlayerWatchedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<DecrementPlayerWatchedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Decrement player watch count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully decremented player watched count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to decrement watched count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(DecrementPlayerWatchedCountMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(callback);
    }

    void decrementDraftCount(final Context context, final String playerId) {
        GraphQLCall.Callback<DecrementPlayerDraftedCountMutation.Data> callback = new GraphQLCall
                .Callback<DecrementPlayerDraftedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<DecrementPlayerDraftedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Decrement player draft count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully decremented player draft count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to decrement draft count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(DecrementPlayerDraftedCountMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(callback);
    }

    void incrementViewCount(final Activity activity, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerViewCountMutation.Data> incrementViewCallback = new GraphQLCall
                .Callback <IncrementPlayerViewCountMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<IncrementPlayerViewCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player view count failed: " + error.message());
                    }
                } else if (response.data().incrementPlayerViewCount() != null &&
                        response.data().incrementPlayerViewCount().viewCount() != null) {
                    final IncrementPlayerViewCountMutation.IncrementPlayerViewCount metadata = response.data().incrementPlayerViewCount();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((PlayerInfo)activity).setAggregatePlayerMetadata(metadata.viewCount(),
                                    metadata.watchCount() == null ? 0 : metadata.watchCount(),
                                    metadata.draftCount() == null ? 0 : metadata.draftCount());
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform increment player view count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(IncrementPlayerViewCountMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(incrementViewCallback);
    }

    private String getPlayerId(String playerId) {
        return playerId + Constants.YEAR_KEY;
    }
}
