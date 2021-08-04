package com.devingotaswitch.rankings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.util.StringUtils
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.appsync.AppSyncHelper.addPlayerComparisonCount
import com.devingotaswitch.appsync.AppSyncHelper.getUserSettings
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.domain.DailyProjection
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import com.devingotaswitch.rankings.sources.ParseMath
import com.devingotaswitch.rankings.sources.ParsePlayerNews
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DisplayUtils.getDatumForPlayer
import com.devingotaswitch.utils.DisplayUtils.getPlayerKeyFromListViewItem
import com.devingotaswitch.utils.DisplayUtils.getPositionalRank
import com.devingotaswitch.utils.DisplayUtils.getVerticalDividerDecoration
import com.devingotaswitch.utils.DraftUtils.AuctionCostInterface
import com.devingotaswitch.utils.DraftUtils.getAuctionCostDialog
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.getPlayerIdFromSearchView
import com.devingotaswitch.utils.GeneralUtils.getPlayerSearchAdapter
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.utils.GraphUtils.getLineDataSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jsoup.Jsoup
import java.util.*

class PlayerComparator : AppCompatActivity() {
    private lateinit var rankings: Rankings
    private var playerA: Player? = null
    private var playerB: Player? = null
    private var inputA: AutoCompleteTextView? = null
    private var inputB: AutoCompleteTextView? = null
    private var projGraph: LineChart? = null
    private var comparatorScroller: ScrollView? = null
    private var inputList: RecyclerView? = null
    private val data: MutableList<MutableMap<String, String?>> = ArrayList()
    private var adapter: RecyclerViewAdapter? = null
    private var rankingsDB: RankingsDBWrapper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_comparator)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankings = Rankings.init()

        // Set toolbar for this screen
        val toolbar = findViewById<Toolbar>(R.id.player_comparator_toolbar)
        toolbar.title = ""
        val mainTitle = findViewById<TextView>(R.id.main_toolbar_title)
        mainTitle.text = "Compare Players"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val act: Activity = this
        toolbar.setNavigationOnClickListener {
            hideKeyboard(act)
            onBackPressed()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideKeyboard(this)
        try {
            init()
            if (intent.hasExtra(Constants.PLAYER_ID)) {
                val playerId = intent.getStringExtra(Constants.PLAYER_ID)!!
                playerA = rankings.getPlayer(playerId)
                inputA!!.setText(playerA!!.name)
                inputB!!.requestFocus()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            hideKeyboard(this)
            onBackPressed()
        }
    }

    private fun setSuggestionAdapter() {
        inputA = findViewById(R.id.comparator_input_a)
        inputB = findViewById(R.id.comparator_input_b)
        val mAdapter: FilterWithSpaceAdapter<*> = getPlayerSearchAdapter(rankings, this,
                rankings.userSettings.isHideDraftedComparator,
                rankings.userSettings.isHideRanklessComparator)
        inputA!!.setAdapter(mAdapter)
        inputB!!.setAdapter(mAdapter)
        inputA!!.setOnLongClickListener {
            clearInputs()
            true
        }
        inputB!!.setOnLongClickListener {
            clearInputs()
            true
        }
    }

    private fun init() {
        rankingsDB = RankingsDBWrapper()
        projGraph = findViewById(R.id.comparator_graph)
        comparatorScroller = findViewById(R.id.comparator_output_scroller)
        inputList = findViewById(R.id.comparator_input_list)
        setSuggestionAdapter()
        if (playerA != null && playerB != null) {
            displayResults(playerA, playerB)
        } else {
            displayOptions()
        }
        val localCopy: Activity = this
        inputA!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
            playerA = getPlayerFromView(view)
            inputA!!.setText(playerA!!.name)
            if (playerB != null && playerA!!.uniqueId == playerB!!.uniqueId) {
                generateTextOnlyFlashbar(localCopy, "No can do", "Select two different players", Flashbar.Gravity.TOP)
                        .show()
            } else if (playerB != null) {
                displayResults(playerA, playerB)
            } else {
                toggleListItemStar(playerA, true)
            }
        }
        inputB!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
            playerB = getPlayerFromView(view)
            inputB!!.setText(playerB!!.name)
            if (playerA != null && playerA!!.uniqueId == playerB!!.uniqueId) {
                generateTextOnlyFlashbar(localCopy, "No can do", "Select two different players", Flashbar.Gravity.TOP)
                        .show()
            } else if (playerA != null) {
                displayResults(playerA, playerB)
            } else {
                toggleListItemStar(playerB, true)
            }
        }
        inputA!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (playerA != null && playerA!!.name != editable.toString()) {
                    toggleListItemStar(playerA, false)
                    playerA = null
                }
            }
        })
        inputB!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (playerB != null && playerB!!.name != editable.toString()) {
                    toggleListItemStar(playerB, false)
                    playerB = null
                }
            }
        })
        if (View.GONE == comparatorScroller!!.visibility) {
            getUserSettings(this)
        } else {
            Log.d(TAG, "Comparator already showing, assuming back was pressed and ignoring settings.")
        }
    }

    private fun toggleListItemStar(player: Player?, doStar: Boolean) {
        for (datum in data) {
            val basic = datum[Constants.PLAYER_BASIC]
            val teamPos = datum[Constants.PLAYER_INFO]
            if (basic!!.contains(player!!.name) && teamPos!!.contains(player.teamName) &&
                    teamPos.contains(player.position)) {
                if (doStar) {
                    datum[Constants.PLAYER_STATUS] = R.drawable.star.toString()
                } else {
                    datum[Constants.PLAYER_STATUS] = null
                }
                adapter!!.notifyDataSetChanged()
                break
            }
        }
    }

    private fun displayOptions() {
        inputList!!.visibility = View.VISIBLE
        comparatorScroller!!.visibility = View.GONE
        inputList!!.adapter = null
        data.clear()
        val playerRanks = getPositionalRank(rankings.orderedIds,
                rankings)
        adapter = RecyclerViewAdapter(this, data,
                R.layout.list_item_layout, arrayOf(Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO,
                Constants.PLAYER_ADDITIONAL_INFO_2), intArrayOf(R.id.player_basic, R.id.player_info,
                R.id.player_status, R.id.player_more_info, R.id.player_additional_info_2))
        for (i in 0 until Constants.COMPARATOR_LIST_MAX.coerceAtMost(rankings.orderedIds.size)) {
            val player = rankings.getPlayer(rankings.orderedIds[i])
            if (rankings.draft.isDrafted(player) && rankings.userSettings.isHideDraftedComparator ||
                    rankings.userSettings.isHideRanklessComparator && Constants.DEFAULT_DISPLAY_RANK_NOT_SET == player.getDisplayValue(rankings)) {
                continue
            }
            if (rankings.getLeagueSettings().rosterSettings.isPositionValid(player.position)) {
                if (rankings.getLeagueSettings().isRookie && player.rookieRank == Constants.DEFAULT_RANK) {
                    // the constant is 'not set', so skip these. No sense showing a 10 year vet in rookie ranks.
                    continue
                }
                val datum: MutableMap<String, String?> = getDatumForPlayer(rankings, player,
                        false, playerRanks[player.uniqueId] ?: error(""), false)
                data.add(datum)
            }
        }
        adapter!!.setOnItemClickListener(object: RecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val clickedPlayer = rankings.getPlayer(getPlayerKeyFromListViewItem(view!!))
                if (playerA != null && playerA!!.uniqueId == clickedPlayer.uniqueId) {
                    toggleListItemStar(playerA, false)
                    playerA = null
                    inputA!!.text = null
                } else {
                    if (playerA == null && playerB == null) {
                        playerA = clickedPlayer
                        inputA!!.setText(playerA!!.name)
                        inputA!!.clearFocus()
                        toggleListItemStar(playerA, true)
                    } else if (playerA == null && playerB != null) {
                        playerA = clickedPlayer
                        inputA!!.setText(playerA!!.name)
                        inputA!!.clearFocus()
                        displayResults(playerA, playerB)
                    } else {
                        playerB = clickedPlayer
                        inputB!!.setText(playerB!!.name)
                        inputB!!.clearFocus()
                        displayResults(playerA, playerB)
                    }
                }
            }
        })
        inputList!!.layoutManager = LinearLayoutManager(this)
        inputList!!.addItemDecoration(getVerticalDividerDecoration(this))
        inputList!!.adapter = adapter
    }

    private fun getPlayerFromView(view: View): Player {
        return rankings.getPlayer(getPlayerIdFromSearchView(view))
    }

    private fun clearInputs() {
        playerA = null
        playerB = null
        inputA!!.setText("")
        inputB!!.setText("")
        projGraph!!.visibility = View.GONE
        findViewById<View>(R.id.note_output_row).visibility = View.GONE
        hideKeyboard(this)
        displayOptions()
    }

    private fun displayResults(a: Player?, b: Player?) {
        hideKeyboard(this)
        if (View.GONE == comparatorScroller!!.visibility) {
            addPlayerComparisonCount(this, playerA!!.uniqueId, playerB!!.uniqueId)
        } else {
            Log.d(TAG, "Comparator already showing, assuming back was pressed and ignoring count incrementing.")
        }
        comparatorScroller!!.visibility = View.VISIBLE
        inputList!!.visibility = View.GONE
        // Ensure note doesn't re-display the last comparison's note if two are done in a row.
        findViewById<View>(R.id.note_output_row).visibility = View.GONE
        val parseFP = ParseFP(this, playerA, playerB)
        parseFP.execute()
        val outputBase = findViewById<LinearLayout>(R.id.comparator_output_base)
        outputBase.visibility = View.VISIBLE
        inputA!!.clearFocus()
        inputB!!.clearFocus()
        val playerA = rankingsDB!!.getPlayer(this, a!!.name, a.teamName, a.position)
        val playerB = rankingsDB!!.getPlayer(this, b!!.name, b.teamName, b.position)

        // Name
        val nameA = findViewById<TextView>(R.id.comparator_name_a)
        val nameB = findViewById<TextView>(R.id.comparator_name_b)
        var titleA = playerA.name
        var titleB = playerB.name
        if (rankings.draft.isDrafted(playerA)) {
            titleA += Constants.COMPARATOR_DRAFTED_SUFFIX
        }
        if (rankings.draft.isDrafted(playerB)) {
            titleB += Constants.COMPARATOR_DRAFTED_SUFFIX
        }
        nameA.text = titleA
        nameB.text = titleB
        nameA.setOnClickListener { goToPlayerInfo(playerA) }
        nameB.setOnClickListener { goToPlayerInfo(playerB) }
        nameA.setOnLongClickListener {
            if (!rankings.draft.isDrafted(playerA)) {
                if (rankings.getLeagueSettings().isAuction) {
                    getAuctionCost(playerA, nameA)
                } else {
                    draftPlayer(playerA, nameA, 0)
                }
                return@setOnLongClickListener true
            }
            false
        }
        nameB.setOnLongClickListener {
            if (!rankings.draft.isDrafted(playerB)) {
                if (rankings.getLeagueSettings().isAuction) {
                    getAuctionCost(playerB, nameB)
                } else {
                    draftPlayer(playerB, nameB, 0)
                }
                return@setOnLongClickListener true
            }
            false
        }

        // Age
        val ageA = findViewById<TextView>(R.id.comparator_age_a)
        val ageB = findViewById<TextView>(R.id.comparator_age_b)
        val ageBase = findViewById<LinearLayout>(R.id.comparator_age_base)
        if ((playerA.age == null || playerA.age == 0) && (playerB.age == null || playerB.age == 0)) {
            ageBase.visibility = View.GONE
        } else {
            ageA.text = if (playerA.age != null && playerA.age!! > 0) playerA.age.toString() else "?"
            ageB.text = if (playerB.age != null && playerB.age!! > 0) playerB.age.toString() else "?"
        }

        // Bye week
        val byeA = findViewById<TextView>(R.id.comparator_bye_a)
        val byeB = findViewById<TextView>(R.id.comparator_bye_b)
        val teamA = rankings.getTeam(playerA)
        val teamB = rankings.getTeam(playerB)
        byeA.text = if (teamA != null) teamA.bye else "?"
        byeB.text = if (teamB != null) teamB.bye else "?"

        // Note
        if (!StringUtils.isBlank(rankings.getPlayerNote(playerA.uniqueId)) ||
                !StringUtils.isBlank(rankings.getPlayerNote(playerB.uniqueId))) {
            findViewById<View>(R.id.note_output_row).visibility = View.VISIBLE
            val noteA = findViewById<TextView>(R.id.comparator_note_a)
            val noteB = findViewById<TextView>(R.id.comparator_note_b)
            noteA.text = rankings.getPlayerNote(playerA.uniqueId)
            noteB.text = rankings.getPlayerNote(playerB.uniqueId)
        }

        // Expert's selection percentages (default to hidden)
        val ecrRow = findViewById<LinearLayout>(R.id.expert_output_row)
        ecrRow.visibility = View.GONE

        // ECR val
        val ecrA = findViewById<TextView>(R.id.comparator_ecr_val_a)
        val ecrB = findViewById<TextView>(R.id.comparator_ecr_val_b)
        ecrA.text = playerA.ecr.toString()
        ecrB.text = playerB.ecr.toString()
        when {
            playerA.ecr < playerB.ecr -> {
                setColors(ecrA, ecrB)
            }
            playerA.ecr > playerB.ecr -> {
                setColors(ecrB, ecrA)
            }
            else -> {
                clearColors(ecrA, ecrB)
            }
        }

        //ADP
        val adpA = findViewById<TextView>(R.id.comparator_adp_a)
        val adpB = findViewById<TextView>(R.id.comparator_adp_b)
        adpA.text = playerA.adp.toString()
        adpB.text = playerB.adp.toString()
        when {
            playerA.adp < playerB.adp -> {
                setColors(adpA, adpB)
            }
            playerA.adp > playerB.adp -> {
                setColors(adpB, adpA)
            }
            else -> {
                clearColors(adpA, adpB)
            }
        }

        // Dynasty/keeper ranks
        val dynA = findViewById<TextView>(R.id.comparator_dynasty_a)
        val dynB = findViewById<TextView>(R.id.comparator_dynasty_b)
        dynA.text = playerA.dynastyRank.toString()
        dynB.text = playerB.dynastyRank.toString()
        when {
            playerA.dynastyRank < playerB.dynastyRank -> {
                setColors(dynA, dynB)
            }
            playerB.dynastyRank < playerA.dynastyRank -> {
                setColors(dynB, dynA)
            }
            else -> {
                clearColors(dynA, dynB)
            }
        }

        // Rookie ranks
        val rookieRow = findViewById<LinearLayout>(R.id.rookie_output_row)
        if (rankings.getLeagueSettings().isRookie && (playerA.rookieRank < 300.0 || playerB.rookieRank < 300.0)) {
            rookieRow.visibility = View.VISIBLE
            val rookA = findViewById<TextView>(R.id.comparator_rookie_a)
            val rookB = findViewById<TextView>(R.id.comparator_rookie_b)
            if (playerA.rookieRank == 300.0 && playerB.rookieRank < 300.0) {
                rookA.text = "N/A"
                rookB.text = playerB.rookieRank.toString()
                clearColors(rookB, rookA)
            } else if (playerA.rookieRank < 300.0 && playerB.rookieRank == 300.0) {
                rookA.text = playerA.rookieRank.toString()
                rookB.text = "N/A"
                clearColors(rookA, rookB)
            } else {
                rookA.text = playerA.rookieRank.toString()
                rookB.text = playerB.rookieRank.toString()
                when {
                    playerA.rookieRank < playerB.rookieRank -> {
                        setColors(rookA, rookB)
                    }
                    playerB.rookieRank < playerA.rookieRank -> {
                        setColors(rookB, rookA)
                    }
                    else -> {
                        clearColors(rookA, rookB)
                    }
                }
            }
        } else {
            rookieRow.visibility = View.GONE
        }

        // Best ball rank
        val bbA = findViewById<TextView>(R.id.comparator_best_ball_a)
        val bbB = findViewById<TextView>(R.id.comparator_best_ball_b)
        bbA.text = playerA.bestBallRank.toString()
        bbB.text = playerB.bestBallRank.toString()
        when {
            playerA.bestBallRank < playerB.bestBallRank -> {
                setColors(bbA, bbB)
            }
            playerB.bestBallRank < playerA.bestBallRank -> {
                setColors(bbB, bbA)
            }
            else -> {
                clearColors(bbA, bbB)
            }
        }

        // Auction value
        val aucA = findViewById<TextView>(R.id.comparator_auc_a)
        val aucB = findViewById<TextView>(R.id.comparator_auc_b)
        aucA.text = Constants.DECIMAL_FORMAT.format(playerA.getAuctionValueCustom(rankings))
        aucB.text = Constants.DECIMAL_FORMAT.format(playerB.getAuctionValueCustom(rankings))
        when {
            playerA.auctionValue > playerB.auctionValue -> {
                setColors(aucA, aucB)
            }
            playerA.auctionValue < playerB.auctionValue -> {
                setColors(aucB, aucA)
            }
            else -> {
                clearColors(aucA, aucB)
            }
        }

        // Leverage
        val levA = findViewById<TextView>(R.id.comparator_lev_a)
        val levB = findViewById<TextView>(R.id.comparator_lev_b)
        val levAVal = ParseMath.getLeverage(playerA, rankings)
        val levBVal = ParseMath.getLeverage(playerB, rankings)
        levA.text = levAVal.toString()
        levB.text = levBVal.toString()
        when {
            levAVal > levBVal -> {
                setColors(levA, levB)
            }
            levAVal < levBVal -> {
                setColors(levB, levA)
            }
            else -> {
                clearColors(levA, levB)
            }
        }

        // SOS
        val sosA = findViewById<TextView>(R.id.comparator_sos_a)
        val sosB = findViewById<TextView>(R.id.comparator_sos_b)
        if (rankings.getTeam(playerA) != null && rankings.getTeam(playerB) != null) {
            val sosForA = rankings.getTeam(playerA)!!.getSosForPosition(playerA.position)
            val sosForB = rankings.getTeam(playerB)!!.getSosForPosition(playerB.position)
            sosA.text = sosForA.toString()
            sosB.text = sosForB.toString()
            if (sosForA > sosForB) {
                setColors(sosA, sosB)
            } else if (sosForA < sosForB) {
                setColors(sosB, sosA)
            }
        } else {
            clearColors(sosA, sosB)
        }

        // Projection
        val projA = findViewById<TextView>(R.id.comparator_proj_a)
        val projB = findViewById<TextView>(R.id.comparator_proj_b)
        projA.text = Constants.DECIMAL_FORMAT.format(playerA.projection)
        projB.text = Constants.DECIMAL_FORMAT.format(playerB.projection)
        when {
            playerA.projection > playerB.projection -> {
                setColors(projA, projB)
            }
            playerA.projection < playerB.projection -> {
                setColors(projB, projA)
            }
            else -> {
                clearColors(projA, projB)
            }
        }

        // Points Last Year
        if (playerA.lastYearPoints > 0.0 && playerB.lastYearPoints > 0.0) {
            val pointsA = findViewById<TextView>(R.id.comparator_last_year_points_a)
            val pointsB = findViewById<TextView>(R.id.comparator_last_year_points_b)
            pointsA.text = Constants.DECIMAL_FORMAT.format(playerA.lastYearPoints)
            pointsB.text = Constants.DECIMAL_FORMAT.format(playerB.lastYearPoints)
            when {
                playerA.lastYearPoints > playerB.lastYearPoints -> {
                    setColors(pointsA, pointsB)
                }
                playerA.lastYearPoints < playerB.lastYearPoints -> {
                    setColors(pointsB, pointsA)
                }
                else -> {
                    clearColors(pointsB, pointsB)
                }
            }
        } else {
            val row = findViewById<LinearLayout>(R.id.last_year_points_output_row)
            row.visibility = View.GONE
        }

        // PAA
        val paaA = findViewById<TextView>(R.id.comparator_paa_a)
        val paaB = findViewById<TextView>(R.id.comparator_paa_b)
        paaA.text = Constants.DECIMAL_FORMAT.format(playerA.paa) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerA.getScaledPAA(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX
        paaB.text = Constants.DECIMAL_FORMAT.format(playerB.paa) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerB.getScaledPAA(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX
        when {
            playerA.getScaledPAA(rankings) > playerB.getScaledPAA(rankings) -> {
                setColors(paaA, paaB)
            }
            playerA.getScaledPAA(rankings) < playerB.getScaledPAA(rankings) -> {
                setColors(paaB, paaA)
            }
            else -> {
                clearColors(paaA, paaB)
            }
        }

        // XVal
        val xvalA = findViewById<TextView>(R.id.comparator_xval_a)
        val xvalB = findViewById<TextView>(R.id.comparator_xval_b)
        xvalA.text = Constants.DECIMAL_FORMAT.format(playerA.xval) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerA.getScaledXVal(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX
        xvalB.text = Constants.DECIMAL_FORMAT.format(playerB.xval) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerB.getScaledXVal(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX
        when {
            playerA.getScaledXVal(rankings) > playerB.getScaledXVal(rankings) -> {
                setColors(xvalA, xvalB)
            }
            playerA.getScaledXVal(rankings) < playerB.getScaledXVal(rankings) -> {
                setColors(xvalB, xvalA)
            }
            else -> {
                clearColors(xvalA, xvalB)
            }
        }

        // VoLS
        val volsA = findViewById<TextView>(R.id.comparator_vols_a)
        val volsB = findViewById<TextView>(R.id.comparator_vols_b)
        volsA.text = Constants.DECIMAL_FORMAT.format(playerA.vols) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerA.getScaledVOLS(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX
        volsB.text = Constants.DECIMAL_FORMAT.format(playerB.vols) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerB.getScaledVOLS(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX
        when {
            playerA.getScaledVOLS(rankings) > playerB.getScaledVOLS(rankings) -> {
                setColors(volsA, volsB)
            }
            playerA.getScaledVOLS(rankings) < playerB.getScaledVOLS(rankings) -> {
                setColors(volsB, volsA)
            }
            else -> {
                clearColors(volsA, volsB)
            }
        }

        // Graph
        projGraph = findViewById(R.id.comparator_graph)
        val historyA = rankings.playerProjectionHistory[playerA.uniqueId]
        val historyB = rankings.playerProjectionHistory[playerB.uniqueId]
        if (historyA != null && historyA.size >= 2 && historyB != null && historyB.size >= 2) {
            projGraph!!.visibility = View.VISIBLE
            graphPlayers(projGraph, playerA, playerB, historyA, historyB)
        } else {
            projGraph!!.visibility = View.GONE
        }
        hideKeyboard(this)
    }

    private fun clearColors(playerA: TextView, playerB: TextView) {
        playerA.setBackgroundColor(Color.parseColor(WORSE_COLOR))
        playerB.setBackgroundColor(Color.parseColor(WORSE_COLOR))
    }

    private fun setColors(winner: TextView, loser: TextView) {
        winner.setBackgroundColor(Color.parseColor(BETTER_COLOR))
        loser.setBackgroundColor(Color.parseColor(WORSE_COLOR))
    }

    private fun graphPlayers(lineGraph: LineChart?, playerA: Player, playerB: Player, historyA: List<DailyProjection>, historyB: List<DailyProjection>) {
        val dataSetA = getLineDataSet(playerA, historyA, "blue")
        val dataSetB = getLineDataSet(playerB, historyB, "green")
        val lineData = LineData()
        lineData.addDataSet(dataSetA)
        lineData.addDataSet(dataSetB)
        lineGraph!!.data = lineData
        lineGraph.setDrawBorders(true)
        val description = Description()
        description.text = ""
        lineGraph.description = description
        lineGraph.invalidate()
        lineGraph.setTouchEnabled(true)
        lineGraph.setPinchZoom(true)
        lineGraph.isDragEnabled = true
        lineGraph.animateX(1500)
        lineGraph.animateY(1500)
        val x = lineGraph.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawAxisLine(false)
        x.setDrawLabels(false)
        x.setDrawGridLines(false)
        val yR = lineGraph.axisRight
        yR.setDrawAxisLine(false)
        yR.setDrawGridLines(false)
        yR.setDrawLabels(false)
        val yL = lineGraph.axisLeft
        yL.setDrawLabels(true)
        yL.setDrawGridLines(true)
        yL.setDrawAxisLine(false)
    }

    private fun getLineDataSet(player: Player, projections: List<DailyProjection>, color: String): LineDataSet {
        val projectionDays: MutableList<Entry?> = ArrayList()
        for (i in projections.indices) {
            val projection = projections[i]
            projectionDays.add(Entry(i.toFloat(), projection.getProjection(rankings.getLeagueSettings().scoringSettings).toFloat()))
        }
        return getLineDataSet(projectionDays,
                player.name + " Projections", color)
    }

    private fun goToPlayerInfo(player: Player) {
        val intent = Intent(this, PlayerInfo::class.java)
        intent.putExtra(Constants.PLAYER_ID, player.uniqueId)
        startActivity(intent)
    }

    private fun getAuctionCost(player: Player, title: TextView) {
        val localCopy: Activity = this
        val callback: AuctionCostInterface = object : AuctionCostInterface {
            override fun onValidInput(cost: Int?) {
                draftPlayer(player, title, cost!!)
            }

            override fun onInvalidInput() {
                generateTextOnlyFlashbar(localCopy, "No can do", "Must provide a number for cost", Flashbar.Gravity.TOP)
                        .show()
            }

            override fun onCancel() {}
        }
        val alertDialog = getAuctionCostDialog(this, player, callback)
        alertDialog.show()
    }

    private fun draftPlayer(player: Player, title: TextView, cost: Int) {
        val listener = object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                undraftPlayer(player, title)
            }
        }
        rankings.draft.draftByMe(rankings, player, this, cost, listener)
        title.text = player.name + Constants.COMPARATOR_DRAFTED_SUFFIX
    }

    private fun undraftPlayer(player: Player, title: TextView) {
        rankings.draft.undraft(rankings, player, this)
        title.text = player.name
    }

    private fun displayECR(ecrResults: Map<String, String>, playerA: Player?, playerB: Player?) {
        val ecrRow = findViewById<LinearLayout>(R.id.expert_output_row)
        ecrRow.visibility = View.VISIBLE
        val ecrA = findViewById<TextView>(R.id.comparator_ecr_a)
        val ecrB = findViewById<TextView>(R.id.comparator_ecr_b)
        val percentStrA = ecrResults[playerA!!.uniqueId]
        val percentStrB = ecrResults[playerB!!.uniqueId]
        ecrA.text = percentStrA
        ecrB.text = percentStrB
        val trimmedA = percentStrA!!.substring(0, percentStrA.length - 1)
        val trimmedB = percentStrB!!.substring(0, percentStrB.length - 1)
        val ecrValA = trimmedA.toInt()
        val ecrValB = trimmedB.toInt()
        when {
            ecrValA > ecrValB -> {
                setColors(ecrA, ecrB)
            }
            ecrValA < ecrValB -> {
                setColors(ecrB, ecrA)
            }
            else -> {
                clearColors(ecrA, ecrB)
            }
        }
    }

    private fun clearECR() {
        val ecrRow = findViewById<LinearLayout>(R.id.expert_output_row)
        ecrRow.visibility = View.GONE
        val ecrA = findViewById<TextView>(R.id.comparator_ecr_a)
        val ecrB = findViewById<TextView>(R.id.comparator_ecr_b)
        ecrA.setBackgroundColor(Color.parseColor(WORSE_COLOR))
        ecrB.setBackgroundColor(Color.parseColor(WORSE_COLOR))
    }

    private inner class ParseFP(activity: PlayerComparator, private val playerA: Player?, private val playerB: Player?) : AsyncTask<Any?, Void?, Map<String, String>?>() {
        private val pdia: AlertDialog = MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle("Please wait")
                .setMessage("Trying to get expert's preferences...")
                .create()
        private val act: PlayerComparator = activity
        override fun onPreExecute() {
            super.onPreExecute()
            pdia.show()
        }

        override fun onPostExecute(result: Map<String, String>?) {
            super.onPostExecute(result)
            pdia.dismiss()
            if (result != null) {
                act.displayECR(result, playerA, playerB)
            } else {
                act.clearECR()
            }
        }

        override fun doInBackground(vararg data: Any?): Map<String, String>? {
            var baseURL = "http://www.fantasypros.com/nfl/draft/"
            baseURL += (ParsePlayerNews.playerNameUrl(playerA!!.name, playerA.teamName) + "-"
                    + ParsePlayerNews.playerNameUrl(playerB!!.name, playerB.teamName) + ".php")
            if (rankings.getLeagueSettings().scoringSettings.receptions >= 1.0) {
                baseURL += "?scoring=PPR"
            } else if (rankings.getLeagueSettings().scoringSettings.receptions > 0) {
                baseURL += "?scoring=HALF"
            }
            val results: MutableMap<String, String> = HashMap()
            Log.d(TAG, "Looking up expert numbers at $baseURL")
            try {
                val doc = Jsoup.connect(baseURL).get()
                val elems = doc.select("div.pick-percent")
                val percentOne = elems[0].text()
                val percentTwo = elems[1].text()
                val tableElem = elems[0].parent().parent().parent().parent()
                val nameOne = tableElem.child(2).child(1).child(1).child(0).child(0).child(0).text()
                val nameTwo = tableElem.child(2).child(1).child(2).child(0).child(0).child(0).text()
                when (playerA.name) {
                    nameOne -> {
                        results[playerA.uniqueId] = percentOne
                        results[playerB.uniqueId] = percentTwo
                    }
                    nameTwo -> {
                        results[playerA.uniqueId] = percentTwo
                        results[playerB.uniqueId] = percentOne
                    }
                    else -> {
                        Log.d(TAG, "Failed to get unique id: " + nameOne + ", " + nameTwo + ": "
                                + playerA.uniqueId + ", " + playerB.uniqueId)
                        return null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get ecr numbers", e)
                return null
            }
            return results
        }

    }

    companion object {
        private const val TAG = "PlayerComparator"
        private const val BETTER_COLOR = "#F3F3F3"
        private const val WORSE_COLOR = "#FAFAFA"
    }
}