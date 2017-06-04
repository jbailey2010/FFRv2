package com.devingotaswitch.rankings.domain;

import com.devingotaswitch.utils.Constants;

import java.util.UUID;

public class ScoringSettings {

    private String id;

    private int passingTds;
    private int rushingTds;
    private int receivingTds;

    private int fumbles;
    private int interceptions;

    private int passingYards;
    private int rushingYards;
    private int receivingYards;

    private double receptions;

    public ScoringSettings() {
        this(UUID.randomUUID().toString(), Constants.DEFAULT_TD_WORTH, Constants.DEFAULT_TD_WORTH, Constants.DEFAULT_TD_WORTH,
                Constants.DEFAULT_TURNOVER_WORTH, Constants.DEFAULT_TURNOVER_WORTH, Constants.DEFAULT_PASSING_YDS, Constants.DEFAULT_RUSHING_YDS,
                Constants.DEFAULT_RECEIVING_YDS, Constants.DEFAULT_RECEPTIONS);
    }

    public ScoringSettings(String id, int passingTds, int rushingTds, int receivingTds, int fumbles,
                           int interceptions, int passingYds, int rushingYds, int receivingYds, double receptions) {
        this.setId(id);
        this.setPassingTds(passingTds);
        this.setRushingTds(rushingTds);
        this.setReceivingTds(receivingTds);
        this.setFumbles(fumbles);
        this.setInterceptions(interceptions);
        this.setPassingYards(passingYds);
        this.setRushingYards(rushingYds);
        this.setReceivingYards(receivingYds);
        this.setReceptions(receptions);
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

    public double getReceptions() {
        return receptions;
    }

    public void setReceptions(double receptions) {
        this.receptions = receptions;
    }
}
