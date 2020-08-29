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
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.util.StringUtils
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.appsync.AppSyncHelper.getUserCustomPlayerData
import com.devingotaswitch.appsync.AppSyncHelper.getUserSettings
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.fileio.LocalSettingsHelper
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.domain.LeagueSettings
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.UserSettings
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter
import com.devingotaswitch.rankings.extras.RankingsListView
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import com.devingotaswitch.rankings.extras.SwipeDismissTouchListener
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DisplayUtils.getDatumForPlayer
import com.devingotaswitch.utils.DisplayUtils.getDisplayAdapter
import com.devingotaswitch.utils.DisplayUtils.getPlayerKeyFromListViewItem
import com.devingotaswitch.utils.DisplayUtils.getPositionalRank
import com.devingotaswitch.utils.DisplayUtils.getVerticalDividerDecoration
import com.devingotaswitch.utils.DraftUtils.AuctionCostInterface
import com.devingotaswitch.utils.DraftUtils.getAuctionCostDialog
import com.devingotaswitch.utils.DraftUtils.getUndraftListener
import com.devingotaswitch.utils.FlashbarFactory.generateFlashbarWithUndo
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.confirmInternet
import com.devingotaswitch.utils.GeneralUtils.getPlayerIdFromSearchView
import com.devingotaswitch.utils.GeneralUtils.getPlayerSearchAdapter
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.utils.GeneralUtils.isInteger
import com.devingotaswitch.youruserpools.CUPHelper.currUser
import com.devingotaswitch.youruserpools.CUPHelper.pool
import com.devingotaswitch.youruserpools.ChangePasswordActivity
import com.devingotaswitch.youruserpools.MainActivity
import com.google.android.material.navigation.NavigationView
import org.angmarch.views.NiceSpinner
import java.util.*

class RankingsHome : AppCompatActivity() {
    private val tag = "RankingsActivity"

    // Cognito user objects
    private var user: CognitoUser? = null
    private var username: String? = null
    private var filterItem: MenuItem? = null
    private lateinit var nDrawer: NavigationView
    private lateinit var mDrawer: DrawerLayout
    private lateinit var rankingsDB: RankingsDBWrapper
    private var currentLeague: LeagueSettings? = null
    private var rankings: Rankings? = null
    private lateinit var rankingsBase: LinearLayout
    private lateinit var searchBase: RelativeLayout
    private lateinit var buttonBase: LinearLayout
    private var maxPlayers = 0
    private var loadRanks = false
    private var ranksDisplayed = false
    private var settingsNeedRefresh = true
    private var filterToggleConfigured = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rankings_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Relying on onResume to display stuff.
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_rankings_menu, menu)
        filterItem = menu.findItem(R.id.filter_rankings)
        if (rankings != null) {
            // There's a possible case where the rankings load after the menu is set up, and it
            // clears ranks. To fight that, we don't hit that code until rankings are fetched.
            setFilterItemVisibility()
            filterToggleConfigured = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Find which menu item was selected
        val menuItem = item.itemId
        if (menuItem == R.id.filter_rankings) {
            toggleFilterView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        exit()
    }

    public override fun onResume() {
        super.onResume()
        hideKeyboard(this)
        try {
            initApp()
            init()
        } catch (e: Exception) {
            Log.d(tag, "Error initializing app", e)
            try {
                initApp()
                init()
            } catch (e2: Exception) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setFilterItemVisibility() {
        filterItem!!.isVisible = rankings!!.leagueSettings != null && rankings!!.players.isNotEmpty()
    }

    private fun initApp() {
        // Set toolbar for this screen
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        toolbar.title = ""
        toolbar.elevation = 0f
        setSupportActionBar(toolbar)

        // Set navigation drawer for this screen
        mDrawer = findViewById(R.id.user_drawer_layout)
        val act: Activity = this
        val mDrawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close) {
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                hideKeyboard(act)
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                hideKeyboard(act)
            }
        }
        mDrawer.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
        nDrawer = findViewById(R.id.nav_view)
        setNavDrawer()
        rankingsDB = RankingsDBWrapper()
        val currentLeagueId = LocalSettingsHelper.getCurrentLeagueName(this)
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currentLeague = rankingsDB!!.getLeague(this, currentLeagueId)
        }
        loadRanks = intent.extras != null && intent.extras!!.getBoolean(Constants.RANKINGS_UPDATED)
        if (!StringUtils.isBlank(currUser)) {
            val navigationHeader = nDrawer.getHeaderView(0)
            val navHeaderSubTitle = navigationHeader.findViewById<TextView>(R.id.textViewNavUserSub)
            navHeaderSubTitle.text = currUser
        }
    }

    fun setUserSettings(userSettings: UserSettings) {
        Rankings.setUserSettings(userSettings)
        settingsNeedRefresh = false
        setSearchAutocomplete()
        (findViewById<View>(R.id.rankings_list) as RankingsListView)
                .setRefreshRanksOnOverscroll(userSettings.isRefreshOnOverscroll)
    }

    private fun toggleFilterView() {
        val filterBase = findViewById<LinearLayout>(R.id.rankings_filter_base)
        if (filterBase.visibility == View.GONE) {
            filterBase.visibility = View.VISIBLE
        } else {
            filterBase.visibility = View.GONE
        }
        // In place to quickly revert filterings on closing and
        // prevent a weird issue where the watched star would
        // be out of date on visibility change.
        displayRankings(rankings!!.orderedIds)
        val teams: NiceSpinner = filterBase.findViewById(R.id.rankings_filter_teams)
        val teamList: MutableList<String> = ArrayList(rankings!!.teams.keys)
        teamList.sort()
        teamList.add(0, Constants.ALL_TEAMS)
        teams.attachDataSource(teamList)
        teams.setBackgroundColor(Color.parseColor("#FAFAFA"))
        val positions: NiceSpinner = filterBase.findViewById(R.id.rankings_filter_positions)
        val posList: MutableList<String> = ArrayList()
        val roster = rankings!!.leagueSettings.rosterSettings
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
        val watched = filterBase.findViewById<CheckBox>(R.id.rankings_filter_watched)
        val maxPlayersField = filterBase.findViewById<EditText>(R.id.max_players_visible)
        maxPlayersField.setText(maxPlayers.toString())
        val submit = filterBase.findViewById<Button>(R.id.rankings_filter_submit)
        val act: Activity = this
        submit.setOnClickListener {
            val currentTeam = teamList[teams.selectedIndex]
            val currentPosition = posList[positions.selectedIndex]
            val isWatched = watched.isChecked
            var filteredIds = rankings!!.orderedIds
            if (Constants.ALL_POSITIONS != currentPosition) {
                filteredIds = rankings!!.getPlayersByPosition(filteredIds, currentPosition)
            }
            if (Constants.ALL_TEAMS != currentTeam) {
                filteredIds = rankings!!.getPlayersByTeam(filteredIds, currentTeam)
            }
            if (isWatched) {
                filteredIds = rankings!!.getWatchedPlayers(filteredIds)
                if (!rankings!!.userSettings.isSortWatchListByTime) {
                    val filteredPlayers: MutableList<Player> = ArrayList()
                    for (id in filteredIds) {
                        filteredPlayers.add(rankings!!.getPlayer(id))
                    }
                    filteredIds = rankings!!.orderPlayersByLeagueType(filteredPlayers)
                }
            }
            val maxPlayersInput = maxPlayersField.text.toString()
            if (isInteger(maxPlayersInput)) {
                maxPlayers = maxPlayersInput.toInt()
                LocalSettingsHelper.saveNumVisiblePlayers(application, maxPlayers)
            }
            hideKeyboard(act)
            displayRankings(filteredIds)
        }
    }

    private fun init() {
        // Rankings stuff
        searchBase = findViewById(R.id.rankings_search_base)
        buttonBase = findViewById(R.id.rankings_button_bar)
        maxPlayers = LocalSettingsHelper.getNumVisiblePlayers(this)
        val adpSimulator = buttonBase.findViewById<ImageButton>(R.id.rankings_simulator)
        adpSimulator.setOnClickListener { adpSimulator() }
        val draftInfo = buttonBase.findViewById<ImageButton>(R.id.rankings_draft_info)
        draftInfo.setOnClickListener { draftInfo() }
        val comparePlayers = buttonBase.findViewById<ImageButton>(R.id.rankings_comparator)
        comparePlayers.setOnClickListener { comparePlayers() }
        val sortPlayers = buttonBase.findViewById<ImageButton>(R.id.rankings_sort)
        sortPlayers.setOnClickListener { sortPlayers() }
        initRankingsContext()
        hideKeyboard(this)
        if (LocalSettingsHelper.wereRankingsFetched(this)) {
            getUserCustomPlayerData(this)
        }

        // Cogneato stuff
        username = currUser
        user = pool!!.getUser(username)
        refreshTokens()
    }

    fun setUserCustomData(watchList: MutableList<String>, notes: MutableMap<String, String>) {
        Rankings.setCustomUserData(watchList, notes)

        // Just in case to avoid random activity shifts
        if (findViewById<View?>(R.id.rankings_list) != null) {
            displayRankings(rankings!!.orderedIds)
        }
    }

    private fun initRankingsContext() {
        val currentLeagueId = LocalSettingsHelper.getCurrentLeagueName(this)
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currentLeague = rankingsDB!!.getLeague(this, currentLeagueId)
        }
        rankingsBase = findViewById(R.id.rankings_base_layout)
        establishLayout()
    }

    private fun establishLayout() {
        if (LocalSettingsHelper.wereRankingsFetched(this)) {
            nDrawer.menu.findItem(R.id.nav_refresh_ranks).isVisible = true
            nDrawer.menu.findItem(R.id.nav_export_rankings).isVisible = true
            // If rankings are saved, load (and ultimately display) them
            if (rankings == null || rankings!!.players.isEmpty() || loadRanks) {
                Log.d(tag, "Loading rankings")
                Rankings.loadRankings(this, rankingsDB)
            } else {
                var nullPlayer = false
                for (key in rankings!!.players.keys) {
                    if (rankings!!.getPlayer(key) == null) {
                        nullPlayer = true
                    }
                }
                if (nullPlayer) {
                    Log.d(tag, "Null value was found, re-loading rankings.")
                    Rankings.loadRankings(this, rankingsDB)
                } else {
                    processNewRankings(rankings, false)
                }
            }
        } else if (!LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
            // Otherwise, if no league is set up, display that message
            clearAndAddView(R.layout.content_rankings_no_league)
            rankings = Rankings.initWithDefaults(currentLeague!!)
            searchBase.visibility = View.GONE
            buttonBase.visibility = View.GONE
            nDrawer.menu.findItem(R.id.nav_refresh_ranks).isVisible = false
            nDrawer.menu.findItem(R.id.nav_export_rankings).isVisible = false
        } else {
            // If neither of the above, there's a league but no ranks. Tell the user.
            clearAndAddView(R.layout.content_rankings_no_ranks)
            rankings = Rankings.initWithDefaults(currentLeague!!)
            searchBase.visibility = View.GONE
            buttonBase.visibility = View.GONE
            nDrawer.menu.findItem(R.id.nav_refresh_ranks).isVisible = true
            nDrawer.menu.findItem(R.id.nav_export_rankings).isVisible = false
        }
    }

    fun processNewRankings(newRankings: Rankings?, saveRanks: Boolean) {
        rankings = newRankings
        if (!filterToggleConfigured && filterItem != null) {
            // There's a possible case where the rankings load after the menu is set up, and it
            // clears ranks. To fight that, we don't hit that code until rankings are fetched.
            setFilterItemVisibility()
        }
        clearAndAddView(R.layout.content_rankings_display)
        displayRankings(rankings!!.orderedIds)
        if (saveRanks) {
            // Don't save again if we're just displaying rankings we just loaded
            rankings!!.saveRankings(this, rankingsDB)
        }
        if (intent.getBooleanExtra(Constants.RANKINGS_LIST_RELOAD_NEEDED, false)) {
            setUserSettings(rankings!!.userSettings)
        }
        if (settingsNeedRefresh) {
            getUserSettings(this)
        }
    }

    private fun clearAndAddView(viewId: Int) {
        rankingsBase.removeAllViews()
        val child = layoutInflater.inflate(viewId, null)
        rankingsBase.addView(child)
    }

    private fun displayRankings(orderedIds: List<String>) {
        // First, pre-process the ordered ids to get personal ranks
        val playerRanks = getPositionalRank(orderedIds, rankings!!)
        ranksDisplayed = false
        searchBase.visibility = View.VISIBLE
        buttonBase.visibility = View.VISIBLE
        nDrawer.menu.findItem(R.id.nav_refresh_ranks).isVisible = true
        nDrawer.menu.findItem(R.id.nav_export_rankings).isVisible = true
        if (filterItem != null) {
            filterItem!!.isVisible = true
        }
        val listview = findViewById<RankingsListView>(R.id.rankings_list)
        listview.adapter = null
        val data: MutableList<MutableMap<String, String?>> = ArrayList()
        val adapter = getDisplayAdapter(this, data)
        listview.adapter = adapter
        for (i in 0 until orderedIds.size.coerceAtMost(maxPlayers)) {
            val player = rankings!!.getPlayer(orderedIds[i])
            if (rankings!!.leagueSettings.rosterSettings.isPositionValid(player.position) &&
                    !rankings!!.draft.isDrafted(player)) {
                if (rankings!!.leagueSettings.isRookie && player.rookieRank == Constants.DEFAULT_RANK) {
                    // the constant is 'not set', so skip these. No sense showing a 10 year vet in rookie ranks.
                    continue
                }
                val datum: MutableMap<String, String?> = getDatumForPlayer(rankings!!, player,
                        true, playerRanks[player.uniqueId] ?: error(""),
                        rankings!!.userSettings.isShowNoteRank)
                data.add(datum)
            }
        }
        adapter.notifyDataSetChanged()
        val act: Activity = this
        val localCopy: Activity = this
        val swipeListener = SwipeDismissTouchListener(listview,
                object : SwipeDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(view: View?): Boolean {
                        return true
                    }

                    override fun onDismiss(listView: RecyclerView?,
                                           reverseSortedPositions: IntArray?,
                                           rightDismiss: Boolean) {
                        for (position in reverseSortedPositions!!) {
                            val datum = data[position]
                            val name = datum[Constants.PLAYER_BASIC]!!.split(Constants.RANKINGS_LIST_DELIMITER).toTypedArray()[1]
                            val posAndTeam = datum[Constants.PLAYER_INFO]!!.split("\n".toRegex()).toTypedArray()[0].split(" \\(".toRegex()).toTypedArray()[0]
                            val pos = posAndTeam.split(Constants.POS_TEAM_DELIMITER).toTypedArray()[0].replace("\\d".toRegex(), "")
                            val team = posAndTeam.split(Constants.POS_TEAM_DELIMITER).toTypedArray()[1]
                            val player = rankings!!.getPlayer(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos)
                            val listener = getUndraftListener(localCopy, rankings!!, player, adapter,
                                    data, datum, position, true)
                            if (!rightDismiss) {
                                rankings!!.draft.draftBySomeone(rankings!!, player, localCopy, listener)
                                if (rankings!!.userSettings.isHideDraftedSearch) {
                                    setSearchAutocomplete()
                                }
                            } else {
                                if (rankings!!.leagueSettings.isAuction) {
                                    getAuctionCost(player, position, data, datum, adapter, listener)
                                } else {
                                    draftByMe(player, 0, listener)
                                }
                            }
                            data.removeAt(position)
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
                object : RecyclerViewAdapter.OnItemLongClickListener {
                    override fun onItemLongClick(view: View?, position: Int): Boolean {
                        val playerKey = getPlayerKeyFromListViewItem(view!!)
                        val playerStatus = view.findViewById<ImageView>(R.id.player_status)
                        val player = rankings!!.getPlayer(playerKey)
                        if (rankings!!.isPlayerWatched(playerKey)) {
                            rankings!!.togglePlayerWatched(act, player.uniqueId)
                            val add = object : OnActionTapListener {
                                override fun onActionTapped(bar: Flashbar) {
                                    bar.dismiss()
                                    playerStatus.setImageResource(R.drawable.star)
                                    rankings!!.togglePlayerWatched(act, player.uniqueId)
                                }
                            }
                            generateFlashbarWithUndo(act, "Success!", player.name + " removed from watch list", Flashbar.Gravity.BOTTOM, add)
                                    .show()
                            playerStatus.setImageResource(0)
                        } else {
                            rankings!!.togglePlayerWatched(act, player.uniqueId)
                            val remove = object : OnActionTapListener {
                                override fun onActionTapped(bar: Flashbar) {
                                    bar.dismiss()
                                    playerStatus.setImageResource(0)
                                    rankings!!.togglePlayerWatched(act, player.uniqueId)
                                }
                            }
                            generateFlashbarWithUndo(act, "Success!", player.name + " added to watch list", Flashbar.Gravity.BOTTOM, remove)
                                    .show()
                            playerStatus.setImageResource(R.drawable.star)
                        }
                        return true
                    }
                })
        adapter.setOnTouchListener(swipeListener)
        listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(view: RecyclerView, scrollState: Int) {
                swipeListener.setEnabled(scrollState == RecyclerView.SCROLL_STATE_IDLE)
            }

            override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                if (ranksDisplayed) {
                    // A flag is used to green light this set, otherwise onScroll is set to 0 on initial display
                    val layoutManager = view.layoutManager as LinearLayoutManager?
                    selectedIndex = layoutManager!!.findFirstCompletelyVisibleItemPosition()
                }
            }
        })
        listview.layoutManager!!.scrollToPosition(selectedIndex)
        ranksDisplayed = true
        listview.addItemDecoration(getVerticalDividerDecoration(this))
        adapter.notifyDataSetChanged()
        setSearchAutocomplete()
    }

    private fun getAuctionCost(player: Player, position: Int, data: MutableList<MutableMap<String, String?>>,
                               datum: MutableMap<String, String?>, adapter: RecyclerViewAdapter, listener: OnActionTapListener) {
        val act: Activity = this
        val callback: AuctionCostInterface = object : AuctionCostInterface {
            override fun onValidInput(cost: Int?) {
                hideKeyboard(act)
                draftByMe(player, cost!!, listener)
            }

            override fun onInvalidInput() {
                hideKeyboard(act)
                generateTextOnlyFlashbar(act, "No can do", "Must provide a number for cost", Flashbar.Gravity.BOTTOM)
                        .show()
                data.add(position, datum)
                adapter.notifyDataSetChanged()
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
        rankings!!.draft.draftByMe(rankings!!, player, this, cost, listener)
        if (rankings!!.userSettings.isHideDraftedSearch) {
            setSearchAutocomplete()
        }
    }

    private fun setSearchAutocomplete() {
        val searchInput = searchBase.findViewById<AutoCompleteTextView>(R.id.ranking_search)
        searchInput.setAdapter(null)
        val mAdapter: FilterWithSpaceAdapter<*> = getPlayerSearchAdapter(rankings!!, this,
                rankings!!.userSettings.isHideDraftedSearch,
                rankings!!.userSettings.isHideRanklessSearch)
        searchInput.setAdapter(mAdapter)
        searchInput.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, view: View?, _: Int, _: Long ->
            searchInput.setText("")
            displayPlayerInfo(getPlayerIdFromSearchView(view!!))
        }
        val listview = findViewById<RankingsListView>(R.id.rankings_list)
        searchInput.setOnLongClickListener {
            if (searchInput.text.isNotEmpty()) {
                searchInput.setText("")
            } else {
                listview.smoothScrollToPosition(0)
            }
            true
        }
    }

    private fun displayPlayerInfo(playerKey: String) {
        val intent = Intent(this, PlayerInfo::class.java)
        intent.putExtra(Constants.PLAYER_ID, playerKey)
        startActivity(intent)
    }

    private fun adpSimulator() {
            val intent = Intent(this, ADPSimulator::class.java)
            startActivity(intent)
    }
    private fun draftInfo() {
        val intent = Intent(this, DraftInfo::class.java)
        startActivity(intent)
    }

    private fun comparePlayers() {
        val intent = Intent(this, PlayerComparator::class.java)
        startActivity(intent)
    }

    private fun sortPlayers() {
        val intent = Intent(this, PlayerSorter::class.java)
        startActivity(intent)
    }

    // Handle when the a navigation item is selected
    private fun setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener { item: MenuItem ->
            performAction(item)
            true
        }
    }

    // Perform the action for the selected navigation item
    private fun performAction(item: MenuItem) {
        // Close the navigation drawer
        mDrawer.closeDrawers()
        when (item.itemId) {
            R.id.nav_player_news -> playerNews()
            R.id.nav_league_settings -> leagueSettings()
            R.id.nav_refresh_ranks -> refreshRanks()
            R.id.nav_export_rankings -> exportRanks()
            R.id.nav_rankings_help -> help
            R.id.nav_user_settings -> userSettings()
            R.id.nav_user_change_password -> changePassword()
            R.id.nav_user_sign_out -> signOut()
        }
    }

    private fun playerNews() {
        val playerNews = Intent(this, FantasyNews::class.java)
        startActivity(playerNews)
    }

    private fun leagueSettings() {
        val leagueSettingsActivity = Intent(this, LeagueSettingsActivity::class.java)
        startActivity(leagueSettingsActivity)
    }

    private fun exportRanks() {
        if (rankings!!.players.isNotEmpty()) {
            val exportRanksActivity = Intent(this, ExportRankings::class.java)
            startActivity(exportRanksActivity)
        } else {
            generateTextOnlyFlashbar(this, "No can do", "No rankings saved to export", Flashbar.Gravity.BOTTOM)
                    .show()
        }
    }

    private val help: Unit
        get() {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

    fun refreshRanks() {
        // Don't let the user refresh if there's no saved league
        if (confirmInternet(this)) {
            if (LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
                rankings!!.refreshRankings(this)
            } else {
                generateTextOnlyFlashbar(this, "No can do", "Set up a league before getting rankings", Flashbar.Gravity.BOTTOM)
                        .show()
            }
        } else {
            generateTextOnlyFlashbar(this, "No can do", "No internet connection", Flashbar.Gravity.BOTTOM)
                    .show()
        }
    }

    private fun userSettings() {
        val settingsActivity = Intent(this, SettingsActivity::class.java)
        startActivity(settingsActivity)
    }

    private fun changePassword() {
        val changePssActivity = Intent(this, ChangePasswordActivity::class.java)
        startActivity(changePssActivity)
    }

    private fun signOut() {
        user!!.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun refreshTokens() {
        Log.d(tag, "Beginning token refresh check")
        pool!!.getUser(username).getSessionInBackground(RefreshSessionHandler())
    }

    private inner class RefreshSessionHandler : AuthenticationHandler {
        override fun onSuccess(cognitoUserSession: CognitoUserSession, device: CognitoDevice?) {
            Log.i(tag, "Refresh success")
        }

        override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, username: String) {
            Log.d(tag, "Get auth details challenge thrown from rankings page, should never happen.")
            signOut()
        }

        override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
            Log.d(tag, "MFA challenge thrown from rankings page, should never happen.")
            signOut()
        }

        override fun authenticationChallenge(continuation: ChallengeContinuation) {
            Log.d(tag, "Authentication challenge thrown from rankings page, should never happen.")
            signOut()
        }

        override fun onFailure(e: Exception) {
            Log.d(tag, "Failed to refresh token from rankings page, should never happen.", e)
            signOut()
        }
    }

    private fun exit() {
        finishAffinity()
    }

    companion object {
        private var selectedIndex = 0
    }
}