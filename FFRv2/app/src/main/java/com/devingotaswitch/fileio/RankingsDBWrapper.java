package com.devingotaswitch.fileio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.DBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    //---------- Players ----------
    public void togglePlayerWatched(Context context, Player player, boolean isWatched) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.PLAYER_WATCHED_COLUMN, isWatched);
        updateMultipleKeyEntry(db, player.getName(), player.getPosition(), values, Constants.PLAYER_CUSTOM_TABLE_NAME, Constants.PLAYER_NAME_COLUMN, Constants.PLAYER_POSITION_COLUMN);
    }

    public void setPlayerNote(Context context, Player player, String note) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.PLAYER_NOTE_COLUMN, note);
        updateMultipleKeyEntry(db, player.getName(), player.getPosition(), values, Constants.PLAYER_CUSTOM_TABLE_NAME, Constants.PLAYER_NAME_COLUMN, Constants.PLAYER_POSITION_COLUMN);
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

    //---------- Leagues ----------

    public List<LeagueSettings> getLeagues(Context context) {
        List<LeagueSettings> leagues = new ArrayList<>();
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor result = db.rawQuery(DBUtils.getSelectAllString(Constants.LEAGUE_TABLE_NAME), null);
        result.moveToFirst();
        while(result.isAfterLast() == false) {
            leagues.add(cursorToFullLeague(db, result));
            result.moveToNext();
        }
        result.close();
        return leagues;
    }

    public LeagueSettings getLeague(Context context, String leagueId) {
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor result = getEntry(db, leagueId, Constants.LEAGUE_TABLE_NAME, Constants.LEAGUE_ID_COLUMN);
        LeagueSettings league = cursorToFullLeague(db, result);
        result.close();
        return league;
    }

    private LeagueSettings cursorToFullLeague(SQLiteDatabase db, Cursor result) {
        String rosterId = result.getString(result.getColumnIndex(Constants.ROSTER_ID_COLUMN));
        String scoringId = result.getString(result.getColumnIndex(Constants.SCORING_ID_COLUMN));
        LeagueSettings league = DBUtils.cursorToLeague(result, getRoster(db, rosterId), getScoring(db, scoringId));
        return league;
    }

    public void insertLeague(Context context, LeagueSettings league) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        insertRoster(db, league.getRosterSettings());
        insertScoring(db, league.getScoringSettings());
        insertEntry(db, DBUtils.leagueToContentValues(league), Constants.LEAGUE_TABLE_NAME);
    }

    public void updateLeague(Context context, Map<String, String> leagueUpdates, Map<String, String> rosterUpdates,
                             Map<String, String> scoringUpdates, LeagueSettings league) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        if (rosterUpdates != null && !rosterUpdates.isEmpty()) {
            updateRoster(db, league.getRosterSettings().getId(), rosterUpdates);
        }
        if (scoringUpdates != null && !scoringUpdates.isEmpty()) {
            updateScoring(db, league.getScoringSettings().getId(), scoringUpdates);
        }
        if (leagueUpdates != null && !leagueUpdates.isEmpty()) {
            updateEntry(db, league.getId(), DBUtils.updatedValuesToContentValues(leagueUpdates), Constants.LEAGUE_TABLE_NAME, Constants.LEAGUE_ID_COLUMN);
        }
    }

    public void deleteLeague(Context context, LeagueSettings league) {
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        deleteRoster(db, league.getRosterSettings().getId());
        deleteScoring(db, league.getScoringSettings().getId());
        deleteEntry(db, league.getId(), Constants.LEAGUE_TABLE_NAME, Constants.LEAGUE_ID_COLUMN);
    }

    //---------- Rosters ----------

    private RosterSettings getRoster(SQLiteDatabase db, String rosterId) {
        Cursor result = getEntry(db, rosterId, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN);
        RosterSettings roster = DBUtils.cursorToRoster(result);
        result.close();
        return roster;
    }

    private void insertRoster(SQLiteDatabase db, RosterSettings roster) {
        insertEntry(db, DBUtils.rosterToContentValues(roster), Constants.ROSTER_TABLE_NAME);
    }

    private void updateRoster(SQLiteDatabase db, String id, Map<String, String> updatedFields) {
        updateEntry(db, id, DBUtils.updatedValuesToContentValues(updatedFields), Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN);
    }

    private void deleteRoster(SQLiteDatabase db, String id) {
        deleteEntry(db, id, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN);
    }

    //---------- Scoring ----------

    private ScoringSettings getScoring(SQLiteDatabase db, String scoringId) {
        Cursor result = getEntry(db, scoringId, Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN);
        ScoringSettings scoring = DBUtils.cursorToScoring(result);
        result.close();
        return scoring;
    }

    private void insertScoring(SQLiteDatabase db, ScoringSettings scoring) {
        insertEntry(db, DBUtils.scoringToContentValues(scoring), Constants.SCORING_TABLE_NAME);
    }

    private void updateScoring(SQLiteDatabase db, String id, Map<String, String> updatedFields) {
        updateEntry(db, id, DBUtils.updatedValuesToContentValues(updatedFields), Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN);
    }

    private void deleteScoring(SQLiteDatabase db, String id) {
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

    private void updateEntry(SQLiteDatabase db, String id, ContentValues updatedFields, String tableName, String idColumn) {
        db.update(tableName, updatedFields, DBUtils.getUpdateAndDeleteKeyString(idColumn), new String[] {id});
    }

    private void updateMultipleKeyEntry(SQLiteDatabase db, String idOne, String idTwo, ContentValues updatedFields, String tableName, String columnOne, String columnTwo) {
        db.update(tableName, updatedFields, DBUtils.getMultiKeyUpdateAndDeleteKeyString(columnOne, columnTwo), new String[] {idOne, idTwo});
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