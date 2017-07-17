package com.devingotaswitch.rankings.domain;

import android.util.Log;

import com.devingotaswitch.utils.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RosterSettings {

    private String id;
    private int qbCount;
    private int rbCount;
    private int wrCount;
    private int teCount;
    private int dstCount;
    private int kCount;
    private int benchCount;
    private Flex flex;

    private Set<String> validPositions;

    public RosterSettings() {
        this(UUID.randomUUID().toString(), Constants.ONE_STARTER, Constants.TWO_STARTERS, Constants.TWO_STARTERS, Constants.ONE_STARTER, Constants.ONE_STARTER,
                Constants.ONE_STARTER, Constants.BENCH_DEFAULT, new Flex());
    }

    public RosterSettings(int qbCt, int rbCt, int wrCt, int teCt, int dCt, int kCt,
                          int benchCt) {
        this(UUID.randomUUID().toString(), qbCt, rbCt, wrCt, teCt, dCt, kCt, benchCt, new Flex());
    }

    public RosterSettings(String id, int qbCt, int rbCt, int wrCt, int teCt, int dCt, int kCt,
                          int benchCt, Flex flex) {
        this.setId(id);
        this.setQbCount(qbCt);
        this.setRbCount(rbCt);
        this.setWrCount(wrCt);
        this.setTeCount(teCt);
        this.setDstCount(dCt);
        this.setkCount(kCt);
        this.setBenchCount(benchCt);
        this.setFlex(flex);

        this.validPositions = new HashSet<>();
        if (qbCt > 0 || flex.getQbrbwrteCount() > 0) {
            validPositions.add(Constants.QB);
        }
        if (rbCt > 0 || flex.getRbwrCount() > 0 || flex.getRbwrteCount() > 0 || flex.getRbteCount() > 0 || flex.getQbrbwrteCount() > 0) {
            validPositions.add(Constants.RB);
        }
        if (wrCt > 0 || flex.getRbwrCount() > 0 || flex.getRbwrteCount() > 0 || flex.getWrteCount() > 0 || flex.getQbrbwrteCount() > 0) {
            validPositions.add(Constants.WR);
        }
        if (teCt > 0 || flex.getRbwrteCount() > 0 || flex.getRbteCount() > 0 || flex.getWrteCount() > 0 || flex.getQbrbwrteCount() > 0) {
            validPositions.add(Constants.TE);
        }
        if (dCt > 0) {
            validPositions.add(Constants.DST);
        }
        if (kCt > 0) {
            validPositions.add(Constants.K);
        }
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public int getQbCount() {
        return qbCount;
    }

    public void setQbCount(int qbCount) {
        this.qbCount = qbCount;
    }

    public int getRbCount() {
        return rbCount;
    }

    public void setRbCount(int rbCount) {
        this.rbCount = rbCount;
    }

    public int getWrCount() {
        return wrCount;
    }

    public void setWrCount(int wrCount) {
        this.wrCount = wrCount;
    }

    public int getTeCount() {
        return teCount;
    }

    public void setTeCount(int teCount) {
        this.teCount = teCount;
    }

    public int getDstCount() {
        return dstCount;
    }

    public void setDstCount(int dstCount) {
        this.dstCount = dstCount;
    }

    public int getkCount() {
        return kCount;
    }

    public void setkCount(int kCount) {
        this.kCount = kCount;
    }

    public int getBenchCount() {
        return benchCount;
    }

    public void setBenchCount(int benchCount) {
        this.benchCount = benchCount;
    }

    public Flex getFlex() {
        return flex;
    }

    public void setFlex(Flex flex) {
        this.flex = flex;
    }

    public int getRosterSize() {
        int size = qbCount + rbCount + wrCount + teCount + dstCount + kCount + benchCount;
        if (flex != null) {
            size += flex.getQbrbwrteCount() + flex.getRbwrteCount() + flex.getRbteCount() + flex.getRbwrCount() + flex.getWrteCount();
        }
        return size;
    }

    public int getNumberStartedOfPos(String position) {
        int total = 0;
        if (Constants.QB.equals(position)) {
            total = getQbCount();
            if (flex != null) {
                // Assume all qbs
                total += flex.getQbrbwrteCount();
            }
        } else if (Constants.RB.equals(position)) {
            total = getRbCount();
            if (flex != null) {
                total += flex.getRbteCount();
                total += flex.getRbwrCount();
                total += flex.getRbwrteCount();
            }
        } else if (Constants.WR.equals(position)) {
            total = getWrCount();
            if (flex != null) {
                total += flex.getWrteCount();
                total += flex.getRbwrCount();
                total += flex.getRbwrteCount();
            }
        } else if (Constants.TE.equals(position)) {
            total = getTeCount();
            // Assume no flex spot would go to a te
        } else if (Constants.DST.equals(position)) {
            total = getDstCount();
        } else if (Constants.K.equals(position)) {
            total = getkCount();
        }

        return total;
    }

    public boolean isPositionValid(String pos) {
        return validPositions.contains(pos);
    }

    public static class Flex {

        private int rbwrCount;
        private int rbwrteCount;
        private int rbteCount;
        private int wrteCount;
        private int qbrbwrteCount;

        public Flex() {
            this(Constants.ONE_STARTER, Constants.NO_STARTERS, Constants.NO_STARTERS, Constants.NO_STARTERS, Constants.NO_STARTERS);
        }

        public Flex(int rbwr, int rbwrte, int rbte, int wrte, int qbrbwrte) {
            this.setRbwrCount(rbwr);
            this.setRbteCount(rbte);
            this.setRbwrteCount(rbwrte);
            this.setWrteCount(wrte);
            this.setQbrbwrteCount(qbrbwrte);
        }

        public int getRbwrCount() {
            return rbwrCount;
        }

        public void setRbwrCount(int rbwrCount) {
            this.rbwrCount = rbwrCount;
        }

        public int getRbwrteCount() {
            return rbwrteCount;
        }

        public void setRbwrteCount(int rbwrteCount) {
            this.rbwrteCount = rbwrteCount;
        }

        public int getRbteCount() {
            return rbteCount;
        }

        public void setRbteCount(int rbteCount) {
            this.rbteCount = rbteCount;
        }

        public int getWrteCount() {
            return wrteCount;
        }

        public void setWrteCount(int wrteCount) {
            this.wrteCount = wrteCount;
        }

        public int getQbrbwrteCount() {
            return qbrbwrteCount;
        }

        public void setQbrbwrteCount(int qbrbwrteCount) {
            this.qbrbwrteCount = qbrbwrteCount;
        }
    }
}
