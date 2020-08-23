package com.devingotaswitch.rankings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrognito.flashbar.Flashbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.fileio.LocalSettingsHelper
import com.devingotaswitch.rankings.domain.PlayerNews
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter.OnItemClickListener
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DisplayUtils.getVerticalDividerDecoration
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.confirmInternet
import com.devingotaswitch.utils.JsoupUtils.getElemsFromDoc
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.angmarch.views.NiceSpinner
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class FantasyNews : AppCompatActivity() {
    private var rankings: Rankings? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fantasy_news)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankings = Rankings.init()
        val toolbar = findViewById<Toolbar>(R.id.toolbar_player_news)
        toolbar.title = ""
        val main_title = findViewById<TextView>(R.id.main_toolbar_title)
        main_title.text = "News"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    public override fun onResume() {
        super.onResume()
        try {
            init()
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            onBackPressed()
        }
    }

    private fun init() {
        nameToId = HashMap()
        for (key in rankings!!.players.keys) {
            val player = rankings!!.getPlayer(key)
            nameToId!![player.name] = key
        }
        val sources: List<String> = ArrayList(listOf(Constants.MFL_AGGREGATE_TITLE,
                Constants.SPOTRAC_TRANSACTIONS_TITLE, Constants.FP_ALL_NEWS,
                Constants.FP_RUMORS_TITLE, Constants.FP_BREAKING_NEWS_TITLE, Constants.FP_INJURY_TITLE))
        val sourcesSpinner = findViewById<NiceSpinner>(R.id.news_source_selector)
        sourcesSpinner.attachDataSource(sources)
        sourcesSpinner.setBackgroundColor(Color.parseColor("#FAFAFA"))
        sourcesSpinner.selectedIndex = sources.indexOf(LocalSettingsHelper.getSelectedNewsSource(this))
        val submit = findViewById<Button>(R.id.news_selection_submit)
        val localCopy: Activity = this
        submit.setOnClickListener {
            if (confirmInternet(localCopy)) {
                val selectedSource = sources[sourcesSpinner.selectedIndex]
                getNews(selectedSource)
                LocalSettingsHelper.saveSelectedNewsSource(localCopy, selectedSource)
            } else {
                generateTextOnlyFlashbar(localCopy, "No can do", "No internet connection available", Flashbar.Gravity.BOTTOM)
                        .show()
            }
        }
        val cachedNews = LocalSettingsHelper.loadNews(this)
        if (cachedNews.size > 0) {
            displayNews(cachedNews)
        }
    }

    private fun getNews(source: String) {
        val news = ParseNews(this, source)
        news.execute()
    }

    private fun displayNews(news: List<PlayerNews>?) {
        val listview = findViewById<RecyclerView>(R.id.news_list)
        val data: MutableList<MutableMap<String, String?>> = ArrayList()
        for (newsItem in news!!) {
            val datum: MutableMap<String, String?> = HashMap(3)
            datum[Constants.PLAYER_BASIC] = newsItem.news
            datum[Constants.PLAYER_INFO] = newsItem.impact +
                    Constants.LINE_BREAK +
                    Constants.LINE_BREAK +
                    newsItem.date
            data.add(datum)
        }
        val adapter = RecyclerViewAdapter(this, data,
                R.layout.list_item_layout, arrayOf(Constants.PLAYER_BASIC, Constants.PLAYER_INFO), intArrayOf(R.id.player_basic, R.id.player_info))

        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                if (nameToId!!.isNotEmpty()) {
                    val newsMainArr = (view!!.findViewById<View>(R.id.player_basic) as TextView).text.toString()
                            .replace(":", "").replace(",", "")
                            .replace("\\?".toRegex(), "").split(" ")
                    for (i in 0 until newsMainArr.size - 1) {
                        val possibleName = newsMainArr[i] +
                                " " +
                                newsMainArr[i + 1]
                        if (nameToId!!.containsKey(possibleName)) {
                            displayPlayerInfo(nameToId!![possibleName])
                        }
                    }
                }
            }
        })

        listview.layoutManager = LinearLayoutManager(this)
        listview.addItemDecoration(getVerticalDividerDecoration(this))
        listview.adapter = adapter
        findViewById<View>(R.id.main_toolbar_title).setOnClickListener { listview.smoothScrollToPosition(0) }
        LocalSettingsHelper.cacheNews(this, news)
    }

    private fun displayPlayerInfo(playerKey: String?) {
        val intent = Intent(this, PlayerInfo::class.java)
        intent.putExtra(Constants.PLAYER_ID, playerKey)
        startActivity(intent)
    }

    internal inner class ParseNews(act: FantasyNews?, val source: String) : AsyncTask<Any?, Void?, List<PlayerNews>?>() {
        private val pdia: AlertDialog = MaterialAlertDialogBuilder(act!!)
                .setTitle("Please wait")
                .setMessage("Fetching the news...")
                .setCancelable(false)
                .create()

        override fun onPreExecute() {
            super.onPreExecute()
            pdia.show()
        }

        override fun onPostExecute(result: List<PlayerNews>?) {
            super.onPostExecute(result)
            pdia.dismiss()
            displayNews(result)
        }

        override fun doInBackground(vararg data: Any?): List<PlayerNews>? {
            var news: List<PlayerNews>? = null
            try {
                when (source) {
                    Constants.FP_ALL_NEWS -> news = parseFantasyPros("https://www.fantasypros.com/nfl/player-news.php")
                    Constants.FP_BREAKING_NEWS_TITLE -> news = parseFantasyPros("https://www.fantasypros.com/nfl/breaking-news.php")
                    Constants.FP_RUMORS_TITLE -> news = parseFantasyPros("https://www.fantasypros.com/nfl/rumors.php")
                    Constants.FP_INJURY_TITLE -> news = parseFantasyPros("https://www.fantasypros.com/nfl/injury-news.php")
                    Constants.MFL_AGGREGATE_TITLE -> news = parseMFL()
                    Constants.SPOTRAC_TRANSACTIONS_TITLE -> news = parseSpotrac()
                }
                return news
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get news", e)
            }
            return null
        }

    }

    @Throws(IOException::class)
    private fun parseSpotrac(): List<PlayerNews> {
        val newsSet: MutableList<PlayerNews> = ArrayList()
        val doc = Jsoup.connect("https://www.spotrac.com/nfl/transactions/").timeout(0).get()
        val dates = getElemsFromDoc(doc, "span.date")
        val headlines = getElemsFromDoc(doc, "div.transactions div#transactionslist div.cnt a")
        val content = getElemsFromDoc(doc, "div.transactions div#transactionslist div.cnt p")
        for (i in dates.indices) {
            val newsItem = PlayerNews()
            newsItem.date = dates[i]
            newsItem.news = headlines[i]
            newsItem.impact = content[i]
            newsSet.add(newsItem)
        }
        return newsSet
    }

    @Throws(IOException::class)
    private fun parseFantasyPros(url: String): List<PlayerNews> {
        val newsSet: MutableList<PlayerNews> = ArrayList()
        val doc = Jsoup.connect(url).timeout(0).get()
        val reportSet = getElemsFromDoc(doc, "div.player-news-header div.ten a")
        val links = doc.select("div.ten p")
        val impactSet: MutableList<String> = ArrayList()
        for (element in links) {
            if (!element.text().startsWith("Category:")) {
                impactSet.add(element.text())
            }
        }
        val dateSet = getElemsFromDoc(doc, "div.player-news-header div.ten p")
        for (i in dateSet.indices) {
            val news = PlayerNews()
            news.news = reportSet[i * 2]
            val impact = impactSet[i * 3 + 1] +
                    Constants.LINE_BREAK +
                    Constants.LINE_BREAK +
                    impactSet[i * 3 + 2]
            news.impact = impact
            news.date = dateSet[i].split(" By ")[0]
            newsSet.add(news)
        }
        return newsSet
    }

    @Throws(IOException::class)
    private fun parseMFL(): List<PlayerNews> {
        val newsSet: MutableList<PlayerNews> = ArrayList()
        val url = "http://www03.myfantasyleague.com/" + Constants.YEAR_KEY + "/news_articles?L=&PLAYERS=*&SOURCE=*&TEAM=*&POSITION=*&DAYS=7"
        val doc = Jsoup.connect(url).timeout(0).get()
        val title = getElemsFromDoc(doc, "td.headline b a")
        val elems = doc.select("tr.oddtablerow")
        val elems2 = doc.select("tr.eventablerow")
        val news: MutableList<String> = ArrayList()
        for (i in elems.indices) {
            val odd = elems[i].child(2)
            val even = elems2[i].child(2)
            news.add(odd.text())
            news.add(even.text())
        }
        val time = getElemsFromDoc(doc, "td.timestamp")
        for (i in 0..74) {
            var newsStr = news[i]
            newsStr = newsStr.substring(newsStr.indexOf(")") + 2)
            if (newsStr.contains("... (More)")) {
                newsStr = newsStr.split("\\(More\\)".toRegex())[0]
            }
            val newsObj = PlayerNews()
            newsObj.news = title[i]
            newsObj.impact = newsStr
            newsObj.date = time[i] + " ago"
            newsSet.add(newsObj)
        }
        return newsSet
    }

    companion object {
        private const val TAG = "ParseNews"
        private var nameToId: MutableMap<String, String?>? = null
    }
}