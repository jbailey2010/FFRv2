package com.devingotaswitch.rankings.sources

import android.util.Log
import com.amazonaws.util.StringUtils
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.JsoupUtils.getDocument
import com.devingotaswitch.utils.JsoupUtils.getElemsFromDoc
import com.devingotaswitch.utils.ParsingUtils.normalizeNames
import com.devingotaswitch.utils.ParsingUtils.normalizeTeams
import org.jsoup.nodes.Element
import java.io.IOException
import java.util.*

object ParseFA {
    private const val TAG = "ParseFA"
    @Throws(IOException::class)
    fun parseFAClasses(rankings: Rankings) {
        val arrivingFA: MutableMap<String?, String?> = HashMap()
        val departingFA: MutableMap<String?, String?> = HashMap()
        getFAChanges(arrivingFA, departingFA)
        try {
            // This is an especially hacky parser, so making it optional
            getTradeChanges(arrivingFA, departingFA)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse trade changes", e)
        }
        for (key in arrivingFA.keys) {
            val team = rankings.getTeam(key)
            if (team != null) {
                team.incomingFA = arrivingFA[key]
                team.outgoingFA = departingFA[key]
            }
        }
    }

    @Throws(IOException::class)
    private fun getTradeChanges(arrivingFA: MutableMap<String?, String?>,
                                departingFA: MutableMap<String?, String?>) {
        val doc = getDocument("https://www.spotrac.com/nfl/transactions/"
                + Constants.YEAR_KEY + "/trade/")
        val teamNames = doc.select("table.tradetable tbody tr td.tradeitem img.tradelogo")
        var i = 0
        while (i < teamNames.size) {
            val elementA = teamNames[i]
            val elementB = teamNames[i + 1]
            // First, we get the team names, left to right, top to bottom. In text is says New York acquires,
            // which is ambiguous, so it parses the team name from the source for the image nearby.
            val logoAURL = elementA.attr("src")
            val teamAName = normalizeTeams(logoAURL.substring(
                    logoAURL.lastIndexOf('/') + 1).split(".png")[0])
            val logoBURL = elementB.attr("src")
            val teamBName = normalizeTeams(logoBURL.substring(
                    logoBURL.lastIndexOf('/') + 1).split(".png")[0])
            val fromAToB = parseTradeHaul(elementB)
            val fromBToA = parseTradeHaul(elementA)
            for (playerEntry in fromAToB) {
                applyPlayerChange(arrivingFA, teamBName, departingFA, teamAName, playerEntry)
            }
            for (playerEntry in fromBToA) {
                applyPlayerChange(arrivingFA, teamAName, departingFA, teamBName, playerEntry)
            }
            i += 2
        }
        cleanUpSpacing(arrivingFA)
        cleanUpSpacing(departingFA)
    }

    private fun parseTradeHaul(element: Element): List<String> {
        val tradeParent = element.parent()
        val tradeHaul = tradeParent.select("span.tradedata span.tradeplayer")
        val fromAToB: MutableList<String> = ArrayList()
        for (tradeElement in tradeHaul) {
            // Skip picks and what those picks turned into
            if (tradeElement.text().contains("round pick") || tradeElement.text().startsWith("(#")) {
                continue
            }
            val tradePiece = (tradeElement.text().split(" \\(\\$".toRegex())
                    [0].replace(" (", ", ")
                    .replace(")", "")
                    + " (traded)")
            fromAToB.add(tradePiece)
        }
        return fromAToB
    }

    @Throws(IOException::class)
    private fun getFAChanges(arrivingFA: MutableMap<String?, String?>,
                             departingFA: MutableMap<String?, String?>) {
        val doc = getDocument("https://www.spotrac.com/nfl/free-agents/")
        val td = getElemsFromDoc(doc, "table.datatable tbody tr td")
        var i = 0
        while (i < td.size) {
            val wonkyName = td[i]
            // The site has a hidden span with only the last name, so we find the last name and
            // split the string to filter it out. It starts out as BridgewaterTeddy Bridgewater.
            val lastName = wonkyName.split(" ")[1]
            val name = normalizeNames(wonkyName.replaceFirst(lastName.toRegex(), ""))
            val pos = td[i + 1]
            var age = td[i + 2]
            age = if (!StringUtils.isBlank(age)) age else "?"
            val oldTeam = normalizeTeams(td[i + 3])
            // Normalize teams turns tbd into tampa bay, but here it means unsigned.
            val parsedTeam = td[i + 4]
            val newTeam = if ("TBD" == parsedTeam) parsedTeam else normalizeTeams(td[i + 4])!!
            if (oldTeam != newTeam) {
                var playerEntry = name +
                        ": "
                if ("?" != age) {
                    playerEntry += age +
                            ", "
                }
                playerEntry += pos

                // Make sure we're not at an unsigned, last in the table entry.
                if (i + 6 < td.size) {
                    val contractLength = td[i + 5]
                    val contractValue = td[i + 6]
                    if (!StringUtils.isBlank(contractLength) && !contractLength.contains("N/A") &&
                            !contractLength.contains("-") && !StringUtils.isBlank(contractValue) &&
                            !contractValue.contains("-")) {
                        playerEntry += " (" +
                                contractLength +
                                (if ("1" == contractLength) " year, " else " years, ") +
                                contractValue +
                                ")"
                    }
                }
                applyPlayerChange(arrivingFA, newTeam, departingFA, oldTeam, playerEntry)
            }
            if ("TBD" == newTeam) {
                // Yet-unsigned players only have 6 entries per row in the table instead of 12.
                // So we're offsetting the index by 6 so the next iteration will count correctly.
                i -= 6
            }
            i += 12
        }
        postProcessFA(arrivingFA)
        postProcessFA(departingFA)
    }

    private fun applyPlayerChange(arrivingFA: MutableMap<String?, String?>, newTeam: String?,
                                  departingFA: MutableMap<String?, String?>, oldTeam: String?,
                                  playerEntry: String) {
        if (arrivingFA.containsKey(newTeam)) {
            val updatedEntry = arrivingFA[newTeam] +
                    Constants.LINE_BREAK +
                    playerEntry
            arrivingFA[newTeam] = updatedEntry
        } else {
            arrivingFA[newTeam] = playerEntry
        }
        if (departingFA.containsKey(oldTeam)) {
            val updatedEntry = departingFA[oldTeam] +
                    Constants.LINE_BREAK +
                    playerEntry
            departingFA[oldTeam] = updatedEntry
        } else {
            departingFA[oldTeam] = playerEntry
        }
    }

    private fun postProcessFA(fa: MutableMap<String?, String?>) {
        // Add a line break at the end of each fa set, so there's a break between traded and fa.
        for (key in fa.keys) {
            fa[key] = fa[key] + Constants.LINE_BREAK
        }
    }

    private fun cleanUpSpacing(fa: MutableMap<String?, String?>) {
        // Clean up any trailing line breaks on sets that don't have trades.
        for (key in fa.keys) {
            var entrySet = fa[key]
            if (entrySet!!.endsWith(Constants.LINE_BREAK)) {
                entrySet = entrySet.substring(0, entrySet.length - 1)
                fa[key] = entrySet
            }
        }
    }
}