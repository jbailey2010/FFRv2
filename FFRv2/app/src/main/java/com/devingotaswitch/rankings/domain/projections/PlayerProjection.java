package com.devingotaswitch.rankings.domain.projections;

import android.util.Log;

import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PlayerProjection {

    private static final Gson GSON = new GsonBuilder().create();

    private PassingProjection passingProjection;
    private RushingProjection rushingProjection;
    private ReceivingProjection receivingProjection;
    private DefensiveProjection defensiveProjection;
    private KickingProjection kickingProjection;

    private double projection;

    public PlayerProjection(ScoringSettings scoringSettings) {
        this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, scoringSettings);
    }

    public PlayerProjection(double passingYds, double passingTds, double rushingYds, double rushingTds,
                            double receivingYds, double receivingTds, double receptions, double fumbles,
                            double ints, double def, double k, ScoringSettings scoringSettings) {
        this.passingProjection = new PassingProjection(passingYds, passingTds, ints);
        this.rushingProjection = new RushingProjection(rushingYds, rushingTds, fumbles);
        this.receivingProjection = new ReceivingProjection(receivingYds, receptions, receivingTds);
        this.defensiveProjection = new DefensiveProjection(def);
        this.kickingProjection = new KickingProjection(k);

        updateAndGetFormattedProjectedPoints(scoringSettings);
    }

    public double getProjectedPoints(ScoringSettings scoringSettings) {
        return this.passingProjection.getProjectedPoints(scoringSettings) +
                this.rushingProjection.getProjectedPoints(scoringSettings) +
                this.receivingProjection.getProjectedPoints(scoringSettings) +
                this.defensiveProjection.getProjectedPoints(scoringSettings) +
                this.kickingProjection.getProjectedPoints(scoringSettings);
    }

    public double updateAndGetFormattedProjectedPoints(ScoringSettings scoringSettings) {
        this.projection = Double.parseDouble(Constants.DECIMAL_FORMAT.format(getProjectedPoints(scoringSettings)));
        return this.projection;
    }

    public double getFormattedProjectedPoints() {
        return this.projection;
    }

    public PassingProjection getPassingProjection() {
        return passingProjection;
    }
    public RushingProjection getRushingProjection() {
        return rushingProjection;
    }
    public ReceivingProjection getReceivingProjection() {
        return receivingProjection;
    }

    public String getDisplayString(String position) {
        switch (position) {
            case Constants.QB:
                return new StringBuilder()
                        .append(this.passingProjection.getDisplayString())
                        .append(Constants.LINE_BREAK)
                        .append(this.rushingProjection.getDisplayString())
                        .toString();
            case Constants.RB:
                return new StringBuilder()
                        .append(this.rushingProjection.getDisplayString())
                        .append(Constants.LINE_BREAK)
                        .append(this.receivingProjection.getDisplayString())
                        .toString();
            case Constants.WR:
                return new StringBuilder()
                        .append(this.receivingProjection.getDisplayString())
                        .append(Constants.LINE_BREAK)
                        .append(this.rushingProjection.getDisplayString())
                        .toString();
            case Constants.TE:
                return this.receivingProjection.getDisplayString();
            case Constants.DST:
                return this.defensiveProjection.getDisplayString();
            case Constants.K:
                return this.kickingProjection.getDisplayString();
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return GSON.toJson(this, PlayerProjection.class);
    }

    public static PlayerProjection fromJson(String json) {
        return GSON.fromJson(json, PlayerProjection.class);
    }
}
