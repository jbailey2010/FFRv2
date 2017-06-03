package com.devingotaswitch.rankings.domain;

import java.util.UUID;

public class LeagueSettings {

    private static final String TABLE_NAME = "league_settings";
    private static final String ID_COLUMN = "league_id";
    private static final String NAME_COLUMN = "league_name";
    private static final String TEAM_COUNT_COLUMN = "team_count";
    private static final String IS_AUCTION_COLUMN = "is_auction";
    private static final String AUCTION_BUDGET_COLUMN = "auction_budget";
    private static final String SCORING_ID_COLUMN = "scoring_id";
    private static final String ROSTER_ID_COLUMN = "roster_id";

    private String id;
    private String name;
    private int teamCount;

    private boolean isAuction;
    private int auctionBudget;

    private ScoringSettings scoringSettings;
    private RosterSettings rosterSettings;

    private static final String DEFAULT_NAME = "My League";
    private static final Integer DEFAULT_TEAM_COUNT = 10;
    private static final Boolean DEFAULT_IS_AUCTION = true;
    private static final Integer DEFAULT_AUCTION_BUDGET = 200;

    public LeagueSettings(ScoringSettings scoring, RosterSettings roster) {
        this(UUID.randomUUID().toString(), DEFAULT_NAME, DEFAULT_TEAM_COUNT, DEFAULT_IS_AUCTION,
                DEFAULT_AUCTION_BUDGET, scoring, roster);
    }

    public LeagueSettings(String id, String name, int teamCount, boolean isAuction,
                          int auctionBudget, ScoringSettings scoring, RosterSettings roster) {
        this.setId(id);
        this.setName(name);
        this.setTeamCount(teamCount);
        this.setAuction(isAuction);
        this.setAuctionBudget(auctionBudget);
        this.setScoringSettings(scoring);
        this.setRosterSettings(roster);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    public boolean isAuction() {
        return isAuction;
    }

    public void setAuction(boolean auction) {
        isAuction = auction;
    }

    public int getAuctionBudget() {
        return auctionBudget;
    }

    public void setAuctionBudget(int auctionBudget) {
        this.auctionBudget = auctionBudget;
    }

    public ScoringSettings getScoringSettings() {
        return scoringSettings;
    }

    public void setScoringSettings(ScoringSettings scoringSettings) {
        this.scoringSettings = scoringSettings;
    }

    public RosterSettings getRosterSettings() {
        return rosterSettings;
    }

    public void setRosterSettings(RosterSettings rosterSettings) {
        this.rosterSettings = rosterSettings;
    }

    public static String getCreateTableSQL() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                ID_COLUMN              + " TEXT PRIMARY KEY," +
                NAME_COLUMN            + " TEXT," +
                TEAM_COUNT_COLUMN      + " INTEGER," +
                IS_AUCTION_COLUMN      + " BOOLEAN," +
                AUCTION_BUDGET_COLUMN  + " INTEGER," +
                SCORING_ID_COLUMN      + " TEXT," +
                ROSTER_ID_COLUMN       + " TEXT);";
    }
}
