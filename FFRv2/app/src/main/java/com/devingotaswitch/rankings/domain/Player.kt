package com.devingotaswitch.rankings.domain

import com.devingotaswitch.rankings.domain.projections.PlayerProjection
import com.devingotaswitch.utils.Constants

class Player {
    lateinit var name: String
    var age: Int? = null
    var experience = -1
    lateinit var position: String
    var ecr = Constants.DEFAULT_RANK
    var adp = Constants.DEFAULT_RANK
    var dynastyRank = Constants.DEFAULT_RANK
    var rookieRank = Constants.DEFAULT_RANK
    var bestBallRank = Constants.DEFAULT_RANK
    lateinit var teamName: String
    var stats: String? = null
    var injuryStatus: String? = null
    var auctionValue = 0.0
    private var numRankings = 0.0
    var risk = Constants.DEFAULT_RISK
    lateinit var playerProjection: PlayerProjection
    val projection: Double
        get() {
            return playerProjection.projection
        }
    var paa: Double = Constants.DEFAULT_VBD
    var xval: Double = Constants.DEFAULT_VBD
    var vols: Double = Constants.DEFAULT_VBD
    fun getAuctionValueCustom(rankings: Rankings): Double {
        return getAuctionValueCustom(rankings.leagueSettings.teamCount, rankings.leagueSettings.auctionBudget)
    }

    fun getAuctionValueCustom(teamCount: Int, auctionBudget: Int): Double {
        if (auctionValue <= 1.0) {
            return auctionValue
        }
        var scalar = auctionBudget.toDouble() / Constants.DEFAULT_AUCTION_BUDGET.toDouble()
        if (teamCount > Constants.AUCTION_TEAM_SCALE_COUNT) {
            // First, get the extra % of money. If there's 14 teams, that means 14/12 = 1.16667 = 16.6667 % more money.
            // To limit crazy numbers, it's capped at 16 teams/33.333% above.
            var teamScaleDelta = teamCount.toDouble().coerceAtMost(16.0) /
                    Constants.AUCTION_TEAM_SCALE_COUNT.toDouble() - 1.0
            // Next, scale that down a bit.
            teamScaleDelta *= Constants.AUCTION_TEAM_SCALE_THRESHOLD
            // Finally, add it back to 1 so we can scale values accordingly, x * (1.16667 * scale down factor).
            teamScaleDelta += 1.0
            scalar *= teamScaleDelta
        }
        return auctionValue * scalar
    }

    fun updateProjection(scoringSettings: ScoringSettings?) {
        playerProjection.updateAndGetFormattedProjectedPoints(scoringSettings)
    }

    fun handleNewValue(newValue: Double) {
        var auctionTotal = auctionValue * numRankings
        numRankings++
        auctionTotal += newValue
        auctionValue = auctionTotal / numRankings
    }

    val uniqueId: String
        get() = name +
                Constants.PLAYER_ID_DELIMITER +
                teamName +
                Constants.PLAYER_ID_DELIMITER +
                position

    fun getScaledPAA(rankings: Rankings): Double {
        return getScaledValue(paa, rankings.leagueSettings.rosterSettings.getNumberStartedOfPos(position),
                rankings.draft.getPlayersDraftedForPos(position).size)
    }

    fun getScaledXVal(rankings: Rankings): Double {
        return getScaledValue(xval, rankings.leagueSettings.rosterSettings.getNumberStartedOfPos(position),
                rankings.draft.getPlayersDraftedForPos(position).size)
    }

    fun getScaledVOLS(rankings: Rankings): Double {
        return getScaledValue(vols, rankings.leagueSettings.rosterSettings.getNumberStartedOfPos(position),
                rankings.draft.getPlayersDraftedForPos(position).size)
    }

    private fun getScaledValue(value: Double?, numStarted: Int, numDrafted: Int): Double {
        var scaleFactor: Double
        if (numStarted == 0) {
            scaleFactor = 0.0
        } else if (numDrafted == 0 || numDrafted < numStarted) {
            scaleFactor = 1.0
        } else {
            scaleFactor = 1.0 - (numDrafted.toDouble() - (numStarted - 1)) * 0.2
            if (scaleFactor <= 0.0) {
                scaleFactor = 0.2
            }
        }
        return if (value!! < 0) {
            // If it's negative, multiplying by a smaller value would make it look better
            value / scaleFactor
        } else {
            value * scaleFactor
        }
    }

    fun getDisplayValue(rankings: Rankings): String {
        val league = rankings.leagueSettings
        return if (league.isRookie) {
            if (rookieRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else rookieRank.toString()
        } else if (league.isDynasty) {
            if (dynastyRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else dynastyRank.toString()
        } else if (league.isSnake) {
            if (ecr == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else ecr.toString()
        } else if (league.isAuction) {
            Constants.DECIMAL_FORMAT.format(getAuctionValueCustom(rankings))
        } else if (league.isBestBall) {
            if (bestBallRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else bestBallRank.toString()
        } else {
            ""
        }
    }

    companion object {

    }
}