package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;

import com.devingotaswitch.rankings.domain.UserSettings;
import com.devingotaswitch.rankings.domain.appsync.tags.Tag;

import java.util.List;
import java.util.Map;

public class AppSyncHelper {

    private static final PlayerMetadataActivity metadataActivity = new PlayerMetadataActivity();
    private static final CommentActivity commentActivity = new CommentActivity();
    private static final UserSettingsActivity userSettingsActivity = new UserSettingsActivity();
    private static final UserCustomPlayerDataActivity customPlayerDataActivity = new UserCustomPlayerDataActivity();

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

    public static void incrementTagCount(Activity activity, String playerId, Tag tag, List<String> tags) {
        metadataActivity.incrementTagCount(activity, playerId, tag.getRemoteTitle(), tags);
    }

    public static void decrementTagCount(Activity activity, String playerId, Tag tag, List<String> tags) {
        metadataActivity.decrementTagCount(activity, playerId, tag.getRemoteTitle(), tags);
    }

    public static void createComment(Activity activity, String comment, String playerId, String replyToId, Integer replyDepth) {
        commentActivity.createComment(activity, comment, playerId, replyToId, replyDepth);
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

    public static void updateUserSettings(Activity activity, UserSettings userSettings) {
        userSettingsActivity.updateUserSettings(activity, userSettings);
    }

    public static void getUserSettings(Activity activity) {
        userSettingsActivity.getUserSettings(activity);
    }

    public static void updateUserCustomPlayerData(Activity activity, List<String> watchList, Map<String, String> notes) {
        customPlayerDataActivity.updateCustomPlayerData(activity, watchList, notes);
    }

    public static void getUserCustomPlayerData(Activity activity) {
        customPlayerDataActivity.getCustomPlayerData(activity);
    }
}
