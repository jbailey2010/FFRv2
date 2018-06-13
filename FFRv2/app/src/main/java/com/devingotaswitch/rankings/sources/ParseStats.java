package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseStats {

    public static void setStats(Rankings rankings)
            throws IOException {
        // Fetch the stats
        Map<String, String> qbs = parseQBStats();
        Map<String, String> rbs = parseRBStats();
        Map<String, String> wrs = parseWRStats();
        Map<String, String> tes = parseTEStats();
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (!player.getPosition().equals(Constants.K)
                    && !player.getPosition().equals(Constants.DST)) {
                switch (player.getPosition()) {
                    case Constants.QB:
                        if (qbs.containsKey(getUniqueIdFirstInitial(player))) {
                            player.setStats(qbs.get(getUniqueIdFirstInitial(player)));
                        } else if (qbs.containsKey(getUniqueIdFirstTwoLetters(player))) {
                            player.setStats(qbs.get(getUniqueIdFirstTwoLetters(player)));
                        }
                        break;
                    case Constants.RB:
                        if (rbs.containsKey(getUniqueIdFirstInitial(player))) {
                            player.setStats(rbs.get(getUniqueIdFirstInitial(player)));
                        } else if (rbs.containsKey(getUniqueIdFirstTwoLetters(player))) {
                            player.setStats(rbs.get(getUniqueIdFirstTwoLetters(player)));
                        }
                        break;
                    case Constants.WR:
                        if (wrs.containsKey(getUniqueIdFirstInitial(player))) {
                            player.setStats(wrs.get(getUniqueIdFirstInitial(player)));
                        } else if (wrs.containsKey(getUniqueIdFirstTwoLetters(player))) {
                            player.setStats(wrs.get(getUniqueIdFirstTwoLetters(player)));
                        }
                        break;
                    case Constants.TE:
                        if (tes.containsKey(getUniqueIdFirstInitial(player))) {
                            player.setStats(tes.get(getUniqueIdFirstInitial(player)));
                        } else if (tes.containsKey(getUniqueIdFirstTwoLetters(player))) {
                            player.setStats(tes.get(getUniqueIdFirstTwoLetters(player)));
                        }
                        break;
                }
            }
        }
    }

    private static String getPlayerIdKey(String name, String team, String pos) {
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos;
    }

    private static String getUniqueIdFirstInitial(Player player) {
        String[] name = player.getName().split(" ");
        String testName;
        if (name[0].contains(".")) {
            testName = player.getName().replaceAll(" ", "");
        } else {
            testName = name[0].charAt(0) + "." + name[1];
        }
        return testName +
                Constants.PLAYER_ID_DELIMITER +
                player.getTeamName() +
                Constants.PLAYER_ID_DELIMITER +
                player.getPosition();
    }

    private static String getUniqueIdFirstTwoLetters(Player player) {
        String[] name = player.getName().split(" ");
        String testName = name[0].substring(0,2) + "." + name[1];
        return testName +
                Constants.PLAYER_ID_DELIMITER +
                player.getTeamName() +
                Constants.PLAYER_ID_DELIMITER +
                player.getPosition();
    }

    private static Map<String, String> parseQBStats() throws IOException {
        List<String> rows = JsoupUtils.parseURLWithUA(
                "http://www.footballoutsiders.com/stats/qb", "tr");
        Map<String, String> qbPlayers = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String[] player = rows.get(i).split(" ");
            StringBuilder data = new StringBuilder(500);
            // Name
            String name = ParsingUtils.normalizeNames(player[0]);
            String team = ParsingUtils.normalizeTeams(player[1]);
            if (player[0].equals("Player")
                    || (!qbPlayers.containsKey(getPlayerIdKey(name, team, Constants.QB)) && player.length < 17)) {
                continue;
            }
            if (qbPlayers.containsKey(getPlayerIdKey(name, team, Constants.QB))) {
                String yards = player[player.length - 4];
                String effectiveYards = player[player.length - 3];
                String tds = player[player.length - 2];
                String normal = qbPlayers.get(getPlayerIdKey(name, team, Constants.QB));
                normal += "\nRushing Yards: " + yards + Constants.LINE_BREAK;
                normal += "Adjusted Rushing Yards: " + effectiveYards + Constants.LINE_BREAK;
                normal += "Rushing Touchdowns: " + tds;
                qbPlayers.put(getPlayerIdKey(name, team, Constants.QB), normal);
            } else {
                data.append("Pass Attempts: ").append(player[player.length - 9]).append(Constants.LINE_BREAK);
                data.append("Yards: ").append(player[player.length - 8].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Adjusted Yards: ").append(player[player.length - 7].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Touchdowns: ").append(player[player.length - 6]).append(Constants.LINE_BREAK);
                data.append("Completion Percentage: ").append(player[player.length - 2]).append(Constants.LINE_BREAK);
                data.append("Interceptions: ").append(player[player.length - 3]).append(Constants.LINE_BREAK);
                data.append("Fumbles Lost: ").append(player[player.length - 4])
                        .append(Constants.LINE_BREAK);
                if (player.length > 17) {
                    data.append("DVOA: ")
                            .append(player[player.length - 15]);
                } else {
                    data.append("DVOA: ")
                            .append(player[player.length - 13]);
                }
                qbPlayers.put(getPlayerIdKey(name, team, Constants.QB), data.toString());
            }
        }
        return qbPlayers;
    }

    private static Map<String, String> parseRBStats() throws IOException {
        List<String> rows = JsoupUtils.parseURLWithUA(
                "http://www.footballoutsiders.com/stats/rb", "tr");
        Map<String, String> rbPlayers = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String[] player = rows.get(i).split(" ");
            String name = "";
            String team = "";
            StringBuilder data = new StringBuilder(500);
            if (player[0].equals("Player")) {
                continue;
            }
            name = ParsingUtils.normalizeNames(player[0]);
            team = ParsingUtils.normalizeTeams(player[1]);
            if (name.split(" ").length == 3) {
                name = name.split(" ")[0] + " " + name.split(" ")[2];
            }
            if (rbPlayers.containsKey(getPlayerIdKey(name, team, Constants.RB))) {
                String catches = player[player.length - 6];
                String yards = player[player.length - 5];
                String effectiveYards = player[player.length - 4];
                String tds = player[player.length - 3];
                String catchRate = player[player.length - 2];
                String normal = rbPlayers.get(getPlayerIdKey(name, team, Constants.RB)) + "\nTargets: " + catches + Constants.LINE_BREAK +
                        "Catch Rate: " + catchRate + Constants.LINE_BREAK +
                        "Receiving Yards: " + yards + Constants.LINE_BREAK +
                        "Adjusted Receiving Yards: " + effectiveYards + Constants.LINE_BREAK +
                        "Receiving Touchdowns: " + tds;
                rbPlayers.put(getPlayerIdKey(name, team, Constants.RB),
                        normal);
            } else {
                int incr = 1;
                if (player[player.length - 2].contains("%")) {
                    incr = -1;
                }
                data.append("Carries: ").append(player[player.length - 6 + incr]).append(Constants.LINE_BREAK);
                data.append("Yards: ").append(player[player.length - 5 + incr].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Adjusted Yards: ").append(player[player.length - 4 + incr].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Touchdowns: ").append(player[player.length - 3 + incr]).append(Constants.LINE_BREAK);
                data.append("Fumbles: ").append(player[player.length - 2 + incr])
                        .append(Constants.LINE_BREAK);
                if (player.length > 12) {
                    data.append("DVOA: ")
                            .append(player[player.length - 9 + incr]);
                } else {
                    data.append("DVOA: ")
                            .append(player[player.length - 8 + incr]);
                }

                rbPlayers.put(getPlayerIdKey(name, team, Constants.RB), data.toString());
            }
        }
        return rbPlayers;
    }

    private static Map<String, String> parseWRStats() throws IOException {
        List<String> rows = JsoupUtils.parseURLWithUA(
                "http://www.footballoutsiders.com/stats/wr", "tr");
        Map<String, String> wrPlayers = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String[] player = rows.get(i).split(" ");
            StringBuilder data = new StringBuilder(500);
            if (player[0].equals("Player")) {
                continue;
            }
            String name = ParsingUtils.normalizeNames(player[0]);
            String team = ParsingUtils.normalizeTeams(player[1]);
            if (name.split(" ").length == 3) {
                name = name.split(" ")[0] + " " + name.split(" ")[2];
            }
            if (!wrPlayers.containsKey(getPlayerIdKey(name, team, Constants.WR))
                    && player[6].contains("%") && player[8].contains("%")
                    && player.length < 15) {
                continue;
            }
            if (wrPlayers.containsKey(getPlayerIdKey(name, team, Constants.WR))) {
                String rushes = player[player.length - 4];
                String yards = player[player.length - 3];
                String tds = player[player.length - 2];
                String normal = wrPlayers.get(getPlayerIdKey(name, team, Constants.WR));
                normal += "\nRushes: " + rushes + Constants.LINE_BREAK;
                normal += "Rushing Yards: " + yards + Constants.LINE_BREAK;
                normal += "Rushing Touchdowns: " + tds;
                wrPlayers.put(getPlayerIdKey(name, team, Constants.WR), normal);
            } else {
                data.append("Targets: ").append(player[player.length - 7]).append(Constants.LINE_BREAK);
                data.append("Yards: ").append(player[player.length - 6]).append(Constants.LINE_BREAK);
                data.append("Adjusted Yards: ").append(player[player.length - 5].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Touchdowns: ").append(player[player.length - 4]).append(Constants.LINE_BREAK);
                data.append("Catch Rate: ").append(player[player.length - 3]).append(Constants.LINE_BREAK);
                data.append("Fumbles: ")
                        .append(player[player.length - 2])
                        .append(Constants.LINE_BREAK);
                if (player.length > 13) {
                    data.append("DVOA: ")
                            .append(player[player.length - 10]);
                } else {
                    data.append("DVOA: ")
                            .append(player[player.length - 9]);
                }
                wrPlayers.put(getPlayerIdKey(name, team, Constants.WR), data.toString());
            }
        }
        return wrPlayers;
    }

    private static Map<String, String> parseTEStats() throws IOException {
        List<String> rows = JsoupUtils.parseURLWithUA(
                "http://www.footballoutsiders.com/stats/te", "tr");
        Map<String, String> tePlayers = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String[] player = rows.get(i).split(" ");
            StringBuilder data = new StringBuilder(500);
            if (player[0].equals("Player")) {
                continue;
            }
            String name = ParsingUtils.normalizeNames(player[0]);
            String team = ParsingUtils.normalizeTeams(player[1]);
            if (name.split(" ").length == 3) {
                name = name.split(" ")[0] + " " + name.split(" ")[2];
            }
            if (!tePlayers.containsKey(getPlayerIdKey(name, team, Constants.TE))
                    && player[6].contains("%") && player[8].contains("%")
                    && player.length < 15) {
                continue;
            }
            data.append("Targets: ").append(player[player.length - 7]).append(Constants.LINE_BREAK);
            data.append("Yards: ").append(player[player.length - 6]).append(Constants.LINE_BREAK);
            data.append("Adjusted Yards: ").append(player[player.length - 5].replace(",", "")).append(Constants.LINE_BREAK);
            data.append("Touchdowns: ").append(player[player.length - 4]).append(Constants.LINE_BREAK);
            data.append("Catch Rate: ").append(player[player.length - 3]).append(Constants.LINE_BREAK);
            data.append("Fumbles: ")
                    .append(player[player.length - 2])
                    .append(Constants.LINE_BREAK);
            if (player.length > 13) {
                data.append("DVOA: ")
                        .append(player[player.length - 10]);
            } else {
                data.append("DVOA: ")
                        .append(player[player.length - 9]);
            }
            tePlayers.put(getPlayerIdKey(name, team, Constants.TE), data.toString());
        }
        return tePlayers;
    }
}
