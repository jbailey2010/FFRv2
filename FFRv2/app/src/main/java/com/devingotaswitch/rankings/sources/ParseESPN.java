package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;

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
        for (int i = 0; i < brokenValues.size(); i++) {
            Log.d("ESPN", i + ": " + brokenValues.get(i));
        }
        for (int i = min; i < brokenValues.size(); i += 8) {
            if (i + 1 >= brokenValues.size()) {
                break;
            }
            String name = brokenValues.get(i + 1).split(", ")[0].replace("*", "");
            String team = brokenValues.get(i + 1).split(", ")[1];
            String val = brokenValues.get(i + 5);
            double worth = Double.parseDouble(val);
            String pos = brokenValues.get(i+2);
            Log.d("ESPN", name + ": " + team + ", " + pos + " - " + worth);
        }
    }
}
