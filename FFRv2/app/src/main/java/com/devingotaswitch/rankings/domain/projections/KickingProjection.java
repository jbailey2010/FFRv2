package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;

class KickingProjection extends ProjectionBase {
    private double projection = 0.0;

    KickingProjection(double projection) {
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
