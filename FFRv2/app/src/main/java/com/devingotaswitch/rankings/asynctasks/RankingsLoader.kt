package com.devingotaswitch.rankings.asynctasks

import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.devingotaswitch.fileio.LocalSettingsHelper
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.rankings.domain.DailyProjection
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.Team
import com.devingotaswitch.utils.GeneralUtils.getLatency
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RankingsLoader {

    class RanksLoader(activity: RankingsHome, private val rankingsDB: RankingsDBWrapper) : AsyncTask<Any?, String?, Rankings?>() {
        private val pdia: AlertDialog = MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle("Please wait")
                .setMessage("Loading the rankings...")
                .create()
        private val act: RankingsHome = activity
        private var start: Long = 0
        override fun onPreExecute() {
            super.onPreExecute()
            pdia.show()
        }

        override fun onPostExecute(result: Rankings?) {
            super.onPostExecute(result)
            pdia.dismiss()
            Log.d(TAG, getLatency(start).toString() + " to load from file")
            act.processNewRankings(result, false)
        }

        override fun doInBackground(vararg data: Any?): Rankings? {
            start = System.currentTimeMillis()
            val userLeagues = rankingsDB.getLeagues(act)
            val players = rankingsDB.getPlayers(act)
            val teams = rankingsDB.getTeams(act)
            val currentLeague = userLeagues.currentLeague
            val orderedIds = rankingsDB.getPlayersSorted(act, currentLeague)
            val draft = LocalSettingsHelper.loadDraft(act, currentLeague.teamCount, currentLeague.auctionBudget,
                    currentLeague.name, players)
            val playerProjectionHistory: MutableMap<String, MutableList<DailyProjection>> =
                    rankingsDB.getPlayerProjectionHistory(act)
            return Rankings.init(teams, players, orderedIds, userLeagues, draft, playerProjectionHistory)
        }
    }

    class LeaguesLoader(activity: RankingsHome, private val rankingsDB: RankingsDBWrapper) : AsyncTask<Any?, String?, Rankings?>() {
        private val pdia: AlertDialog = MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle("Please wait")
                .setMessage("Loading the leagues...")
                .create()
        private val act: RankingsHome = activity
        private var start: Long = 0
        override fun onPreExecute() {
            super.onPreExecute()
            pdia.show()
        }

        override fun onPostExecute(result: Rankings?) {
            super.onPostExecute(result)
            pdia.dismiss()
            Log.d(TAG, getLatency(start).toString() + " to load from file")
            act.setLeague(result)
        }

        override fun doInBackground(vararg data: Any?): Rankings? {
            start = System.currentTimeMillis()
            val userLeagues = rankingsDB.getLeagues(act)
            return Rankings.initWithDefaults(userLeagues)
        }
    }

    class RanksSaver(activity: RankingsHome, private val rankingsDB: RankingsDBWrapper) : AsyncTask<Any?, String?, Void?>() {
        private val pdia: AlertDialog = MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle("Please wait")
                .setMessage("Saving the rankings...")
                .create()
        private val act: RankingsHome = activity
        private var start: Long = 0
        override fun onPreExecute() {
            super.onPreExecute()
            pdia.show()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            pdia.dismiss()
            Log.d(TAG, getLatency(start).toString() + " to save to file")
        }

        override fun doInBackground(vararg data: Any?): Void? {
            start = System.currentTimeMillis()
            val players = data[0] as Map<String, Player?>
            val teams = data[1] as Map<String, Team?>
            rankingsDB.saveTeams(act, teams.values)
            rankingsDB.savePlayers(act, players.values)
            return null
        }
    }

    companion object {
        private const val TAG = "RankingsLoader"
    }
}