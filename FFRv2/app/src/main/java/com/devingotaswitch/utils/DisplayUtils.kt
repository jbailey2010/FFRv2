package com.devingotaswitch.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import com.amazonaws.util.StringUtils
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter
import java.util.*

object DisplayUtils {
    private fun generateOutputSubtext(player: Player, rankings: Rankings, posSuffix: String,
                                      showNote: Boolean): String {
        var sub = StringBuilder(player.position)
                .append(posSuffix)
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.teamName)
        val team = rankings.getTeam(player)
        if (Constants.NO_TEAM != player.teamName) {
            if (team != null) {
                sub = sub.append(" (Bye: ")
                        .append(team.bye)
                        .append(")")
            }
            sub = sub.append(Constants.LINE_BREAK)
                    .append("Projection: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.projection))
        } else {
            // An empty line if there's no team so sizing doesn't get wonky as players are drafted.
            sub = sub.append(Constants.LINE_BREAK)
        }
        if (showNote && !StringUtils.isBlank(rankings.getPlayerNote(player.uniqueId))) {
            sub = sub.append(Constants.LINE_BREAK)
                    .append(rankings.getPlayerNote(player.uniqueId))
        } else if (showNote) {
            // An empty line if there's no team so sizing doesn't get wonky as players are drafted.
            sub = sub.append(Constants.LINE_BREAK)
        }
        return sub.toString()
    }

    @JvmStatic
    fun getPlayerKeyFromListViewItem(view: View): String {
        val playerMain = view.findViewById<TextView>(R.id.player_basic)
        val playerInfo = view.findViewById<TextView>(R.id.player_info)
        val name = playerMain.text.toString().split(Constants.RANKINGS_LIST_DELIMITER)[1]
        val teamPosBye = playerInfo.text.toString().split(Constants.LINE_BREAK)[0]
        val teamPos = teamPosBye.split(" (")[0]
        val team = teamPos.split(Constants.POS_TEAM_DELIMITER)[1]
        val pos = teamPos.split(Constants.POS_TEAM_DELIMITER)[0].replace("\\d".toRegex(), "")
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos
    }

    private val positionRankMap: MutableMap<String, Int>
        get() {
            val positionRankMap: MutableMap<String, Int> = HashMap()
            positionRankMap[Constants.QB] = 1
            positionRankMap[Constants.RB] = 1
            positionRankMap[Constants.WR] = 1
            positionRankMap[Constants.TE] = 1
            positionRankMap[Constants.DST] = 1
            positionRankMap[Constants.K] = 1
            return positionRankMap
        }

    @JvmStatic
    fun getPositionalRank(orderedIds: List<String>, rankings: Rankings): Map<String, Int?> {
        // First, pre-process the ordered ids to get personal ranks
        val positionalRanks = positionRankMap
        val playerRanks: MutableMap<String, Int?> = HashMap()
        for (id in orderedIds) {
            val player = rankings.getPlayer(id)
            playerRanks[player.uniqueId] = positionalRanks[player.position]
            positionalRanks[player.position] = positionalRanks[player.position]!! + 1
        }
        return playerRanks
    }

    @JvmStatic
    fun getDatumForPlayer(rankings: Rankings, player: Player, markWatched: Boolean,
                          posRank: Int, showNote: Boolean): MutableMap<String, String?> {
        val posSuffix: String = posRank.toString()
        val playerBasicContent = player.getDisplayValue(rankings) +
                Constants.RANKINGS_LIST_DELIMITER +
                player.name
        val datum: MutableMap<String, String?> = HashMap(5)
        datum[Constants.PLAYER_BASIC] = playerBasicContent
        datum[Constants.PLAYER_INFO] = generateOutputSubtext(player, rankings, posSuffix, showNote)
        if (markWatched && rankings.isPlayerWatched(player.uniqueId)) {
            datum[Constants.PLAYER_STATUS] = R.drawable.star.toString()
        }
        if (player.age != null && Constants.DST != player.position && player.age > 0) {
            datum[Constants.PLAYER_ADDITIONAL_INFO] = "Age: " + player.age
        }
        if (player.experience != null && player.experience >= 0 && Constants.DST != player.position) {
            datum[Constants.PLAYER_ADDITIONAL_INFO_2] = "Exp: " + player.experience
        }
        return datum
    }

    @JvmStatic
    fun getDisplayAdapter(act: Activity, data: MutableList<MutableMap<String, String?>>): RecyclerViewAdapter {
        return RecyclerViewAdapter(act, data,
                R.layout.list_item_layout, arrayOf(Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO,
                Constants.PLAYER_ADDITIONAL_INFO_2), intArrayOf(R.id.player_basic, R.id.player_info,
                R.id.player_status, R.id.player_more_info, R.id.player_additional_info_2))
    }

    @JvmStatic
    fun getVerticalDividerDecoration(context: Context?): DividerItemDecoration {
        return DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
    }
}