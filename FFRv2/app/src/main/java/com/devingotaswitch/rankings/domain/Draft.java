package com.devingotaswitch.rankings.domain;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Draft {

    private List<String> draftedPlayers;
    private Map<String, Integer> myPlayers;
    private List<Player> myQbs;
    private List<Player> myRbs;
    private List<Player> myWrs;
    private List<Player> myTes;
    private List<Player> myDsts;
    private List<Player> myKs;

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

    public double getDraftValue() {
        return draftValue;
    }

    public double getTotalPAA() {
        return getQBPAA() + getRBPAA() + getWRPAA() + getTEPAA() + getDSTPAA() + getKPAA();
    }

    public double getQBPAA() {
        return getPAAForPos(myQbs, Constants.QB);
    }

    public double getWRPAA() {
        return getPAAForPos(myWrs, Constants.WR);
    }

    public double getRBPAA() {
        return getPAAForPos(myRbs, Constants.RB);
    }

    public double getTEPAA() {
        return getPAAForPos(myTes, Constants.TE);
    }

    public double getDSTPAA() {
        return getPAAForPos(myDsts, Constants.DST);
    }

    public double getKPAA() {
        return getPAAForPos(myKs, Constants.K);
    }

    private double getPAAForPos(List<Player> players, String position) {
        double posPAA = 0.0;
        for (Player player : players) {
            if (position.equals(player.getPosition())) {
                posPAA += player.getPaa();
            }
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

    public void unDraftPlayer(Player player) {
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

    public String getPAALeft(String pos, Rankings rankings) {
        DecimalFormat df = new DecimalFormat("#.#");
        String result = pos + "s: ";
        double paaLeft = 0.0;
        int counter = 0;

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

        for (Player player : players) {
            paaLeft += player.getPaa();
            counter++;
            if (counter == 10) {
                result += df.format(paaLeft);
                break;
            }
            if (counter == 3) {
                result += df.format(paaLeft) + "/";
            }
            if (counter == 5) {
                result += df.format(paaLeft) + "/";
            }
        }
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
        draftedPlayers.clear();
        draftValue = 0.0;
        LocalSettingsHelper.clearDraft(context);
    }
}
