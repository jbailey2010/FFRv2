package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.getDocument
import com.devingotaswitch.utils.JsoupUtils.getElemsFromDoc
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException
import java.util.*

object ParseDraft {
    @Throws(IOException::class)
    fun parseTeamDraft(rankings: Rankings) {
        val doc = getDocument("https://www.spotrac.com/nfl/draft/")
        val names = getElemsFromDoc(doc, "table.datatable tbody tr td.player a")
        val pickContexts = getElemsFromDoc(doc, "table.datatable tbody tr td.center")
        val picks = HashMap<String?, String?>()
        for (i in names.indices) {

            // There are 10 total items in a row, 1 of type player, 9 of type center. Since they embed news in
            // some rows, I did two selections, one of each class. We're iterating based on players, then converting
            // that index for the other stuff. Since there's 9 of those per player, it's x*9 + y.
            val contextIndex = i * 9
            val pick = pickContexts[contextIndex]
            val team = normalizeTeams(pickContexts[contextIndex + 1].split(" from ")[0])
            val name = names[i]
            val pos = pickContexts[contextIndex + 2]
            val age = pickContexts[contextIndex + 3]
            val college = pickContexts[contextIndex + 4]
            val draftData = pick +
                    ": " +
                    name +
                    ", " +
                    pos +
                    " - " +
                    college +
                    " (" +
                    age +
                    ")"
            if (picks.containsKey(team)) {
                val existingData = picks[team]
                val updated = existingData +
                        Constants.LINE_BREAK +
                        draftData
                picks[team] = updated
            } else {
                picks[team] = draftData
            }
        }
        for (key in picks.keys) {
            val team = rankings.getTeam(key)
            team.draftClass = picks[key]
        }
    }
}