package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;

class PassingProjection extends ProjectionBase {

    private double yards = 0.0;
    private double tds = 0.0;
    private double ints = 0.0;

    PassingProjection(double yards, double tds, double ints) {
        this.tds = tds;
        this.yards = yards;
        this.ints = ints;
    }

    @Override
    double getProjectedPoints(ScoringSettings scoringSettings) {
        double yardPoints = (yards / (scoringSettings.getPassingYards()));
        double tdPoints = tds * scoringSettings.getPassingTds();
        double intPoints = ints * scoringSettings.getInterceptions();
        return yardPoints + tdPoints + intPoints;
    }
}
