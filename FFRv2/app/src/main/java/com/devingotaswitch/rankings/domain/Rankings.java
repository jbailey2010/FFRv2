package com.devingotaswitch.rankings.domain;

import android.app.Activity;

import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.asynctasks.RankingsFetcher;
import com.devingotaswitch.rankings.asynctasks.RankingsLoader;
import com.devingotaswitch.utils.Constants;
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

    public LeagueSettings getLeagueSettings() {
        return this.leagueSettings;
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public List<String> getOrderedIds() {
        return orderedIds;
    }

    public void setOrderedIds(List<String> orderedIds) {
        this.orderedIds = orderedIds;
    }

    public List<Player> getQbs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            if (key.contains(Constants.QB)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getRbs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            if (key.contains(Constants.RB)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getWrs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            if (key.contains(Constants.WR)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getTes() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            if (key.contains(Constants.TE)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getDsts() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            if (key.contains(Constants.DST)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getKs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            if (key.contains(Constants.K)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public Map<String, Player> getPlayers() { return players; }

    public Team getTeam(Player player) { return getTeam(player.getTeamName()); }

    public Team getTeam(String teamName) {
        return teams.get(teamName);
    }

    public void addTeam(Team team) {
        if (teams.get(team.getName()) == null) {
            team.setName(ParsingUtils.normalizeTeams(team.getName()));
            teams.put(team.getName(), team);
        }
    }

    public void clearRankings() {
        this.players.clear();
        this.teams.clear();
        this.orderedIds.clear();
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
        if (loader == null) {
            loader = new RankingsLoader();
        }
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
