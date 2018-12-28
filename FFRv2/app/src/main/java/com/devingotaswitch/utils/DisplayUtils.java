package com.devingotaswitch.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayUtils {


    private static String generateOutputSubtext(Player player, Rankings rankings) {
        StringBuilder sub = new StringBuilder(player.getPosition())
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.getTeamName());
        Team team = rankings.getTeam(player);
        if (team != null) {
            sub = sub.append(" (Bye: ")
                    .append(team.getBye())
                    .append(")");
        }
        return sub.append(Constants.LINE_BREAK)
                .append("Projection: ")
                .append(Constants.DECIMAL_FORMAT.format(player.getProjection()))
                .toString();
    }

    public static String getPlayerKeyFromListViewItem(View view) {
        TextView playerMain = view.findViewById(R.id.player_basic);
        TextView playerInfo = view.findViewById(R.id.player_info);
        String name = playerMain.getText().toString().split(Constants.RANKINGS_LIST_DELIMITER)[1];
        String teamPosBye = playerInfo.getText().toString().split(Constants.LINE_BREAK)[0];
        String teamPos = teamPosBye.split(" \\(")[0];
        String team = teamPos.split(Constants.POS_TEAM_DELIMITER)[1];
        String pos = teamPos.split(Constants.POS_TEAM_DELIMITER)[0];

        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos;
    }

    public static Map<String, String> getDatumForPlayer(Rankings rankings, Player player, boolean markWatched) {
        String playerBasicContent = player.getDisplayValue(rankings) +
                Constants.RANKINGS_LIST_DELIMITER +
                player.getName();
        Map<String, String> datum = new HashMap<>(5);
        datum.put(Constants.PLAYER_BASIC, playerBasicContent);
        datum.put(Constants.PLAYER_INFO, generateOutputSubtext(player, rankings));
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
