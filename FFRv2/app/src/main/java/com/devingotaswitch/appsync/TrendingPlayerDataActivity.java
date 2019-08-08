package com.devingotaswitch.appsync;

import android.app.Activity;
import android.util.Log;

import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.AddPlayerComparisonCountMutation;
import com.devingotaswitch.graphqlstuff.UpdateUserCustomPlayerDataMutation;
import com.devingotaswitch.utils.AWSClientFactory;
import com.devingotaswitch.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class TrendingPlayerDataActivity  extends AppSyncActivity {

    private static final String TAG = "TrendingPlayerDataActivity";

    public void addPlayerComparisonCount(Activity activity, String playerIdA, String playerIdB) {

        GraphQLCall.Callback<AddPlayerComparisonCountMutation.Data> callback = new GraphQLCall
                .Callback<AddPlayerComparisonCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<AddPlayerComparisonCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Add player comparison count failed: " + error.message());
                    }
                } else {
                    Log.d(TAG, "Successfully added player comparison count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to add player comparison count.", e);
            }
        };

        String trendingId = getTrendingId(getPlayerId(playerIdA), getPlayerId(playerIdB));

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                AddPlayerComparisonCountMutation.builder()
                        .trendingId(trendingId)
                        .build()
        ).enqueue(callback);

    }

    private String getTrendingId(String idA, String idB) {
        if (idA.compareTo(idB) < 0) {
            return idA + Constants.TRENDING_ID_DELIMITER + idB;
        }
        return idB + Constants.TRENDING_ID_DELIMITER + idA;
    }
}
