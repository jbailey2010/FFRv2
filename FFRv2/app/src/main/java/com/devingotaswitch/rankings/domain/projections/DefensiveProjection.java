package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;

class DefensiveProjection extends ProjectionBase {
    private final double projection;

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
