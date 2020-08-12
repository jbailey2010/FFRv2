package com.devingotaswitch.rankings.domain

import com.devingotaswitch.utils.Constants
import java.util.*

class ScoringSettings(id: String, passingTds: Int, rushingTds: Int, receivingTds: Int, fumbles: Double,
                      interceptions: Double, passingYds: Int, rushingYds: Int, receivingYds: Int, receptions: Double) {
    var id: String? = null
        private set
    var passingTds = 0
    var rushingTds = 0
    var receivingTds = 0
    var fumbles = 0.0
    var interceptions = 0.0
    var passingYards = 0
    var rushingYards = 0
    var receivingYards = 0
    var receptions = 0.0

    @JvmOverloads
    constructor(passingTds: Int = Constants.DEFAULT_TD_WORTH, rushingTds: Int = Constants.DEFAULT_TD_WORTH,
                receivingTds: Int = Constants.DEFAULT_TD_WORTH, fumbles: Double = Constants.DEFAULT_TURNOVER_WORTH.toDouble(),
                interceptions: Double = Constants.DEFAULT_TURNOVER_WORTH.toDouble(), passingYds: Int = Constants.DEFAULT_PASSING_YDS,
                rushingYds: Int = Constants.DEFAULT_RUSHING_YDS, receivingYds: Int = Constants.DEFAULT_RECEIVING_YDS,
                receptions: Double = Constants.DEFAULT_RECEPTIONS) :
            this(UUID.randomUUID().toString(), passingTds, rushingTds, receivingTds, fumbles, interceptions, passingYds,
            rushingYds, receivingYds, receptions)

    private fun setId(id: String) {
        this.id = id
    }

    init {
        setId(id)
        this.passingTds = passingTds
        this.rushingTds = rushingTds
        this.receivingTds = receivingTds
        this.fumbles = fumbles
        this.interceptions = interceptions
        this.passingYards = passingYds
        this.rushingYards = rushingYds
        this.receivingYards = receivingYds
        this.receptions = receptions
    }
}