package com.devingotaswitch.rankings.sources

import android.util.Log
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils
import com.devingotaswitch.utils.JsoupUtils
import com.devingotaswitch.utils.ParsingUtils
import java.io.IOException
import java.util.HashMap

object ParseBestBallRanks {

    @Throws(IOException::class)
    fun parseBestBallWrapper(rankings: Rankings) {
        val bestBall: MutableMap<String, Double?> = HashMap()
        val url = "https://www.rotoballer.com/updated-2021-best-ball-rankings-fantasy-football-post-nfl-draft/876182"
        parseBestBallWorker(url, bestBall)
        for (player in rankings.players.values) {
            val playerId = player.name + Constants.PLAYER_ID_DELIMITER + player.position
            if (bestBall.containsKey(playerId)) {
                player.bestBallRank = bestBall[playerId]!!
            }
        }
    }

    @Throws(IOException::class)
    private fun parseBestBallWorker(url: String, bestBall: MutableMap<String, Double?>) {
        val td = JsoupUtils.parseURLWithUA(url, "table tbody tr td")
        for (i in td.indices step 4) {
            if(GeneralUtils.isInteger(td[i+1])) {
                val name = ParsingUtils.normalizeNames(td[i + 2])
                val bestBallVal = td[i + 1].toDouble()
                val posInd = td[i + 3]
                bestBall[name + Constants.PLAYER_ID_DELIMITER + posInd] = bestBallVal
            }
        }
    }
}