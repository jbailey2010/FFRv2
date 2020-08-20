package com.devingotaswitch.appsync

import android.app.Activity
import android.util.Log
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.devingotaswitch.graphqlstuff.GetUserSettingsQuery
import com.devingotaswitch.graphqlstuff.UpdateUserSettingsMutation
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.rankings.domain.UserSettings
import com.devingotaswitch.utils.AWSClientFactory.getAppSyncInstance
import javax.annotation.Nonnull

class UserSettingsActivity : AppSyncActivity() {
    fun updateUserSettings(activity: Activity?, userSettings: UserSettings) {
        val callback: GraphQLCall.Callback<UpdateUserSettingsMutation.Data?> = object : GraphQLCall.Callback<UpdateUserSettingsMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<UpdateUserSettingsMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Update user settings failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully updated user settings.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to update user settings.", e)
            }
        }
        getAppSyncInstance(activity!!)!!.mutate(
                UpdateUserSettingsMutation.builder()
                        .refreshOnOverscroll(userSettings.isRefreshOnOverscroll)
                        .hideDraftedComparator(userSettings.isHideDraftedComparator)
                        .hideDraftedSearch(userSettings.isHideDraftedSearch)
                        .hideDraftedSort(userSettings.isHideDraftedSort)
                        .hideIrrelevantComparator(userSettings.isHideRanklessComparator)
                        .hideIrrelevantSearch(userSettings.isHideRanklessSearch)
                        .hideIrrelevantSort(userSettings.isHideRanklessSort)
                        .showNoteOnRanks(userSettings.isShowNoteRank)
                        .showNoteOnSort(userSettings.isShowNoteSort)
                        .sortWatchListByTime(userSettings.isSortWatchListByTime)
                        .build()
        ).enqueue(callback)
    }

    fun getUserSettings(activity: Activity?) {
        val callback: GraphQLCall.Callback<GetUserSettingsQuery.Data> = object : GraphQLCall.Callback<GetUserSettingsQuery.Data>() {
            override fun onResponse(@Nonnull response: Response<GetUserSettingsQuery.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Get user settings failed: " + error.message())
                    }
                } else if (response.data()!!.userSettings != null) {
                    Log.d(TAG, "Successfully retrieved user settings.")
                    val settings = response.data()!!.userSettings
                    val userSettings = UserSettings()
                    userSettings.isShowNoteSort = settings!!.showNoteOnSort()
                    userSettings.isShowNoteRank = settings.showNoteOnRanks()
                    userSettings.isRefreshOnOverscroll = settings.refreshOnOverscroll()
                    userSettings.isHideDraftedComparator = settings.hideDraftedComparator()
                    userSettings.isHideDraftedSort = settings.hideDraftedSort()
                    userSettings.isHideDraftedSearch = settings.hideDraftedSearch()
                    userSettings.isHideRanklessComparator = settings.hideIrrelevantComparator()
                    userSettings.isHideRanklessSearch = settings.hideIrrelevantSearch()
                    userSettings.isHideRanklessSort = settings.hideIrrelevantSort()
                    userSettings.isSortWatchListByTime = settings.sortWatchListByTime()
                    runOnUiThread {
                        if (activity is RankingsHome) {
                            activity.setUserSettings(userSettings)
                        }
                    }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to get user settings.", e)
            }
        }
        getAppSyncInstance(activity!!)!!.query(
                GetUserSettingsQuery.builder()
                        .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(callback)
    }

    companion object {
        private const val TAG = "UserSettingsActivity"
    }
}