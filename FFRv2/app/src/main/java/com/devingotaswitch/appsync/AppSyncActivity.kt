package com.devingotaswitch.appsync

import android.text.format.DateUtils
import androidx.appcompat.app.AppCompatActivity
import com.devingotaswitch.utils.Constants
import com.google.gson.Gson
import java.util.*

open class AppSyncActivity : AppCompatActivity() {
    fun getPlayerId(playerId: String): String {
        return playerId + Constants.PLAYER_ID_DELIMITER + Constants.YEAR_KEY
    }

    fun getPosFromPlayerId(playerId: String): String {
        val totalDots = playerId.split(("\\" + Constants.PLAYER_ID_DELIMITER).toRegex()).size - 1
        return playerId.split(("\\" + Constants.PLAYER_ID_DELIMITER).toRegex())[totalDots]
    }

    val currentTime: Long
        get() = Date().time

    fun formatCurrentTime(time: Long): String {
        return DateUtils.getRelativeTimeSpanString(time, currentTime, DateUtils.MINUTE_IN_MILLIS).toString()
    }

    companion object {
        @JvmField
        val GSON = Gson()
    }
}