package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.RosterSettings.Flex;

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
        if (roster.getQbCount() == 1 && flex.getQbrbwrteCount() == 0) {
            qbLimit = (1.25 * x) + 1.33333;
        } else if (roster.getQbCount() == 0 && flex.getQbrbwrteCount() == 1) {
            qbLimit = (1.25 * x);
        } else if (roster.getQbCount() >= 2 || flex.getQbrbwrteCount() >= 2) {
            qbLimit = (6 * x) - 30;
        } else if (roster.getQbCount() == 1 && flex.getQbrbwrteCount() == 1) {
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
        if (flex.getRbwrCount() > 0 || flex.getRbwrteCount() > 0) {
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
        if (flex.getQbrbwrteCount() > 0) {
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

        // TODO: Finish
    }
}
