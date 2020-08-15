package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings

abstract class ProjectionBase {
    abstract fun getProjectedPoints(scoringSettings: ScoringSettings): Double
    abstract fun getDisplayString(): String
}