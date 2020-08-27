package com.devingotaswitch.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.amazonaws.util.StringUtils
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter
import java.util.*

object GeneralUtils {
    private const val SECONDS_CONVERSION_THRESHOLD = 1000L
    @JvmStatic
    fun confirmInternet(cont: Context): Boolean {
        val connectivityManager = cont
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager
                .activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun sortData(
            data: MutableList<String>): MutableList<String> {
        data.sortWith(Comparator { a: String, b: String ->
            val aName = a.split(Constants.RANKINGS_LIST_DELIMITER).toTypedArray()[1].split(Constants.LINE_BREAK).toTypedArray()[0]
            val bName = b.split(Constants.RANKINGS_LIST_DELIMITER).toTypedArray()[1].split(Constants.LINE_BREAK).toTypedArray()[0]
            val judgment = aName.compareTo(bName)
            judgment.compareTo(0)
        })
        return data
    }

    @JvmStatic
    fun isInteger(s: String): Boolean {
        try {
            s.toInt()
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

    @JvmStatic
    fun isDouble(s: String): Boolean {
        try {
            s.toDouble()
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

    @JvmStatic
    fun getLatency(start: Long): Long {
        return (System.currentTimeMillis() - start) / SECONDS_CONVERSION_THRESHOLD
    }

    @JvmStatic
    fun getPlayerSearchAdapter(rankings: Rankings, activity: Activity, hideDrafted: Boolean,
                               hideRankless: Boolean): FilterWithSpaceAdapter<String> {
        val dropdownList: MutableList<String> = ArrayList()
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            val prefix = player.getDisplayValue(rankings)
            if (rankings.draft.isDrafted(player) && hideDrafted ||
                    hideRankless && Constants.DEFAULT_DISPLAY_RANK_NOT_SET == player.getDisplayValue(rankings)) {
                continue
            }
            if (rankings.leagueSettings.rosterSettings.isPositionValid(player.position) &&
                    !StringUtils.isBlank(player.teamName) && player.teamName.length > 3) {
                val dropdownStr = prefix +
                        Constants.RANKINGS_LIST_DELIMITER +
                        player.name +
                        Constants.LINE_BREAK +
                        player.position +
                        Constants.POS_TEAM_DELIMITER +
                        player.teamName
                dropdownList.add(dropdownStr)
            }
        }
        val dataSorted = sortData(dropdownList)
        return FilterWithSpaceAdapter(activity,
                R.layout.dropdown_item, R.id.dropdown_text, dataSorted)
    }

    @JvmStatic
    fun getPlayerIdFromSearchView(view: View): String {
        val fullStr = (view.findViewById<View>(R.id.dropdown_text) as TextView).text.toString().split(Constants.RANKINGS_LIST_DELIMITER).toTypedArray()[1]
        val playerArr = fullStr.split(Constants.LINE_BREAK).toTypedArray()
        val posAndTeam = playerArr[1].split(Constants.POS_TEAM_DELIMITER).toTypedArray()
        val name = playerArr[0]
        val pos = posAndTeam[0]
        val team = posAndTeam[1]
        return name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}