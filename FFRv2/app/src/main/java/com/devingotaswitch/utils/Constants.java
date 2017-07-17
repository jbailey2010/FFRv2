package com.devingotaswitch.utils;

public class Constants {
    // Positions
    public final static String QB = "QB";
    public final static String RB = "RB";
    public final static String WR = "WR";
    public final static String TE = "TE";
    public final static String DST = "D/ST";
    public final static String K = "K";
    // Flex positions
    public final static String RBTE = "RB/TE";
    public final static String RBWR = "RB/WR";
    public final static String RBWRTE = "RB/WR/TE";
    public final static String WRTE = "WR/TE";
    public final static String QBRBWRTE = "QB/RB/WR/TE";

    // Filter constants
    public final static String ALL_POSITIONS = "All Positions";
    public final static String ALL_TEAMS = "All Teams";

    // Sort factors
    public final static String SORT_ALL = "Default Sort";
    public final static String SORT_ECR = "ECR";
    public final static String SORT_ADP = "ADP";
    public final static String SORT_UNDERDRAFTED = "Underdrafted";
    public final static String SORT_AUCTION = "Auction Value";
    public final static String SORT_PROJECTION = "Projection";
    public final static String SORT_PAA = "Points Above Average";
    public final static String SORT_PAA_SCALED = "Scaled Points Above Average";
    public final static String SORT_PAAPD = "Points Above Average Per Dollar";
    public final static String SORT_XVAL = "X Value";
    public final static String SORT_XVAL_SCALED = "Scaled X Value";
    public final static String SORT_RISK = "Risk";
    public final static String SORT_SOS = "Positional SOS";
    public final static String SORT_TIERS = "Positional Tier";

    // Sort boolean factors
    public final static String SORT_DEFAULT_STRING = "Additional Factors";
    public final static String SORT_HIDE_DRAFTED = "Hide Drafted";
    public final static String SORT_EASY_SOS = "Easy SOS";
    public final static String SORT_ONLY_HEALTHY = "Healthy";

    // SP: keys
    public static final String APP_KEY = "FFRv2";
    public static final String LEAGUE_NAME = "CURRENT_LEAGUE_NAME";
    public static final String NUM_PLAYERS = "MAX_PLAYERS_VISIBLE";
    public static final String RANKINGS_FETCHED = "RANKINGS_FETCHED";

    // SP: defaults
    public static final String NOT_SET_KEY = "NOT_SAVED";
    public static final Integer DEFAULT_NUM_PLAYERS = 500;
    public static final Boolean NOT_SET_BOOLEAN = false;

    // League settings: SQL
    public static final String LEAGUE_TABLE_NAME = "league_settings";
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

    // Team settings: SQL
    public static final String TEAM_TABLE_NAME = "team_data";
    public static final String TEAM_NAME_COLUMN = "team_name";
    public static final String OLINE_RANKS_COLUMN = "oline_ranks";
    public static final String DRAFT_CLASS_COLUMN = "draft_class";
    public static final String QB_SOS_COLUMN = "qb_sos";
    public static final String RB_SOS_COLUMN = "rb_sos";
    public static final String WR_SOS_COLUMN = "wr_sos";
    public static final String TE_SOS_COLUMN = "te_sos";
    public static final String DST_SOS_COLUMN = "dst_sos";
    public static final String K_SOS_COLUMN = "k_sos";
    public static final String BYE_COLUMN = "bye_week";
    public static final String FREE_AGENCY_COLUMN = "fa_class";

    // Player settings: SQL
    public static final String PLAYER_TABLE_NAME = "player_data";
    public static final String PLAYER_CUSTOM_TABLE_NAME = "custom_player_data";
    public static final String PLAYER_NAME_COLUMN = "player_name";
    public static final String PLAYER_POSITION_COLUMN = "position";
    public static final String PLAYER_AGE_COLUMN = "age";
    public static final String PLAYER_ECR_COLUMN = "ecr";
    public static final String PLAYER_ADP_COLUMN = "adp";
    public static final String PLAYER_RISK_COLUMN = "risk";
    public static final String PLAYER_STATS_COLUMN = "stats";
    public static final String PLAYER_INJURED_COLUMN = "injury_status";
    public static final String PLAYER_PROJECTION_COLUMN = "projection";
    public static final String PLAYER_PAA_COLUMN = "paa";
    public static final String PLAYER_XVAL_COLUMN = "xval";
    public static final String PLAYER_TIER_COLUMN = "positional_tier";
    // Team name is already in team settings
    public static final String PLAYER_NOTE_COLUMN = "player_note";
    public static final String PLAYER_WATCHED_COLUMN = "player_watched";
    public static final String AUCTION_VALUE_COLUMN = "auction_value";

    // Other internal stuff
    public final static String NUMBER_FORMAT = "#.##";
    public final static String PLAYER_ID_DELIMITER = ".";
    public final static String HASH_DELIMITER = "###";
    public final static String LINE_BREAK = "\n";
    public final static String RANKINGS_LIST_DELIMITER = ": ";
    public final static String PLAYER_ID = "player_id";
    public final static String RANKINGS_UPDATED = "rankings_updated";

    // Draft
    public final static String CURRENT_DRAFT = "current_draft";
    public final static String CURRENT_TEAM = "current_team";

    // Display rankings utilities
    public final static String PLAYER_BASIC = "main";
    public final static String PLAYER_INFO = "info";
    public final static String PLAYER_STATUS = "status";
    public final static String PLAYER_TIER = "tier";
    public final static String DROPDOWN_MAIN = "main";
    public final static String DROPDOWN_SUB = "sub";
    public final static String POS_TEAM_DELIMITER = " - ";
    public final static String NOTE_SUB = "Click to update note, click and hold to clear";
    public final static String DEFAULT_NOTE = "No note found";
}
