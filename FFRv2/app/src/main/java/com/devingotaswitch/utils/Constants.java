package com.devingotaswitch.utils;

public class Constants {
    // Positions
    public final static String QB = "QB";
    public final static String RB = "RB";
    public final static String WR = "WR";
    public final static String TE = "TE";
    public final static String DST = "D/ST";
    public final static String K = "K";

    // SP: keys
    public static final String APP_KEY = "FFRv2";
    public static final String LEAGUE_ID = "CURRENT_LEAGUE_ID";
    public static final String NUM_PLAYERS = "MAX_PLAYERS_VISIBLE";

    // SP: defaults
    public static final String NOT_SET_KEY = "NOT_SAVED";
    public static final Integer DEFAULT_NUM_PLAYERS = 1000;

    // League settings: SQL
    public static final String LEAGUE_TABLE_NAME = "league_settings";
    public static final String LEAGUE_ID_COLUMN = "league_id";
    public static final String NAME_COLUMN = "league_name";
    public static final String TEAM_COUNT_COLUMN = "team_count";
    public static final String IS_AUCTION_COLUMN = "is_auction";
    public static final String AUCTION_BUDGET_COLUMN = "auction_budget";
    public static final String SCORING_ID_COLUMN = "scoring_id";
    public static final String ROSTER_ID_COLUMN = "roster_id";

    // League settings: defaults
    public static final String DEFAULT_NAME = "My League";
    public static final Integer DEFAULT_TEAM_COUNT = 10;
    public static final Boolean DEFAULT_IS_AUCTION = true;
    public static final Integer DEFAULT_AUCTION_BUDGET = 200;

    // Scoring settings: SQL
    public static final String SCORING_TABLE_NAME = "scoring_settings";
    // Scoring id is already in league settings
    public static final String PASSING_TDS_COLUMN = "pts_per_passing_td";
    public static final String RUSHING_TDS_COLUMN = "pts_per_rushing_td";
    public static final String RECEIVING_TDS_COLUMN = "pts_per_receiving_td";
    public static final String FUMBLES_COLUMN = "pts_per_fumble";
    public static final String INTERCEPTIONS_COLUMN = "pts_per_interception";
    public static final String PASSING_YARDS_COLUMN = "passing_yards_per_point";
    public static final String RUSHING_YARDS_COLUMN = "rushing_yards_per_point";
    public static final String RECEIVING_YARDS_COLUMN = "receiving_yards_per_point";
    public static final String RECEPTIONS_COLUMN = "pts_per_reception";

    // Scoring settings: defaults
    public static final Integer DEFAULT_TD_WORTH = 6;
    public static final Integer DEFAULT_TURNOVER_WORTH = -2;
    public static final Integer DEFAULT_PASSING_YDS = 25;
    public static final Integer DEFAULT_RUSHING_YDS = 10;
    public static final Integer DEFAULT_RECEIVING_YDS = 10;
    public static final Double DEFAULT_RECEPTIONS = 1.0;

    // Roster settings: SQL
    public static final String ROSTER_TABLE_NAME = "roster_settings";
    // Roster id is already in league settings
    public static final String QB_COUNT_COLUMN = "starting_qbs";
    public static final String RB_COUNT_COLUMN = "starting_rbs";
    public static final String WR_COUNT_COLUMN = "starting_wrs";
    public static final String TE_COUNT_COLUMN = "starting_tes";
    public static final String DST_COUNT_COLUMN = "starting_dsts";
    public static final String K_COUNT_COLUMN = "starting_ks";
    public static final String BENCH_COUNT_COLUMN = "bench_count";
    public static final String RBWR_COUNT_COLUMN = "starting_rbwr";
    public static final String RBTE_COUNT_COLUMN = "starting_rbte";
    public static final String RBWRTE_COUNT_COLUMN = "starting_rbwrte";
    public static final String WRTE_COUNT_COLUMN = "starting_wrte";
    public static final String QBRBWRTE_COUNT_COLUMN = "starting_qbrbwrte";

    // Roster settings: defaults
    public static final Integer NO_STARTERS = 0;
    public static final Integer ONE_STARTER = 1;
    public static final Integer TWO_STARTERS = 2;
    public static final Integer BENCH_DEFAULT = 6;
}
