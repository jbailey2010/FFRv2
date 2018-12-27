package com.devingotaswitch.rankings.domain.projections;

import android.util.Log;

import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;

public class ReceivingProjection extends ProjectionBase {
    private double yards = 0.0;
    private double receptions = 0.0;
    private double tds = 0.0;

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
        return new StringBuilder()
                .append("Catches: ")
                .append(receptions)
                .append(Constants.LINE_BREAK)
                .append("Receiving Yards: ")
                .append(yards)
                .append(Constants.LINE_BREAK)
                .append("Receiving TDs: ")
                .append(tds)
                .toString();
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
