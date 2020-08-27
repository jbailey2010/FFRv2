package com.devingotaswitch.fileio

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.devingotaswitch.rankings.domain.*
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DBUtils.cursorToLeague
import com.devingotaswitch.utils.DBUtils.cursorToPlayer
import com.devingotaswitch.utils.DBUtils.cursorToPlayerBasic
import com.devingotaswitch.utils.DBUtils.cursorToPlayerProjection
import com.devingotaswitch.utils.DBUtils.cursorToRoster
import com.devingotaswitch.utils.DBUtils.cursorToScoring
import com.devingotaswitch.utils.DBUtils.cursorToTeam
import com.devingotaswitch.utils.DBUtils.getDeleteAllString
import com.devingotaswitch.utils.DBUtils.getMultiKeyUpdateAndDeleteKeyString
import com.devingotaswitch.utils.DBUtils.getSelectAllString
import com.devingotaswitch.utils.DBUtils.getSelectSingleString
import com.devingotaswitch.utils.DBUtils.getSelectThreeAttrString
import com.devingotaswitch.utils.DBUtils.getUpdateAndDeleteKeyString
import com.devingotaswitch.utils.DBUtils.leagueToContentValues
import com.devingotaswitch.utils.DBUtils.playerProjectionToContentValues
import com.devingotaswitch.utils.DBUtils.playerToContentValues
import com.devingotaswitch.utils.DBUtils.rosterToContentValues
import com.devingotaswitch.utils.DBUtils.sanitizeName
import com.devingotaswitch.utils.DBUtils.scoringToContentValues
import com.devingotaswitch.utils.DBUtils.teamToContentValues
import com.devingotaswitch.utils.DBUtils.updatedValuesToContentValues
import com.devingotaswitch.youruserpools.CUPHelper.currUser
import java.util.*

class RankingsDBWrapper {
    @Synchronized
    private fun getInstance(context: Context): RankingsDBHelper? {
        val user = currUser
        if (rankingsDB == null || user != rankingsDB!!.dBOwner) {
            rankingsDB = RankingsDBHelper(context, user!!)
        }
        return rankingsDB
    }

    //---------- Players ----------
    fun savePlayers(context: Context, players: Collection<Player?>) {
        val db = getInstance(context)!!.writableDatabase
        val values: MutableSet<ContentValues> = HashSet()
        for (player in players) {
            values.add(playerToContentValues(player!!))
        }
        emptyTableAndBulkSave(db, Constants.PLAYER_TABLE_NAME, values)
        bulkSaveDailyProjections(db, players)
    }

    private fun bulkSaveDailyProjections(db: SQLiteDatabase, players: Collection<Player?>) {
        val projections: MutableSet<ContentValues> = HashSet()
        for (player in players) {
            projections.add(playerProjectionToContentValues(player!!))
        }
        bulkUpsert(db, Constants.PLAYER_PROJECTIONS_TABLE_NAME, projections)
    }

    fun getPlayerProjectionHistory(context: Context): Map<String?, MutableList<DailyProjection>?> {
        val playerProjectionHistory: MutableMap<String?, MutableList<DailyProjection>?> = HashMap()
        val db = getInstance(context)!!.readableDatabase
        val allProj = getAllEntries(db, Constants.PLAYER_PROJECTIONS_TABLE_NAME)
        while (!allProj.isAfterLast) {
            val proj = cursorToPlayerProjection(allProj)
            if (!playerProjectionHistory.containsKey(proj.playerKey)) {
                val projections: MutableList<DailyProjection> = ArrayList()
                projections.add(proj)
                playerProjectionHistory[proj.playerKey] = projections
            } else {
                playerProjectionHistory[proj.playerKey]!!.add(proj)
            }
            allProj.moveToNext()
        }
        return playerProjectionHistory
    }

    fun getPlayers(context: Context): Map<String, Player> {
        val players: MutableMap<String, Player> = HashMap()
        val db = getInstance(context)!!.readableDatabase
        val result = getAllEntries(db, Constants.PLAYER_TABLE_NAME)
        while (!result.isAfterLast) {
            val player = cursorToPlayer(result)
            players[player.uniqueId] = player
            result.moveToNext()
        }
        result.close()
        return players
    }

    fun getPlayersSorted(context: Context, leagueSettings: LeagueSettings): List<String> {
        val players: MutableList<String> = ArrayList()
        val db = getInstance(context)!!.readableDatabase
        var columnName = Constants.PLAYER_ECR_COLUMN
        var orderSuffix = " ASC"
        when {
            leagueSettings.isAuction -> {
                columnName = Constants.AUCTION_VALUE_COLUMN
                orderSuffix = " DESC"
            }
            leagueSettings.isDynasty -> {
                columnName = Constants.PLAYER_DYNASTY_COLUMN
            }
            leagueSettings.isRookie -> {
                columnName = Constants.PLAYER_ROOKIE_COLUMN
            }
            leagueSettings.isBestBall -> {
                columnName = Constants.PLAYER_BEST_BALL_COLUMN
            }
        }
        val projection = arrayOf(
                Constants.PLAYER_NAME_COLUMN,
                Constants.TEAM_NAME_COLUMN,
                Constants.PLAYER_POSITION_COLUMN
        )
        val sortOrder = columnName + orderSuffix
        val result = db.query(
                Constants.PLAYER_TABLE_NAME,  // The table to query
                projection,  // The columns to return
                null,  // The columns for the WHERE clause
                null,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
        )
        result.moveToFirst()
        while (!result.isAfterLast) {
            val player = cursorToPlayerBasic(result)
            players.add(player.uniqueId)
            result.moveToNext()
        }
        result.close()
        return players
    }

    fun getPlayer(context: Context, name: String?, team: String, position: String): Player {
        val db = getInstance(context)!!.readableDatabase
        val result = getThreeKeyEntry(db, Constants.PLAYER_NAME_COLUMN, Constants.TEAM_NAME_COLUMN,
                Constants.PLAYER_POSITION_COLUMN, sanitizeName(name), team, position,
                Constants.PLAYER_TABLE_NAME)
        val player = cursorToPlayer(result)
        result.close()
        return player
    }

    private fun getCustomPlayerCursor(db: SQLiteDatabase, player: Player): Cursor {
        return getThreeKeyEntry(db, Constants.PLAYER_NAME_COLUMN, Constants.TEAM_NAME_COLUMN, Constants.PLAYER_POSITION_COLUMN,
                sanitizeName(player.name), player.teamName, player.position, Constants.PLAYER_CUSTOM_TABLE_NAME)
    }

    //---------- Teams ----------
    fun getTeams(context: Context): Map<String?, Team> {
        val db = getInstance(context)!!.readableDatabase
        val teams: MutableMap<String?, Team> = HashMap()
        val result = getAllEntries(db, Constants.TEAM_TABLE_NAME)
        while (!result.isAfterLast) {
            val team = cursorToTeam(result)
            teams[team.name] = team
            result.moveToNext()
        }
        result.close()
        return teams
    }

    fun saveTeams(context: Context, teams: Collection<Team?>) {
        val db = getInstance(context)!!.writableDatabase
        val values: MutableSet<ContentValues> = HashSet()
        for (team in teams) {
            values.add(teamToContentValues(team!!))
        }
        emptyTableAndBulkSave(db, Constants.TEAM_TABLE_NAME, values)
    }

    //---------- Leagues ----------
    fun getLeagues(context: Context): MutableMap<String?, LeagueSettings> {
        val leagues: MutableMap<String?, LeagueSettings> = HashMap()
        val db = getInstance(context)!!.readableDatabase
        val result = db.rawQuery(getSelectAllString(Constants.LEAGUE_TABLE_NAME), null)
        result.moveToFirst()
        while (!result.isAfterLast) {
            val league = cursorToFullLeague(db, result)
            leagues[league.name] = league
            result.moveToNext()
        }
        result.close()
        return leagues
    }

    fun getLeague(context: Context, leagueId: String?): LeagueSettings {
        val db = getInstance(context)!!.readableDatabase
        val result = getEntry(db, sanitizeName(leagueId), Constants.LEAGUE_TABLE_NAME, Constants.NAME_COLUMN)
        val league = cursorToFullLeague(db, result)
        result.close()
        return league
    }

    private fun cursorToFullLeague(db: SQLiteDatabase, result: Cursor): LeagueSettings {
        val rosterId = result.getString(result.getColumnIndex(Constants.ROSTER_ID_COLUMN))
        val scoringId = result.getString(result.getColumnIndex(Constants.SCORING_ID_COLUMN))
        return cursorToLeague(result, getRoster(db, rosterId), getScoring(db, scoringId))
    }

    fun insertLeague(context: Context, league: LeagueSettings) {
        val db = getInstance(context)!!.writableDatabase
        insertRoster(db, league.rosterSettings)
        insertScoring(db, league.scoringSettings)
        insertEntry(db, leagueToContentValues(league), Constants.LEAGUE_TABLE_NAME)
    }

    fun updateLeague(context: Context, leagueUpdates: Map<String?, String?>?, rosterUpdates: Map<String?, String?>?,
                     scoringUpdates: Map<String?, String?>?, league: LeagueSettings) {
        val db = getInstance(context)!!.writableDatabase
        if (rosterUpdates != null && rosterUpdates.isNotEmpty()) {
            updateRoster(db, league.rosterSettings.id, rosterUpdates)
        }
        if (scoringUpdates != null && scoringUpdates.isNotEmpty()) {
            updateScoring(db, league.scoringSettings.id, scoringUpdates)
        }
        if (leagueUpdates != null && leagueUpdates.isNotEmpty()) {
            updateEntry(db, league.name, updatedValuesToContentValues(leagueUpdates), Constants.LEAGUE_TABLE_NAME, Constants.NAME_COLUMN)
        }
    }

    fun deleteLeague(context: Context, league: LeagueSettings) {
        val db = getInstance(context)!!.writableDatabase
        deleteRoster(db, league.rosterSettings.id)
        deleteScoring(db, league.scoringSettings.id)
        deleteEntry(db, league.name, Constants.LEAGUE_TABLE_NAME, Constants.NAME_COLUMN)
    }

    //---------- Rosters ----------
    private fun getRoster(db: SQLiteDatabase, rosterId: String): RosterSettings {
        val result = getEntry(db, rosterId, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN)
        val roster = cursorToRoster(result)
        result.close()
        return roster
    }

    private fun insertRoster(db: SQLiteDatabase, roster: RosterSettings?) {
        insertEntry(db, rosterToContentValues(roster!!), Constants.ROSTER_TABLE_NAME)
    }

    private fun updateRoster(db: SQLiteDatabase, id: String?, updatedFields: Map<String?, String?>) {
        updateEntry(db, id, updatedValuesToContentValues(updatedFields), Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN)
    }

    private fun deleteRoster(db: SQLiteDatabase, id: String?) {
        deleteEntry(db, id, Constants.ROSTER_TABLE_NAME, Constants.ROSTER_ID_COLUMN)
    }

    //---------- Scoring ----------
    private fun getScoring(db: SQLiteDatabase, scoringId: String): ScoringSettings {
        val result = getEntry(db, scoringId, Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN)
        val scoring = cursorToScoring(result)
        result.close()
        return scoring
    }

    private fun insertScoring(db: SQLiteDatabase, scoring: ScoringSettings?) {
        insertEntry(db, scoringToContentValues(scoring!!), Constants.SCORING_TABLE_NAME)
    }

    private fun updateScoring(db: SQLiteDatabase, id: String?, updatedFields: Map<String?, String?>) {
        updateEntry(db, id, updatedValuesToContentValues(updatedFields), Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN)
    }

    private fun deleteScoring(db: SQLiteDatabase, id: String?) {
        deleteEntry(db, id, Constants.SCORING_TABLE_NAME, Constants.SCORING_ID_COLUMN)
    }

    //---------- Utilities ----------
    private fun getEntry(db: SQLiteDatabase, id: String, tableName: String, idColumn: String): Cursor {
        val result = db.rawQuery(getSelectSingleString(tableName,
                idColumn, id), null)
        result.moveToFirst()
        return result
    }

    private fun getThreeKeyEntry(db: SQLiteDatabase, columnOne: String, columnTwo: String, columnThree: String,
                                 valueOne: String, valueTwo: String, valueThree: String, tableName: String): Cursor {
        val result = db.rawQuery(getSelectThreeAttrString(tableName, columnOne, columnTwo, columnThree,
                valueOne, valueTwo, valueThree), null)
        result.moveToFirst()
        return result
    }

    private fun insertEntry(db: SQLiteDatabase, values: ContentValues, tableName: String) {
        db.insert(tableName, null, values)
    }

    private fun upsertEntry(db: SQLiteDatabase, values: ContentValues, tableName: String) {
        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun updateEntry(db: SQLiteDatabase, id: String?, updatedFields: ContentValues, tableName: String, idColumn: String) {
        db.update(tableName, updatedFields, getUpdateAndDeleteKeyString(idColumn), arrayOf(id))
    }

    private fun updateMultipleKeyEntry(db: SQLiteDatabase, idOne: String, idTwo: String, updatedFields: ContentValues, tableName: String, columnOne: String, columnTwo: String) {
        db.update(tableName, updatedFields, getMultiKeyUpdateAndDeleteKeyString(columnOne, columnTwo), arrayOf(idOne, idTwo))
    }

    private fun deleteEntry(db: SQLiteDatabase, id: String?, tableName: String, idColumn: String) {
        db.delete(tableName, getUpdateAndDeleteKeyString(idColumn), arrayOf(id))
    }

    private fun emptyTableAndBulkSave(db: SQLiteDatabase, tableName: String, valuesToInsert: Set<ContentValues>) {
        deleteItemsInTable(db, tableName)
        bulkSave(db, tableName, valuesToInsert)
    }

    private fun bulkSave(db: SQLiteDatabase, tableName: String, valuesToInsert: Set<ContentValues>) {
        db.beginTransaction()
        try {
            bulkSaveWorker(db, tableName, valuesToInsert)
        } finally {
            db.endTransaction()
        }
    }

    private fun bulkUpsert(db: SQLiteDatabase, tableName: String, valuesToInsert: Set<ContentValues>) {
        db.beginTransaction()
        try {
            for (values in valuesToInsert) {
                upsertEntry(db, values, tableName)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun bulkSaveWorker(db: SQLiteDatabase, tableName: String, valuesToInsert: Set<ContentValues>) {
        for (values in valuesToInsert) {
            val result = db.insert(tableName, null, values)
        }
        db.setTransactionSuccessful()
    }

    private fun getNumberOfRowsInTable(db: SQLiteDatabase, tableName: String): Int {
        return DatabaseUtils.queryNumEntries(db, tableName).toInt()
    }

    private fun deleteItemsInTable(db: SQLiteDatabase, tableName: String) {
        db.execSQL(getDeleteAllString(tableName))
    }

    private fun getAllEntries(db: SQLiteDatabase, tableName: String): Cursor {
        val result = db.rawQuery(getSelectAllString(tableName), null)
        result.moveToFirst()
        return result
    }

    companion object {
        private var rankingsDB: RankingsDBHelper? = null
    }
}