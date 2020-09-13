package com.devingotaswitch.rankings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.amazonaws.util.StringUtils
import com.andrognito.flashbar.Flashbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.domain.LeagueSettings
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.RosterSettings
import com.devingotaswitch.rankings.domain.RosterSettings.Flex
import com.devingotaswitch.rankings.domain.ScoringSettings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.utils.GeneralUtils.isDouble
import com.devingotaswitch.utils.GeneralUtils.isInteger
import org.angmarch.views.NiceSpinner
import java.util.*

class LeagueSettingsActivity : AppCompatActivity() {
    private val tag = "LeagueSettings"
    private val createNewLeagueSpinnerText = "Create New League"
    private lateinit var rankingsDB: RankingsDBWrapper
    private var baseLayout: LinearLayout? = null
    private lateinit var rankings: Rankings
    private var mainTitle: TextView? = null
    private var rankingsUpdated = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_league_settings)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Set toolbar for this screen
        val toolbar = findViewById<Toolbar>(R.id.toolbar_league_settings)
        toolbar.title = ""
        mainTitle = findViewById(R.id.main_toolbar_title)
        mainTitle!!.text = "League Settings"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        rankingsUpdated = false
        val localCopy: Activity = this
        toolbar.setNavigationOnClickListener {
            val intent = Intent(applicationContext, RankingsHome::class.java)
            intent.putExtra(Constants.RANKINGS_UPDATED, rankingsUpdated)
            localCopy.startActivity(intent)
        }
    }

    public override fun onResume() {
        super.onResume()
        try {
            init()
        } catch (e: Exception) {
            Log.d(tag, "Failure setting up activity, falling back to Rankings", e)
            onBackPressed()
        }
    }

    private fun init() {
        if (!this::rankingsDB.isInitialized) {
            rankingsDB = RankingsDBWrapper()
        }
        rankings = Rankings.init()
        baseLayout = findViewById(R.id.league_settings_base)
        initLeagues()
        hideKeyboard(this)
    }

    private fun initLeagues() {
        if (rankings.getUserLeagues() != null && rankings.getUserLeagues()!!.leaguesMap.isNotEmpty()) {
            displayLeague(rankings.getUserLeagues()!!.currentLeague)
        } else {
            displayNoLeague()
        }
        initializeLeagueSpinner()
    }

    private fun initializeLeagueSpinner() {
        val spinner = findViewById<NiceSpinner>(R.id.league_settings_spinner)
        if (rankings.getUserLeagues() == null || rankings.getUserLeagues()!!.leaguesMap.isEmpty()) {
            spinner.visibility = View.GONE
            return
        }
        spinner.visibility = View.VISIBLE
        val leagueNames: MutableList<String?> = ArrayList()
        var currLeagueIndex = 0
        val leagueList = rankings.getUserLeagues()!!.leaguesMap.values.sortedByDescending { it.name }
        for (league in leagueList) {
            leagueNames.add(league.name)
            if (league.name == rankings.getLeagueSettings().name) {
                currLeagueIndex = leagueList.indexOf(league)
            }
        }
        leagueNames.add(createNewLeagueSpinnerText)
        spinner.attachDataSource(leagueNames)
        spinner.selectedIndex = currLeagueIndex
        spinner.setBackgroundColor(Color.parseColor("#FAFAFA"))
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                if (createNewLeagueSpinnerText == leagueNames[i]) {
                    displayNoLeague()
                } else {
                    displayLeague(rankings.getUserLeagues()!!.getLeagueByName(leagueNames[i]!!))
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                // Nada
            }
        })
    }

    private fun displayLeague(currentLeague: LeagueSettings?) {
        val view = initializeLeagueSettingsBase()
        mainTitle!!.text = "League Settings"
        val leagueName = view.findViewById<EditText>(R.id.league_settings_name)
        leagueName.setText(currentLeague!!.name)
        leagueName.visibility = View.GONE
        val teamCount = view.findViewById<EditText>(R.id.league_settings_team_count)
        teamCount.setText(java.lang.String.valueOf(currentLeague.teamCount))
        val auctionBudget = view.findViewById<EditText>(R.id.league_settings_auction_budget)
        hideKeyboard(this)
        val isAuction = view.findViewById<RadioButton>(R.id.league_settings_auction)
        val isSnake = view.findViewById<RadioButton>(R.id.league_settings_snake)
        val isDynasty = view.findViewById<RadioButton>(R.id.league_settings_dynasty_startup)
        val isRookie = view.findViewById<RadioButton>(R.id.league_settings_dynasty_rookie)
        val isBestBall = view.findViewById<RadioButton>(R.id.league_settings_best_ball)
        when {
            currentLeague.isAuction -> {
                isAuction.isSelected = true
                isAuction.isChecked = true
                auctionBudget.setText(java.lang.String.valueOf(currentLeague.auctionBudget))
            }
            currentLeague.isSnake -> {
                isSnake.isSelected = true
                isSnake.isChecked = true
            }
            currentLeague.isDynasty -> {
                isDynasty.isSelected = true
                isDynasty.isChecked = true
            }
            currentLeague.isRookie -> {
                isRookie.isSelected = true
                isRookie.isChecked = true
            }
            currentLeague.isBestBall -> {
                isBestBall.isSelected = true
                isBestBall.isChecked = true
            }
        }
        val delete = findViewById<Button>(R.id.league_settings_delete_league)
        delete.visibility = View.VISIBLE
        val save = view.findViewById<Button>(R.id.league_settings_create_default)
        save.text = "Update"
        val advanced = view.findViewById<Button>(R.id.league_settings_advanced_settings)
        val act: Activity = this
        save.setOnClickListener {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction, false)) {
                return@setOnClickListener
            }
            val newLeague = getLeagueUpdates(currentLeague, leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget)
            if (newLeague == null) {
                hideKeyboard(act)
                generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show()
                return@setOnClickListener
            }
            updateLeague(null, null, null, newLeague, currentLeague)
            generateTextOnlyFlashbar(act, "Success!", currentLeague.name + " updated", Flashbar.Gravity.BOTTOM)
                    .show()
        }
        advanced.setOnClickListener {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction, false)) {
                return@setOnClickListener
            }
            val newLeague = getLeagueUpdates(currentLeague, leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget)
            displayRoster(currentLeague, newLeague)
        }
        delete.setOnClickListener {
            if (rankings.getUserLeagues()!!.leaguesMap.size > 1) {
                deleteLeague(currentLeague)
            } else {
                hideKeyboard(act)
                generateTextOnlyFlashbar(act, "No can do", "Can't delete league, none would remain", Flashbar.Gravity.BOTTOM)
                        .show()
            }
        }
        rankings.getUserLeagues()!!.updateCurrentLeague(currentLeague, this)
    }

    private fun displayNoLeague() {
        val view = initializeLeagueSettingsBase()
        mainTitle!!.text = "League Settings"
        val advanced = view.findViewById<Button>(R.id.league_settings_advanced_settings)
        val save = view.findViewById<Button>(R.id.league_settings_create_default)
        val delete = view.findViewById<Button>(R.id.league_settings_delete_league)
        delete.visibility = View.GONE
        val leagueName = view.findViewById<EditText>(R.id.league_settings_name)
        leagueName.visibility = View.VISIBLE
        val teamCount = view.findViewById<EditText>(R.id.league_settings_team_count)
        val auctionBudget = view.findViewById<EditText>(R.id.league_settings_auction_budget)
        val isAuction = view.findViewById<RadioButton>(R.id.league_settings_auction)
        val isSnake = view.findViewById<RadioButton>(R.id.league_settings_snake)
        val isDynasty = view.findViewById<RadioButton>(R.id.league_settings_dynasty_startup)
        val isRookie = view.findViewById<RadioButton>(R.id.league_settings_dynasty_rookie)
        val isBestBall = view.findViewById<RadioButton>(R.id.league_settings_best_ball)
        isSnake.isChecked = true
        leagueName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // don't care
            }

            override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
                if (s.toString().trim { it <= ' ' }.isEmpty()) {
                    deactivateButton(advanced)
                    deactivateButton(save)
                } else if (isInteger(teamCount.text.toString())) {
                    activateButton(advanced)
                    activateButton(save)
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // Don't care
            }
        })
        teamCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // Don't care
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!isInteger(charSequence.toString())) {
                    deactivateButton(save)
                    deactivateButton(advanced)
                } else if (!StringUtils.isBlank(leagueName.text.toString())) {
                    activateButton(save)
                    activateButton(advanced)
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // Don't care
            }
        })
        deactivateButton(save)
        deactivateButton(advanced)
        val act: Activity = this
        save.setOnClickListener {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction, true)) {
                return@setOnClickListener
            }
            val defaults = getLeagueSettingsFromFirstPage(leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget)
            saveNewLeague(defaults)
            generateTextOnlyFlashbar(act, "Success!", defaults.name + " saved", Flashbar.Gravity.BOTTOM)
                    .show()
        }
        advanced.setOnClickListener {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction, true)) {
                return@setOnClickListener
            }
            val defaults = getLeagueSettingsFromFirstPage(leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget)
            displayRosterNoLeague(defaults)
        }
    }

    private fun deactivateButton(button: Button) {
        button.isClickable = false
        button.isEnabled = false
        button.setBackgroundColor(-0x1e1e1f)
    }

    private fun activateButton(button: Button) {
        button.isClickable = true
        button.isEnabled = true
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default))
    }

    private fun validateLeagueInputs(name: EditText, teamCount: EditText, auctionBudget: EditText,
                                     isAuction: RadioButton, isNewLeague: Boolean): Boolean {
        val givenName = name.text.toString()
        val givenTeamCount = teamCount.text.toString()
        val givenAuctionBudget = auctionBudget.text.toString()
        if (StringUtils.isBlank(givenName)) {
            generateTextOnlyFlashbar(this, "No can do", "League name can't be empty", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (isNewLeague && rankings.getUserLeagues() != null && rankings.getUserLeagues()!!.leaguesMap.containsKey(givenName)) {
            generateTextOnlyFlashbar(this, "No can do",
                    "$givenName is already a league name, must be unique.", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(givenTeamCount) ||
                !isInteger(givenTeamCount)) {
            generateTextOnlyFlashbar(this, "No can do", "Team count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        val teamCountInt = givenTeamCount.toInt()
        if (teamCountInt < 1 || teamCountInt > 32) {
            generateTextOnlyFlashbar(this, "No can do", "Invalid team count given", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (isAuction.isChecked) {
            if (StringUtils.isBlank(givenAuctionBudget) || !isInteger(givenAuctionBudget)) {
                generateTextOnlyFlashbar(this, "No can do", "Auction budget not provided", Flashbar.Gravity.TOP)
                        .show()
                return true
            }
            val auctionBudgetInt = givenAuctionBudget.toInt()
            if (auctionBudgetInt < 1) {
                generateTextOnlyFlashbar(this, "No can do", "Auction budget must be a positive number", Flashbar.Gravity.TOP)
                        .show()
                return true
            }
        }
        return false
    }

    private fun getLeagueUpdates(league: LeagueSettings?, name: EditText, teamCount: EditText,
                                 isAuction: RadioButton, isSnake: RadioButton, isDynasty: RadioButton,
                                 isRookie: RadioButton, isBestBall: RadioButton, auctionBudget: EditText): LeagueSettings? {
        var auctionBudgetNew = Constants.DEFAULT_AUCTION_BUDGET
        if (isAuction.isChecked) {
            auctionBudgetNew = auctionBudget.text.toString().toInt()
        }
        if (league!!.name != name.text.toString() || teamCount.text.toString().toInt() != league.teamCount ||
                isSnake.isChecked && !league.isSnake || isAuction.isChecked && !league.isAuction ||
                isDynasty.isChecked && !league.isDynasty || isRookie.isChecked && !league.isRookie ||
                isBestBall.isChecked && !league.isBestBall || (league.isAuction && league.auctionBudget != auctionBudgetNew)) {
            return LeagueSettings(name.text.toString(), teamCount.text.toString().toInt(),
                    isSnake.isChecked, isAuction.isChecked, isDynasty.isChecked, isRookie.isChecked, isBestBall.isChecked,
                    true, auctionBudgetNew)
        }
        return null
    }

    private fun getLeagueSettingsFromFirstPage(leagueName: EditText, teamCount: EditText, isAuction: RadioButton,
                                               isSnake: RadioButton, isDynasty: RadioButton, isRookie: RadioButton,
                                               isBestBall: RadioButton, auctionBudget: EditText): LeagueSettings {
        var realBudget = Constants.DEFAULT_AUCTION_BUDGET
        if (isInteger(auctionBudget.text.toString())) {
            realBudget = auctionBudget.text.toString().toInt()
        }
        return LeagueSettings(leagueName.text.toString(), teamCount.text.toString().toInt(), isSnake.isChecked, isAuction.isChecked,
                isDynasty.isChecked, isRookie.isChecked, isBestBall.isChecked, true, realBudget)
    }

    private fun initializeLeagueSettingsBase(): View {
        baseLayout!!.removeAllViews()
        val child = layoutInflater.inflate(R.layout.league_settings_base, null)
        baseLayout!!.addView(child)

        // Hide auction budget on snake selection
        val auctionBudget = child.findViewById<EditText>(R.id.league_settings_auction_budget)
        val auctionBudgetHeader = child.findViewById<TextView>(R.id.league_settings_auction_budget_header)
        val isAuction = child.findViewById<RadioButton>(R.id.league_settings_auction)
        isAuction.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                auctionBudget.visibility = View.VISIBLE
                auctionBudgetHeader.visibility = View.VISIBLE
            } else {
                auctionBudget.visibility = View.GONE
                auctionBudgetHeader.visibility = View.GONE
            }
        }
        return child
    }

    private fun displayRoster(currentLeague: LeagueSettings?, newLeague: LeagueSettings?) {
        val view = initializeLeagueSettingsRoster()
        mainTitle!!.text = "Roster Settings"
        val update = view.findViewById<Button>(R.id.league_roster_create_default)
        update.text = "Update"
        val advanced = view.findViewById<Button>(R.id.league_roster_advanced_settings)
        val roster = currentLeague!!.rosterSettings
        val qbs = view.findViewById<EditText>(R.id.league_settings_qbs)
        qbs.setText(java.lang.String.valueOf(roster.qbCount))
        val rbs = view.findViewById<EditText>(R.id.league_settings_rbs)
        rbs.setText(java.lang.String.valueOf(roster.rbCount))
        val wrs = view.findViewById<EditText>(R.id.league_settings_wrs)
        wrs.setText(java.lang.String.valueOf(roster.wrCount))
        val tes = view.findViewById<EditText>(R.id.league_settings_tes)
        tes.setText(java.lang.String.valueOf(roster.teCount))
        val dsts = view.findViewById<EditText>(R.id.league_settings_dsts)
        dsts.setText(java.lang.String.valueOf(roster.dstCount))
        val ks = view.findViewById<EditText>(R.id.league_settings_ks)
        ks.setText(java.lang.String.valueOf(roster.kCount))
        val bench = view.findViewById<EditText>(R.id.league_settings_bench)
        bench.setText(java.lang.String.valueOf(roster.benchCount))
        val dstskHeader = findViewById<LinearLayout>(R.id.league_roster_space2)
        val benchHeader = findViewById<LinearLayout>(R.id.league_roster_space3)
        if (currentLeague.isRookie) {
            dsts.visibility = View.GONE
            ks.visibility = View.GONE
            bench.visibility = View.GONE
            dsts.setText(0.toString())
            ks.setText(0.toString())
            bench.setText(0.toString())
            dstskHeader.visibility = View.GONE
            benchHeader.visibility = View.GONE
        } else {
            dsts.visibility = View.VISIBLE
            ks.visibility = View.VISIBLE
            bench.visibility = View.VISIBLE
            dstskHeader.visibility = View.VISIBLE
            benchHeader.visibility = View.VISIBLE
        }
        val act: Activity = this
        update.setOnClickListener {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return@setOnClickListener
            }
            val newRoster = getRosterUpdates(qbs, rbs, wrs, tes, dsts, ks, bench, currentLeague)
            if (newRoster == null && newLeague == null) {
                hideKeyboard(act)
                generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show()
                return@setOnClickListener
            }
            updateLeague(null, newRoster, null, newLeague, currentLeague)
            generateTextOnlyFlashbar(act, "Success!", currentLeague.name + " updated", Flashbar.Gravity.BOTTOM)
                    .show()
        }
        advanced.setOnClickListener {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return@setOnClickListener
            }
            val newRoster = getRosterUpdates(qbs, rbs, wrs, tes, dsts, ks, bench, currentLeague)
            displayFlex(currentLeague, newLeague, newRoster)
        }
    }

    private fun displayRosterNoLeague(newLeague: LeagueSettings) {
        val view = initializeLeagueSettingsRoster()
        mainTitle!!.text = "Roster Settings"
        val qbs = view.findViewById<EditText>(R.id.league_settings_qbs)
        val rbs = view.findViewById<EditText>(R.id.league_settings_rbs)
        val wrs = view.findViewById<EditText>(R.id.league_settings_wrs)
        val tes = view.findViewById<EditText>(R.id.league_settings_tes)
        val dsts = view.findViewById<EditText>(R.id.league_settings_dsts)
        val ks = view.findViewById<EditText>(R.id.league_settings_ks)
        val bench = view.findViewById<EditText>(R.id.league_settings_bench)
        val dstskHeader = findViewById<LinearLayout>(R.id.league_roster_space2)
        val benchHeader = findViewById<LinearLayout>(R.id.league_roster_space3)
        if (newLeague.isRookie) {
            dsts.visibility = View.GONE
            ks.visibility = View.GONE
            bench.visibility = View.GONE
            dsts.setText(0.toString())
            ks.setText(0.toString())
            bench.setText(0.toString())
            dstskHeader.visibility = View.GONE
            benchHeader.visibility = View.GONE
        } else {
            dsts.visibility = View.VISIBLE
            ks.visibility = View.VISIBLE
            bench.visibility = View.VISIBLE
            dstskHeader.visibility = View.VISIBLE
            benchHeader.visibility = View.VISIBLE
            dsts.setText("1")
            ks.setText("1")
            bench.setText("6")
        }
        val save = view.findViewById<Button>(R.id.league_roster_create_default)
        val advanced = view.findViewById<Button>(R.id.league_roster_advanced_settings)
        val act: Activity = this
        save.setOnClickListener {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return@setOnClickListener
            }
            val defaults = getRosterSettingsFromFirstPage(qbs, rbs, wrs, tes, dsts, ks, bench)
            newLeague.rosterSettings = defaults
            saveNewLeague(newLeague)
            generateTextOnlyFlashbar(act, "Success!", newLeague.name + " saved", Flashbar.Gravity.BOTTOM)
                    .show()
        }
        advanced.setOnClickListener {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return@setOnClickListener
            }
            val defaults = getRosterSettingsFromFirstPage(qbs, rbs, wrs, tes, dsts, ks, bench)
            newLeague.rosterSettings = defaults
            displayFlexNoLeague(newLeague)
        }
    }

    private fun initializeLeagueSettingsRoster(): View {
        baseLayout!!.removeAllViews()
        val child = layoutInflater.inflate(R.layout.league_settings_roster, null)
        baseLayout!!.addView(child)
        return child
    }

    private fun getRosterSettingsFromFirstPage(qbs: EditText, rbs: EditText, wrs: EditText, tes: EditText, dsts: EditText,
                                               ks: EditText, bench: EditText): RosterSettings {
        val qbTotal = qbs.text.toString().toInt()
        val rbTotal = rbs.text.toString().toInt()
        val wrTotal = wrs.text.toString().toInt()
        val teTotal = tes.text.toString().toInt()
        val dstTotal = dsts.text.toString().toInt()
        val kTotal = ks.text.toString().toInt()
        val benchTotal = bench.text.toString().toInt()
        return RosterSettings(qbTotal, rbTotal, wrTotal, teTotal, dstTotal, kTotal, benchTotal)
    }

    private fun validateRosterInputs(qbs: EditText, rbs: EditText, wrs: EditText, tes: EditText, dsts: EditText,
                                     ks: EditText, bench: EditText): Boolean {
        val qbStr = qbs.text.toString()
        val rbStr = rbs.text.toString()
        val wrStr = wrs.text.toString()
        val teStr = tes.text.toString()
        val dstStr = dsts.text.toString()
        val kStr = ks.text.toString()
        val benchStr = bench.text.toString()
        if (StringUtils.isBlank(qbStr) || !isInteger(qbStr)) {
            generateTextOnlyFlashbar(this, "No can do", "QB count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(rbStr) || !isInteger(rbStr)) {
            generateTextOnlyFlashbar(this, "No can do", "RB count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(wrStr) || !isInteger(wrStr)) {
            generateTextOnlyFlashbar(this, "No can do", "WR count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(teStr) || !isInteger(teStr)) {
            generateTextOnlyFlashbar(this, "No can do", "TE count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(dstStr) || !isInteger(dstStr)) {
            generateTextOnlyFlashbar(this, "No can do", "DST count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(kStr) || !isInteger(kStr)) {
            generateTextOnlyFlashbar(this, "No can do", "K count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(benchStr) || !isInteger(benchStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Bench count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        return false
    }

    private fun getRosterUpdates(qbs: EditText, rbs: EditText, wrs: EditText, tes: EditText, dsts: EditText,
                                 ks: EditText, bench: EditText, league: LeagueSettings?): RosterSettings? {
        val qbTotal = qbs.text.toString().toInt()
        val rbTotal = rbs.text.toString().toInt()
        val wrTotal = wrs.text.toString().toInt()
        val teTotal = tes.text.toString().toInt()
        val dstTotal = dsts.text.toString().toInt()
        val kTotal = ks.text.toString().toInt()
        val benchTotal = bench.text.toString().toInt()
        val roster = league!!.rosterSettings
        if (qbTotal != roster.qbCount || rbTotal != roster.rbCount || wrTotal != roster.wrCount ||
                teTotal != roster.teCount || dstTotal != roster.dstCount || kTotal != roster.kCount ||
                benchTotal != roster.benchCount) {
            return RosterSettings(qbTotal, rbTotal, wrTotal, teTotal, dstTotal, kTotal, benchTotal)
        }
        return null
    }

    private fun displayFlex(currentLeague: LeagueSettings?, newLeague: LeagueSettings?, newRoster: RosterSettings?) {
        val view = initializeLeagueSettingsFlex()
        mainTitle!!.text = "Flex Settings"
        val rbwr = view.findViewById<EditText>(R.id.league_flex_rbwr)
        val rbte = view.findViewById<EditText>(R.id.league_flex_rbte)
        val rbwrte = view.findViewById<EditText>(R.id.league_flex_rbwrte)
        val wrte = view.findViewById<EditText>(R.id.league_flex_wrte)
        val op = view.findViewById<EditText>(R.id.league_flex_op)
        rbwr.setText(java.lang.String.valueOf(currentLeague!!.rosterSettings.flex!!.rbwrCount))
        rbte.setText(java.lang.String.valueOf(currentLeague.rosterSettings.flex!!.rbteCount))
        rbwrte.setText(java.lang.String.valueOf(currentLeague.rosterSettings.flex!!.rbwrteCount))
        wrte.setText(java.lang.String.valueOf(currentLeague.rosterSettings.flex!!.wrteCount))
        op.setText(java.lang.String.valueOf(currentLeague.rosterSettings.flex!!.qbrbwrteCount))
        val update = findViewById<Button>(R.id.league_flex_create_default)
        update.text = "Update"
        val advanced = findViewById<Button>(R.id.league_flex_advanced_settings)
        val act: Activity = this
        update.setOnClickListener {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return@setOnClickListener
            }
            val newFlex = getFlexUpdates(rbwr, rbte, rbwrte, wrte, op, newRoster, currentLeague)
            if (newFlex == null && newRoster == null && newLeague == null) {
                hideKeyboard(act)
                generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show()
                return@setOnClickListener
            }
            updateLeague(null, newRoster, newFlex, newLeague, currentLeague)
            generateTextOnlyFlashbar(act, "Success!", currentLeague.name + " updated", Flashbar.Gravity.BOTTOM)
                    .show()
        }
        advanced.setOnClickListener {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return@setOnClickListener
            }
            val newFlex = getFlexUpdates(rbwr, rbte, rbwrte, wrte, op, newRoster, currentLeague)
            displayScoring(currentLeague, newLeague, newRoster, newFlex)
        }
    }

    private fun displayFlexNoLeague(newLeague: LeagueSettings) {
        val view = initializeLeagueSettingsFlex()
        mainTitle!!.text = "Flex Settings"
        val rbwr = view.findViewById<EditText>(R.id.league_flex_rbwr)
        val rbte = view.findViewById<EditText>(R.id.league_flex_rbte)
        val rbwrte = view.findViewById<EditText>(R.id.league_flex_rbwrte)
        val wrte = view.findViewById<EditText>(R.id.league_flex_wrte)
        val op = view.findViewById<EditText>(R.id.league_flex_op)
        val advanced = findViewById<Button>(R.id.league_flex_advanced_settings)
        val save = findViewById<Button>(R.id.league_flex_create_default)
        val act: Activity = this
        save.setOnClickListener {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return@setOnClickListener
            }
            val defaults = getFlexSettingsFromFirstPage(rbwr, rbte, rbwrte, wrte, op)
            newLeague.rosterSettings.flex = defaults
            saveNewLeague(newLeague)
            generateTextOnlyFlashbar(act, "Success!", newLeague.name + " saved", Flashbar.Gravity.BOTTOM)
                    .show()
        }
        advanced.setOnClickListener {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return@setOnClickListener
            }
            val defaults = getFlexSettingsFromFirstPage(rbwr, rbte, rbwrte, wrte, op)
            newLeague.rosterSettings.flex = defaults
            displayScoringNoTeam(newLeague)
        }
    }

    private fun initializeLeagueSettingsFlex(): View {
        baseLayout!!.removeAllViews()
        val child = layoutInflater.inflate(R.layout.league_settings_flex, null)
        baseLayout!!.addView(child)
        return child
    }

    private fun getFlexSettingsFromFirstPage(rbwr: EditText, rbte: EditText, rbwrte: EditText, wrte: EditText,
                                             op: EditText): Flex {
        val rbwrTotal = rbwr.text.toString().toInt()
        val rbteTotal = rbte.text.toString().toInt()
        val rbwrteTotal = rbwrte.text.toString().toInt()
        val wrteTotal = wrte.text.toString().toInt()
        val opTotal = op.text.toString().toInt()
        return Flex(rbwrTotal, rbteTotal, rbwrteTotal, wrteTotal, opTotal)
    }

    private fun validateFlexInputs(rbwr: EditText, rbte: EditText, rbwrte: EditText, wrte: EditText,
                                   op: EditText): Boolean {
        val rbwrStr = rbwr.text.toString()
        val rbteStr = rbte.text.toString()
        val rbwrteStr = rbwrte.text.toString()
        val wrteStr = wrte.text.toString()
        val opStr = op.text.toString()
        if (StringUtils.isBlank(rbwrStr) || !isInteger(rbwrStr)) {
            generateTextOnlyFlashbar(this, "No can do", "RB/WR count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(rbteStr) || !isInteger(rbteStr)) {
            generateTextOnlyFlashbar(this, "No can do", "RB/TE count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(rbwrteStr) || !isInteger(rbwrteStr)) {
            generateTextOnlyFlashbar(this, "No can do", "RB/WR/TE count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(wrteStr) || !isInteger(wrteStr)) {
            generateTextOnlyFlashbar(this, "No can do", "WR/TE count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(opStr) || !isInteger(opStr)) {
            generateTextOnlyFlashbar(this, "No can do", "QB/RB/WR/TE count not provided", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        return false
    }

    private fun getFlexUpdates(rbwr: EditText, rbte: EditText, rbwrte: EditText, wrte: EditText,
                               op: EditText, newRoster: RosterSettings?, league: LeagueSettings?): Flex? {
        val rbwrTotal = rbwr.text.toString().toInt()
        val rbteTotal = rbte.text.toString().toInt()
        val rbwrteTotal = rbwrte.text.toString().toInt()
        val wrteTotal = wrte.text.toString().toInt()
        val opTotal = op.text.toString().toInt()
        val roster = league!!.rosterSettings
        val flex = roster.flex
        if (rbwrTotal != flex!!.rbwrCount || rbteTotal != flex.rbteCount || rbwrteTotal != flex.rbwrteCount ||
                wrteTotal != flex.wrteCount || opTotal != flex.qbrbwrteCount) {
            return Flex(rbwrTotal, rbwrteTotal, rbteTotal, wrteTotal, opTotal)
        }
        return null
    }

    private fun displayScoringNoTeam(newLeague: LeagueSettings) {
        val view = initializeLeagueSettingsScoring()
        mainTitle!!.text = "Scoring Settings"
        val passTds = view.findViewById<EditText>(R.id.league_scoring_passing_tds)
        val rushTds = view.findViewById<EditText>(R.id.league_scoring_rushing_tds)
        val recTds = view.findViewById<EditText>(R.id.league_scoring_receiving_tds)
        val passYds = view.findViewById<EditText>(R.id.league_scoring_yds_per_passing_pt)
        val rushYds = view.findViewById<EditText>(R.id.league_scoring_yds_per_rushing_point)
        val recYds = view.findViewById<EditText>(R.id.league_scoring_yds_per_receiving_point)
        val ints = view.findViewById<EditText>(R.id.league_scoring_ints)
        val fumbles = view.findViewById<EditText>(R.id.league_scoring_fumbles)
        val ppr = view.findViewById<EditText>(R.id.league_scoring_ppr)
        val save = view.findViewById<Button>(R.id.league_scoring_save)
        val act: Activity = this
        save.setOnClickListener {
            if (validateScoringInputs(passTds, rushTds, recTds, passYds, rushYds, recYds,
                            ints, fumbles, ppr)) {
                return@setOnClickListener
            }
            val scoring = getScoringSettingsFromFirstPage(passTds, rushTds, recTds, passYds, rushYds, recYds,
                    ints, fumbles, ppr)
            newLeague.scoringSettings = scoring
            saveNewLeague(newLeague)
            generateTextOnlyFlashbar(act, "Success!", newLeague.name + " saved", Flashbar.Gravity.BOTTOM)
                    .show()
        }
    }

    private fun displayScoring(currentLeague: LeagueSettings?, newLeague: LeagueSettings?, newRoster: RosterSettings?,
            newFlex: Flex?) {
        val view = initializeLeagueSettingsScoring()
        mainTitle!!.text = "Scoring Settings"
        val passTds = view.findViewById<EditText>(R.id.league_scoring_passing_tds)
        val rushTds = view.findViewById<EditText>(R.id.league_scoring_rushing_tds)
        val recTds = view.findViewById<EditText>(R.id.league_scoring_receiving_tds)
        val passYds = view.findViewById<EditText>(R.id.league_scoring_yds_per_passing_pt)
        val rushYds = view.findViewById<EditText>(R.id.league_scoring_yds_per_rushing_point)
        val recYds = view.findViewById<EditText>(R.id.league_scoring_yds_per_receiving_point)
        val ints = view.findViewById<EditText>(R.id.league_scoring_ints)
        val fumbles = view.findViewById<EditText>(R.id.league_scoring_fumbles)
        val ppr = view.findViewById<EditText>(R.id.league_scoring_ppr)
        val update = view.findViewById<Button>(R.id.league_scoring_save)
        update.text = "Update"
        passTds.setText(java.lang.String.valueOf(currentLeague!!.scoringSettings.passingTds))
        rushTds.setText(java.lang.String.valueOf(currentLeague.scoringSettings.rushingTds))
        recTds.setText(java.lang.String.valueOf(currentLeague.scoringSettings.receivingTds))
        passYds.setText(java.lang.String.valueOf(currentLeague.scoringSettings.passingYards))
        rushYds.setText(java.lang.String.valueOf(currentLeague.scoringSettings.rushingYards))
        recYds.setText(java.lang.String.valueOf(currentLeague.scoringSettings.receivingYards))
        ints.setText(java.lang.String.valueOf(currentLeague.scoringSettings.interceptions))
        fumbles.setText(java.lang.String.valueOf(currentLeague.scoringSettings.fumbles))
        ppr.setText(java.lang.String.valueOf(currentLeague.scoringSettings.receptions))
        val act: Activity = this
        update.setOnClickListener {
            if (validateScoringInputs(passTds, rushTds, recTds, passYds, rushYds, recYds,
                            ints, fumbles, ppr)) {
                return@setOnClickListener
            }
            val newScoring = getScoringUpdates(passTds, rushTds, recTds, passYds, rushYds, recYds,
                    ints, fumbles, ppr, currentLeague)
            if (newRoster == null && newLeague == null && newScoring == null && newFlex == null) {
                hideKeyboard(act)
                generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show()
                return@setOnClickListener
            }
            updateLeague(newScoring, newRoster, newFlex, newLeague, currentLeague)
            generateTextOnlyFlashbar(act, "Success!", currentLeague.name + " updated", Flashbar.Gravity.BOTTOM)
                    .show()
        }
    }

    private fun initializeLeagueSettingsScoring(): View {
        baseLayout!!.removeAllViews()
        val child = layoutInflater.inflate(R.layout.league_settings_scoring, null)
        baseLayout!!.addView(child)
        return child
    }

    private fun getScoringSettingsFromFirstPage(passTds: EditText, rushTds: EditText, recTds: EditText, passYds: EditText,
                                                rushYds: EditText, recYds: EditText, ints: EditText, fumbles: EditText,
                                                receptions: EditText): ScoringSettings {
        val passTdsTotal = passTds.text.toString().toInt()
        val rushTdsTotal = rushTds.text.toString().toInt()
        val recTdsTotal = recTds.text.toString().toInt()
        val passYdsTotal = passYds.text.toString().toInt()
        val rushYdsTotal = rushYds.text.toString().toInt()
        val recYdsTotal = recYds.text.toString().toInt()
        val intsTotal = ints.text.toString().toDouble()
        val fumblesTotal = fumbles.text.toString().toDouble()
        val receptionsTotal = receptions.text.toString().toDouble()
        return ScoringSettings(passTdsTotal, rushTdsTotal, recTdsTotal, fumblesTotal, intsTotal, passYdsTotal,
                rushYdsTotal, recYdsTotal, receptionsTotal)
    }

    private fun validateScoringInputs(passTds: EditText, rushTds: EditText, recTds: EditText, passYds: EditText,
                                      rushYds: EditText, recYds: EditText, ints: EditText, fumbles: EditText,
                                      receptions: EditText): Boolean {
        val pTdsStr = passTds.text.toString()
        val ruTdsStr = rushTds.text.toString()
        val reTdsStr = recTds.text.toString()
        val pYdsStr = passYds.text.toString()
        val ruYdsStr = rushYds.text.toString()
        val reYdsStr = recYds.text.toString()
        val intStr = ints.text.toString()
        val fumblesStr = fumbles.text.toString()
        val recStr = receptions.text.toString()
        if (StringUtils.isBlank(pTdsStr) || !isInteger(pTdsStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Pts/passing td must be an integer", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(ruTdsStr) || !isInteger(ruTdsStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Pts/rushing td must be an integer", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(reTdsStr) || !isInteger(reTdsStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Pts/receiving td must be an integer", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(pYdsStr) || !isInteger(pYdsStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Passing yards/point must be an integer", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(ruYdsStr) || !isInteger(ruYdsStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Rushing yards/point must be an integer", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(reYdsStr) || !isInteger(reYdsStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Receiving yards/point must be an integer", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(intStr) || !isDouble(intStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Points/int must be a number (decimals allowed)", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(fumblesStr) || !isDouble(fumblesStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Points/fumble must be a number (decimals allowed)", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        if (StringUtils.isBlank(recStr) || !isDouble(recStr)) {
            generateTextOnlyFlashbar(this, "No can do", "Points/reception must be a number (decimals allowed)", Flashbar.Gravity.TOP)
                    .show()
            return true
        }
        return false
    }

    private fun getScoringUpdates(passTds: EditText, rushTds: EditText, recTds: EditText, passYds: EditText,
                                  rushYds: EditText, recYds: EditText, ints: EditText, fumbles: EditText,
                                  receptions: EditText, league: LeagueSettings?): ScoringSettings? {
        val passTdsTotal = passTds.text.toString().toInt()
        val rushTdsTotal = rushTds.text.toString().toInt()
        val recTdsTotal = recTds.text.toString().toInt()
        val passYdsTotal = passYds.text.toString().toInt()
        val rushYdsTotal = rushYds.text.toString().toInt()
        val recYdsTotal = recYds.text.toString().toInt()
        val intsTotal = ints.text.toString().toDouble()
        val fumblesTotal = fumbles.text.toString().toDouble()
        val receptionsTotal = receptions.text.toString().toDouble()
        val scoring = league!!.scoringSettings
        if (passTdsTotal != scoring.passingTds || rushTdsTotal != scoring.rushingTds ||
                recTdsTotal != scoring.receivingTds || passYdsTotal != scoring.passingYards ||
                rushYdsTotal != scoring.rushingYards || recYdsTotal != scoring.receivingYards ||
                intsTotal != scoring.interceptions || fumblesTotal != scoring.fumbles ||
                receptionsTotal != scoring.receptions) {
            return ScoringSettings(passTdsTotal, rushTdsTotal, recTdsTotal, fumblesTotal, intsTotal,
                passYdsTotal, rushYdsTotal, recYdsTotal, receptionsTotal)
        }
        return null
    }

    private fun saveNewLeague(league: LeagueSettings) {
        rankings.getUserLeagues()!!.insertCurrentLeague(league, this)
        initLeagues()
        rankingsUpdated = true
    }

    private fun deleteLeague(league: LeagueSettings?) {
        rankings.getUserLeagues()!!.deleteLeague(league!!, this)
        initializeLeagueSpinner()
        displayLeague(rankings.getLeagueSettings())
        generateTextOnlyFlashbar(this, "Success!", league.name + " deleted", Flashbar.Gravity.BOTTOM)
                .show()
        rankingsUpdated = true
    }

    private fun updateLeague(newScoring: ScoringSettings?, newRoster: RosterSettings?, newFlex: Flex?,
                             newLeague: LeagueSettings?, oldLeague: LeagueSettings) {
        var shouldUpdateVBD = false
        var shouldUpdateProjections = false
        if (newScoring != null) {
            shouldUpdateVBD = true
            shouldUpdateProjections = true
            oldLeague.scoringSettings = newScoring
        }
        if (newRoster != null) {
            shouldUpdateVBD = true
            oldLeague.rosterSettings = newRoster
        }
        if (newFlex != null) {
            oldLeague.rosterSettings.flex = newFlex
        }
        if (newLeague != null) {
            if (oldLeague.isAuction != newLeague.isAuction || oldLeague.auctionBudget != newLeague.auctionBudget) {
                rankingsUpdated = true
            }
            if (oldLeague.teamCount != newLeague.teamCount) {
                shouldUpdateVBD = true
            }
            oldLeague.teamCount = newLeague.teamCount
            oldLeague.auctionBudget = newLeague.auctionBudget
            oldLeague.name = newLeague.name
            oldLeague.isSnake = newLeague.isSnake
            oldLeague.isAuction = newLeague.isAuction
            oldLeague.isBestBall = newLeague.isBestBall
            oldLeague.isDynasty = newLeague.isDynasty
            oldLeague.isRookie = newLeague.isRookie
        }
        rankings.getUserLeagues()!!.updateCurrentLeague(oldLeague, this)
        initLeagues()
        if (shouldUpdateVBD && rankings.players.isNotEmpty()) {
            Log.d(tag, "Updating vbd, based on league updates")
            var updateProjections = false
            if (shouldUpdateProjections) {
                Log.d(tag, "Projections to be updated, too.")
                updateProjections = true
                rankingsUpdated = true
            }
            rankings.updateProjectionsAndVBD(this, oldLeague, updateProjections, rankingsDB)
        }
    }
}