package com.devingotaswitch.rankings.domain

import android.app.Activity
import android.content.Context
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.appsync.AppSyncHelper.decrementPlayerDraftCount
import com.devingotaswitch.appsync.AppSyncHelper.incrementPlayerDraftCount
import com.devingotaswitch.fileio.LocalSettingsHelper
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.FlashbarFactory.generateFlashbarWithUndo
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import java.text.DecimalFormat
import java.util.*

class Draft {
    val draftedPlayers: MutableList<String>
    val myPlayers: MutableMap<String, Int>
    val myQbs: MutableList<Player>
    val myRbs: MutableList<Player>
    val myWrs: MutableList<Player>
    val myTes: MutableList<Player>
    val myDsts: MutableList<Player>
    val myKs: MutableList<Player>
    var draftValue: Double
        private set

    fun getPlayersDraftedForPos(position: String?): List<Player> {
        when (position) {
            Constants.QB -> return myQbs
            Constants.RB -> return myRbs
            Constants.WR -> return myWrs
            Constants.TE -> return myTes
            Constants.DST -> return myDsts
            Constants.K -> return myKs
        }
        return ArrayList()
    }

    fun getPlayersWithSameByeAndPos(player: Player, rankings: Rankings): List<Player> {
        return getPlayersWithSameBye(getPlayersDraftedForPos(player.position),
                rankings, rankings.getTeam(player)!!.bye)
    }

    fun getPlayersWithSameBye(player: Player, rankings: Rankings): List<Player> {
        val allMyPicks: MutableList<Player> = ArrayList()
        allMyPicks.addAll(myQbs)
        allMyPicks.addAll(myRbs)
        allMyPicks.addAll(myWrs)
        allMyPicks.addAll(myTes)
        allMyPicks.addAll(myDsts)
        allMyPicks.addAll(myKs)
        return getPlayersWithSameBye(allMyPicks, rankings, rankings.getTeam(player)!!.bye)
    }

    private fun getPlayersWithSameBye(toCheck: List<Player>, rankings: Rankings, bye: String?): List<Player> {
        val sameBye: MutableList<Player> = ArrayList()
        for (posPlayer in toCheck) {
            if (bye == rankings.getTeam(posPlayer)?.bye) {
                sameBye.add(posPlayer)
            }
        }
        return sameBye
    }

    val totalPAA: Double
        get() = qBPAA + rBPAA + wRPAA + tEPAA + dstpaa + kpaa
    val totalXVal: Double
        get() = qBXval + rBXval + wRXval + tEXval + dstXval + kXval
    val totalVoLS: Double
        get() = qBVoLS + rBVoLS + wRVoLS + tEVoLS + dstVoLS + kVoLS
    val qBXval: Double
        get() = getXValForPos(myQbs)
    val wRXval: Double
        get() = getXValForPos(myWrs)
    val rBXval: Double
        get() = getXValForPos(myRbs)
    val tEXval: Double
        get() = getXValForPos(myTes)
    val dstXval: Double
        get() = getXValForPos(myDsts)
    val kXval: Double
        get() = getXValForPos(myKs)

    private fun getXValForPos(players: List<Player>): Double {
        var posXval = 0.0
        for (player in players) {
            posXval += player.xval
        }
        return posXval
    }

    val qBVoLS: Double
        get() = getVoLSForPos(myQbs)
    val rBVoLS: Double
        get() = getVoLSForPos(myRbs)
    val wRVoLS: Double
        get() = getVoLSForPos(myWrs)
    val tEVoLS: Double
        get() = getVoLSForPos(myTes)
    val dstVoLS: Double
        get() = getVoLSForPos(myDsts)
    val kVoLS: Double
        get() = getVoLSForPos(myKs)

    private fun getVoLSForPos(players: List<Player>): Double {
        var posVoLS = 0.0
        for (player in players) {
            posVoLS += player.vols
        }
        return posVoLS
    }

    val qBPAA: Double
        get() = getPAAForPos(myQbs)
    val wRPAA: Double
        get() = getPAAForPos(myWrs)
    val rBPAA: Double
        get() = getPAAForPos(myRbs)
    val tEPAA: Double
        get() = getPAAForPos(myTes)
    val dstpaa: Double
        get() = getPAAForPos(myDsts)
    val kpaa: Double
        get() = getPAAForPos(myKs)

    private fun getPAAForPos(players: List<Player>): Double {
        var posPAA = 0.0
        for (player in players) {
            posPAA += player.paa
        }
        return posPAA
    }

    fun draftPlayer(player: Player, teamCount: Int, auctionBudget: Int, myPick: Boolean, cost: Int) {
        draftedPlayers.add(player.uniqueId)
        if (myPick) {
            myPlayers[player.uniqueId] = cost
            draftValue += player.getAuctionValueCustom(teamCount, auctionBudget) - cost.toDouble()
            when (player.position) {
                Constants.QB -> myQbs.add(player)
                Constants.RB -> myRbs.add(player)
                Constants.WR -> myWrs.add(player)
                Constants.TE -> myTes.add(player)
                Constants.DST -> myDsts.add(player)
                Constants.K -> myKs.add(player)
            }
        }
    }

    private fun unDraftPlayer(player: Player, rankings: Rankings) {
        draftedPlayers.remove(player.uniqueId)
        if (isDraftedByMe(player)) {
            val cost = myPlayers[player.uniqueId]!!
            myPlayers.remove(player.uniqueId)
            draftValue -= player.getAuctionValueCustom(rankings) - cost.toDouble()
            when (player.position) {
                Constants.QB -> myQbs.remove(player)
                Constants.RB -> myRbs.remove(player)
                Constants.WR -> myWrs.remove(player)
                Constants.TE -> myTes.remove(player)
                Constants.DST -> myDsts.remove(player)
                Constants.K -> myKs.remove(player)
            }
        }
    }

    fun isDraftedByMe(player: Player): Boolean {
        return myPlayers.containsKey(player.uniqueId)
    }

    fun isDrafted(player: Player): Boolean {
        return draftedPlayers.contains(player.uniqueId)
    }

    fun draftedToSerializedString(): String {
        val draftedStr = StringBuilder()
        for (key in draftedPlayers) {
            draftedStr.append(key)
                    .append(Constants.HASH_DELIMITER)
        }
        return draftedStr.toString()
    }

    fun myTeamToSerializedString(): String {
        val myTeamStr = StringBuilder()
        for (key in myPlayers.keys) {
            val cost: Int = myPlayers[key]!!
            myTeamStr.append(key)
                    .append(Constants.HASH_DELIMITER)
                    .append(cost)
                    .append(Constants.HASH_DELIMITER)
        }
        return myTeamStr.toString()
    }

    fun getSortedAvailablePlayersForPosition(pos: String, rankings: Rankings): List<Player> {
        val comparator = Comparator { a: Player, b: Player -> b.projection.compareTo(a.projection) }
        val players: MutableList<Player> = ArrayList()
        for (player in rankings.players.values) {
            if (!isDrafted(player) && player.position == pos) {
                players.add(player)
            }
        }
        Collections.sort(players, comparator)
        return players
    }

    fun getPAANAvailablePlayersBack(players: List<Player>, limit: Int): Double {
        var counter = 0
        var paaLeft = 0.0
        for (player in players) {
            paaLeft += player.paa
            counter++
            if (counter == limit) {
                break
            }
        }
        return paaLeft
    }

    fun getPAALeft(pos: String, rankings: Rankings): String {
        val df = DecimalFormat("#.#")
        var result = pos + "s: "
        val players = getSortedAvailablePlayersForPosition(pos, rankings)
        result += df.format(getPAANAvailablePlayersBack(players, 3)) + "/"
        result += df.format(getPAANAvailablePlayersBack(players, 5)) + "/"
        result += df.format(getPAANAvailablePlayersBack(players, 10))
        if (result.endsWith("/")) {
            result = result.substring(0, result.length - 1)
        }
        return result
    }

    fun resetDraft(context: Context, leagueName: String) {
        myQbs.clear()
        myRbs.clear()
        myWrs.clear()
        myTes.clear()
        myDsts.clear()
        myKs.clear()
        myPlayers.clear()
        draftedPlayers.clear()
        draftValue = 0.0
        LocalSettingsHelper.clearDraft(context, leagueName)
    }

    fun draftBySomeone(rankings: Rankings, player: Player, act: Activity, listener: OnActionTapListener?) {
        draftPlayer(player, rankings.getLeagueSettings().teamCount, rankings.getLeagueSettings().auctionBudget, false, 0)
        if (listener == null) {
            generateTextOnlyFlashbar(act, "Success!", player.name + " drafted", Flashbar.Gravity.BOTTOM)
                    .show()
        } else {
            generateFlashbarWithUndo(act, "Success!", player.name + " drafted", Flashbar.Gravity.BOTTOM, listener)
                    .show()
        }
        saveDraft(rankings, act)
        incrementPlayerDraftCount(act, player.uniqueId)
    }

    fun draftByMe(rankings: Rankings, player: Player, act: Activity, cost: Int, listener: OnActionTapListener?) {
        draftPlayer(player, rankings.getLeagueSettings().teamCount, rankings.getLeagueSettings().auctionBudget, true, cost)
        if (listener == null) {
            generateTextOnlyFlashbar(act, "Success!", player.name + " drafted by you", Flashbar.Gravity.BOTTOM)
                    .show()
        } else {
            generateFlashbarWithUndo(act, "Success!", player.name + " drafted by you", Flashbar.Gravity.BOTTOM, listener)
                    .show()
        }
        saveDraft(rankings, act)
        incrementPlayerDraftCount(act, player.uniqueId)
    }

    fun undraft(rankings: Rankings, player: Player, act: Activity) {
        unDraftPlayer(player, rankings)
        generateTextOnlyFlashbar(act, "Success!", player.name + " undrafted", Flashbar.Gravity.BOTTOM)
                .show()
        saveDraft(rankings, act)
        decrementPlayerDraftCount(act, player.uniqueId)
    }

    private fun saveDraft(rankings: Rankings, act: Activity) {
        LocalSettingsHelper.saveDraft(act, rankings.getLeagueSettings().name, rankings.draft)
    }

    init {
        draftedPlayers = ArrayList()
        myQbs = ArrayList()
        myRbs = ArrayList()
        myWrs = ArrayList()
        myTes = ArrayList()
        myDsts = ArrayList()
        myKs = ArrayList()
        myPlayers = HashMap()
        draftValue = 0.0
    }
}