package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.getPlayerFromRankings
import java.io.IOException

object ParseYahoo {
    @Throws(IOException::class)
    fun parseYahooWrapper(rankings: Rankings) {
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=QB&sort=DA_PC",
                Constants.QB)
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=RB&sort=DA_PC",
                Constants.RB)
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=WR&sort=DA_PC",
                Constants.WR)
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=TE&sort=DA_PC",
                Constants.TE)
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=K&sort=DA_PC",
                Constants.K)
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=DEF&sort=DA_PC",
                Constants.DST)
    }

    @Throws(IOException::class)
    private fun parseYahoo(rankings: Rankings, url: String, pos: String) {
        val td = parseURLWithUA(url, "td")
        var startingIndex = 0
        for (i in td.indices) {
            if (td[i].contains("Note") || td[i].contains("Notes")) {
                startingIndex = i
                break
            }
        }
        var i = startingIndex
        while (i < td.size) {
            if (td[i].contains("AdChoices")) {
                break
            }
            var name = StringBuilder()
            var splitter = "Note "
            if (td[i].split(" \\(".toRegex())[0].contains("Notes")) {
                splitter = "Notes "
            }
            val fName = td[i].split(" \\(".toRegex())[0].split(splitter)[1]
                    .split(" - ")[0]
            val nameSet = fName.split(" ")
            for (j in 0 until nameSet.size - 1) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(name.substring(0, name.length - 1))
            val team = nameSet[nameSet.size - 1]
            if (td[i].contains("DEF")) {
                name = StringBuilder(team)
            }
            val rank = td[i + 1].split("\\$".toRegex())[1]
            val aavStr = td[i + 2].split("\\$".toRegex())[1]
            var aav = 1.0
            val worth = rank.toDouble()
            if (aavStr != "-" && aavStr != "0.0") {
                aav = aavStr.toDouble()
            }
            rankings.processNewPlayer(getPlayerFromRankings(name.toString(), team, pos, aav))
            rankings.processNewPlayer(getPlayerFromRankings(name.toString(), team, pos, worth))
            i += 4
        }
    }
}