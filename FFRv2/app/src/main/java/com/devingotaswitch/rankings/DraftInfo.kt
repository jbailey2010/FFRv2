package com.devingotaswitch.rankings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrognito.flashbar.Flashbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.RosterSettings
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DisplayUtils.getVerticalDividerDecoration
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.*

class DraftInfo : AppCompatActivity() {
    private lateinit var rankings: Rankings
    private var displayPlayers = false
    private var baseLayout: LinearLayout? = null
    private var viewTeam: MenuItem? = null
    private var undraftPlayers: MenuItem? = null
    private var viewAvailable: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_info)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankings = Rankings.init()
        val toolbar = findViewById<Toolbar>(R.id.toolbar_draft_info)
        toolbar.title = ""
        val mainTitle = findViewById<TextView>(R.id.main_toolbar_title)
        mainTitle.text = "Current Draft"
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
        try {
            init()
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            hideKeyboard(this)
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_draft_info_menu, menu)
        viewTeam = menu.findItem(R.id.draft_info_team)
        undraftPlayers = menu.findItem(R.id.draft_info_players)
        viewAvailable = menu.findItem(R.id.draft_info_available)
        viewTeam!!.isVisible = false
        if (rankings.draft.draftedPlayers.size == 0) {
            // No need to let someone draft or undraft if there's no draft picks.
            undraftPlayers!!.isVisible = false
            menu.findItem(R.id.draft_info_clear).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Find which menu item was selected
        return when (item.itemId) {
            R.id.draft_info_clear -> {
                clearDraft()
                true
            }
            R.id.draft_info_available -> {
                availablePlayers()
                viewTeam!!.isVisible = true
                undraftPlayers!!.isVisible = true
                viewAvailable!!.isVisible = false
                true
            }
            R.id.draft_info_team -> {
                displayTeam()
                viewTeam!!.isVisible = false
                undraftPlayers!!.isVisible = true
                viewAvailable!!.isVisible = true
                true
            }
            R.id.draft_info_players -> {
                displayPlayers()
                viewTeam!!.isVisible = true
                undraftPlayers!!.isVisible = false
                viewAvailable!!.isVisible = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearDraft() {
        rankings.draft.resetDraft(this, rankings!!.getLeagueSettings().name)
        displayTeam()
        findViewById<View>(R.id.team_graph).visibility = View.GONE
        generateTextOnlyFlashbar(this, "Draft cleared", "All players are available again", Flashbar.Gravity.BOTTOM)
                .show()
        displayTeam()
    }

    private fun init() {
        baseLayout = findViewById(R.id.draft_info_base)
        if (displayPlayers) {
            displayPlayers()
        } else {
            displayTeam()
        }
    }

    private fun displayTeam() {
        displayPlayers = false
        val view = clearAndAddView(R.layout.content_draft_info_team)
        val teamView = view.findViewById<TextView>(R.id.base_textview_team)
        val teamOutput = StringBuilder(teamStr)
        if (rankings!!.getLeagueSettings().isAuction) {
            teamOutput.append(auctionValue)
        }
        teamView.text = teamOutput.toString()
        val teamSub = view.findViewById<TextView>(R.id.draft_info_team_sub)
        if (rankings.draft.myPlayers.isEmpty()) {
            teamSub.text = "You have drafted no players."
        } else {
            teamSub.text = "Current VBD metrics are listed (PAA, XVal, VoLS)"
        }
        if (rankings.draft.myPositionsDrafted(rankings).size > 1) {
            graphTeam()
        }
    }

    private fun availablePlayers() {
        displayPlayers = false
        val view = clearAndAddView(R.layout.content_draft_info_available)
        val paaLeft = view.findViewById<TextView>(R.id.base_textview_paa_left)
        paaLeft.text = pAALeft
        val playersDrafted = view.findViewById<TextView>(R.id.base_textview_players_drafted)
        val playersDraftedString = "Total players drafted: " + rankings.draft.draftedPlayers.size
        playersDrafted.text = playersDraftedString
        val graphLegend = view.findViewById<TextView>(R.id.base_textview_graph_header)
        var graphHeader = "Available PAA of positions  "
        val roster = rankings!!.getLeagueSettings().rosterSettings
        graphHeader += conditionallyAddPosition(Constants.QB, roster)
        graphHeader += conditionallyAddPosition(Constants.RB, roster)
        graphHeader += conditionallyAddPosition(Constants.WR, roster)
        graphHeader += conditionallyAddPosition(Constants.TE, roster)
        graphHeader += conditionallyAddPosition(Constants.DST, roster)
        graphHeader += conditionallyAddPosition(Constants.K, roster)
        if (graphHeader.endsWith(", ")) {
            // Shouldn't ever *not* be true, but just in case someone is screwing with something
            graphHeader = graphHeader.substring(0, graphHeader.length - 2)
        }
        graphLegend.text = graphHeader
        graphPAALeft()
    }

    private fun conditionallyAddPosition(pos: String, roster: RosterSettings?): String {
        return if (roster!!.isPositionValid(pos)) {
            "$pos, "
        } else ""
    }

    private val pAALeft: String
        get() {
            val paaLeft = StringBuilder()
            val roster = rankings!!.getLeagueSettings().rosterSettings
            if (roster.isPositionValid(Constants.QB)) {
                paaLeft.append(rankings.draft.getPAALeft(Constants.QB, rankings))
                        .append(Constants.LINE_BREAK)
            }
            if (roster.isPositionValid(Constants.RB)) {
                paaLeft.append(rankings.draft.getPAALeft(Constants.RB, rankings))
                        .append(Constants.LINE_BREAK)
            }
            if (roster.isPositionValid(Constants.WR)) {
                paaLeft.append(rankings.draft.getPAALeft(Constants.WR, rankings))
                        .append(Constants.LINE_BREAK)
            }
            if (roster.isPositionValid(Constants.TE)) {
                paaLeft.append(rankings.draft.getPAALeft(Constants.TE, rankings))
                        .append(Constants.LINE_BREAK)
            }
            if (roster.isPositionValid(Constants.DST)) {
                paaLeft.append(rankings.draft.getPAALeft(Constants.DST, rankings))
                        .append(Constants.LINE_BREAK)
            }
            if (roster.isPositionValid(Constants.K)) {
                paaLeft.append(rankings.draft.getPAALeft(Constants.K, rankings))
                        .append(Constants.LINE_BREAK)
            }
            return paaLeft.toString()
        }
    private val auctionValue: String
        get() = "Value: " +
                Constants.DECIMAL_FORMAT.format(rankings.draft.draftValue)
    private val teamStr: String
        get() {
            val teamOutput = StringBuilder()
            val roster = rankings!!.getLeagueSettings().rosterSettings
            val draft = rankings.draft
            if (roster.isPositionValid(Constants.QB)) {
                teamOutput.append(Constants.QB)
                        .append("s: ")
                        .append(getPosString(draft.myQbs, draft.qBPAA, draft.qBXval, draft.qBVoLS))
            }
            if (roster.isPositionValid(Constants.RB)) {
                teamOutput.append(Constants.LINE_BREAK)
                        .append(Constants.RB)
                        .append("s: ")
                        .append(getPosString(draft.myRbs, draft.rBPAA, draft.rBXval, draft.rBVoLS))
            }
            if (roster.isPositionValid(Constants.WR)) {
                teamOutput.append(Constants.LINE_BREAK)
                        .append(Constants.WR)
                        .append("s: ")
                        .append(getPosString(draft.myWrs, draft.wRPAA, draft.wRXval, draft.wRVoLS))
            }
            if (roster.isPositionValid(Constants.TE)) {
                teamOutput.append(Constants.LINE_BREAK)
                        .append(Constants.TE)
                        .append("s: ")
                        .append(getPosString(draft.myTes, draft.tEPAA, draft.tEXval, draft.tEVoLS))
            }
            if (roster.isPositionValid(Constants.DST)) {
                teamOutput.append(Constants.LINE_BREAK)
                        .append(Constants.DST)
                        .append("s: ")
                        .append(getPosString(draft.myDsts, draft.dstpaa, draft.dstXval, draft.dstVoLS))
            }
            if (roster.isPositionValid(Constants.K)) {
                teamOutput.append(Constants.LINE_BREAK)
                        .append(Constants.K)
                        .append("s: ")
                        .append(getPosString(draft.myKs, draft.kpaa, draft.kXval, draft.kVoLS))
            }
            return teamOutput.append(Constants.LINE_BREAK)
                    .append("Total PAA: ")
                    .append(Constants.DECIMAL_FORMAT.format(draft.totalPAA))
                    .append(Constants.LINE_BREAK)
                    .append("Total XVal: ")
                    .append(Constants.DECIMAL_FORMAT.format(draft.totalXVal))
                    .append(Constants.LINE_BREAK)
                    .append("Total VoLS: ")
                    .append(Constants.DECIMAL_FORMAT.format(draft.totalVoLS))
                    .append(Constants.LINE_BREAK)
                    .toString()
        }

    private fun getPosString(players: List<Player>, posPAA: Double, posXVal: Double, posVoLS: Double): String {
        if (players.isEmpty()) {
            return "None"
        }
        val posStr = StringBuilder()
        for (player in players) {
            posStr.append(player.name)
                    .append(", ")
        }
        val playerStr = posStr.toString()
        return playerStr.substring(0, playerStr.length - 2) +
                " (" +
                Constants.DECIMAL_FORMAT.format(posPAA) +
                ", " +
                Constants.DECIMAL_FORMAT.format(posXVal) +
                ", " +
                Constants.DECIMAL_FORMAT.format(posVoLS) +
                ")"
    }

    private fun displayPlayers() {
        displayPlayers = true
        val view = clearAndAddView(R.layout.content_draft_info_undraft)
        val listview: RecyclerView = view.findViewById(R.id.base_list)
        val data: MutableList<MutableMap<String, String?>> = ArrayList()
        for (playerKey in rankings.draft.draftedPlayers) {
            val player = rankings.getPlayer(playerKey)
            val playerBasicContent = player.getDisplayValue(rankings) +
                    Constants.RANKINGS_LIST_DELIMITER +
                    player.name
            val datum: MutableMap<String, String?> = HashMap(3)
            datum[Constants.PLAYER_BASIC] = playerBasicContent
            datum[Constants.PLAYER_INFO] = generateOutputSubtext(player)
            data.add(datum)
        }
        val recyclerAdapter = RecyclerViewAdapter(this, data, R.layout.list_item_layout, arrayOf(Constants.PLAYER_BASIC, Constants.PLAYER_INFO), intArrayOf(R.id.player_basic, R.id.player_info))
        recyclerAdapter.setOnItemClickListener(object : RecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                viewPlayer(view!!)
            }
        })
        recyclerAdapter.setOnItemLongClickListener(object: RecyclerViewAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View?, position: Int): Boolean {
                undraftPlayer(view!!)
                return true
            }
        })
        listview.layoutManager = LinearLayoutManager(this)
        listview.addItemDecoration(getVerticalDividerDecoration(this))
        listview.adapter = recyclerAdapter
    }

    private fun viewPlayer(view: View) {
        val key = getPlayerKeyFromListViewItem(view)
        val intent = Intent(this, PlayerInfo::class.java)
        intent.putExtra(Constants.PLAYER_ID, key)
        startActivity(intent)
    }

    private fun undraftPlayer(view: View) {
        val key = getPlayerKeyFromListViewItem(view)
        val player = rankings.getPlayer(key)
        rankings.draft.undraft(rankings, player, this)
        displayPlayers()
    }

    private fun getPlayerKeyFromListViewItem(view: View): String {
        val playerMain = view.findViewById<TextView>(R.id.player_basic)
        val playerInfo = view.findViewById<TextView>(R.id.player_info)
        val name = playerMain.text.toString().split(Constants.RANKINGS_LIST_DELIMITER).toTypedArray()[1]
        val teamPosBye = playerInfo.text.toString().split(Constants.LINE_BREAK).toTypedArray()[0]
        val teamPos = teamPosBye.split(" \\(".toRegex()).toTypedArray()[0]
        val team = teamPos.split(Constants.POS_TEAM_DELIMITER).toTypedArray()[1]
        val pos = teamPos.split(Constants.POS_TEAM_DELIMITER).toTypedArray()[0]
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos
    }

    private fun generateOutputSubtext(player: Player): String {
        var sub = StringBuilder(player.position)
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.teamName)
        val team = rankings.getTeam(player)
        if (team != null) {
            sub = sub.append(" (Bye: ")
                    .append(team.bye)
                    .append(")")
        }
        return sub.append(Constants.LINE_BREAK)
                .append("Projection: ")
                .append(Constants.DECIMAL_FORMAT.format(player.projection))
                .toString()
    }

    private fun clearAndAddView(viewId: Int): View {
        baseLayout!!.removeAllViews()
        val child = layoutInflater.inflate(viewId, null)
        baseLayout!!.addView(child)
        return child
    }

    private fun graphPAALeft() {
        val barChart = findViewById<BarChart>(R.id.paa_left_graph)
        val barData = BarData()
        conditionallyGraphPosition(barData, Constants.QB)
        conditionallyGraphPosition(barData, Constants.RB)
        conditionallyGraphPosition(barData, Constants.WR)
        conditionallyGraphPosition(barData, Constants.TE)
        conditionallyGraphPosition(barData, Constants.DST)
        conditionallyGraphPosition(barData, Constants.K)
        barChart.data = barData
        val left = barChart.axisLeft
        left.setDrawZeroLine(true)
        barChart.axisRight.isEnabled = false
        barChart.xAxis.isEnabled = false
        barChart.description = null
        barChart.invalidate()
        barChart.setTouchEnabled(true)
        barChart.setPinchZoom(true)
        barChart.isDragEnabled = true
        barChart.legend.setCustom(ArrayList())
        barChart.animateX(1500)
        barChart.animateY(1500)
    }

    private fun conditionallyGraphPosition(barData: BarData, position: String) {
        if (rankings!!.getLeagueSettings().rosterSettings.isPositionValid(position)) {
            val entries: MutableList<BarEntry> = ArrayList()
            val players = rankings.draft.getSortedAvailablePlayersForPosition(position, rankings)
            val threeBack = rankings.draft.getPAANAvailablePlayersBack(players, 3)
            val fiveBack = rankings.draft.getPAANAvailablePlayersBack(players, 5)
            val tenBack = rankings.draft.getPAANAvailablePlayersBack(players, 10)
            val stackedEntry = BarEntry(barData.dataSetCount.toFloat(),
                    floatArrayOf(threeBack.toFloat(), fiveBack.toFloat(), tenBack.toFloat()))
            stackedEntry.data = position
            entries.add(stackedEntry)
            val barDataSet = BarDataSet(entries, position)
            barDataSet.setColors(Color.BLUE, Color.GREEN, Color.CYAN)
            barDataSet.setDrawValues(false)
            barData.addDataSet(barDataSet)
        }
    }

    private fun graphTeam() {
        val draft = rankings.draft
        if (draft.myPlayers.isEmpty()) {
            return
        }
        val teamPAA = findViewById<BarChart>(R.id.team_graph)
        val barData = BarData()
        conditionallyAddData(draft.myQbs, barData.dataSetCount, draft.qBPAA, barData, "QBs", "green")
        conditionallyAddData(draft.myRbs, barData.dataSetCount, draft.rBPAA, barData, "RBs", "red")
        conditionallyAddData(draft.myWrs, barData.dataSetCount, draft.wRPAA, barData, "WRs", "blue")
        conditionallyAddData(draft.myTes, barData.dataSetCount, draft.tEPAA, barData, "TEs", "yellow")
        conditionallyAddData(draft.myDsts, barData.dataSetCount, draft.dstpaa, barData, "DSTs", "grey")
        conditionallyAddData(draft.myKs, barData.dataSetCount, draft.kpaa, barData, "Ks", "black")
        if (barData.dataSetCount > 1) {
            val entries: MutableList<BarEntry> = ArrayList()
            val entry = BarEntry(barData.dataSetCount.toFloat(), draft.totalPAA.toFloat())
            entries.add(entry)
            barData.addDataSet(getBarDataSet(entries, "All", "purple"))
        }
        teamPAA.data = barData
        val description = Description()
        description.text = ""
        teamPAA.description = description
        teamPAA.invalidate()
        teamPAA.setTouchEnabled(true)
        teamPAA.setPinchZoom(true)
        teamPAA.isDragEnabled = true
        teamPAA.animateX(1500)
        teamPAA.animateY(1500)
        val left = teamPAA.axisLeft
        left.setDrawAxisLine(false)
        left.setDrawGridLines(false)
        left.setDrawLabels(false)
        left.setDrawZeroLine(true)
        teamPAA.axisRight.isEnabled = false
        teamPAA.xAxis.isEnabled = false
        val legend = teamPAA.legend
        legend.textSize = 10f
        legend.setDrawInside(false)
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.VERTICAL
        teamPAA.visibility = View.VISIBLE
    }

    private fun conditionallyAddData(players: List<Player>, xIndex: Int, posPaa: Double,
                                     barData: BarData, label: String, color: String) {
        var xIndex = xIndex
        val entries: MutableList<BarEntry> = ArrayList()
        if (players.isNotEmpty()) {
            val entry = BarEntry(xIndex.toFloat(), posPaa.toFloat())
            entries.add(entry)
            barData.addDataSet(getBarDataSet(entries, label, color))
            xIndex++
        }
    }

    private fun getBarDataSet(entries: List<BarEntry>, label: String, color: String): BarDataSet {
        val dataSet = BarDataSet(entries, label)
        dataSet.color = Color.parseColor(color)
        dataSet.valueTextSize = 10f
        return dataSet
    }

    companion object {
        private const val TAG = "DraftInfo"
    }
}