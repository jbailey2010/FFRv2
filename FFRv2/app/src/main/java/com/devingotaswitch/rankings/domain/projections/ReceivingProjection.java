package com.devingotaswitch.rankings.domain.projections;

import android.util.Log;

import com.devingotaswitch.rankings.domain.ScoringSettings;

class ReceivingProjection extends ProjectionBase {
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
}
