package com.devingotaswitch.rankings.sources

import android.os.AsyncTask
import android.util.Log
import com.devingotaswitch.rankings.PlayerInfo
import com.devingotaswitch.rankings.domain.PlayerNews
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.getDocument
import java.util.*

object ParsePlayerNews {
    private const val TAG = "ParsePlayerNews"
    fun startNews(playerName: String, playerTeam: String, activity: PlayerInfo) {
        val baseUrl = ("http://www.fantasypros.com/nfl/news/"
                + playerNameUrl(playerName, playerTeam) + ".php")
        val notesUrl = ("http://www.fantasypros.com/nfl/notes/"
                + playerNameUrl(playerName, playerTeam) + ".php")
        val objParse = NewsParser(activity, baseUrl, notesUrl)
        objParse.execute()
    }

    fun playerNameUrl(playerName: String, teamName: String): String {
        return if (!playerName.contains(Constants.DST)) {
            val nameSet = playerName.toLowerCase(Locale.US).replace("\\.".toRegex(), "")
                    .replace("'", "").split(" ")
            val nameBuilder = StringBuilder(100)
            for (name in nameSet) {
                nameBuilder.append(name)
                        .append("-")
            }
            var base = nameBuilder.toString()
            base = base.substring(0, base.length - 1)
            base
        } else {
            val teamMascot = playerName.split(" D/ST")[0]
            val teamCity = teamName.split(teamMascot)[0]
            val nameSet = teamCity.toLowerCase(Locale.US).replace("\\.".toRegex(), "")
                    .split(" ")
            val nameBuilder = StringBuilder(100)
            for (name in nameSet) {
                nameBuilder.append(name)
                        .append("-")
            }
            nameBuilder.append("defense")
            nameBuilder.toString()
        }
    }

    internal class NewsParser(val act: PlayerInfo, private var urlNews: String, private val urlNotes: String) : AsyncTask<Any?, String?, List<PlayerNews>>() {
        override fun onPostExecute(result: List<PlayerNews>) {
            super.onPostExecute(result)
            if (result.isNotEmpty()) {
                act.populateNews(result)
            }
        }

        override fun doInBackground(vararg data: Any?): List<PlayerNews> {
            val newsList: MutableList<PlayerNews> = ArrayList()
            try {
                urlNews = urlNews.toLowerCase(Locale.US)
                var doc = getDocument(urlNotes)
                var noteElems = doc.select("div.body-row div.content")
                try {
                    for (element in noteElems) {
                        // First, get the 'notes'
                        val title = element.child(0).text()
                        val date = element.parent().parent().child(1).child(1).text()
                        val author = element.parent().parent().child(1).child(0).text()
                        val news = PlayerNews()
                        news.news = title
                        news.impact = author + Constants.LINE_BREAK + date
                        newsList.add(news)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get player notes from $urlNews", e)
                }
                doc = getDocument(urlNews)
                noteElems = doc.select("div.body-row div.content")
                for (element in noteElems) {
                    // Then get the 'news'
                    val title = element.child(1).text()
                    val body = element.child(3).text()
                    val date = element.parent().parent().child(1).child(1).text()
                    val author = element.parent().parent().child(1).child(0).text()
                    val news = PlayerNews()
                    news.news = title
                    news.impact = body + Constants.LINE_BREAK + Constants.LINE_BREAK + author + Constants.LINE_BREAK + date
                    newsList.add(news)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get news", e)
                return newsList
            }
            return newsList
        }
    }
}