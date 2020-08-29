package com.devingotaswitch.appsync

import android.app.Activity
import android.util.Log
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.devingotaswitch.graphqlstuff.GetUserCustomPlayerDataQuery
import com.devingotaswitch.graphqlstuff.UpdateUserCustomPlayerDataMutation
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.utils.AWSClientFactory.getAppSyncInstance
import java.util.*
import javax.annotation.Nonnull

class UserCustomPlayerDataActivity : AppSyncActivity() {
    fun updateCustomPlayerData(activity: Activity?, watchList: List<String?>?, notes: Map<String, String>) {
        val callback: GraphQLCall.Callback<UpdateUserCustomPlayerDataMutation.Data?> = object : GraphQLCall.Callback<UpdateUserCustomPlayerDataMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<UpdateUserCustomPlayerDataMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Update user custom data failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully updated custom data settings.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to update user custom data.", e)
            }
        }
        val notesString = GSON.toJson(notes).replace("\"".toRegex(), "\\\\\"")
        val watchListString = GSON.toJson(watchList).replace("\"".toRegex(), "\\\\\"")
        getAppSyncInstance(activity!!)!!.mutate(
                UpdateUserCustomPlayerDataMutation.builder()
                        .watchList(watchListString)
                        .notes(notesString)
                        .build()
        ).enqueue(callback)
    }

    fun getCustomPlayerData(activity: Activity) {
        val callback: GraphQLCall.Callback<GetUserCustomPlayerDataQuery.Data> = object : GraphQLCall.Callback<GetUserCustomPlayerDataQuery.Data>() {
            override fun onResponse(@Nonnull response: Response<GetUserCustomPlayerDataQuery.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Get user custom data failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully got custom data settings.")
                    val watchList: MutableList<String> = ArrayList()
                    val notes: MutableMap<String, String> = HashMap()
                    val data = response.data()!!.userCustomPlayerData
                    if (data?.watchList() != null && data.watchList()!!.isNotEmpty()) {
                        watchList.addAll(GSON.fromJson<List<String>>(data.watchList(), MutableList::class.java))
                    }
                    if (data?.notes() != null && data.notes()!!.isNotEmpty()) {
                        notes.putAll(GSON.fromJson<Map<String, String>>(data.notes(), MutableMap::class.java))
                    }
                    runOnUiThread { (activity as RankingsHome).setUserCustomData(watchList, notes) }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to get user custom data.", e)
            }
        }
        getAppSyncInstance(activity)!!.query(
                GetUserCustomPlayerDataQuery.builder()
                        .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(callback)
    }

    companion object {
        private const val TAG = "UserCustomPlayerDataActivity"
    }
}