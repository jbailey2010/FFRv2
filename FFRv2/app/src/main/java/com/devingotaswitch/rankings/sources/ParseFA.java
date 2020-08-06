package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFA {

    public static void parseFAClasses(Rankings rankings) throws IOException {
        Map<String, String> arrivingFA = new HashMap<>();
        Map<String, String> departingFA = new HashMap<>();
        getTradeChanges(arrivingFA, departingFA);
        getFAChanges(arrivingFA, departingFA);

        for (String key : arrivingFA.keySet()) {
            Team team = rankings.getTeam(key);
            if (team != null) {
                team.setIncomingFA(arrivingFA.get(key));
                team.setOutgoingFA(departingFA.get(key));
            }
        }
    }

    private static void getTradeChanges(Map<String, String> arrivingFA,
                                        Map<String, String> departingFA) {

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
                        ": " +
                        age +
                        ", " +
                        pos;

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
            if ("TBD".equals(newTeam)) {
                // Yet-unsigned players only have 6 entries per row in the table instead of 12.
                // So we're offsetting the index by 6 so the next iteration will count correctly.
                i -= 6;
            }
        }
    }
}
