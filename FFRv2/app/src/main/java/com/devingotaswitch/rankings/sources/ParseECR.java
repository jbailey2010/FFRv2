package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseECR {
    public static void parseECRWrapper(Rankings rankings)
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
        parseECRWorker(url, ecr, risk);
        parseADPWorker(adp, adpUrl);

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

    private static void parseECRWorker(String url,
                                      Map<String, Double> ecr, Map<String, Double> risk) throws IOException {
        Document doc = JsoupUtils.getDocument(url);
        List<String> names = JsoupUtils.getElemsFromDoc(doc, "table.player-table tbody tr td span.full-name");
        List<String> td = JsoupUtils.getElemsFromDoc(doc, "table.player-table tbody tr td");
        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (GeneralUtils.isInteger(td.get(i))) {
                min = i + 2;
                break;
            }
        }
        int playerCount = 0;
        for (int i = min; i < td.size(); i += 9) {
            if (i + 9 >= td.size()) {
                break;
            } else if ("".equals(td.get(i))) {
                i++;
            }
            String fullName = names.get(playerCount++);
            String filteredName = td.get(i).split(
                    " \\(")[0].split(", ")[0];
            String team = ParsingUtils.normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim());
            String name = ParsingUtils
                    .normalizeNames(ParsingUtils.normalizeDefenses(fullName));
            double ecrVal = Double.parseDouble(td.get(i + 5));
            double riskVal = Double.parseDouble(td.get(i + 6));
            String posInd = td.get(i + 1).replaceAll("(\\d+,\\d+)|\\d+", "")
                    .replaceAll("DST", Constants.DST);
            if (Constants.DST.equals(posInd)) {
                team = ParsingUtils.normalizeTeams(fullName);
            }
            ecr.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd, ecrVal);
            risk.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd, riskVal);
            if (td.get(i + 7).contains("Tier")) {
                i += 2;
            }
        }
    }

    private static void parseADPWorker(Map<String, Double> adp, String adpUrl)
            throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(adpUrl, "table.player-table tbody tr td");
        int min = 0;
        try {
            for (int i = 0; i < td.size(); i++) {

                if (GeneralUtils.isInteger(td.get(i))) {
                    min = i;
                    break;
                }
            }
            for (int i = min; i < td.size(); i += 10) {
                if (i + 10 >= td.size()) {
                    break;
                } else if ("".equals(td.get(i))) {
                    i++;
                }
                String filteredName = td.get(i + 1).split(
                        " \\(")[0].split(", ")[0];
                String team = ParsingUtils.normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim());
                String withoutTeam = filteredName.substring(0, filteredName.lastIndexOf(" "));
                String name = ParsingUtils.normalizeNames(ParsingUtils.normalizeDefenses(withoutTeam));
                if (i + 6 >= td.size()) {
                    break;
                }
                Double adpStr = Double.parseDouble(td.get(i + 9));
                String posInd = td.get(i + 2)
                        .replaceAll("(\\d+,\\d+)|\\d+", "")
                        .replaceAll("DST", Constants.DST);
                if (Constants.DST.equals(posInd)) {
                    team = ParsingUtils.normalizeTeams(withoutTeam);
                }
                adp.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd, adpStr);
            }
        } catch (ArrayIndexOutOfBoundsException notUp) {
        }
    }
}
