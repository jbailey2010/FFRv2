package com.devingotaswitch.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.RosterSettings.Flex;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.rankings.domain.Team;

import java.util.Map;

public class DBUtils {

    public static String getSelectAllString(String tableName) {
        return new StringBuilder("SELECT * FROM ")
                .append(tableName)
                .toString();
    }

    public static String getSelectSingleString(String tableName, String idColumn, String idValue) {
        return new StringBuilder("SELECT * FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(idColumn)
                .append(" = ")
                .append(idValue)
                .toString();
    }

    public static String getDeleteAllString(String tableName) {
        return new StringBuilder("DELETE * FROM ")
                .append(tableName)
                .toString();
    }

    public static String getUpdateAndDeleteKeyString(String idColumn) {
        return new StringBuilder(idColumn)
                .append(" = ?")
                .toString();
    }

    public static String getMultiKeyUpdateAndDeleteKeyString(String columnOne, String columnTwo) {
        return new StringBuilder(columnOne)
                .append(" = ? AND")
                .append(columnTwo)
                .append(" = ?")
                .toString();
    }

    public static ContentValues updatedValuesToContentValues(Map<String, String> updatedValues) {
        ContentValues values = new ContentValues();
        for (String key : updatedValues.keySet()) {
            values.put(key, updatedValues.get(key));
        }
        return values;
    }

    public static ContentValues leagueToContentValues(LeagueSettings league) {
        ContentValues values = new ContentValues();
        values.put(Constants.LEAGUE_ID_COLUMN, league.getId());
        values.put(Constants.NAME_COLUMN, league.getName());
        values.put(Constants.TEAM_COUNT_COLUMN, league.getTeamCount());
        values.put(Constants.IS_AUCTION_COLUMN, league.isAuction());
        values.put(Constants.AUCTION_BUDGET_COLUMN, league.getAuctionBudget());
        values.put(Constants.SCORING_ID_COLUMN, league.getScoringSettings().getId());
        values.put(Constants.ROSTER_ID_COLUMN, league.getRosterSettings().getId());
        return values;
    }

    public static LeagueSettings cursorToLeague(Cursor result, RosterSettings roster, ScoringSettings scoring) {
        return new LeagueSettings(
                result.getString(result.getColumnIndex(Constants.LEAGUE_ID_COLUMN)),
                result.getString(result.getColumnIndex(Constants.NAME_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.TEAM_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.IS_AUCTION_COLUMN)) > 0,
                result.getInt(result.getColumnIndex(Constants.AUCTION_BUDGET_COLUMN)),
                scoring,
                roster
        );
    }

    public static ContentValues scoringToContentValues(ScoringSettings scoring) {
        ContentValues values = new ContentValues();
        values.put(Constants.SCORING_ID_COLUMN, scoring.getId());
        values.put(Constants.PASSING_TDS_COLUMN, scoring.getPassingTds());
        values.put(Constants.RUSHING_TDS_COLUMN, scoring.getRushingTds());
        values.put(Constants.RECEIVING_TDS_COLUMN, scoring.getReceivingTds());
        values.put(Constants.FUMBLES_COLUMN, scoring.getFumbles());
        values.put(Constants.INTERCEPTIONS_COLUMN, scoring.getInterceptions());
        values.put(Constants.PASSING_YARDS_COLUMN, scoring.getPassingYards());
        values.put(Constants.RUSHING_YARDS_COLUMN, scoring.getRushingYards());
        values.put(Constants.RECEIVING_YARDS_COLUMN, scoring.getReceivingYards());
        values.put(Constants.RECEPTIONS_COLUMN, scoring.getReceptions());
        return values;
    }

    public static ScoringSettings cursorToScoring(Cursor result) {
        return new ScoringSettings(
                result.getString(result.getColumnIndex(Constants.SCORING_ID_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.PASSING_TDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RUSHING_TDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RECEIVING_TDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.FUMBLES_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.INTERCEPTIONS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.PASSING_YARDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RUSHING_YARDS_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RECEIVING_YARDS_COLUMN)),
                result.getDouble(result.getColumnIndex(Constants.RECEPTIONS_COLUMN))
        );
    }
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
    public static ContentValues rosterToContentValues(RosterSettings roster) {
        ContentValues values = new ContentValues();
        values.put(Constants.ROSTER_ID_COLUMN, roster.getId());
        values.put(Constants.QB_COUNT_COLUMN, roster.getQbCount());
        values.put(Constants.RB_COUNT_COLUMN, roster.getRbCount());
        values.put(Constants.WR_COUNT_COLUMN, roster.getWrCount());
        values.put(Constants.TE_COUNT_COLUMN, roster.getTeCount());
        values.put(Constants.DST_COUNT_COLUMN, roster.getDstCount());
        values.put(Constants.K_COUNT_COLUMN, roster.getkCount());
        values.put(Constants.BENCH_COUNT_COLUMN, roster.getBenchCount());
        values.put(Constants.RBWR_COUNT_COLUMN, roster.getFlex().getRbwrCount());
        values.put(Constants.RBTE_COUNT_COLUMN, roster.getFlex().getRbteCount());
        values.put(Constants.RBWRTE_COUNT_COLUMN, roster.getFlex().getRbwrteCount());
        values.put(Constants.WRTE_COUNT_COLUMN, roster.getFlex().getWrteCount());
        values.put(Constants.QBRBWRTE_COUNT_COLUMN, roster.getFlex().getQbrbwrteCount());
        return values;
    }

    public static RosterSettings cursorToRoster(Cursor result) {
        Flex flex = new Flex();
        flex.setRbwrCount(result.getInt(result.getColumnIndex(Constants.RBWR_COUNT_COLUMN)));
        flex.setRbteCount(result.getInt(result.getColumnIndex(Constants.RBTE_COUNT_COLUMN)));
        flex.setRbwrteCount(result.getInt(result.getColumnIndex(Constants.RBWRTE_COUNT_COLUMN)));
        flex.setWrteCount(result.getInt(result.getColumnIndex(Constants.WRTE_COUNT_COLUMN)));
        flex.setQbrbwrteCount(result.getInt(result.getColumnIndex(Constants.QBRBWRTE_COUNT_COLUMN)));

        return new RosterSettings(
                result.getString(result.getColumnIndex(Constants.ROSTER_ID_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.QB_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.RB_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.WR_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.TE_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.DST_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.K_COUNT_COLUMN)),
                result.getInt(result.getColumnIndex(Constants.BENCH_COUNT_COLUMN)),
                flex
                );
    }

    public static ContentValues teamToContentValues(Team team) {
        ContentValues values = new ContentValues();
        values.put(Constants.TEAM_NAME_COLUMN, team.getName());
        values.put(Constants.OLINE_RANKS_COLUMN, team.getoLineRanks());
        values.put(Constants.DRAFT_CLASS_COLUMN, team.getDraftClass());
        values.put(Constants.QB_SOS_COLUMN, team.getQbSos());
        values.put(Constants.RB_SOS_COLUMN, team.getRbSos());
        values.put(Constants.WR_SOS_COLUMN, team.getWrSos());
        values.put(Constants.TE_SOS_COLUMN, team.getTeSos());
        values.put(Constants.DST_SOS_COLUMN, team.getDstSos());
        values.put(Constants.K_SOS_COLUMN, team.getkSos());
        values.put(Constants.BYE_COLUMN, team.getBye());
        values.put(Constants.FREE_AGENCY_COLUMN, team.getFaClass());
        return values;
    }

    public static Team cursorToTeam(Cursor result) {
        Team team = new Team();
        team.setName(result.getString(result.getColumnIndex(Constants.TEAM_NAME_COLUMN)));
        team.setoLineRanks(result.getString(result.getColumnIndex(Constants.OLINE_RANKS_COLUMN)));
        team.setDraftClass(result.getString(result.getColumnIndex(Constants.DRAFT_CLASS_COLUMN)));
        team.setQbSos(result.getInt(result.getColumnIndex(Constants.QB_SOS_COLUMN)));
        team.setRbSos(result.getInt(result.getColumnIndex(Constants.RB_SOS_COLUMN)));
        team.setWrSos(result.getInt(result.getColumnIndex(Constants.WR_SOS_COLUMN)));
        team.setTeSos(result.getInt(result.getColumnIndex(Constants.TE_SOS_COLUMN)));
        team.setDstSos(result.getInt(result.getColumnIndex(Constants.DST_SOS_COLUMN)));
        team.setkSos(result.getInt(result.getColumnIndex(Constants.K_SOS_COLUMN)));
        team.setBye(result.getString(result.getColumnIndex(Constants.BYE_COLUMN)));
        team.setFaClass(result.getString(result.getColumnIndex(Constants.FREE_AGENCY_COLUMN)));
        return team;
    }
}