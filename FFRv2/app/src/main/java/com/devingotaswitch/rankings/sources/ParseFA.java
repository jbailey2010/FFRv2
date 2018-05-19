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
        List<String> td = JsoupUtils.parseURLWithUA("https://www.profootballfocus.com/" + Constants.YEAR_KEY + "-nfl-free-agency-tracker/",
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
                String playerEntry = new StringBuilder (name)
                        .append(": ")
                        .append(age)
                        .append(", ")
                        .append(pos)
                        .append(" (")
                        .append(snaps)
                        .append(" snaps, ")
                        .append(grade)
                        .append(" PFF grade)")
                        .toString();

                if (arrivingFA.containsKey(newTeam)) {
                    String updatedEntry = new StringBuilder(arrivingFA.get(newTeam))
                            .append(Constants.LINE_BREAK)
                            .append(playerEntry)
                            .toString();
                    arrivingFA.put(newTeam, updatedEntry);
                } else {
                    arrivingFA.put(newTeam, playerEntry);
                }
                if (departingFA.containsKey(oldTeam)) {
                    String updatedEntry = new StringBuilder(departingFA.get(oldTeam))
                            .append(Constants.LINE_BREAK)
                            .append(playerEntry)
                            .toString();
                    departingFA.put(oldTeam, updatedEntry);
                } else {
                    departingFA.put(oldTeam, playerEntry);
                }
            }
        }
        for (String key : arrivingFA.keySet()) {
            Team team = rankings.getTeam(key);
            if (team != null) {
                String faClass = new StringBuilder("Arriving:")
                        .append(Constants.LINE_BREAK)
                        .append(arrivingFA.get(key))
                        .append(Constants.LINE_BREAK)
                        .append(Constants.LINE_BREAK)
                        .append("Departing:")
                        .append(Constants.LINE_BREAK)
                        .append(departingFA.get(key))
                        .toString();
                team.setFaClass(faClass);
            }
        }
    }
}
