package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;

public class AppSyncHelper {

    private static final PlayerMetadataActivity metadataActivity = new PlayerMetadataActivity();
    private static final CommentActivity commentActivity = new CommentActivity();

    public static void getOrCreatePlayerMetadataAndIncrementViewCount(Activity activity, String playerId) {
        metadataActivity.incrementViewCount(activity, playerId);
    }

    public static void incrementPlayerWatchedCount(Context context, String playerId) {
        metadataActivity.incrementWatchCount(context, playerId);
    }

    public static void decrementPlayerWatchedCount(Context context, String playerId) {
        metadataActivity.decrementWatchCount(context, playerId);
    }

    public static void incrementPlayerDraftCount(Context context, String playerId) {
        metadataActivity.incrementDraftCount(context, playerId);
    }

    public static void decrementPlayerDraftCount(Context context, String playerId) {
        metadataActivity.decrementDraftCount(context, playerId);
    }

    public static void createComment(Activity activity, String comment, String playerId) {
        commentActivity.createComment(activity, comment, playerId);
    }

    public static void getCommentsForPlayer(Activity activity, String playerId, String nextToken, boolean topComments) {
        commentActivity.getCommentsForPlayer(activity, playerId, nextToken, topComments);
    }

    public static void deleteComment(Activity activity, String commentId) {
        commentActivity.deleteComment(activity, commentId);
    }

    public static void upvoteComment(Activity activity, String commentId, boolean decrementDownvote) {
        commentActivity.upvoteComment(activity, commentId, decrementDownvote);
    }

    public static void downvoteComment(Activity activity, String commentId, boolean decrementUpvote) {
        commentActivity.downvoteComment(activity, commentId, decrementUpvote);
    }
}
