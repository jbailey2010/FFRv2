package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.utils.Constants;

public class Player {

    private String name;
    private Integer age;
    private String position;
    private Double ecr;
    private Double adp;
    private String teamName;
    private String note;
    private boolean isWatched;
    private Double auctionValue = 0.0;
    private Double numRankings = 0.0;

    private Double projection;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isWatched() { return isWatched; }

    public void setWatched(boolean isWatched) { this.isWatched = isWatched; }

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
