package com.devingotaswitch.rankings.asynctasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.devingotaswitch.fileio.LocalSettingsHelper
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.rankings.domain.DailyProjection
import com.devingotaswitch.rankings.domain.LeagueSettings
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.sources.*
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils.getLatency
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class RankingsFetcher {

    class VBDUpdater(private val rankings: Rankings, private val activity: Activity, private val league: LeagueSettings, private val updateProjections: Boolean,
                     private val rankingsDB: RankingsDBWrapper) : AsyncTask<Any?, Void?, Void?>() {

        override fun doInBackground(vararg data: Any?): Void? {
            if (updateProjections) {
                Log.i(TAG, "Updating projections")
                try {
                    for (player in rankings.players.values) {
                        player.updateProjection(league.scoringSettings)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update player projections", e)
                }
            }
            Log.i(TAG, "Getting paa calculations")
            try {
                ParseMath.setPlayerPAA(rankings, league)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate PAA", e)
            }
            Log.i(TAG, "Getting VoRP calculations")
            try {
                ParseMath.setPlayerVoLS(rankings, league)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate VoRP", e)
            }
            Log.i(TAG, "Setting player xvals")
            try {
                ParseMath.setPlayerXval(rankings, league)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate Xval", e)
            }
            rankingsDB.savePlayers(activity, rankings.players.values)
            return null
        }
    }

    class RanksAggregator(activity: RankingsHome, private val rankings: Rankings) : AsyncTask<Any?, String?, Rankings?>() {
        private val pdia: AlertDialog = MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle("Please wait")
                .setMessage("Fetching the rankings...")
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
            Log.d(TAG, getLatency(start).toString() + " seconds to fetch rankings")
            act.processNewRankings(result, true)
        }

        private fun dedupPlayers() {
            // Get players with at least one value set (meaning, probably real) and those with none (meaning, possibly have an old team).
            val realPlayers: MutableMap<String, Player?> = HashMap()
            val possiblyFake: MutableSet<Player> = HashSet()
            for (key in rankings.players.keys) {
                val player = rankings.getPlayer(key)
                if (player.projection == 0.0 && player.age == null && player.experience == -1) {
                    possiblyFake.add(player)
                } else {
                    realPlayers[getDedupKey(player)] = player
                }
            }

            // Find matches between the two. Delete 'fake' players, and apply auction values over.
            for (player in possiblyFake) {
                if (realPlayers.containsKey(getDedupKey(player))) {
                    rankings.dedupPlayer(player, realPlayers[getDedupKey(player)]!!)
                }
            }
        }

        private fun getDedupKey(player: Player): String {
            return player.name + Constants.PLAYER_ID_DELIMITER + player.position
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            pdia.setMessage(values[0])
        }

            override fun doInBackground(vararg data: Any?): Rankings? {
            start = System.currentTimeMillis()
            rankings.clearRankings()
            Log.i(TAG, "Getting WF rankings")
            try {
                ParseWalterFootball.wfRankings(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse WF", e)
            }
            publishProgress("Fetching rankings... 1/14")
            Log.i(TAG, "Getting Yahoo rankings")
            try {
                ParseYahoo.parseYahooWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Yahoo", e)
            }
            publishProgress("Fetching rankings... 6/14")
            Log.i(TAG, "Getting NFL rankings")
            try {
                ParseNFL.parseNFLAAVWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse NFL", e)
            }
            publishProgress("Fetching rankings... 8/14")
            Log.i(TAG, "Getting Draft Wizard rankings")
            try {
                ParseDraftWizard.parseRanksWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Draft Wizard", e)
            }
            publishProgress("Fetching rankings... 10/14")
            Log.i(TAG, "Getting FFTB rankings")
            try {
                ParseFFTB.parseFFTBRankingsWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse FFTB", e)
            }
            publishProgress("Fetching rankings... 4/14")
            Log.i(TAG, "Getting projections")
            publishProgress("Getting projections...")
            try {
                ParseProjections.projPointsWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse projections", e)
            }
            publishProgress("Getting consensus rankings...")
            Log.i(TAG, "Getting ECR rankings")
            try {
                ParseFantasyPros.parseECRWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse ECR/risk", e)
            }
            Log.i(TAG, "Getting ADP rankings")
            try {
                ParseFantasyPros.parseADPWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse ADP", e)
            }
            Log.i(TAG, "Getting Dynasty rankings")
            try {
                ParseFantasyPros.parseDynastyWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse dynasty ranks", e)
            }
            Log.i(TAG, "Getting rookie rankings")
            try {
                ParseRookieRanks.parseRookieWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse rookie ranks", e)
            }
            Log.i(TAG, "Getting best ball rankings")
            try {
                ParseBestBallRanks.parseBestBallWrapper(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse best ball ranks", e)
            }
            Log.i(TAG, "Cleaning up duplicate players")
            dedupPlayers()
            Log.i(TAG, "Getting auction values from adp/ecr")
            var adpSet = false
            var ecrSet = false
            for (player in rankings.players.values) {
                if (player.adp < 300.0) {
                    adpSet = true
                }
                if (player.ecr < 300.0) {
                    ecrSet = true
                }
                if (adpSet && ecrSet) {
                    break
                }
            }
            if (adpSet) {
                ParseMath.getADPAuctionValue(rankings)
            } else {
                Log.d(TAG, "Not setting ADP auction values, ADP not set.")
            }
            if (ecrSet) {
                ParseMath.getECRAuctionValue(rankings)
            } else {
                Log.d(TAG, "Not setting ECR auction values, ECR not set.")
            }
            publishProgress("Fetching rankings... 12/14")
            Log.i(TAG, "Getting paa calculations")
            publishProgress("Calculating PAA...")
            try {
                ParseMath.setPlayerPAA(rankings, rankings.getLeagueSettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate PAA", e)
            }
            Log.i(TAG, "Getting VoRP calculations")
            publishProgress("Calculating VoRP...")
            try {
                ParseMath.setPlayerVoLS(rankings, rankings.getLeagueSettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate VoRP", e)
            }
            Log.i(TAG, "Setting player xvals")
            publishProgress("Calculating xVal...")
            try {
                ParseMath.setPlayerXval(rankings, rankings.getLeagueSettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to calculate Xval", e)
            }
            Log.i(TAG, "Getting PAA rankings")
            var projSet = false
            for (player in rankings.players.values) {
                if (player.projection > 0.0) {
                    projSet = true
                    break
                }
            }
            if (projSet) {
                ParseMath.getPAAAuctionValue(rankings)
            } else {
                Log.d(TAG, "Not setting PAA auction values, no projections are set.")
            }
            publishProgress("Fetching rankings... 14/14")
            Log.i(TAG, "Getting positional sos")
            publishProgress("Getting positional SOS...")
            try {
                ParseSOS.getSOS(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse SOS", e)
            }
            Log.i(TAG, "Getting injury statuses")
            publishProgress("Getting injury status...")
            try {
                ParseInjuries.parsePlayerInjuries(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse player injuries", e)
            }
            Log.i(TAG, "Getting player stats")
            publishProgress("Getting last year's stats...")
            try {
                ParseStats.setStats(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get player stats", e)
            }
            Log.i(TAG, "Getting draft info")
            publishProgress("Getting draft information...")
            try {
                ParseDraft.parseTeamDraft(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get draft info", e)
            }
            Log.i(TAG, "Getting free agency info")
            publishProgress("Getting free agency classes...")
            try {
                ParseFA.parseFAClasses(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FA info", e)
            }
            Log.i(TAG, "Getting team schedules")
            publishProgress("Getting team schedules...")
            try {
                ParseFantasyPros.parseSchedule(rankings)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get schedules", e)
            }
            Log.i(TAG, "Ordering players for display")
            rankings.orderedIds = rankings.orderPlayersByLeagueType(rankings.players.values)

            // If there were no projection histories saved against the rankings, we'll set them here.
            val today = Constants.DATE_FORMAT.format(Calendar.getInstance().time)
            if (rankings.playerProjectionHistory.isEmpty()) {
                for (player in rankings.players.values) {
                    val proj = DailyProjection()
                    proj.playerKey = player.uniqueId
                    proj.playerProjection = player.playerProjection
                    proj.date = today
                    val projections: MutableList<DailyProjection> = ArrayList()
                    projections.add(proj)
                    rankings.playerProjectionHistory[player.uniqueId] = projections
                }
            } else {
                // If there's no projection history saved for today, set it here
                val topPlayerKey = rankings.orderedIds[0]
                var isNewDay = true
                for (proj in rankings.playerProjectionHistory[topPlayerKey]!!) {
                    if (proj.date == today) {
                        isNewDay = false
                        break
                    }
                }
                if (isNewDay) {
                    for (player in rankings.players.values) {
                        val proj = DailyProjection()
                        proj.playerKey = player.uniqueId
                        proj.playerProjection = player.playerProjection
                        proj.date = today
                        if (rankings.playerProjectionHistory.containsKey(player.uniqueId)) {
                            rankings.playerProjectionHistory[player.uniqueId]!!.add(proj)
                        } else {
                            val newProj: MutableList<DailyProjection> = ArrayList()
                            newProj.add(proj)
                            rankings.playerProjectionHistory[player.uniqueId] = newProj
                        }
                    }
                }
            }
            LocalSettingsHelper.saveLastRankingsSavedDate(act)
            return rankings
        }
    }

    companion object {
        private const val TAG = "RankingsFetcher"
    }
}