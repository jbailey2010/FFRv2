package com.devingotaswitch.fileio

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.text.format.DateUtils
import com.amazonaws.util.StringUtils
import com.devingotaswitch.rankings.domain.Draft
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.PlayerNews
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.youruserpools.CUPHelper.currUser
import java.util.*

object LocalSettingsHelper {
    fun wasPresent(value: String): Boolean {
        return Constants.NOT_SET_KEY != value
    }

    private fun getSharedPreferences(cont: Context): SharedPreferences {
        val userName = currUser
        return cont.getSharedPreferences(userName + Constants.APP_KEY, Context.MODE_PRIVATE)
    }

    fun getSelectedNewsSource(cont: Context): String? {
        return getSharedPreferences(cont).getString(Constants.NEWS_SOURCE, Constants.MFL_AGGREGATE_TITLE)
    }

    fun saveSelectedNewsSource(cont: Context, newsSource: String?) {
        getSharedPreferences(cont).edit().putString(Constants.NEWS_SOURCE, newsSource).apply()
    }

    fun getCurrentLeagueName(cont: Context): String {
        return getSharedPreferences(cont).getString(Constants.LEAGUE_NAME, Constants.NOT_SET_KEY)!!
    }

    fun saveCurrentLeagueName(cont: Context, name: String?) {
        getSharedPreferences(cont).edit().putString(Constants.LEAGUE_NAME, name).apply()
    }

    fun getNumVisiblePlayers(cont: Context): Int {
        return getSharedPreferences(cont).getInt(Constants.NUM_PLAYERS, Constants.DEFAULT_NUM_PLAYERS)
    }

    fun saveNumVisiblePlayers(cont: Context, numVisible: Int) {
        getSharedPreferences(cont).edit().putInt(Constants.NUM_PLAYERS, numVisible).apply()
    }

    fun wereRankingsFetched(cont: Context): Boolean {
        return getSharedPreferences(cont).getBoolean(Constants.RANKINGS_FETCHED, Constants.NOT_SET_BOOLEAN)
    }

    fun saveRankingsFetched(cont: Context, wereFetched: Boolean) {
        getSharedPreferences(cont).edit().putBoolean(Constants.RANKINGS_FETCHED, wereFetched).apply()
    }

    fun saveDraft(cont: Context, leagueName: String, draft: Draft) {
        val editor = getSharedPreferences(cont).edit()
        editor.putString(leagueName + Constants.CURRENT_DRAFT, draft.draftedToSerializedString())
        editor.putString(leagueName + Constants.CURRENT_TEAM, draft.myTeamToSerializedString())
        editor.apply()
    }

    fun loadDraft(cont: Context, teamCount: Int, auctionBudget: Int, leagueName: String, players: Map<String, Player>): Draft {
        val draftStr = getSharedPreferences(cont).getString(leagueName + Constants.CURRENT_DRAFT, null)
        val teamStr = getSharedPreferences(cont).getString(leagueName + Constants.CURRENT_TEAM, null)
        val draft = Draft()
        if (!StringUtils.isBlank(teamStr)) {
            val myTeam = teamStr!!.split(Constants.HASH_DELIMITER).toTypedArray()
            var i = 0
            while (i < myTeam.size) {
                val key = myTeam[i]
                if (key.isNotEmpty()) {
                    val cost = myTeam[i + 1].toInt()
                    draft.draftPlayer(players[key]
                            ?: error(""), teamCount, auctionBudget, true, cost)
                }
                i += 2
            }
        }
        if (!StringUtils.isBlank(draftStr)) {
            val drafted = draftStr!!.split(Constants.HASH_DELIMITER).toTypedArray()
            for (key in drafted) {
                if (key.isNotEmpty()) {
                    val player = players[key]
                    if (!draft.isDrafted(player!!)) {
                        draft.draftPlayer(player, teamCount, auctionBudget, false, 0)
                    }
                }
            }
        }
        return draft
    }

    fun clearDraft(cont: Context, leagueName: String) {
        val editor = getSharedPreferences(cont).edit()
        editor.remove(leagueName + Constants.CURRENT_DRAFT)
        editor.remove(leagueName + Constants.CURRENT_TEAM)
        editor.apply()
    }

    fun saveCommentSortType(cont: Context, commentSortType: String?) {
        getSharedPreferences(cont).edit().putString(Constants.COMMENT_SORT_KEY, commentSortType).apply()
    }

    fun getCommentSortType(cont: Context): String? {
        return getSharedPreferences(cont).getString(Constants.COMMENT_SORT_KEY, Constants.COMMENT_SORT_DATE)
    }

    fun getNumberOfCommentsOnPlayer(cont: Context, playerKey: String): Int {
        return getSharedPreferences(cont).getInt(Constants.PLAYER_COMMENT_COUNT_PREFIX + playerKey + Constants.YEAR_KEY, 0)
    }

    fun setNumberOfCommentsOnPlayer(cont: Context, playerKey: String, newCount: Int) {
        getSharedPreferences(cont).edit().putInt(Constants.PLAYER_COMMENT_COUNT_PREFIX + playerKey + Constants.YEAR_KEY, newCount).apply()
    }

    fun getLastRankingsFetchedDate(cont: Context): String {
        val timeFetched = getSharedPreferences(cont).getLong(Constants.LAST_RANKINGS_FETCHED_TIME,
                0L)
        return if (timeFetched > 0L) {
            DateUtils.getRelativeTimeSpanString(timeFetched, Date().time, DateUtils.MINUTE_IN_MILLIS).toString()
        } else Constants.NOT_SET_KEY
    }

    fun saveLastRankingsSavedDate(cont: Context) {
        getSharedPreferences(cont).edit().putLong(Constants.LAST_RANKINGS_FETCHED_TIME, Date().time).apply()
    }

    fun cacheNews(cont: Context, news: List<PlayerNews>) {
        var newsStr = StringBuilder()
        for (newsItem in news) {
            newsStr = newsStr.append(newsItem.news)
                    .append(Constants.CACHE_DELIMITER)
                    .append(newsItem.date)
                    .append(Constants.CACHE_DELIMITER)
                    .append(newsItem.impact)
                    .append(Constants.CACHE_ITEM_DELIMITER)
        }
        getSharedPreferences(cont).edit().putString(Constants.PLAYER_NEWS, newsStr.toString()).apply()
    }

    fun loadNews(cont: Context): List<PlayerNews> {
        val playerNews: MutableList<PlayerNews> = ArrayList()
        if (getSharedPreferences(cont).contains(Constants.PLAYER_NEWS)) {
            val newsStr = getSharedPreferences(cont).getString(Constants.PLAYER_NEWS, "")
            val itemArr = newsStr!!.split(Constants.CACHE_ITEM_DELIMITER).toTypedArray()
            for (item in itemArr) {
                val newsArr = item.split(Constants.CACHE_DELIMITER).toTypedArray()
                val news = PlayerNews()
                news.news = newsArr[0]
                news.date = newsArr[1]
                news.impact = newsArr[2]
                playerNews.add(news)
            }
        }
        return playerNews
    }
}