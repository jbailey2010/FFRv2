package com.devingotaswitch.fileio;

import android.content.Context;
import android.content.SharedPreferences;

import com.devingotaswitch.utils.Constants;

public class LocalSettingsHelper {

    public static boolean wasPresent(String value) {
        return Constants.NOT_SET_KEY.equals(value);
    }

    private static SharedPreferences getSharedPreferences(Context cont) {
        return cont.getSharedPreferences(Constants.APP_KEY, Context.MODE_PRIVATE);
    }

    public static String getCurrentLeagueId(Context cont) {
        return getSharedPreferences(cont).getString(Constants.LEAGUE_ID, Constants.NOT_SET_KEY);
    }

    public static void saveCurrentLeagueId(Context cont, String id) {
        getSharedPreferences(cont).edit().putString(Constants.LEAGUE_ID, id).apply();
    }

    public static int getNumVisiblePlayers(Context cont) {
        return getSharedPreferences(cont).getInt(Constants.NUM_PLAYERS, Constants.DEFAULT_NUM_PLAYERS);
    }

    public static void saveNumVisiblePlayers(Context cont, int numVisible) {
        getSharedPreferences(cont).edit().putInt(Constants.NUM_PLAYERS, numVisible).apply();
    }
}
