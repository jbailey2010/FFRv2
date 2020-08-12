package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings
import com.devingotaswitch.utils.Constants

class ReceivingProjection internal constructor(val yards: Double, val receptions: Double, val tds: Double) : ProjectionBase() {
    override fun getProjectedPoints(scoringSettings: ScoringSettings): Double {
        val yardPoints = yards / scoringSettings.rushingYards
        val receptionPoints = receptions * scoringSettings.receptions
        val tdPoints = tds * scoringSettings.receivingTds
        return yardPoints + receptionPoints + tdPoints
    }

    override fun getDisplayString(): String {
        return "Catches: " +
                receptions +
                Constants.LINE_BREAK +
                "Receiving Yards: " +
                yards +
                Constants.LINE_BREAK +
                "Receiving TDs: " +
                tds
    }

}