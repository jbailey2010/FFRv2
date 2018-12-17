package com.devingotaswitch.rankings.domain.projections;

import android.util.Log;

import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;

class RushingProjection extends ProjectionBase {
    private double yards = 0.0;
    private double tds = 0.0;
    private double fumbles = 0.0;

    RushingProjection(double yards, double tds, double fumbles) {
        this.yards = yards;
        this.fumbles = fumbles;
        this.tds = tds;
    }

    @Override
    double getProjectedPoints(ScoringSettings scoringSettings) {
        double yardPoints = (yards / (scoringSettings.getRushingYards()));
        double fumblePoints = fumbles * scoringSettings.getFumbles();
        double tdPoints = tds * scoringSettings.getRushingTds();
        return yardPoints + fumblePoints + tdPoints;
    }

    @Override
    String getDisplayString() {
        return new StringBuilder()
                .append("Rushing Yards: ")
                .append(yards)
                .append(Constants.LINE_BREAK)
                .append("Rushing TDs: ")
                .append(tds)
                .append(Constants.LINE_BREAK)
                .append("Fumbles: ")
                .append(fumbles)
                .toString();
    }
}
