package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.List;

public class ParsePFO {
    public static void parsePFOLineData(Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(
                "https://www.footballoutsiders.com/stats/nfl/offensive-line/" + Constants.LAST_YEAR_KEY,
                "table.stats td");
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

            String team = ParsingUtils.normalizeTeams(td.get(i+1));
            String sackRate = td.get(i + 15);
            String pbRank = td.get(i + 13);
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

            String olData = sackRate +
                    " adjusted team sack rate." +
                    Constants.LINE_BREAK +
                    adjYPC +
                    " adjusted team yards per carry (" +
                    adjYPCRank +
                    ")" +
                    Constants.LINE_BREAK +
                    "Pass Block Ranking: " +
                    pbRank +
                    Constants.LINE_BREAK +
                    power +
                    " success rate with < 3 yards to go (" +
                    powerRank +
                    ")" +
                    Constants.LINE_BREAK +
                    stuff +
                    " rate of being stuffed at the line (" +
                    stuffRank +
                    ")" +
                    Constants.LINE_BREAK +
                    secLevel +
                    " YPC earned 5 to 10 yards past LOS (" +
                    secLevelRank +
                    ")" +
                    Constants.LINE_BREAK +
                    openField +
                    " YPC earned 10+ yards past LOS (" +
                    openFieldRank +
                    ")" +
                    Constants.LINE_BREAK +
                    Constants.LINE_BREAK +
                    "Pass Block Ranking: " +
                    pbRank +
                    Constants.LINE_BREAK +
                    "Run Block Ranking: " +
                    adjYPCRank;

            rankings.getTeam(team).setoLineRanks(olData);
        }
    }
}
