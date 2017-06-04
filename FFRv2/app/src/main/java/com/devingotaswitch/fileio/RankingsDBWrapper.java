package com.devingotaswitch.fileio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;

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

    private String getSelectAllString(String tableName) {
        return new StringBuilder("SELECT * FROM ")
                .append(tableName)
                .toString();
    }

    private String getSelectSingleString(String tableName, String idColumn, String idValue) {
        return new StringBuilder("SELECT * FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(idColumn)
                .append(" = ")
                .append(idValue)
                .toString();
    }

    public Map<String, Team> getAllTeams(Context context) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Map<String, Team> teams = new HashMap<>();

        Cursor results =  db.rawQuery(getSelectAllString(Constants.TEAM_TABLE_NAME), null);
        results.moveToFirst();
        while(results.isAfterLast() == false){
            Team team = cursorToTeam(results);
            teams.put(team.getName(), team);
            results.moveToNext();
        }
        results.close();
        return teams;
    }

    public Team getTeam(Context context, String teamName) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor result = db.rawQuery(getSelectSingleString(Constants.TEAM_TABLE_NAME, Constants.TEAM_NAME_COLUMN, teamName), null);
        result.moveToFirst();
        return cursorToTeam(result);
    }

    private Team cursorToTeam(Cursor results) {
        Team team = new Team();
        team.setName(results.getString(results.getColumnIndex(Constants.TEAM_NAME_COLUMN)));
        team.setoLineRanks(results.getString(results.getColumnIndex(Constants.OLINE_RANKS_COLUMN)));
        team.setDraftClass(results.getString(results.getColumnIndex(Constants.DRAFT_CLASS_COLUMN)));
        team.setQbSos(results.getInt(results.getColumnIndex(Constants.QB_SOS_COLUMN)));
        team.setRbSos(results.getInt(results.getColumnIndex(Constants.RB_SOS_COLUMN)));
        team.setWrSos(results.getInt(results.getColumnIndex(Constants.WR_SOS_COLUMN)));
        team.setTeSos(results.getInt(results.getColumnIndex(Constants.TE_SOS_COLUMN)));
        team.setDstSos(results.getInt(results.getColumnIndex(Constants.DST_SOS_COLUMN)));
        team.setkSos(results.getInt(results.getColumnIndex(Constants.K_SOS_COLUMN)));
        team.setBye(results.getString(results.getColumnIndex(Constants.BYE_COLUMN)));
        team.setFaClass(results.getString(results.getColumnIndex(Constants.FREE_AGENCY_COLUMN)));
        return team;
    }
}
