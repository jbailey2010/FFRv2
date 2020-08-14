package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings

internal class DefensiveProjection(private val projection: Double) : ProjectionBase() {
    public override fun getProjectedPoints(scoringSettings: ScoringSettings): Double {
        return projection
    }

    public override fun getDisplayString(): String {
        return ""
    }

}