package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParseMFL {

    private static Set<String> positions = new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR,
            Constants.TE, Constants.DST, Constants.K));
    public static void getMFLAAVs(Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.handleLists(
                "http://www03.myfantasyleague.com/2017/aav?COUNT=500&POS=*&CUTOFF=5&IS_PPR=1&IS_KEEPER=-1&TIME=",
                "table.report tbody tr td");

        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).contains(", ")) {
                min = i;
                break;
            }
        }

        for (int i = min; i < td.size(); i += 6) {
            if (i >= td.size() - 3) {
                break;
            }
            double val = Double.parseDouble(td.get(i+1).substring(1)) * 2.0;
            String nameTeamPos = td.get(i).replaceAll("\\*", "");
            String pos = nameTeamPos.substring(nameTeamPos.lastIndexOf(" ")).replaceAll("\\s","");
            String nameTeam = nameTeamPos.substring(0, nameTeamPos.lastIndexOf(" "));
            String team = nameTeam.substring(nameTeam.lastIndexOf(" "));
            String nameBackwards = nameTeam.substring(0, nameTeam.lastIndexOf(" "));
            String[] nameSet = nameBackwards.split(", ");
            String name = nameSet[1] + " " + nameSet[0];
            team = team.replaceAll("\\s","");
            if ("Def".equals(pos)) {
                pos = Constants.DST;
            } else if ("PK".equals(pos)) {
                pos = Constants.K;
            }
            if (positions.contains(pos)) {
                rankings.processNewPlayer(ParsingUtils.getPlayerFromRankings(name, team, pos, val));
            }
        }
    }
}
