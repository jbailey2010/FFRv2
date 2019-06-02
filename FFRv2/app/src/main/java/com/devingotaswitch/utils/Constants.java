package com.devingotaswitch.utils;

import android.graphics.Color;

import java.text.DecimalFormat;

public class Constants {
    public final static String YEAR_KEY = "2019";
    public final static String LAST_YEAR_KEY = String.valueOf(Integer.parseInt(YEAR_KEY) - 1);
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
    public final static String SORT_BACK = "Back";
    public final static String SORT_ALL = "Default Sort";
    public final static String SORT_ECR = "ECR";
    public final static String SORT_ADP = "ADP";
    public final static String SORT_UNDERDRAFTED = "ADP Good Values";
    public final static String SORT_OVERDRAFTED = "ADP Bad Values";
    public final static String SORT_AUCTION = "Auction Value";
    public final static String SORT_DYNASTY = "Dynasty/Keeper Rank";
    public final static String SORT_ROOKIE = "Rookie Rank";
    public final static String SORT_BEST_BALL = "Best Ball Rank";
    public final static String SORT_PROJECTION_EXPANDED = "Projections >";
    public final static String SORT_PROJECTION = "Total Points";
    public final static String SORT_PASSING_TDS = "Passing TDs";
    public final static String SORT_PASSING_YDS = "Passing Yards";
    public final static String SORT_RUSHING_TDS = "Rushing TDs";
    public final static String SORT_RUSHING_YDS = "Rushing Yards";
    public final static String SORT_RECEIVING_TDS = "Receiving TDs";
    public final static String SORT_RECEIVING_YDS = "Receiving Yards";
    public final static String SORT_RECEPTIONS = "Receptions";
    public final static String SORT_VBD_EXPANDED = "VBD >";
    public final static String SORT_PAA = "PAA";
    public final static String SORT_PAA_SCALED = "Scaled PAA";
    public final static String SORT_PAAPD = "PAA Per Dollar";
    public final static String SORT_XVAL = "X Value";
    public final static String SORT_XVAL_SCALED = "Scaled X Value";
    public final static String SORT_XVALPD = "X Value Per Dollar";
    public final static String SORT_VOLS = "VoLS";
    public final static String SORT_VOLS_SCALED = "Scaled VoLS";
    public final static String SORT_VOLSPD = "VoLS Per Dollar";
    public final static String SORT_VBD_SUGGESTED = "VBD Suggested Pick";
    public final static String SORT_RISK = "Risk";
    public final static String SORT_SOS = "Positional SOS";

    // Help topics
    public final static String HELP_LEAGUE = "League Settings";
    public final static String HELP_RANKINGS = "Rankings";
    public final static String HELP_PLAYER_INFO = "Player Info";
    public final static String HELP_DRAFTING = "Drafting";
    public final static String HELP_ADP_SIMULARTOR = "ADP Simulator";
    public final static String HELP_COMPARE_PLAYERS = "Compare Players";
    public final static String HELP_SORT_PLAYERS = "Sort Players";
    public final static String HELP_NEWS = "News";
    public final static String HELP_EXPORT = "Exporting Rankings";
    public final static String HELP_PROFILE = "Your Stuff";
    public final static String HELP_STATS = "Stat Explanations";
    public final static String HELP_REFRESH = "When To Refresh Rankings";

    // Sort boolean factors
    public final static String SORT_DEFAULT_STRING = "Additional Factors";
    public final static String SORT_HIDE_DRAFTED = "Hide Drafted";
    public final static String SORT_EASY_SOS = "Easy SOS";
    public final static String SORT_ONLY_HEALTHY = "Healthy";
    public final static String SORT_ONLY_WATCHED = "On Watch List";
    public final static String SORT_ONLY_ROOKIES = "Rookies Only";
    public final static String SORT_UNDER_30 = "Under 30";
    public final static String SORT_IGNORE_LATE = "Ignore late rounds";
    public final static String SORT_IGNORE_EARLY = "Ignore early rounds";

    // Sort boolean thresholds
    public final static Integer SORT_EASY_SOS_THRESHOLD = 10;
    public final static Integer SORT_YOUNG_THRESHOLD = 30;
    public final static Double SORT_IGNORE_LATE_THRESHOLD_ROUNDS = 11.0;
    public final static Double SORT_IGNORE_EARLY_THRESHOLD_ROUNDS = 3.0;

    // News sources
    public static final String RW_HEADLINE_TITLE = "Rotoworld Headline News";
    public static final String RW_PLAYER_TITLE = "Rotoworld Player News";
    public static final String MFL_AGGREGATE_TITLE = "MFL Aggregate News";

    // Flashbar constants
    public static final Long FLASHBAR_DURATION = 2500L;
    public static final Long FLASHBAR_WITH_RESPONSE_DURATION = 3000L;
    public static final Long FLASHBAR_ANIMATION_ENTER_DURATION = 750L;
    public static final Long FLASHBAR_ANIMATION_EXIT_DURATION = 400L;

    // SP: keys
    public static final String APP_KEY = "FFRv2";
    public static final String LEAGUE_NAME = "CURRENT_LEAGUE_NAME";
    public static final String NUM_PLAYERS = "MAX_PLAYERS_VISIBLE";
    public static final String RANKINGS_FETCHED = "RANKINGS_FETCHED";
    public static final String NEWS_SOURCE = "NEWS_SOURCE";
    public static final String HIDE_DRAFTED_SEARCH = "HIDE_DRAFTED_SEARCH";
    public static final String HIDE_DRAFTED_COMPARATOR_LIST = "HIDE_DRAFTED_COMPARATOR_LIST";
    public static final String HIDE_DRAFTED_COMPARATOR_SUGGESTION = "HIDE_DRAFTED_COMPARATOR_SUGGESTION";
    public static final String HIDE_DRAFTED_SORT_OUTPUT = "HIDE_DRAFTED_SORT_OUTPUT";
    public static final String HIDE_RANKLESS_SEARCH = "HIDE_RANKLESS_SEARCH";
    public static final String HIDE_RANKLESS_COMPARATOR_LIST = "HIDE_RANKLESS_COMPARATOR_LIST";
    public static final String HIDE_RANKLESS_COMPARATOR_SUGGESTION = "HIDE_RANKLESS_COMPARATOR_SUGGESTION";
    public static final String HIDE_RANKLESS_SORT_OUTPUT = "HIDE_RANKLESS_SORT_OUTPUT";
    public static final String REFRESH_RANKS_ON_OVERSCROLL = "REFRESH_RANKS_ON_OVERSCROLL";
    public static final String PLAYER_COMMENT_COUNT_PREFIX = "PLAYER_COMMENT_COUNT";

    // SP: defaults
    public static final String NOT_SET_KEY = "NOT_SAVED";
    public static final Integer DEFAULT_NUM_PLAYERS = 500;
    public static final Boolean NOT_SET_BOOLEAN = false;

    // League settings: SQL
    public static final String LEAGUE_TABLE_NAME = "league_settings";
    public static final String NAME_COLUMN = "league_name";
    public static final String TEAM_COUNT_COLUMN = "team_count";
    public static final String IS_AUCTION_COLUMN = "is_auction";
    public static final String IS_DYNASTY_STARTUP_COLUMN = "is_dynasty_startup";
    public static final String IS_DYNASTY_ROOKIE_COLUMN = "is_dynasty_rookie";
    public static final String IS_SNAKE_COLUMN = "is_snake";
    public static final String IS_BEST_BALL_COLUMN = "is_best_ball";
    public static final String AUCTION_BUDGET_COLUMN = "auction_budget";
    public static final String SCORING_ID_COLUMN = "scoring_id";
    public static final String ROSTER_ID_COLUMN = "roster_id";

    // League settings: defaults
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
    public static final Integer AUCTION_TEAM_SCALE_COUNT = 12;
    public static final Double AUCTION_TEAM_SCALE_THRESHOLD = 0.70;

    // Player values: defaults
    public static final Double DEFAULT_RISK = 50.0;
    public static final Double DEFAULT_RANK = 500.0;
    public static final String DEFAULT_DISPLAY_RANK_NOT_SET = "N/A";

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
    public static final String SCHEDULE_COLUMN = "schedule";

    // Player settings: SQL
    public static final String PLAYER_TABLE_NAME = "player_data";
    public static final String PLAYER_CUSTOM_TABLE_NAME = "custom_player_data";
    public static final String PLAYER_PROJECTIONS_TABLE_NAME = "player_projections";
    public static final String PLAYER_NAME_COLUMN = "player_name";
    public static final String PLAYER_POSITION_COLUMN = "position";
    public static final String PLAYER_AGE_COLUMN = "age";
    public static final String PLAYER_EXPERIENCE_COLUMN = "experience";
    public static final String PLAYER_ECR_COLUMN = "ecr";
    public static final String PLAYER_ADP_COLUMN = "adp";
    public static final String PLAYER_DYNASTY_COLUMN = "dynasty";
    public static final String PLAYER_ROOKIE_COLUMN = "rookie";
    public static final String PLAYER_BEST_BALL_COLUMN = "best_ball";
    public static final String PLAYER_RISK_COLUMN = "risk";
    public static final String PLAYER_STATS_COLUMN = "stats";
    public static final String PLAYER_INJURED_COLUMN = "injury_status";
    public static final String PLAYER_PROJECTION_COLUMN = "projection";
    public static final String PLAYER_PROJECTION_DATE_COLUMN = "projection_date";
    public static final String PLAYER_PAA_COLUMN = "paa";
    public static final String PLAYER_XVAL_COLUMN = "xval";
    public static final String PLAYER_VORP_COLUMN = "vorp";
    // Team name is already in team settings
    public static final String PLAYER_NOTE_COLUMN = "player_note";
    public static final String PLAYER_WATCHED_COLUMN = "player_watched";
    public static final String AUCTION_VALUE_COLUMN = "auction_value";

    // Other internal stuff
    public final static String PLAYER_ID_DELIMITER = ".";
    public final static String HASH_DELIMITER = "###";
    public final static String LINE_BREAK = "\n";
    public final static String RANKINGS_LIST_DELIMITER = ": ";
    public final static String PLAYER_ID = "player_id";
    public final static String RANKINGS_UPDATED = "rankings_updated";
    public final static Long OVERSCROLL_REFRESH_THRESHOLD = 30L;

    // Draft
    public final static String CURRENT_DRAFT = "current_draft";
    public final static String CURRENT_TEAM = "current_team";
    public final static String DISPLAY_DRAFTED = "Drafted";
    public final static String COMPARATOR_DRAFTED_SUFFIX = " *";

    // Comparator list display constants
    public static final int COMPARATOR_LIST_MAX = 250;

    // Display rankings utilities
    public final static String PLAYER_BASIC = "main";
    public final static String PLAYER_INFO = "info";
    public final static String PLAYER_STATUS = "status";
    public final static String PLAYER_ADDITIONAL_INFO = "additional_info";
    public final static String PLAYER_ADDITIONAL_INFO_2 = "additional_info_2";
    public final static String POS_TEAM_DELIMITER = " - ";
    public final static String COMPARATOR_SCALED_PREFIX = " (";
    public final static String COMPARATOR_SCALED_SUFFIX = ")";
    public final static String NOTE_SUB = "Click to update note, click and hold to clear";
    public final static String DEFAULT_NOTE = "No note found";
    public final static String NO_TEAM = "Free Agent/Retired";
    public final static String NESTED_SPINNER_DISPLAY = "nested_display";

    // Comments
    public final static String COMMENT_AUTHOR = "author";
    public final static String COMMENT_CONTENT = "content";
    public final static String COMMENT_TIMESTAMP = "timestamp";
    public final static String COMMENT_ID = "id";
    public final static String COMMENT_REPLY_DEPTH = "reply_depth";
    public final static String COMMENT_NO_REPLY_ID = "not_reply";
    public final static String COMMENT_REPLY_ID = "reply_id";
    public final static String COMMENT_UPVOTE_IMAGE = "upvote_img";
    public final static String COMMENT_DOWNVOTE_IMAGE = "downvote_img";
    public final static String COMMENT_UPVOTE_COUNT = "upvote_count";
    public final static String COMMENT_DOWNVOTE_COUNT = "downvote_count";
    public final static String COMMENT_UPVOTE = "comment_upvote";
    public final static String COMMENT_DOWNVOTE = "comment_downvote";
    public final static String COMMENT_SORT_KEY = "comment_sort_type";
    public final static String COMMENT_SORT_TOP = "Sort By Upvotes";
    public final static String COMMENT_SORT_DATE = "Sort By Date";

    // Tags
    public final static String SP_TAG_SUFFIX = "tag";
    public final static String TAG_TEXT_DELIMITER = ": ";
    public final static String AGING_TAG = "Aging";
    public final static String BOOM_OR_BUST_TAG = "Boom or Bust";
    public final static String BOUNCE_BACK_TAG = "Bounce Back";
    public final static String BREAKOUT_TAG = "Breakout";
    public final static String BUST_TAG = "Bust";
    public final static String CONSISTENT_TAG = "Consistent";
    public final static String EFFICIENT_TAG = "Efficient";
    public final static String HANDCUFF_TAG = "Handcuff";
    public final static String INEFFICIENT_TAG = "Inefficient";
    public final static String INJURY_BOUNCE_BACK_TAG = "Injury Bounce Back";
    public final static String INJURY_PRONE_TAG = "Injury Prone";
    public final static String LOTTERY_TICKET_TAG = "Lottery Ticket";
    public final static String NEW_STAFF_TAG = "New Coaching Staff";
    public final static String NEW_TEAM_TAG = "New Team";
    public final static String OVERVALUED_TAG = "Overvalued";
    public final static String POST_HYPE_SLEEPER_TAG = "Post-Hype Sleeper";
    public final static String PPR_SPECIALIST_TAG = "PPR Specialist";
    public final static String RETURNER_TAG = "Returner";
    public final static String RISKY_TAG = "Risky";
    public final static String SAFE_TAG = "Safe";
    public final static String SLEEPER_TAG = "Sleeper";
    public final static String STUD_TAG = "Stud";
    public final static String UNDERVALUED_TAG = "Undervalued";
    public final static String AGING_TAG_REMOTE = "aging";
    public final static String BOOM_OR_BUST_TAG_REMOTE = "boomOrBust";
    public final static String BOUNCE_BACK_TAG_REMOTE = "bounceBack";
    public final static String BREAKOUT_TAG_REMOTE = "breakout";
    public final static String BUST_TAG_REMOTE = "bust";
    public final static String CONSISTENT_TAG_REMOTE = "consistentScorer";
    public final static String EFFICIENT_TAG_REMOTE = "efficient";
    public final static String HANDCUFF_TAG_REMOTE = "handcuff";
    public final static String INEFFICIENT_TAG_REMOTE = "inefficient";
    public final static String INJURY_BOUNCE_BACK_TAG_REMOTE = "injuryBounceBack";
    public final static String INJURY_PRONE_TAG_REMOTE = "injuryProne";
    public final static String LOTTERY_TICKET_TAG_REMOTE = "lotteryTicket";
    public final static String NEW_STAFF_TAG_REMOTE = "newStaff";
    public final static String NEW_TEAM_TAG_REMOTE = "newTeam";
    public final static String OVERVALUED_TAG_REMOTE = "overvalued";
    public final static String POST_HYPE_SLEEPER_TAG_REMOTE = "postHypeSleeper";
    public final static String PPR_SPECIALIST_TAG_REMOTE = "pprSpecialist";
    public final static String RETURNER_TAG_REMOTE = "returner";
    public final static String RISKY_TAG_REMOTE = "risky";
    public final static String SAFE_TAG_REMOTE = "safe";
    public final static String SLEEPER_TAG_REMOTE = "sleeper";
    public final static String STUD_TAG_REMOTE = "stud";
    public final static String UNDERVALUED_TAG_REMOTE = "undervalued";

    // Non-primitives
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
}
