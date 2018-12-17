package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;

class DefensiveProjection extends ProjectionBase {
    private double projection = 0.0;

    DefensiveProjection(double projection) {
        this.projection = projection;
    }

    @Override
    double getProjectedPoints(ScoringSettings scoringSettings) {
        return projection;
    }

    @Override
    String getDisplayString() {
        return "";
    }
}