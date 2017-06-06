package com.devingotaswitch.rankings.domain;

public class Player {

    private String name;
    private Integer age;
    private String position;
    private Double ecr;
    private Double adp;
    private String teamName;
    private String note;
    private boolean isWatched;

    private Double rankingCount;
    private Double auctionValue;

    //TODO: Projection stuff, points/paa...etc.?

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

    private Double getRankingCount() {
        return rankingCount;
    }

    private void setRankingCount(Double rankingCount) {
        this.rankingCount = rankingCount;
    }

    public Double getAuctionValue() {
        return auctionValue;
    }

    private void setAuctionValue(Double auctionValue) {
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
}
