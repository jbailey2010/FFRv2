package com.devingotaswitch.rankings.sources;

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

public class ParseInjuries {

    public static Map<String, String> parsePlayerInjuries(Rankings rankings)
            throws IOException {
        Map<String, String> injuries = new HashMap<>();
        List<String> perRow = JsoupUtils.parseURLWithUA(
                "http://www.rotoworld.com/teams/injuries/nfl/all/", "td");
        for (int i = 7; i < perRow.size(); i++) {
            String pos = perRow.get(i + 2);
            String name = ParsingUtils.normalizeNames(perRow.get(i));
            String status = perRow.get(i += 3);
            String injuryType = perRow.get(i += 2);
            if (injuryType.equals("-")) {
                injuryType = "Suspended";
            }
            String returnDate = perRow.get(++i);
            String output = "Injury Status: " + status + Constants.LINE_BREAK
                    + "Type of Injury: " + injuryType + Constants.LINE_BREAK
                    + "Expected Return: " + returnDate + Constants.LINE_BREAK;
            injuries.put(name + Constants.PLAYER_ID_DELIMITER + pos, output);
        }

        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            String injuryStatus = injuries.get(player.getName() + Constants.PLAYER_ID_DELIMITER + player.getPosition());
            if (!StringUtils.isBlank(injuryStatus)) {
                player.setInjuryStatus(injuryStatus);
            }
        }
        return injuries;
    }
}
