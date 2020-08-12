package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings
import com.devingotaswitch.utils.Constants

class RushingProjection internal constructor(val yards: Double, val tds: Double, private val fumbles: Double) : ProjectionBase() {
    override fun getProjectedPoints(scoringSettings: ScoringSettings): Double {
        val yardPoints = yards / scoringSettings.rushingYards
        val fumblePoints = fumbles * scoringSettings.fumbles
        val tdPoints = tds * scoringSettings.rushingTds
        return yardPoints + fumblePoints + tdPoints
    }

    override fun getDisplayString(): String {
        return "Rushing Yards: " +
                yards +
                Constants.LINE_BREAK +
                "Rushing TDs: " +
                tds +
                Constants.LINE_BREAK +
                "Fumbles: " +
                fumbles
    }

}