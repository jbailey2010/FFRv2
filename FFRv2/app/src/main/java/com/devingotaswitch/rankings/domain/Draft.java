package com.devingotaswitch.rankings.domain;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Draft {

    private final List<String> draftedPlayers;
    private final Map<String, Integer> myPlayers;
    private final List<Player> myQbs;
    private final List<Player> myRbs;
    private final List<Player> myWrs;
    private final List<Player> myTes;
    private final List<Player> myDsts;
    private final List<Player> myKs;

    private double draftValue;

    public Draft() {
        draftedPlayers = new ArrayList<>();
        myQbs = new ArrayList<>();
        myRbs = new ArrayList<>();
        myWrs = new ArrayList<>();
        myTes = new ArrayList<>();
        myDsts = new ArrayList<>();
        myKs = new ArrayList<>();
        myPlayers = new HashMap<>();
        draftValue = 0.0;
    }

    public List<String> getDraftedPlayers() {
        return draftedPlayers;
    }

    public Map<String, Integer> getMyPlayers() {
        return myPlayers;
    }

    public List<Player> getMyQbs() {
        return myQbs;
    }

    public List<Player> getMyRbs() {
        return myRbs;
    }

    public List<Player> getMyWrs() {
        return myWrs;
    }

    public List<Player> getMyTes() {
        return myTes;
    }

    public List<Player> getMyDsts() {
        return myDsts;
    }

    public List<Player> getMyKs() {
        return myKs;
    }

    public List<Player> getPlayersDraftedForPos(String position) {
        if (Constants.QB.equals(position)) {
            return getMyQbs();
        } else if (Constants.RB.equals(position)) {
            return getMyRbs();
        } else if (Constants.WR.equals(position)) {
            return getMyWrs();
        } else if (Constants.TE.equals(position)) {
            return getMyTes();
        } else if (Constants.DST.equals(position)) {
            return getMyDsts();
        } else if (Constants.K.equals(position)) {
            return getMyKs();
        }
        return new ArrayList<>();
    }

    public List<Player> getPlayersWithSameByeAndPos(Player player, Rankings rankings) {
        return getPlayersWithSameBye(getPlayersDraftedForPos(player.getPosition()),
                rankings, rankings.getTeam(player).getBye());
    }

    public List<Player> getPlayersWithSameBye(Player player, Rankings rankings) {
        List<Player> allMyPicks = new ArrayList<>();
        allMyPicks.addAll(getMyQbs());
        allMyPicks.addAll(getMyRbs());
        allMyPicks.addAll(getMyWrs());
        allMyPicks.addAll(getMyTes());
        allMyPicks.addAll(getMyDsts());
        allMyPicks.addAll(getMyKs());
        return getPlayersWithSameBye(allMyPicks, rankings, rankings.getTeam(player).getBye());
    }

    private List<Player> getPlayersWithSameBye(List<Player> toCheck, Rankings rankings, String bye) {
        List<Player> sameBye = new ArrayList<>();
        for (Player posPlayer : toCheck) {
            if (bye.equals(rankings.getTeam(posPlayer).getBye())) {
                sameBye.add(posPlayer);
            }
        }
        return sameBye;
    }

    public double getDraftValue() {
        return draftValue;
    }

    public double getTotalPAA() {
        return getQBPAA() + getRBPAA() + getWRPAA() + getTEPAA() + getDSTPAA() + getKPAA();
    }

    public double getTotalXVal() {
        return getQBXval() + getRBXval() + getWRXval() + getTEXval() + getDSTXval() + getKXval();
    }

    public double getTotalVoLS() {
        return getQBVoLS() + getRBVoLS() + getWRVoLS() + getTEVoLS() + getDSTVoLS() + getKVoLS();
    }

    public double getQBXval() {
        return getXValForPos(myQbs);
    }

    public double getWRXval() {
        return getXValForPos(myWrs);
    }

    public double getRBXval() {
        return getXValForPos(myRbs);
    }

    public double getTEXval() {
        return getXValForPos(myTes);
    }

    public double getDSTXval() {
        return getXValForPos(myDsts);
    }

    public double getKXval() {
        return getXValForPos(myKs);
    }

    private double getXValForPos(List<Player> players) {
        double posXval = 0.0;
        for (Player player : players) {
            posXval += player.getxVal();
        }
        return posXval;
    }

    public double getQBVoLS() {
        return getVoLSForPos(myQbs);
    }

    public double getRBVoLS() {
        return getVoLSForPos(myRbs);
    }

    public double getWRVoLS() {
        return getVoLSForPos(myWrs);
    }

    public double getTEVoLS() {
        return getVoLSForPos(myTes);
    }

    public double getDSTVoLS() {
        return getVoLSForPos(myDsts);
    }

    public double getKVoLS() {
        return getVoLSForPos(myKs);
    }

    private double getVoLSForPos(List<Player> players) {
        double posVoLS = 0.0;
        for (Player player : players) {
            posVoLS += player.getvOLS();
        }
        return posVoLS;
    }

    public double getQBPAA() {
        return getPAAForPos(myQbs);
    }

    public double getWRPAA() {
        return getPAAForPos(myWrs);
    }

    public double getRBPAA() {
        return getPAAForPos(myRbs);
    }

    public double getTEPAA() {
        return getPAAForPos(myTes);
    }

    public double getDSTPAA() {
        return getPAAForPos(myDsts);
    }

    public double getKPAA() {
        return getPAAForPos(myKs);
    }

    private double getPAAForPos(List<Player> players) {
        double posPAA = 0.0;
        for (Player player : players) {
            posPAA += player.getPaa();
        }
        return posPAA;
    }

    public void draftPlayer(Player player, boolean myPick, int cost) {
        draftedPlayers.add(player.getUniqueId());
        if (myPick) {
            this.myPlayers.put(player.getUniqueId(), cost);
            this.draftValue += (player.getAuctionValue() - (double) cost);
            if (Constants.QB.equals(player.getPosition())) {
                myQbs.add(player);
            } else if (Constants.RB.equals(player.getPosition())) {
                myRbs.add(player);
            } else if (Constants.WR.equals(player.getPosition())) {
                myWrs.add(player);
            } else if (Constants.TE.equals(player.getPosition())) {
                myTes.add(player);
            } else if (Constants.DST.equals(player.getPosition())) {
                myDsts.add(player);
            } else if (Constants.K.equals(player.getPosition())) {
                myKs.add(player);
            }
        }
    }

    private void unDraftPlayer(Player player) {
        draftedPlayers.remove(player.getUniqueId());
        if (isDraftedByMe(player)) {
            int cost = myPlayers.get(player.getUniqueId());
            this.myPlayers.remove(player.getUniqueId());
            this.draftValue -= (player.getAuctionValue() - (double) cost);
            if (Constants.QB.equals(player.getPosition())) {
                myQbs.remove(player);
            } else if (Constants.RB.equals(player.getPosition())) {
                myRbs.remove(player);
            } else if (Constants.WR.equals(player.getPosition())) {
                myWrs.remove(player);
            } else if (Constants.TE.equals(player.getPosition())) {
                myTes.remove(player);
            } else if (Constants.DST.equals(player.getPosition())) {
                myDsts.remove(player);
            } else if (Constants.K.equals(player.getPosition())) {
                myKs.remove(player);
            }
        }
    }

    public boolean isDraftedByMe(Player player) {
        return myPlayers.containsKey(player.getUniqueId());
    }

    public boolean isDrafted(Player player) { return draftedPlayers.contains(player.getUniqueId()); }

    public String draftedToSerializedString() {
        StringBuilder draftedStr = new StringBuilder();
        for (String key : draftedPlayers) {
            draftedStr.append(key)
                    .append(Constants.HASH_DELIMITER);
        }
        return draftedStr.toString();
    }

    public String myTeamToSerializedString() {
        StringBuilder myTeamStr = new StringBuilder();
        for (String key : myPlayers.keySet()) {
            int cost = myPlayers.get(key);
            myTeamStr.append(key)
                    .append(Constants.HASH_DELIMITER)
                    .append(cost)
                    .append(Constants.HASH_DELIMITER);
        }
        return myTeamStr.toString();
    }

    public List<Player> getSortedAvailablePlayersForPosition(String pos, Rankings rankings) {
        Comparator<Player> comparator = new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getProjection() > b.getProjection()) {
                    return -1;
                }
                if (a.getProjection() < b.getProjection()) {
                    return 1;
                }
                return 0;
            }
        };
        List<Player> players = new ArrayList<>();
        for (Player player : rankings.getPlayers().values()) {
            if (!isDrafted(player) && player.getPosition().equals(pos)) {
                players.add(player);
            }
        }
        Collections.sort(players, comparator);
        return players;
    }

    public double getPAANAvailablePlayersBack(List<Player> players, int limit) {
        int counter = 0;
        double paaLeft = 0.0;
        for (Player player : players) {
            paaLeft += player.getPaa();
            counter++;
            if (counter == limit) {
                break;
            }
        }
        return paaLeft;
    }

    public String getPAALeft(String pos, Rankings rankings) {
        DecimalFormat df = new DecimalFormat("#.#");
        String result = pos + "s: ";
        List<Player> players = getSortedAvailablePlayersForPosition(pos, rankings);

        result += df.format(getPAANAvailablePlayersBack(players, 3)) + "/";
        result += df.format(getPAANAvailablePlayersBack(players, 5)) + "/";
        result += df.format(getPAANAvailablePlayersBack(players, 10));

        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public void resetDraft(Context context) {
        myQbs.clear();
        myRbs.clear();
        myWrs.clear();
        myTes.clear();
        myDsts.clear();
        myKs.clear();
        myPlayers.clear();
        draftedPlayers.clear();
        draftValue = 0.0;
        LocalSettingsHelper.clearDraft(context);
    }

    public void draftBySomeone(Rankings rankings, Player player, Activity act, View view, View.OnClickListener listener) {
        draftPlayer(player, false, 0);
        if (listener == null) {
            Snackbar.make(view, player.getName() + " drafted", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, player.getName() + " drafted", Snackbar.LENGTH_LONG).setAction("Undo", listener).show();
        }
        saveDraft(rankings, act);
    }

    public void draftByMe(Rankings rankings, Player player, Activity act, int cost, View view, View.OnClickListener listener) {
        draftPlayer(player, true, cost);
        if (listener == null) {
            Snackbar.make(view, player.getName() + " drafted by you", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, player.getName() + " drafted by you", Snackbar.LENGTH_LONG).setAction("Undo", listener).show();
        }
        saveDraft(rankings, act);
    }

    public void undraft(Rankings rankings, Player player, Activity act, View view) {
        unDraftPlayer(player);
        Snackbar.make(view, player.getName() + " undrafted", Snackbar.LENGTH_SHORT).show();
        saveDraft(rankings, act);
    }

    private void saveDraft(Rankings rankings, Activity act) {
        LocalSettingsHelper.saveDraft(act, rankings.getDraft());
    }
}
