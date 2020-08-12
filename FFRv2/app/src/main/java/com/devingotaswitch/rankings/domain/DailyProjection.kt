package com.devingotaswitch.rankings.domain

import com.devingotaswitch.rankings.domain.projections.PlayerProjection

class DailyProjection {
    var date: String? = null
    var playerProjection: PlayerProjection? = null
    var playerKey: String? = null

    fun getProjection(scoringSettings: ScoringSettings?): Double {
        return playerProjection!!.getProjectedPoints(scoringSettings)
    }

}