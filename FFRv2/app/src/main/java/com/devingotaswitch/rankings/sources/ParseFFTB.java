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
        System.out.println(url);
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

            String name = "";
            String team = brokenUp.get(i + 1);
            if (Constants.DST.equals(pos)) {
                name = team;
            } else {
                name = brokenUp.get(i);
            }
            String age = brokenUp.get(i + 4);
            String val = brokenUp.get(i + 6);
            String bye = brokenUp.get(i + 3);

            if (team.split(" ").length <= 3) {
                val = val.substring(1, val.length());
                Player player = ParsingUtils.getPlayerFromRankings(name, team, pos, Double.parseDouble(val));
                if (GeneralUtils.isInteger(age)) {
                    player.setAge(Integer.parseInt(age));
                }
                rankings.processNewPlayer(player);
            }
            Team newTeam = new Team();
            newTeam.setBye(bye);
            newTeam.setName(team);
            rankings.addTeam(newTeam);
        }
    }
}
