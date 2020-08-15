package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings
import com.devingotaswitch.utils.Constants

class PassingProjection internal constructor(val yards: Double, val tds: Double,
                                             private val ints: Double) : ProjectionBase() {
    override fun getProjectedPoints(scoringSettings: ScoringSettings): Double {
        val yardPoints = yards / scoringSettings.passingYards
        val tdPoints = tds * scoringSettings.passingTds
        val intPoints = ints * scoringSettings.interceptions
        return yardPoints + tdPoints + intPoints
    }

    override fun getDisplayString(): String {
        return "Passing Yards: " +
                yards +
                Constants.LINE_BREAK +
                "Passing TDs: " +
                tds +
                Constants.LINE_BREAK +
                "Interceptions: " +
                ints
    }

}