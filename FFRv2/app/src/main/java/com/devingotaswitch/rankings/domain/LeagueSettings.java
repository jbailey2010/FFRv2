package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.utils.Constants;

import java.util.UUID;

public class LeagueSettings {

    private String name;
    private int teamCount;

    private boolean isAuction;
    private int auctionBudget;

    private ScoringSettings scoringSettings;
    private RosterSettings rosterSettings;

    public LeagueSettings(ScoringSettings scoring, RosterSettings roster) {
        this(Constants.DEFAULT_NAME, Constants.DEFAULT_TEAM_COUNT, Constants.DEFAULT_IS_AUCTION,
                Constants.DEFAULT_AUCTION_BUDGET, scoring, roster);
    }

    public LeagueSettings(String name, int teamCount, boolean isAuction, int auctionBudget) {
        this(name, teamCount, isAuction, auctionBudget, new ScoringSettings(),
                new RosterSettings());
    }

    public LeagueSettings(String name, int teamCount, boolean isAuction,
                          int auctionBudget, ScoringSettings scoring, RosterSettings roster) {
        this.setName(name);
        this.setTeamCount(teamCount);
        this.setAuction(isAuction);
        this.setAuctionBudget(auctionBudget);
        this.setScoringSettings(scoring);
        this.setRosterSettings(roster);
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
}
