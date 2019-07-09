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
        List<String> td = JsoupUtils.parseURLWithUA(
                "http://www.footballoutsiders.com/stats/ol", "table.stats td");
        int start = 0;
        for (int i = 0; i < td.size(); i++) {
            if (GeneralUtils.isInteger(td.get(i))) {
                start = i;
                break;
            }
        }
        for (int i = start; i < td.size(); i += 15) {
            if (td.get(i).equals("RUN BLOCKING")) {
                i += 2;
                continue;
            } else if (td.get(i + 1).equals("Team") || "".equals(td.get(i+1))) {
                i += 15;
                continue;
            } else if (td.get(i + 1).equals("NFL")) {
                break;
            }

            String team = ParsingUtils.normalizeTeams(td.get(i+1));
            String sackRate = td.get(i + 14);
            String pbRank = td.get(i + 12);
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

            String olData = new StringBuilder(sackRate)
                    .append(" adjusted team sack rate.")
                    .append(Constants.LINE_BREAK)
                    .append(adjYPC)
                    .append(" adjusted team yards per carry (")
                    .append(adjYPCRank)
                    .append(")")
                    .append(Constants.LINE_BREAK)
                    .append("Pass Block Ranking: ")
                    .append(pbRank)
                    .append(Constants.LINE_BREAK)
                    .append(power)
                    .append(" success rate with < 3 yards to go (")
                    .append(powerRank)
                    .append(")")
                    .append(Constants.LINE_BREAK)
                    .append(stuff)
                    .append(" rate of being stuffed at the line (")
                    .append(stuffRank)
                    .append(")")
                    .append(Constants.LINE_BREAK)
                    .append(secLevel)
                    .append(" YPC earned 5 to 10 yards past LOS (")
                    .append(secLevelRank)
                    .append(")")
                    .append(Constants.LINE_BREAK)
                    .append(openField)
                    .append(" YPC earned 10+ yards past LOS (")
                    .append(openFieldRank)
                    .append(")")
                    .append(Constants.LINE_BREAK)
                    .append(Constants.LINE_BREAK)
                    .append("Pass Block Ranking: ")
                    .append(pbRank)
                    .append(Constants.LINE_BREAK)
                    .append("Run Block Ranking: ")
                    .append(adjYPCRank)
                    .toString();

            rankings.getTeam(team).setoLineRanks(olData);
        }
    }
}
