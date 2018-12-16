package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.projections.PlayerProjection;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseProjections {
    public static void projPointsWrapper(Rankings rankings)
            throws IOException {
        Map<String, Double> points = new HashMap<>();
        qbProj("http://www.fantasypros.com/nfl/projections/qb.php?year=" + Constants.YEAR_KEY + "&week=draft",
                points, rankings);
        rbProj("http://www.fantasypros.com/nfl/projections/rb.php?year=" + Constants.YEAR_KEY + "&week=draft",
                points, rankings);
        wrProj("http://www.fantasypros.com/nfl/projections/wr.php?year=" + Constants.YEAR_KEY + "&week=draft",
                points, rankings);
        teProj("http://www.fantasypros.com/nfl/projections/te.php?year=" + Constants.YEAR_KEY + "&week=draft",
                points, rankings);
        defProj("http://www.fantasypros.com/nfl/projections/dst.php?year=" + Constants.YEAR_KEY + "&week=draft",
                points);
        kProj("http://www.fantasypros.com/nfl/projections/k.php?year=" + Constants.YEAR_KEY + "&week=draft",
                points);

        for (String playerId : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(playerId);
            if (points.containsKey(playerId)) {
                player.setProjection(points.get(playerId));
            } else {
                player.setProjection(0.0);
            }
        }
    }

    private static void qbProj(String url, Map<String, Double> points,
                               Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "table.table-bordered tbody tr td");

        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 11) {
            double proj = 0;
            StringBuilder name = new StringBuilder();
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name.append(nameSet[j]).append(" ");
            }
            name = new StringBuilder(ParsingUtils.normalizeNames(name.substring(0, name.length() - 1)));
            String team = ParsingUtils.normalizeTeams(nameSet[nameSet.length - 1]);
            double yards = Double.parseDouble(td.get(i + 3).replace(",", ""));
            double passTd = Double.parseDouble(td.get(i + 4));
            double ints = Double.parseDouble(td.get(i + 5));
            double rushYards = Double.parseDouble(td.get(i + 7));
            double rushTD = Double.parseDouble(td.get(i + 8));
            double fumbles = Double.parseDouble(td.get(i + 9));
            PlayerProjection projection = new PlayerProjection(yards, passTd, rushYards, rushTD, 0.0, 0.0, 0.0,
                    fumbles, ints, 0.0, 0.0);
            proj = projection.getFormattedProjectedPoints(rankings.getLeagueSettings().getScoringSettings());
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.QB, proj);
        }
    }

    private static void rbProj(String url, Map<String, Double> points,
                               Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "table.table-bordered tbody tr td");

        int min = 0;

        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 9) {
            double proj = 0;
            StringBuilder name = new StringBuilder();
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name.append(nameSet[j]).append(" ");
            }
            name = new StringBuilder(ParsingUtils.normalizeNames(name.substring(0, name.length() - 1)));
            String team = ParsingUtils.normalizeTeams(nameSet[nameSet.length - 1]);
            double rushYards = Double.parseDouble(td.get(i + 2)
                    .replace(",", ""));
            double rushTD = Double.parseDouble(td.get(i + 3));
            double catches = Double.parseDouble(td.get(i + 4));
            double recYards = Double
                    .parseDouble(td.get(i + 5).replace(",", ""));
            double recTD = Double.parseDouble(td.get(i + 6));
            double fumbles = Double.parseDouble(td.get(i + 7));
            PlayerProjection projection = new PlayerProjection(0.0, 0.0, rushYards, rushTD,
                    recYards, recTD, catches, fumbles, 0.0, 0.0, 0.0);
            proj = projection.getFormattedProjectedPoints(rankings.getLeagueSettings().getScoringSettings());
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.RB, proj);
        }
    }

    private static void wrProj(String url, Map<String, Double> points,
                               Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "table.table-bordered tbody tr td");

        int min = 0;

        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 9) {
            double proj = 0;
            StringBuilder name = new StringBuilder();
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name.append(nameSet[j]).append(" ");
            }
            name = new StringBuilder(ParsingUtils.normalizeNames(name.substring(0, name.length() - 1)));
            String team = ParsingUtils.normalizeTeams(nameSet[nameSet.length - 1]);
            double rushYards = Double.parseDouble(td.get(i + 5)
                    .replace(",", ""));
            double rushTD = Double.parseDouble(td.get(i + 6));
            double catches = Double.parseDouble(td.get(i + 1));
            double recYards = Double
                    .parseDouble(td.get(i + 2).replace(",", ""));
            double recTD = Double.parseDouble(td.get(i + 3));
            double fumbles = Double.parseDouble(td.get(i + 7));
            PlayerProjection projection = new PlayerProjection(0.0, 0.0, rushYards, rushTD,
                    recYards, recTD, catches, fumbles, 0.0, 0.0, 0.0);
            proj = projection.getFormattedProjectedPoints(rankings.getLeagueSettings().getScoringSettings());
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.WR, proj);
        }
    }

    private static void teProj(String url, Map<String, Double> points,
                               Rankings rankings) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "table.table-bordered tbody tr td");

        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 6) {
            double proj = 0;
            StringBuilder name = new StringBuilder();
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name.append(nameSet[j]).append(" ");
            }
            name = new StringBuilder(ParsingUtils.normalizeNames(name.substring(0, name.length() - 1)));
            String team = ParsingUtils.normalizeTeams(nameSet[nameSet.length - 1]);
            double catches = Double.parseDouble(td.get(i + 1).replace(",", ""));
            double recTD = Double.parseDouble(td.get(i + 3));
            double recYards = Double
                    .parseDouble(td.get(i + 2).replace(",", ""));
            double fumbles = Double.parseDouble(td.get(i + 4));
            PlayerProjection projection = new PlayerProjection(0.0, 0.0, 0.0, 0.0,
                    recYards, recTD, catches, fumbles, 0.0, 0.0, 0.0);
            proj = projection.getFormattedProjectedPoints(rankings.getLeagueSettings().getScoringSettings());
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.TE, proj);
        }
    }

    private static void defProj(String url, Map<String, Double> points) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "table.table-bordered tbody tr td");

        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 2 && GeneralUtils.isDouble(td.get(i+1))) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 10) {
            StringBuilder name = new StringBuilder();
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name.append(nameSet[j]).append(" ");
            }
            name = new StringBuilder(name.substring(0, name.length() - 1));
            String team = ParsingUtils.normalizeTeams(name.toString());
            name = new StringBuilder(ParsingUtils.normalizeDefenses(name.toString()));
            Double proj = Double.parseDouble(td.get(i+9));
            PlayerProjection projection = new PlayerProjection(0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, proj, 0.0);
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.DST, proj);
        }
    }

    private static void kProj(String url, Map<String, Double> points) throws IOException {
        List<String> td = JsoupUtils.parseURLWithUA(url, "table.table-bordered tbody tr td");

        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 5) {
            StringBuilder name = new StringBuilder();
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name.append(nameSet[j]).append(" ");
            }
            name = new StringBuilder(ParsingUtils.normalizeNames(name.substring(0, name.length() - 1)));
            String team = ParsingUtils.normalizeTeams(nameSet[nameSet.length - 1]);
            double proj = Double.parseDouble(td.get(i + 4));
            PlayerProjection projection = new PlayerProjection(0.0, 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, proj);
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + Constants.K, proj);
        }
    }
}
