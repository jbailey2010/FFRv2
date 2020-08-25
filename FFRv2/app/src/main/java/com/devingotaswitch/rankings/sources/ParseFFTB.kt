package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.Team
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils.isInteger
import com.devingotaswitch.utils.JsoupUtils.parseURLWithoutUAOrTls
import com.devingotaswitch.utils.ParsingUtils.getPlayerFromRankings
import com.devingotaswitch.utils.ParsingUtils.normalizeDefenses
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException

object ParseFFTB {
    @Throws(IOException::class)
    fun parseFFTBRankingsWrapper(rankings: Rankings) {
        val teams = java.lang.String.valueOf(rankings.leagueSettings.teamCount)
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=QB&teams=" + teams + "&budget=200", Constants.QB)
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=RB&teams=" + teams + "&budget=200", Constants.RB)
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=WR&teams=" + teams + "&budget=200", Constants.WR)
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=TE&teams=" + teams + "&budget=200", Constants.TE)
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=PK&teams=" + teams + "&budget=200", Constants.K)
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=Def&teams=" + teams + "&budget=200", Constants.DST)
    }

    @Throws(IOException::class)
    private fun parseFFTBPage(rankings: Rankings, url: String, pos: String) {
        val brokenUp = parseURLWithoutUAOrTls(url, "td")
        var min = 0
        for (i in brokenUp.indices) {
            if (isInteger(brokenUp[i])) {
                min = i + 1
                break
            }
        }
        var i = min
        while (i < brokenUp.size) {
            if (i + 8 > brokenUp.size) {
                break
            }
            var name: String?
            val team = normalizeTeams(brokenUp[i + 1])
            name = if (Constants.DST == pos) {
                normalizeDefenses(team)
            } else {
                normalizeNames(brokenUp[i])
            }
            val age = brokenUp[i + 4]
            val exp = brokenUp[i + 5]
            val bye = brokenUp[i + 3]
            if (team!!.split(" ").size <= 3) {
                var isNewPlayer = false
                val playerId = name +
                        Constants.PLAYER_ID_DELIMITER +
                        team +
                        Constants.PLAYER_ID_DELIMITER +
                        pos
                var player: Player
                if (rankings.players.containsKey(playerId)) {
                    player = rankings.getPlayer(playerId)
                } else {
                    // FF Toolbox rankings are ass, so we'll just default to 1 if we haven't seen it yet.
                    // They have retired players for $30+, so their value is suspect.
                    isNewPlayer = true
                    player = getPlayerFromRankings(name!!, team, pos, 1.0)
                }
                if (isInteger(age)) {
                    player.age = age.toInt()
                }
                if (isInteger(exp)) {
                    player.experience = exp.toInt()
                } else if ("R" == exp) {
                    player.experience = 0
                }
                if (isNewPlayer) {
                    rankings.processNewPlayer(player)
                } else {
                    rankings.players[player.uniqueId] = player
                }
            }
            val newTeam = Team()
            newTeam.bye = bye
            newTeam.name = team
            rankings.addTeam(newTeam)
            i += 8
        }
    }
}