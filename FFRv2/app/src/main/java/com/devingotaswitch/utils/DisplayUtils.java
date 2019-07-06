package com.devingotaswitch.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayUtils {


    private static String generateOutputSubtext(Player player, Rankings rankings, String posSuffix) {
        StringBuilder sub = new StringBuilder(player.getPosition())
                .append(posSuffix)
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.getTeamName());
        Team team = rankings.getTeam(player);
        if (!Constants.NO_TEAM.equals(player.getTeamName())) {
            if (team != null) {
                sub = sub.append(" (Bye: ")
                        .append(team.getBye())
                        .append(")");
            }
            sub = sub.append(Constants.LINE_BREAK)
                    .append("Projection: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.getProjection()));
        }
        return sub.toString();
    }

    public static String getPlayerKeyFromListViewItem(View view) {
        TextView playerMain = view.findViewById(R.id.player_basic);
        TextView playerInfo = view.findViewById(R.id.player_info);
        String name = playerMain.getText().toString().split(Constants.RANKINGS_LIST_DELIMITER)[1];
        String teamPosBye = playerInfo.getText().toString().split(Constants.LINE_BREAK)[0];
        String teamPos = teamPosBye.split(" \\(")[0];
        String team = teamPos.split(Constants.POS_TEAM_DELIMITER)[1];
        String pos = teamPos.split(Constants.POS_TEAM_DELIMITER)[0].replaceAll("\\d","");

        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos;
    }

    public static Map<String, Integer> getPositionRankMap() {
        Map<String, Integer> positionRankMap = new HashMap<>();
        positionRankMap.put(Constants.QB, 1);
        positionRankMap.put(Constants.RB, 1);
        positionRankMap.put(Constants.WR, 1);
        positionRankMap.put(Constants.TE, 1);
        positionRankMap.put(Constants.DST, 1);
        positionRankMap.put(Constants.K, 1);
        return positionRankMap;
    }

    public static Map<String, String> getDatumForPlayer(Rankings rankings, Player player, boolean markWatched,
                                                        Map<String, Integer> posRankMap) {
        String posSuffix = "";
        if (posRankMap != null) {
            Integer suffix = posRankMap.get(player.getPosition());
            posRankMap.put(player.getPosition(), posRankMap.get(player.getPosition()) + 1);
            posSuffix = String.valueOf(suffix);
        }
        String playerBasicContent = player.getDisplayValue(rankings) +
                Constants.RANKINGS_LIST_DELIMITER +
                player.getName();
        Map<String, String> datum = new HashMap<>(5);
        datum.put(Constants.PLAYER_BASIC, playerBasicContent);
        datum.put(Constants.PLAYER_INFO, generateOutputSubtext(player, rankings, posSuffix));
        if (markWatched && player.isWatched()) {
            datum.put(Constants.PLAYER_STATUS, Integer.toString(R.drawable.star));
        }
        if (player.getAge() != null  && !Constants.DST.equals(player.getPosition())) {
            datum.put(Constants.PLAYER_ADDITIONAL_INFO, "Age: " + player.getAge());
        }
        if (player.getExperience() != null && player.getExperience() >= 0 && !Constants.DST.equals(player.getPosition())) {
            datum.put(Constants.PLAYER_ADDITIONAL_INFO_2, "Exp: " + player.getExperience());
        }
        return datum;
    }

    public static SimpleAdapter getDisplayAdapter(Activity act, List<Map<String, String>> data) {
        return new SimpleAdapter(act, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO,
                        Constants.PLAYER_ADDITIONAL_INFO_2},
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status, R.id.player_more_info, R.id.player_additional_info_2 });
    }

    public static DividerItemDecoration getVerticalDividerDecoration(Context context) {
        return new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
    }
}
