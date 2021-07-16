package com.devingotaswitch.rankings.sources

import android.util.Log
import com.amazonaws.util.StringUtils
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

object ParseStats {

    @Throws(IOException::class)
    fun setStats(rankings: Rankings) {
        // Fetch the stats
        val players: MutableMap<String, String?> = HashMap()
        parsePassingStats(players)
        parseRushingStats(players)
        parseReceivingStats(players)
        parseKickingStats(players)
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (players.containsKey(player.uniqueId)) {
                player.stats = players[player.uniqueId]
            }
        }

        // Now, do a second pass, only looking at players who have no stats
        // Players who changed teams will be marked by their old team.
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (StringUtils.isBlank(player.stats)) {
                applyStatsChangedTeam(players, player)
            }
        }
    }

    private fun applyStatsChangedTeam(statsMap: Map<String, String?>, player: Player) {
        for (key in statsMap.keys) {
            if (key.startsWith(player.name)
                    && key.endsWith(player.position)) {
                player.stats = statsMap[key]
            }
        }
    }

    private fun getPlayerIdKey(name: String?, team: String?, pos: String?): String {
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos
    }

    @Throws(IOException::class)
    private fun parsePassingStats(players: MutableMap<String, String?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/passing.htm",
        "table.stats_table tbody tr td")
        for (i in td.indices step 30) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", ""))
            val team = normalizeTeams(td[i+1])
            val pos = td[i+3].toUpperCase()
            val data = "Games Started: " + td[i+5] + Constants.LINE_BREAK +
                    "Pass Attempts: " + td[i+8] + Constants.LINE_BREAK +
                    "Completion Percentage: " + td[i+9] + Constants.LINE_BREAK +
                    "Passing Yards: " + td[i+10] + Constants.LINE_BREAK +
                    "Passing Touchdowns: " + td[i+11] + Constants.LINE_BREAK +
                    "Interceptions: " + td[i+13] + Constants.LINE_BREAK +
                    "Yards Per Attempt: " + td[i+17] + Constants.LINE_BREAK +
                    "QB Rating: " + td[i+21] + Constants.LINE_BREAK
            players[getPlayerIdKey(name, team, pos)] = data
        }
    }

    @Throws(IOException::class)
    private fun parseRushingStats(players: MutableMap<String, String?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/rushing.htm",
                "table.stats_table tbody tr td")
        for (i in td.indices step 14) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", ""))
            val team = normalizeTeams(td[i + 1])
            val pos = td[i + 3].toUpperCase()
            var localData = "Carries: " + td[i+6] + Constants.LINE_BREAK +
                    "Rushing Yards: " + td[i+7] + Constants.LINE_BREAK +
                    "Rushing Touchdowns: " + td[i+8] + Constants.LINE_BREAK +
                    "Yards Per Carry: " + td[i+11] + Constants.LINE_BREAK +
                    "Fumbles: " + td[i+13] + Constants.LINE_BREAK

            // If we have data already, meaning it's a qb, don't re-add games.
            if (!players.containsKey(getPlayerIdKey(name, team, pos))) {
                localData = "Games Started: " + td[i+5] + Constants.LINE_BREAK + localData
                players[getPlayerIdKey(name, team, pos)] = localData

            } else {
                players[getPlayerIdKey(name, team, pos)] = players[getPlayerIdKey(name, team, pos)] + localData
            }

        }
    }

    @Throws(IOException::class)
    private fun parseReceivingStats(players: MutableMap<String, String?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/receiving.htm",
                "table.stats_table tbody tr td")
        for (i in td.indices step 18) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", ""))
            val team = normalizeTeams(td[i + 1])
            val pos = td[i + 3].toUpperCase()
            var localData = "Targets: " + td[i+6] + Constants.LINE_BREAK +
                            "Receptions: " + td[i+7] + Constants.LINE_BREAK +
                            "Catch Rate: " + td[i+8] + Constants.LINE_BREAK +
                            "Receiving Yards: " + td[i+9] + Constants.LINE_BREAK +
                            "Receiving Touchdowns: " + td[i+11] + Constants.LINE_BREAK
            // Prepend games data if we don't have anything saved (meaning, wr or te)
            if (!players.containsKey(getPlayerIdKey(name, team, pos))) {
                localData = "Games Started: " + td[i+5] + Constants.LINE_BREAK + localData
                players[getPlayerIdKey(name, team, pos)] = localData
            } else {
                // Otherwise, we'll check position. If it's a wr who just has rushing stats, we'll
                // prepend for readability. Otherwise, we'll append.
                val existingStats = players[getPlayerIdKey(name, team, pos)]
                if (pos == Constants.TE || pos == Constants.WR) {
                    players[getPlayerIdKey(name, team, pos)] = localData + existingStats
                } else {
                    players[getPlayerIdKey(name, team, pos)] = existingStats + localData
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun parseKickingStats(players: MutableMap<String, String?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/kicking.htm",
                "table.stats_table tbody tr td")
        for (i in td.indices step 33) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", ""))
            val team = normalizeTeams(td[i+1])
            val data = "FG Attempted: " + td[i+16] + Constants.LINE_BREAK +
                    "FG Percentage: " + td[i+19] + Constants.LINE_BREAK +
                    "XP Attempted: " + td[i+20] + Constants.LINE_BREAK +
                    "XP Percentage: " + td[i+22] + Constants.LINE_BREAK
            players[getPlayerIdKey(name, team, Constants.K)] = data
        }
    }
}