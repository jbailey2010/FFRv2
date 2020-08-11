package com.devingotaswitch.rankings.domain

import com.devingotaswitch.rankings.domain.projections.PlayerProjection

class DailyProjection {
    var date: String? = null
    private var projection: PlayerProjection? = null
    var playerKey: String? = null

    fun getProjection(scoringSettings: ScoringSettings?): Double {
        return projection!!.getProjectedPoints(scoringSettings)
    }

    fun setPlayerProjection(projection: PlayerProjection?) {
        this.projection = projection
    }

}