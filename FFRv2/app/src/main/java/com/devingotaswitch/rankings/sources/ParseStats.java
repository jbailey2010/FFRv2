package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.amazonaws.util.StringUtils;
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

    private static final String TAG = "ParseStats";

    public static void setStats(Rankings rankings)
            throws IOException {
        // Fetch the stats
        Map<String, String> qbs = parseQBStats();
        Map<String, String> rbs = parseRBStats();
        Map<String, String> wrs = parseWRStats();
        Map<String, String> tes = parseTEStats();
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            switch (player.getPosition()) {
                case Constants.QB:
                    applyStats(qbs, player);
                    break;
                case Constants.RB:
                    applyStats(rbs, player);
                    break;
                case Constants.WR:
                    applyStats(wrs, player);
                    break;
                case Constants.TE:
                    applyStats(tes, player);
                    break;
            }
        }

        // Now, do a second pass, only looking at players who have no stats
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (StringUtils.isBlank(player.getStats())) {
                switch (player.getPosition()) {
                    case Constants.QB:
                        applyStatsChangedTeam(qbs, player);
                        break;
                    case Constants.RB:
                        applyStatsChangedTeam(rbs, player);
                        break;
                    case Constants.WR:
                        applyStatsChangedTeam(wrs, player);
                        break;
                    case Constants.TE:
                        applyStatsChangedTeam(tes, player);
                        break;
                }
            }
        }
    }

    private static void applyStatsChangedTeam(Map<String, String> statsMap, Player player) {
        String oneInitial = getNameFirstInitial(player);
        String twoInitials = getNameFirstTwoLetters(player);
        for (String key : statsMap.keySet()) {
            if ((key.startsWith(oneInitial) || key.startsWith(twoInitials))
                    && key.endsWith(player.getPosition())) {
                player.setStats(statsMap.get(key));
            }
        }
    }

    private static void applyStats(Map<String, String> statsMap, Player player) {
        if (statsMap.containsKey(getUniqueIdFirstInitial(player))) {
            player.setStats(statsMap.get(getUniqueIdFirstInitial(player)));
        } else if (statsMap.containsKey(getUniqueIdFirstTwoLetters(player))) {
            player.setStats(statsMap.get(getUniqueIdFirstTwoLetters(player)));
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
        return getPlayerIdKey(getNameFirstInitial(player), player.getTeamName(), player.getPosition());
    }

    private static String getNameFirstInitial(Player player) {
        String[] name = player.getName().split(" ");
        if (name[0].contains(".")) {
            return player.getName().replaceAll(" ", "");
        } else {
            return name[0].charAt(0) + "." + name[1];
        }
    }

    private static String getUniqueIdFirstTwoLetters(Player player) {
        return getPlayerIdKey(getNameFirstTwoLetters(player), player.getTeamName(), player.getPosition());
    }

    private static String getNameFirstTwoLetters(Player player) {
        String[] name = player.getName().split(" ");
        return name[0].substring(0,2) + "." + name[1];
    }

    private static Map<String, String> parseQBStats() throws IOException {
        List<String> rows = JsoupUtils.parseURLWithUA(
                "http://www.footballoutsiders.com/stats/qb/" + Constants.LAST_YEAR_KEY, "tr");
        Map<String, String> qbPlayers = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String[] player = rows.get(i).split(" ");
            StringBuilder data = new StringBuilder(500);
            // Name
            String name = ParsingUtils.normalizeNames(player[0]);
            String team = ParsingUtils.normalizeTeams(player[1]);
            if (name.equals("AJ") && team.equals("McCarron")) {
                // handle the bullshit convention they broke with AJ McCarron. If this is resolved, remove this code.
                continue;
            }
            if (player[0].equals("Player")
                    || (!qbPlayers.containsKey(getPlayerIdKey(name, team, Constants.QB)) && player.length < 17)) {
                continue;
            }
            Log.d("JEFF", "Looking at stats for " + name + " of " + team);
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
                String attempts = player[player.length - 10];
                Integer attemptsNum = Integer.parseInt(attempts);
                String completionPercentage = player[player.length - 3];
                Double completionInt = Double.parseDouble(completionPercentage.substring(0, completionPercentage.length() - 1));
                Double completionRate = completionInt / 100.0;
                Integer completionsIsh = (int) Math.round(attemptsNum * completionRate);
                data.append("Pass Attempts: ").append(attempts).append(Constants.LINE_BREAK);
                data.append("Completions: ").append(completionsIsh).append(Constants.LINE_BREAK);
                data.append("Completion Percentage: ").append(completionPercentage).append(Constants.LINE_BREAK);
                data.append("Yards: ").append(player[player.length - 9].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Adjusted Yards: ").append(player[player.length - 8].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Touchdowns: ").append(player[player.length - 7]).append(Constants.LINE_BREAK);
                data.append("Interceptions: ").append(player[player.length - 4]).append(Constants.LINE_BREAK);
                data.append("Fumbles: ").append(player[player.length - 5])
                        .append(Constants.LINE_BREAK);
                data.append("DPI: ").append(player[player.length - 2]).append(Constants.LINE_BREAK);
                data.append("ALEX: ").append(player[player.length - 1]).append(Constants.LINE_BREAK);
                if (player.length > 17) {
                    for (int j = 0; j < player.length; j++) {
                        Log.d("JEFF", i + ": " + player[j]);
                    }
                    data.append("DYAR: ")
                            .append(player[player.length - 19]).append(Constants.LINE_BREAK);
                    data.append("DVOA: ")
                            .append(player[player.length - 15]);
                } else {
                    data.append("DYAR: ")
                            .append(player[player.length - 15]).append(Constants.LINE_BREAK);
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
                "http://www.footballoutsiders.com/stats/rb/" + Constants.LAST_YEAR_KEY, "tr");
        Map<String, String> rbPlayers = new HashMap<>();
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
            if (rbPlayers.containsKey(getPlayerIdKey(name, team, Constants.RB))) {
                String catches = player[player.length - 6];
                String yards = player[player.length - 5];
                String effectiveYards = player[player.length - 4];
                String tds = player[player.length - 3];
                String catchRate = player[player.length - 2];
                Double receptionInt = Double.parseDouble(
                        catchRate.substring(0, catchRate.length() - 1));
                Double receptionRate = receptionInt / 100.0;
                Integer receptionsIsh = (int) Math.round(Integer.parseInt(catches) * receptionRate);
                String normal = rbPlayers.get(getPlayerIdKey(name, team, Constants.RB)) + Constants.LINE_BREAK +
                        "Targets: " + catches + Constants.LINE_BREAK +
                        "Receptions: " + receptionsIsh + Constants.LINE_BREAK +
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
                    data.append("DYAR: ")
                            .append(player[player.length - 13 + incr]).append(Constants.LINE_BREAK);
                    data.append("DVOA: ")
                            .append(player[player.length - 9 + incr]);
                } else {
                    data.append("DYAR: ")
                            .append(player[player.length - 10 + incr]).append(Constants.LINE_BREAK);
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
                "http://www.footballoutsiders.com/stats/wr/" + Constants.LAST_YEAR_KEY, "tr");
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
                String catchRateStr = player[player.length - 3];
                Double catchRate = Double.parseDouble(catchRateStr.substring(0, catchRateStr.length() - 1));
                Integer targets = Integer.parseInt(player[player.length - 7]);
                Integer catchesIsh = (int) Math.round(targets * (catchRate / 100.0));
                data.append("Targets: ").append(targets).append(Constants.LINE_BREAK);
                data.append("Receptions: ").append(catchesIsh).append(Constants.LINE_BREAK);
                data.append("Catch Rate: ").append(catchRateStr).append(Constants.LINE_BREAK);
                data.append("Yards: ").append(player[player.length - 6]).append(Constants.LINE_BREAK);
                data.append("Adjusted Yards: ").append(player[player.length - 5].replace(",", "")).append(Constants.LINE_BREAK);
                data.append("Touchdowns: ").append(player[player.length - 4]).append(Constants.LINE_BREAK);
                data.append("Fumbles: ")
                        .append(player[player.length - 2])
                        .append(Constants.LINE_BREAK);
                data.append("DPI: ").append(player[player.length - 1]).append(Constants.LINE_BREAK);
                if (player.length > 13) {
                    data.append("DYAR: ")
                            .append(player[player.length - 14]).append(Constants.LINE_BREAK);
                    data.append("DVOA: ")
                            .append(player[player.length - 10]);
                } else {
                    data.append("DYAR: ")
                            .append(player[player.length - 11]).append(Constants.LINE_BREAK);
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
                "http://www.footballoutsiders.com/stats/te/" + Constants.LAST_YEAR_KEY, "tr");
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
            String catchRateStr = player[player.length - 3];
            Double catchRate = Double.parseDouble(catchRateStr.substring(0, catchRateStr.length() - 1));
            Integer targets = Integer.parseInt(player[player.length - 7]);
            Integer catchesIsh = (int) Math.round(targets * (catchRate / 100.0));
            data.append("Targets: ").append(targets).append(Constants.LINE_BREAK);
            data.append("Receptions: ").append(catchesIsh).append(Constants.LINE_BREAK);
            data.append("Catch Rate: ").append(catchRateStr).append(Constants.LINE_BREAK);
            data.append("Yards: ").append(player[player.length - 6]).append(Constants.LINE_BREAK);
            data.append("Adjusted Yards: ").append(player[player.length - 5].replace(",", "")).append(Constants.LINE_BREAK);
            data.append("Touchdowns: ").append(player[player.length - 4]).append(Constants.LINE_BREAK);
            data.append("Fumbles: ")
                    .append(player[player.length - 2])
                    .append(Constants.LINE_BREAK);
            data.append("DPI: ").append(player[player.length - 1]).append(Constants.LINE_BREAK);
            if (player.length > 13) {
                data.append("DYAR: ")
                        .append(player[player.length - 14]).append(Constants.LINE_BREAK);
                data.append("DVOA: ")
                        .append(player[player.length - 10]);
            } else {
                data.append("DYAR: ")
                        .append(player[player.length - 11]).append(Constants.LINE_BREAK);
                data.append("DVOA: ")
                        .append(player[player.length - 9]);
            }
            tePlayers.put(getPlayerIdKey(name, team, Constants.TE), data.toString());
        }
        return tePlayers;
    }
}
