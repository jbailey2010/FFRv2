package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.JsoupUtils.getDocument
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException
import java.util.*

object ParseSOS {
    @Throws(IOException::class)
    fun getSOS(rankings: Rankings) {
        val doc = getDocument(
                "https://www.fantasypros.com/nfl/strength-of-schedule.php")
        val elems = doc.select("table.table-striped tbody tr td")
        val allArr: MutableList<String> = ArrayList()
        run {
            var i = 0
            while (i < elems.size) {
                allArr.add(elems[i].text())
                for (j in i + 1 until i + 7) {
                    allArr.add(elems[j].attr("data-raw-stars"))
                }
                i += 7
            }
        }
        var i = 0
        while (i < allArr.size) {
            val teamName = normalizeTeams(allArr[i])
            val currentTeam = rankings.getTeam(teamName)
            currentTeam.qbSos = allArr[i + 1].toDouble()
            currentTeam.rbSos = allArr[i + 2].toDouble()
            currentTeam.wrSos = allArr[i + 3].toDouble()
            currentTeam.teSos = allArr[i + 4].toDouble()
            currentTeam.kSos = allArr[i + 5].toDouble()
            currentTeam.dstSos = allArr[i + 6].toDouble()
            i += 7
        }
    }
}