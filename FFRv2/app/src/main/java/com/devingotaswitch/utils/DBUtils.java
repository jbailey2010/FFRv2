package com.devingotaswitch.utils;

import android.content.ContentValues;
import android.database.Cursor;

import com.devingotaswitch.rankings.domain.Team;

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

    public static Team cursorToTeam(Cursor results) {
        Team team = new Team();
        team.setName(results.getString(results.getColumnIndex(Constants.TEAM_NAME_COLUMN)));
        team.setoLineRanks(results.getString(results.getColumnIndex(Constants.OLINE_RANKS_COLUMN)));
        team.setDraftClass(results.getString(results.getColumnIndex(Constants.DRAFT_CLASS_COLUMN)));
        team.setQbSos(results.getInt(results.getColumnIndex(Constants.QB_SOS_COLUMN)));
        team.setRbSos(results.getInt(results.getColumnIndex(Constants.RB_SOS_COLUMN)));
        team.setWrSos(results.getInt(results.getColumnIndex(Constants.WR_SOS_COLUMN)));
        team.setTeSos(results.getInt(results.getColumnIndex(Constants.TE_SOS_COLUMN)));
        team.setDstSos(results.getInt(results.getColumnIndex(Constants.DST_SOS_COLUMN)));
        team.setkSos(results.getInt(results.getColumnIndex(Constants.K_SOS_COLUMN)));
        team.setBye(results.getString(results.getColumnIndex(Constants.BYE_COLUMN)));
        team.setFaClass(results.getString(results.getColumnIndex(Constants.FREE_AGENCY_COLUMN)));
        return team;
    }
}
