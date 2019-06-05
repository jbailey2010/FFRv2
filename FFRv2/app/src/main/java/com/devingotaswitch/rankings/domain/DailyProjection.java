package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.rankings.domain.projections.PlayerProjection;

public class DailyProjection {

    private String date;
    private PlayerProjection projection;
    private String playerKey;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getProjection(ScoringSettings scoringSettings) {
        return projection.getProjectedPoints(scoringSettings);
    }

    public void setPlayerProjection(PlayerProjection projection) {
        this.projection = projection;
    }

    public String getPlayerKey() {
        return playerKey;
    }

    public void setPlayerKey(String playerKey) {
        this.playerKey = playerKey;
    }
}
