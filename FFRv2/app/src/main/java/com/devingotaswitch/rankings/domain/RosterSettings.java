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

    public RosterSettings() {
        this.setId(UUID.randomUUID().toString());

        this.setQbCount(1);
        this.setRbCount(2);
        this.setWrCount(2);
        this.setTeCount(1);
        this.setDstCount(1);
        this.setkCount(1);
        this.setBenchCount(6);

        Flex flex = new Flex();
        flex.setRbteCount(0);
        flex.setRbwrCount(1);
        flex.setRbteCount(0);
        flex.setWrrbteCount(0);
        flex.setQbrbwrteCount(0);
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

    class Flex {

        private int rbwrCount;
        private int wrrbteCount;
        private int rbteCount;
        private int wrteCount;
        private int qbrbwrteCount;

        public int getRbwrCount() {
            return rbwrCount;
        }

        public void setRbwrCount(int rbwrCount) {
            this.rbwrCount = rbwrCount;
        }

        public int getWrrbteCount() {
            return wrrbteCount;
        }

        public void setWrrbteCount(int wrrbteCount) {
            this.wrrbteCount = wrrbteCount;
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
