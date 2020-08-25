package com.devingotaswitch.rankings.sources

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
    private const val TAG = "ParseStats"
    @Throws(IOException::class)
    fun setStats(rankings: Rankings) {
        // Fetch the stats
        val qbs = parseQBStats()
        val rbs = parseRBStats()
        val wrs = parseWRStats()
        val tes = parseTEStats()
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            when (player.position) {
                Constants.QB -> applyStats(qbs, player)
                Constants.RB -> applyStats(rbs, player)
                Constants.WR -> applyStats(wrs, player)
                Constants.TE -> applyStats(tes, player)
            }
        }

        // Now, do a second pass, only looking at players who have no stats
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (StringUtils.isBlank(player.stats)) {
                when (player.position) {
                    Constants.QB -> applyStatsChangedTeam(qbs, player)
                    Constants.RB -> applyStatsChangedTeam(rbs, player)
                    Constants.WR -> applyStatsChangedTeam(wrs, player)
                    Constants.TE -> applyStatsChangedTeam(tes, player)
                }
            }
        }
    }

    private fun applyStatsChangedTeam(statsMap: Map<String, String?>, player: Player) {
        val oneInitial = getNameFirstInitial(player)
        val twoInitials = getNameFirstTwoLetters(player)
        for (key in statsMap.keys) {
            if ((key.startsWith(oneInitial) || key.startsWith(twoInitials))
                    && key.endsWith(player.position)) {
                player.stats = statsMap[key]
            }
        }
    }

    private fun applyStats(statsMap: Map<String, String?>, player: Player) {
        if (statsMap.containsKey(getUniqueIdFirstInitial(player))) {
            player.stats = statsMap[getUniqueIdFirstInitial(player)]
        } else if (statsMap.containsKey(getUniqueIdFirstTwoLetters(player))) {
            player.stats = statsMap[getUniqueIdFirstTwoLetters(player)]
        }
    }

    private fun getPlayerIdKey(name: String?, team: String?, pos: String): String {
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos
    }

    private fun getUniqueIdFirstInitial(player: Player): String {
        return getPlayerIdKey(getNameFirstInitial(player), player.teamName, player.position)
    }

    private fun getNameFirstInitial(player: Player): String {
        val name = player.name.split(" ")
        return if (name[0].contains(".")) {
            player.name.replace(" ", "")
        } else {
            name[0][0].toString() + "." + name[1]
        }
    }

    private fun getUniqueIdFirstTwoLetters(player: Player): String {
        return getPlayerIdKey(getNameFirstTwoLetters(player), player.teamName, player.position)
    }

    private fun getNameFirstTwoLetters(player: Player): String {
        val name = player.name.split(" ")
        return name[0].substring(0, 2) + "." + name[1]
    }

    @Throws(IOException::class)
    private fun parseQBStats(): Map<String, String?> {
        val rows = parseURLWithUA(
                "http://www.footballoutsiders.com/stats/qb/" + Constants.LAST_YEAR_KEY, "tr")
        val qbPlayers: MutableMap<String, String?> = HashMap()
        for (i in rows.indices) {
            val player = rows[i].split(" ")
            val data = StringBuilder(500)
            // Name
            val name = normalizeNames(player[0])
            val team = normalizeTeams(player[1])
            if (name == "AJ" && team == "McCarron") {
                // handle the bullshit convention they broke with AJ McCarron. If this is resolved, remove this code.
                continue
            }
            if (player[0] == "Player" || !qbPlayers.containsKey(getPlayerIdKey(name, team, Constants.QB)) && player.size < 17) {
                continue
            }
            if (qbPlayers.containsKey(getPlayerIdKey(name, team, Constants.QB))) {
                val yards = player[player.size - 4]
                val effectiveYards = player[player.size - 3]
                val tds = player[player.size - 2]
                var normal = qbPlayers[getPlayerIdKey(name, team, Constants.QB)]
                normal += """

                    Rushing Yards: $yards${Constants.LINE_BREAK}
                    """.trimIndent()
                normal += "Adjusted Rushing Yards: " + effectiveYards + Constants.LINE_BREAK
                normal += "Rushing Touchdowns: $tds"
                qbPlayers[getPlayerIdKey(name, team, Constants.QB)] = normal
            } else {
                val attempts = player[player.size - 10]
                val attemptsNum = attempts.toInt()
                val completionPercentage = player[player.size - 3]
                val completionInt = completionPercentage.substring(0, completionPercentage.length - 1).toDouble()
                val completionRate = completionInt / 100.0
                val completionsIsh = (attemptsNum * completionRate).roundToInt()
                data.append("Pass Attempts: ").append(attempts).append(Constants.LINE_BREAK)
                data.append("Completions: ").append(completionsIsh).append(Constants.LINE_BREAK)
                data.append("Completion Percentage: ").append(completionPercentage).append(Constants.LINE_BREAK)
                data.append("Yards: ").append(player[player.size - 9].replace(",", "")).append(Constants.LINE_BREAK)
                data.append("Adjusted Yards: ").append(player[player.size - 8].replace(",", "")).append(Constants.LINE_BREAK)
                data.append("Touchdowns: ").append(player[player.size - 7]).append(Constants.LINE_BREAK)
                data.append("Interceptions: ").append(player[player.size - 4]).append(Constants.LINE_BREAK)
                data.append("Fumbles: ").append(player[player.size - 5])
                        .append(Constants.LINE_BREAK)
                data.append("DPI: ").append(player[player.size - 2]).append(Constants.LINE_BREAK)
                data.append("ALEX: ").append(player[player.size - 1]).append(Constants.LINE_BREAK)
                if (player.size > 17) {
                    data.append("DYAR: ")
                            .append(player[player.size - 19]).append(Constants.LINE_BREAK)
                    data.append("DVOA: ")
                            .append(player[player.size - 15])
                } else {
                    data.append("DYAR: ")
                            .append(player[player.size - 15]).append(Constants.LINE_BREAK)
                    data.append("DVOA: ")
                            .append(player[player.size - 13])
                }
                qbPlayers[getPlayerIdKey(name, team, Constants.QB)] = data.toString()
            }
        }
        return qbPlayers
    }

    @Throws(IOException::class)
    private fun parseRBStats(): Map<String, String?> {
        val rows = parseURLWithUA(
                "http://www.footballoutsiders.com/stats/rb/" + Constants.LAST_YEAR_KEY, "tr")
        val rbPlayers: MutableMap<String, String?> = HashMap()
        for (i in rows.indices) {
            val player = rows[i].split(" ")
            val data = StringBuilder(500)
            if (player[0] == "Player") {
                continue
            }
            var name = normalizeNames(player[0])
            val team = normalizeTeams(player[1])
            if (name!!.split(" ").size == 3) {
                name = name.split(" ")[0] + " " + name.split(" ")[2]
            }
            if (rbPlayers.containsKey(getPlayerIdKey(name, team, Constants.RB))) {
                val catches = player[player.size - 6]
                val yards = player[player.size - 5]
                val effectiveYards = player[player.size - 4]
                val tds = player[player.size - 3]
                val catchRate = player[player.size - 2]
                val receptionInt =
                        catchRate.substring(0, catchRate.length - 1).toDouble()
                val receptionRate = receptionInt / 100.0
                val receptionsIsh = (catches.toInt() * receptionRate).roundToInt()
                val normal = rbPlayers[getPlayerIdKey(name, team, Constants.RB)] + Constants.LINE_BREAK +
                        "Targets: " + catches + Constants.LINE_BREAK +
                        "Receptions: " + receptionsIsh + Constants.LINE_BREAK +
                        "Catch Rate: " + catchRate + Constants.LINE_BREAK +
                        "Receiving Yards: " + yards + Constants.LINE_BREAK +
                        "Adjusted Receiving Yards: " + effectiveYards + Constants.LINE_BREAK +
                        "Receiving Touchdowns: " + tds
                rbPlayers[getPlayerIdKey(name, team, Constants.RB)] = normal
            } else {
                var incr = 1
                if (player[player.size - 2].contains("%")) {
                    incr = -1
                }
                data.append("Carries: ").append(player[player.size - 6 + incr]).append(Constants.LINE_BREAK)
                data.append("Yards: ").append(player[player.size - 5 + incr].replace(",", "")).append(Constants.LINE_BREAK)
                data.append("Adjusted Yards: ").append(player[player.size - 4 + incr].replace(",", "")).append(Constants.LINE_BREAK)
                data.append("Touchdowns: ").append(player[player.size - 3 + incr]).append(Constants.LINE_BREAK)
                data.append("Fumbles: ").append(player[player.size - 2 + incr])
                        .append(Constants.LINE_BREAK)
                if (player.size > 12) {
                    data.append("DYAR: ")
                            .append(player[player.size - 13 + incr]).append(Constants.LINE_BREAK)
                    data.append("DVOA: ")
                            .append(player[player.size - 9 + incr])
                } else {
                    data.append("DYAR: ")
                            .append(player[player.size - 10 + incr]).append(Constants.LINE_BREAK)
                    data.append("DVOA: ")
                            .append(player[player.size - 8 + incr])
                }
                rbPlayers[getPlayerIdKey(name, team, Constants.RB)] = data.toString()
            }
        }
        return rbPlayers
    }

    @Throws(IOException::class)
    private fun parseWRStats(): Map<String, String?> {
        val rows = parseURLWithUA(
                "http://www.footballoutsiders.com/stats/wr/" + Constants.LAST_YEAR_KEY, "tr")
        val wrPlayers: MutableMap<String, String?> = HashMap()
        for (i in rows.indices) {
            val player = rows[i].split(" ")
            val data = StringBuilder(500)
            if (player[0] == "Player") {
                continue
            }
            var name = normalizeNames(player[0])
            val team = normalizeTeams(player[1])
            if (name!!.split(" ").size == 3) {
                name = name.split(" ")[0] + " " + name.split(" ")[2]
            }
            if (!wrPlayers.containsKey(getPlayerIdKey(name, team, Constants.WR))
                    && player[6].contains("%") && player[8].contains("%")
                    && player.size < 15) {
                continue
            }
            if (wrPlayers.containsKey(getPlayerIdKey(name, team, Constants.WR))) {
                val rushes = player[player.size - 4]
                val yards = player[player.size - 3]
                val tds = player[player.size - 2]
                var normal = wrPlayers[getPlayerIdKey(name, team, Constants.WR)]
                normal += """

                    Rushes: $rushes${Constants.LINE_BREAK}
                    """.trimIndent()
                normal += "Rushing Yards: " + yards + Constants.LINE_BREAK
                normal += "Rushing Touchdowns: $tds"
                wrPlayers[getPlayerIdKey(name, team, Constants.WR)] = normal
            } else {
                val catchRateStr = player[player.size - 3]
                val catchRate = catchRateStr.substring(0, catchRateStr.length - 1).toDouble()
                val targets = player[player.size - 7].toInt()
                val catchesIsh = (targets * (catchRate / 100.0)).roundToInt()
                data.append("Targets: ").append(targets).append(Constants.LINE_BREAK)
                data.append("Receptions: ").append(catchesIsh).append(Constants.LINE_BREAK)
                data.append("Catch Rate: ").append(catchRateStr).append(Constants.LINE_BREAK)
                data.append("Yards: ").append(player[player.size - 6]).append(Constants.LINE_BREAK)
                data.append("Adjusted Yards: ").append(player[player.size - 5].replace(",", "")).append(Constants.LINE_BREAK)
                data.append("Touchdowns: ").append(player[player.size - 4]).append(Constants.LINE_BREAK)
                data.append("Fumbles: ")
                        .append(player[player.size - 2])
                        .append(Constants.LINE_BREAK)
                data.append("DPI: ").append(player[player.size - 1]).append(Constants.LINE_BREAK)
                if (player.size > 13) {
                    data.append("DYAR: ")
                            .append(player[player.size - 14]).append(Constants.LINE_BREAK)
                    data.append("DVOA: ")
                            .append(player[player.size - 10])
                } else {
                    data.append("DYAR: ")
                            .append(player[player.size - 11]).append(Constants.LINE_BREAK)
                    data.append("DVOA: ")
                            .append(player[player.size - 9])
                }
                wrPlayers[getPlayerIdKey(name, team, Constants.WR)] = data.toString()
            }
        }
        return wrPlayers
    }

    @Throws(IOException::class)
    private fun parseTEStats(): Map<String, String?> {
        val rows = parseURLWithUA(
                "http://www.footballoutsiders.com/stats/te/" + Constants.LAST_YEAR_KEY, "tr")
        val tePlayers: MutableMap<String, String?> = HashMap()
        for (i in rows.indices) {
            val player = rows[i].split(" ")
            val data = StringBuilder(500)
            if (player[0] == "Player") {
                continue
            }
            var name = normalizeNames(player[0])
            val team = normalizeTeams(player[1])
            if (name!!.split(" ").size == 3) {
                name = name.split(" ")[0] + " " + name.split(" ")[2]
            }
            if (!tePlayers.containsKey(getPlayerIdKey(name, team, Constants.TE))
                    && player[6].contains("%") && player[8].contains("%")
                    && player.size < 15) {
                continue
            }
            val catchRateStr = player[player.size - 3]
            val catchRate = catchRateStr.substring(0, catchRateStr.length - 1).toDouble()
            val targets = player[player.size - 7].toInt()
            val catchesIsh = (targets * (catchRate / 100.0)).roundToInt()
            data.append("Targets: ").append(targets).append(Constants.LINE_BREAK)
            data.append("Receptions: ").append(catchesIsh).append(Constants.LINE_BREAK)
            data.append("Catch Rate: ").append(catchRateStr).append(Constants.LINE_BREAK)
            data.append("Yards: ").append(player[player.size - 6]).append(Constants.LINE_BREAK)
            data.append("Adjusted Yards: ").append(player[player.size - 5].replace(",", "")).append(Constants.LINE_BREAK)
            data.append("Touchdowns: ").append(player[player.size - 4]).append(Constants.LINE_BREAK)
            data.append("Fumbles: ")
                    .append(player[player.size - 2])
                    .append(Constants.LINE_BREAK)
            data.append("DPI: ").append(player[player.size - 1]).append(Constants.LINE_BREAK)
            if (player.size > 13) {
                data.append("DYAR: ")
                        .append(player[player.size - 14]).append(Constants.LINE_BREAK)
                data.append("DVOA: ")
                        .append(player[player.size - 10])
            } else {
                data.append("DYAR: ")
                        .append(player[player.size - 11]).append(Constants.LINE_BREAK)
                data.append("DVOA: ")
                        .append(player[player.size - 9])
            }
            tePlayers[getPlayerIdKey(name, team, Constants.TE)] = data.toString()
        }
        return tePlayers
    }
}