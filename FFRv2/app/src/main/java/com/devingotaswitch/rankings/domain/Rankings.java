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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rankings {
    private static Map<String, Player> players;
    private static Map<String, Team> teams;
    private static List<String> orderedIds;
    private static Draft draft;
    private static LeagueSettings leagueSettings;
    private static RankingsFetcher processor;
    private static RankingsLoader loader;

    public static Rankings init() {
        return new Rankings();
    }

    public static Rankings initWithDefaults(LeagueSettings leagueSettings) {
        return init(new HashMap<String, Team>(), new HashMap<String, Player>(), new ArrayList<String>(), leagueSettings,
                new Draft());
    }

    public static Rankings init(Map<String, Team> inputTeams, Map<String, Player> inputPlayers, List<String> inputIds,
                                LeagueSettings inputSettings, Draft inputDraft) {
        players = inputPlayers;
        teams = inputTeams;
        leagueSettings = inputSettings;
        processor = new RankingsFetcher();
        loader = new RankingsLoader();
        orderedIds = inputIds;
        draft = inputDraft;
        return new Rankings();
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

    public Draft getDraft() {
        return draft;
    }

    public List<Player> getQbs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            Player player = getPlayer(key);
            if (player.getPosition().equals(Constants.QB)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getRbs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            Player player = getPlayer(key);
            if (player.getPosition().equals(Constants.RB)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getWrs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            Player player = getPlayer(key);
            if (player.getPosition().equals(Constants.WR)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getTes() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            Player player = getPlayer(key);
            if (player.getPosition().equals(Constants.TE)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getDsts() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            Player player = getPlayer(key);
            if (player.getPosition().equals(Constants.DST)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public List<Player> getKs() {
        List<Player> pos = new ArrayList<>();
        for (String key : players.keySet()) {
            Player player = getPlayer(key);
            if (player.getPosition().equals(Constants.K)) {
                pos.add(players.get(key));
            }
        }
        return pos;
    }

    public Map<String, Player> getPlayers() { return players; }

    public Map<String, Team> getTeams() { return teams; }

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

        RankingsFetcher.RanksAggregator ranksParser = processor.new RanksAggregator(activity, this);
        ranksParser.execute();
    }

    public void updateProjectionsAndVBD(Activity activity, LeagueSettings league, boolean updateProjections,
                                        RankingsDBWrapper rankingsDB) {
        RankingsFetcher.VBDUpdater vbdUpdater = processor.new VBDUpdater(this, activity, league, updateProjections, rankingsDB);
        vbdUpdater.execute();
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

    public void dedupPlayer(Player fake, Player real) {
        players.remove(fake.getUniqueId());
        orderedIds.remove(fake.getUniqueId());
        real.handleNewValue(fake.getAuctionValue());
    }

    public void processNewPlayer(Player player) {
        if (!players.containsKey(player.getUniqueId())) {
            players.put(player.getUniqueId(), ParsingUtils.normalizePlayerFields(player));
        } else {
            Player existingPlayer = players.get(player.getUniqueId());
            players.put(player.getUniqueId(), ParsingUtils.conditionallyAddContext(existingPlayer, player));

        }
    }

    public List<String> getPlayersByTeam(List<String> source, String team) {
        List<String> idsOnTeam = new ArrayList<>();
        for (String key : source) {
            Player player = players.get(key);
            if (player.getTeamName().equals(team)) {
                idsOnTeam.add(key);
            }
        }
        return idsOnTeam;
    }

    public List<String> getWatchedPlayers(List<String> source) {
        List<String> watchedIds = new ArrayList<>();
        for (String key : source) {
            Player player = players.get(key);
            if (player.isWatched()) {
                watchedIds.add(key);
            }
        }
        return watchedIds;
    }

    public List<String> getPlayersByPosition(List<String> source, String position) {
        Set<String> positions = new HashSet<>();
        switch (position) {
            case Constants.RBWR:
                positions.add(Constants.RB);
                positions.add(Constants.WR);
                break;
            case Constants.RBTE:
                positions.add(Constants.RB);
                positions.add(Constants.TE);
                break;
            case Constants.RBWRTE:
                positions.add(Constants.RB);
                positions.add(Constants.WR);
                positions.add(Constants.TE);
                break;
            case Constants.WRTE:
                positions.add(Constants.WR);
                positions.add(Constants.TE);
                break;
            case Constants.QBRBWRTE:
                positions.add(Constants.QB);
                positions.add(Constants.RB);
                positions.add(Constants.WR);
                positions.add(Constants.TE);
                break;
            default:
                positions.add(position);
                break;
        }
        return getPlayersByPositionInternal(source, positions);
    }

    private List<String> getPlayersByPositionInternal(List<String> source, Set<String> positions) {
        List<String> idsByPos = new ArrayList<>();
        for (String key : source) {
            Player player = players.get(key);
            if (positions.contains(player.getPosition())) {
                idsByPos.add(key);
            }
        }
        return idsByPos;
    }
}
