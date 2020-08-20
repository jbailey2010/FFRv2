package com.devingotaswitch.appsync

import android.app.Activity
import android.util.Log
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.devingotaswitch.graphqlstuff.*
import com.devingotaswitch.rankings.PlayerInfo
import com.devingotaswitch.rankings.domain.appsync.comments.Comment
import com.devingotaswitch.utils.AWSClientFactory.getAppSyncInstance
import com.devingotaswitch.youruserpools.CUPHelper.currUser
import java.util.*
import javax.annotation.Nonnull

class CommentActivity : AppSyncActivity() {
    fun upvoteComment(activity: Activity, commentId: String, decrementDownvote: Boolean) {
        val callback: GraphQLCall.Callback<UpvoteCommentMutation.Data> = object : GraphQLCall.Callback<UpvoteCommentMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<UpvoteCommentMutation.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Upvote comment failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully upvoted comment $commentId")
                    val comment = response.data()!!.upvoteComment()
                    runOnUiThread { (activity as PlayerInfo).updateVoteCount(comment!!.id(), comment.upvotes()!!, comment.downvotes()!!) }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to upvote comment $commentId", e)
            }
        }
        getAppSyncInstance(activity)!!.mutate(
                UpvoteCommentMutation.builder()
                        .id(commentId)
                        .decrementDownvote(decrementDownvote)
                        .build()
        )
                .enqueue(callback)
    }

    fun downvoteComment(activity: Activity, commentId: String, decrementUpvote: Boolean) {
        val callback: GraphQLCall.Callback<DownvoteCommentMutation.Data> = object : GraphQLCall.Callback<DownvoteCommentMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<DownvoteCommentMutation.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Downvote comment failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully downvoted comment $commentId")
                    val comment = response.data()!!.downvoteComment()
                    runOnUiThread { (activity as PlayerInfo).updateVoteCount(comment!!.id(), comment.upvotes()!!, comment.downvotes()!!) }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to downvote comment $commentId", e)
            }
        }
        getAppSyncInstance(activity)!!.mutate(
                DownvoteCommentMutation.builder()
                        .id(commentId)
                        .decrementUpvote(decrementUpvote)
                        .build()
        )
                .enqueue(callback)
    }

    fun getCommentsForPlayer(activity: Activity, playerId: String?, nextToken: String?, topComments: Boolean) {
        val callback: GraphQLCall.Callback<GetCommentsOnPlayerQuery.Data> = object : GraphQLCall.Callback<GetCommentsOnPlayerQuery.Data>() {
            override fun onResponse(@Nonnull response: Response<GetCommentsOnPlayerQuery.Data>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Get comments for player failed: " + error.message())
                    }
                } else if (response.data()!!.commentsOnPlayer!!.items() != null) {
                    val nextToken = response.data()!!.commentsOnPlayer!!.nextToken()
                    val comments: MutableList<Comment> = ArrayList()
                    for (item in response.data()!!.commentsOnPlayer!!.items()!!) {
                        val comment = Comment()
                        comment.time = formatCurrentTime(java.lang.Double.valueOf(item.date()).toLong())
                        comment.playerId = playerId
                        comment.upvotes = item.upvotes()
                        comment.downvotes = item.downvotes()
                        comment.author = item.author()
                        comment.content = item.content()
                        comment.id = item.id()
                        comment.replyDepth = item.replyDepth()
                        comment.replyToId = if (item.replyToId() == null) "" else item.replyToId()
                        if (item.userVoteStatus() != null) {
                            comment.isUpvoted = item.userVoteStatus()!!.upvoted()!!
                            comment.isDownvoted = item.userVoteStatus()!!.downvoted()!!
                        } else {
                            comment.isUpvoted = false
                            comment.isDownvoted = false
                        }
                        comments.add(comment)
                    }
                    runOnUiThread { (activity as PlayerInfo).addComments(comments, nextToken) }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to get comments for player.", e)
            }
        }
        getAppSyncInstance(activity)!!.query(
                GetCommentsOnPlayerQuery.builder()
                        .playerId(getPlayerId(playerId!!))
                        .nextToken(nextToken)
                        .topComments(topComments)
                        .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(callback)
    }

    fun deleteComment(activity: Activity?, commentId: String) {
        val callback: GraphQLCall.Callback<DeleteCommentMutation.Data?> = object : GraphQLCall.Callback<DeleteCommentMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<DeleteCommentMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Delete comment failed: " + error.message())
                    }
                } else {
                    Log.d(TAG, "Successfully deleted comment $commentId")
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to delete comment $commentId", e)
            }
        }
        getAppSyncInstance(activity!!)!!.mutate(
                DeleteCommentMutation.builder()
                        .id(commentId)
                        .build()
        ).enqueue(callback)
    }

    fun createComment(activity: Activity, comment: String?, playerId: String, replyToId: String?,
                      replyDepth: Int?) {
        val id = UUID.randomUUID().toString()
        val time = currentTime
        val callback: GraphQLCall.Callback<CreateCommentMutation.Data?> = object : GraphQLCall.Callback<CreateCommentMutation.Data?>() {
            override fun onResponse(@Nonnull response: Response<CreateCommentMutation.Data?>) {
                if (response.errors().isNotEmpty()) {
                    for (error in response.errors()) {
                        Log.e(TAG, "Create comment failed: " + error.message())
                    }
                } else {
                    val newComment = Comment()
                    newComment.content = comment
                    newComment.author = currUser
                    newComment.downvotes = 0
                    newComment.upvotes = 1
                    newComment.playerId = playerId
                    newComment.time = formatCurrentTime(time)
                    newComment.id = id
                    newComment.isUpvoted = true
                    newComment.isDownvoted = false
                    newComment.replyDepth = replyDepth
                    newComment.replyToId = replyToId
                    val dummyList: MutableList<Comment> = ArrayList()
                    dummyList.add(newComment)
                    runOnUiThread {
                        try {
                            (activity as PlayerInfo).addComments(dummyList, null)
                        } catch (e: Exception) {
                            Log.d(TAG, "Failed to add comments for player $playerId", e)
                        }
                    }
                }
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e(TAG, "Failed to create comment.", e)
            }
        }
        getAppSyncInstance(activity)!!.mutate(
                CreateCommentMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .id(id)
                        .author(currUser!!)
                        .content(comment!!)
                        .replyToId(replyToId!!)
                        .replyDepth(replyDepth!!)
                        .date(time.toDouble())
                        .build()
        ).enqueue(callback)
    }

    companion object {
        private const val TAG = "CommentActivity"
    }
}