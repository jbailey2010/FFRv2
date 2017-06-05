package com.devingotaswitch.fileio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
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

    //---------- Teams ---------- 

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
        Cursor result = getEntry(db, teamName, Constants.TEAM_TABLE_NAME, Constants.TEAM_NAME_COLUMN);
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

    //---------- Rosters ----------

    public RosterSettings getRoster(SQLiteDatabase db, String rosterId) {
        Cursor result = getEntry(db, rosterId, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN);
        RosterSettings roster = DBUtils.cursorToRoster(result);
        result.close();
        return roster;
    }

    public void insertRoster(SQLiteDatabase db, RosterSettings roster) {
        insertEntry(db, DBUtils.rosterToContentValues(roster), Constants.ROSTER_TABLE_NAME);
    }

    public void updateRoster(SQLiteDatabase db, String id, Map<String, String> updatedFields) {
        updateEntry(db, id, updatedFields, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN);
    }

    public void deleteRoster(SQLiteDatabase db, String id) {
        deleteEntry(db, id, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN);
    }

    //---------- Scoring ----------

    public ScoringSettings getScoring(SQLiteDatabase db, String scoringId) {
        Cursor result = getEntry(db, scoringId, Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN);
        ScoringSettings scoring = DBUtils.cursorToScoring(result);
        result.close();
        return scoring;
    }

    public void insertScoring(SQLiteDatabase db, ScoringSettings scoring) {
        insertEntry(db, DBUtils.scoringToContentValues(scoring), Constants.SCORING_TABLE_NAME);
    }

    public void updateScoring(SQLiteDatabase db, String id, Map<String, String> updatedFields) {
        updateEntry(db, id, updatedFields, Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN);
    }

    public void deleteScoring(SQLiteDatabase db, String id) {
        deleteEntry(db, id, Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN);
    }

    //---------- Utilities ----------

    private Cursor getEntry(SQLiteDatabase db, String id, String tableName, String idColumn) {
        Cursor result = db.rawQuery(DBUtils.getSelectSingleString(tableName,
                idColumn, id), null);
        result.moveToFirst();
        return result;
    }

    private void insertEntry(SQLiteDatabase db, ContentValues values, String tableName) {
        db.insert(tableName, null, values);
    }

    private void updateEntry(SQLiteDatabase db, String id, Map<String, String> updatedFields, String tableName, String idColumn) {
        db.update(tableName, DBUtils.updatedValuesToContentValues(updatedFields),
                DBUtils.getUpdateAndDeleteKeyString(idColumn), new String[] {id});
    }

    private void deleteEntry(SQLiteDatabase db, String id, String tableName, String idColumn) {
        db.delete(tableName, DBUtils.getUpdateAndDeleteKeyString(idColumn), new String[] {id});
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
