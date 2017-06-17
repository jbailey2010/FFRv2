package com.devingotaswitch.rankings;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;

import java.io.IOException;
import java.util.List;

public class ParseWalterFootball {
    public static void wfRankings(Rankings rankings)
            throws IOException {
        RosterSettings r = rankings.getLeagueSettings().getRosterSettings();
        ScoringSettings s = rankings.getLeagueSettings().getScoringSettings();
        if (r.getQbCount() > 1 || (r.getFlex() != null && r.getFlex().getQbrbwrteCount() > 0)) {
            wfRankingsHelper(rankings,
                    "http://walterfootball.com/fantasycheatsheet/2017/twoqb");
        } else if (s.getReceptions() > 0) {
            wfRankingsHelper(rankings,
                    "http://walterfootball.com/fantasycheatsheet/2017/ppr");
        } else {
            wfRankingsHelper(rankings,
                    "http://walterfootball.com/fantasycheatsheet/2017/traditional");
        }
    }

    private static void wfRankingsHelper(Rankings rankings, String url)
            throws IOException, ArrayIndexOutOfBoundsException {
        List<String> perPlayer = JsoupUtils.handleLists(url,
                "ol.fantasy-board div li");
        String[][] all = new String[perPlayer.size()][];
        for (int i = 0; i < perPlayer.size(); i++) {
            all[i] = perPlayer.get(i).split(", ");
            String playerName = all[i][0];
            String pos = "";
            double val = 0.0;
            if (!perPlayer.get(i).contains("DEF")) {
                pos = all[i][1];
            } else {
                playerName += " D/ST";
                pos = Constants.DST;
            }
            val = Double
                    .parseDouble(perPlayer.get(i).split("\\$")[1].split(" ")[0]);
            String team = all[i][2].split("\\. ")[0];
            Player player = new Player();
            player.setName(playerName);
            player.setTeamName(team);
            player.setPosition(pos);
            player.handleNewValue(val);
            rankings.processNewPlayer(player);
        }
    }
}
