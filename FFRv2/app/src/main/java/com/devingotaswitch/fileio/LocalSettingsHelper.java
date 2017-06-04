package com.devingotaswitch.fileio;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalSettingsHelper {

    // SP constants
    private static final String APP_KEY = "FFRv2";
    private static final String LEAGUE_ID = "CURRENT_LEAGUE_ID";
    private static final String NUM_PLAYERS = "MAX_PLAYERS_VISIBLE";

    // Default values
    private static final String NOT_SET_KEY = "NOT_SAVED";
    private static final Integer DEFAULT_NUM_PLAYERS = 1000;

    public static boolean wasPresent(String value) {
        return NOT_SET_KEY.equals(value);
    }

    private static SharedPreferences getSharedPreferences(Context cont) {
        return cont.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE);
    }

    public static String getCurrentLeagueId(Context cont) {
        return getSharedPreferences(cont).getString(LEAGUE_ID, NOT_SET_KEY);
    }

    public static void saveCurrentLeagueId(Context cont, String id) {
        getSharedPreferences(cont).edit().putString(LEAGUE_ID, id).apply();
    }

    public static int getNumVisiblePlayers(Context cont) {
        return getSharedPreferences(cont).getInt(NUM_PLAYERS, DEFAULT_NUM_PLAYERS);
    }

    public static void saveNumVisiblePlayers(Context cont, int numVisible) {
        getSharedPreferences(cont).edit().putInt(NUM_PLAYERS, numVisible).apply();
    }
}
