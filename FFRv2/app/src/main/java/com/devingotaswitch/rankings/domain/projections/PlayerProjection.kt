package com.devingotaswitch.rankings.domain.projections

import com.devingotaswitch.rankings.domain.ScoringSettings
import com.devingotaswitch.rankings.domain.projections.PlayerProjection
import com.devingotaswitch.utils.Constants
import com.google.gson.GsonBuilder

class PlayerProjection(passingYds: Double, passingTds: Double, rushingYds: Double, rushingTds: Double,
                       receivingYds: Double, receivingTds: Double, receptions: Double, fumbles: Double,
                       ints: Double, def: Double, k: Double, scoringSettings: ScoringSettings) {
    val passingProjection: PassingProjection = PassingProjection(passingYds, passingTds, ints)
    val rushingProjection: RushingProjection = RushingProjection(rushingYds, rushingTds, fumbles)
    val receivingProjection: ReceivingProjection = ReceivingProjection(receivingYds, receptions, receivingTds)
    private val defensiveProjection: DefensiveProjection = DefensiveProjection(def)
    private val kickingProjection: KickingProjection = KickingProjection(k)
    var formattedProjectedPoints = 0.0
      private set

    constructor(scoringSettings: ScoringSettings) : this(0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, scoringSettings) {
    }

    fun getProjectedPoints(scoringSettings: ScoringSettings): Double {
        return passingProjection.getProjectedPoints(scoringSettings) +
                rushingProjection.getProjectedPoints(scoringSettings) +
                receivingProjection.getProjectedPoints(scoringSettings) +
                defensiveProjection.getProjectedPoints(scoringSettings) +
                kickingProjection.getProjectedPoints(scoringSettings)
    }

    fun updateAndGetFormattedProjectedPoints(scoringSettings: ScoringSettings) {
        formattedProjectedPoints = Constants.DECIMAL_FORMAT.format(getProjectedPoints(scoringSettings)).toDouble()
    }

    fun getDisplayString(position: String?): String {
        return when (position) {
            Constants.QB -> passingProjection.getDisplayString() +
                    Constants.LINE_BREAK +
                    rushingProjection.getDisplayString()
            Constants.RB -> rushingProjection.getDisplayString() +
                    Constants.LINE_BREAK +
                    receivingProjection.getDisplayString()
            Constants.WR -> receivingProjection.getDisplayString() +
                    Constants.LINE_BREAK +
                    rushingProjection.getDisplayString()
            Constants.TE -> receivingProjection.getDisplayString()
            Constants.DST -> defensiveProjection.getDisplayString()
            Constants.K -> kickingProjection.getDisplayString()
            else -> ""
        }
    }

    override fun toString(): String {
        return GSON.toJson(this, PlayerProjection::class.java)
    }

    companion object {
        private val GSON = GsonBuilder().create()
        @JvmStatic
        fun fromJson(json: String?): PlayerProjection {
            return GSON.fromJson(json, PlayerProjection::class.java)
        }
    }

    init {
        updateAndGetFormattedProjectedPoints(scoringSettings)
    }
}