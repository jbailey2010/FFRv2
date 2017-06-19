package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;

import java.io.IOException;
import java.util.List;

public class ParseCBS {
    public static void cbsRankings(Rankings rankings)
            throws IOException {
        String type = "standard/";
        if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0.0) {
            type = "ppr/";
        }
        String url = "http://www.cbssports.com/fantasy/football/rankings/";
        cbsWorker(rankings, url + type + "QB/yearly/", Constants.QB);
        cbsWorker(rankings, url + type + "RB/yearly/", Constants.RB);
        cbsWorker(rankings, url + type + "WR/yearly/", Constants.WR);
        cbsWorker(rankings, url + type + "TE/yearly/", Constants.TE);
        cbsWorker(rankings, url + type + "DST/yearly/", Constants.DST);
        cbsWorker(rankings, url + type + "K/yearly/", Constants.K);

    }

    private static void cbsWorker(Rankings rankings, String url, String pos) throws IOException {
        List<String> td = JsoupUtils.handleLists(url,
                "tbody.rankings-body tr.ranking-tbl-data-inner-tr td");
        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (!GeneralUtils.isInteger(td.get(i))) {
                min = i;
                break;
            }
        }
        Log.d("CBS", min + " starting");
        for (int i = min; i < td.size(); i++) {
            Log.d("CBS", i + ": " + td.get(i));
        }

        for (int i = min; i < td.size(); i+=2) {
            String[] playerData = td.get(i).split(" ");
            String value = playerData[playerData.length - 1];
            int aucValue = Integer.parseInt(value.replace("$", "")) * 2;
            String nameAndTeam = td.get(i).substring(0, td.get(i).lastIndexOf(" "));
            String name = nameAndTeam.substring(0, nameAndTeam.lastIndexOf(" "));
            String team = nameAndTeam.substring(nameAndTeam.lastIndexOf(" "));
            if (name.split(" ").length == 1) {
                name += " D/ST";
            }
            Log.d("CBS", name + ": " + team + ", " + pos + " - " + aucValue);
        }
    }
}
