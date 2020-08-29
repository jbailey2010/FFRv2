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
        val risk: MutableMap<String, Double> = HashMap()
        var url = "http://www.fantasypros.com/nfl/rankings/consensus-cheatsheets.php"
        if (rankings.leagueSettings.scoringSettings.receptions >= 1.0) {
            url = "http://www.fantasypros.com/nfl/rankings/ppr-cheatsheets.php"
        } else if (rankings.leagueSettings.scoringSettings.receptions > 0) {
            url = "https://www.fantasypros.com/nfl/rankings/half-point-ppr-cheatsheets.php"
        }
        parseECRWorker(url, ecr, risk)
        for (playerId in rankings.players.keys) {
            if (ecr.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.ecr = ecr[playerId]!!
                if (rankings.leagueSettings.isSnake || rankings.leagueSettings.isAuction) {
                    player.risk = risk[playerId]!!
                }
            }
        }
    }

    @Throws(IOException::class)
    fun parseADPWrapper(rankings: Rankings) {
        val adp: MutableMap<String, Double?> = HashMap()
        val adpUrl: String
        val rowSize: Int
        when {
            rankings.leagueSettings.isBestBall -> {
                adpUrl = "https://www.fantasypros.com/nfl/adp/best-ball-overall.php"
                rowSize = 7
            }
            rankings.leagueSettings.scoringSettings.receptions >= 1.0 -> {
                adpUrl = "http://www.fantasypros.com/nfl/adp/ppr-overall.php"
                rowSize = 8
            }
            rankings.leagueSettings.scoringSettings.receptions > 0 -> {
                adpUrl = "https://www.fantasypros.com/nfl/adp/half-point-ppr-overall.php"
                rowSize = 6
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
        val risk: MutableMap<String, Double> = HashMap()
        val url = "https://www.fantasypros.com/nfl/rankings/dynasty-overall.php"
        parseDynastyWorker(url, dynasty, risk)
        for (playerId in rankings.players.keys) {
            if (dynasty.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.dynastyRank = dynasty[playerId]!!
                if (rankings.leagueSettings.isDynasty) {
                    player.risk = risk[playerId]!!
                }
            }
        }
    }

    @Throws(IOException::class)
    fun parseRookieWrapper(rankings: Rankings) {
        val rookie: MutableMap<String, Double?> = HashMap()
        val risk: MutableMap<String, Double> = HashMap()
        val url = "https://www.fantasypros.com/nfl/rankings/rookies.php"
        parseRookieWorker(url, rookie, risk)
        for (playerId in rankings.players.keys) {
            if (rookie.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.rookieRank = rookie[playerId]!!
                if (rankings.leagueSettings.isRookie) {
                    player.risk = risk[playerId]!!
                }
            }
        }
    }

    @Throws(IOException::class)
    fun parseBestBallWrapper(rankings: Rankings) {
        val bestBall: MutableMap<String, Double?> = HashMap()
        val risk: MutableMap<String, Double> = HashMap()
        val url = "https://www.fantasypros.com/nfl/rankings/best-ball-overall.php"
        parseBestBallWorker(url, bestBall, risk)
        for (playerId in rankings.players.keys) {
            if (bestBall.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.bestBallRank = bestBall[playerId]!!
                if (rankings.leagueSettings.isBestBall) {
                    player.risk = risk[playerId]!!
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun parseECRWorker(url: String,
                               ecr: MutableMap<String, Double?>, risk: MutableMap<String, Double>) {
        val doc = getDocument(url)
        val names = getElemsFromDoc(doc, "table.player-table tbody tr td span.full-name")
        val td = getElemsFromDoc(doc, "table.player-table tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (isInteger(td[i])) {
                min = i + 2
                break
            }
        }
        var playerCount = 0
        var i = min
        while (i < td.size) {
            try {
                if (i + 9 >= td.size) {
                    break
                }
                while (td[i].split(" ").size < 3 && i < td.size) {
                    i++
                }
                val fullName = names[playerCount++].split(" \\(".toRegex())[0]
                val filteredName = td[i].split(
                        " \\(".toRegex())[0].split(", ")[0]
                var team: String?
                team = if (filteredName.split(" ").size > 1) {
                    normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim { it <= ' ' })
                } else {
                    normalizeTeams(filteredName.trim { it <= ' ' })
                }
                val name = normalizeNames(normalizeDefenses(fullName))
                val ecrVal = td[i + 5].toDouble()
                val riskVal = td[i + 6].toDouble()
                val posInd = td[i + 1].replace("(\\d+,\\d+)|\\d+".toRegex(), "")
                        .replace("DST", Constants.DST)
                if (Constants.DST == posInd) {
                    team = normalizeTeams(fullName)
                }
                ecr[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = ecrVal
                risk[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = riskVal
                if (td[i + 7].contains("Tier")) {
                    i += 2
                }
            } catch (siooe: StringIndexOutOfBoundsException) {
                Log.d(TAG, "Failed to parse a player's ECR", siooe)
            }
            i += 9
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
    private fun parseDynastyWorker(url: String, dynasty: MutableMap<String, Double?>, risk: MutableMap<String, Double>) {
        val doc = getDocument(url)
        val names = getElemsFromDoc(doc, "table.player-table tbody tr td span.full-name")
        val td = getElemsFromDoc(doc, "table.player-table tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (isInteger(td[i])) {
                min = i + 2
                break
            }
        }
        var playerCount = 0
        var i = min
        while (i < td.size) {
            try {
                if (i + 9 >= td.size) {
                    break
                }
                while (td[i].split(" ").size < 3 && i < td.size) {
                    i++
                }
                val fullName = names[playerCount++].split(" \\(".toRegex())[0]
                val filteredName = td[i].split(
                        " \\(".toRegex())[0].split(", ")[0]
                var team: String?
                team = if (filteredName.split(" ").size > 1) {
                    normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim { it <= ' ' })
                } else {
                    normalizeTeams(filteredName.trim { it <= ' ' })
                }
                val name = normalizeNames(normalizeDefenses(fullName))
                val dynastyVal = td[i + 6].toDouble()
                val riskVal = td[i + 7].toDouble()
                val posInd = td[i + 1].replace("(\\d+,\\d+)|\\d+".toRegex(), "")
                        .replace("DST", Constants.DST)
                if (Constants.DST == posInd) {
                    team = normalizeTeams(fullName)
                }
                dynasty[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = dynastyVal
                risk[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = riskVal
            } catch (siooe: StringIndexOutOfBoundsException) {
                Log.d(TAG, "Failed to parse a player's dynasty rank", siooe)
            }
            i += 9
        }
    }

    @Throws(IOException::class)
    private fun parseRookieWorker(url: String, rookie: MutableMap<String, Double?>, risk: MutableMap<String, Double>) {
        val doc = getDocument(url)
        val names = getElemsFromDoc(doc, "table.player-table tbody tr td span.full-name")
        val td = getElemsFromDoc(doc, "table.player-table tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (isInteger(td[i])) {
                min = i + 2
                break
            }
        }
        var playerCount = 0
        var i = min
        while (i < td.size) {
            try {
                if (i + 10 >= td.size) {
                    break
                }
                while (td[i].split(" ").size < 3 && i < td.size) {
                    i++
                }
                val fullName = names[playerCount++].split(" \\(".toRegex())[0]
                val filteredName = td[i].split(
                        " \\(".toRegex())[0].split(", ")[0]
                var team = normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim { it <= ' ' })
                val name = normalizeNames(normalizeDefenses(fullName))
                val rookieVal = td[i + 6].toDouble()
                val riskVal = td[i + 7].toDouble()
                val posInd = td[i + 1].replace("(\\d+,\\d+)|\\d+".toRegex(), "")
                        .replace("DST", Constants.DST)
                if (Constants.DST == posInd) {
                    team = normalizeTeams(fullName)
                }
                rookie[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = rookieVal
                risk[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = riskVal
            } catch (siooe: StringIndexOutOfBoundsException) {
                Log.d(TAG, "Failed to parse a player's rookie rank", siooe)
            }
            i += 10
        }
    }

    @Throws(IOException::class)
    private fun parseBestBallWorker(url: String, bestBall: MutableMap<String, Double?>, risk: MutableMap<String, Double>) {
        val doc = getDocument(url)
        val names = getElemsFromDoc(doc, "table.player-table tbody tr td span.full-name")
        val td = getElemsFromDoc(doc, "table.player-table tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (isInteger(td[i])) {
                min = i + 1
                break
            }
        }
        var playerCount = 0
        var i = min
        while (i < td.size) {
            try {
                if (i + 10 >= td.size) {
                    break
                }
                while (td[i].split(" ").size < 3 && i < td.size) {
                    i++
                }
                val fullName = names[playerCount++].split(" \\(".toRegex())[0]
                val filteredName = td[i].split(
                        " \\(".toRegex())[0].split(", ")[0]
                var team: String?
                team = if (filteredName.split(" ").size > 1) {
                    normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim { it <= ' ' })
                } else {
                    normalizeTeams(filteredName.trim { it <= ' ' })
                }
                val name = normalizeNames(normalizeDefenses(fullName))
                val rookieVal = td[i + 5].toDouble()
                val riskVal = td[i + 6].toDouble()
                val posInd = td[i + 1].replace("(\\d+,\\d+)|\\d+".toRegex(), "")
                        .replace("DST", Constants.DST)
                if (Constants.DST == posInd) {
                    team = normalizeTeams(fullName)
                }
                bestBall[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = rookieVal
                risk[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = riskVal
            } catch (siooe: StringIndexOutOfBoundsException) {
                Log.d(TAG, "Failed to parse a player's best ball rank", siooe)
            }
            i += 10
        }
    }

    @Throws(IOException::class)
    fun parseSchedule(rankings: Rankings) {
        val elems = parseURLWithUA("https://www.fantasypros.com/nfl/schedule.php",
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
            i += 18
        }
    }
}