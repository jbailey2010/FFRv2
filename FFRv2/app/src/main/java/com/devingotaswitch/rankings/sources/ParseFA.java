package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFA {

    private static final String TAG = "ParseFA";

    public static void parseFAClasses(Rankings rankings) throws IOException {
        Map<String, String> arrivingFA = new HashMap<>();
        Map<String, String> departingFA = new HashMap<>();
        getFAChanges(arrivingFA, departingFA);
        try {
            // This is an especially hacky parser, so making it optional
            getTradeChanges(arrivingFA, departingFA);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse trade changes", e);
        }

        for (String key : arrivingFA.keySet()) {
            Team team = rankings.getTeam(key);
            if (team != null) {
                team.setIncomingFA(arrivingFA.get(key));
                team.setOutgoingFA(departingFA.get(key));
            }
        }
    }

    private static void getTradeChanges(Map<String, String> arrivingFA,
                                        Map<String, String> departingFA) throws IOException{
        Document doc = JsoupUtils.getDocument("https://www.spotrac.com/nfl/transactions/"
                + Constants.YEAR_KEY + "/trade/");

        Elements teamNames = doc.select("table.tradetable tbody tr td.tradeitem img.tradelogo");
        for (int i = 0; i < teamNames.size(); i+=2) {
            Element elementA = teamNames.get(i);
            Element elementB = teamNames.get(i+1);
            // First, we get the team names, left to right, top to bottom. In text is says New York acquires,
            // which is ambiguous, so it parses the team name from the source for the image nearby.
            String logoAURL = elementA.attr("src");
            String teamAName = ParsingUtils.normalizeTeams(logoAURL.substring(
                    logoAURL.lastIndexOf('/') + 1).split(".png")[0]);
            String logoBURL = elementB.attr("src");
            String teamBName = ParsingUtils.normalizeTeams(logoBURL.substring(
                    logoBURL.lastIndexOf('/') + 1).split(".png")[0]);

            List<String> fromAToB = parseTradeHaul(elementB);
            List<String> fromBToA = parseTradeHaul(elementA);

            for (String playerEntry : fromAToB) {
                applyPlayerChange(arrivingFA, teamBName, departingFA, teamAName, playerEntry);
            }

            for (String playerEntry : fromBToA) {
                applyPlayerChange(arrivingFA, teamAName, departingFA, teamBName, playerEntry);
            }
        }

        cleanUpSpacing(arrivingFA);
        cleanUpSpacing(departingFA);
    }

    private static List<String> parseTradeHaul(Element element) {
        Element tradeParent = element.parent();
        Elements tradeHaul = tradeParent.select("span.tradedata span.tradeplayer");
        List<String> fromAToB = new ArrayList<>();
        for (Element tradeElement : tradeHaul) {
            // Skip picks and what those picks turned into
            if (tradeElement.text().contains("round pick") || tradeElement.text().startsWith("(#")) {
                continue;
            }
            String tradePiece = tradeElement.text().split(" \\(\\$")[0].replace(" (", ", ")
                    .replace(")", "")
                    + " (traded)";
            fromAToB.add(tradePiece);
        }
        return fromAToB;
    }

    private static void getFAChanges(Map<String, String> arrivingFA,
                                     Map<String, String> departingFA) throws IOException{
        Document doc = JsoupUtils.getDocument("https://www.spotrac.com/nfl/free-agents/");
        List<String> td = JsoupUtils.getElemsFromDoc(doc, "table.datatable tbody tr td");
        for (int i = 0; i < td.size(); i+=12) {
            String wonkyName = td.get(i);
            // The site has a hidden span with only the last name, so we find the last name and
            // split the string to filter it out. It starts out as BridgewaterTeddy Bridgewater.
            String lastName = wonkyName.split(" ")[1];
            String name = ParsingUtils.normalizeNames(wonkyName.replaceFirst(lastName, ""));

            String pos = td.get(i+1);
            String age = td.get(i+2);
            age = !StringUtils.isBlank(age) ? age : "?";
            String oldTeam = ParsingUtils.normalizeTeams(td.get(i+3));
            // Normalize teams turns tbd into tampa bay, but here it means unsigned.
            String parsedTeam = td.get(i+4);
            String newTeam = "TBD".equals(parsedTeam) ? parsedTeam : ParsingUtils.normalizeTeams(td.get(i+4));
            if (!oldTeam.equals(newTeam)) {
                String playerEntry = name +
                        ": ";
                if (!"?".equals(age)) {
                    playerEntry +=
                            age +
                            ", ";
                }
                playerEntry += pos;

                // Make sure we're not at an unsigned, last in the table entry.
                if (i+6 < td.size()) {
                    String contractLength = td.get(i + 5);
                    String contractValue = td.get(i + 6);
                    if (!StringUtils.isBlank(contractLength) && !contractLength.contains("N/A") &&
                            !contractLength.contains("-") && !StringUtils.isBlank(contractValue) &&
                            !contractValue.contains("-")) {
                        playerEntry += " (" +
                                contractLength +
                                ("1".equals(contractLength) ? " year, " : " years, ") +
                                contractValue +
                                ")";
                    }
                }

                applyPlayerChange(arrivingFA, newTeam, departingFA, oldTeam, playerEntry);

            }
            if ("TBD".equals(newTeam)) {
                // Yet-unsigned players only have 6 entries per row in the table instead of 12.
                // So we're offsetting the index by 6 so the next iteration will count correctly.
                i -= 6;
            }
        }

        postProcessFA(arrivingFA);
        postProcessFA(departingFA);
    }

    private static void applyPlayerChange(Map<String, String> arrivingFA, String newTeam,
                                   Map<String, String> departingFA, String oldTeam,
                                   String playerEntry) {
        if (arrivingFA.containsKey(newTeam)) {
            String updatedEntry = arrivingFA.get(newTeam) +
                    Constants.LINE_BREAK +
                    playerEntry;
            arrivingFA.put(newTeam, updatedEntry);
        } else {
            arrivingFA.put(newTeam, playerEntry);
        }
        if (departingFA.containsKey(oldTeam)) {
            String updatedEntry = departingFA.get(oldTeam) +
                    Constants.LINE_BREAK +
                    playerEntry;
            departingFA.put(oldTeam, updatedEntry);
        } else {
            departingFA.put(oldTeam, playerEntry);
        }
    }

    private static void postProcessFA(Map<String, String> fa) {
        // Add a line break at the end of each fa set, so there's a break between traded and fa.
        for (String key : fa.keySet()) {
            fa.put(key, fa.get(key) + Constants.LINE_BREAK);
        }
    }

    private static void cleanUpSpacing(Map<String, String> fa) {
        // Clean up any trailing line breaks on sets that don't have trades.
        for (String key : fa.keySet()) {
            String entrySet = fa.get(key);
            if (entrySet.endsWith(Constants.LINE_BREAK)) {
                entrySet = entrySet.substring(0, entrySet.length() - 1);
                fa.put(key, entrySet);
            }
        }
    }
}
