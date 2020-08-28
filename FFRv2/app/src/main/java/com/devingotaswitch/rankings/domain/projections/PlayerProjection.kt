package com.devingotaswitch.rankings.domain.projections

import android.util.Log
import com.devingotaswitch.rankings.domain.ScoringSettings
import com.devingotaswitch.rankings.domain.projections.PlayerProjection
import com.devingotaswitch.utils.Constants
import com.google.gson.GsonBuilder

class PlayerProjection {
    val passingProjection: PassingProjection
    val rushingProjection: RushingProjection
    val receivingProjection: ReceivingProjection
    private val defensiveProjection: DefensiveProjection
    private val kickingProjection: KickingProjection
    var projection = 0.0

    constructor(scoringSettings: ScoringSettings?) : this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, scoringSettings)

    constructor(json: String?) {
        val parsedValues = GSON.fromJson(json, this.javaClass)
        passingProjection = parsedValues.passingProjection
        rushingProjection = parsedValues.rushingProjection
        receivingProjection = parsedValues.receivingProjection
        defensiveProjection = parsedValues.defensiveProjection
        kickingProjection = parsedValues.kickingProjection
        projection = parsedValues.projection
    }

    constructor(passingYds: Double, passingTds: Double, rushingYds: Double, rushingTds: Double,
                receivingYds: Double, receivingTds: Double, receptions: Double, fumbles: Double,
                ints: Double, def: Double, k: Double, scoringSettings: ScoringSettings?) {
        passingProjection = PassingProjection(passingYds, passingTds, ints)
        rushingProjection = RushingProjection(rushingYds, rushingTds, fumbles)
        receivingProjection = ReceivingProjection(receivingYds, receptions, receivingTds)
        defensiveProjection = DefensiveProjection(def)
        kickingProjection = KickingProjection(k)
        updateAndGetFormattedProjectedPoints(scoringSettings)
    }

    fun getProjectedPoints(scoringSettings: ScoringSettings?): Double {
        return passingProjection.getProjectedPoints(scoringSettings!!) +
                rushingProjection.getProjectedPoints(scoringSettings) +
                receivingProjection.getProjectedPoints(scoringSettings) +
                defensiveProjection.getProjectedPoints(scoringSettings) +
                kickingProjection.getProjectedPoints(scoringSettings)
    }

    fun updateAndGetFormattedProjectedPoints(scoringSettings: ScoringSettings?) {
        projection = Constants.DECIMAL_FORMAT.format(getProjectedPoints(scoringSettings)).toDouble()
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
    }
}