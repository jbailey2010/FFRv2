package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class ParseESPN {
    public static void parseESPNAggregate(Rankings rankings) throws IOException {
        List<String> brokenValues = JsoupUtils.handleLists(
                "http://games.espn.go.com/ffl/livedraftresults",
                "table.tableBody tbody tr td");

        int min = 0;
        for (int i = 0; i < brokenValues.size(); i++) {
            if (GeneralUtils.isInteger(brokenValues.get(i))) {
                min = i;
                break;
            }
        }
        for (int i = min; i < brokenValues.size(); i += 8) {
            if (i + 1 >= brokenValues.size()) {
                break;
            }
            String name;
            String team;
            if (brokenValues.get(i+1).contains(", ")) {
                name = brokenValues.get(i + 1).split(", ")[0].replace("*", "");
                team = brokenValues.get(i + 1).split(", ")[1];
            } else {
                // Defense
                name = brokenValues.get(i + 1);
                team = name.split(" D/ST")[0];
            }
            String val = brokenValues.get(i + 5);
            Double worth = Double.parseDouble(val);
            String pos = brokenValues.get(i+2);
            rankings.processNewPlayer(ParsingUtils.getPlayerFromRankings(name, team, pos, worth));
        }
    }
}
