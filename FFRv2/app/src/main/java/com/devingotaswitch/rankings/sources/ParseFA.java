package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFA {

    public static void parseFAClasses(Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA("https://www.profootballfocus.com/news/pro-"
                        + Constants.YEAR_KEY + "-nfl-free-agent-tracker/",
                "article table tbody tr td");
        Map<String, String> arrivingFA = new HashMap<>();
        Map<String, String> departingFA = new HashMap<>();
        for (int i = 0; i < td.size(); i+=7) {
            String name = ParsingUtils.normalizeNames(td.get(i));
            String grade = td.get(i+1).split(" \\(")[0];
            String pos = td.get(i+2);
            String age = td.get(i+3);
            String oldTeam = ParsingUtils.normalizeTeams(td.get(i+4).split(" \\(")[0]);
            String newTeam = ParsingUtils.normalizeTeams(td.get(i+6).split(" \\(")[0]);
            String snaps = td.get(i+5);

            // Ignore re-signings and bit players
            if (!oldTeam.equals(newTeam) && GeneralUtils.isInteger(snaps) && Integer.parseInt(snaps) > 200) {
                String playerEntry = name +
                        ": " +
                        age +
                        ", " +
                        pos +
                        " (" +
                        snaps +
                        " snaps, " +
                        grade +
                        " PFF grade)";

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
        }
        for (String key : arrivingFA.keySet()) {
            Team team = rankings.getTeam(key);
            if (team != null) {
                String faClass = "Arriving:" +
                        Constants.LINE_BREAK +
                        arrivingFA.get(key) +
                        Constants.LINE_BREAK +
                        Constants.LINE_BREAK +
                        "Departing:" +
                        Constants.LINE_BREAK +
                        departingFA.get(key);
                team.setFaClass(faClass);
            }
        }
    }
}
