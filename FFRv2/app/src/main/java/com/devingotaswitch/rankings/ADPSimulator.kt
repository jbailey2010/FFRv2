package com.devingotaswitch.rankings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.andrognito.flashbar.Flashbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.getPlayerIdFromSearchView
import com.devingotaswitch.utils.GeneralUtils.getPlayerSearchAdapter
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.utils.GeneralUtils.isInteger
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ADPSimulator : AppCompatActivity() {
    private var rankings: Rankings? = null
    private var playerToSearch: Player? = null
    private var searchInput: AutoCompleteTextView? = null
    private var roundInput: EditText? = null
    private var pickInput: EditText? = null
    private var result: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adp_simulator)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankings = Rankings.init()
        val toolbar = findViewById<Toolbar>(R.id.toolbar_adp_simulator)
        toolbar.title = ""
        val mainTitle = findViewById<TextView>(R.id.main_toolbar_title)
        mainTitle.text = "ADP Simulator"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val act: Activity = this
        toolbar.setNavigationOnClickListener { v: View? ->
            hideKeyboard(act)
            onBackPressed()
        }
    }

    public override fun onResume() {
        super.onResume()
        try {
            init()
            if (intent.hasExtra(Constants.PLAYER_ID)) {
                setPlayerToBeChecked(intent.getStringExtra(Constants.PLAYER_ID)!!)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            hideKeyboard(this)
            onBackPressed()
        }
    }

    private fun init() {
        searchInput = findViewById(R.id.adp_player_selection)
        searchInput!!.setAdapter(null)
        val mAdapter: FilterWithSpaceAdapter<*> = getPlayerSearchAdapter(rankings!!, this, hideDrafted = false, hideRankless = false)
        searchInput!!.setAdapter(mAdapter)
        searchInput!!.setOnItemClickListener { _: AdapterView<*>?, view: View?, _: Int, _: Long -> setPlayerToBeChecked(getPlayerIdFromSearchView(view!!)) }
        result = findViewById(R.id.adp_output_view)
        roundInput = findViewById(R.id.adp_pick_round)
        pickInput = findViewById(R.id.adp_pick_in_round)
        val submit = findViewById<Button>(R.id.adp_submit_button)
        val act: Activity = this
        submit.setOnClickListener { v: View? ->
            if (playerToSearch == null) {
                generateTextOnlyFlashbar(act, "No can do", "Invalid player, use the dropdown to pick", Flashbar.Gravity.TOP)
                        .show()
                return@setOnClickListener
            }
            val roundStr = roundInput!!.text.toString()
            val pickStr = pickInput!!.text.toString()
            if (!isInteger(roundStr) || !isInteger(pickStr)) {
                generateTextOnlyFlashbar(act, "No can do", "Pick/round must be provided as numbers", Flashbar.Gravity.TOP)
                        .show()
                return@setOnClickListener
            }
            val round = roundStr.toInt()
            val pick = pickStr.toInt()
            if (pick > rankings!!.leagueSettings.teamCount) {
                generateTextOnlyFlashbar(act, "No can do", "Pick can't be higher than current league team count", Flashbar.Gravity.TOP)
                        .show()
                return@setOnClickListener
            }
            val overallPick = (round - 1) * rankings!!.leagueSettings.teamCount + pick
            hideKeyboard(act)
            getADPOddsForInput(overallPick)
        }
    }

    private fun getADPOddsForInput(pick: Int) {
        val oddsParser = ParseADPOdds(rankings, this)
        oddsParser.execute(pick, playerToSearch)
    }

    private fun setPlayerToBeChecked(id: String) {
        playerToSearch = rankings!!.getPlayer(id)
        searchInput!!.setText(playerToSearch!!.name + ": " + playerToSearch!!.position + ", " + playerToSearch!!.teamName)
    }

    private fun displayResult(output: String) {
        result!!.text = output
        val localCopy: Activity = this
        val playerId = playerToSearch!!.uniqueId
        result!!.setOnLongClickListener {
            val intent = Intent(localCopy, PlayerInfo::class.java)
            intent.putExtra(Constants.PLAYER_ID, playerId)
            startActivity(intent)
            true
        }
        clearInputs()
    }

    private fun clearInputs() {
        playerToSearch = null
        searchInput!!.setText("")
        pickInput!!.setText("")
        roundInput!!.setText("")
    }

    private fun getPlayerADPOdds(url: String, player: Player, pick: Int): String {
        try {
            val td = parseURLWithUA(url,
                    "table.table td")
            var i = 0
            while (i < td.size) {
                val possibleName = normalizeNames(td[i])
                val possiblePos = td[i + 1]
                val possibleTeam = normalizeTeams(td[i + 2])
                if (possiblePos == player.position && possibleTeam == player.teamName &&
                        (possibleName == player.name.replace("\\.".toRegex(), "") || possibleName == player.name)) {
                    return ("Odds " + player.name + " is available at pick " + pick
                            + ": " + td[i + 4])
                }
                i += 6
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get player adp likelihood", e)
        }
        return "An error occurred. Either the data is unavailable, or the internet may have dropped."
    }

    companion object {
        private const val TAG = "ADPSimulator"

        private class ParseADPOdds(private val rankings: Rankings?,
                                   private val adpSimulator: ADPSimulator) : AsyncTask<Any?, Void?, String>() {
            private val pdia: AlertDialog = MaterialAlertDialogBuilder(adpSimulator)
                    .setCancelable(false)
                    .setTitle("Please wait")
                    .setMessage("Doing fancy math...")
                    .create()

            override fun onPreExecute() {
                super.onPreExecute()
                pdia.show()
            }
    
            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                pdia.dismiss()
                adpSimulator.displayResult(result)
            }

            override fun doInBackground(vararg data: Any?): String {
                val pick = data[0] as Int
                val player = data[1] as Player
                var type = "standard"
                val scoring = rankings!!.leagueSettings.scoringSettings
                val roster = rankings.leagueSettings.rosterSettings
                if (roster.qbCount > 1 || roster.flex != null && roster.qbCount > 0 && roster.flex!!.qbrbwrteCount > 0) {
                    type = "2qb"
                } else if (scoring.receptions > 0.0) {
                    type = "ppr"
                }
                var teams = "8"
                val numTeams = rankings.leagueSettings.teamCount
                if (numTeams >= 14 && "2qb" != type) {
                    teams = "14"
                } else if (numTeams >= 12) {
                    teams = "12"
                } else if (numTeams >= 10) {
                    teams = "10"
                }
                val url = ("https://fantasyfootballcalculator.com/scenario-calculator?format="
                        + type + "&num_teams=" + teams + "&draft_pick=" + pick)
                Log.d(TAG, url)
                ParsingUtils.init()
                var first = adpSimulator.getPlayerADPOdds(url, player, pick)
                if (scoring.receptions > 0.0 && first.contains("error") && url.contains("ppr")) {
                    first = adpSimulator.getPlayerADPOdds(url.replace("ppr", "standard"), player, pick)
                }
                return first
            }
        }
    }
}