package com.devingotaswitch.appsync;

import android.app.Activity;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.graphqlstuff.CreateCommentMutation;
import com.devingotaswitch.graphqlstuff.DeleteCommentMutation;
import com.devingotaswitch.graphqlstuff.DownvoteCommentMutation;
import com.devingotaswitch.graphqlstuff.GetCommentsOnPlayerQuery;
import com.devingotaswitch.graphqlstuff.UpvoteCommentMutation;
import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.rankings.domain.appsync.comments.Comment;
import com.devingotaswitch.utils.AWSClientFactory;
import com.devingotaswitch.youruserpools.CUPHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class CommentActivity extends AppSyncActivity {

    private static final String TAG = "CommentActivity";

    void upvoteComment(final Activity activity, final String commentId, final boolean decrementDownvote) {
        GraphQLCall.Callback<UpvoteCommentMutation.Data> callback = new GraphQLCall
                .Callback<UpvoteCommentMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<UpvoteCommentMutation.Data> response) {
                for (Error error : response.errors()) {
                    Log.e(TAG, "Upvote comment failed: " + error.message());
                }
                Log.d(TAG, "Successfully upvoted comment " + commentId);
                final UpvoteCommentMutation.UpvoteComment comment = response.data().upvoteComment();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((PlayerInfo) activity).updateVoteCount(comment.id(), comment.upvotes(), comment.downvotes());
                    }
                });

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to upvote comment " + commentId, e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                UpvoteCommentMutation.builder()
                        .id(commentId)
                        .decrementDownvote(decrementDownvote)
                        .build()
                )
                .enqueue(callback);
    }

    void downvoteComment(final Activity activity, final String commentId, final boolean decrementUpvote) {
        GraphQLCall.Callback<DownvoteCommentMutation.Data> callback = new GraphQLCall
                .Callback<DownvoteCommentMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<DownvoteCommentMutation.Data> response) {
                for (Error error : response.errors()) {
                    Log.e(TAG, "Downvote comment failed: " + error.message());
                }
                Log.d(TAG, "Successfully downvoted comment " + commentId);
                final DownvoteCommentMutation.DownvoteComment comment = response.data().downvoteComment();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((PlayerInfo) activity).updateVoteCount(comment.id(), comment.upvotes(), comment.downvotes());
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to downvote comment " + commentId, e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                DownvoteCommentMutation.builder()
                        .id(commentId)
                        .decrementUpvote(decrementUpvote)
                        .build()
                )
                .enqueue(callback);
    }

    void getCommentsForPlayer(final Activity activity, final String playerId, final String nextToken, final boolean topComments) {
        GraphQLCall.Callback<GetCommentsOnPlayerQuery.Data> callback = new GraphQLCall
                .Callback<GetCommentsOnPlayerQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<GetCommentsOnPlayerQuery.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Get comments for player failed: " + error.message());
                    }
                } else if (response.data().getCommentsOnPlayer().items() != null){
                    final String nextToken = response.data().getCommentsOnPlayer().nextToken();
                    final List<Comment> comments = new ArrayList<>();
                    for (GetCommentsOnPlayerQuery.Item item : response.data().getCommentsOnPlayer().items()) {
                        Comment comment = new Comment();
                        comment.setTime(formatCurrentTime(Double.valueOf(item.date()).longValue()));
                        comment.setPlayerId(playerId);
                        comment.setUpvotes(item.upvotes());
                        comment.setDownvotes(item.downvotes());
                        comment.setAuthor(item.author());
                        comment.setContent(item.content());
                        comment.setId(item.id());
                        comment.setReplyDepth(item.replyDepth());
                        comment.setReplyToId(item.replyToId() == null ? "" : item.replyToId());
                        comments.add(comment);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((PlayerInfo)activity).addComments(comments, nextToken);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to get comments for player.", e);
            }
        };


        AWSClientFactory.getAppSyncInstance(activity).query(
                GetCommentsOnPlayerQuery.builder()
                        .playerId(getPlayerId(playerId))
                        .nextToken(nextToken)
                        .topComments(topComments)
                .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(callback);
    }

    void deleteComment(final Activity activity, final String commentId) {
        GraphQLCall.Callback<DeleteCommentMutation.Data> callback = new GraphQLCall
                .Callback<DeleteCommentMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<DeleteCommentMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Delete comment failed: " + error.message());
                    }
                }
                Log.d(TAG, "Successfully deleted comment " + commentId);
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to delete comment " + commentId, e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                DeleteCommentMutation.builder()
                        .id(commentId)
                .build()
        ).enqueue(callback);
    }

    void createComment(final Activity activity, final String comment, final String playerId, final String replyToId, final Integer replyDepth) {
        final String id = UUID.randomUUID().toString();
        final long time = getCurrentTime();
        GraphQLCall.Callback<CreateCommentMutation.Data> callback = new GraphQLCall
                .Callback<CreateCommentMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<CreateCommentMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Create comment failed: " + error.message());
                    }
                } else {
                    Comment newComment = new Comment();
                    newComment.setContent(comment);
                    newComment.setAuthor(CUPHelper.getCurrUser());
                    newComment.setDownvotes(0);
                    newComment.setUpvotes(1);
                    newComment.setPlayerId(playerId);
                    newComment.setTime(formatCurrentTime(time));
                    newComment.setId(id);
                    newComment.setReplyToId(replyToId);
                    final List<Comment> dummyList = new ArrayList<>();
                    dummyList.add(newComment);
                    LocalSettingsHelper.upvotePost(activity, id);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ((PlayerInfo) activity).addComments(dummyList, null);
                            } catch (Exception e) {
                                Log.d(TAG, "Failed to add comments for player " + playerId, e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to create comment.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                CreateCommentMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .id(id)
                        .author(CUPHelper.getCurrUser())
                        .content(comment)
                        .date(time)
                .build()
        ).enqueue(callback);
    }
}
