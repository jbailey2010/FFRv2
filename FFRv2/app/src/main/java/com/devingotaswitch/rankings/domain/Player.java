package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.rankings.domain.projections.PlayerProjection;
import com.devingotaswitch.utils.Constants;

public class Player {

    private static final String TAG = "Player";

    private String name;
    private Integer age;
    private Integer experience = -1;
    private String position;
    private Double ecr = Constants.DEFAULT_RANK;
    private Double adp = Constants.DEFAULT_RANK;
    private Double dynastyRank = Constants.DEFAULT_RANK;
    private Double rookieRank = Constants.DEFAULT_RANK;
    private Double bestBallRank = Constants.DEFAULT_RANK;
    private String teamName;
    private String stats;
    private String injuryStatus;
    private Double auctionValue = 0.0;
    private Double numRankings = 0.0;
    private Double risk = Constants.DEFAULT_RISK;
    private PlayerProjection projection;
    private Double paa;
    private Double xVal;
    private Double vOLS;

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

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
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

    public Double getBestBallRank() {
        return bestBallRank;
    }

    public void setBestBallRank(Double bestBall) {
        this.bestBallRank = bestBall;
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
        double scalar = ((double) auctionBudget) / ((double) Constants.DEFAULT_AUCTION_BUDGET);
        if (teamCount > Constants.AUCTION_TEAM_SCALE_COUNT) {
            // First, get the extra % of money. If there's 14 teams, that means 14/12 = 1.16667 = 16.6667 % more money.
            // To limit crazy numbers, it's capped at 16 teams/33.333% above.
            double teamScaleDelta = (Math.min(teamCount, 16.0)) /
                    ((double) Constants.AUCTION_TEAM_SCALE_COUNT) - 1.0;
            // Next, scale that down a bit.
            teamScaleDelta *= Constants.AUCTION_TEAM_SCALE_THRESHOLD;
            // Finally, add it back to 1 so we can scale values accordingly, x * (1.16667 * scale down factor).
            teamScaleDelta += 1.0;
            scalar *= teamScaleDelta;
        }
        return getAuctionValue() * scalar;
    }

    public String getStats() { return stats; }

    public void setStats(String stats) { this.stats = stats; }

    public String getInjuryStatus() { return injuryStatus; }

    public void setInjuryStatus(String injuryStatus) { this.injuryStatus = injuryStatus; }

    public Double getRisk() { return risk; }

    public void setRisk(Double risk) { this.risk = risk; }

    public Double getProjection() {
        return projection.getFormattedProjectedPoints();
    }

    public PlayerProjection getPlayerProjection() {
        return projection;
    }

    public void updateProjection(ScoringSettings scoringSettings) {
        this.projection.updateAndGetFormattedProjectedPoints(scoringSettings);
    }

    public void setPlayerProjection(PlayerProjection projection) {
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

    public Double getVOLS() {
        return vOLS;
    }

    public void setVOLS(Double vOLS) {
        this.vOLS = vOLS;
    }

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

    public Double getScaledVOLS(Rankings rankings) {
        return getScaledValue(getVOLS(), rankings.getLeagueSettings().getRosterSettings().getNumberStartedOfPos(getPosition()),
                rankings.getDraft().getPlayersDraftedForPos(getPosition()).size());
    }

    private double getScaledValue(Double value, int numStarted, int numDrafted) {
        double scaleFactor;
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

    public String getDisplayValue(Rankings rankings) {
        LeagueSettings league = rankings.getLeagueSettings();
        if (league.isRookie()) {
            return String.valueOf(getRookieRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : getRookieRank());
        } else if (league.isDynasty()) {
            return String.valueOf(getDynastyRank().equals(Constants.DEFAULT_RANK)  ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : getDynastyRank());
        } else if (league.isSnake()) {
            return String.valueOf(getEcr().equals(Constants.DEFAULT_RANK)  ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : getEcr());
        } else if (league.isAuction()) {
            return Constants.DECIMAL_FORMAT.format(getAuctionValueCustom(rankings));
        } else if (league.isBestBall()) {
            return String.valueOf(getBestBallRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : getBestBallRank());
        } else {
            return "";
        }

    }
}
