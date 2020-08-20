package com.devingotaswitch.appsync

import android.app.Activity
import android.util.Log
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.devingotaswitch.graphqlstuff.AddPlayerComparisonCountMutation
import com.devingotaswitch.utils.AWSClientFactory.getAppSyncInstance
import com.devingotaswitch.utils.Constants
import javax.annotation.Nonnull

class TrendingPlayerDataActivity : AppSyncActivity() {
    fun addPlayerComparisonCount(activity: Activity?, playerIdA: String?, playerIdB: String?) {
        val callback: GraphQLCall.Callback<AddPlayerComparisonCountMutation.Data?> = object : GraphQLCall.Callback<AddPlayerComparisonCountMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<AddPlayerComparisonCountMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Add player comparison count failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully added player comparison count.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to add player comparison count.", e)
            }
        }
        val trendingId = getTrendingId(getPlayerId(playerIdA!!), getPlayerId(playerIdB!!))
        getAppSyncInstance(activity!!)!!.mutate(
                AddPlayerComparisonCountMutation.builder()
                        .trendingId(trendingId)
                        .build()
        ).enqueue(callback)
    }

    private fun getTrendingId(idA: String, idB: String): String {
        return if (idA < idB) {
            idA + Constants.TRENDING_ID_DELIMITER + idB
        } else idB + Constants.TRENDING_ID_DELIMITER + idA
    }

    companion object {
        private const val TAG = "TrendingPlayerDataActivity"
    }
}