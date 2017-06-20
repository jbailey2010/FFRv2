package com.devingotaswitch.rankings.sources;

import android.content.Context;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseProjections {
    public static void projPointsWrapper(Rankings rankings)
            throws IOException {
        // TODO: don't think week=draft is ultimately a must, complicates regular season mode
        Map<String, Double> points = new HashMap<>();
        qbProj("http://www.fantasypros.com/nfl/projections/qb.php?year=2017&week=draft",
                points, rankings, Constants.QB);
        rbProj("http://www.fantasypros.com/nfl/projections/rb.php?year=2017&week=draft",
                points, rankings, Constants.RB);
        wrProj("http://www.fantasypros.com/nfl/projections/wr.php?year=2017&week=draft",
                points, rankings, Constants.WR);
        teProj("http://www.fantasypros.com/nfl/projections/te.php?year=2017&week=draft",
                points, rankings, Constants.TE);
        defProj("http://www.fantasypros.com/nfl/projections/dst.php?year=2017&week=draft",
                points, Constants.DST);
        kProj("http://www.fantasypros.com/nfl/projections/k.php?year=2017&week=draft",
                points, Constants.K);

        for (String playerId : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(playerId);
            if (points.containsKey(playerId)) {
                player.setProjection(points.get(playerId));
            } else {
                player.setProjection(0.0);
            }
        }
    }

    public static void qbProj(String url, Map<String, Double> points,
                              Rankings rankings, String pos) throws IOException {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        List<String> td = JsoupUtils.handleLists(url, "table.table-bordered tbody tr td");

        int min = 0;
        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 11) {
            double proj = 0;
            String name = "";
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name += nameSet[j] + " ";
            }
            name = name.substring(0, name.length() - 1));
            String team = nameSet[nameSet.length - 1];
            double yards = Double.parseDouble(td.get(i + 3).replace(",", ""));
            double passTd = Double.parseDouble(td.get(i + 4));
            double ints = Double.parseDouble(td.get(i + 5));
            double rushYards = Double.parseDouble(td.get(i + 7));
            double rushTD = Double.parseDouble(td.get(i + 8));
            double fumbles = Double.parseDouble(td.get(i + 9));
            proj += (yards / (rankings.getLeagueSettings().getScoringSettings().getPassingYards()));
            proj += ints * rankings.getLeagueSettings().getScoringSettings().getInterceptions();
            proj += passTd * rankings.getLeagueSettings().getScoringSettings().getPassingTds();
            proj += (rushYards / (rankings.getLeagueSettings().getScoringSettings().getRushingYards()));
            proj += rushTD * rankings.getLeagueSettings().getScoringSettings().getRushingTds();
            proj += fumbles * rankings.getLeagueSettings().getScoringSettings().getFumbles();
            proj = Double.parseDouble(df.format(proj));
            points.put(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos, proj);
        }
    }

    public static void rbProj(String url, Map<String, Double> points,
                              Scoring scoring, String pos) throws IOException {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        List<String> td = HandleBasicQueries.handleLists(url, "table.table-bordered tbody tr td");

        int min = 0;
        ParseRankings.handleHashes();

        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 9) {
            double proj = 0;
            String name = "";
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name += nameSet[j] + " ";
            }
            name = ParseRankings.fixNames(name.substring(0, name.length() - 1));
            String team = ParseRankings.fixTeams(nameSet[nameSet.length - 1]);
            double rushYards = Double.parseDouble(td.get(i + 2)
                    .replace(",", ""));
            double rushTD = Double.parseDouble(td.get(i + 3));
            double catches = Double.parseDouble(td.get(i + 4));
            double recYards = Double
                    .parseDouble(td.get(i + 5).replace(",", ""));
            double recTD = Double.parseDouble(td.get(i + 6));
            double fumbles = Double.parseDouble(td.get(i + 7));
            proj += (rushYards / (scoring.rushYards));
            proj += rushTD * scoring.rushTD;
            proj += catches * scoring.catches;
            proj += (recYards / (scoring.recYards));
            proj += recTD * scoring.recTD;
            proj -= fumbles * scoring.fumble;
            proj = Double.parseDouble(df.format(proj));
            points.put(name + Constants.HASH_DELIMITER + team + Constants.HASH_DELIMITER + pos, proj);
        }
    }

    public static void wrProj(String url, Map<String, Double> points,
                              Scoring scoring, String pos) throws IOException {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        List<String> td = HandleBasicQueries.handleLists(url, "table.table-bordered tbody tr td");

        int min = 0;
        ParseRankings.handleHashes();

        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 9) {
            double proj = 0;
            String name = "";
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name += nameSet[j] + " ";
            }
            name = ParseRankings.fixNames(name.substring(0, name.length() - 1));
            String team = ParseRankings.fixTeams(nameSet[nameSet.length - 1]);
            double rushYards = Double.parseDouble(td.get(i + 2)
                    .replace(",", ""));
            double rushTD = Double.parseDouble(td.get(i + 3));
            double catches = Double.parseDouble(td.get(i + 4));
            double recYards = Double
                    .parseDouble(td.get(i + 5).replace(",", ""));
            double recTD = Double.parseDouble(td.get(i + 6));
            double fumbles = Double.parseDouble(td.get(i + 7));
            proj += (rushYards / (scoring.rushYards));
            proj += rushTD * scoring.rushTD;
            proj += catches * scoring.catches;
            proj += (recYards / (scoring.recYards));
            proj += recTD * scoring.recTD;
            proj -= fumbles * scoring.fumble;
            proj = Double.parseDouble(df.format(proj));
            points.put(name + Constants.HASH_DELIMITER + team + Constants.HASH_DELIMITER + pos, proj);
        }
    }

    public static void teProj(String url, Map<String, Double> points,
                              Scoring scoring, String pos) throws IOException {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        List<String> td = HandleBasicQueries.handleLists(url, "table.table-bordered tbody tr td");

        int min = 0;
        ParseRankings.handleHashes();

        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 6) {
            double proj = 0;
            String name = "";
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name += nameSet[j] + " ";
            }
            name = ParseRankings.fixNames(name.substring(0, name.length() - 1));
            String team = ParseRankings.fixTeams(nameSet[nameSet.length - 1]);
            double catches = Double.parseDouble(td.get(i + 1).replace(",", ""));
            double recTD = Double.parseDouble(td.get(i + 3));
            double recYards = Double
                    .parseDouble(td.get(i + 2).replace(",", ""));
            double fumbles = Double.parseDouble(td.get(i + 4));
            proj += catches * scoring.catches;
            proj += (recYards / (scoring.recYards));
            proj += recTD * scoring.recTD;
            proj -= fumbles * scoring.fumble;
            proj = Double.parseDouble(df.format(proj));
            points.put(name + Constants.HASH_DELIMITER + team + Constants.HASH_DELIMITER + pos, proj);
        }
    }

    public static void kProj(String url, Map<String, Double> points,
                             String pos) throws IOException {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        List<String> td = HandleBasicQueries.handleLists(url, "table.table-bordered tbody tr td");

        int min = 0;
        ParseRankings.handleHashes();

        for (int i = 0; i < td.size(); i++) {
            if (td.get(i).split(" ").length >= 3) {
                min = i;
                break;
            }
        }
        for (int i = min; i < td.size(); i += 5) {
            double proj = 0;
            String name = "";
            String[] nameSet = td.get(i).split(" ");
            if (nameSet.length == 1) {
                if (td.get(i+1).contains("Site Projections")) {
                    break;
                }
                nameSet = td.get(++i).split(" ");
            }
            int nameLimit = (nameSet.length == 2)  ? nameSet.length : nameSet.length - 1;

            for (int j = 0; j < nameLimit; j++) {
                name += nameSet[j] + " ";
            }
            name = ParseRankings.fixNames(name.substring(0, name.length() - 1));
            String team = ParseRankings.fixTeams(nameSet[nameSet.length - 1]);
            proj = Double.parseDouble(td.get(i + 4));
            points.put(name + Constants.HASH_DELIMITER + team + Constants.HASH_DELIMITER + pos, proj);
        }
    }
}
