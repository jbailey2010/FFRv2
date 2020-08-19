package com.devingotaswitch.rankings

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.utils.Constants
import org.angmarch.views.NiceSpinner
import java.util.*

class HelpActivity : AppCompatActivity() {
    private var helpLeague: TextView? = null
    private var helpRankings: TextView? = null
    private var helpPlayerInfo: TextView? = null
    private var helpDrafting: TextView? = null
    private var helpADPSimulator: TextView? = null
    private var helpCompare: TextView? = null
    private var helpSort: TextView? = null
    private var helpNews: TextView? = null
    private var helpExport: TextView? = null
    private var helpProfile: TextView? = null
    private var helpStats: TextView? = null
    private var helpRefresh: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val toolbar = findViewById<Toolbar>(R.id.toolbar_rankings_help)
        toolbar.title = ""
        val main_title = findViewById<TextView>(R.id.main_toolbar_title)
        main_title.text = "Help"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        init()
    }

    private fun init() {
        helpLeague = findViewById(R.id.help_league_body)
        helpRankings = findViewById(R.id.help_rankings_body)
        helpPlayerInfo = findViewById(R.id.help_info_body)
        helpDrafting = findViewById(R.id.help_draft_body)
        helpADPSimulator = findViewById(R.id.help_adp_body)
        helpCompare = findViewById(R.id.help_compare_body)
        helpSort = findViewById(R.id.help_sort_body)
        helpNews = findViewById(R.id.help_news_body)
        helpExport = findViewById(R.id.help_export_body)
        helpProfile = findViewById(R.id.help_profile_body)
        helpStats = findViewById(R.id.help_stats_body)
        helpRefresh = findViewById(R.id.help_refresh_body)
        val spinner = findViewById<NiceSpinner>(R.id.help_topics)
        spinner.setBackgroundColor(Color.parseColor("#FAFAFA"))
        val posList: MutableList<String> = ArrayList()
        posList.add(Constants.HELP_LEAGUE)
        posList.add(Constants.HELP_RANKINGS)
        posList.add(Constants.HELP_PLAYER_INFO)
        posList.add(Constants.HELP_DRAFTING)
        posList.add(Constants.HELP_SORT_PLAYERS)
        posList.add(Constants.HELP_COMPARE_PLAYERS)
        posList.add(Constants.HELP_ADP_SIMULARTOR)
        posList.add(Constants.HELP_NEWS)
        posList.add(Constants.HELP_EXPORT)
        posList.add(Constants.HELP_PROFILE)
        posList.add(Constants.HELP_STATS)
        posList.add(Constants.HELP_REFRESH)
        spinner.attachDataSource(posList)
        val submit = findViewById<Button>(R.id.help_selection_submit)
        submit.setOnClickListener { v: View? ->
            val selected = posList[spinner.selectedIndex]
            updateLayout(selected)
        }
    }

    private fun updateLayout(selection: String) {
        hideAll()
        when (selection) {
            Constants.HELP_LEAGUE -> makeVisible(helpLeague)
            Constants.HELP_RANKINGS -> makeVisible(helpRankings)
            Constants.HELP_PLAYER_INFO -> makeVisible(helpPlayerInfo)
            Constants.HELP_DRAFTING -> makeVisible(helpDrafting)
            Constants.HELP_ADP_SIMULARTOR -> makeVisible(helpADPSimulator)
            Constants.HELP_COMPARE_PLAYERS -> makeVisible(helpCompare)
            Constants.HELP_SORT_PLAYERS -> makeVisible(helpSort)
            Constants.HELP_NEWS -> makeVisible(helpNews)
            Constants.HELP_EXPORT -> makeVisible(helpExport)
            Constants.HELP_PROFILE -> makeVisible(helpProfile)
            Constants.HELP_STATS -> makeVisible(helpStats)
            Constants.HELP_REFRESH -> makeVisible(helpRefresh)
        }
    }

    private fun makeVisible(selected: TextView?) {
        selected!!.visibility = View.VISIBLE
    }

    private fun hideAll() {
        helpLeague!!.visibility = View.GONE
        helpRankings!!.visibility = View.GONE
        helpPlayerInfo!!.visibility = View.GONE
        helpDrafting!!.visibility = View.GONE
        helpADPSimulator!!.visibility = View.GONE
        helpCompare!!.visibility = View.GONE
        helpSort!!.visibility = View.GONE
        helpNews!!.visibility = View.GONE
        helpExport!!.visibility = View.GONE
        helpProfile!!.visibility = View.GONE
        helpStats!!.visibility = View.GONE
    }
}