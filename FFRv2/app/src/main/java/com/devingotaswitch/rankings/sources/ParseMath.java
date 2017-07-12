package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.RosterSettings.Flex;
import com.devingotaswitch.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class ParseMath {
    private static double qbLimit;
    private static double rbLimit;
    private static double wrLimit;
    private static double teLimit;
    private static double dLimit;
    private static double kLimit;

    private static void setXValLimits(Rankings rankings) {
        // Top 100 picks for a standard roster.
        RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
        qbLimit = 15.0;
        rbLimit = 36.0;
        wrLimit = 38.0;
        teLimit = 8.0;
        dLimit = 2.0;
        kLimit = 1.0;

        if (roster.getQbCount() > 1 || (roster.getQbCount()>0 && roster.getFlex() != null && roster.getFlex().getQbrbwrteCount() > 0)) {
            qbLimit+=4;
            teLimit--;
            rbLimit-=2;
            wrLimit--;
        }
        if (roster.getTeCount() > 1) {
            teLimit+=2;
            wrLimit--;
            rbLimit--;
        }
        if (roster.getDstCount() == 0) {
            dLimit=0.0;
            rbLimit++;
            wrLimit++;
        }
        if (roster.getkCount() == 0) {
            kLimit = 0.0;
            wrLimit++;
        }

        Log.d("XVal", "XVal QB Limit: " + qbLimit);
        Log.d("XVal", "XVal RB Limit: " + rbLimit);
        Log.d("XVal", "XVal WR Limit: " + wrLimit);
        Log.d("XVal", "XVal TE Limit: " + teLimit);
        Log.d("XVal", "XVal DST Limit: " + dLimit);
        Log.d("XVal", "XVal K Limit: " + kLimit);
    }

    private static void setPAALimits(Rankings rankings) {
        int x = rankings.getLeagueSettings().getTeamCount();
        RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
        Flex flex = roster.getFlex();

        // First, the layups. Assume 1 started.
        dLimit = 1.25 * x;
        kLimit = 1.25 * x;

        // Now, tight ends. These are almost not impacted by flex at all.
        if (roster.getTeCount() < 2) {
            teLimit = (1.75 * x) - 3.3333333;
        } else {
            teLimit = (7.5 * x) - 41.66667;
        }
        if (flex != null && (flex.getRbwrteCount() > 0 || flex.getWrteCount() > 0 ||
                flex.getRbteCount() > 0 || flex.getQbrbwrteCount() > 0)) {
            teLimit += 12.0 / x;
        }

        // Next, QBs. Boring if one, very interesting if not.
        if (roster.getQbCount() == 1 && flex != null && flex.getQbrbwrteCount() == 0) {
            qbLimit = (1.25 * x) + 1.33333;
        } else if (roster.getQbCount() == 0 && flex != null && flex.getQbrbwrteCount() == 1) {
            qbLimit = (1.25 * x);
        } else if (roster.getQbCount() >= 2 || (flex != null && flex.getQbrbwrteCount() >= 2)) {
            qbLimit = (6 * x) - 30;
        } else if (roster.getQbCount() == 1 && flex != null && flex.getQbrbwrteCount() == 1) {
            qbLimit = (6 * x) - 32;
        }

        // Finally, RB/WR. Just all the hell over the place.
        if (roster.getRbCount() < 2) {
            rbLimit = (1.5 * x) - 2;
        } else if (roster.getRbCount() < 3) {
            rbLimit = (3.25 * x) - 5.33333;
        } else {
            rbLimit = (6 * x) - 16.33333;
        }
        if (roster.getWrCount() < 2) {
            wrLimit = (1.25 * x) + 0.33333;
        } else if (roster.getWrCount() < 3) {
            wrLimit = (2.75 * x) - 1.66666667;
        } else {
            wrLimit = (4.5 * x) - 5;
        }
        if (flex != null && (flex.getRbwrCount() > 0 || flex.getRbwrteCount() > 0)) {
            if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
                // Legit
                if (roster.getRbCount() == 2 && roster.getWrCount() == 2) {
                    rbLimit = 3.75 * x - 10.666667;
                    wrLimit = 4.25 * x - 2.33333;
                }
                if (roster.getRbCount() == 1 && roster.getWrCount() > 2) {
                    rbLimit = 3 * x - 3.3333;
                    wrLimit = 4.75 * x - 6.3333;
                }
                if (roster.getRbCount() == 2 && roster.getWrCount() > 2) {
                    rbLimit = 4.5 * x - 5.33333;
                    wrLimit = 5.75 * x - 14;
                }
                // Guesstimated
                if (roster.getRbCount() == 1 && roster.getWrCount() == 1) {
                    rbLimit = 2 * x - 3.3333;
                    wrLimit = 2 * x - 1;
                }
                if (roster.getRbCount() == 1 && roster.getWrCount() == 2) {
                    rbLimit = 2.5 * x;
                    wrLimit = 4.25 * x - 5;
                }
                if (roster.getRbCount() == 2 && roster.getWrCount() == 1) {
                    rbLimit = 3.5 * x - 10;
                    wrLimit = 2.25 * x - 1;
                }
                if (roster.getRbCount() > 2 && roster.getWrCount() == 1) {
                    wrLimit = 2.5 * x + 1;
                    rbLimit = 4.7 * x - 5;
                }
                if (roster.getRbCount() > 2 && roster.getWrCount() == 2) {
                    rbLimit = 4.75 * x - 4.33333;
                    wrLimit = 4.25 * x;
                }
                if (roster.getRbCount() > 2 && roster.getWrCount() > 2) {
                    rbLimit = 4.75 * x - 1;
                    wrLimit = 5.75 * x - 12;
                }
            } else {
                // Legit
                if (roster.getRbCount() == 2 && roster.getWrCount() == 2) {
                    rbLimit = 2.75 * x + 6;
                    wrLimit = 4.25 * x - 7.3333;
                }
                if (roster.getRbCount() == 1 && roster.getWrCount() > 2) {
                    rbLimit = 2.5 * x + 3.3333;
                    wrLimit = 5.25 * x - 13;
                }
                if (roster.getRbCount() == 2 && roster.getWrCount() > 2) {
                    rbLimit = 4.5 * x - 5.3333;
                    wrLimit = 5.75 * x - 14;
                }
                // Guesstimated
                if (roster.getRbCount() == 1 && roster.getWrCount() == 1) {
                    rbLimit = 2 * x - 2;
                    wrLimit = 2 * x - 1.66667;
                }
                if (roster.getRbCount() == 1 && roster.getWrCount() == 2) {
                    rbLimit = 2.5 * x + 1;
                    wrLimit = 4.25 * x - 6;
                }
                if (roster.getRbCount() == 2 && roster.getWrCount() == 1) {
                    rbLimit = 3.5 * x - 9;
                    wrLimit = 2.25 * x - 1.666667;
                }
                if (roster.getRbCount() > 2 && roster.getWrCount() == 1) {
                    wrLimit = 2.5 * x + 1.5;
                    rbLimit = 4.7 * x - 3.6667;
                }
                if (roster.getRbCount() > 2 && roster.getWrCount() == 2) {
                    rbLimit = 4.75 * x - 3.666667;
                    wrLimit = 4.25 * x - 1;
                }
                if (roster.getRbCount() > 2 && roster.getWrCount() > 2) {
                    rbLimit = 4.75 * x;
                    wrLimit = 5.75 * x - 13;
                }
            }
        }
        if (flex != null && flex.getQbrbwrteCount() > 0) {
            if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
                rbLimit += x / 11.0;
                wrLimit += x / 10.0;
            } else {
                rbLimit += x / 10.0;
                wrLimit += x / 11.0;
            }
        }

        Log.d("PAA", "QB PAA limit: " + qbLimit);
        Log.d("PAA", "RB PAA limit: " + rbLimit);
        Log.d("PAA", "WR PAA limit: " + wrLimit);
        Log.d("PAA", "TE PAA limit: " + teLimit);
        Log.d("PAA", "DST PAA limit: " + dLimit);
        Log.d("PAA", "K PAA limit: " + kLimit);
    }

    public static void setPlayerXval(Rankings rankings) {
        setXValLimits(rankings);
        double qbTotal = getPositionalProjection(qbLimit, rankings.getQbs());
        double rbTotal = getPositionalProjection(rbLimit, rankings.getRbs());
        double wrTotal = getPositionalProjection(wrLimit, rankings.getWrs());
        double teTotal = getPositionalProjection(teLimit, rankings.getTes());
        double dTotal  = getPositionalProjection(dLimit,  rankings.getDsts());
        double kTotal  = getPositionalProjection(kLimit,  rankings.getKs());
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (Constants.QB.equals(player.getPosition())) {
                player.setxVal(player.getProjection() - qbTotal);
            } else if (Constants.RB.equals(player.getPosition())) {
                player.setxVal(player.getProjection() - rbTotal);
            } else if (Constants.WR.equals(player.getPosition())) {
                player.setxVal(player.getProjection() - wrTotal);
            } else if (Constants.TE.equals(player.getPosition())) {
                player.setxVal(player.getProjection() - teTotal);
            } else if (Constants.DST.equals(player.getPosition())) {
                player.setxVal(player.getProjection() - dTotal);
            } else if (Constants.K.equals(player.getPosition())) {
                player.setxVal(player.getProjection() - kTotal);
            }
        }
    }

    public static void setPlayerPAA(Rankings rankings) {
        setPAALimits(rankings);
        double qbTotal = getPositionalProjection(qbLimit, rankings.getQbs());
        double rbTotal = getPositionalProjection(rbLimit, rankings.getRbs());
        double wrTotal = getPositionalProjection(wrLimit, rankings.getWrs());
        double teTotal = getPositionalProjection(teLimit, rankings.getTes());
        double dTotal  = getPositionalProjection(dLimit,  rankings.getDsts());
        double kTotal  = getPositionalProjection(kLimit,  rankings.getKs());
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (Constants.QB.equals(player.getPosition())) {
                player.setPaa(player.getProjection() - qbTotal);
            } else if (Constants.RB.equals(player.getPosition())) {
                player.setPaa(player.getProjection() - rbTotal);
            } else if (Constants.WR.equals(player.getPosition())) {
                player.setPaa(player.getProjection() - wrTotal);
            } else if (Constants.TE.equals(player.getPosition())) {
                player.setPaa(player.getProjection() - teTotal);
            } else if (Constants.DST.equals(player.getPosition())) {
                player.setPaa(player.getProjection() - dTotal);
            } else if (Constants.K.equals(player.getPosition())) {
                player.setPaa(player.getProjection() - kTotal);
            }
        }
    }

    private static double getPositionalProjection(double limit, List<Player> players) {
        double posCounter;
        double posTotal = 0.0;
        PriorityQueue<Player> playerQueue = new PriorityQueue<>(300,
                new Comparator<Player>() {
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
                });
        playerQueue.addAll(players);
        int posCap = Math.min((int) limit, playerQueue.size());
        for (posCounter = 0; posCounter < posCap; posCounter++) {
            posTotal += playerQueue.poll().getProjection();
        }
        posTotal /= posCounter;
        return posTotal;
    }

    public static void getECRAuctionValue(Rankings rankings) {
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            player.handleNewValue(convertRanking(player.getEcr()));
        }
    }

    public static void getADPAuctionValue(Rankings rankings) {
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            player.handleNewValue(convertRanking(player.getAdp()));
        }
    }

    private static double convertRanking(double ranking) {
        double possVal = 78.6341 - 15.893 * Math.log(ranking);
        if (possVal < 1.0) {
            possVal = 1.0;
        }
        return possVal;
    }

    public static void getPAAAuctionValue(Rankings rankings) {
        double discretCash = getDiscretionaryCash(rankings.getLeagueSettings().getAuctionBudget(),
                rankings.getLeagueSettings().getRosterSettings());
        Map<String, Double> zMap = initZMap(rankings);
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            double possVal = paa1Calc(zMap, player, discretCash);
            player.handleNewValue(possVal);
        }
    }

    private static double paa1Calc(Map<String, Double> zMap,
                                  Player player, double discretCash) {
        double coeff = player.getPaa() / zMap.get(player.getPosition());
        double possVal = discretCash * coeff + 1.0;
        if (player.getPosition().equals(Constants.DST)) {
            possVal /= 10;
        }
        if (player.getPosition().equals(Constants.K)) {
            possVal /= 20;
        }
        if (possVal < 1.0) {
            possVal = 1.0;
        }
        return possVal;
    }

    private static Map<String, Double> initZMap(Rankings rankings) {
        Map<String, Double> zMap = new HashMap<>();
        zMap.put(Constants.QB, avgPAAPos(rankings.getQbs()));
        zMap.put(Constants.RB, avgPAAPos(rankings.getRbs()));
        zMap.put(Constants.WR, avgPAAPos(rankings.getWrs()));
        zMap.put(Constants.TE, avgPAAPos(rankings.getTes()));
        zMap.put(Constants.DST, avgPAAPos(rankings.getDsts()));
        zMap.put(Constants.K, avgPAAPos(rankings.getKs()));
        return zMap;
    }

    private static double avgPAAPos(Collection<Player> players) {
        double paaTotal = 0.0;
        double paaCount = 0.0;
        for (Player player : players) {
            if (player.getPaa() > 0.0) {
                paaTotal += player.getPaa();
                paaCount++;
            }
        }
        if ( paaCount == 0.0) {
            // Just to prevent divide by 0 madness if a projection breaks
            return 1.0;
        }
        return paaTotal / paaCount;
    }

    private static double getDiscretionaryCash(int auctionBudget, RosterSettings roster) {
        int rosterSize = roster.getRosterSize();
        return (auctionBudget - rosterSize) / (rosterSize - roster.getBenchCount());
    }

    public static double getLeverage(Player player, Rankings rankings) {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        Player topPlayer = null;
        double maxVal = 0.0;
        for (String key : rankings.getOrderedIds()) {
            Player possibleTop = rankings.getPlayer(key);
            if (possibleTop.getAuctionValue() > maxVal && possibleTop.getPosition().equals(player.getPosition())) {
                maxVal = possibleTop.getAuctionValue();
                topPlayer = possibleTop;
            }
        }
        return Double.parseDouble(df.format((player.getProjection() / topPlayer.getProjection()) /
                (player.getAuctionValueCustom(rankings) / topPlayer.getAuctionValueCustom(rankings))));
    }

    public static void getTiers(Rankings rankings) {
        // sort the input data
        List<Player> sortedQBs = getSortedPlayers(rankings.getQbs());
        List<Player> sortedRBs = getSortedPlayers(rankings.getRbs());
        List<Player> sortedWRs = getSortedPlayers(rankings.getWrs());
        List<Player> sortedTEs = getSortedPlayers(rankings.getTes());

        List<Set<Player>> qbTiers = getTiersInternal(sortedQBs, 1, 1, 1, 6.5, new ArrayList<Set<Player>>(), new HashSet<Player>());
        List<Set<Player>> rbTiers = getTiersInternal(sortedRBs, 1, 1, 1, 6.5, new ArrayList<Set<Player>>(), new HashSet<Player>());
        List<Set<Player>> wrTiers = getTiersInternal(sortedWRs, 1, 1, 1, 6.5, new ArrayList<Set<Player>>(), new HashSet<Player>());
        List<Set<Player>> teTiers = getTiersInternal(sortedTEs, 1, 1, 1, 6.5, new ArrayList<Set<Player>>(), new HashSet<Player>());

        setTiers(rankings, qbTiers);
        setTiers(rankings, rbTiers);
        setTiers(rankings, wrTiers);
        setTiers(rankings, teTiers);
    }

    private static void setTiers(Rankings rankings, List<Set<Player>> tierList) {
        for (int i = 0; i < tierList.size(); i++) {
            Set<Player> currTier = tierList.get(i);
            int tierId = i + 1;
            for (Player player : currTier) {
                player.setPositionalTier(tierId);
            }
        }
    }

    private static List<Set<Player>> getTiersInternal(List<Player> sorted, int currIndex, int tierCount, int tierTotal,
                                         double tierThreshold, List<Set<Player>> tierSet, Set<Player> currTier) {
        Player playerA = sorted.get(currIndex - 1);
        Player playerB = sorted.get(currIndex);
        double diff = playerB.getEcr() - playerA.getEcr();
        double portion = (diff / playerA.getEcr()) * 100.0;
        currTier.add(playerA);
        boolean newTier = false;
        if (portion > tierThreshold) {
            if (tierCount == 1 || (tierCount > 1&& tierTotal > 1)) {
                tierCount++;
                tierTotal = 0;
                tierSet.add(currTier);
                newTier = true;
            } else {
                tierTotal++;
                currTier.add(playerB);
            }
        } else {
            tierTotal++;
            currTier.add(playerB);
        }
        if (currIndex + 1 >= sorted.size()) {
            tierSet.add(currTier);
            return tierSet;
        }
        return getTiersInternal(sorted, ++currIndex, tierCount, tierTotal, tierThreshold, tierSet,
                newTier ? new HashSet<Player>() : currTier);
    }

    private static List<Player> getSortedPlayers(List<Player> pos) {
        PriorityQueue<Player> sorted = new PriorityQueue<Player>(
                100, new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getEcr() > b.getEcr()) {
                    return 1;
                }
                if (a.getEcr() < b.getEcr()) {
                    return -1;
                }
                return 0;
            }
        });
        sorted.addAll(pos);
        List<Player> results = new ArrayList<>();
        while (!sorted.isEmpty()) {
            results.add(sorted.poll());
        }
        return results;
    }
}
