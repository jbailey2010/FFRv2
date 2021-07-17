package com.devingotaswitch.rankings.sources

import android.util.Log
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils
import com.devingotaswitch.utils.JsoupUtils
import com.devingotaswitch.utils.ParsingUtils
import java.io.IOException
import java.util.HashMap

object ParseRookieRanks {

    private const val TAG = "ParseRookieRanks"

    @Throws(IOException::class)
    fun parseRookieWrapper(rankings: Rankings) {
        val rookie: MutableMap<String, Double?> = HashMap()
        val url = "https://fantasyfootballcalculator.com/rankings/rookie"
        parseRookieWorker(url, rookie)
        for (playerId in rankings.players.keys) {
            if (rookie.containsKey(playerId)) {
                val player = rankings.getPlayer(playerId)
                player.rookieRank = rookie[playerId]!!
            }
        }
    }

    @Throws(IOException::class)
    private fun parseRookieWorker(url: String, rookie: MutableMap<String, Double?>) {
        val doc = JsoupUtils.getDocument(url)
        val td = JsoupUtils.getElemsFromDoc(doc, "table.table-striped tbody tr td")
        for (i in td.indices step 5) {
            var team = ParsingUtils.normalizeTeams(td[i+2])
            val name = ParsingUtils.normalizeNames(ParsingUtils.normalizeDefenses(td[i+1]))
            val rookieVal = td[i].replace(".", "").toDouble()
            val posInd = td[i + 3]
            rookie[name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd] = rookieVal
        }
    }
}