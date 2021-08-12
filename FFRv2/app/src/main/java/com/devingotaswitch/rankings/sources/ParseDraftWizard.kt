package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.getPlayerFromRankings
import java.io.IOException

object ParseDraftWizard {
    @Throws(IOException::class)
    fun parseRanksWrapper(rankings: Rankings) {
        var type = "STD"
        if (rankings.getLeagueSettings().scoringSettings.receptions >= 1.0) {
            type = "PPR"
        }
        if (rankings.getLeagueSettings().scoringSettings.receptions > 0) {
            type = "HALF"
        }
        var url = ("http://draftwizard.fantasypros.com/editor/createFromProjections.jsp?sport=nfl&scoringSystem="
                + type + "&showAuction=Y")
        url += "&teams=" + rankings.getLeagueSettings().teamCount
        url += "&tb=200"
        var qbTotal = rankings.getLeagueSettings().rosterSettings.qbCount
        if (rankings.getLeagueSettings().rosterSettings.flex != null) {
            qbTotal += rankings.getLeagueSettings().rosterSettings.flex!!.qbrbwrteCount
        }
        url += "&QB=$qbTotal"
        url += "&RB=" + rankings.getLeagueSettings().rosterSettings.rbCount
        url += "&WR=" + rankings.getLeagueSettings().rosterSettings.wrCount
        url += "&TE=" + rankings.getLeagueSettings().rosterSettings.teCount
        url += "&DST=" + rankings.getLeagueSettings().rosterSettings.dstCount
        url += "&K=" + rankings.getLeagueSettings().rosterSettings.kCount
        url += "&BN=" + rankings.getLeagueSettings().rosterSettings.benchCount
        // Removed the flex customizations, they were being dropped and a zero value provided
        parseRanksWorker(rankings, url)
    }

    @Throws(IOException::class)
    private fun parseRanksWorker(rankings: Rankings, url: String) {
        val td = parseURLWithUA(url,
                "table#OverallTable td")
        var startingIndex = 0
        for (i in td.indices) {
            if (td[i].contains(" - ") && td[i].split(" ").size > 3) {
                startingIndex = i
                break
            }
        }
        var i = startingIndex
        while (i < td.size) {
            val aucVal = td[i + 2].substring(1
            ).toDouble()
            val playerName = td[i].split(" \\(".toRegex())[0]
            val teamPos = td[i].split(" \\(".toRegex())[1]
            val team = teamPos.split(" - ")[0]
            val pos = teamPos.split(" - ")[1].split("\\)".toRegex())[0]
            val player = getPlayerFromRankings(playerName, team, pos, aucVal)

            rankings.processNewPlayer(player)
            i += 5
        }
    }
}