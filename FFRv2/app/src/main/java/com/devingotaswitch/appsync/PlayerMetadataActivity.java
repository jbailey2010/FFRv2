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
import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.utils.AWSClientFactory;
import com.devingotaswitch.utils.Constants;

import javax.annotation.Nonnull;

class PlayerMetadataActivity extends AppCompatActivity {

    private static final String TAG = "PlayerMetadataActivity";

    private void createPlayerMetadata(final Activity activity, final String playerId) {
        GraphQLCall.Callback<CreatePlayerMetadataMutation.Data> createCallback = new GraphQLCall.Callback <CreatePlayerMetadataMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<CreatePlayerMetadataMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Create player metadata failed: " + error.message());
                    }
                } else if (response.data() == null || response.data().createPlayerMetadata() == null) {
                    Log.d(TAG, "Mutation had an empty response. This should never happen.");
                } else {
                    final CreatePlayerMetadataMutation.CreatePlayerMetadata metadata = response.data().createPlayerMetadata();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((PlayerInfo)activity).setAggregatePlayerMetadata(metadata.viewCount(), metadata.watchCount());
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform create player metadata", e);
            }
        };
        AWSClientFactory.getAppSyncInstance(activity).mutate(CreatePlayerMetadataMutation.builder().playerId(getPlayerId(playerId)).build())
                .enqueue(createCallback);
    }

    void incrementViewCount(final Activity activity, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerViewCountMutation.Data> incrementWatchCallback = new GraphQLCall
                .Callback <IncrementPlayerViewCountMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<IncrementPlayerViewCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player view count failed: " + error.message());
                    }
                } else  if (response.data() == null || response.data().incrementPlayerViewCount() == null ||
                        response.data().incrementPlayerViewCount().viewCount() == null) {
                    createPlayerMetadata(activity, playerId);
                } else {
                    final IncrementPlayerViewCountMutation.IncrementPlayerViewCount metadata = response.data().incrementPlayerViewCount();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((PlayerInfo)activity).setAggregatePlayerMetadata(metadata.viewCount(),
                                    metadata.watchCount() == null ? 0 : metadata.watchCount());
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
                .enqueue(incrementWatchCallback);
    }

    private String getPlayerId(String playerId) {
        return playerId + Constants.YEAR_KEY;
    }
}
