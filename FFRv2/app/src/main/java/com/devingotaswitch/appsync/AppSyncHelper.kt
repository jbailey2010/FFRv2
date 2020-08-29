package com.devingotaswitch.appsync

import android.app.Activity
import android.content.Context
import com.devingotaswitch.rankings.domain.UserSettings
import com.devingotaswitch.rankings.domain.appsync.tags.Tag

object AppSyncHelper {
    private val metadataActivity = PlayerMetadataActivity()
    private val commentActivity = CommentActivity()
    private val userSettingsActivity = UserSettingsActivity()
    private val customPlayerDataActivity = UserCustomPlayerDataActivity()
    private val trendingPlayerDataActivity = TrendingPlayerDataActivity()
    @JvmStatic
    fun getOrCreatePlayerMetadataAndIncrementViewCount(activity: Activity, playerId: String) {
        metadataActivity.incrementViewCount(activity, playerId)
    }

    @JvmStatic
    fun incrementPlayerWatchedCount(context: Context, playerId: String) {
        metadataActivity.incrementWatchCount(context, playerId)
    }

    @JvmStatic
    fun decrementPlayerWatchedCount(context: Context, playerId: String) {
        metadataActivity.decrementWatchCount(context, playerId)
    }

    @JvmStatic
    fun incrementPlayerDraftCount(context: Context, playerId: String) {
        metadataActivity.incrementDraftCount(context, playerId)
    }

    @JvmStatic
    fun decrementPlayerDraftCount(context: Context, playerId: String) {
        metadataActivity.decrementDraftCount(context, playerId)
    }

    @JvmStatic
    fun incrementTagCount(activity: Activity, playerId: String, tag: Tag, tags: List<String>) {
        metadataActivity.incrementTagCount(activity, playerId, tag.remoteTitle, tags)
    }

    @JvmStatic
    fun decrementTagCount(activity: Activity, playerId: String, tag: Tag, tags: List<String>) {
        metadataActivity.decrementTagCount(activity, playerId, tag.remoteTitle, tags)
    }

    @JvmStatic
    fun createComment(activity: Activity, comment: String, playerId: String, replyToId: String, replyDepth: Int) {
        commentActivity.createComment(activity, comment, playerId, replyToId, replyDepth)
    }

    @JvmStatic
    fun getCommentsForPlayer(activity: Activity, playerId: String, nextToken: String?, topComments: Boolean) {
        commentActivity.getCommentsForPlayer(activity, playerId, nextToken, topComments)
    }

    @JvmStatic
    fun deleteComment(activity: Activity, commentId: String) {
        commentActivity.deleteComment(activity, commentId)
    }

    @JvmStatic
    fun upvoteComment(activity: Activity, commentId: String, decrementDownvote: Boolean) {
        commentActivity.upvoteComment(activity, commentId, decrementDownvote)
    }

    @JvmStatic
    fun downvoteComment(activity: Activity, commentId: String, decrementUpvote: Boolean) {
        commentActivity.downvoteComment(activity, commentId, decrementUpvote)
    }

    @JvmStatic
    fun updateUserSettings(activity: Activity, userSettings: UserSettings) {
        userSettingsActivity.updateUserSettings(activity, userSettings)
    }

    @JvmStatic
    fun getUserSettings(activity: Activity) {
        userSettingsActivity.getUserSettings(activity)
    }

    @JvmStatic
    fun updateUserCustomPlayerData(activity: Activity, watchList: List<String>, notes: Map<String, String>) {
        customPlayerDataActivity.updateCustomPlayerData(activity, watchList, notes)
    }

    @JvmStatic
    fun getUserCustomPlayerData(activity: Activity) {
        customPlayerDataActivity.getCustomPlayerData(activity)
    }

    @JvmStatic
    fun addPlayerComparisonCount(activity: Activity, idA: String, idB: String) {
        trendingPlayerDataActivity.addPlayerComparisonCount(activity, idA, idB)
    }
}