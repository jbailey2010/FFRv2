package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.utils.ParsingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rankings {
    private Map<String, Player> players;

    public Rankings() {
        this.players = new HashMap<>();
    }

    public void processNewPlayer(Player player) {
        if (!players.containsKey(player.getUniqueId())) {
            players.put(player.getUniqueId(), ParsingUtils.normalizePlayerFields(player));
        } else {
            Player existingPlayer = players.get(player.getUniqueId());
            players.put(player.getUniqueId(), ParsingUtils.conditionallyAddContext(existingPlayer, player));
        }
    }
}
