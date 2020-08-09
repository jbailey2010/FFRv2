package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.List;

public class ParseFFTB {

    public static void parseFFTBRankingsWrapper(Rankings rankings)
            throws IOException {
        String teams = String.valueOf(rankings.getLeagueSettings().getTeamCount());
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=QB&teams=" + teams + "&budget=200", Constants.QB);
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=RB&teams=" + teams + "&budget=200", Constants.RB);
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=WR&teams=" + teams + "&budget=200", Constants.WR);
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=TE&teams=" + teams + "&budget=200", Constants.TE);
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=PK&teams=" + teams + "&budget=200", Constants.K);
        parseFFTBPage(
                rankings,
                "http://www.fftoolbox.com/football/" + Constants.YEAR_KEY + "/auction-values.cfm?pos=Def&teams=" + teams + "&budget=200", Constants.DST);
    }

    private static void parseFFTBPage(Rankings rankings, String url, String pos)
            throws IOException {
        List<String> brokenUp = JsoupUtils.parseURLWithoutUAOrTls(url, "td");
        int min = 0;
        for (int i = 0; i < brokenUp.size(); i++) {
            if (GeneralUtils.isInteger(brokenUp.get(i))) {
                min = i + 1;
                break;
            }
        }
        for (int i = min; i < brokenUp.size(); i += 8) {
            if (i + 8 > brokenUp.size()) {
                break;
            }

            String name;
            String team = ParsingUtils.normalizeTeams(brokenUp.get(i + 1));
            if (Constants.DST.equals(pos)) {
                name = ParsingUtils.normalizeDefenses(team);
            } else {
                name = ParsingUtils.normalizeNames(brokenUp.get(i));
            }
            String age = brokenUp.get(i + 4);
            String exp = brokenUp.get(i + 5);
            String bye = brokenUp.get(i + 3);

            if (team.split(" ").length <= 3) {
                boolean isNewPlayer = false;
                String playerId = name +
                        Constants.PLAYER_ID_DELIMITER +
                        team +
                        Constants.PLAYER_ID_DELIMITER +
                        pos;
                Player player;
                if (rankings.getPlayers().containsKey(playerId)) {
                    player = rankings.getPlayer(playerId);
                } else {
                    // FF Toolbox rankings are ass, so we'll just default to 1 if we haven't seen it yet.
                    // They have retired players for $30+, so their value is suspect.
                    isNewPlayer = true;
                    player = ParsingUtils.getPlayerFromRankings(name,team,pos, 1.0);
                }

                if (GeneralUtils.isInteger(age)) {
                    player.setAge(Integer.parseInt(age));
                }
                if (GeneralUtils.isInteger(exp)) {
                    player.setExperience(Integer.parseInt(exp));
                } else if ("R".equals(exp)) {
                    player.setExperience(0);
                }

                if (isNewPlayer) {
                    rankings.processNewPlayer(player);
                } else {
                    rankings.getPlayers().put(player.getUniqueId(), player);
                }
            }
            Team newTeam = new Team();
            newTeam.setBye(bye);
            newTeam.setName(team);
            rankings.addTeam(newTeam);
        }
    }
}
