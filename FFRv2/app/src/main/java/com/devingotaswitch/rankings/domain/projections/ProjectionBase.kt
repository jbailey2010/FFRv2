package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings

abstract class ProjectionBase {
    internal abstract fun getProjectedPoints(scoringSettings: ScoringSettings): Double
    internal abstract fun getDisplayString(): String
}