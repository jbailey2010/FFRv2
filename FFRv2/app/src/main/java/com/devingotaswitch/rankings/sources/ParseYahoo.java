package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.List;

public class ParseYahoo {
    public static void parseYahooWrapper(Rankings rankings) throws IOException {
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=QB&sort=DA_PC",
                Constants.QB);
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=RB&sort=DA_PC",
                Constants.RB);
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=WR&sort=DA_PC",
                Constants.WR);
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=TE&sort=DA_PC",
                Constants.TE);
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=K&sort=DA_PC",
                Constants.K);
        parseYahoo(
                rankings,
                "http://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=DEF&sort=DA_PC",
                Constants.DST);
    }

    private static void parseYahoo(Rankings rankings, String url, String pos)
            throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "td");
        int startingIndex = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).contains("Note") || td.get(i).contains("Notes")) {
                startingIndex = i;
                break;
            }
        }
        for (int i = startingIndex; i < td.size(); i += 4) {
            if (td.get(i).contains("AdChoices")) {
                break;
            }
            String name = "";
            String splitter = "Note ";
            if (td.get(i).split(" \\(")[0].contains("Notes")) {
                splitter = "Notes ";
            }
            String fName = td.get(i).split(" \\(")[0].split(splitter)[1]
                    .split(" - ")[0];
            String[] nameSet = fName.split(" ");
            for (int j = 0; j < nameSet.length - 1; j++) {
                name += nameSet[j] + " ";
            }
            name = name.substring(0, name.length() - 1);
            String team = nameSet[nameSet.length - 1];
            if (td.get(i).contains("DEF")) {
                if (td.get(i).contains("NYG")) {
                    name = "New York Giants";
                } else if (td.get(i).contains("NYJ")) {
                    name = "New York Jets";
                }
                name = team;
            }
            String rank = td.get(i + 1).split("\\$")[1];
            String aavStr = td.get(i + 2).split("\\$")[1];
            double aav = 0.0;
            double worth = Double.parseDouble(rank);
            if (!aavStr.equals("-") && !aavStr.equals("0.0")) {
                aav = Double.parseDouble(aavStr);
            }

            rankings.processNewPlayer(ParsingUtils.getPlayerFromRankings(name, team, pos, aav));
            rankings.processNewPlayer(ParsingUtils.getPlayerFromRankings(name, team, pos, worth));
        }
    }
}
