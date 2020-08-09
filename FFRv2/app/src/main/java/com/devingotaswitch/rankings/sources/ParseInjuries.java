package com.devingotaswitch.rankings.sources;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseInjuries {

    public static void parsePlayerInjuries(Rankings rankings)
            throws IOException {
        Map<String, String> injuries = new HashMap<>();
        Document doc = JsoupUtils.getDocument(
                "https://www.pro-football-reference.com/players/injuries.htm");
        List<String> players = JsoupUtils.getElemsFromDoc(doc, "table.stats_table th.left a");
        List<String> left = JsoupUtils.getElemsFromDoc(doc, "table.stats_table td.left");
        List<String> right = JsoupUtils.getElemsFromDoc(doc, "table.stats_table td.right");
                //"table.stats_table tbody tr");
        for (int i = 0; i < players.size(); i++) {
            String name = ParsingUtils.normalizeNames(players.get(i));

            int leftIndex = i * 3;
            String team = ParsingUtils.normalizeTeams(left.get(leftIndex ));
            String pos = left.get(leftIndex + 1);
            String comment = left.get(leftIndex + 2);

            int rightIndex = i * 2;
            String injuryType = right.get(rightIndex);
            String playerStatus = right.get(rightIndex + 1);
            playerStatus = playerStatus.substring(0, 1).toUpperCase() + playerStatus.substring(1);


            String injuryStr;

            if (pos.equals("CB") || pos.equals("LB") || pos.equals("DT") || pos.equals("DB") ||
                    pos.equals("DE") || pos.equals("S")) {
                String playerName = name;
                name = ParsingUtils.normalizeDefenses(team);
                pos = Constants.DST;
                String playerId = getPlayerId(name, pos, team);

                // If it's defense, we'll track it collectively.
                String baseStr = "";
                if (injuries.containsKey(team)) {
                    baseStr = injuries.get(playerId) + Constants.LINE_BREAK;
                }
                baseStr = baseStr +
                        playerName +
                        ": " +
                        playerStatus +
                        " (" +
                        injuryType +
                        ")";
                injuries.put(playerId, baseStr);

            } else {
                String playerId = getPlayerId(name, pos, team);

                injuryStr = playerStatus +
                        " (" +
                        injuryType +
                        ")" +
                        Constants.LINE_BREAK +
                        Constants.LINE_BREAK +
                        comment;

                injuries.put(playerId, injuryStr);
            }
        }

        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            String injuryStatus = injuries.get(player.getUniqueId());
            if (!StringUtils.isBlank(injuryStatus)) {
                player.setInjuryStatus(injuryStatus);
            }
        }
    }

    private static String getPlayerId(String name, String pos, String team) {
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos;
    }
}
