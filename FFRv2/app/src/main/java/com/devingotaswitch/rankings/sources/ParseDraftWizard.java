package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.List;

public class ParseDraftWizard {

    public static void parseRanksWrapper(Rankings rankings)
            throws IOException {
        String type = "STD";
        if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
            type = "PPR";
        }
        String url = "http://draftwizard.fantasypros.com/editor/createFromProjections.jsp?sport=nfl&scoringSystem="
                + type + "&showAuction=Y";
        url += "&teams=" + rankings.getLeagueSettings().getTeamCount();
        url += "&QB=" + rankings.getLeagueSettings().getRosterSettings().getQbCount();
        url += "&WR=" + rankings.getLeagueSettings().getRosterSettings().getWrCount();
        url += "&RB=" + rankings.getLeagueSettings().getRosterSettings().getRbCount();
        url += "&TE=" + rankings.getLeagueSettings().getRosterSettings().getTeCount();
        url += "&DST=" + rankings.getLeagueSettings().getRosterSettings().getDstCount();
        url += "&K=" + rankings.getLeagueSettings().getRosterSettings().getkCount();
        if (rankings.getLeagueSettings().getRosterSettings().getFlex() != null) {
            url += "&WR/RB=" + rankings.getLeagueSettings().getRosterSettings().getFlex().getRbwrCount();
            url += "&WR/RB/TE=" + rankings.getLeagueSettings().getRosterSettings().getFlex().getRbwrteCount();
            url += "&RB/TE=" + rankings.getLeagueSettings().getRosterSettings().getFlex().getRbteCount();
            url += "&WR/TE=" + rankings.getLeagueSettings().getRosterSettings().getFlex().getWrteCount();
            url += "&QB/WR/RB/TE=" + rankings.getLeagueSettings().getRosterSettings().getFlex().getQbrbwrteCount();
        }
        parseRanksWorker(rankings, url);
    }

    public static void parseRanksWorker(Rankings rankings, String url)
            throws IOException {
        List<String> td = JsoupUtils.handleLists(url,
                "table#OverallTable td");
        Log.d("FP", url);
        int startingIndex = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).contains(" - ") && td.get(i).split(" ").length > 3) {
                startingIndex = i;
                break;
            }
        }
        for (int i = startingIndex; i < td.size(); i += 5) {
            double aucVal = Double.parseDouble(td.get(i + 2).substring(1,
                    td.get(i + 2).length()));
            String playerName = td.get(i).split(" \\(")[0];
            String teamPos = td.get(i).split(" \\(")[1];
            String team = teamPos.split(" - ")[0];
            String pos = teamPos.split(" - ")[1].split("\\)")[0];
            Player player = ParsingUtils.getPlayerFromRankings(playerName, team, pos, aucVal);

            // Double count it because math
            rankings.processNewPlayer(player);
            rankings.processNewPlayer(player);
        }
    }
}
