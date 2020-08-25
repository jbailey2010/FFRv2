package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils.isInteger
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException

object ParsePFO {
    @Throws(IOException::class)
    fun parsePFOLineData(rankings: Rankings) {
        val td = parseURLWithUA(
                "https://www.footballoutsiders.com/stats/nfl/offensive-line/" + Constants.LAST_YEAR_KEY,
                "table.stats td")
        var start = 0
        for (i in td.indices) {
            if (isInteger(td[i])) {
                start = i
                break
            }
        }
        var i = start
        while (i < td.size) {
            if (td[i] == "RUN BLOCKING") {
                i += 2
                i += 16
                continue
            } else if (td[i + 1] == "Team" || "" == td[i + 1]) {
                i += 16
                i += 16
                continue
            } else if (td[i + 1] == "NFL") {
                break
            }
            val team = normalizeTeams(td[i + 1])
            val sackRate = td[i + 15]
            val pbRank = td[i + 13]
            val adjYPC = td[i + 2]
            val adjYPCRank = td[i]
            val power = td[i + 4]
            val powerRank = td[i + 5]
            val stuff = td[i + 6]
            val stuffRank = td[i + 7]
            val secLevel = td[i + 8]
            val secLevelRank = td[i + 9]
            val openField = td[i + 10]
            val openFieldRank = td[i + 11]
            val olData = sackRate +
                    " adjusted team sack rate." +
                    Constants.LINE_BREAK +
                    adjYPC +
                    " adjusted team yards per carry (" +
                    adjYPCRank +
                    ")" +
                    Constants.LINE_BREAK +
                    "Pass Block Ranking: " +
                    pbRank +
                    Constants.LINE_BREAK +
                    power +
                    " success rate with < 3 yards to go (" +
                    powerRank +
                    ")" +
                    Constants.LINE_BREAK +
                    stuff +
                    " rate of being stuffed at the line (" +
                    stuffRank +
                    ")" +
                    Constants.LINE_BREAK +
                    secLevel +
                    " YPC earned 5 to 10 yards past LOS (" +
                    secLevelRank +
                    ")" +
                    Constants.LINE_BREAK +
                    openField +
                    " YPC earned 10+ yards past LOS (" +
                    openFieldRank +
                    ")" +
                    Constants.LINE_BREAK +
                    Constants.LINE_BREAK +
                    "Pass Block Ranking: " +
                    pbRank +
                    Constants.LINE_BREAK +
                    "Run Block Ranking: " +
                    adjYPCRank
            rankings.getTeam(team).oLineRanks = olData
            i += 16
        }
    }
}