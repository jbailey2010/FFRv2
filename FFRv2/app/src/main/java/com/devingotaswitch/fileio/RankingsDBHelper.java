package com.devingotaswitch.fileio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.devingotaswitch.utils.Constants;

class RankingsDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME_SUFFIX = "Rankings.db";

    private final String userName;

    RankingsDBHelper(Context context, String userName) {
        super(context, userName + DATABASE_NAME_SUFFIX, null, DATABASE_VERSION);
        this.userName = userName;
    }

    public String getDBOwner() {
        return userName;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateScoringTableSQL());
        db.execSQL(getCreateRosterTableSQL());
        db.execSQL(getCreateLeagueTableSQL());
        db.execSQL(getCreateTeamTableSQL());
        db.execSQL(getCreatePlayerTableSQL());
        db.execSQL(getCreatePlayerCustomTableSQL());
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(getDeleteScoringTableSQL());
        db.execSQL(getDeleteRosterTableSQL());
        db.execSQL(getDeleteLeagueTableSQL());
        db.execSQL(getDeleteTeamTableSQL());
        db.execSQL(getDeletePlayerTableSQL());
        db.execSQL(getDeletePlayerCustomTableSQL());
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private String getCreateLeagueTableSQL() {
        return "CREATE TABLE " + Constants.LEAGUE_TABLE_NAME + " (" +
                Constants.NAME_COLUMN                + " TEXT PRIMARY KEY," +
                Constants.TEAM_COUNT_COLUMN          + " INTEGER," +
                Constants.IS_SNAKE_COLUMN            + " BOOLEAN," +
                Constants.IS_AUCTION_COLUMN          + " BOOLEAN," +
                Constants.IS_DYNASTY_STARTUP_COLUMN  + " BOOLEAN," +
                Constants.IS_DYNASTY_ROOKIE_COLUMN   + " BOOLEAN," +
                Constants.IS_BEST_BALL_COLUMN        + " BOOLEAN," +
                Constants.AUCTION_BUDGET_COLUMN      + " INTEGER," +
                Constants.SCORING_ID_COLUMN          + " TEXT," +
                Constants.ROSTER_ID_COLUMN           + " TEXT);";
    }

    private String getDeleteLeagueTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.LEAGUE_TABLE_NAME;
    }
    private String getCreateRosterTableSQL() {
        return "CREATE TABLE " + Constants.ROSTER_TABLE_NAME + " (" +
                Constants.ROSTER_ID_COLUMN       + " TEXT PRIMARY KEY," +
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
                Constants.FUMBLES_COLUMN         + " REAL," +
                Constants.INTERCEPTIONS_COLUMN   + " REAL," +
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
                Constants.FREE_AGENCY_COLUMN     + " TEXT," +
                Constants.SCHEDULE_COLUMN        + " TEXT);";
    }

    private String getDeleteTeamTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.TEAM_TABLE_NAME;
    }

    private String getCreatePlayerTableSQL() {
        return "CREATE TABLE "                     + Constants.PLAYER_TABLE_NAME + " (" +
                Constants.PLAYER_NAME_COLUMN       + " TEXT," +
                Constants.PLAYER_POSITION_COLUMN   + " TEXT," +
                Constants.TEAM_NAME_COLUMN         + " TEXT," +
                Constants.PLAYER_AGE_COLUMN        + " INTEGER," +
                Constants.PLAYER_EXPERIENCE_COLUMN + " INTEGER," +
                Constants.PLAYER_STATS_COLUMN      + " TEXT," +
                Constants.PLAYER_INJURED_COLUMN    + " TEXT," +
                Constants.PLAYER_ECR_COLUMN        + " REAL," +
                Constants.PLAYER_RISK_COLUMN       + " REAL," +
                Constants.PLAYER_ADP_COLUMN        + " REAL," +
                Constants.PLAYER_DYNASTY_COLUMN    + " REAL," +
                Constants.PLAYER_ROOKIE_COLUMN     + " REAL," +
                Constants.PLAYER_BEST_BALL_COLUMN  + " REAL," +
                Constants.AUCTION_VALUE_COLUMN     + " REAL," +
                Constants.PLAYER_PROJECTION_COLUMN + " TEXT," +
                Constants.PLAYER_PAA_COLUMN        + " REAL," +
                Constants.PLAYER_XVAL_COLUMN       + " REAL," +
                Constants.PLAYER_VORP_COLUMN       + " REAL," +
                "PRIMARY KEY(" + Constants.PLAYER_NAME_COLUMN + ", " + Constants.TEAM_NAME_COLUMN + ", " + Constants.PLAYER_POSITION_COLUMN + ")" +
                ");";
    }

    private String getDeletePlayerTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.PLAYER_TABLE_NAME;
    }

    private String getCreatePlayerCustomTableSQL() {
        return "CREATE TABLE "                   + Constants.PLAYER_CUSTOM_TABLE_NAME + " (" +
                Constants.PLAYER_NAME_COLUMN     + " TEXT," +
                Constants.PLAYER_POSITION_COLUMN + " TEXT," +
                Constants.TEAM_NAME_COLUMN       + " TEXT," +
                Constants.PLAYER_NOTE_COLUMN     + " TEXT," +
                Constants.PLAYER_WATCHED_COLUMN  + " BOOLEAN," +
                "PRIMARY KEY(" + Constants.PLAYER_NAME_COLUMN + ", " + Constants.TEAM_NAME_COLUMN + ", " + Constants.PLAYER_POSITION_COLUMN + ")" +
                ");";
    }

    private String getDeletePlayerCustomTableSQL() {
        return "DROP TABLE IF EXISTS " + Constants.PLAYER_CUSTOM_TABLE_NAME;
    }
}
