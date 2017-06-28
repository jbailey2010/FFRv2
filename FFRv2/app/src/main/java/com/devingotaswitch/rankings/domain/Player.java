package com.devingotaswitch.rankings.domain;

import android.util.Log;

import com.devingotaswitch.utils.Constants;

public class Player {

    private String name;
    private Integer age;
    private String position;
    private Double ecr = 300.0;
    private Double adp = 300.0;
    private String teamName;
    private String note;
    private String stats;
    private String injuryStatus;
    private boolean isWatched;
    private Double auctionValue = 0.0;
    private Double numRankings = 0.0;
    private Double risk;
    private Double projection = 0.0;
    private Double paa;
    private Double xVal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Double getEcr() {
        return ecr;
    }

    public void setEcr(Double ecr) {
        this.ecr = ecr;
    }

    public Double getAdp() {
        return adp;
    }

    public void setAdp(Double adp) {
        this.adp = adp;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Double getAuctionValue() {
        return auctionValue;
    }

    public void setAuctionValue(Double auctionValue) {
        this.auctionValue = auctionValue;
    }

    public Double getAuctionValueCustom(Rankings rankings) {
        double scalar = ((double)rankings.getLeagueSettings().getAuctionBudget()) / ((double)Constants.DEFAULT_AUCTION_BUDGET);
        return getAuctionValue() * scalar;
    }
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStats() { return stats; }

    public void setStats(String stats) { this.stats = stats; }

    public String getInjuryStatus() { return injuryStatus; }

    public void setInjuryStatus(String injuryStatus) { this.injuryStatus = injuryStatus; }

    public boolean isWatched() { return isWatched; }

    public void setWatched(boolean isWatched) { this.isWatched = isWatched; }

    public Double getRisk() { return risk; }

    public void setRisk(Double risk) { this.risk = risk; }

    public Double getProjection() {
        return projection;
    }

    public void setProjection(Double projection) {
        this.projection = projection;
    }

    public Double getPaa() {
        return paa;
    }

    public void setPaa(Double paa) {
        this.paa = paa;
    }

    public Double getxVal() {
        return xVal;
    }

    public void setxVal(Double xVal) {
        this.xVal = xVal;
    }

    public void handleNewValue(Double newValue) {
        double auctionTotal = auctionValue * numRankings;
        numRankings++;
        auctionTotal += newValue;
        auctionValue = auctionTotal / numRankings;
    }

    public String getUniqueId() {
        return new StringBuilder(name)
                .append(Constants.PLAYER_ID_DELIMITER)
                .append(teamName)
                .append(Constants.PLAYER_ID_DELIMITER)
                .append(position)
                .toString();
    }
}
