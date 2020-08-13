package com.devingotaswitch.rankings.domain

class LeagueSettings @JvmOverloads constructor(name: String?, teamCount: Int, isSnake: Boolean, isAuction: Boolean, isDynasty: Boolean,
                                               isRookie: Boolean, isBestBall: Boolean, auctionBudget: Int, scoring: ScoringSettings? = ScoringSettings(), roster: RosterSettings? =
                                                       RosterSettings()) {
    var name: String? = null
    var teamCount = 0
    var isAuction = false
    var isDynasty = false
    var isRookie = false
    var isSnake = false
    var isBestBall = false
    var auctionBudget = 0
    var scoringSettings: ScoringSettings? = null
    var rosterSettings: RosterSettings? = null

    init {
        this.name = name
        this.teamCount = teamCount
        this.isSnake = isSnake
        this.isAuction = isAuction
        this.isDynasty = isDynasty
        this.isRookie = isRookie
        this.isBestBall = isBestBall
        this.auctionBudget = auctionBudget
        scoringSettings = scoring
        rosterSettings = roster
    }
}