package com.devingotaswitch.rankings.sources

import android.util.Log
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils.isInteger
import com.devingotaswitch.utils.JsoupUtils.getDocument
import com.devingotaswitch.utils.JsoupUtils.getElemsFromDoc
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.normalizeDefenses
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException
import java.util.*

object ParseFantasyPros {
    private const val TAG = "ParseFantasyPros"
    @Throws(IOException::class)
    fun parseECRWrapper(rankings: Rankings) {
        val ecr: MutableMap<String, Double?> = HashMap()
        var url = "http://www.fantasypros.com/nfl/cheatsheets/top-players.php"
        if (rankings!!.getLeagueSettings().scoringSettings.receptions >= 1.0) {
            url = "http://www.fantasypros.com/nfl/cheatsheets/top-ppr-players.php"
        } else if (rankings!!.getLeagueSettings().scoringSettings.receptions > 0) {
            url = "https://www.fantasypros.com/nfl/cheatsheets/top-half-ppr-players.php"
        }
        parseFPCheatSheetWorker(url, ecr)
        for (playerId in rankings.players.keys) {
            if (ecr.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.ecr = ecr[playerId]!!
            }
        }
    }

    @Throws(IOException::class)
    fun parseADPWrapper(rankings: Rankings) {
        val adp: MutableMap<String, Double?> = HashMap()
        val adpUrl: String
        val rowSize: Int
        when {
            rankings!!.getLeagueSettings().isBestBall -> {
                adpUrl = "https://www.fantasypros.com/nfl/adp/best-ball-overall.php"
                rowSize = 6
            }
            rankings!!.getLeagueSettings().scoringSettings.receptions >= 1.0 -> {
                adpUrl = "http://www.fantasypros.com/nfl/adp/ppr-overall.php"
                rowSize = 9
            }
            rankings!!.getLeagueSettings().scoringSettings.receptions > 0 -> {
                adpUrl = "https://www.fantasypros.com/nfl/adp/half-point-ppr-overall.php"
                rowSize = 7
            }
            else -> {
                adpUrl = "http://www.fantasypros.com/nfl/adp/overall.php"
                rowSize = 7
            }
        }
        parseADPWorker(adp, adpUrl, rowSize)
        for (playerId in rankings.players.keys) {
            if (adp.containsKey(playerId)) {
                rankings.getPlayer(playerId).adp = adp[playerId]!!
            }
        }
    }

    @Throws(IOException::class)
    fun parseDynastyWrapper(rankings: Rankings) {
        val dynasty: MutableMap<String, Double?> = HashMap()
        val url = "https://www.fantasypros.com/nfl/cheatsheets/top-players.php?type=dynasty"
        parseFPCheatSheetWorker(url, dynasty)
        for (playerId in rankings.players.keys) {
            if (dynasty.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.dynastyRank = dynasty[playerId]!!
            }
        }
    }

    @Throws(IOException::class)
    private fun parseFPCheatSheetWorker(url: String,
                                        rankingsMap: MutableMap<String, Double?>) {
        val doc = getDocument(url)
        val ecrArr = getElemsFromDoc(doc, "div.player-list div div li")
        for (i in ecrArr.indices) {
            try {
                val ecrVal = ecrArr[i].substringBefore(". ").toDouble()
                val playerData = ecrArr[i].substringAfter(". ")
                val teamPosArr = playerData.substring(playerData.lastIndexOf(' ') + 1).split('-')
                val playerName = playerData.substring(0, playerData.lastIndexOf(' '))

                val filteredName = teamPosArr[1]
                var team: String?
                team = if (filteredName.split(" ").size > 1) {
                    normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim { it <= ' ' })
                } else {
                    normalizeTeams(filteredName.trim { it <= ' ' })
                }
                val name = normalizeNames(normalizeDefenses(playerName))
                val posInd = teamPosArr[0].replace("(\\d+,\\d+)|\\d+".toRegex(), "")
                        .replace("DST", Constants.DST)
                if (Constants.DST == posInd) {
                    team = normalizeTeams(playerName)
                }
                rankingsMap[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = ecrVal
            } catch (siooe: StringIndexOutOfBoundsException) {
                Log.d(TAG, "Failed to parse a player's ECR", siooe)
            }
        }
    }

    @Throws(IOException::class)
    private fun parseADPWorker(adp: MutableMap<String, Double?>, adpUrl: String, rowSize: Int) {
        val td = parseURLWithUA(adpUrl, "table.player-table tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (isInteger(td[i])) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            try {
                if (i + rowSize >= td.size) {
                    break
                } else if ("" == td[i]) {
                    i++
                }
                val filteredName = td[i + 1].split(
                        " \\(".toRegex())[0].split(", ")[0]
                var team: String?
                team = if (filteredName.split(" ").size > 1) {
                    normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim { it <= ' ' })
                } else {
                    normalizeTeams(filteredName.trim { it <= ' ' })
                }
                val withoutTeam = filteredName.substring(0, filteredName.lastIndexOf(" "))
                val name = normalizeNames(normalizeDefenses(withoutTeam))
                if (i + rowSize >= td.size) {
                    break
                }
                val adpStr = td[i + (rowSize - 1)].replace(",", "").toDouble()
                val posInd = td[i + 2]
                        .replace("(\\d+,\\d+)|\\d+".toRegex(), "")
                        .replace("DST", Constants.DST)
                if (Constants.DST == posInd) {
                    team = normalizeTeams(withoutTeam)
                }
                adp[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = adpStr
            } catch (siooe: StringIndexOutOfBoundsException) {
                //Log.d(TAG, "Failed to parse a player's ADP", siooe);
            }
            i += rowSize
        }
    }

    @Throws(IOException::class)
    fun parseSchedule(rankings: Rankings) {
        val elems = parseURLWithUA("https://www.fantasypros.com/nfl/schedule/grid.php?week=0",
                "table.table-bordered tbody tr td")
        var i = 0
        while (i < elems.size) {
            val teamName = normalizeTeams(elems[i])
            val schedule = StringBuilder()
            for ((weekCounter, j) in (i + 1 until i + 18).withIndex()) {
                val opponentFull = elems[j]
                var suffix: String
                suffix = if (opponentFull.split(" ").size > 1) {
                    val gameLocation = opponentFull.split(" ")[0]
                    val opponent = normalizeTeams(opponentFull.split(" ")[1])
                    "$gameLocation $opponent"
                } else {
                    "BYE"
                }
                schedule.append(weekCounter + 1)
                        .append(": ")
                        .append(suffix)
                        .append(Constants.LINE_BREAK)
            }
            val scheduleString = schedule.substring(0, schedule.length - 1)
            val team = rankings.getTeam(teamName)!!
            team.schedule = scheduleString
            i += 19
        }
    }
}