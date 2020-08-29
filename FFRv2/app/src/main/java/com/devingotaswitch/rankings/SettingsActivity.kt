package com.devingotaswitch.rankings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.devingotaswitch.appsync.AppSyncHelper.updateUserSettings
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.UserSettings
import com.devingotaswitch.utils.Constants

class SettingsActivity : AppCompatActivity() {
    private var dSearch: CheckBox? = null
    private var rSearch: CheckBox? = null
    private var dsOutput: CheckBox? = null
    private var rsOutput: CheckBox? = null
    private var dcSuggestion: CheckBox? = null
    private var rcSuggestion: CheckBox? = null
    private var noteSort: CheckBox? = null
    private var noteRanks: CheckBox? = null
    private var overscrollRefresh: CheckBox? = null
    private var sortWatchListByTime: CheckBox? = null
    private var isRankingsReloadNeeded = false
    private var rankings: Rankings? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val toolbar = findViewById<Toolbar>(R.id.toolbar_rankings_settings)
        toolbar.title = ""
        val mainTitle = findViewById<TextView>(R.id.main_toolbar_title)
        mainTitle.text = "Settings"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val localCopy: Activity = this
        toolbar.setNavigationOnClickListener {
            val intent = Intent(applicationContext, RankingsHome::class.java)
            intent.putExtra(Constants.RANKINGS_LIST_RELOAD_NEEDED, isRankingsReloadNeeded)
            localCopy.startActivity(intent)
            onBackPressed()
        }
        rankings = Rankings.init()
        init()
    }

    private fun init() {
        dSearch = findViewById(R.id.hide_search_drafted)
        rSearch = findViewById(R.id.hide_search_rankless)
        dsOutput = findViewById(R.id.hide_sort_output_drafted)
        rsOutput = findViewById(R.id.hide_sort_output_rankless)
        dcSuggestion = findViewById(R.id.hide_comparator_input_drafted)
        rcSuggestion = findViewById(R.id.hide_comparator_input_rankless)
        noteRanks = findViewById(R.id.show_note_ranks)
        noteSort = findViewById(R.id.show_note_sort)
        overscrollRefresh = findViewById(R.id.general_refresh_on_overscroll)
        sortWatchListByTime = findViewById(R.id.general_sort_watch_list_by_time)
        val settings = rankings!!.userSettings
        dSearch!!.isChecked = settings.isHideDraftedSearch
        rSearch!!.isChecked = settings.isHideRanklessSearch
        dsOutput!!.isChecked = settings.isHideDraftedSort
        rsOutput!!.isChecked = settings.isHideRanklessSort
        dcSuggestion!!.isChecked = settings.isHideDraftedComparator
        rcSuggestion!!.isChecked = settings.isHideRanklessComparator
        noteRanks!!.isChecked = settings.isShowNoteRank
        noteSort!!.isChecked = settings.isShowNoteSort
        overscrollRefresh!!.isChecked = settings.isRefreshOnOverscroll
        sortWatchListByTime!!.isChecked = settings.isSortWatchListByTime
        dSearch!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            isRankingsReloadNeeded = true
            settings.isHideDraftedSearch = b
            updateUserSettings(settings)
        }
        dsOutput!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isHideDraftedSort = b
            updateUserSettings(settings)
        }
        dcSuggestion!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isHideDraftedComparator = b
            updateUserSettings(settings)
        }
        rSearch!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            isRankingsReloadNeeded = true
            settings.isHideRanklessSearch = b
            updateUserSettings(settings)
        }
        rsOutput!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isHideRanklessSort = b
            updateUserSettings(settings)
        }
        rcSuggestion!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isHideRanklessComparator = b
            updateUserSettings(settings)
        }
        noteRanks!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isShowNoteRank = b
            updateUserSettings(settings)
        }
        noteSort!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isShowNoteSort = b
            updateUserSettings(settings)
        }
        overscrollRefresh!!.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            settings.isRefreshOnOverscroll = b
            isRankingsReloadNeeded = true
            updateUserSettings(settings)
        }
        sortWatchListByTime!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            settings.isSortWatchListByTime = isChecked
            updateUserSettings(settings)
        }
    }

    private fun updateUserSettings(userSettings: UserSettings) {
        Rankings.setUserSettings(userSettings)
        updateUserSettings(this, userSettings)
    }
}