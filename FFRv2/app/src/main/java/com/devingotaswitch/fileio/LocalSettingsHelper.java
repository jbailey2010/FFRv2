package com.devingotaswitch.fileio;

import android.content.Context;
import android.content.SharedPreferences;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.domain.Draft;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.youruserpools.CUPHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
        return getSharedPreferences(cont).getString(Constants.NEWS_SOURCE, Constants.MFL_AGGREGATE_TITLE);
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

    public static void saveCommentSortType(Context cont, String commentSortType) {
        getSharedPreferences(cont).edit().putString(Constants.COMMENT_SORT_KEY, commentSortType).apply();
    }

    public static String getCommentSortType(Context cont) {
        return getSharedPreferences(cont).getString(Constants.COMMENT_SORT_KEY, Constants.COMMENT_SORT_DATE);
    }

    public static int getNumberOfCommentsOnPlayer(Context cont, String playerKey) {
        return getSharedPreferences(cont).getInt(Constants.PLAYER_COMMENT_COUNT_PREFIX + playerKey + Constants.YEAR_KEY, 0);
    }

    public static void setNumberOfCommentsOnPlayer(Context cont, String playerKey, int newCount) {
        getSharedPreferences(cont).edit().putInt(Constants.PLAYER_COMMENT_COUNT_PREFIX + playerKey + Constants.YEAR_KEY, newCount).apply();
    }

    public static String getLastRankingsFetchedDate(Context cont) {
        return getSharedPreferences(cont).getString(Constants.LAST_RANKINGS_FETCHED_TIME, Constants.NOT_SET_KEY);
    }

    public static void saveLastRankingsSavedDate(Context cont) {
        String today = Constants.DATE_FORMAT.format(Calendar.getInstance().getTime());
        getSharedPreferences(cont).edit().putString(Constants.LAST_RANKINGS_FETCHED_TIME, today).apply();
    }

    public static void cacheNews(Context cont, List<PlayerNews> news) {
        StringBuilder newsStr = new StringBuilder();
        for (PlayerNews newsItem : news) {
            newsStr = newsStr.append(newsItem.getNews())
                    .append(Constants.CACHE_DELIMITER)
                    .append(newsItem.getDate())
                    .append(Constants.CACHE_DELIMITER)
                    .append(newsItem.getImpact())
                    .append(Constants.CACHE_ITEM_DELIMITER);
        }
        getSharedPreferences(cont).edit().putString(Constants.PLAYER_NEWS, newsStr.toString()).apply();
    }

    public static List<PlayerNews> loadNews(Context cont) {
        List<PlayerNews> playerNews = new ArrayList<>();
        if (getSharedPreferences(cont).contains(Constants.PLAYER_NEWS)) {
            String newsStr = getSharedPreferences(cont).getString(Constants.PLAYER_NEWS, "");
            String[] itemArr = newsStr.split(Constants.CACHE_ITEM_DELIMITER);
            for (String item : itemArr) {
                String[] newsArr = item.split(Constants.CACHE_DELIMITER);
                PlayerNews news = new PlayerNews();
                news.setNews(newsArr[0]);
                news.setDate(newsArr[1]);
                news.setImpact(newsArr[2]);
                playerNews.add(news);
            }
        }
        return playerNews;
    }
}
