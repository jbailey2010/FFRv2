package com.devingotaswitch.rankings.domain;

import java.util.UUID;

public class RosterSettings {
    private static final String TABLE_NAME = "roster_settings";
    private static final String ID_COLUMN = "roster_id";
    private static final String QB_COUNT_COLUMN = "starting_qbs";
    private static final String RB_COUNT_COLUMN = "starting_rbs";
    private static final String WR_COUNT_COLUMN = "starting_wrs";
    private static final String TE_COUNT_COLUMN = "starting_tes";
    private static final String DST_COUNT_COLUMN = "starting_dsts";
    private static final String K_COUNT_COLUMN = "starting_ks";
    private static final String BENCH_COUNT_COLUMN = "bench_count";
    private static final String RBWR_COUNT_COLUMN = "starting_rbwr";
    private static final String RBTE_COUNT_COLUMN = "starting_rbte";
    private static final String RBWRTE_COUNT_COLUMN = "starting_rbwrte";
    private static final String WRTE_COUNT_COLUMN = "starting_wrte";
    private static final String QBRBWRTE_COUNT_COLUMN = "starting_qbrbwrte";

    private String id;
    private int qbCount;
    private int rbCount;
    private int wrCount;
    private int teCount;
    private int dstCount;
    private int kCount;
    private int benchCount;
    private Flex flex;

    private static final Integer NO_STARTERS = 0;
    private static final Integer ONE_STARTER = 1;
    private static final Integer TWO_STARTERS = 2;
    private static final Integer BENCH_DEFAULT = 6;

    public RosterSettings() {
        this(UUID.randomUUID().toString(), ONE_STARTER, TWO_STARTERS, TWO_STARTERS, ONE_STARTER, ONE_STARTER,
                ONE_STARTER, BENCH_DEFAULT, new Flex());
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

    private void setFlex(Flex flex) {
        this.flex = flex;
    }

    public static String getCreateTableSQL() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                ID_COLUMN              + " TEXT PRIMARY KEY," +
                QB_COUNT_COLUMN        + " INTEGER," +
                RB_COUNT_COLUMN        + " INTEGER," +
                WR_COUNT_COLUMN        + " INTEGER," +
                TE_COUNT_COLUMN        + " INTEGER," +
                DST_COUNT_COLUMN       + " INTEGER," +
                K_COUNT_COLUMN         + " INTEGER," +
                BENCH_COUNT_COLUMN     + " INTEGER," +
                RBWR_COUNT_COLUMN      + " INTEGER," +
                RBTE_COUNT_COLUMN      + " INTEGER," +
                RBWRTE_COUNT_COLUMN    + " INTEGER," +
                WRTE_COUNT_COLUMN      + " INTEGER," +
                QBRBWRTE_COUNT_COLUMN  + " INTEGER);";
    }

    static class Flex {

        private int rbwrCount;
        private int rbwrteCount;
        private int rbteCount;
        private int wrteCount;
        private int qbrbwrteCount;

        public Flex() {
            this(ONE_STARTER, NO_STARTERS, NO_STARTERS, NO_STARTERS, NO_STARTERS);
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