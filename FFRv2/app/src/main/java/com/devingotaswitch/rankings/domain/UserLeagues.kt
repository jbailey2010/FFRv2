package com.devingotaswitch.rankings.domain

import android.content.Context
import com.devingotaswitch.fileio.RankingsDBWrapper

class UserLeagues(var leaguesMap: MutableMap<String, LeagueSettings>) {

    lateinit var currentLeague: LeagueSettings

    fun updateCurrentLeague(newLeague: LeagueSettings, context: Context) {
        for (league in leaguesMap.values) {
            league.isCurrentLeague = league.name == newLeague.name
        }
        currentLeague = newLeague
        RankingsDBWrapper().upsertLeagues(context, leaguesMap.values)
    }

    fun insertCurrentLeague(newLeague: LeagueSettings, context: Context) {
        leaguesMap[newLeague.name] = newLeague
        updateCurrentLeague(newLeague, context)
    }

    fun deleteLeague(league: LeagueSettings, context: Context) {
        leaguesMap.remove(league.name)
        val currLeague = leaguesMap[
                leaguesMap.keys.iterator().next()]!!
        updateCurrentLeague(currLeague, context)
        RankingsDBWrapper().deleteLeague(context, league)
    }

    fun getLeagueByName(name: String): LeagueSettings? {
        return leaguesMap[name]
    }

    init {
        for (league in leaguesMap.values) {
            if (league.isCurrentLeague) {
                currentLeague = league
            }
        }
    }
}