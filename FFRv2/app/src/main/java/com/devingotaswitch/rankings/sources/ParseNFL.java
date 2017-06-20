package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.List;

public class ParseNFL {
    public static void parseNFLAAVWrapper(Rankings rankings) throws IOException {
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=0&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=26&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=51&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=76&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=101&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=126&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=151&sort=draftAveragePosition");
        parseNFLAAVWorker(
                rankings,
                "http://fantasy.nfl.com/draftcenter/breakdown?leagueId=&offset=176&sort=draftAveragePosition");
    }

    private static void parseNFLAAVWorker(Rankings rankings, String url)
            throws IOException {
        List<String> td = JsoupUtils.handleLists(url, "td");
        for (int i = 0; i < td.size(); i += 4) {
            String nameSet[] = td.get(i).split(" ");
            String name = "";
            int filter = 0;
            for (int j = 0; j < nameSet.length; j++) {
                if (nameSet[j].equals("DEF")) {
                    filter = j;
                    break;
                }
                if (nameSet[j].equals("-")) {
                    filter = j - 1;
                    break;
                }
                if (nameSet[j].equals("View")) {
                    filter = j - 1;
                    break;
                }
                if (nameSet[j].length() == j) {
                    filter = j;
                    break;
                }
                if (nameSet[j].equals(Constants.QB) || nameSet[j].equals(Constants.RB)
                        || nameSet[j].equals(Constants.WR) || nameSet[j].equals(Constants.TE)
                        || nameSet[j].equals(Constants.K)) {
                    filter = j;
                    break;
                }
            }
            for (int j = 0; j < filter; j++) {
                name += nameSet[j] + " ";
            }
            name = name.substring(0, name.length() - 1);
            String pos = nameSet[filter];
            String worth = td.get(i + 3);
            double val = Double.parseDouble(worth);
            String team = nameSet[nameSet.length - 1];
            if ("DEF".equals(pos)) {
                team = name;
            }
            Player player = new Player();
            player.setName(name);
            player.setTeamName(team);
            player.setPosition(pos);
            player.handleNewValue(val);
            player = ParsingUtils.getPlayerFromRankings(name, team, pos, val);
            rankings.processNewPlayer(player);
        }
    }
}
