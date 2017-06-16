package com.devingotaswitch.rankings.domain;

import android.app.Activity;

import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.asynctasks.RankingsFetcher;
import com.devingotaswitch.rankings.asynctasks.RankingsLoader;
import com.devingotaswitch.utils.ParsingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rankings {
    private Map<String, Player> players;
    private Map<String, Team> teams;
    private List<String> orderedIds;
    private LeagueSettings leagueSettings;
    private RankingsFetcher processor;
    private static RankingsLoader loader;

    public Rankings(LeagueSettings leagueSettings) {
        this(new HashMap<String, Team>(), new HashMap<String, Player>(), new ArrayList<String>(), leagueSettings);
    }

    public Rankings(Map<String, Team> teams, Map<String, Player> players, List<String> orderedIds, LeagueSettings leagueSettings) {
        this.players = players;
        this.teams = teams;
        this.leagueSettings = leagueSettings;
        this.processor = new RankingsFetcher();
        this.loader = new RankingsLoader();
        this.orderedIds = orderedIds;
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public Team getPlayersTeam(Player player) {
        return teams.get(player.getTeamName());
    }

    public void refreshRankings(RankingsHome activity) {
        ParsingUtils.init();

        RankingsFetcher.RanksAggregator ranksParser = processor.new RanksAggregator(activity, this, leagueSettings);
        ranksParser.execute();
    }

    public void saveRankings(RankingsHome activity, RankingsDBWrapper rankingsDB) {
        RankingsLoader.RanksSaver ranksSaver = loader.new RanksSaver(activity, rankingsDB);
        ranksSaver.execute(players, teams);
    }

    public static void loadRankings(RankingsHome activity, RankingsDBWrapper rankingsDB) {
        RankingsLoader.RanksLoader ranksLoader = loader.new RanksLoader(activity, rankingsDB);
        ranksLoader.execute();
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
