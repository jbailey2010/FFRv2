package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;

public class ReceivingProjection extends ProjectionBase {
    private final double yards;
    private final double receptions;
    private final double tds;

    ReceivingProjection(double yards, double receptions, double tds) {
        this.yards = yards;
        this.receptions = receptions;
        this.tds = tds;
    }

    @Override
    double getProjectedPoints(ScoringSettings scoringSettings) {
        double yardPoints = (yards / (scoringSettings.getRushingYards()));
        double receptionPoints = receptions * scoringSettings.getReceptions();
        double tdPoints = tds * scoringSettings.getReceivingTds();
        return yardPoints + receptionPoints + tdPoints;
    }

    @Override
    String getDisplayString() {
        return "Catches: " +
                receptions +
                Constants.LINE_BREAK +
                "Receiving Yards: " +
                yards +
                Constants.LINE_BREAK +
                "Receiving TDs: " +
                tds;
    }

    public double getTds() {
        return tds;
    }

    public double getYards() {
        return yards;
    }

    public double getReceptions() {
        return receptions;
    }
}
