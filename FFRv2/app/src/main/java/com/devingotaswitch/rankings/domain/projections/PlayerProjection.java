package com.devingotaswitch.rankings.domain.projections;

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

    public PlayerProjection(double passingYds, double passingTds, double rushingYds, double rushingTds,
                            double receivingYds, double receivingTds, double receptions, double fumbles,
                            double ints, double def, double k) {
        this.passingProjection = new PassingProjection(passingYds, passingTds, ints);
        this.rushingProjection = new RushingProjection(rushingYds, rushingTds, fumbles);
        this.receivingProjection = new ReceivingProjection(receivingYds, receptions, receivingTds);
        this.defensiveProjection = new DefensiveProjection(def);
        this.kickingProjection = new KickingProjection(k);
    }

    private double getProjectedPoints(ScoringSettings scoringSettings) {
        return this.passingProjection.getProjectedPoints(scoringSettings) +
                this.rushingProjection.getProjectedPoints(scoringSettings) +
                this.receivingProjection.getProjectedPoints(scoringSettings) +
                this.defensiveProjection.getProjectedPoints(scoringSettings) +
                this.kickingProjection.getProjectedPoints(scoringSettings);
    }

    public double getFormattedProjectedPoints(ScoringSettings scoringSettings) {
        return Double.parseDouble(Constants.DECIMAL_FORMAT.format(getProjectedPoints(scoringSettings)));
    }

    public String toString() {
        return GSON.toJson(this, PlayerProjection.class);
    }

    public static PlayerProjection fromJson(String json) {
        return GSON.fromJson(json, PlayerProjection.class);
    }
}
