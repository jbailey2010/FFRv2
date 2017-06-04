package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.utils.Constants;

import java.util.UUID;

public class LeagueSettings {

    private String id;
    private String name;
    private int teamCount;

    private boolean isAuction;
    private int auctionBudget;

    private ScoringSettings scoringSettings;
    private RosterSettings rosterSettings;

    public LeagueSettings(ScoringSettings scoring, RosterSettings roster) {
        this(UUID.randomUUID().toString(), Constants.DEFAULT_NAME, Constants.DEFAULT_TEAM_COUNT, Constants.DEFAULT_IS_AUCTION,
                Constants.DEFAULT_AUCTION_BUDGET, scoring, roster);
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
        return "CREATE TABLE " + Constants.LEAGUE_TABLE_NAME + " (" +
                Constants.LEAGUE_ID_COLUMN + " TEXT PRIMARY KEY," +
                Constants.NAME_COLUMN            + " TEXT," +
                Constants.TEAM_COUNT_COLUMN      + " INTEGER," +
                Constants.IS_AUCTION_COLUMN      + " BOOLEAN," +
                Constants.AUCTION_BUDGET_COLUMN  + " INTEGER," +
                Constants.SCORING_ID_COLUMN      + " TEXT," +
                Constants.ROSTER_ID_COLUMN       + " TEXT);";
    }
}
