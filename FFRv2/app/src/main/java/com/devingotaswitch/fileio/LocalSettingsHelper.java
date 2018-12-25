package com.devingotaswitch.fileio;

import android.content.Context;
import android.content.SharedPreferences;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.domain.Draft;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.youruserpools.CUPHelper;

import java.util.Map;

public class LocalSettingsHelper {

    public static boolean wasPresent(String value) {
        return !Constants.NOT_SET_KEY.equals(value);
    }

    private static SharedPreferences getSharedPreferences(Context cont) {
        String userName = CUPHelper.getCurrUser();
        return cont.getSharedPreferences(userName + Constants.APP_KEY, Context.MODE_PRIVATE);
    }

    public static String getSelectedNewsSource(Context cont) {
        return getSharedPreferences(cont).getString(Constants.NEWS_SOURCE, Constants.RW_HEADLINE_TITLE);
    }

    public static void saveSelectedNewsSource(Context cont, String newsSource) {
        getSharedPreferences(cont).edit().putString(Constants.NEWS_SOURCE, newsSource).apply();
    }

    public static String getCurrentLeagueName(Context cont) {
        return getSharedPreferences(cont).getString(Constants.LEAGUE_NAME, Constants.NOT_SET_KEY);
    }

    public static void saveCurrentLeagueName(Context cont, String name) {
        getSharedPreferences(cont).edit().putString(Constants.LEAGUE_NAME, name).apply();
    }

    public static int getNumVisiblePlayers(Context cont) {
        return getSharedPreferences(cont).getInt(Constants.NUM_PLAYERS, Constants.DEFAULT_NUM_PLAYERS);
    }

    public static void saveNumVisiblePlayers(Context cont, int numVisible) {
        getSharedPreferences(cont).edit().putInt(Constants.NUM_PLAYERS, numVisible).apply();
    }

    public static boolean wereRankingsFetched(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.RANKINGS_FETCHED, Constants.NOT_SET_BOOLEAN);
    }

    public static void saveRankingsFetched(Context cont, boolean wereFetched) {
        getSharedPreferences(cont).edit().putBoolean(Constants.RANKINGS_FETCHED, wereFetched).apply();
    }

    public static void saveDraft(Context cont, String leagueName, Draft draft) {
        SharedPreferences.Editor editor = getSharedPreferences(cont).edit();
        editor.putString(leagueName + Constants.CURRENT_DRAFT, draft.draftedToSerializedString());
        editor.putString(leagueName + Constants.CURRENT_TEAM, draft.myTeamToSerializedString());
        editor.apply();
    }

    public static Draft loadDraft(Context cont, int teamCount, int auctionBudget, String leagueName, Map<String, Player> players) {
        String draftStr = getSharedPreferences(cont).getString(leagueName + Constants.CURRENT_DRAFT, null);
        String teamStr = getSharedPreferences(cont).getString(leagueName + Constants.CURRENT_TEAM, null);
        Draft draft = new Draft();
        if (!StringUtils.isBlank(teamStr)) {
            String[] myTeam = teamStr.split(Constants.HASH_DELIMITER);
            for (int i = 0; i < myTeam.length; i+=2) {
                String key = myTeam[i];
                int cost = Integer.parseInt(myTeam[i+1]);
                draft.draftPlayer(players.get(key), teamCount, auctionBudget,true, cost);
            }
        }
        if (!StringUtils.isBlank(draftStr)) {
            String[] drafted = draftStr.split(Constants.HASH_DELIMITER);
            for (String key : drafted) {
                Player player = players.get(key);
                if (!draft.isDrafted(player)) {
                    draft.draftPlayer(player, teamCount, auctionBudget,false, 0);
                }
            }
        }
        return draft;
    }

    public static void clearDraft(Context cont, String leagueName) {
        SharedPreferences.Editor editor = getSharedPreferences(cont).edit();
        editor.remove(leagueName + Constants.CURRENT_DRAFT);
        editor.remove(leagueName + Constants.CURRENT_TEAM);
        editor.apply();
    }

    public static boolean isPostUpvoted(Context cont, String commentId) {
        return getSharedPreferences(cont).getBoolean(commentId + Constants.COMMENT_UPVOTE, Constants.NOT_SET_BOOLEAN);
    }

    public static boolean isPostDownvoted(Context cont, String commentId) {
        return getSharedPreferences(cont).getBoolean(commentId + Constants.COMMENT_DOWNVOTE, Constants.NOT_SET_BOOLEAN);
    }

    public static void downvotePost(Context cont, String commentId) {
        if (!isPostDownvoted(cont, commentId)) {
            SharedPreferences.Editor editor = getSharedPreferences(cont).edit();
            editor.remove(commentId + Constants.COMMENT_UPVOTE);
            editor.putBoolean(commentId + Constants.COMMENT_DOWNVOTE, true);
            editor.apply();
        }
    }

    public static void upvotePost(Context cont, String commentId) {
        if (!isPostUpvoted(cont, commentId)) {
            SharedPreferences.Editor editor = getSharedPreferences(cont).edit();
            editor.remove(commentId + Constants.COMMENT_DOWNVOTE);
            editor.putBoolean(commentId + Constants.COMMENT_UPVOTE, true);
            editor.apply();
        }
    }

    public static boolean isPlayerTagged(Context cont, String playerId, String tagType) {
        return getSharedPreferences(cont).getBoolean(playerId + tagType + Constants.SP_TAG_SUFFIX, Constants.NOT_SET_BOOLEAN);
    }

    public static void tagPlayer(Context cont, String playerId, String tagType) {
        if (!isPlayerTagged(cont, playerId, tagType)) {
            getSharedPreferences(cont).edit().putBoolean(playerId + tagType + Constants.SP_TAG_SUFFIX, true).apply();
        }
    }

    public static void untagPlayer(Context cont, String playerId, String tagType) {
        if (isPlayerTagged(cont, playerId, tagType)) {
            getSharedPreferences(cont).edit().putBoolean(playerId + tagType + Constants.SP_TAG_SUFFIX, false).apply();
        }
    }

    public static void saveCommentSortType(Context cont, String commentSortType) {
        getSharedPreferences(cont).edit().putString(Constants.COMMENT_SORT_KEY, commentSortType).apply();;
    }

    public static String getCommentSortType(Context cont) {
        return getSharedPreferences(cont).getString(Constants.COMMENT_SORT_KEY, Constants.COMMENT_SORT_DATE);
    }
    
    public static boolean hideDraftedSearch(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_DRAFTED_SEARCH, Constants.NOT_SET_BOOLEAN);
    }
    
    public static void setHideDraftedSearch(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_DRAFTED_SEARCH, doHide).apply();
    }
    
    public static boolean hideDraftedSortOutput(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_DRAFTED_SORT_OUTPUT, Constants.NOT_SET_BOOLEAN);
    }
    
    public static void setHideDraftedSortOutput(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_DRAFTED_SORT_OUTPUT, doHide).apply();
    }
    
    public static boolean hideDraftedComparatorList(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_DRAFTED_COMPARATOR_LIST, Constants.NOT_SET_BOOLEAN);
    }
    
    public static void setHideDraftedComparatorList(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_DRAFTED_COMPARATOR_LIST, doHide).apply();
    }
    
    public static boolean hideDraftedComparatorSuggestion(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_DRAFTED_COMPARATOR_SUGGESTION, Constants.NOT_SET_BOOLEAN);
    }
    
    public static void setHideDraftedComparatorSuggestion(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_DRAFTED_COMPARATOR_SUGGESTION, doHide).apply();
    }

    public static boolean hideRanklessSearch(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_RANKLESS_SEARCH, Constants.NOT_SET_BOOLEAN);
    }

    public static void setHideRanklessSearch(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_RANKLESS_SEARCH, doHide).apply();
    }

    public static boolean hideRanklessSortOutput(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_RANKLESS_SORT_OUTPUT, Constants.NOT_SET_BOOLEAN);
    }

    public static void setHideRanklessSortOutput(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_RANKLESS_SORT_OUTPUT, doHide).apply();
    }

    public static boolean hideRanklessComparatorList(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_RANKLESS_COMPARATOR_LIST, Constants.NOT_SET_BOOLEAN);
    }

    public static void setHideRanklessComparatorList(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_RANKLESS_COMPARATOR_LIST, doHide).apply();
    }

    public static boolean hideRanklessComparatorSuggestion(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.HIDE_RANKLESS_COMPARATOR_SUGGESTION, Constants.NOT_SET_BOOLEAN);
    }

    public static void setHideRanklessComparatorSuggestion(Context cont, boolean doHide) {
        getSharedPreferences(cont).edit().putBoolean(Constants.HIDE_RANKLESS_COMPARATOR_SUGGESTION, doHide).apply();
    }

    public static boolean refreshRanksOnOverscroll(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.REFRESH_RANKS_ON_OVERSCROLL, Constants.NOT_SET_BOOLEAN);
    }

    public static void setRefreshRanksOnOverscroll(Context cont, boolean doRefresh) {
        getSharedPreferences(cont).edit().putBoolean(Constants.REFRESH_RANKS_ON_OVERSCROLL, doRefresh).apply();
    }

    public static int getNumberOfCommentsOnPlayer(Context cont, String playerKey) {
        return getSharedPreferences(cont).getInt(Constants.PLAYER_COMMENT_COUNT_PREFIX + playerKey + Constants.YEAR_KEY, 0);
    }

    public static void setNumberOfCommentsOnPlayer(Context cont, String playerKey, int newCount) {
        getSharedPreferences(cont).edit().putInt(Constants.PLAYER_COMMENT_COUNT_PREFIX + playerKey + Constants.YEAR_KEY, newCount).apply();
    }
}
