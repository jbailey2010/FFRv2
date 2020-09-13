package com.devingotaswitch.rankings.domain

class LeagueSettings @JvmOverloads constructor(var name: String, teamCount: Int, isSnake: Boolean, isAuction: Boolean, isDynasty: Boolean,
                                               isRookie: Boolean, isBestBall: Boolean, isCurrentLeague: Boolean, auctionBudget: Int,
                                               scoring: ScoringSettings = ScoringSettings(), roster: RosterSettings = RosterSettings()) {
    var teamCount = 0
    var isAuction = false
    var isDynasty = false
    var isRookie = false
    var isSnake = false
    var isBestBall = false
    var isCurrentLeague = false
    var auctionBudget = 0
    var scoringSettings: ScoringSettings
    var rosterSettings: RosterSettings

    init {
        this.teamCount = teamCount
        this.isSnake = isSnake
        this.isAuction = isAuction
        this.isDynasty = isDynasty
        this.isRookie = isRookie
        this.isBestBall = isBestBall
        this.isCurrentLeague = isCurrentLeague
        this.auctionBudget = auctionBudget
        scoringSettings = scoring
        rosterSettings = roster
    }
}