package com.devingotaswitch.rankings.domain

import android.app.Activity
import android.util.Log
import com.amazonaws.util.StringUtils
import com.devingotaswitch.appsync.AppSyncHelper.decrementPlayerWatchedCount
import com.devingotaswitch.appsync.AppSyncHelper.incrementPlayerWatchedCount
import com.devingotaswitch.appsync.AppSyncHelper.updateUserCustomPlayerData
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.rankings.asynctasks.RankingsFetcher.RanksAggregator
import com.devingotaswitch.rankings.asynctasks.RankingsFetcher.VBDUpdater
import com.devingotaswitch.rankings.asynctasks.RankingsLoader
import com.devingotaswitch.rankings.asynctasks.RankingsLoader.*
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.ParsingUtils
import com.devingotaswitch.utils.ParsingUtils.conditionallyAddContext
import com.devingotaswitch.utils.ParsingUtils.normalizePlayerFields
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Rankings {
    val userSettings: UserSettings
        get() = Companion.userSettings

    fun isPlayerWatched(playerId: String): Boolean {
        return playerWatchList.contains(playerId)
    }

    fun updatePlayerNote(act: Activity, playerId: String, note: String) {
        if (!StringUtils.isBlank(note)) {
            playerNotes[playerId] = note
        } else {
            playerNotes.remove(playerId)
        }
        updateUserCustomPlayerData(act, playerWatchList, playerNotes)
    }

    fun togglePlayerWatched(act: Activity, playerId: String) {
        if (playerWatchList.contains(playerId)) {
            playerWatchList.remove(playerId)
            decrementPlayerWatchedCount(act, playerId)
        } else {
            playerWatchList.add(playerId)
            incrementPlayerWatchedCount(act, playerId)
        }
        updateUserCustomPlayerData(act, playerWatchList, playerNotes)
    }

    fun getPlayerNote(playerId: String): String {
        return if (playerNotes.containsKey(playerId)) {
            playerNotes[playerId]!!
        } else ""
    }

    fun getLeagueSettings(): LeagueSettings {
        return userLeagues!!.currentLeague
    }

    fun getUserLeagues(): UserLeagues? {
        return userLeagues
    }

    fun getPlayer(id: String): Player {
        return Companion.players[id]!!
    }

    var orderedIds: MutableList<String>
        get() = Companion.orderedIds
        set(orderedIds){ Companion.orderedIds = orderedIds }

    val draft: Draft
        get() = Companion.draft
    val qbs: MutableList<Player>
        get() {
            val pos: MutableList<Player> = ArrayList()
            for (key in Companion.players.keys) {
                val player = getPlayer(key)
                if (player.position == Constants.QB) {
                    pos.add(Companion.players[key]!!)
                }
            }
            return pos
        }
    val rbs: MutableList<Player>
        get() {
            val pos: MutableList<Player> = ArrayList()
            for (key in Companion.players.keys) {
                val player = getPlayer(key)
                if (player.position == Constants.RB) {
                    pos.add(Companion.players[key]!!)
                }
            }
            return pos
        }
    val wrs: MutableList<Player>
        get() {
            val pos: MutableList<Player> = ArrayList()
            for (key in Companion.players.keys) {
                val player = getPlayer(key)
                if (player.position == Constants.WR) {
                    pos.add(Companion.players[key]!!)
                }
            }
            return pos
        }
    val tes: MutableList<Player>
        get() {
            val pos: MutableList<Player> = ArrayList()
            for (key in Companion.players.keys) {
                val player = getPlayer(key)
                if (player.position == Constants.TE) {
                    pos.add(Companion.players[key]!!)
                }
            }
            return pos
        }
    val dsts: MutableList<Player>
        get() {
            val pos: MutableList<Player> = ArrayList()
            for (key in Companion.players.keys) {
                val player = getPlayer(key)
                if (player.position == Constants.DST) {
                    pos.add(Companion.players[key]!!)
                }
            }
            return pos
        }
    val ks: MutableList<Player>
        get() {
            val pos: MutableList<Player> = ArrayList()
            for (key in Companion.players.keys) {
                val player = getPlayer(key)
                if (player.position == Constants.K) {
                    pos.add(Companion.players[key]!!)
                }
            }
            return pos
        }
    val players: MutableMap<String, Player>
        get() = Companion.players
    val teams: Map<String, Team>
        get() = Companion.teams
    val playerProjectionHistory: MutableMap<String, MutableList<DailyProjection>>
        get() = Companion.playerProjectionHistory


    fun getTeam(player: Player): Team? {
        return getTeam(player.teamName)
    }

    fun getTeam(teamName: String): Team? {
        return Companion.teams[teamName]
    }

    fun addTeam(team: Team) {
        if (Companion.teams[team.name] == null) {
            team.name = normalizeTeams(team.name)
            Companion.teams[team.name] = team
        }
    }

    fun clearRankings() {
        Companion.players.clear()
        Companion.teams.clear()
        Companion.orderedIds.clear()
    }

    fun refreshRankings(activity: RankingsHome) {
        ParsingUtils.init()
        val ranksParser = RanksAggregator(activity, this)
        ranksParser.execute()
    }

    fun updateProjectionsAndVBD(activity: Activity, league: LeagueSettings, updateProjections: Boolean,
                                rankingsDB: RankingsDBWrapper) {
        val vbdUpdater = VBDUpdater(this, activity, league, updateProjections, rankingsDB)
        vbdUpdater.execute()
    }

    fun saveRankings(activity: RankingsHome?, rankingsDB: RankingsDBWrapper) {
        val ranksSaver = RanksSaver(activity!!, rankingsDB)
        ranksSaver.execute(Companion.players, Companion.teams)
    }

    fun dedupPlayer(fake: Player, real: Player) {
        Companion.players.remove(fake.uniqueId)
        Companion.orderedIds.remove(fake.uniqueId)
        real.handleNewValue(fake.auctionValue)
    }

    fun processNewPlayer(player: Player) {
        if (!Companion.players.containsKey(player.uniqueId)) {
            Companion.players[player.uniqueId] = normalizePlayerFields(player)
        } else {
            val existingPlayer = Companion.players[player.uniqueId]
            Companion.players[player.uniqueId] = conditionallyAddContext(existingPlayer!!, player)
        }
    }

    fun getPlayersByTeam(source: List<String>, team: String): MutableList<String> {
        val idsOnTeam: MutableList<String> = ArrayList()
        for (key in source) {
            val player = Companion.players[key]
            if (player != null && player.teamName == team) {
                idsOnTeam.add(key)
            }
        }
        return idsOnTeam
    }

    fun getWatchedPlayers(source: List<String?>): MutableList<String> {
        val overlap: MutableList<String> = ArrayList()
        for (id in playerWatchList) {
            if (source.contains(id)) {
                overlap.add(id)
            }
        }
        return overlap
    }

    fun getPlayersByPosition(source: List<String>, position: String): MutableList<String> {
        val positions: MutableSet<String> = HashSet()
        when (position) {
            Constants.RBWR -> {
                positions.add(Constants.RB)
                positions.add(Constants.WR)
            }
            Constants.RBTE -> {
                positions.add(Constants.RB)
                positions.add(Constants.TE)
            }
            Constants.RBWRTE -> {
                positions.add(Constants.RB)
                positions.add(Constants.WR)
                positions.add(Constants.TE)
            }
            Constants.WRTE -> {
                positions.add(Constants.WR)
                positions.add(Constants.TE)
            }
            Constants.QBRBWRTE -> {
                positions.add(Constants.QB)
                positions.add(Constants.RB)
                positions.add(Constants.WR)
                positions.add(Constants.TE)
            }
            else -> positions.add(position)
        }
        return getPlayersByPositionInternal(source, positions)
    }

    private fun getPlayersByPositionInternal(source: List<String>, positions: Set<String>): MutableList<String> {
        val idsByPos: MutableList<String> = ArrayList()
        for (key in source) {
            val player = Companion.players[key]
            if (player != null && positions.contains(player.position)) {
                idsByPos.add(key)
            }
        }
        return idsByPos
    }

    fun orderPlayersByLeagueType(players: Collection<Player>): MutableList<String> {
        val orderedIds: MutableList<String> = ArrayList()
        val comparator: Comparator<Player>
        val leagueSettings = getLeagueSettings()
        comparator = when {
            leagueSettings.isAuction -> {
                Comparator { a: Player, b: Player -> b.auctionValue.compareTo(a.auctionValue) }
            }
            leagueSettings.isDynasty -> {
                Comparator { a: Player, b: Player -> a.dynastyRank.compareTo(b.dynastyRank) }
            }
            leagueSettings.isRookie -> {
                Comparator { a: Player, b: Player -> a.rookieRank.compareTo(b.rookieRank) }
            }
            leagueSettings.isBestBall -> {
                Comparator { a: Player, b: Player -> a.bestBallRank.compareTo(b.bestBallRank) }
            }
            else -> {
                Comparator { a: Player, b: Player -> a.ecr.compareTo(b.ecr) }
            }
        }
        val playerList: List<Player> = ArrayList(players)
        Collections.sort(playerList, comparator)
        for (player in playerList) {
            orderedIds.add(player.uniqueId)
        }
        return orderedIds
    }

    companion object {
        private var players: MutableMap<String, Player> = HashMap()
        private var teams: MutableMap<String, Team> = HashMap()
        private var orderedIds: MutableList<String> = ArrayList()
        private var draft: Draft = Draft()
        private var userLeagues: UserLeagues? = null
        private var loader: RankingsLoader = RankingsLoader()
        private var playerProjectionHistory: MutableMap<String, MutableList<DailyProjection>> = HashMap()

        // AppSync stuff
        private var userSettings = UserSettings()
        private var playerWatchList: MutableList<String> = ArrayList()
        private var playerNotes: MutableMap<String, String> = HashMap()
        fun init(): Rankings {
            if (userLeagues == null) {
                userLeagues = UserLeagues(HashMap())
            }
            return Rankings()
        }

        fun initWithDefaults(userLeagues: UserLeagues): Rankings {
            return init(HashMap(), HashMap(), ArrayList(), userLeagues,
                    Draft(), HashMap())
        }

        fun init(inputTeams: MutableMap<String, Team>, inputPlayers: MutableMap<String, Player>, inputIds: MutableList<String>,
                 inputLeagues: UserLeagues, inputDraft: Draft, inputProjectionHistory: MutableMap<String, MutableList<DailyProjection>>): Rankings {
            players = inputPlayers
            teams = inputTeams
            userLeagues = inputLeagues
            loader = RankingsLoader()
            orderedIds = inputIds
            draft = inputDraft
            playerProjectionHistory = inputProjectionHistory
            return Rankings()
        }

        fun setUserSettings(settings: UserSettings) {
            userSettings = settings
        }

        fun setCustomUserData(watchList: MutableList<String>, notes: MutableMap<String, String>) {
            playerWatchList = watchList
            playerNotes = notes
        }

        fun loadRankings(activity: RankingsHome, rankingsDB: RankingsDBWrapper) {
            val ranksLoader = RanksLoader(activity, rankingsDB)
            ranksLoader.execute()
        }

        fun loadLeagues(activity: RankingsHome, rankingsDB: RankingsDBWrapper) {
            val leaguesLoader = LeaguesLoader(activity, rankingsDB)
            leaguesLoader.execute()
        }
    }
}