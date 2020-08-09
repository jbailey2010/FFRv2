package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ParseDraft {

    public static void parseTeamDraft(Rankings rankings) throws IOException {
        Document doc = JsoupUtils.getDocument("https://www.spotrac.com/nfl/draft/");
        List<String> names = JsoupUtils.getElemsFromDoc(doc, "table.datatable tbody tr td.player a");
        List<String> pickContexts = JsoupUtils.getElemsFromDoc(doc, "table.datatable tbody tr td.center");
        HashMap<String, String> picks = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {

            // There are 10 total items in a row, 1 of type player, 9 of type center. Since they embed news in
            // some rows, I did two selections, one of each class. We're iterating based on players, then converting
            // that index for the other stuff. Since there's 9 of those per player, it's x*9 + y.
            int contextIndex = i * 9;
            String pick = pickContexts.get(contextIndex);
            String team = ParsingUtils.normalizeTeams(pickContexts.get(contextIndex+1).split(" from ")[0]);
            String name = names.get(i);
            String pos = pickContexts.get(contextIndex+2);
            String age = pickContexts.get(contextIndex+3);
            String college = pickContexts.get(contextIndex+4);
            String draftData = pick +
                    ": " +
                    name +
                    ", " +
                    pos +
                    " - " +
                    college +
                    " (" +
                    age +
                    ")";

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
