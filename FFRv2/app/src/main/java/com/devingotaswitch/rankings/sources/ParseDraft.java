package com.devingotaswitch.rankings.sources;

import android.util.Log;

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
                .handleLists(
                        "https://www.sbnation.com/nfl/2017/4/27/15372494/2017-nfl-draft-results-pick-by-pick",
                        "table.p-data-table tbody tr td");
        HashMap<String, String> picks = new HashMap<>();
        for (int i = 0; i < perPick.size(); i+= 7) {
            String round = perPick.get(i);
            String pick = perPick.get(i+1);
            String team = ParsingUtils.normalizeTeams(perPick.get(i+3));
            String name = perPick.get(i+4);
            String pos = perPick.get(i+5);
            String college = perPick.get(i+6);
            String draftData = new StringBuilder(round)
                    .append(".")
                    .append(pick)
                    .append(": ")
                    .append(name)
                    .append(", ")
                    .append(pos)
                    .append(" - ")
                    .append(college)
                    .toString();
            if (picks.containsKey(team)) {
                String existingData = picks.get(team);
                String updated = new StringBuilder(existingData)
                        .append(Constants.LINE_BREAK)
                        .append(draftData)
                        .toString();
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
