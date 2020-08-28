package com.devingotaswitch.rankings.domain

import com.devingotaswitch.rankings.domain.projections.PlayerProjection

class DailyProjection {
    lateinit var date: String
    lateinit var playerProjection: PlayerProjection
    lateinit var playerKey: String

    fun getProjection(scoringSettings: ScoringSettings): Double {
        return playerProjection.getProjectedPoints(scoringSettings)
    }

}