package com.devingotaswitch.rankings.domain;

import android.content.Context;
import android.widget.Toast;

import com.devingotaswitch.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Draft {

    private Set<String> draftedPlayers;
    private Set<String> myPlayers;
    private List<Player> myQbs;
    private List<Player> myRbs;
    private List<Player> myWrs;
    private List<Player> myTes;
    private List<Player> myDsts;
    private List<Player> myKs;

    private boolean isAuction;
    private int remainingSalary;
    private double draftValue;

    public Draft(LeagueSettings leagueSettings) {
        draftedPlayers = new HashSet<>();
        myQbs = new ArrayList<>();
        myRbs = new ArrayList<>();
        myWrs = new ArrayList<>();
        myTes = new ArrayList<>();
        myDsts = new ArrayList<>();
        myKs = new ArrayList<>();
        myPlayers = new HashSet<>();

        remainingSalary = leagueSettings.getAuctionBudget();
        isAuction = leagueSettings.isAuction();
        draftValue = 0.0;
    }

    public Set<String> getDraftedPlayers() {
        return draftedPlayers;
    }

    public Set<String> getMyPlayers() {
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

    public int getRemainingSalary() {
        return remainingSalary;
    }

    public double getDraftValue() {
        return draftValue;
    }

    public void draftPlayer(Context context, Player player, boolean myPick, int cost) {
        if (draftedPlayers.contains(player.getUniqueId())) {
            Toast.makeText(context, "Player already drafted", Toast.LENGTH_LONG).show();
        }
        draftedPlayers.add(player.getUniqueId());
        if (myPick) {
            this.myPlayers.add(player.getUniqueId());
            this.remainingSalary -= cost;
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

    public void unDraftPlayer(Context context, Player player, boolean myPick, int cost) {
        draftedPlayers.remove(player.getUniqueId());
        if (myPick) {
            this.myPlayers.remove(player.getUniqueId());
            this.remainingSalary += cost;
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

    public boolean isOnMyTeam(Player player) {
        return myPlayers.contains(player.getUniqueId());
    }

    public void resetDraft() {
        myQbs.clear();
        myRbs.clear();
        myWrs.clear();
        myTes.clear();
        myDsts.clear();
        myKs.clear();
        draftedPlayers.clear();
        draftValue = 0.0;
        remainingSalary = 200;
    }
}
