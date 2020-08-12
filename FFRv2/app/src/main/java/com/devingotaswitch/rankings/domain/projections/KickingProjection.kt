package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings

internal class KickingProjection(private val projection: Double) : ProjectionBase() {
    override fun getProjectedPoints(scoringSettings: ScoringSettings): Double {
        return projection
    }

    override fun getDisplayString(): String {
        return ""
    }

}