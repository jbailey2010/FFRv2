package com.devingotaswitch.fileio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.devingotaswitch.utils.Constants;

public class RankingsDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Rankings.db";

    RankingsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateScoringTableSQL());
        db.execSQL(getCreateRosterTableSQL());
        db.execSQL(getCreateLeagueTableSQL());
        db.execSQL(getCreateTeamTableSQL());
        db.execSQL(getCreatePlayerTableSQL());
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(getDeleteScoringTableSQL());
        db.execSQL(getDeleteRosterTableSQL());
        db.execSQL(getDeleteLeagueTableSQL());
        db.execSQL(getDeleteTeamTableSQL());
        db.execSQL(getDeletePlayerTableSQL());
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private String getCreateLeagueTableSQL() {
        return "CREATE TABLE " + Constants.LEAGUE_TABLE_NAME + " (" +
                Constants.LEAGUE_ID_COLUMN + " TEXT PRIMARY KEY," +
                Constants.NAME_COLUMN            + " TEXT," +
                Constants.TEAM_COUNT_COLUMN      + " INTEGER," +
                Constants.IS_AUCTION_COLUMN      + " BOOLEAN," +
                Constants.AUCTION_BUDGET_COLUMN  + " INTEGER," +
                Constants.SCORING_ID_COLUMN      + " TEXT," +
                Constants.ROSTER_ID_COLUMN       + " TEXT);";
    }

    private String getDeleteLeagueTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.LEAGUE_TABLE_NAME;
    }
    private String getCreateRosterTableSQL() {
        return "CREATE TABLE " + Constants.ROSTER_TABLE_NAME + " (" +
                Constants.ROSTER_ID_COLUMN + " TEXT PRIMARY KEY," +
                Constants.QB_COUNT_COLUMN        + " INTEGER," +
                Constants.RB_COUNT_COLUMN        + " INTEGER," +
                Constants.WR_COUNT_COLUMN        + " INTEGER," +
                Constants.TE_COUNT_COLUMN        + " INTEGER," +
                Constants.DST_COUNT_COLUMN       + " INTEGER," +
                Constants.K_COUNT_COLUMN         + " INTEGER," +
                Constants.BENCH_COUNT_COLUMN     + " INTEGER," +
                Constants.RBWR_COUNT_COLUMN      + " INTEGER," +
                Constants.RBTE_COUNT_COLUMN      + " INTEGER," +
                Constants.RBWRTE_COUNT_COLUMN    + " INTEGER," +
                Constants.WRTE_COUNT_COLUMN      + " INTEGER," +
                Constants.QBRBWRTE_COUNT_COLUMN  + " INTEGER);";
    }

    private String getDeleteRosterTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.ROSTER_TABLE_NAME;
    }

    private String getCreateScoringTableSQL() {
        return "CREATE TABLE " + Constants.SCORING_TABLE_NAME + " (" +
                Constants.SCORING_ID_COLUMN      + " TEXT PRIMARY KEY," +
                Constants.PASSING_TDS_COLUMN     + " INTEGER," +
                Constants.RUSHING_TDS_COLUMN     + " INTEGER," +
                Constants.RECEIVING_TDS_COLUMN   + " INTEGER," +
                Constants.FUMBLES_COLUMN         + " INTEGER," +
                Constants.INTERCEPTIONS_COLUMN   + " INTEGER," +
                Constants.PASSING_YARDS_COLUMN   + " INTEGER," +
                Constants.RUSHING_YARDS_COLUMN   + " INTEGER," +
                Constants.RECEIVING_YARDS_COLUMN + " INTEGER," +
                Constants.RECEPTIONS_COLUMN      + " REAL);";
    }

    private String getDeleteScoringTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.SCORING_TABLE_NAME;
    }

    private String getCreateTeamTableSQL() {
        return "CREATE TABLE "                   + Constants.TEAM_TABLE_NAME + " (" +
                Constants.TEAM_NAME_COLUMN       + " TEXT PRIMARY KEY," +
                Constants.OLINE_RANKS_COLUMN     + " TEXT," +
                Constants.DRAFT_CLASS_COLUMN     + " TEXT," +
                Constants.QB_SOS_COLUMN          + " INTEGER," +
                Constants.RB_SOS_COLUMN          + " INTEGER," +
                Constants.WR_SOS_COLUMN          + " INTEGER," +
                Constants.TE_SOS_COLUMN          + " INTEGER," +
                Constants.DST_SOS_COLUMN         + " INTEGER," +
                Constants.K_SOS_COLUMN           + " INTEGER," +
                Constants.BYE_COLUMN             + " TEXT," +
                Constants.FREE_AGENCY_COLUMN     + " TEXT);";
    }

    private String getDeleteTeamTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.TEAM_TABLE_NAME;
    }

    private String getCreatePlayerTableSQL() {
        return "CREATE TABLE "                   + Constants.PLAYER_TABLE_NAME + " (" +
                Constants.PLAYER_NAME_COLUMN     + " TEXT," +
                Constants.PLAYER_POSITION_COLUMN + " TEXT," +
                Constants.TEAM_NAME_COLUMN       + " TEXT," +
                Constants.PLAYER_AGE_COLUMN      + " INTEGER," +
                Constants.PLAYER_ECR_COLUMN      + " REAL," +
                Constants.PLAYER_ADP_COLUMN      + " REAL," +
                Constants.PLAYER_NOTE_COLUMN     + " TEXT," +
                Constants.RANKING_COUNT_COLUMN   + " REAL," +
                Constants.AUCTION_VALUE_COLUMN   + " REAL," +
                "PRIMARY KEY(" + Constants.PLAYER_NAME_COLUMN + ", " + Constants.PLAYER_POSITION_COLUMN + ")" +
                ");";
    }

    private String getDeletePlayerTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.PLAYER_TABLE_NAME;
    }
}
