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
import kotlin.collections.HashMap
import kotlin.math.roundToInt

object ParseStats {

    @Throws(IOException::class)
    fun setStats(rankings: Rankings) {
        // Fetch the stats
        val players: MutableMap<String, String?> = HashMap()
        val age: MutableMap<String, Int?> = HashMap()
        parsePassingStats(players, age)
        parseRushingStats(players, age)
        parseReceivingStats(players, age)
        parseKickingStats(players, age)
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (players.containsKey(player.uniqueId)) {
                player.stats = players[player.uniqueId]
            }
            // If age isn't set (FFTB is iffy for young players), try to set again
            if ((player.age == null || player.age == 0) && age.containsKey(player.uniqueId)) {
                player.age = age[player.uniqueId]
            }
        }

        // Now, do a second pass, only looking at players who have no stats
        // Players who changed teams will be marked by their old team.
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (StringUtils.isBlank(player.stats)) {
                applyStatsChangedTeam(players, player)
            }
            // Also, make an additional attempt to set a player's age, for changed teams.
            if ((player.age == null || player.age == 0)) {
                applyAgeChangedTeam(age, player)
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

    private fun applyAgeChangedTeam(ageMap: Map<String, Int?>, player: Player) {
        for (key in ageMap.keys) {
            if (key.startsWith(player.name)
                    && key.endsWith(player.position)) {
                player.age = ageMap[key]
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
    private fun parsePassingStats(players: MutableMap<String, String?>, ageMap: MutableMap<String, Int?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/passing.htm",
        "table.stats_table tbody tr td")
        for (i in td.indices step 30) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", "").trim())
            val team = normalizeTeams(td[i+1])
            val age = td[i+2].toInt()
            // Some rows are missing a position. Proceed with those and QBs, ignore other positions.
            if (td[i+3].isEmpty() || td[i+3].toUpperCase() == Constants.QB) {
                var data = Constants.STATS_GAMES_STARTED + td[i + 5] + Constants.LINE_BREAK +
                        Constants.STATS_ATTEMPTS + td[i + 8] + Constants.LINE_BREAK +
                        Constants.STATS_COMPLETION_PERCENTAGE + td[i + 9] + "%" + Constants.LINE_BREAK +
                        Constants.STATS_PASSING_YARDS + td[i + 10] + Constants.LINE_BREAK +
                        Constants.STATS_PASSING_TDS + td[i + 11] + Constants.LINE_BREAK +
                        Constants.STATS_INTS + td[i + 13] + Constants.LINE_BREAK +
                        Constants.STATS_YPA + td[i + 17] + Constants.LINE_BREAK +
                        Constants.STATS_QBR + td[i + 21] + Constants.LINE_BREAK

                val inferredKey = getPlayerIdKey(name, team, Constants.QB)

                players[inferredKey] = data
                ageMap[inferredKey] = age
            }
        }
    }

    @Throws(IOException::class)
    private fun parseRushingStats(players: MutableMap<String, String?>, ageMap: MutableMap<String, Int?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/rushing.htm",
                "table.stats_table tbody tr td")
        for (i in td.indices step 14) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", "").trim())
            val team = normalizeTeams(td[i + 1])
            val age = td[i+2].toInt()
            // Some rows are missing a position. If so, infer the norm for the stat.
            val pos = if (td[i+3].isEmpty()) {
                Constants.RB
            } else {
                td[i+3].toUpperCase()
            }
            val inferredKey = getPlayerIdKey(name, team, pos)
            var localData = Constants.STATS_CARRIES + td[i+6] + Constants.LINE_BREAK +
                    Constants.STATS_RUSHING_YARDS + td[i+7] + Constants.LINE_BREAK +
                    Constants.STATS_RUSHING_TDS + td[i+8] + Constants.LINE_BREAK +
                    Constants.STATS_YPC + td[i+11] + Constants.LINE_BREAK +
                    Constants.STATS_FUMBLES + td[i+13] + Constants.LINE_BREAK

            // If we have data already, meaning it's a qb, don't re-add games.
            if (!players.containsKey(inferredKey)) {
                localData = Constants.STATS_GAMES_STARTED + td[i+5] + Constants.LINE_BREAK + localData
                players[inferredKey] = localData

            } else {
                players[inferredKey] = players[inferredKey] + localData
            }
            ageMap[inferredKey] = age

        }
    }

    @Throws(IOException::class)
    private fun parseReceivingStats(players: MutableMap<String, String?>, ageMap: MutableMap<String, Int?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/receiving.htm",
                "table.stats_table tbody tr td")
        for (i in td.indices step 18) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", "").trim())
            val team = normalizeTeams(td[i + 1])
            val age = td[i+2].toInt()
            val pos = if (td[i+3].isEmpty()) {
                Constants.WR
            } else {
                td[i+3].toUpperCase()
            }
            val inferredKey = getPlayerIdKey(name, team, pos)
            var localData = Constants.STATS_TARGETS + td[i+6] + Constants.LINE_BREAK +
                            Constants.STATS_RECEPTIONS + td[i+7] + Constants.LINE_BREAK +
                            Constants.STATS_CATCH_RATE + td[i+8] + Constants.LINE_BREAK +
                            Constants.STATS_RECEIVING_YARDS + td[i+9] + Constants.LINE_BREAK +
                            Constants.STATS_RECEIVING_TDS + td[i+11] + Constants.LINE_BREAK

            // Prepend games data if we don't have anything saved (meaning, wr or te)
            if (!players.containsKey(inferredKey)) {
                localData = Constants.STATS_GAMES_STARTED + td[i+5] + Constants.LINE_BREAK + localData
                players[inferredKey] = localData
            } else {
                // Otherwise, we'll check position. If it's a wr who just has rushing stats, we'll
                // prepend for readability. Otherwise, we'll append.
                val existingStats = players[inferredKey]
                if (pos == Constants.TE || pos == Constants.WR) {
                    players[inferredKey] = localData + existingStats
                } else {
                    players[inferredKey] = existingStats + localData
                }
            }
            ageMap[inferredKey] = age
        }
    }

    @Throws(IOException::class)
    private fun parseKickingStats(players: MutableMap<String, String?>, ageMap: MutableMap<String, Int?>) {
        val td = parseURLWithUA(
                "https://www.pro-football-reference.com/years/" + Constants.LAST_YEAR_KEY + "/kicking.htm",
                "table.stats_table tbody tr td")
        for (i in td.indices step 33) {
            val name = normalizeNames(td[i].replace("*", "").replace("+", "").trim())
            val team = normalizeTeams(td[i+1])
            val age = td[i+2].toInt()
            val data = Constants.STATS_FG_ATTEMPTED + td[i+16] + Constants.LINE_BREAK +
                    Constants.STATS_FG_MADE + td[i+17] + Constants.LINE_BREAK +
                    Constants.STATS_XP_ATTEMPTED + td[i+20] + Constants.LINE_BREAK +
                    Constants.STATS_XP_MADE + td[i+21] + Constants.LINE_BREAK
            val inferredKey = getPlayerIdKey(name, team, Constants.K)
            players[inferredKey] = data
            ageMap[inferredKey] = age
        }
    }
}