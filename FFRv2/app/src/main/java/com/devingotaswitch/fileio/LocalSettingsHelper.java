package com.devingotaswitch.fileio;

import android.content.Context;
import android.content.SharedPreferences;

import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.utils.Constants;

public class LocalSettingsHelper {

    public static boolean wasPresent(String value) {
        return !Constants.NOT_SET_KEY.equals(value);
    }

    private static SharedPreferences getSharedPreferences(Context cont) {
        return cont.getSharedPreferences(Constants.APP_KEY, Context.MODE_PRIVATE);
    }

    public static String getCurrentLeagueName(Context cont) {
        return getSharedPreferences(cont).getString(Constants.LEAGUE_NAME, Constants.NOT_SET_KEY);
    }

    public static void saveCurrentLeagueName(Context cont, String name) {
        getSharedPreferences(cont).edit().putString(Constants.LEAGUE_NAME, name).apply();
    }

    public static int getNumVisiblePlayers(Context cont) {
        return getSharedPreferences(cont).getInt(Constants.NUM_PLAYERS, Constants.DEFAULT_NUM_PLAYERS);
    }

    public static void saveNumVisiblePlayers(Context cont, int numVisible) {
        getSharedPreferences(cont).edit().putInt(Constants.NUM_PLAYERS, numVisible).apply();
    }

    public static boolean wereRankingsFetched(Context cont) {
        return getSharedPreferences(cont).getBoolean(Constants.RANKINGS_FETCHED, Constants.NOT_SET_BOOLEAN);
    }

    public static void saveRankingsFetched(Context cont, boolean wereFetched) {
        getSharedPreferences(cont).edit().putBoolean(Constants.RANKINGS_FETCHED, wereFetched).apply();
    }
}
