package com.devingotaswitch.rankings.sources

import android.util.Log
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.getPlayerFromRankings
import java.io.IOException

object ParseNFL {
    @Throws(IOException::class)
    fun parseNFLAAVWrapper(rankings: Rankings) {
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=0&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=26&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=51&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=76&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=101&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=126&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=151&sort=draftAveragePosition")
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=176&sort=draftAveragePosition")
    }

    @Throws(IOException::class)
    private fun parseNFLAAVWorker(rankings: Rankings, url: String) {
        val td = parseURLWithUA(url, "td")
        var i = 0
        while (i < td.size) {
            val nameSet = td[i].split("NFI")[0].split(" ")
            var name = StringBuilder()
            var filter = 0
            for (j in nameSet.indices) {
                if (nameSet[j] == "-") {
                    filter = j - 1
                    break
                }
                if (nameSet[j] == "View") {
                    filter = j - 1
                    break
                }
                if (nameSet[j].length == j) {
                    filter = j
                    break
                }
                if (nameSet[j] == Constants.QB || nameSet[j] == Constants.RB || nameSet[j] == Constants.WR
                        || nameSet[j] == Constants.TE || nameSet[j] == "DEF" || nameSet[j] == Constants.K) {
                    filter = j
                    break
                }
            }
            for (j in 0 until filter) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(name.substring(0, name.length - 1))
            var pos = nameSet[filter]
            val worth = td[i + 3]
            val `val` = worth.toDouble()
            var team = nameSet[nameSet.size - 1]
            if (team.isBlank() || team.length == 1 || team == "PUP") {
                team = nameSet[nameSet.size - 2]
            }
            if (td[i].contains("View News") && td[i].contains("View Videos")) {
                // Sometimes it's <name> <pos> - <team> View News View Videos
                team = nameSet[nameSet.size - 5]
            } else if (td[i].contains("View News") || td[i].contains("View Videos")) {
                // Sometimes it's <name> <pos> - <team> View News/Videos
                team = nameSet[nameSet.size - 3]
            }
            if ("DEF" == pos) {
                team = name.toString()
                pos = Constants.DST
            }
            var player = Player()
            player.name = name.toString()
            player.teamName = team
            player.position = pos
            player.handleNewValue(`val`)
            player = getPlayerFromRankings(name.toString(), team, pos, `val`)
            rankings.processNewPlayer(player)
            i += 4
        }
    }
}