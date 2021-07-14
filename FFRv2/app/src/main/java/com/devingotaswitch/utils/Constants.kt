package com.devingotaswitch.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*

object Constants {
    @JvmField
    val YEAR_KEY = Year.now().value.toString()
    @JvmField
    val LAST_YEAR_KEY = (YEAR_KEY.toInt() - 1).toString()

    // Positions
    const val QB = "QB"
    const val RB = "RB"
    const val WR = "WR"
    const val TE = "TE"
    const val DST = "D/ST"
    const val K = "K"

    // Flex positions
    const val RBTE = "RB/TE"
    const val RBWR = "RB/WR"
    const val RBWRTE = "RB/WR/TE"
    const val WRTE = "WR/TE"
    const val QBRBWRTE = "QB/RB/WR/TE"

    // Filter constants
    const val ALL_POSITIONS = "All Positions"
    const val ALL_TEAMS = "All Teams"

    // Sort factors
    const val SORT_BACK = "Back"
    const val SORT_ALL = "Default Sort"
    const val SORT_ECR = "ECR"
    const val SORT_ADP = "ADP"
    const val SORT_UNDERDRAFTED = "ADP Good Values"
    const val SORT_OVERDRAFTED = "ADP Bad Values"
    const val SORT_AUCTION = "Auction Value"
    const val SORT_DYNASTY = "Dynasty/Keeper Rank"
    const val SORT_ROOKIE = "Rookie Rank"
    const val SORT_BEST_BALL = "Best Ball Rank"
    const val SORT_PROJECTION_EXPANDED = "Projections >"
    const val SORT_PROJECTION = "Total Points"
    const val SORT_PASSING_TDS = "Passing TDs"
    const val SORT_PASSING_YDS = "Passing Yards"
    const val SORT_RUSHING_TDS = "Rushing TDs"
    const val SORT_RUSHING_YDS = "Rushing Yards"
    const val SORT_RECEIVING_TDS = "Receiving TDs"
    const val SORT_RECEIVING_YDS = "Receiving Yards"
    const val SORT_RECEPTIONS = "Receptions"
    const val SORT_VBD_EXPANDED = "VBD >"
    const val SORT_PAA = "PAA"
    const val SORT_PAA_SCALED = "Scaled PAA"
    const val SORT_PAAPD = "PAA Per Dollar"
    const val SORT_XVAL = "X Value"
    const val SORT_XVAL_SCALED = "Scaled X Value"
    const val SORT_XVALPD = "X Value Per Dollar"
    const val SORT_VOLS = "VoLS"
    const val SORT_VOLS_SCALED = "Scaled VoLS"
    const val SORT_VOLSPD = "VoLS Per Dollar"
    const val SORT_VBD_SUGGESTED = "VBD Suggested Pick"
    const val SORT_RISK = "Risk"
    const val SORT_SOS = "Positional SOS"

    // Help topics
    const val HELP_LEAGUE = "League Settings"
    const val HELP_RANKINGS = "Rankings"
    const val HELP_PLAYER_INFO = "Player Info"
    const val HELP_DRAFTING = "Drafting"
    const val HELP_ADP_SIMULARTOR = "ADP Simulator"
    const val HELP_COMPARE_PLAYERS = "Compare Players"
    const val HELP_SORT_PLAYERS = "Sort Players"
    const val HELP_NEWS = "News"
    const val HELP_EXPORT = "Exporting Rankings"
    const val HELP_PROFILE = "Your Stuff"
    const val HELP_STATS = "Stat Explanations"
    const val HELP_REFRESH = "When To Refresh Rankings"

    // Sort boolean factors
    const val SORT_DEFAULT_STRING = "Additional Factors"
    const val SORT_HIDE_DRAFTED = "Hide Drafted"
    const val SORT_EASY_SOS = "Easy SOS"
    const val SORT_ONLY_HEALTHY = "Healthy"
    const val SORT_ONLY_WATCHED = "On Watch List"
    const val SORT_ONLY_ROOKIES = "Rookies Only"
    const val SORT_UNDER_30 = "Under 30"
    const val SORT_IGNORE_LATE = "Ignore late rounds"
    const val SORT_IGNORE_EARLY = "Ignore early rounds"

    // Sort boolean thresholds
    const val SORT_EASY_SOS_THRESHOLD = 10
    const val SORT_YOUNG_THRESHOLD = 30
    const val SORT_IGNORE_LATE_THRESHOLD_ROUNDS = 11.0
    const val SORT_IGNORE_EARLY_THRESHOLD_ROUNDS = 3.0

    // News sources
    const val MFL_AGGREGATE_TITLE = "MFL Aggregate News"
    const val FP_ALL_NEWS = "FantasyPros Aggregate News"
    const val FP_BREAKING_NEWS_TITLE = "FantasyPros Breaking News"
    const val FP_RUMORS_TITLE = "FantasyPros Rumors"
    const val FP_INJURY_TITLE = "FantasyPros Injuries"
    const val SIP_INJURY_TITLE = "Sports Injury Predictor Injuries"
    const val SPOTRAC_TRANSACTIONS_TITLE = "Spotrac Transactions"

    // Flashbar constants
    const val FLASHBAR_DURATION = 2500L
    const val FLASHBAR_WITH_RESPONSE_DURATION = 3000L
    const val FLASHBAR_ANIMATION_ENTER_DURATION = 750L
    const val FLASHBAR_ANIMATION_EXIT_DURATION = 400L

    // SP: keys
    const val APP_KEY = "FFRv2"
    const val LEAGUE_NAME = "CURRENT_LEAGUE_NAME"
    const val NUM_PLAYERS = "MAX_PLAYERS_VISIBLE"
    const val RANKINGS_FETCHED = "RANKINGS_FETCHED"
    const val NEWS_SOURCE = "NEWS_SOURCE"
    const val PLAYER_COMMENT_COUNT_PREFIX = "PLAYER_COMMENT_COUNT"
    const val PLAYER_NEWS = "PLAYER_NEWS"
    const val LAST_RANKINGS_FETCHED_TIME = "LAST_RANKINGS_FETCHED_TIME"
    const val CACHE_DELIMITER = "~~~"
    const val CACHE_ITEM_DELIMITER = "###"

    // SP: defaults
    const val NOT_SET_KEY = "NOT_SAVED"
    const val DEFAULT_NUM_PLAYERS = 500
    const val NOT_SET_BOOLEAN = false

    // League settings: SQL
    const val LEAGUE_TABLE_NAME = "league_settings"
    const val NAME_COLUMN = "league_name"
    const val TEAM_COUNT_COLUMN = "team_count"
    const val IS_AUCTION_COLUMN = "is_auction"
    const val IS_DYNASTY_STARTUP_COLUMN = "is_dynasty_startup"
    const val IS_DYNASTY_ROOKIE_COLUMN = "is_dynasty_rookie"
    const val IS_SNAKE_COLUMN = "is_snake"
    const val IS_BEST_BALL_COLUMN = "is_best_ball"
    const val AUCTION_BUDGET_COLUMN = "auction_budget"
    const val CURRENT_LEAGUE_COLUMN = "current_league"
    const val SCORING_ID_COLUMN = "scoring_id"
    const val ROSTER_ID_COLUMN = "roster_id"

    // League settings: defaults
    const val DEFAULT_AUCTION_BUDGET = 200

    // Scoring settings: SQL
    const val SCORING_TABLE_NAME = "scoring_settings"

    // Scoring id is already in league settings
    const val PASSING_TDS_COLUMN = "pts_per_passing_td"
    const val RUSHING_TDS_COLUMN = "pts_per_rushing_td"
    const val RECEIVING_TDS_COLUMN = "pts_per_receiving_td"
    const val FUMBLES_COLUMN = "pts_per_fumble"
    const val INTERCEPTIONS_COLUMN = "pts_per_interception"
    const val PASSING_YARDS_COLUMN = "passing_yards_per_point"
    const val RUSHING_YARDS_COLUMN = "rushing_yards_per_point"
    const val RECEIVING_YARDS_COLUMN = "receiving_yards_per_point"
    const val RECEPTIONS_COLUMN = "pts_per_reception"

    // Scoring settings: defaults
    const val DEFAULT_PASSING_TD_WORTH = 4
    const val DEFAULT_TD_WORTH = 6
    const val DEFAULT_TURNOVER_WORTH = -2
    const val DEFAULT_PASSING_YDS = 25
    const val DEFAULT_RUSHING_YDS = 10
    const val DEFAULT_RECEIVING_YDS = 10
    const val DEFAULT_RECEPTIONS = 0.5

    // Roster settings: SQL
    const val ROSTER_TABLE_NAME = "roster_settings"

    // Roster id is already in league settings
    const val QB_COUNT_COLUMN = "starting_qbs"
    const val RB_COUNT_COLUMN = "starting_rbs"
    const val WR_COUNT_COLUMN = "starting_wrs"
    const val TE_COUNT_COLUMN = "starting_tes"
    const val DST_COUNT_COLUMN = "starting_dsts"
    const val K_COUNT_COLUMN = "starting_ks"
    const val BENCH_COUNT_COLUMN = "bench_count"
    const val RBWR_COUNT_COLUMN = "starting_rbwr"
    const val RBTE_COUNT_COLUMN = "starting_rbte"
    const val RBWRTE_COUNT_COLUMN = "starting_rbwrte"
    const val WRTE_COUNT_COLUMN = "starting_wrte"
    const val QBRBWRTE_COUNT_COLUMN = "starting_qbrbwrte"

    // Roster settings: defaults
    const val NO_STARTERS = 0
    const val ONE_STARTER = 1
    const val TWO_STARTERS = 2
    const val BENCH_DEFAULT = 6
    const val AUCTION_TEAM_SCALE_COUNT = 12
    const val AUCTION_TEAM_SCALE_THRESHOLD = 0.70

    // Player values: defaults
    const val DEFAULT_RISK = 50.0
    const val DEFAULT_RANK = 500.0
    const val DEFAULT_VBD = 0.0
    const val DEFAULT_DISPLAY_RANK_NOT_SET = "N/A"
    const val DEFAULT_SOS = -1.0

    // Team settings: SQL
    const val TEAM_TABLE_NAME = "team_data"
    const val TEAM_NAME_COLUMN = "team_name"
    const val OLINE_RANKS_COLUMN = "oline_ranks"
    const val DRAFT_CLASS_COLUMN = "draft_class"
    const val QB_SOS_COLUMN = "qb_sos"
    const val RB_SOS_COLUMN = "rb_sos"
    const val WR_SOS_COLUMN = "wr_sos"
    const val TE_SOS_COLUMN = "te_sos"
    const val DST_SOS_COLUMN = "dst_sos"
    const val K_SOS_COLUMN = "k_sos"
    const val BYE_COLUMN = "bye_week"
    const val FREE_AGENCY_COLUMN = "fa_class"
    const val SCHEDULE_COLUMN = "schedule"

    // Player settings: SQL
    const val PLAYER_TABLE_NAME = "player_data"
    const val PLAYER_CUSTOM_TABLE_NAME = "custom_player_data"
    const val PLAYER_PROJECTIONS_TABLE_NAME = "player_projections"
    const val PLAYER_NAME_COLUMN = "player_name"
    const val PLAYER_POSITION_COLUMN = "position"
    const val PLAYER_AGE_COLUMN = "age"
    const val PLAYER_EXPERIENCE_COLUMN = "experience"
    const val PLAYER_ECR_COLUMN = "ecr"
    const val PLAYER_ADP_COLUMN = "adp"
    const val PLAYER_DYNASTY_COLUMN = "dynasty"
    const val PLAYER_ROOKIE_COLUMN = "rookie"
    const val PLAYER_BEST_BALL_COLUMN = "best_ball"
    const val PLAYER_RISK_COLUMN = "risk"
    const val PLAYER_STATS_COLUMN = "stats"
    const val PLAYER_INJURED_COLUMN = "injury_status"
    const val PLAYER_PROJECTION_COLUMN = "projection"
    const val PLAYER_PROJECTION_DATE_COLUMN = "projection_date"
    const val PLAYER_PAA_COLUMN = "paa"
    const val PLAYER_XVAL_COLUMN = "xval"
    const val PLAYER_VORP_COLUMN = "vorp"

    // Team name is already in team settings
    const val AUCTION_VALUE_COLUMN = "auction_value"

    // Other internal stuff
    const val PLAYER_ID_DELIMITER = "."
    const val HASH_DELIMITER = "###"
    const val LINE_BREAK = "\n"
    const val RANKINGS_LIST_DELIMITER = ": "
    const val PLAYER_ID = "player_id"
    const val RANKINGS_UPDATED = "rankings_updated"
    const val RANKINGS_LIST_RELOAD_NEEDED = "rankings_list_reload_needed"
    const val OVERSCROLL_REFRESH_THRESHOLD = 30L
    const val OVERSCROLL_DEPTH_THRESHOLD = -40
    const val TEST_DEBUG_TAG = "JEFFF"

    // Draft
    const val CURRENT_DRAFT = "current_draft"
    const val CURRENT_TEAM = "current_team"
    const val DISPLAY_DRAFTED = "Drafted"
    const val COMPARATOR_DRAFTED_SUFFIX = " *"

    // Comparator
    const val COMPARATOR_LIST_MAX = 250

    // Display rankings utilities
    const val PLAYER_BASIC = "main"
    const val PLAYER_INFO = "info"
    const val PLAYER_STATUS = "status"
    const val PLAYER_ADDITIONAL_INFO = "additional_info"
    const val PLAYER_ADDITIONAL_INFO_2 = "additional_info_2"
    const val POS_TEAM_DELIMITER = " - "
    const val COMPARATOR_SCALED_PREFIX = " ("
    const val COMPARATOR_SCALED_SUFFIX = ")"
    const val NOTE_SUB = "Click to update note, click and hold to clear"
    const val DEFAULT_NOTE = "No note found"
    const val NO_TEAM = "Free Agent/Retired"
    const val NESTED_SPINNER_DISPLAY = "nested_display"

    // Comments
    const val COMMENT_AUTHOR = "author"
    const val COMMENT_CONTENT = "content"
    const val COMMENT_TIMESTAMP = "timestamp"
    const val COMMENT_ID = "id"
    const val COMMENT_REPLY_DEPTH = "reply_depth"
    const val COMMENT_NO_REPLY_ID = "not_reply"
    const val COMMENT_REPLY_ID = "reply_id"
    const val COMMENT_UPVOTE_IMAGE = "upvote_img"
    const val COMMENT_DOWNVOTE_IMAGE = "downvote_img"
    const val COMMENT_UPVOTE_COUNT = "upvote_count"
    const val COMMENT_DOWNVOTE_COUNT = "downvote_count"
    const val COMMENT_SORT_KEY = "comment_sort_type"
    const val COMMENT_SORT_TOP = "Sort By Upvotes"
    const val COMMENT_SORT_DATE = "Sort By Date"

    // Tags
    const val TAG_TEXT_DELIMITER = ": "
    const val AGING_TAG = "Aging"
    const val BOOM_OR_BUST_TAG = "Boom or Bust"
    const val BOUNCE_BACK_TAG = "Bounce Back"
    const val BREAKOUT_TAG = "Breakout"
    const val BUST_TAG = "Bust"
    const val CONSISTENT_TAG = "Consistent"
    const val EFFICIENT_TAG = "Efficient"
    const val HANDCUFF_TAG = "Handcuff"
    const val INEFFICIENT_TAG = "Inefficient"
    const val INJURY_BOUNCE_BACK_TAG = "Injury Bounce Back"
    const val INJURY_PRONE_TAG = "Injury Prone"
    const val LOTTERY_TICKET_TAG = "Lottery Ticket"
    const val NEW_STAFF_TAG = "New Coaching Staff"
    const val NEW_TEAM_TAG = "New Team"
    const val OVERVALUED_TAG = "Overvalued"
    const val POST_HYPE_SLEEPER_TAG = "Post-Hype Sleeper"
    const val PPR_SPECIALIST_TAG = "PPR Specialist"
    const val RETURNER_TAG = "Returner"
    const val RISKY_TAG = "Risky"
    const val SAFE_TAG = "Safe"
    const val SLEEPER_TAG = "Sleeper"
    const val STUD_TAG = "Stud"
    const val UNDERVALUED_TAG = "Undervalued"
    const val AGING_TAG_REMOTE = "aging"
    const val BOOM_OR_BUST_TAG_REMOTE = "boomOrBust"
    const val BOUNCE_BACK_TAG_REMOTE = "bounceBack"
    const val BREAKOUT_TAG_REMOTE = "breakout"
    const val BUST_TAG_REMOTE = "bust"
    const val CONSISTENT_TAG_REMOTE = "consistentScorer"
    const val EFFICIENT_TAG_REMOTE = "efficient"
    const val HANDCUFF_TAG_REMOTE = "handcuff"
    const val INEFFICIENT_TAG_REMOTE = "inefficient"
    const val INJURY_BOUNCE_BACK_TAG_REMOTE = "injuryBounceBack"
    const val INJURY_PRONE_TAG_REMOTE = "injuryProne"
    const val LOTTERY_TICKET_TAG_REMOTE = "lotteryTicket"
    const val NEW_STAFF_TAG_REMOTE = "newStaff"
    const val NEW_TEAM_TAG_REMOTE = "newTeam"
    const val OVERVALUED_TAG_REMOTE = "overvalued"
    const val POST_HYPE_SLEEPER_TAG_REMOTE = "postHypeSleeper"
    const val PPR_SPECIALIST_TAG_REMOTE = "pprSpecialist"
    const val RETURNER_TAG_REMOTE = "returner"
    const val RISKY_TAG_REMOTE = "risky"
    const val SAFE_TAG_REMOTE = "safe"
    const val SLEEPER_TAG_REMOTE = "sleeper"
    const val STUD_TAG_REMOTE = "stud"
    const val UNDERVALUED_TAG_REMOTE = "undervalued"

    // Trending
    const val TRENDING_ID_DELIMITER = "-.-"

    // Non-primitives
    @JvmField
    val DECIMAL_FORMAT = DecimalFormat("#.##")
    @JvmField
    val DATE_FORMAT = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
}