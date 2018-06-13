package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ParseDraft {

    public static void parseTeamDraft(Rankings rankings) throws IOException {
        List<String> perPick = JsoupUtils
                .parseURLWithUA(
                        "http://www.nfl.com/draft/history/fulldraft",
                        "table.data-table1 tbody tr td");
        HashMap<String, String> picks = new HashMap<>();
        for (int i = 0; i < perPick.size(); i+= 5) {
            String pick = perPick.get(i);
            String team = ParsingUtils.normalizeTeams(perPick.get(i+1));
            String name = perPick.get(i+2);
            String pos = perPick.get(i+3);
            String college = perPick.get(i+4);
            String draftData = pick +
                    ": " +
                    name +
                    ", " +
                    pos +
                    " - " +
                    college;
            if (picks.containsKey(team)) {
                String existingData = picks.get(team);
                String updated = existingData +
                        Constants.LINE_BREAK +
                        draftData;
                picks.put(team, updated);
            }
            else {
                picks.put(team, draftData);
            }
        }

        for (String key : picks.keySet()) {
            Team team = rankings.getTeam(key);
            team.setDraftClass(picks.get(key));
        }
    }
}
