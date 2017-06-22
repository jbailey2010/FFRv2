package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.RosterSettings.Flex;
import com.devingotaswitch.utils.Constants;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ParseMath {
    private static double qbLimit;
    private static double rbLimit;
    private static double wrLimit;
    private static double teLimit;
    private static double dLimit;
    private static double kLimit;

    private static void setLimits(Rankings rankings) {
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

    public static void setPlayerPAA(Rankings rankings) {
        setLimits(rankings);

        double qbCounter = 0.0;
        double rbCounter = 0.0;
        double wrCounter = 0.0;
        double teCounter = 0.0;
        double dCounter = 0.0;
        double kCounter = 0.0;
        double qbTotal = 0.0;
        double rbTotal = 0.0;
        double wrTotal = 0.0;
        double teTotal = 0.0;
        double dTotal = 0.0;
        double kTotal = 0.0;
        PriorityQueue<Player> qb = new PriorityQueue<>(300,
                new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getAuctionValue() > b.getAuctionValue()) {
                            return -1;
                        }
                        if (a.getAuctionValue() < b.getAuctionValue()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        PriorityQueue<Player> rb = new PriorityQueue<>(300,
                new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getAuctionValue() > b.getAuctionValue()) {
                            return -1;
                        }
                        if (a.getAuctionValue() < b.getAuctionValue()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        PriorityQueue<Player> wr = new PriorityQueue<>(300,
                new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getAuctionValue() > b.getAuctionValue()) {
                            return -1;
                        }
                        if (a.getAuctionValue() < b.getAuctionValue()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        PriorityQueue<Player> te = new PriorityQueue<>(300,
                new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getAuctionValue() > b.getAuctionValue()) {
                            return -1;
                        }
                        if (a.getAuctionValue() < b.getAuctionValue()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        PriorityQueue<Player> def = new PriorityQueue<>(300,
                new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getAuctionValue() > b.getAuctionValue()) {
                            return -1;
                        }
                        if (a.getAuctionValue() < b.getAuctionValue()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        PriorityQueue<Player> k = new PriorityQueue<>(300,
                new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getAuctionValue() > b.getAuctionValue()) {
                            return -1;
                        }
                        if (a.getAuctionValue() < b.getAuctionValue()) {
                            return 1;
                        }
                        return 0;
                    }
                });
        qb.addAll(rankings.getQbs());
        rb.addAll(rankings.getRbs());
        wr.addAll(rankings.getWrs());
        te.addAll(rankings.getTes());
        def.addAll(rankings.getDsts());
        k.addAll(rankings.getKs());

        int qbCap = Math.min((int) qbLimit, qb.size());
        for (qbCounter = 0; qbCounter < qbCap; qbCounter++) {
            qbTotal += qb.poll().getProjection();
        }
        int rbCap = Math.min((int) rbLimit, rb.size());
        for (rbCounter = 0; rbCounter < rbCap; rbCounter++) {
            rbTotal += rb.poll().getProjection();
        }
        int wrCap = Math.min((int) wrLimit, wr.size());
        for (wrCounter = 0; wrCounter < wrCap; wrCounter++) {
            wrTotal += wr.poll().getProjection();
        }
        int teCap = Math.min((int) teLimit, te.size());
        for (teCounter = 0; teCounter < teCap; teCounter++) {
            teTotal += te.poll().getProjection();
        }
        int dCap = Math.min((int) dLimit, def.size());
        for (dCounter = 0; dCounter < dCap; dCounter++) {
            dTotal += def.poll().getProjection();
        }
        int kCap = Math.min((int) kLimit, k.size());
        for (kCounter = 0; kCounter < kCap; kCounter++) {
            kTotal += k.poll().getProjection();
        }
        qbTotal /= qbCounter;
        rbTotal /= rbCounter;
        wrTotal /= wrCounter;
        teTotal /= teCounter;
        dTotal /= dCounter;
        kTotal /= kCounter;
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
            if (possVal > 1.0) {
                Log.d("PAAAuc", key + ": " + player.getAuctionValue() + " - " + possVal);
            }
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
        return paaTotal / paaCount;
    }

    private static double getDiscretionaryCash(int auctionBudget, RosterSettings roster) {
        int rosterSize = roster.getRosterSize();
        return (auctionBudget - rosterSize) / rosterSize;
    }
}
