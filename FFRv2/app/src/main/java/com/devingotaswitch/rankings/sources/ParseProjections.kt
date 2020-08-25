package com.devingotaswitch.rankings.sources

import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.projections.PlayerProjection
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils.isDouble
import com.devingotaswitch.utils.JsoupUtils.parseURLWithUA
import com.devingotaswitch.utils.ParsingUtils.normalizeDefenses
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import java.io.IOException
import java.util.*

object ParseProjections {
    @Throws(IOException::class)
    fun projPointsWrapper(rankings: Rankings) {
        val points: MutableMap<String, PlayerProjection?> = HashMap()
        qbProj(
                points, rankings)
        rbProj(
                points, rankings)
        wrProj(
                points, rankings)
        teProj(
                points, rankings)
        defProj(
                points, rankings)
        kProj(
                points, rankings)
        for (playerId in rankings.players.keys) {
            val player = rankings.getPlayer(playerId)
            if (points.containsKey(playerId)) {
                player.playerProjection = points[playerId]
            } else {
                player.playerProjection = PlayerProjection(rankings.leagueSettings.scoringSettings)
            }
        }
    }

    @Throws(IOException::class)
    private fun qbProj(points: MutableMap<String, PlayerProjection?>,
                       rankings: Rankings) {
        val td = parseURLWithUA("http://www.fantasypros.com/nfl/projections/qb.php?year=2020&week=draft", "table.table-bordered tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (td[i].split(" ").size >= 3) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            var name = StringBuilder()
            var nameSet: Array<String?> = td[i].split(" ").toTypedArray()
            if (nameSet.size == 1) {
                if (td[i + 1].contains("Site Projections")) {
                    break
                }
                nameSet = td[++i].split(" ").toTypedArray()
            }
            val nameLimit = if (nameSet.size == 2) nameSet.size else nameSet.size - 1
            for (j in 0 until nameLimit) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(normalizeNames(name.substring(0, name.length - 1)))
            val team = normalizeTeams(nameSet[nameSet.size - 1])
            val yards = td[i + 3].replace(",", "").toDouble()
            val passTd = td[i + 4].toDouble()
            val ints = td[i + 5].toDouble()
            val rushYards = td[i + 7].toDouble()
            val rushTD = td[i + 8].toDouble()
            val fumbles = td[i + 9].toDouble()
            val projection = PlayerProjection(yards, passTd, rushYards, rushTD, 0.0, 0.0, 0.0,
                    fumbles, ints, 0.0, 0.0, rankings.leagueSettings.scoringSettings)
            points["" + name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.QB] = projection
            i += 11
        }
    }

    @Throws(IOException::class)
    private fun rbProj(points: MutableMap<String, PlayerProjection?>,
                       rankings: Rankings) {
        val td = parseURLWithUA("http://www.fantasypros.com/nfl/projections/rb.php?year=2020&week=draft", "table.table-bordered tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (td[i].split(" ").size >= 3) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            var name = StringBuilder()
            var nameSet: Array<String?> = td[i].split(" ").toTypedArray()
            if (nameSet.size == 1) {
                if (td[i + 1].contains("Site Projections")) {
                    break
                }
                nameSet = td[++i].split(" ").toTypedArray()
            }
            val nameLimit = if (nameSet.size == 2) nameSet.size else nameSet.size - 1
            for (j in 0 until nameLimit) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(normalizeNames(name.substring(0, name.length - 1)))
            val team = normalizeTeams(nameSet[nameSet.size - 1])
            val rushYards = td[i + 2]
                    .replace(",", "").toDouble()
            val rushTD = td[i + 3].toDouble()
            val catches = td[i + 4].toDouble()
            val recYards = td[i + 5].replace(",", "").toDouble()
            val recTD = td[i + 6].toDouble()
            val fumbles = td[i + 7].toDouble()
            val projection = PlayerProjection(0.0, 0.0, rushYards, rushTD,
                    recYards, recTD, catches, fumbles, 0.0, 0.0, 0.0, rankings.leagueSettings.scoringSettings)
            points["" + name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.RB] = projection
            i += 9
        }
    }

    @Throws(IOException::class)
    private fun wrProj(points: MutableMap<String, PlayerProjection?>,
                       rankings: Rankings) {
        val td = parseURLWithUA("http://www.fantasypros.com/nfl/projections/wr.php?year=2020&week=draft", "table.table-bordered tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (td[i].split(" ").size >= 3) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            var name = StringBuilder()
            var nameSet: Array<String?> = td[i].split(" ").toTypedArray()
            if (nameSet.size == 1) {
                if (td[i + 1].contains("Site Projections")) {
                    break
                }
                nameSet = td[++i].split(" ").toTypedArray()
            }
            val nameLimit = if (nameSet.size == 2) nameSet.size else nameSet.size - 1
            for (j in 0 until nameLimit) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(normalizeNames(name.substring(0, name.length - 1)))
            val team = normalizeTeams(nameSet[nameSet.size - 1])
            val rushYards = td[i + 5]
                    .replace(",", "").toDouble()
            val rushTD = td[i + 6].toDouble()
            val catches = td[i + 1].toDouble()
            val recYards = td[i + 2].replace(",", "").toDouble()
            val recTD = td[i + 3].toDouble()
            val fumbles = td[i + 7].toDouble()
            val projection = PlayerProjection(0.0, 0.0, rushYards, rushTD,
                    recYards, recTD, catches, fumbles, 0.0, 0.0, 0.0, rankings.leagueSettings.scoringSettings)
            points["" + name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.WR] = projection
            i += 9
        }
    }

    @Throws(IOException::class)
    private fun teProj(points: MutableMap<String, PlayerProjection?>,
                       rankings: Rankings) {
        val td = parseURLWithUA("http://www.fantasypros.com/nfl/projections/te.php?year=2020&week=draft", "table.table-bordered tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (td[i].split(" ").size >= 3) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            var name = StringBuilder()
            var nameSet: Array<String?> = td[i].split(" ").toTypedArray()
            if (nameSet.size == 1) {
                if (td[i + 1].contains("Site Projections")) {
                    break
                }
                nameSet = td[++i].split(" ").toTypedArray()
            }
            val nameLimit = if (nameSet.size == 2) nameSet.size else nameSet.size - 1
            for (j in 0 until nameLimit) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(normalizeNames(name.substring(0, name.length - 1)))
            val team = normalizeTeams(nameSet[nameSet.size - 1])
            val catches = td[i + 1].replace(",", "").toDouble()
            val recTD = td[i + 3].toDouble()
            val recYards = td[i + 2].replace(",", "").toDouble()
            val fumbles = td[i + 4].toDouble()
            val projection = PlayerProjection(0.0, 0.0, 0.0, 0.0,
                    recYards, recTD, catches, fumbles, 0.0, 0.0, 0.0, rankings.leagueSettings.scoringSettings)
            points["" + name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.TE] = projection
            i += 6
        }
    }

    @Throws(IOException::class)
    private fun defProj(points: MutableMap<String, PlayerProjection?>, rankings: Rankings) {
        val td = parseURLWithUA("http://www.fantasypros.com/nfl/projections/dst.php?year=2020&week=draft", "table.table-bordered tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (td[i].split(" ").size >= 2 && isDouble(td[i + 1])) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            var name = StringBuilder()
            var nameSet: Array<String?> = td[i].split(" ").toTypedArray()
            if (nameSet.size == 1) {
                if (td[i + 1].contains("Site Projections")) {
                    break
                }
                nameSet = td[++i].split(" ").toTypedArray()
            }
            val nameLimit = if (nameSet.size == 2) nameSet.size else nameSet.size - 1
            for (j in 0 until nameLimit) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(name.substring(0, name.length - 1))
            val team = normalizeTeams(name.toString())
            name = StringBuilder(normalizeDefenses(name.toString()))
            val proj = td[i + 9].toDouble()
            val projection = PlayerProjection(0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, proj, 0.0,
                    rankings.leagueSettings.scoringSettings)
            points["" + name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.DST] = projection
            i += 10
        }
    }

    @Throws(IOException::class)
    private fun kProj(points: MutableMap<String, PlayerProjection?>, rankings: Rankings) {
        val td = parseURLWithUA("http://www.fantasypros.com/nfl/projections/k.php?year=2020&week=draft", "table.table-bordered tbody tr td")
        var min = 0
        for (i in td.indices) {
            if (td[i].split(" ").size >= 3) {
                min = i
                break
            }
        }
        var i = min
        while (i < td.size) {
            var name = StringBuilder()
            var nameSet: Array<String?> = td[i].split(" ").toTypedArray()
            if (nameSet.size == 1) {
                if (td[i + 1].contains("Site Projections")) {
                    break
                }
                nameSet = td[++i].split(" ").toTypedArray()
            }
            val nameLimit = if (nameSet.size == 2) nameSet.size else nameSet.size - 1
            for (j in 0 until nameLimit) {
                name.append(nameSet[j]).append(" ")
            }
            name = StringBuilder(normalizeNames(name.substring(0, name.length - 1)))
            val team = normalizeTeams(nameSet[nameSet.size - 1])
            val proj = td[i + 4].toDouble()
            val projection = PlayerProjection(0.0, 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, proj,
                    rankings.leagueSettings.scoringSettings)
            points["" + name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.K] = projection
            i += 5
        }
    }
}