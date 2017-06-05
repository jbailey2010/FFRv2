package com.devingotaswitch.fileio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.DBUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public void saveTeams(Context context, Set<Team> teams) {
        Set<ContentValues> values = new HashSet<>();
        for (Team team : teams) {
            values.add(DBUtils.teamToContentValues(team));
        }
        emptyTableAndBulkSave(context, Constants.TEAM_TABLE_NAME, values);
    }

    public RosterSettings getRoster(Context context, String rosterId) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor result = db.rawQuery(DBUtils.getSelectSingleString(Constants.ROSTER_TABLE_NAME,
                Constants.ROSTER_ID_COLUMN, rosterId), null);
        result.moveToFirst();
        RosterSettings roster = DBUtils.cursorToRoster(result);
        result.close();
        return roster;
    }

    public void insertRoster(Context context, RosterSettings roster) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        db.insert(Constants.ROSTER_TABLE_NAME, null, DBUtils.rosterToContentValues(roster));
    }

    public void updateRoster(Context context, String id, Map<String, String> updatedFields) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        db.update(Constants.ROSTER_TABLE_NAME,
                DBUtils.updatedValuesToContentValues(updatedFields),
                DBUtils.getUpdateKeyString(Constants.ROSTER_ID_COLUMN),
                new String[] {id});
    }

    private void emptyTableAndBulkSave(Context context, String tableName, Set<ContentValues> valuesToInsert) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        deleteItemsInTable(db, tableName);
        db.beginTransaction();
        try {
            for(ContentValues values : valuesToInsert) {
                db.insert(tableName, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private int getNumberOfRowsInTable(SQLiteDatabase db, String tableName) {
        return (int) DatabaseUtils.queryNumEntries(db, tableName);
    }

    private void deleteItemsInTable(SQLiteDatabase db, String tableName) {
        db.execSQL(DBUtils.getDeleteAllString(tableName));
    }
}
