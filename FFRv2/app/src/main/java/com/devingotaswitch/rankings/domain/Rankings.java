package com.devingotaswitch.rankings.domain;

import android.app.Activity;
import android.util.Log;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.asynctasks.RankingsFetcher;
import com.devingotaswitch.rankings.asynctasks.RankingsLoader;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.ParsingUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    private static RankingsLoader loader;
    private static Map<String, List<DailyProjection>> playerProjectionHistory;

    // AppSync stuff
    private static UserSettings userSettings = new UserSettings();
    private static List<String> playerWatchList = new ArrayList<>();
    private static Map<String, String> playerNotes = new HashMap<>();

    public static Rankings init() {
        return new Rankings();
    }

    public static Rankings initWithDefaults(LeagueSettings leagueSettings) {
        return init(new HashMap<String, Team>(), new HashMap<String, Player>(), new ArrayList<String>(), leagueSettings,
                new Draft(), new HashMap<String, List<DailyProjection>>());
    }

    public static Rankings init(Map<String, Team> inputTeams, Map<String, Player> inputPlayers, List<String> inputIds,
                                LeagueSettings inputSettings, Draft inputDraft, Map<String, List<DailyProjection>> inputProjectionHistory) {
        players = inputPlayers;
        teams = inputTeams;
        leagueSettings = inputSettings;
        loader = new RankingsLoader();
        orderedIds = inputIds;
        draft = inputDraft;
        playerProjectionHistory = inputProjectionHistory;
        return new Rankings();
    }

    public static void setUserSettings(UserSettings settings) {
        userSettings = settings;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public static void setCustomUserData(List<String> watchList, Map<String, String> notes) {
        playerWatchList = watchList;
        playerNotes = notes;
    }

    public boolean isPlayerWatched(String playerId) {
        return playerWatchList.contains(playerId);
    }

    public void updatePlayerNote(Activity act, String playerId, String note) {
        if (!StringUtils.isBlank(note)) {
            playerNotes.put(playerId, note);
        } else {
            playerNotes.remove(playerId);
        }
        AppSyncHelper.updateUserCustomPlayerData(act, playerWatchList, playerNotes);
    }

    public void togglePlayerWatched(Activity act, String playerId) {
        if (playerWatchList.contains(playerId)) {
            playerWatchList.remove(playerId);
            AppSyncHelper.decrementPlayerWatchedCount(act, playerId);
        } else {
            playerWatchList.add(playerId);
            AppSyncHelper.incrementPlayerWatchedCount(act, playerId);
        }
        AppSyncHelper.updateUserCustomPlayerData(act, playerWatchList, playerNotes);
    }

    public String getPlayerNote(String playerId) {
        if (playerNotes.containsKey(playerId)) {
            return playerNotes.get(playerId);
        }
        return "";
    }

    public LeagueSettings getLeagueSettings() {
        return leagueSettings;
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public List<String> getOrderedIds() {
        return orderedIds;
    }

    public void setOrderedIds(List<String> orderedIds) {
        Rankings.orderedIds = orderedIds;
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

    public Map<String, List<DailyProjection>> getPlayerProjectionHistory() {
        return playerProjectionHistory;
    }

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
        players.clear();
        teams.clear();
        orderedIds.clear();
    }

    public void refreshRankings(RankingsHome activity) {
        ParsingUtils.init();

        RankingsFetcher.RanksAggregator ranksParser = new RankingsFetcher.RanksAggregator(activity, this);
        ranksParser.execute();
    }

    public void updateProjectionsAndVBD(Activity activity, LeagueSettings league, boolean updateProjections,
                                        RankingsDBWrapper rankingsDB) {
        RankingsFetcher.VBDUpdater vbdUpdater = new RankingsFetcher.VBDUpdater(this, activity, league, updateProjections, rankingsDB);
        vbdUpdater.execute();
    }

    public void saveRankings(RankingsHome activity, RankingsDBWrapper rankingsDB) {
        RankingsLoader.RanksSaver ranksSaver = new RankingsLoader.RanksSaver(activity, rankingsDB);
        ranksSaver.execute(players, teams);
    }

    public static void loadRankings(RankingsHome activity, RankingsDBWrapper rankingsDB) {
        if (loader == null) {
            loader = new RankingsLoader();
        }
        RankingsLoader.RanksLoader ranksLoader = new RankingsLoader.RanksLoader(activity, rankingsDB);
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
        List<String> overlap = new ArrayList<>();
        for (String id : playerWatchList) {
            if (source.contains(id)) {
                overlap.add(id);
            }
        }
        return overlap;
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

    public List<String> orderPlayersByLeagueType(Collection<Player> players) {
        List<String> orderedIds = new ArrayList<>();
        Comparator<Player> comparator;
        if (getLeagueSettings().isAuction()) {
            comparator = (a, b) -> b.getAuctionValue().compareTo(a.getAuctionValue());
        } else if (getLeagueSettings().isDynasty()) {
            comparator = (a, b) -> a.getDynastyRank().compareTo(b.getDynastyRank());
        } else if (getLeagueSettings().isRookie()) {
            comparator = (a, b) -> a.getRookieRank().compareTo(b.getRookieRank());
        } else if (getLeagueSettings().isBestBall()) {
            comparator = (a, b) -> a.getBestBallRank().compareTo(b.getBestBallRank());
        } else {
            comparator = (a, b) -> a.getEcr().compareTo(b.getEcr());
        }
        List<Player> playerList = new ArrayList<>(players);
        Collections.sort(playerList, comparator);
        for (Player player : playerList) {
            orderedIds.add(player.getUniqueId());
        }
        return orderedIds;
    }
}
