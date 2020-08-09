package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;

public class PassingProjection extends ProjectionBase {

    private final double yards;
    private final double tds;
    private final double ints;

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

    @Override
    String getDisplayString() {
        return "Passing Yards: " +
                yards +
                Constants.LINE_BREAK +
                "Passing TDs: " +
                tds +
                Constants.LINE_BREAK +
                "Interceptions: " +
                ints;
    }

    public double getTds() {
        return tds;
    }

    public double getYards() {
        return yards;
    }
}
