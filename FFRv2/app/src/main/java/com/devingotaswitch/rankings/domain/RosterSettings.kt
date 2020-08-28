package com.devingotaswitch.rankings.domain

import com.devingotaswitch.utils.Constants
import java.util.*

class RosterSettings @JvmOverloads constructor(id: String = UUID.randomUUID().toString(),
                                               qbCt: Int = Constants.ONE_STARTER, rbCt: Int = Constants.TWO_STARTERS,
                                               wrCt: Int = Constants.TWO_STARTERS, teCt: Int = Constants.ONE_STARTER,
                                               dCt: Int = Constants.ONE_STARTER, kCt: Int = Constants.ONE_STARTER,
                                               benchCt: Int = Constants.BENCH_DEFAULT, flex: Flex = Flex()) {
    var id: String = ""
        private set
    var qbCount = 0
    var rbCount = 0
    var wrCount = 0
    var teCount = 0
    var dstCount = 0
    var kCount = 0
    var benchCount = 0
    var flex: Flex? = null
    private val validPositions: MutableSet<String>

    constructor(qbCt: Int, rbCt: Int, wrCt: Int, teCt: Int, dCt: Int, kCt: Int,
                benchCt: Int) : this(UUID.randomUUID().toString(), qbCt, rbCt, wrCt, teCt, dCt, kCt, benchCt, Flex())

    private fun setId(id: String) {
        this.id = id
    }

    val numStartingPositions: Int
        get() = validPositions.size

    val rosterSize: Int
        get() {
            var size = qbCount + rbCount + wrCount + teCount + dstCount + kCount + benchCount
            if (flex != null) {
                size += flex!!.qbrbwrteCount + flex!!.rbwrteCount + flex!!.rbteCount +
                        flex!!.rbwrCount + flex!!.wrteCount
            }
            return size
        }

    fun getNumberStartedOfPos(position: String?): Int {
        var total = 0
        when (position) {
            Constants.QB -> {
                total = qbCount
                if (flex != null) {
                    // Assume all qbs
                    total += flex!!.qbrbwrteCount
                }
            }
            Constants.RB -> {
                total = rbCount
                if (flex != null) {
                    total += flex!!.rbteCount
                    total += flex!!.rbwrCount
                    total += flex!!.rbwrteCount
                }
            }
            Constants.WR -> {
                total = wrCount
                if (flex != null) {
                    total += flex!!.wrteCount
                    total += flex!!.rbwrCount
                    total += flex!!.rbwrteCount
                }
            }
            Constants.TE -> total = teCount
            Constants.DST -> total = dstCount
            Constants.K -> total = kCount
        }
        return total
    }

    fun isPositionValid(pos: String?): Boolean {
        return validPositions.contains(pos)
    }

    class Flex @JvmOverloads constructor(rbwr: Int = Constants.ONE_STARTER, rbwrte: Int = Constants.NO_STARTERS,
                                         rbte: Int = Constants.NO_STARTERS, wrte: Int = Constants.NO_STARTERS,
                                         qbrbwrte: Int = Constants.NO_STARTERS) {
        var rbwrCount = 0
        var rbwrteCount = 0
        var rbteCount = 0
        var wrteCount = 0
        var qbrbwrteCount = 0

        init {
            rbwrCount = rbwr
            rbteCount = rbte
            rbwrteCount = rbwrte
            wrteCount = wrte
            qbrbwrteCount = qbrbwrte
        }
    }

    init {
        setId(id)
        this.qbCount = qbCt
        this.rbCount = rbCt
        this.wrCount = wrCt
        this.teCount = teCt
        this.dstCount = dCt
        this.kCount = kCt
        this.benchCount = benchCt
        this.flex = flex
        this.validPositions = HashSet()
        if (qbCt > 0 || flex.qbrbwrteCount > 0) {
            validPositions.add(Constants.QB)
        }
        if (rbCt > 0 || flex.rbwrCount > 0 || flex.rbwrteCount > 0 || flex.rbteCount > 0 || flex.qbrbwrteCount > 0) {
            validPositions.add(Constants.RB)
        }
        if (wrCt > 0 || flex.rbwrCount > 0 || flex.rbwrteCount > 0 || flex.wrteCount > 0 || flex.qbrbwrteCount > 0) {
            validPositions.add(Constants.WR)
        }
        if (teCt > 0 || flex.rbwrteCount > 0 || flex.rbteCount > 0 || flex.wrteCount > 0 || flex.qbrbwrteCount > 0) {
            validPositions.add(Constants.TE)
        }
        if (dCt > 0) {
            validPositions.add(Constants.DST)
        }
        if (kCt > 0) {
            validPositions.add(Constants.K)
        }
    }
}