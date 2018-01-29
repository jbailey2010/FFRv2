package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class ParsePFO {
    public static void parsePFOLineData(Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.handleLists(
                "http://www.footballoutsiders.com/stats/ol", "td");
        Map<String, String> data = new HashMap<>();
        int start = 0;
        for (int i = 0; i < td.size(); i++) {
            if (GeneralUtils.isInteger(td.get(i))) {
                start = i;
                break;
            }
        }
        for (int i = start; i < td.size(); i += 16) {
            if (td.get(i).equals("RUN BLOCKING")) {
                i += 2;
                continue;
            } else if (td.get(i + 1).equals("Team") || "".equals(td.get(i+1))) {
                i += 16;
                continue;
            } else if (td.get(i + 1).equals("NFL")) {
                break;
            }

            String team2 = ParsingUtils.normalizeTeams(td.get(i + 12));
            String sack = td.get(i + 15);
            data.put(team2 + "/pass", sack + " adjusted team sack rate ("
                    + td.get(i + 13) + ")\n");
            data.put(team2 + "/passranks",
                    "Pass Block Ranking: " + td.get(i + 13));
            String team1 = ParsingUtils.normalizeTeams(td.get(i + 1));
            String adjYPC = td.get(i + 2);
            String adjYPCRank = td.get(i);
            String power = td.get(i + 4);
            String powerRank = td.get(i + 5);
            String stuff = td.get(i + 6);
            String stuffRank = td.get(i + 7);
            String secLevel = td.get(i + 8);
            String secLevelRank = td.get(i + 9);
            String openField = td.get(i + 10);
            String openFieldRank = td.get(i + 11);
            StringBuilder runData = new StringBuilder(1000);
            runData.append(adjYPC + " adjusted team yards per carry ("
                    + adjYPCRank + ")\n");
            runData.append(power + " success rate with < 3 yards per go ("
                    + powerRank + ")\n");
            runData.append(stuff + " rate of being stuffed at the line ("
                    + stuffRank + ")\n");
            runData.append(secLevel
                    + " YPC earned between 5 and 10 yards past LOS ("
                    + secLevelRank + ")\n");
            runData.append(openField + " YPC earned 10+ yards past LOS ("
                    + openFieldRank + ")");
            data.put(team1 + "/run", runData.toString());
            data.put(team1 + "/runranks", "Run Block Ranking: " + adjYPCRank);
        }
        List<String> teams = new ArrayList<>();
        Set<String> keys = data.keySet();
        for (String key : keys) {
            String team = key.split("/")[0];
            if (!teams.contains(team)) {
                teams.add(team);
            }
        }
        PriorityQueue<String> math = new PriorityQueue<>(100,
                new Comparator<String>() {
                    @Override
                    public int compare(String a, String b) {
                        String runA = a.split("~~~~")[1].split(": ")[1];
                        String runB = b.split("~~~~")[1].split(": ")[1];
                        String passA = a.split("~~~~")[0].split(": ")[1];
                        String passB = b.split("~~~~")[0].split(": ")[1];
                        double overallA = (double) (Integer.parseInt(runA) + Integer
                                .parseInt(passA)) / 2.0;
                        double overallB = (double) (Integer.parseInt(runB) + Integer
                                .parseInt(passB)) / 2.0;
                        if (overallA > overallB) {
                            return 1;
                        }
                        if (overallA < overallB) {
                            return -1;
                        }
                        return 0;
                    }
                });
        for (String team : teams) {
            String input = data.get(team + "/passranks") + "~~~~"
                    + data.get(team + "/runranks") + "~~~~" + team;
            if (input.split("~~~~")[1].split(": ").length > 1) {
                math.add(input);
            }
        }
        if (math.size() > 0) {
            for (int i = 1; i < 33; i++) {
                String full = math.poll();
                String team = full.split("~~~~")[2];
                data.put(team + "/overallranks", String.valueOf(i));
            }
        }
        for (String team : teams) {
            String val = new StringBuilder(data.get(team + "/pass"))
                    .append(data.get(team + "/run"))
                    .append(Constants.LINE_BREAK)
                    .append(Constants.LINE_BREAK)
                    .append(data.get(team + "/passranks"))
                    .append(Constants.LINE_BREAK)
                    .append(data.get(team + "/runranks"))
                    .append(Constants.LINE_BREAK)
                    .append("Overall Ranking: ")
                    .append(data.get(team + "/overallranks"))
                    .toString();
            rankings.getTeam(team).setoLineRanks(val);
        }
    }
}
