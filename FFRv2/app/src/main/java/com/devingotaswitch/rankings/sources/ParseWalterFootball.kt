package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.getPlayerFromRankings
import java.io.IOException

object ParseWalterFootball {
    @Throws(IOException::class)
    fun wfRankings(rankings: Rankings) {
        val r = rankings.getLeagueSettings().rosterSettings
        val s = rankings.getLeagueSettings().scoringSettings
        if (r.qbCount > 1 || r.flex != null && r.flex!!.qbrbwrteCount > 0) {
            wfRankingsHelper(rankings,
                    "http://walterfootball.com/fantasycheatsheet/" + Constants.YEAR_KEY + "/twoqb")
        } else if (s.receptions > 0) {
            wfRankingsHelper(rankings,
                    "http://walterfootball.com/fantasycheatsheet/" + Constants.YEAR_KEY + "/ppr")
        } else {
            wfRankingsHelper(rankings,
                    "http://walterfootball.com/fantasycheatsheet/" + Constants.YEAR_KEY + "/traditional")
        }
    }

    @Throws(IOException::class, ArrayIndexOutOfBoundsException::class)
    private fun wfRankingsHelper(rankings: Rankings, url: String) {
        val perPlayer = parseURLWithUA(url,
                "ol.fantasy-board div li")
        var i = 0
        while (i < perPlayer.size) {
            // Rows are name, pos, team. bye $x
            val row: Array<String> = perPlayer[i].split(", ").toTypedArray()
            var playerName = row[0]
            var pos: String
            if (!perPlayer[i].contains("DEF")) {
                pos = row[1]
            } else {
                playerName += " D/ST"
                pos = Constants.DST
            }
            var aucVal = row[2].split("\\$".toRegex())[1].split(" ")[0].toDouble()
            if (aucVal < 0.0) {
                // For whatever reason, had players going as low as -$87
                aucVal = 1.0
            }
            val team = row[2].split("\\. ".toRegex())[0]
            rankings.processNewPlayer(getPlayerFromRankings(playerName, team, pos, aucVal))
            i ++
        }
    }
}