package com.devingotaswitch.fileio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.DBUtils;

import java.util.HashMap;
import java.util.Map;

public class RankingsDBWrapper {
    private static RankingsDBHelper rankingsDB;

    private synchronized RankingsDBHelper getInstance(Context context) {
        if (rankingsDB == null) {
            rankingsDB = new RankingsDBHelper(context);
        }
        return rankingsDB;
    }

    public Map<String, Team> getAllTeams(Context context) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Map<String, Team> teams = new HashMap<>();

        Cursor results =  db.rawQuery(DBUtils.getSelectAllString(Constants.TEAM_TABLE_NAME), null);
        results.moveToFirst();
        while(results.isAfterLast() == false){
            Team team = DBUtils.cursorToTeam(results);
            teams.put(team.getName(), team);
            results.moveToNext();
        }
        results.close();
        return teams;
    }

    public Team getTeam(Context context, String teamName) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor result = db.rawQuery(DBUtils.getSelectSingleString(Constants.TEAM_TABLE_NAME,
                Constants.TEAM_NAME_COLUMN, teamName), null);
        result.moveToFirst();
        Team team = DBUtils.cursorToTeam(result);
        result.close();
        return team;
    }
}
