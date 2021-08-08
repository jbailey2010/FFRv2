package com.devingotaswitch.rankings

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.util.StringUtils
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.appsync.AppSyncHelper.getUserSettings
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.MultiSelectionSpinner
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import com.devingotaswitch.rankings.extras.SwipeDismissTouchListener
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DisplayUtils
import com.devingotaswitch.utils.DisplayUtils.getVerticalDividerDecoration
import com.devingotaswitch.utils.DraftUtils.AuctionCostInterface
import com.devingotaswitch.utils.DraftUtils.getAuctionCostDialog
import com.devingotaswitch.utils.DraftUtils.getUndraftListener
import com.devingotaswitch.utils.FlashbarFactory.generateFlashbarWithUndo
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.utils.GeneralUtils.isInteger
import com.devingotaswitch.utils.GraphUtils.conditionallyAddData
import com.devingotaswitch.utils.GraphUtils.getLineDataSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import org.angmarch.views.NiceSpinner
import org.angmarch.views.OnSpinnerItemSelectedListener
import java.util.*

class PlayerSorter : AppCompatActivity() {
    private lateinit var rankings: Rankings
    private lateinit var graphItem: MenuItem
    private val players: MutableList<Player> = ArrayList()
    private var factor: String? = null
    private var expandedFactor: String? = null
    private var expandedFactorType: String? = null
    private var sortMax = 0
    private var posIndex = 0
    private var sortIndex = 0
    private var selectedIndex = 0
    private var maxVal = 0.0
    private var lastFactorIndex = 0
    private var factorStrings: Set<String> = HashSet(listOf(Constants.SORT_DEFAULT_STRING))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_sorter)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankings = Rankings.init()

        // Set toolbar for this screen
        val toolbar = findViewById<Toolbar>(R.id.player_sorter_toolbar)
        toolbar.title = ""
        val mainTitle = findViewById<TextView>(R.id.main_toolbar_title)
        mainTitle.text = "Sort Players"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val act: Activity = this
        toolbar.setNavigationOnClickListener {
            hideKeyboard(act)
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_sort_menu, menu)
        graphItem = menu.findItem(R.id.graph_sort)
        graphItem.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Find which menu item was selected
        val menuItem = item.itemId
        if (menuItem == R.id.graph_sort) {
            graphSort()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onResume() {
        super.onResume()
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        try {
            init()
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            hideKeyboard(this)
            onBackPressed()
        }
    }

    private fun setSpinnerAdapter(): MultiSelectionSpinner {
        val spinner = findViewById<MultiSelectionSpinner>(R.id.sort_players_additional_factors)
        val list: MutableList<String> = ArrayList()
        if (!rankings.userSettings.isHideDraftedSort) {
            list.add(Constants.SORT_HIDE_DRAFTED)
        }
        list.add(Constants.SORT_ONLY_HEALTHY)
        list.add(Constants.SORT_EASY_SOS)
        list.add(Constants.SORT_ONLY_WATCHED)
        list.add(Constants.SORT_ONLY_ROOKIES)
        list.add(Constants.SORT_UNDER_30)
        if (!rankings.getLeagueSettings().isRookie && !rankings.getLeagueSettings().isDynasty && !rankings.getLeagueSettings().isBestBall) {
            list.add(Constants.SORT_IGNORE_EARLY)
            list.add(Constants.SORT_IGNORE_LATE)
        }
        spinner.setItems(list, Constants.SORT_DEFAULT_STRING)
        spinner.setSelection(ArrayList(factorStrings))
        return spinner
    }

    private fun init() {
        getUserSettings(this)
        val positions = findViewById<NiceSpinner>(R.id.sort_players_position)
        val posList: MutableList<String> = ArrayList()
        val roster = rankings.getLeagueSettings().rosterSettings
        posList.add(Constants.ALL_POSITIONS)
        if (roster.qbCount > 0) {
            posList.add(Constants.QB)
        }
        if (roster.rbCount > 0) {
            posList.add(Constants.RB)
        }
        if (roster.wrCount > 0) {
            posList.add(Constants.WR)
        }
        if (roster.teCount > 0) {
            posList.add(Constants.TE)
        }
        if (roster.dstCount > 0) {
            posList.add(Constants.DST)
        }
        if (roster.kCount > 0) {
            posList.add(Constants.K)
        }
        if (roster.rbCount > 0 && roster.wrCount > 0 && roster.numStartingPositions > 2) {
            posList.add(Constants.RBWR)
        }
        if (roster.rbCount > 0 && roster.teCount > 0 && roster.numStartingPositions > 2) {
            posList.add(Constants.RBTE)
        }
        if (roster.rbCount > 0 && roster.wrCount > 0 && roster.teCount > 0 && roster.numStartingPositions > 3) {
            posList.add(Constants.RBWRTE)
        }
        if (roster.wrCount > 0 && roster.teCount > 0 && roster.numStartingPositions > 2) {
            posList.add(Constants.WRTE)
        }
        if (roster.qbCount > 0 && roster.rbCount > 0 && roster.wrCount > 0 && roster.teCount > 0 && roster.numStartingPositions > 4) {
            posList.add(Constants.QBRBWRTE)
        }
        positions.attachDataSource(posList)
        positions.setBackgroundColor(Color.parseColor("#FAFAFA"))
        val factors = findViewById<NiceSpinner>(R.id.sort_players_factor)
        val factorList: MutableList<String> = ArrayList()
        factorList.add(Constants.SORT_ALL)
        factorList.add(Constants.SORT_ECR)
        factorList.add(Constants.SORT_ADP)
        factorList.add(Constants.SORT_UNDERDRAFTED)
        factorList.add(Constants.SORT_OVERDRAFTED)
        factorList.add(Constants.SORT_PROJECTION_EXPANDED)
        factorList.add(Constants.SORT_VBD_EXPANDED)
        factorList.add(Constants.SORT_AUCTION)
        factorList.add(Constants.SORT_DYNASTY)
        factorList.add(Constants.SORT_ROOKIE)
        factorList.add(Constants.SORT_BEST_BALL)
        factorList.add(Constants.SORT_LAST_YEAR_POINTS)
        factorList.add(Constants.SORT_SOS)
        factors.attachDataSource(factorList)
        factors.setBackgroundColor(Color.parseColor("#FAFAFA"))
        val act: Activity = this
        factors.onSpinnerItemSelectedListener = OnSpinnerItemSelectedListener { _: NiceSpinner?, view: View, position: Int, _: Long ->
            val selection = (view as TextView).text.toString()
            if (Constants.SORT_VBD_EXPANDED == selection || Constants.SORT_PROJECTION_EXPANDED == selection) {
                val popup = ListPopupWindow(act)
                popup.anchorView = factors
                var data: List<Map<String?, String?>?>? = null
                if (Constants.SORT_VBD_EXPANDED == selection) {
                    data = vBDOptions
                } else if (Constants.SORT_PROJECTION_EXPANDED == selection) {
                    data = projectionOptions
                }
                val adapter = SimpleAdapter(act, data,
                        R.layout.nested_spinner_item, arrayOf(Constants.NESTED_SPINNER_DISPLAY), intArrayOf(R.id.text_view_spinner))
                popup.setAdapter(adapter)
                popup.setOnItemClickListener { _: AdapterView<*>?, view1: View, _: Int, _: Long ->
                    if (Constants.SORT_BACK == (view1 as TextView).text.toString()) {
                        popup.dismiss()
                        // Seems to be an annoying bug with the spinner library, can't re-select the same item.
                        // So this is done to work around it.
                        factors.attachDataSource(factorList)
                        factors.showDropDown()
                        if (StringUtils.isBlank(expandedFactor)) {
                            factors.selectedIndex = lastFactorIndex
                        } else {
                            factors.text = expandedFactorType
                        }
                    } else {
                        expandedFactor = view1.text.toString()
                        factors.selectedIndex = position
                        factors.text = factors.text.toString() + " " + expandedFactor
                        popup.dismiss()
                    }
                }
                popup.setOnDismissListener {
                    if (StringUtils.isBlank(expandedFactor)) {
                        factors.selectedIndex = lastFactorIndex
                    }
                }
                popup.show()
            }
        }
        val spinner = setSpinnerAdapter()
        positions.selectedIndex = posIndex
        factors.selectedIndex = sortIndex
        val reverse = findViewById<CheckBox>(R.id.sort_players_reverse)
        val numberShown = findViewById<EditText>(R.id.sort_players_number_shown)
        val submit = findViewById<Button>(R.id.sort_players_submit)
        val activity = this
        submit.setOnClickListener {
            val currentPosition = posList[positions.selectedIndex]
            var filteredIds: List<String> = ArrayList(rankings.orderedIds)
            if (Constants.ALL_POSITIONS != currentPosition) {
                filteredIds = rankings.getPlayersByPosition(filteredIds, currentPosition)
            }
            val selection = factors.text.toString()
            if (!selection.startsWith(Constants.SORT_VBD_EXPANDED) &&
                    !selection.startsWith(Constants.SORT_PROJECTION_EXPANDED) || StringUtils.isBlank(expandedFactor)) {
                factor = factorList[factors.selectedIndex]
                lastFactorIndex = factors.selectedIndex
                expandedFactor = null
                // Seems to be an annoying bug with the spinner library, can't re-select the same item.
                // So this is done to work around it.
                factors.attachDataSource(factorList)
                factors.selectedIndex = lastFactorIndex
            } else {
                factor = expandedFactor
                expandedFactorType = factors.text.toString()
                // Seems to be an annoying bug with the spinner library, can't re-select the same item.
                // So this is done to work around it.
                factors.attachDataSource(factorList)
                factors.text = expandedFactorType
            }
            posIndex = positions.selectedIndex
            sortIndex = factors.selectedIndex
            factorStrings = spinner.selectedStrings
            val numberShownStr = numberShown.text.toString()
            sortMax = 1000
            if (!StringUtils.isBlank(numberShownStr) && isInteger(numberShownStr)) {
                sortMax = numberShownStr.toInt()
            }
            hideKeyboard(activity)
            getComparatorForFactor(filteredIds, spinner.selectedStrings, reverse.isChecked)
            graphItem.isVisible = true
        }
    }

    private val vBDOptions: List<Map<String?, String?>?>
        get() {
            val factorList: MutableList<Map<String?, String?>?> = ArrayList()
            factorList.add(generateSpinnerMap(Constants.SORT_BACK))
            factorList.add(generateSpinnerMap(Constants.SORT_PAA))
            factorList.add(generateSpinnerMap(Constants.SORT_PAA_SCALED))
            factorList.add(generateSpinnerMap(Constants.SORT_PAAPD))
            factorList.add(generateSpinnerMap(Constants.SORT_XVAL))
            factorList.add(generateSpinnerMap(Constants.SORT_XVAL_SCALED))
            factorList.add(generateSpinnerMap(Constants.SORT_XVALPD))
            factorList.add(generateSpinnerMap(Constants.SORT_VOLS))
            factorList.add(generateSpinnerMap(Constants.SORT_VOLS_SCALED))
            factorList.add(generateSpinnerMap(Constants.SORT_VOLSPD))
            factorList.add(generateSpinnerMap(Constants.SORT_VBD_SUGGESTED))
            return factorList
        }
    private val projectionOptions: List<Map<String?, String?>?>
        get() {
            val factorList: MutableList<Map<String?, String?>?> = ArrayList()
            factorList.add(generateSpinnerMap(Constants.SORT_BACK))
            factorList.add(generateSpinnerMap(Constants.SORT_PROJECTION))
            factorList.add(generateSpinnerMap(Constants.SORT_PASSING_TDS))
            factorList.add(generateSpinnerMap(Constants.SORT_PASSING_YDS))
            factorList.add(generateSpinnerMap(Constants.SORT_RUSHING_TDS))
            factorList.add(generateSpinnerMap(Constants.SORT_RUSHING_YDS))
            factorList.add(generateSpinnerMap(Constants.SORT_RECEIVING_TDS))
            factorList.add(generateSpinnerMap(Constants.SORT_RECEIVING_YDS))
            factorList.add(generateSpinnerMap(Constants.SORT_RECEPTIONS))
            return factorList
        }

    private fun generateSpinnerMap(value: String): Map<String?, String?> {
        val datum: MutableMap<String?, String?> = HashMap()
        datum[Constants.NESTED_SPINNER_DISPLAY] = value
        return datum
    }

    private fun getComparatorForFactor(playerIds: List<String?>, booleanFactors: Set<String>, reversePlayers: Boolean) {
        var comparator: Comparator<Player>? = null
        when (factor) {
            Constants.SORT_ECR -> comparator = eCRComparator
            Constants.SORT_ADP -> comparator = aDPComparator
            Constants.SORT_UNDERDRAFTED -> comparator = underdraftedComparator
            Constants.SORT_OVERDRAFTED -> comparator = overdraftedComparator
            Constants.SORT_AUCTION -> comparator = auctionComparator
            Constants.SORT_DYNASTY -> comparator = dynastyComparator
            Constants.SORT_ROOKIE -> comparator = rookieComparator
            Constants.SORT_BEST_BALL -> comparator = bestBallComparator
            Constants.SORT_PROJECTION -> comparator = projectionComparator
            Constants.SORT_PASSING_TDS -> comparator = passingTDsComparator
            Constants.SORT_PASSING_YDS -> comparator = passingYardsComparator
            Constants.SORT_RUSHING_TDS -> comparator = rushingTdsComparator
            Constants.SORT_RUSHING_YDS -> comparator = rushingYardsComparator
            Constants.SORT_RECEIVING_TDS -> comparator = receivingTdsComparator
            Constants.SORT_RECEIVING_YDS -> comparator = receivingYardsComparator
            Constants.SORT_RECEPTIONS -> comparator = receptionsComparator
            Constants.SORT_PAA -> comparator = pAAComparator
            Constants.SORT_PAA_SCALED -> comparator = pAAScaledComparator
            Constants.SORT_PAAPD -> comparator = pAAPDComparator
            Constants.SORT_XVAL -> comparator = xValComparator
            Constants.SORT_XVAL_SCALED -> comparator = xValScaledComparator
            Constants.SORT_XVALPD -> comparator = xvalPDComparator
            Constants.SORT_VOLS -> comparator = voLSComparator
            Constants.SORT_VOLS_SCALED -> comparator = voLSScaledComparator
            Constants.SORT_VOLSPD -> comparator = voLSPDComparator
            Constants.SORT_VBD_SUGGESTED -> comparator = vBDSuggestedComparator
            Constants.SORT_LAST_YEAR_POINTS -> comparator = lastYearPointsComparator
            Constants.SORT_SOS -> comparator = sOSComparator
        }
        filterAndConditionallySortPlayers(playerIds, booleanFactors, reversePlayers, comparator)
    }

    private fun filterAndConditionallySortPlayers(playerIds: List<String?>, booleanFactors: Set<String>, reversePlayers: Boolean,
                                                  comparator: Comparator<Player>?) {
        players.clear()
        for (id in playerIds) {
            val player = rankings.getPlayer(id!!)
            if ((Constants.SORT_ALL == factor && rankings.getLeagueSettings().isRookie || Constants.SORT_ROOKIE == factor)
                    && player.rookieRank == Constants.DEFAULT_RANK) {
                // Default sort for rookies means only rookies. If it's 'not set',  skip.
                // Also skip if we're looking at rookie rank for someone without one (meaning, not a rookie).
                continue
            }
            if (Constants.SORT_SOS == factor && rankings.getTeam(player) == null) {
                // If the player's team isn't a valid team, skip over for sos
                continue
            }
            if ((Constants.SORT_UNDERDRAFTED == factor || Constants.SORT_OVERDRAFTED == factor) &&
                    (player.ecr == Constants.DEFAULT_RANK || player.adp == Constants.DEFAULT_RANK)) {
                // Don't compare adp to ecr if either is not saved
                continue
            }
            if ((booleanFactors.contains(Constants.SORT_HIDE_DRAFTED) || rankings.userSettings.isHideDraftedSort || Constants.SORT_VBD_SUGGESTED == factor) && rankings.draft.isDrafted(player)) {
                continue
            }
            if (rankings.userSettings.isHideRanklessSort && Constants.DEFAULT_DISPLAY_RANK_NOT_SET == player.getDisplayValue(rankings)) {
                continue
            }
            if (booleanFactors.contains(Constants.SORT_EASY_SOS)) {
                val team = rankings.getTeam(player)
                if (team == null || team.getSosForPosition(player.position) > Constants.SORT_EASY_SOS_THRESHOLD) {
                    continue
                }
            }
            if (booleanFactors.contains(Constants.SORT_ONLY_HEALTHY)) {
                if (!StringUtils.isBlank(player.injuryStatus)) {
                    continue
                }
            }
            if (booleanFactors.contains(Constants.SORT_ONLY_WATCHED)) {
                if (!rankings.isPlayerWatched(player.uniqueId)) {
                    continue
                }
            }
            if (booleanFactors.contains(Constants.SORT_ONLY_ROOKIES)) {
                if (player.rookieRank == Constants.DEFAULT_RANK) {
                    continue
                }
            }
            if (booleanFactors.contains(Constants.SORT_UNDER_30)) {
                if (player.age == 0 || player.age == null || player.age!! >= Constants.SORT_YOUNG_THRESHOLD) {
                    continue
                }
            }
            val teamCount = rankings.getLeagueSettings().teamCount
            if (booleanFactors.contains(Constants.SORT_IGNORE_LATE)) {
                if (player.ecr > teamCount * Constants.SORT_IGNORE_LATE_THRESHOLD_ROUNDS) {
                    continue
                }
            }
            if (booleanFactors.contains(Constants.SORT_IGNORE_EARLY)) {
                if (player.ecr < teamCount * Constants.SORT_IGNORE_EARLY_THRESHOLD_ROUNDS) {
                    continue
                }
            }
            players.add(player)
        }
        if (comparator != null) {
            // If it's null, it was default, which means the already ordered list
            players.sortWith(comparator)
        }
        if (reversePlayers) {
            players.reverse()
        }
        displayResults(players)
    }

    private fun displayResults(players: List<Player?>) {
        val listview = findViewById<RecyclerView>(R.id.sort_players_output)
        listview.adapter = null
        val data: MutableList<MutableMap<String, String?>> = ArrayList()
        val adapter = RecyclerViewAdapter(this, data,
                R.layout.list_item_layout, arrayOf(Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO), intArrayOf(R.id.player_basic, R.id.player_info,
                R.id.player_status, R.id.player_more_info))
        listview.adapter = adapter
        maxVal = 0.0
        if (Constants.SORT_VBD_SUGGESTED == factor) {
            for (key in rankings.players.keys) {
                val player = rankings.getPlayer(key)
                if (!rankings.draft.isDrafted(player)) {
                    val currVal = getVBDSuggestedValue(rankings.getPlayer(key))
                    if (currVal > maxVal) {
                        maxVal = currVal
                    }
                }
            }
        }
        var displayedCount = 0
        for (player in players) {
            if (rankings.getLeagueSettings().rosterSettings.isPositionValid(player!!.position)) {
                if (displayedCount >= sortMax) {
                    break
                }
                val datum: MutableMap<String, String?> = HashMap(3)
                datum[Constants.PLAYER_BASIC] = getMainTextForFactor(player)
                datum[Constants.PLAYER_INFO] = getSubTextForFactor(player)
                if (rankings.isPlayerWatched(player.uniqueId)) {
                    datum[Constants.PLAYER_STATUS] = R.drawable.star.toString()
                }
                if (rankings.draft.isDrafted(player)) {
                    datum[Constants.PLAYER_ADDITIONAL_INFO] = "Drafted"
                }
                data.add(datum)
                displayedCount++
            }
        }
        adapter.notifyDataSetChanged()
        val act: Activity = this
        val hideDrafted = factorStrings.contains(Constants.SORT_HIDE_DRAFTED)

        val localCopy: Activity = this
        val swipeListener = SwipeDismissTouchListener(listview, hideDrafted,
                object : SwipeDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(view: View?): Boolean {
                        return !(view!!.findViewById<View>(R.id.player_more_info) as TextView).text.toString().contains(Constants.DISPLAY_DRAFTED)
                    }

                    override fun onDismiss(listView: RecyclerView?, reverseSortedPositions: IntArray?, rightDismiss: Boolean) {
                        for (position in reverseSortedPositions!!) {
                            val datum = data[position]
                            val playerKey = getPlayerKeyFromPieces(datum[Constants.PLAYER_BASIC], datum[Constants.PLAYER_INFO])
                            val player = rankings.getPlayer(playerKey)
                            val listener = getUndraftListener(localCopy, rankings, player, adapter,
                                    data, datum, position, hideDrafted)
                            if (!rightDismiss) {
                                rankings.draft.draftBySomeone(rankings, player, localCopy, listener)
                            } else {
                                if (rankings.getLeagueSettings().isAuction) {
                                    getAuctionCost(listView, player, position, data, datum, adapter, listener)
                                } else {
                                    draftByMe(player, 0, listener)
                                }
                            }
                            if (!hideDrafted) {
                                datum[Constants.PLAYER_ADDITIONAL_INFO] = Constants.DISPLAY_DRAFTED
                            } else {
                                data.removeAt(position)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                },
                object: RecyclerViewAdapter.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val playerKey = getPlayerKeyFromListViewItem(view!!)
                        selectedIndex = position
                        displayPlayerInfo(playerKey)
                    }
                },
                object: RecyclerViewAdapter.OnItemLongClickListener {
                    override fun onItemLongClick(view: View?, position: Int): Boolean {
                        val playerKey = DisplayUtils.getPlayerKeyFromListViewItem(view!!)
                        val playerStatus = view.findViewById<ImageView>(R.id.player_status)
                        val player = rankings.getPlayer(playerKey)
                        if (rankings.isPlayerWatched(player.uniqueId)) {
                            val listener = object : OnActionTapListener {
                                override fun onActionTapped(bar: Flashbar) {
                                    bar.dismiss()
                                    playerStatus.setImageResource(R.drawable.star)
                                    rankings.togglePlayerWatched(act, player.uniqueId)
                                }
                            }
                            generateFlashbarWithUndo(act, "Success!", player.name + " removed from watch list", Flashbar.Gravity.BOTTOM, listener)
                                    .show()
                            rankings.togglePlayerWatched(act, player.uniqueId)
                            playerStatus.setImageResource(0)
                        } else {
                            val listener = object : OnActionTapListener {
                                override fun onActionTapped(bar: Flashbar) {
                                    bar.dismiss()
                                    playerStatus.setImageResource(0)
                                    rankings.togglePlayerWatched(act, player.uniqueId)
                                }
                            }
                            generateFlashbarWithUndo(act, "Success!", player.name + " added to watch list", Flashbar.Gravity.BOTTOM, listener)
                                    .show()
                            rankings.togglePlayerWatched(act, player.uniqueId)
                            playerStatus.setImageResource(R.drawable.star)
                        }
                        return true
                    }
                }
            )
        adapter.setOnTouchListener(swipeListener)
        listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(view: RecyclerView, scrollState: Int) {
                swipeListener.setEnabled(scrollState == RecyclerView.SCROLL_STATE_IDLE)
            }

            override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                if (data.size > 0) {
                    // A flag is used to green light this set, otherwise onScroll is set to 0 on initial display
                    val layoutManager = view.layoutManager as LinearLayoutManager?
                    selectedIndex = layoutManager!!.findFirstCompletelyVisibleItemPosition()
                }
            }
        })
        listview.layoutManager!!.scrollToPosition(selectedIndex)
        listview.addItemDecoration(getVerticalDividerDecoration(this))
        val titleView = findViewById<TextView>(R.id.main_toolbar_title)
        titleView.setOnClickListener { listview.smoothScrollToPosition(0) }
        titleView.setOnLongClickListener {
            listview.scrollToPosition(0)
            true
        }
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun getAuctionCost(listView: RecyclerView?, player: Player, position: Int, data: MutableList<MutableMap<String, String?>>,
                               datum: MutableMap<String, String?>, adapter: RecyclerViewAdapter, listener: OnActionTapListener) {
        val act: Activity = this
        val callback: AuctionCostInterface = object : AuctionCostInterface {
            override fun onValidInput(cost: Int?) {
                hideKeyboard(act)
                draftByMe(player, cost!!, listener)
            }

            override fun onInvalidInput() {
                generateTextOnlyFlashbar(act, "No can do", "Must provide a number for cost", Flashbar.Gravity.TOP)
                        .show()
                data.add(position, datum)
                adapter.notifyDataSetChanged()
                hideKeyboard(act)
            }

            override fun onCancel() {
                data.add(position, datum)
                adapter.notifyDataSetChanged()
            }
        }
        val alertDialog = getAuctionCostDialog(this, player, callback)
        alertDialog.show()
    }

    private fun draftByMe(player: Player, cost: Int, listener: OnActionTapListener) {
        rankings.draft.draftByMe(rankings, player, this, cost, listener)
    }

    private fun displayPlayerInfo(playerKey: String) {
        val intent = Intent(this, PlayerInfo::class.java)
        intent.putExtra(Constants.PLAYER_ID, playerKey)
        startActivity(intent)
    }

    private val eCRComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> a.ecr.compareTo(b.ecr) }
    private val passingTDsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.passingProjection.tds.compareTo(a.playerProjection.passingProjection.tds) }
    private val passingYardsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.passingProjection.yards.compareTo(a.playerProjection.passingProjection.yards) }
    private val rushingYardsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.rushingProjection.yards.compareTo(a.playerProjection.rushingProjection.yards) }
    private val rushingTdsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.rushingProjection.tds.compareTo(a.playerProjection.rushingProjection.tds) }
    private val receivingYardsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.receivingProjection.yards.compareTo(a.playerProjection.receivingProjection.yards) }
    private val receivingTdsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.receivingProjection.tds.compareTo(a.playerProjection.receivingProjection.tds) }
    private val receptionsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.playerProjection.receivingProjection.receptions.compareTo(a.playerProjection.receivingProjection.receptions) }
    private val dynastyComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> a.dynastyRank.compareTo(b.dynastyRank) }
    private val rookieComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> a.rookieRank.compareTo(b.rookieRank) }
    private val bestBallComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> a.bestBallRank.compareTo(b.bestBallRank) }
    private val aDPComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> a.adp.compareTo(b.adp) }
    private val underdraftedComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player ->
            val diffA = a.ecr - a.adp
            val diffB = b.ecr - b.adp
            diffA.compareTo(diffB)
        }
    private val overdraftedComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player ->
            val diffA = a.ecr - a.adp
            val diffB = b.ecr - b.adp
            diffB.compareTo(diffA)
        }
    private val auctionComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.auctionValue.compareTo(a.auctionValue) }
    private val projectionComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.projection.compareTo(a.projection) }
    private val lastYearPointsComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.lastYearPoints.compareTo(a.lastYearPoints) }
    private val pAAComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.paa.compareTo(a.paa) }
    private val pAAScaledComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.getScaledPAA(rankings).compareTo(a.getScaledPAA(rankings)) }
    private val pAAPDComparator: Comparator<Player>
        get() = Comparator { a: Player?, b: Player? ->
            val paapdA = getPAAPD(a)
            val paapdB = getPAAPD(b)
            paapdB.compareTo(paapdA)
        }

    private fun getPAAPD(a: Player?): Double {
        var paapdA = a!!.paa / a.getAuctionValueCustom(rankings)
        if (a.auctionValue == 0.0) {
            paapdA = 0.0
        }
        return paapdA
    }

    private fun getXvalPD(a: Player?): Double {
        var xvalpdA = a!!.xval / a.getAuctionValueCustom(rankings)
        if (a.auctionValue == 0.0) {
            xvalpdA = 0.0
        }
        return xvalpdA
    }

    private fun getVoLSPD(a: Player?): Double {
        var volspdA = a!!.vols / a.getAuctionValueCustom(rankings)
        if (a.auctionValue == 0.0) {
            volspdA = 0.0
        }
        return volspdA
    }

    private fun getVBDSuggestedValue(a: Player?): Double {
        return a!!.getScaledPAA(rankings) + a.getScaledXVal(rankings) + a.getScaledVOLS(rankings)
    }

    private val xValComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.xval.compareTo(a.xval) }
    private val voLSComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.vols.compareTo(a.vols) }
    private val xvalPDComparator: Comparator<Player>
        get() = Comparator { a: Player?, b: Player? ->
            val xvalpdA = getXvalPD(a)
            val xvalpdB = getXvalPD(b)
            xvalpdB.compareTo(xvalpdA)
        }
    private val voLSPDComparator: Comparator<Player>
        get() = Comparator { a: Player?, b: Player? ->
            val volspdA = getVoLSPD(a)
            val volspdB = getVoLSPD(b)
            volspdB.compareTo(volspdA)
        }
    private val vBDSuggestedComparator: Comparator<Player>
        get() = Comparator { a: Player?, b: Player? ->
            val vbdValA = getVBDSuggestedValue(a)
            val vbdValB = getVBDSuggestedValue(b)
            vbdValB.compareTo(vbdValA)
        }
    private val xValScaledComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.getScaledXVal(rankings).compareTo(a.getScaledXVal(rankings)) }
    private val voLSScaledComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player -> b.getScaledVOLS(rankings).compareTo(a.getScaledVOLS(rankings)) }
    private val sOSComparator: Comparator<Player>
        get() = Comparator { a: Player, b: Player ->
            val sosA = getSOS(a)
            val sosB = getSOS(b)
            sosA.compareTo(sosB) * -1
        }

    private fun getSOS(player: Player): Double {
        val team = rankings.getTeam(player) ?: return 1.0
        return team.getSosForPosition(player.position)
    }

    private fun getMainTextForFactor(player: Player): String {
        val prefix = getMainTextPrefixForPlayer(player)
        return prefix +
                Constants.RANKINGS_LIST_DELIMITER +
                player.name
    }

    private fun getMainTextPrefixForPlayer(player: Player): String {
        when (factor) {
            Constants.SORT_ALL -> return player.getDisplayValue(rankings)
            Constants.SORT_ECR -> return if (player.ecr == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.ecr.toInt().toString()
            Constants.SORT_ADP -> return if (player.adp == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.adp.toString()
            Constants.SORT_UNDERDRAFTED, Constants.SORT_OVERDRAFTED -> return Constants.DECIMAL_FORMAT.format(player.ecr - player.adp)
            Constants.SORT_AUCTION -> return Constants.DECIMAL_FORMAT.format(player.getAuctionValueCustom(rankings))
            Constants.SORT_DYNASTY -> return if (player.dynastyRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.dynastyRank.toString()
            Constants.SORT_ROOKIE -> return if (player.rookieRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.rookieRank.toString()
            Constants.SORT_BEST_BALL -> return if (player.bestBallRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.bestBallRank.toString()
            Constants.SORT_PROJECTION -> return Constants.DECIMAL_FORMAT.format(player.projection)
            Constants.SORT_LAST_YEAR_POINTS -> return Constants.DECIMAL_FORMAT.format(player.lastYearPoints)
            Constants.SORT_PASSING_TDS -> return player.playerProjection.passingProjection.tds.toString()
            Constants.SORT_PASSING_YDS -> return player.playerProjection.passingProjection.yards.toString()
            Constants.SORT_RUSHING_TDS -> return player.playerProjection.rushingProjection.tds.toString()
            Constants.SORT_RUSHING_YDS -> return player.playerProjection.rushingProjection.yards.toString()
            Constants.SORT_RECEIVING_TDS -> return player.playerProjection.receivingProjection.tds.toString()
            Constants.SORT_RECEIVING_YDS -> return player.playerProjection.receivingProjection.yards.toString()
            Constants.SORT_RECEPTIONS -> return player.playerProjection.receivingProjection.receptions.toString()
            Constants.SORT_PAA -> return Constants.DECIMAL_FORMAT.format(player.paa)
            Constants.SORT_PAA_SCALED -> return Constants.DECIMAL_FORMAT.format(player.getScaledPAA(rankings))
            Constants.SORT_PAAPD -> return Constants.DECIMAL_FORMAT.format(getPAAPD(player))
            Constants.SORT_XVAL -> return Constants.DECIMAL_FORMAT.format(player.xval)
            Constants.SORT_XVAL_SCALED -> return Constants.DECIMAL_FORMAT.format(player.getScaledXVal(rankings))
            Constants.SORT_XVALPD -> return Constants.DECIMAL_FORMAT.format(getXvalPD(player))
            Constants.SORT_VOLS -> return Constants.DECIMAL_FORMAT.format(player.vols)
            Constants.SORT_VOLS_SCALED -> return Constants.DECIMAL_FORMAT.format(player.getScaledVOLS(rankings))
            Constants.SORT_VOLSPD -> return Constants.DECIMAL_FORMAT.format(getVoLSPD(player))
            Constants.SORT_VBD_SUGGESTED -> return Constants.DECIMAL_FORMAT.format(getVBDSuggestedValue(player) / maxVal * 100.0)
            Constants.SORT_SOS -> return getSOS(player).toString()
        }
        return ""
    }

    private fun getSubTextForFactor(player: Player?): String {
        val subtextBuilder = StringBuilder(generateOutputSubtext(player))
        if (Constants.SORT_PROJECTION != factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Projection: ")
                    .append(player!!.projection)
        }
        if (Constants.SORT_UNDERDRAFTED == factor || Constants.SORT_OVERDRAFTED == factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("ECR: ")
                    .append(player!!.ecr.toInt())
                    .append(Constants.LINE_BREAK)
                    .append("ADP: ")
                    .append(player.adp)
        } else if (Constants.SORT_VBD_SUGGESTED == factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("PAA: ")
                    .append(Constants.DECIMAL_FORMAT.format(player!!.paa))
                    .append(Constants.LINE_BREAK)
                    .append("XVal: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.xval))
                    .append(Constants.LINE_BREAK)
                    .append("VoLS: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.vols))
        }
        val isAuction = rankings.getLeagueSettings().isAuction
        if (isAuction && Constants.SORT_AUCTION != factor && Constants.SORT_ALL != factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Auction Value: ")
                    .append(Constants.DECIMAL_FORMAT.format(player!!.getAuctionValueCustom(rankings)))
        } else if (rankings.getLeagueSettings().isSnake && Constants.SORT_ECR != factor && Constants.SORT_ALL != factor &&
                Constants.SORT_UNDERDRAFTED != factor && Constants.SORT_OVERDRAFTED != factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("ECR: ")
                    .append(if (player!!.ecr == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.ecr.toInt())
        } else if (rankings.getLeagueSettings().isDynasty && Constants.SORT_DYNASTY != factor && Constants.SORT_ALL != factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Dynasty/Keeper Rank: ")
                    .append(if (player!!.dynastyRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.dynastyRank)
            if (player.age != null) {
                subtextBuilder.append(Constants.LINE_BREAK)
                        .append("Age: ")
                        .append(player.age)
            }
        } else if (rankings.getLeagueSettings().isRookie && Constants.SORT_ROOKIE != factor && Constants.SORT_ALL != factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Rookie Rank: ")
                    .append(if (player!!.rookieRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.rookieRank)
        } else if (rankings.getLeagueSettings().isBestBall && Constants.SORT_BEST_BALL != factor && Constants.SORT_ALL != factor) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Best Ball Rank: ")
                    .append(if (player!!.bestBallRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.bestBallRank)
        }
        if (rankings.userSettings.isShowNoteSort &&
                !StringUtils.isBlank(rankings.getPlayerNote(player!!.uniqueId))) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append(rankings.getPlayerNote(player.uniqueId))
        }
        return subtextBuilder.toString()
    }

    private fun generateOutputSubtext(player: Player?): String {
        var sub = StringBuilder(player!!.position)
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.teamName)
        val team = rankings.getTeam(player)
        if (team != null) {
            sub = sub.append(" (Bye: ")
                    .append(team.bye)
                    .append(")")
        }
        return sub.toString()
    }

    private fun getPlayerKeyFromListViewItem(view: View): String {
        val playerMain = view.findViewById<TextView>(R.id.player_basic)
        val playerInfo = view.findViewById<TextView>(R.id.player_info)
        return getPlayerKeyFromPieces(playerMain.text.toString(), playerInfo.text.toString().split(Constants.LINE_BREAK)[0])
    }

    private fun getPlayerKeyFromPieces(playerMain: String?, teamPosBye: String?): String {
        val name = playerMain!!.split(Constants.RANKINGS_LIST_DELIMITER)[1]
        val teamPos = teamPosBye!!.split(" \\(".toRegex())[0].split(Constants.POS_TEAM_DELIMITER)
        val team = teamPos[1]
        val pos = teamPos[0]
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos
    }

    private fun graphSort() {
        val li = LayoutInflater.from(this)
        val graphView = li.inflate(R.layout.sort_graph_popup, null)
        val alertDialogBuilder = AlertDialog.Builder(
                this)
        alertDialogBuilder.setView(graphView)
        val lineGraph: LineChart = graphView.findViewById(R.id.sort_graph)
        val entries: MutableList<Entry?> = ArrayList()
        val qbs: MutableList<Entry?> = ArrayList()
        val rbs: MutableList<Entry?> = ArrayList()
        val wrs: MutableList<Entry?> = ArrayList()
        val tes: MutableList<Entry?> = ArrayList()
        val dsts: MutableList<Entry?> = ArrayList()
        val ks: MutableList<Entry?> = ArrayList()
        var actualIndex = 0
        for (i in 0 until sortMax.coerceAtMost(players.size)) {
            val player = players[i]
            val prefix = getMainTextPrefixForPlayer(player)
            if (Constants.DEFAULT_DISPLAY_RANK_NOT_SET == prefix) {
                continue
            }
            val strVal = getMainTextPrefixForPlayer(player)
            if (strVal != "-∞" && strVal != "∞") {
                val value = strVal.toDouble()
                entries.add(Entry(actualIndex++.toFloat(), value.toFloat()))
                when (player.position) {
                    Constants.QB -> qbs.add(Entry(qbs.size.toFloat(), value.toFloat()))
                    Constants.RB -> rbs.add(Entry(rbs.size.toFloat(), value.toFloat()))
                    Constants.WR -> wrs.add(Entry(wrs.size.toFloat(), value.toFloat()))
                    Constants.TE -> tes.add(Entry(tes.size.toFloat(), value.toFloat()))
                    Constants.DST -> dsts.add(Entry(dsts.size.toFloat(), value.toFloat()))
                    Constants.K -> ks.add(Entry(ks.size.toFloat(), value.toFloat()))
                }
            }
        }
        val allPositions = getLineDataSet(entries, "All Positions", "blue")
        val lineData = LineData()
        conditionallyAddData(lineData, qbs, "QBs", "green")
        conditionallyAddData(lineData, rbs, "RBs", "red")
        conditionallyAddData(lineData, wrs, "WRs", "purple")
        conditionallyAddData(lineData, tes, "TEs", "yellow")
        conditionallyAddData(lineData, dsts, "DSTs", "black")
        conditionallyAddData(lineData, ks, "Ks", "grey")
        if (lineData.dataSetCount > 1) {
            lineData.addDataSet(allPositions)
        }
        lineGraph.data = lineData
        val description = Description()
        description.text = factor
        description.textSize = 12f
        lineGraph.description = description
        lineGraph.invalidate()
        lineGraph.setTouchEnabled(true)
        lineGraph.setPinchZoom(true)
        lineGraph.isDragEnabled = true
        lineGraph.animateX(1500)
        lineGraph.animateY(1500)
        val x = lineGraph.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawAxisLine(true)
        val yR = lineGraph.axisRight
        yR.setDrawAxisLine(true)
        yR.setDrawLabels(false)
        alertDialogBuilder
                .setNegativeButton("Dismiss"
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    companion object {
        private const val TAG = "PlayerSorter"
    }
}