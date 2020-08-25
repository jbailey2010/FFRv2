package com.devingotaswitch.rankings.sources

import com.amazonaws.util.StringUtils
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.getDocument
import com.devingotaswitch.utils.JsoupUtils.getElemsFromDoc
import com.devingotaswitch.utils.ParsingUtils.normalizeDefenses
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException
import java.util.*

object ParseInjuries {
    @Throws(IOException::class)
    fun parsePlayerInjuries(rankings: Rankings) {
        val injuries: MutableMap<String, String?> = HashMap()
        val doc = getDocument(
                "https://www.pro-football-reference.com/players/injuries.htm")
        val players = getElemsFromDoc(doc, "table.stats_table th.left a")
        val left = getElemsFromDoc(doc, "table.stats_table td.left")
        val right = getElemsFromDoc(doc, "table.stats_table td.right")
        //"table.stats_table tbody tr");
        for (i in players.indices) {
            var name = normalizeNames(players[i])
            val leftIndex = i * 3
            val team = normalizeTeams(left[leftIndex])
            var pos = left[leftIndex + 1]
            val comment = left[leftIndex + 2]
            val rightIndex = i * 2
            val injuryType = right[rightIndex]
            var playerStatus = right[rightIndex + 1]
            playerStatus = playerStatus.substring(0, 1).toUpperCase(Locale.US) + playerStatus.substring(1)
            var injuryStr: String
            if (pos == "CB" || pos == "LB" || pos == "DT" || pos == "DB" || pos == "DE" || pos == "S") {
                val playerName = name
                name = normalizeDefenses(team)
                pos = Constants.DST
                val playerId = getPlayerId(name, pos, team)

                // If it's defense, we'll track it collectively.
                var baseStr = ""
                if (injuries.containsKey(team)) {
                    baseStr = injuries[playerId] + Constants.LINE_BREAK
                }
                baseStr = baseStr +
                        playerName +
                        ": " +
                        playerStatus +
                        " (" +
                        injuryType +
                        ")"
                injuries[playerId] = baseStr
            } else {
                val playerId = getPlayerId(name, pos, team)
                injuryStr = playerStatus +
                        " (" +
                        injuryType +
                        ")" +
                        Constants.LINE_BREAK +
                        Constants.LINE_BREAK +
                        comment
                injuries[playerId] = injuryStr
            }
        }
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            val injuryStatus = injuries[player.uniqueId]
            if (!StringUtils.isBlank(injuryStatus)) {
                player.injuryStatus = injuryStatus
            }
        }
    }

    private fun getPlayerId(name: String?, pos: String, team: String?): String {
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos
    }
}