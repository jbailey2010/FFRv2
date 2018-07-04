package com.devingotaswitch.rankings.domain;

import android.util.Log;

import com.devingotaswitch.utils.Constants;

public class Player {

    private static final String TAG = "Player";

    private String name;
    private Integer age;
    private String position;
    private Double ecr = 300.0;
    private Double adp = 300.0;
    private Double dynastyRank = 300.0;
    private Double rookieRank = 300.0;
    private String teamName;
    private String note;
    private String stats;
    private String injuryStatus;
    private boolean isWatched;
    private Double auctionValue = 0.0;
    private Double numRankings = 0.0;
    private Double risk = 50.0;
    private Double projection = 0.0;
    private Double paa;
    private Double xVal;
    private Double vOLS;
    private Integer positionalTier;

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

    public Double getDynastyRank() {
        return dynastyRank;
    }

    public void setDynastyRank(Double dynasty) {
        this.dynastyRank = dynasty;
    }

    public Double getRookieRank() {
        return rookieRank;
    }

    public void setRookieRank(Double rookie) {
        this.rookieRank = rookie;
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
      return getAuctionValueCustom(rankings.getLeagueSettings().getTeamCount(), rankings.getLeagueSettings().getAuctionBudget());
    }

    public Double getAuctionValueCustom(int teamCount, int auctionBudget) {
        if (getAuctionValue() <= 1.0) {
            return getAuctionValue();
        }
        double scalar = ((double)auctionBudget) / ((double)Constants.DEFAULT_AUCTION_BUDGET);
        if (teamCount > Constants.AUCTION_TEAM_SCALE_COUNT) {
            // First, get the extra % of money. If there's 14 teams, that means 14/12 = 1.16667 = 16.6667 % more money.
            // To limit crazy numbers, it's capped at 16 teams/33.333% above.
            double teamScaleDelta = (Math.min((double)teamCount, 16.0)) /
                    ((double)Constants.AUCTION_TEAM_SCALE_COUNT) - 1.0;
            // Next, scale that down a bit.
            teamScaleDelta *= Constants.AUCTION_TEAM_SCALE_THRESHOLD;
            // Finally, add it back to 1 so we can scale values accordingly, x * (1.16667 * scale down factor).
            teamScaleDelta += 1.0;
            scalar *= teamScaleDelta;
        }
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

    public Double getvOLS() {
        return vOLS;
    }

    public void setvOLS(Double vOLS) {
        this.vOLS = vOLS;
    }

    public Integer getPositionalTier() { return positionalTier; }

    public void setPositionalTier(int tier) { this.positionalTier = tier; }

    public void handleNewValue(Double newValue) {
        double auctionTotal = auctionValue * numRankings;
        numRankings++;
        auctionTotal += newValue;
        auctionValue = auctionTotal / numRankings;
    }

    public String getUniqueId() {
        return name +
                Constants.PLAYER_ID_DELIMITER +
                teamName +
                Constants.PLAYER_ID_DELIMITER +
                position;
    }

    public Double getScaledPAA(Rankings rankings) {
        return getScaledValue(getPaa(), rankings.getLeagueSettings().getRosterSettings().getNumberStartedOfPos(getPosition()),
                rankings.getDraft().getPlayersDraftedForPos(getPosition()).size());
    }

    public Double getScaledXVal(Rankings rankings) {
        return getScaledValue(getxVal(), rankings.getLeagueSettings().getRosterSettings().getNumberStartedOfPos(getPosition()),
                rankings.getDraft().getPlayersDraftedForPos(getPosition()).size());
    }

    public Double getScaledVoLS(Rankings rankings) {
        return getScaledValue(getvOLS(), rankings.getLeagueSettings().getRosterSettings().getNumberStartedOfPos(getPosition()),
                rankings.getDraft().getPlayersDraftedForPos(getPosition()).size());
    }

    private double getScaledValue(Double value, int numStarted, int numDrafted) {
        double scaleFactor = 0.0;
        if (numStarted == 0) {
            scaleFactor = 0.0;
        } else if (numDrafted == 0 || numDrafted < numStarted) {
            scaleFactor = 1.0;
        } else {
            scaleFactor = 1.0 - (((double)numDrafted - (numStarted - 1)) * 0.2);
            if (scaleFactor <= 0.0) {
                scaleFactor = 0.2;
            }
        }

        if (value < 0) {
            // If it's negative, multiplying by a smaller value would make it look better
            return value / scaleFactor;
        } else {
            return value * scaleFactor;
        }
    }
}
