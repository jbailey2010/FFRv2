package com.devingotaswitch.rankings.domain

import com.amazonaws.util.StringUtils
import com.devingotaswitch.utils.Constants

class Team {
    var name: String = ""
    var oLineRanks: String? = null
    var draftClass: String? = null
    var qbSos = 0.0
    var rbSos = 0.0
    var wrSos = 0.0
    var teSos = 0.0
    var dstSos = 0.0
    var kSos = 0.0
    var bye: String? = null
    var incomingFA: String? = null
    var outgoingFA: String? = null
    var schedule: String? = null

    var faClass: String?
        get() = if (!StringUtils.isBlank(incomingFA) && !StringUtils.isBlank(outgoingFA)) {
            incomingFA +
                    FA_DELIMITER +
                    outgoingFA
        } else ""
        set(faClass) {
            if (faClass != null && faClass.contains(FA_DELIMITER)) {
                val faArr = faClass.split(FA_DELIMITER)
                incomingFA = faArr[0]
                outgoingFA = faArr[1]
            }
        }

    fun getSosForPosition(position: String?): Double {
        when (position) {
            Constants.QB -> return qbSos
            Constants.RB -> return rbSos
            Constants.WR -> return wrSos
            Constants.TE -> return teSos
            Constants.DST -> return dstSos
            Constants.K -> return kSos
        }
        return Constants.DEFAULT_SOS
    }

    companion object {
        private const val FA_DELIMITER = "~~~"
    }
}