package com.devingotaswitch.fileio

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devingotaswitch.utils.Constants

internal class RankingsDBHelper(context: Context?, val dBOwner: String) : SQLiteOpenHelper(context, dBOwner + DATABASE_NAME_SUFFIX, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createScoringTableSQL)
        db.execSQL(createRosterTableSQL)
        db.execSQL(createLeagueTableSQL)
        db.execSQL(createTeamTableSQL)
        db.execSQL(createPlayerTableSQL)
        db.execSQL(createPlayerProjectionsTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(deleteScoringTableSQL)
        db.execSQL(deleteRosterTableSQL)
        db.execSQL(deleteLeagueTableSQL)
        db.execSQL(deleteTeamTableSQL)
        db.execSQL(deletePlayerTableSQL)
        db.execSQL(deletePlayerProjectionsTableSQL)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    private val createLeagueTableSQL: String
        get() = "CREATE TABLE " + Constants.LEAGUE_TABLE_NAME + " (" +
                Constants.NAME_COLUMN + " TEXT PRIMARY KEY," +
                Constants.TEAM_COUNT_COLUMN + " INTEGER," +
                Constants.IS_SNAKE_COLUMN + " BOOLEAN," +
                Constants.IS_AUCTION_COLUMN + " BOOLEAN," +
                Constants.IS_DYNASTY_STARTUP_COLUMN + " BOOLEAN," +
                Constants.IS_DYNASTY_ROOKIE_COLUMN + " BOOLEAN," +
                Constants.IS_BEST_BALL_COLUMN + " BOOLEAN," +
                Constants.AUCTION_BUDGET_COLUMN + " INTEGER," +
                Constants.SCORING_ID_COLUMN + " TEXT," +
                Constants.ROSTER_ID_COLUMN + " TEXT);"
    private val deleteLeagueTableSQL: String
        get() = "DROP TABLE IF EXISTS " + Constants.LEAGUE_TABLE_NAME
    private val createRosterTableSQL: String
        get() = "CREATE TABLE " + Constants.ROSTER_TABLE_NAME + " (" +
                Constants.ROSTER_ID_COLUMN + " TEXT PRIMARY KEY," +
                Constants.QB_COUNT_COLUMN + " INTEGER," +
                Constants.RB_COUNT_COLUMN + " INTEGER," +
                Constants.WR_COUNT_COLUMN + " INTEGER," +
                Constants.TE_COUNT_COLUMN + " INTEGER," +
                Constants.DST_COUNT_COLUMN + " INTEGER," +
                Constants.K_COUNT_COLUMN + " INTEGER," +
                Constants.BENCH_COUNT_COLUMN + " INTEGER," +
                Constants.RBWR_COUNT_COLUMN + " INTEGER," +
                Constants.RBTE_COUNT_COLUMN + " INTEGER," +
                Constants.RBWRTE_COUNT_COLUMN + " INTEGER," +
                Constants.WRTE_COUNT_COLUMN + " INTEGER," +
                Constants.QBRBWRTE_COUNT_COLUMN + " INTEGER);"
    private val deleteRosterTableSQL: String
        get() = "DROP TABLE IF EXISTS " + Constants.ROSTER_TABLE_NAME
    private val createScoringTableSQL: String
        get() = "CREATE TABLE " + Constants.SCORING_TABLE_NAME + " (" +
                Constants.SCORING_ID_COLUMN + " TEXT PRIMARY KEY," +
                Constants.PASSING_TDS_COLUMN + " INTEGER," +
                Constants.RUSHING_TDS_COLUMN + " INTEGER," +
                Constants.RECEIVING_TDS_COLUMN + " INTEGER," +
                Constants.FUMBLES_COLUMN + " REAL," +
                Constants.INTERCEPTIONS_COLUMN + " REAL," +
                Constants.PASSING_YARDS_COLUMN + " INTEGER," +
                Constants.RUSHING_YARDS_COLUMN + " INTEGER," +
                Constants.RECEIVING_YARDS_COLUMN + " INTEGER," +
                Constants.RECEPTIONS_COLUMN + " REAL);"
    private val deleteScoringTableSQL: String
        get() = "DROP TABLE IF EXISTS " + Constants.SCORING_TABLE_NAME
    private val createTeamTableSQL: String
        get() = "CREATE TABLE " + Constants.TEAM_TABLE_NAME + " (" +
                Constants.TEAM_NAME_COLUMN + " TEXT PRIMARY KEY," +
                Constants.OLINE_RANKS_COLUMN + " TEXT," +
                Constants.DRAFT_CLASS_COLUMN + " TEXT," +
                Constants.QB_SOS_COLUMN + " REAL," +
                Constants.RB_SOS_COLUMN + " REAL," +
                Constants.WR_SOS_COLUMN + " REAL," +
                Constants.TE_SOS_COLUMN + " REAL," +
                Constants.DST_SOS_COLUMN + " REAL," +
                Constants.K_SOS_COLUMN + " REAL," +
                Constants.BYE_COLUMN + " TEXT," +
                Constants.FREE_AGENCY_COLUMN + " TEXT," +
                Constants.SCHEDULE_COLUMN + " TEXT);"
    private val deleteTeamTableSQL: String
        get() = "DROP TABLE IF EXISTS " + Constants.TEAM_TABLE_NAME
    private val createPlayerTableSQL: String
        get() = "CREATE TABLE " + Constants.PLAYER_TABLE_NAME + " (" +
                Constants.PLAYER_NAME_COLUMN + " TEXT," +
                Constants.PLAYER_POSITION_COLUMN + " TEXT," +
                Constants.TEAM_NAME_COLUMN + " TEXT," +
                Constants.PLAYER_AGE_COLUMN + " INTEGER," +
                Constants.PLAYER_EXPERIENCE_COLUMN + " INTEGER," +
                Constants.PLAYER_STATS_COLUMN + " TEXT," +
                Constants.PLAYER_INJURED_COLUMN + " TEXT," +
                Constants.PLAYER_ECR_COLUMN + " REAL," +
                Constants.PLAYER_RISK_COLUMN + " REAL," +
                Constants.PLAYER_ADP_COLUMN + " REAL," +
                Constants.PLAYER_DYNASTY_COLUMN + " REAL," +
                Constants.PLAYER_ROOKIE_COLUMN + " REAL," +
                Constants.PLAYER_BEST_BALL_COLUMN + " REAL," +
                Constants.AUCTION_VALUE_COLUMN + " REAL," +
                Constants.PLAYER_PROJECTION_COLUMN + " TEXT," +
                Constants.PLAYER_PAA_COLUMN + " REAL," +
                Constants.PLAYER_XVAL_COLUMN + " REAL," +
                Constants.PLAYER_VORP_COLUMN + " REAL," +
                "PRIMARY KEY(" + Constants.PLAYER_NAME_COLUMN + ", " + Constants.TEAM_NAME_COLUMN + ", " + Constants.PLAYER_POSITION_COLUMN + ")" +
                ");"
    private val deletePlayerTableSQL: String
        get() = "DROP TABLE IF EXISTS " + Constants.PLAYER_TABLE_NAME
    private val createPlayerProjectionsTableSQL: String
        get() = "CREATE TABLE " + Constants.PLAYER_PROJECTIONS_TABLE_NAME + " (" +
                Constants.PLAYER_NAME_COLUMN + " TEXT," +
                Constants.PLAYER_POSITION_COLUMN + " TEXT," +
                Constants.TEAM_NAME_COLUMN + " TEXT," +
                Constants.PLAYER_PROJECTION_DATE_COLUMN + " TEXT," +
                Constants.PLAYER_PROJECTION_COLUMN + " TEXT," +
                "PRIMARY KEY(" + Constants.PLAYER_NAME_COLUMN + ", " + Constants.TEAM_NAME_COLUMN + ", " + Constants.PLAYER_POSITION_COLUMN +
                ", " + Constants.PLAYER_PROJECTION_DATE_COLUMN + ")" +
                ");"
    private val deletePlayerProjectionsTableSQL: String
        get() = "DROP TABLE IF EXISTS " + Constants.PLAYER_PROJECTIONS_TABLE_NAME

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME_SUFFIX = "Rankings.db"
    }
}