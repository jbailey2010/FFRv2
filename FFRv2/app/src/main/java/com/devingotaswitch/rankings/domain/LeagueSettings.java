package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.utils.Constants;

public class LeagueSettings {

    private String name;
    private int teamCount;

    private boolean isAuction;
    private boolean isDynasty;
    private boolean isRookie;
    private boolean isSnake;
    private int auctionBudget;

    private ScoringSettings scoringSettings;
    private RosterSettings rosterSettings;

    public LeagueSettings(String name, int teamCount, boolean isSnake, boolean isAuction, boolean isDynasty,
                          boolean isRookie, int auctionBudget) {
        this(name, teamCount, isSnake, isAuction, isDynasty, isRookie, auctionBudget, new ScoringSettings(),
                new RosterSettings());
    }

    public LeagueSettings(String name, int teamCount, boolean isSnake, boolean isAuction, boolean isDynasty,
                          boolean isRookie, int auctionBudget, ScoringSettings scoring, RosterSettings roster) {
        this.setName(name);
        this.setTeamCount(teamCount);
        this.setSnake(isSnake);
        this.setAuction(isAuction);
        this.setDynasty(isDynasty);
        this.setRookie(isRookie);
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

    public boolean isDynasty() {
        return isDynasty;
    }

    public boolean isRookie() {
        return isRookie;
    }

    public boolean isSnake() {
        return isSnake;
    }

    public void setAuction(boolean auction) {
        isAuction = auction;
    }

    public void setDynasty(boolean dynasty) {
        isDynasty = dynasty;
    }

    public void setRookie(boolean rookie) {
        isRookie = rookie;
    }

    public void setSnake(boolean snake) {
        isSnake = snake;
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
