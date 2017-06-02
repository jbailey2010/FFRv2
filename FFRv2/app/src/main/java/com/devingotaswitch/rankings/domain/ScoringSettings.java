package com.devingotaswitch.rankings.domain;

import java.util.UUID;

public class ScoringSettings {

    private static final String TABLE_NAME = "scoring_settings";
    private static final String ID_COLUMN = "scoring_id";
    private static final String PASSING_TDS_COLUMN = "pts_per_passing_td";
    private static final String RUSHING_TDS_COLUMN = "pts_per_rushing_td";
    private static final String RECEIVING_TDS_COLUMN = "pts_per_receiving_td";
    private static final String FUMBLES_COLUMN = "pts_per_fumble";
    private static final String INTERCEPTIONS_COLUMN = "pts_per_interception";
    private static final String PASSING_YARDS_COLUMN = "passing_yards_per_point";
    private static final String RUSHING_YARDS_COLUMN = "rushing_yards_per_point";
    private static final String RECEIVING_YARDS_COLUMN = "receiving_yards_per_point";

    private String id;

    private int passingTds;
    private int rushingTds;
    private int receivingTds;

    private int fumbles;
    private int interceptions;

    private int passingYards;
    private int rushingYards;
    private int receivingYards;

    private int receptions;

    public ScoringSettings() {
        this.setId(UUID.randomUUID().toString());

        this.setPassingTds(6);
        this.setRushingTds(6);
        this.setReceivingTds(6);

        this.setFumbles(-2);
        this.setInterceptions(-2);

        this.setPassingYards(25);
        this.setRushingYards(10);
        this.setReceivingYards(10);

        this.setReceptions(1);
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public int getPassingTds() {
        return passingTds;
    }

    public void setPassingTds(int passingTds) {
        this.passingTds = passingTds;
    }

    public int getRushingTds() {
        return rushingTds;
    }

    public void setRushingTds(int rushingTds) {
        this.rushingTds = rushingTds;
    }

    public int getReceivingTds() {
        return receivingTds;
    }

    public void setReceivingTds(int receivingTds) {
        this.receivingTds = receivingTds;
    }

    public int getFumbles() {
        return fumbles;
    }

    public void setFumbles(int fumbles) {
        this.fumbles = fumbles;
    }

    public int getInterceptions() {
        return interceptions;
    }

    public void setInterceptions(int interceptions) {
        this.interceptions = interceptions;
    }

    public int getPassingYards() {
        return passingYards;
    }

    public void setPassingYards(int passingYards) {
        this.passingYards = passingYards;
    }

    public int getRushingYards() {
        return rushingYards;
    }

    public void setRushingYards(int rushingYards) {
        this.rushingYards = rushingYards;
    }

    public int getReceivingYards() {
        return receivingYards;
    }

    public void setReceivingYards(int receivingYards) {
        this.receivingYards = receivingYards;
    }

    public int getReceptions() {
        return receptions;
    }

    public void setReceptions(int receptions) {
        this.receptions = receptions;
    }

    public static String getCreateTableSQL() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                ID_COLUMN              + " TEXT PRIMARY KEY," +
                PASSING_TDS_COLUMN     + " INTEGER," +
                RUSHING_TDS_COLUMN     + " INTEGER," +
                RECEIVING_TDS_COLUMN   + " INTEGER," +
                FUMBLES_COLUMN         + " INTEGER," +
                INTERCEPTIONS_COLUMN   + " INTEGER," +
                PASSING_YARDS_COLUMN   + " INTEGER," +
                RUSHING_YARDS_COLUMN   + " INTEGER," +
                RECEIVING_YARDS_COLUMN + " INTEGER);";
    }
}
