package com.devingotaswitch.rankings.sources

import android.util.Log
import com.devingotaswitch.rankings.domain.LeagueSettings
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.RosterSettings
import com.devingotaswitch.utils.Constants
import java.util.*
import kotlin.math.ln

object ParseMath {
    private var qbLimit = 0.0
    private var rbLimit = 0.0
    private var wrLimit = 0.0
    private var teLimit = 0.0
    private var dLimit = 0.0
    private var kLimit = 0.0
    private fun setXValLimits(league: LeagueSettings) {
        // Top 100 picks for a standard roster.
        val roster = league.rosterSettings
        qbLimit = 15.0
        rbLimit = 36.0
        wrLimit = 38.0
        teLimit = 8.0
        dLimit = 2.0
        kLimit = 1.0
        if (roster.qbCount > 1 || roster.qbCount > 0 && roster.flex != null && roster.flex!!.qbrbwrteCount > 0) {
            qbLimit += 8.0
            teLimit -= 2.0
            rbLimit -= 3.0
            wrLimit -= 3.0
        }
        if (roster.teCount > 1) {
            teLimit += 2.0
            wrLimit--
            rbLimit--
        }
        if (roster.dstCount == 0) {
            dLimit = 0.0
            teLimit++
            wrLimit++
        }
        if (roster.kCount == 0) {
            kLimit = 0.0
            teLimit++
        }
        Log.d("XVal", "XVal QB Limit: $qbLimit")
        Log.d("XVal", "XVal RB Limit: $rbLimit")
        Log.d("XVal", "XVal WR Limit: $wrLimit")
        Log.d("XVal", "XVal TE Limit: $teLimit")
        Log.d("XVal", "XVal DST Limit: $dLimit")
        Log.d("XVal", "XVal K Limit: $kLimit")
    }

    private fun setPAALimits(league: LeagueSettings) {
        val x = league.teamCount
        val roster = league.rosterSettings
        val flex = roster.flex

        // First, the layups. Assume 1 started.
        dLimit = 1.25 * x
        kLimit = 1.25 * x

        // Now, tight ends. These are almost not impacted by flex at all.
        teLimit = if (roster.teCount < 2) {
            1.75 * x - 3.3333333
        } else {
            7.5 * x - 41.66667
        }
        if (flex != null && (flex.rbwrteCount > 0 || flex.wrteCount > 0 || flex.rbteCount > 0 || flex.qbrbwrteCount > 0)) {
            teLimit += 12.0 / x
        }

        // Next, QBs. Boring if one, very interesting if not.
        if (roster.qbCount == 1 && flex != null && flex.qbrbwrteCount == 0) {
            qbLimit = 1.25 * x + 1.33333
        } else if (roster.qbCount == 0 && flex != null && flex.qbrbwrteCount == 1) {
            qbLimit = 1.25 * x
        } else if (roster.qbCount >= 2 || flex != null && flex.qbrbwrteCount >= 2) {
            qbLimit = 6 * x - 30.toDouble()
        } else if (roster.qbCount == 1 && flex != null && flex.qbrbwrteCount == 1) {
            qbLimit = 6 * x - 32.toDouble()
        }

        // Finally, RB/WR. Just all the hell over the place.
        rbLimit = when {
            roster.rbCount < 2 -> {
                1.5 * x - 2
            }
            roster.rbCount < 3 -> {
                3.25 * x - 5.33333
            }
            else -> {
                6 * x - 16.33333
            }
        }
        wrLimit = when {
            roster.wrCount < 2 -> {
                1.25 * x + 0.33333
            }
            roster.wrCount < 3 -> {
                2.75 * x - 1.66666667
            }
            else -> {
                4.5 * x - 5
            }
        }
        if (flex != null && (flex.rbwrCount > 0 || flex.rbwrteCount > 0)) {
            if (league.scoringSettings.receptions > 0) {
                // Legit
                if (roster.rbCount == 2 && roster.wrCount == 2) {
                    rbLimit = 3.75 * x - 10.666667
                    wrLimit = 4.25 * x - 2.33333
                }
                if (roster.rbCount == 1 && roster.wrCount > 2) {
                    rbLimit = 3 * x - 3.3333
                    wrLimit = 4.75 * x - 6.3333
                }
                if (roster.rbCount == 2 && roster.wrCount > 2) {
                    rbLimit = 4.5 * x - 5.33333
                    wrLimit = 5.75 * x - 14
                }
                // Guesstimated
                if (roster.rbCount == 1 && roster.wrCount == 1) {
                    rbLimit = 2 * x - 3.3333
                    wrLimit = 2 * x - 1.toDouble()
                }
                if (roster.rbCount == 1 && roster.wrCount == 2) {
                    rbLimit = 2.5 * x
                    wrLimit = 4.25 * x - 5
                }
                if (roster.rbCount == 2 && roster.wrCount == 1) {
                    rbLimit = 3.5 * x - 10
                    wrLimit = 2.25 * x - 1
                }
                if (roster.rbCount > 2 && roster.wrCount == 1) {
                    wrLimit = 2.5 * x + 1
                    rbLimit = 4.7 * x - 5
                }
                if (roster.rbCount > 2 && roster.wrCount == 2) {
                    rbLimit = 4.75 * x - 4.33333
                    wrLimit = 4.25 * x
                }
                if (roster.rbCount > 2 && roster.wrCount > 2) {
                    rbLimit = 4.75 * x - 1
                    wrLimit = 5.75 * x - 12
                }
            } else {
                // Legit
                if (roster.rbCount == 2 && roster.wrCount == 2) {
                    rbLimit = 2.75 * x + 6
                    wrLimit = 4.25 * x - 7.3333
                }
                if (roster.rbCount == 1 && roster.wrCount > 2) {
                    rbLimit = 2.5 * x + 3.3333
                    wrLimit = 5.25 * x - 13
                }
                if (roster.rbCount == 2 && roster.wrCount > 2) {
                    rbLimit = 4.5 * x - 5.3333
                    wrLimit = 5.75 * x - 14
                }
                // Guesstimated
                if (roster.rbCount == 1 && roster.wrCount == 1) {
                    rbLimit = 2 * x - 2.toDouble()
                    wrLimit = 2 * x - 1.66667
                }
                if (roster.rbCount == 1 && roster.wrCount == 2) {
                    rbLimit = 2.5 * x + 1
                    wrLimit = 4.25 * x - 6
                }
                if (roster.rbCount == 2 && roster.wrCount == 1) {
                    rbLimit = 3.5 * x - 9
                    wrLimit = 2.25 * x - 1.666667
                }
                if (roster.rbCount > 2 && roster.wrCount == 1) {
                    wrLimit = 2.5 * x + 1.5
                    rbLimit = 4.7 * x - 3.6667
                }
                if (roster.rbCount > 2 && roster.wrCount == 2) {
                    rbLimit = 4.75 * x - 3.666667
                    wrLimit = 4.25 * x - 1
                }
                if (roster.rbCount > 2 && roster.wrCount > 2) {
                    rbLimit = 4.75 * x
                    wrLimit = 5.75 * x - 13
                }
            }
        }
        if (flex != null && flex.qbrbwrteCount > 0) {
            if (league.scoringSettings.receptions > 0) {
                rbLimit += x / 11.0
                wrLimit += x / 10.0
            } else {
                rbLimit += x / 10.0
                wrLimit += x / 11.0
            }
        }
        Log.d("PAA", "QB PAA limit: $qbLimit")
        Log.d("PAA", "RB PAA limit: $rbLimit")
        Log.d("PAA", "WR PAA limit: $wrLimit")
        Log.d("PAA", "TE PAA limit: $teLimit")
        Log.d("PAA", "DST PAA limit: $dLimit")
        Log.d("PAA", "K PAA limit: $kLimit")
    }

    fun setPlayerXval(rankings: Rankings, league: LeagueSettings) {
        setXValLimits(league)
        val qbTotal = getPositionalProjection(qbLimit, rankings.qbs)
        val rbTotal = getPositionalProjection(rbLimit, rankings.rbs)
        val wrTotal = getPositionalProjection(wrLimit, rankings.wrs)
        val teTotal = getPositionalProjection(teLimit, rankings.tes)
        val dTotal = getPositionalProjection(dLimit, rankings.dsts)
        val kTotal = getPositionalProjection(kLimit, rankings.ks)
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            when (player.position) {
                Constants.QB -> player.xval = (player.projection - qbTotal)
                Constants.RB -> player.xval = (player.projection - rbTotal)
                Constants.WR -> player.xval = (player.projection - wrTotal)
                Constants.TE -> player.xval = (player.projection - teTotal)
                Constants.DST -> player.xval = (player.projection - dTotal)
                Constants.K -> player.xval = (player.projection - kTotal)
            }
        }
    }

    fun setPlayerVoLS(rankings: Rankings, league: LeagueSettings) {
        setPAALimits(league)
        val qbLS = getPositionalReplacementProjection(qbLimit, rankings.qbs)
        val rbLS = getPositionalReplacementProjection(rbLimit, rankings.rbs)
        val wrLS = getPositionalReplacementProjection(wrLimit, rankings.wrs)
        val teLS = getPositionalReplacementProjection(teLimit, rankings.tes)
        val dLS = getPositionalReplacementProjection(dLimit, rankings.dsts)
        val kLS = getPositionalReplacementProjection(kLimit, rankings.ks)
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            when (player.position) {
                Constants.QB -> player.vols = player.projection - qbLS
                Constants.RB -> player.vols = player.projection - rbLS
                Constants.WR -> player.vols = player.projection - wrLS
                Constants.TE -> player.vols = player.projection - teLS
                Constants.DST -> player.vols = player.projection - dLS
                Constants.K -> player.vols = player.projection - kLS
            }
        }
    }

    private fun getPositionalReplacementProjection(limit: Double, position: MutableList<Player>): Double {
        if (limit == 0.0) {
            return 0.0
        }
        position.sortWith(Comparator { a: Player, b: Player -> b.projection.compareTo(a.projection) })
        val player: Player = if (limit.toInt() <= position.size) {
            position[limit.toInt() - 1]
        } else {
            position[position.size - 1]
        }
        return player.projection
    }

    fun setPlayerPAA(rankings: Rankings, league: LeagueSettings) {
        setPAALimits(league)
        val qbTotal = getPositionalProjection(qbLimit, rankings.qbs)
        val rbTotal = getPositionalProjection(rbLimit, rankings.rbs)
        val wrTotal = getPositionalProjection(wrLimit, rankings.wrs)
        val teTotal = getPositionalProjection(teLimit, rankings.tes)
        val dTotal = getPositionalProjection(dLimit, rankings.dsts)
        val kTotal = getPositionalProjection(kLimit, rankings.ks)
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            when (player.position) {
                Constants.QB -> player.paa = player.projection - qbTotal
                Constants.RB -> player.paa = player.projection - rbTotal
                Constants.WR -> player.paa = player.projection - wrTotal
                Constants.TE -> player.paa = player.projection - teTotal
                Constants.DST -> player.paa = player.projection - dTotal
                Constants.K -> player.paa = player.projection - kTotal
            }
        }
    }

    private fun getPositionalProjection(limit: Double, players: MutableList<Player>): Double {
        var posTotal = 0.0
        players.sortWith { a: Player, b: Player -> b.projection.compareTo(a.projection) }
        val posCap = limit.toInt().coerceAtMost(players.size)
        var posCounter = 0.0
        while (posCounter < posCap) {
            posTotal += players[posCounter.toInt()].projection
            posCounter++
        }
        posTotal /= posCounter
        return posTotal
    }

    fun getECRAuctionValue(rankings: Rankings) {
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            player.handleNewValue(convertRanking(player.ecr))
        }
    }

    fun getADPAuctionValue(rankings: Rankings) {
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            player.handleNewValue(convertRanking(player.adp))
        }
    }

    private fun convertRanking(ranking: Double): Double {
        var possVal = 78.6341 - 13.793 * ln(ranking)
        if (possVal < 1.0) {
            possVal = 1.0
        }
        return possVal
    }

    fun getPAAAuctionValue(rankings: Rankings) {
        val discretCash = getDiscretionaryCash(rankings.leagueSettings.auctionBudget,
                rankings.leagueSettings.rosterSettings)
        val zMap = initZMap(rankings)
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            val possVal = paa1Calc(zMap, player, discretCash)
            player.handleNewValue(possVal)
        }
    }

    private fun paa1Calc(zMap: Map<String, Double>,
                         player: Player, discretCash: Double): Double {
        val coeff = player.paa / (zMap[player.position] ?: error(""))
        var possVal = discretCash * coeff + 1.0
        if (player.position == Constants.DST) {
            possVal /= 10.0
        }
        if (player.position == Constants.K) {
            possVal /= 20.0
        }
        if (possVal < 1.0) {
            possVal = 1.0
        }
        return possVal
    }

    private fun initZMap(rankings: Rankings): Map<String, Double> {
        val zMap: MutableMap<String, Double> = HashMap()
        zMap[Constants.QB] = avgPAAPos(rankings.qbs)
        zMap[Constants.RB] = avgPAAPos(rankings.rbs)
        zMap[Constants.WR] = avgPAAPos(rankings.wrs)
        zMap[Constants.TE] = avgPAAPos(rankings.tes)
        zMap[Constants.DST] = avgPAAPos(rankings.dsts)
        zMap[Constants.K] = avgPAAPos(rankings.ks)
        return zMap
    }

    private fun avgPAAPos(players: Collection<Player>): Double {
        var paaTotal = 0.0
        var paaCount = 0.0
        for (player in players) {
            if (player.paa > 0.0) {
                paaTotal += player.paa
                paaCount++
            }
        }
        return if (paaCount == 0.0) {
            // Just to prevent divide by 0 madness if a projection breaks
            1.0
        } else paaTotal / paaCount
    }

    private fun getDiscretionaryCash(auctionBudget: Int, roster: RosterSettings?): Double {
        val rosterSize = roster!!.rosterSize
        return (auctionBudget - rosterSize).toDouble() / (rosterSize - roster.benchCount).toDouble()
    }

    fun getLeverage(player: Player, rankings: Rankings): Double {
        var topPlayer: Player? = null
        var maxVal = 0.0
        for (key in rankings.orderedIds) {
            val possibleTop = rankings.getPlayer(key)
            if (possibleTop.auctionValue > maxVal && possibleTop.position == player.position) {
                maxVal = possibleTop.auctionValue
                topPlayer = possibleTop
            }
        }
        return Constants.DECIMAL_FORMAT.format(player.projection / topPlayer!!.projection /
                (player.getAuctionValueCustom(rankings) / topPlayer.getAuctionValueCustom(rankings))).toDouble()
    }
}