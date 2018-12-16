package com.devingotaswitch.rankings.domain.projections;

import com.devingotaswitch.rankings.domain.ScoringSettings;

abstract class ProjectionBase {
    abstract double getProjectedPoints(ScoringSettings scoringSettings);
}
