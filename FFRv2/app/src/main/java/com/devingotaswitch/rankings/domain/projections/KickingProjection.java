package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;

class KickingProjection extends ProjectionBase {
    private double projection;

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
