package com.devingotaswitch.appsync

import android.app.Activity
import android.content.Context
import android.util.Log
import com.amazonaws.util.StringUtils
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.devingotaswitch.graphqlstuff.*
import com.devingotaswitch.rankings.PlayerInfo
import com.devingotaswitch.rankings.domain.appsync.tags.*
import com.devingotaswitch.utils.AWSClientFactory.getAppSyncInstance
import java.util.*
import javax.annotation.Nonnull

internal class PlayerMetadataActivity : AppSyncActivity() {
    fun incrementWatchCount(context: Context?, playerId: String?) {
        val callback: GraphQLCall.Callback<IncrementPlayerWatchedCountMutation.Data?> = object : GraphQLCall.Callback<IncrementPlayerWatchedCountMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<IncrementPlayerWatchedCountMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Increment player watch count failed: " + error.message())
                    }
                } else {
                    Log.i(TAG, "Successfully incremented player watched count.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to increment watched count.", e)
            }
        }
        getAppSyncInstance(context!!)!!.mutate(
                IncrementPlayerWatchedCountMutation.builder()
                        .playerId(getPlayerId(playerId!!))
                        .build())
                .enqueue(callback)
    }

    fun incrementDraftCount(context: Context?, playerId: String?) {
        val callback: GraphQLCall.Callback<IncrementPlayerDraftedCountMutation.Data?> = object : GraphQLCall.Callback<IncrementPlayerDraftedCountMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<IncrementPlayerDraftedCountMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Increment player draft count failed: " + error.message())
                    }
                } else {
                    Log.i(TAG, "Successfully incremented player draft count.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to increment draft count.", e)
            }
        }
        getAppSyncInstance(context!!)!!.mutate(
                IncrementPlayerDraftedCountMutation.builder()
                        .playerId(getPlayerId(playerId!!))
                        .build())
                .enqueue(callback)
    }

    fun decrementWatchCount(context: Context?, playerId: String?) {
        val callback: GraphQLCall.Callback<DecrementPlayerWatchedCountMutation.Data?> = object : GraphQLCall.Callback<DecrementPlayerWatchedCountMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<DecrementPlayerWatchedCountMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Decrement player watch count failed: " + error.message())
                    }
                } else {
                    Log.i(TAG, "Successfully decremented player watched count.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to decrement watched count.", e)
            }
        }
        getAppSyncInstance(context!!)!!
                .mutate(DecrementPlayerWatchedCountMutation.builder()
                        .playerId(getPlayerId(playerId!!))
                        .build())
                .enqueue(callback)
    }

    fun decrementDraftCount(context: Context?, playerId: String?) {
        val callback: GraphQLCall.Callback<DecrementPlayerDraftedCountMutation.Data?> = object : GraphQLCall.Callback<DecrementPlayerDraftedCountMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<DecrementPlayerDraftedCountMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Decrement player draft count failed: " + error.message())
                    }
                } else {
                    Log.i(TAG, "Successfully decremented player draft count.")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to decrement draft count.", e)
            }
        }
        getAppSyncInstance(context!!)!!.mutate(
                DecrementPlayerDraftedCountMutation.builder()
                        .playerId(getPlayerId(playerId!!))
                        .build())
                .enqueue(callback)
    }

    fun incrementViewCount(activity: Activity, playerId: String) {
        val incrementViewCallback: GraphQLCall.Callback<IncrementPlayerViewCountMutation.Data> = object : GraphQLCall.Callback<IncrementPlayerViewCountMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<IncrementPlayerViewCountMutation.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Increment player view count failed: " + error.message())
                    }
                } else if (response.data()!!.incrementPlayerViewCount() != null &&
                        response.data()!!.incrementPlayerViewCount()!!.viewCount() != null) {
                    val metadata = response.data()!!.incrementPlayerViewCount()
                    val tags = getTags(playerId, metadata!!.aging(), metadata.boomOrBust(), metadata.bounceBack(), metadata.breakout(), metadata.bust(),
                            metadata.consistentScorer(), metadata.efficient(), metadata.handcuff(), metadata.inefficient(), metadata.injuryBounceBack(), metadata.injuryProne(), metadata.lotteryTicket(), metadata.newStaff(),
                            metadata.newTeam(), metadata.overvalued(), metadata.postHypeSleeper(), metadata.pprSpecialist(), metadata.returner(), metadata.risky(), metadata.safe(), metadata.sleeper(),
                            metadata.stud(), metadata.undervalued())
                    val userTags = getUserTags(metadata.userTags())
                    runOnUiThread {
                        (activity as PlayerInfo).setAggregatePlayerMetadata(metadata.viewCount()!!,
                                (if (metadata.watchCount() == null) 0 else metadata.watchCount()!!),
                                (if (metadata.draftCount() == null) 0 else metadata.draftCount()!!),
                                tags, userTags)
                    }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to perform increment player view count.", e)
            }
        }
        getAppSyncInstance(activity)!!.mutate(
                IncrementPlayerViewCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .build())
                .enqueue(incrementViewCallback)
    }

    private fun getUserTags(serializedTags: String?): MutableList<String> {
        return if (!StringUtils.isBlank(serializedTags)) {
            // We have to do a really hacky parse as AppSync doesn't support ProjectionExpression
            // and the only way to get only the tags field would be to have a whole separate query.
            GSON.fromJson<MutableList<String>>(serializedTags!!.split("userTags=".toRegex()).toTypedArray()[1].split(", ".toRegex()).toTypedArray()[0],
                    MutableList::class.java)
        } else ArrayList()
    }

    fun incrementTagCount(activity: Activity, playerId: String, tagName: String, tags: List<String?>?) {
        val incrementTagCallback: GraphQLCall.Callback<IncrementTagCountMutation.Data> = object : GraphQLCall.Callback<IncrementTagCountMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<IncrementTagCountMutation.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Increment player tag " + tagName + " count failed: " + error.message())
                    }
                } else if (response.data()!!.incrementTagCount() != null) {
                    val metadata = response.data()!!.incrementTagCount()
                    val tags = getTags(playerId, metadata!!.aging(), metadata.boomOrBust(), metadata.bounceBack(), metadata.breakout(), metadata.bust(),
                            metadata.consistentScorer(), metadata.efficient(), metadata.handcuff(), metadata.inefficient(), metadata.injuryBounceBack(), metadata.injuryProne(), metadata.lotteryTicket(), metadata.newStaff(),
                            metadata.newTeam(), metadata.overvalued(), metadata.postHypeSleeper(), metadata.pprSpecialist(), metadata.returner(), metadata.risky(), metadata.safe(), metadata.sleeper(),
                            metadata.stud(), metadata.undervalued())
                    runOnUiThread { (activity as PlayerInfo).setTags(tags) }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to perform increment player tag $tagName count.", e)
            }
        }
        getAppSyncInstance(activity)!!.mutate(
                IncrementTagCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .tagName(tagName)
                        .userTags(GSON.toJson(tags).replace("\"".toRegex(), "\\\\\""))
                        .build())
                .enqueue(incrementTagCallback)
    }

    fun decrementTagCount(activity: Activity, playerId: String, tagName: String, tags: List<String?>?) {
        val decrementTagCallback: GraphQLCall.Callback<DecrementTagCountMutation.Data> = object : GraphQLCall.Callback<DecrementTagCountMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<DecrementTagCountMutation.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Decrement player tag " + tagName + " count failed: " + error.message())
                    }
                } else if (response.data()!!.decrementTagCount() != null) {
                    val metadata = response.data()!!.decrementTagCount()
                    val tags = getTags(playerId, metadata!!.aging(), metadata.boomOrBust(), metadata.bounceBack(), metadata.breakout(), metadata.bust(),
                            metadata.consistentScorer(), metadata.efficient(), metadata.handcuff(), metadata.inefficient(), metadata.injuryBounceBack(), metadata.injuryProne(), metadata.lotteryTicket(), metadata.newStaff(),
                            metadata.newTeam(), metadata.overvalued(), metadata.postHypeSleeper(), metadata.pprSpecialist(), metadata.returner(), metadata.risky(), metadata.safe(), metadata.sleeper(),
                            metadata.stud(), metadata.undervalued())
                    runOnUiThread { (activity as PlayerInfo).setTags(tags) }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to perform decrement player tag $tagName count.", e)
            }
        }
        getAppSyncInstance(activity)!!.mutate(
                DecrementTagCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .tagName(tagName)
                        .userTags(GSON.toJson(tags).replace("\"".toRegex(), "\\\\\""))
                        .build())
                .enqueue(decrementTagCallback)
    }

    private fun getTags(playerId: String, aging: Int?, boomOrBust: Int?, bounceBack: Int?, breakout: Int?, bust: Int?,
                        consistent: Int?, efficient: Int?, handcuff: Int?, inefficient: Int?, injuryBounceBack: Int?, injuryProne: Int?, lotteryTicket: Int?, newStaff: Int?,
                        newTeam: Int?, overvalued: Int?, postHypeSleeper: Int?, pprSpecialist: Int?, returner: Int?, risky: Int?,
                        safe: Int?, sleeper: Int?, stud: Int?, undervalued: Int?): List<Tag> {
        val tags: MutableList<Tag> = ArrayList()
        tags.add(AgingTag(aging ?: 0))
        tags.add(BoomOrBustTag(boomOrBust ?: 0))
        tags.add(BounceBackTag(bounceBack ?: 0))
        tags.add(BreakoutTag(breakout ?: 0))
        tags.add(BustTag(bust ?: 0))
        tags.add(ConsistentTag(consistent ?: 0))
        tags.add(EfficientTag(efficient ?: 0))
        tags.add(HandcuffTag(handcuff ?: 0))
        tags.add(InefficientTag(inefficient ?: 0))
        tags.add(InjuryBounceBackTag(injuryBounceBack ?: 0))
        tags.add(InjuryProneTag(injuryProne ?: 0))
        tags.add(LotteryTicketTag(lotteryTicket ?: 0))
        tags.add(NewStaffTag(newStaff ?: 0))
        tags.add(NewTeamTag(newTeam ?: 0))
        tags.add(OvervaluedTag(overvalued ?: 0))
        tags.add(PostHypeSleeperTag(postHypeSleeper ?: 0))
        tags.add(PPRSpecialistTag(pprSpecialist ?: 0))
        tags.add(ReturnerTag(returner ?: 0))
        tags.add(RiskyTag(risky ?: 0))
        tags.add(SafeTag(safe ?: 0))
        tags.add(SleeperTag(sleeper ?: 0))
        tags.add(StudTag(stud ?: 0))
        tags.add(UndervaluedTag(undervalued ?: 0))
        return filterTagsByPositionToArray(tags, playerId)
    }

    private fun filterTagsByPositionToArray(tags: MutableList<Tag>, playerId: String): List<Tag> {
        val pos = getPosFromPlayerId(playerId)
        val iterator = tags.iterator()
        while (iterator.hasNext()) {
            val tag = iterator.next()
            if (!tag.isValidForPosition(pos)) {
                iterator.remove()
            }
        }
        return tags
    }

    companion object {
        private const val TAG = "PlayerMetadataActivity"
    }
}