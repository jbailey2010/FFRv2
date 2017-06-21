package com.devingotaswitch.rankings.sources;

import android.content.Context;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseECR {
    public static void parseECRWrapper(Rankings rankings, Context cont)
            throws IOException {
        Map<String, Double> ecr = new HashMap<>();
        Map<String, Double> risk = new HashMap<>();
        Map<String, Double> adp = new HashMap<>();
        String url = "http://www.fantasypros.com/nfl/rankings/consensus-cheatsheets.php";
        String adpUrl = "http://www.fantasypros.com/nfl/adp/overall.php";
        if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
            url = "http://www.fantasypros.com/nfl/rankings/ppr-cheatsheets.php";
            adpUrl = "http://www.fantasypros.com/nfl/adp/ppr-overall.php";
        }
        parseECRWorker(url, rankings, ecr, risk);
        parseADPWorker(rankings, adp, adpUrl);

        for (String playerId : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(playerId);
            if (ecr.containsKey(playerId)) {
                player.setEcr(ecr.get(playerId));
                player.setRisk(risk.get(playerId));
            }
            if (adp.containsKey(playerId)) {
                player.setAdp(adp.get(playerId));
            }
        }
    }

    private static void parseECRWorker(String url, Rankings rankings,
                                      Map<String, Double> ecr, Map<String, Double> risk) throws IOException {
        List<String> td = JsoupUtils.handleLists(url, "table.player-table tbody tr td");
        int min = 0;
        int loopIterAdp = 10;
        if (url.contains("ppr")) {
            loopIterAdp = 9;
        }
        for (int i = 0; i < td.size(); i++) {
            if (GeneralUtils.isInteger(td.get(i))) {
                min = i + 1;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 10) {
            String check = td.get(i);
            while(check.split(" ").length == 1 || td.get(i).contains("Tier ") || td.get(i).contains("EDIT")) {
                check = td.get(++i);
            }
            if (i + 10 >= td.size()) {
                break;
            }
            String filteredName = td.get(i).split(
                    " \\(")[0].split(", ")[0];
            String withoutTeam = filteredName.substring(0, filteredName.lastIndexOf(" "));
            String name = ParseRankings
                    .fixNames(ParseRankings.fixDefenses(withoutTeam));
            double ecrVal = Double.parseDouble(td.get(i + 5));
            double riskVal = Double.parseDouble(td.get(i + 6));
            String posInd = td.get(i + 1).replaceAll("(\\d+,\\d+)|\\d+", "")
                    .replaceAll("DST", Constants.DST);
            ecr.put(name + posInd, ecrVal);
            risk.put(name + posInd, riskVal);
        }
    }

    private static void parseADPWorker(Rankings rankings,
                                      Map<String, Double> adp, String adpUrl)
            throws IOException {
        List<String> td = JsoupUtils.handleLists(adpUrl, "table.player-table tbody tr td");
        int min = 0;
        try {
            for (int i = 0; i < td.size(); i++) {

                if (td.get(i + 1).contains(Constants.QB)
                        || td.get(i + 1).contains(Constants.RB)
                        || td.get(i + 1).contains(Constants.WR)
                        || td.get(i + 1).contains(Constants.TE)) {
                    min = i;
                    break;
                }
            }
            for (int i = min; i < td.size(); i += 9) {
                if (td.get(i).split(" ").length == 1) {
                    i++;
                    if (i > td.size()) {
                        break;
                    }
                }
                String filteredName = td.get(i).split(
                        " \\(")[0].split(", ")[0];
                String withoutTeam = filteredName.substring(0, filteredName.lastIndexOf(" "));
                String name = ParseRankings
                        .fixNames(ParseRankings.fixDefenses(withoutTeam));
                if (i + 6 >= td.size()) {
                    break;
                }
                String adpStr = td.get(i + 7);
                String posInd = td.get(i + 1)
                        .replaceAll("(\\d+,\\d+)|\\d+", "")
                        .replaceAll("DST", Constants.DST);
                adp.put(name + posInd, adpStr);
            }
        } catch (ArrayIndexOutOfBoundsException notUp) {
            return;
        }
    }
}
