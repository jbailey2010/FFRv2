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

public class ParseFantasyPros {
    public static void parseECRWrapper(Rankings rankings) throws IOException {
        Map<String, Double> ecr = new HashMap<>();
        Map<String, Double> risk = new HashMap<>();
        String url = "http://www.fantasypros.com/nfl/rankings/consensus-cheatsheets.php";
        if (rankings.getLeagueSettings().getScoringSettings().getReceptions() >= 1.0) {
            url = "http://www.fantasypros.com/nfl/rankings/ppr-cheatsheets.php";
        } else if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
            url =  "https://www.fantasypros.com/nfl/rankings/half-point-ppr-cheatsheets.php";
        }
        parseECRWorker(url, ecr, risk);

        for (String playerId : rankings.getPlayers().keySet()) {
            if (ecr.containsKey(playerId)) {
                Player player = rankings.getPlayer(playerId);
                player.setEcr(ecr.get(playerId));
                player.setRisk(risk.get(playerId));
            }
        }
    }

    public static void parseADPWrapper(Rankings rankings) throws IOException {
        Map<String, Double> adp = new HashMap<>();
        String adpUrl = "http://www.fantasypros.com/nfl/adp/overall.php";
        if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
            adpUrl = "http://www.fantasypros.com/nfl/adp/ppr-overall.php";
        }
        parseADPWorker(adp, adpUrl);

        for (String playerId : rankings.getPlayers().keySet()) {
            if (adp.containsKey(playerId)) {
                Player player = rankings.getPlayer(playerId);
                player.setAdp(adp.get(playerId));
            }
        }
    }

    public static void parseDynastyWrapper(Rankings rankings) throws IOException {
        Map<String, Double> dynasty = new HashMap<>();
        String url = "https://www.fantasypros.com/nfl/rankings/dynasty-overall.php";
        parseDynastyWorker(url, dynasty);

        for (String playerId : rankings.getPlayers().keySet()) {
            if (dynasty.containsKey(playerId)) {
                Player player = rankings.getPlayer(playerId);
                player.setDynastyRank(dynasty.get(playerId));
            }
        }
    }

    public static void parseRookieWrapper(Rankings rankings) throws IOException {
        Map<String, Double> rookie = new HashMap<>();
        String url = "https://www.fantasypros.com/nfl/rankings/rookies.php";
        parseRookieWorker(url, rookie);

        for (String playerId : rankings.getPlayers().keySet()) {
            if (rookie.containsKey(playerId)) {
                Player player = rankings.getPlayer(playerId);
                player.setRookieRank(rookie.get(playerId));
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
            }
            while (td.get(i).split(" ").length < 3 && i < td.size()) {
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
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    private static void parseDynastyWorker(String url,
                                            Map<String, Double> dynasty) throws IOException {
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
            }
            while (td.get(i).split(" ").length < 3 && i < td.size()) {
                i++;
            }
            String fullName = names.get(playerCount++);
            String filteredName = td.get(i).split(
                    " \\(")[0].split(", ")[0];
            String team = ParsingUtils.normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim());
            String name = ParsingUtils
                    .normalizeNames(ParsingUtils.normalizeDefenses(fullName));
            double dynastyVal = Double.parseDouble(td.get(i + 5));
            String posInd = td.get(i + 1).replaceAll("(\\d+,\\d+)|\\d+", "")
                    .replaceAll("DST", Constants.DST);
            if (Constants.DST.equals(posInd)) {
                team = ParsingUtils.normalizeTeams(fullName);
            }
            dynasty.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd, dynastyVal);
        }
    }

    private static void parseRookieWorker(String url,
                                           Map<String, Double> rookie) throws IOException {
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
            }
            while (td.get(i).split(" ").length < 3 && i < td.size()) {
                i++;
            }
            String fullName = names.get(playerCount++);
            String filteredName = td.get(i).split(
                    " \\(")[0].split(", ")[0];
            String team = ParsingUtils.normalizeTeams(filteredName.substring(filteredName.lastIndexOf(" ")).trim());
            String name = ParsingUtils
                    .normalizeNames(ParsingUtils.normalizeDefenses(fullName));
            double rookieVal = Double.parseDouble(td.get(i + 5));
            String posInd = td.get(i + 1).replaceAll("(\\d+,\\d+)|\\d+", "")
                    .replaceAll("DST", Constants.DST);
            if (Constants.DST.equals(posInd)) {
                team = ParsingUtils.normalizeTeams(fullName);
            }
            rookie.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + posInd, rookieVal);
        }
    }
}