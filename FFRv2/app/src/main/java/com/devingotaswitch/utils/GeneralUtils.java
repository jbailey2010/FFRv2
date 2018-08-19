package com.devingotaswitch.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GeneralUtils {

    private static final Long SECONDS_CONVERSION_THRESHOLD = 1000L;

    public static boolean confirmInternet(Context cont) {
        ConnectivityManager connectivityManager = (ConnectivityManager) cont
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static List<String> sortData(
            List<String> data) {
        Collections.sort(data, new Comparator<String>() {
            public int compare(String a, String b) {
                String aName = a.split(Constants.RANKINGS_LIST_DELIMITER)[1].split(Constants.LINE_BREAK)[0];
                String bName = b.split(Constants.RANKINGS_LIST_DELIMITER)[1].split(Constants.LINE_BREAK)[0];
                int judgment = aName.compareTo(bName);
                return Integer.compare(judgment, 0);
            }
        });
        return data;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static long getLatency(long start) {
        return (System.currentTimeMillis() - start) / SECONDS_CONVERSION_THRESHOLD;
    }

    public static FilterWithSpaceAdapter<String> getPlayerSearchAdapter(Rankings rankings, Activity activity) {
        final List<String> dropdownList = new ArrayList<>();
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            String prefix = player.getDisplayValue(rankings);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !StringUtils.isBlank(player.getTeamName()) && player.getTeamName().length() > 3) {
                String dropdownStr = prefix +
                        Constants.RANKINGS_LIST_DELIMITER +
                        player.getName() +
                        Constants.LINE_BREAK +
                        player.getPosition() +
                        Constants.POS_TEAM_DELIMITER +
                        player.getTeamName();
                dropdownList.add(dropdownStr);
            }
        }
        List<String> dataSorted = GeneralUtils.sortData(dropdownList);
        return new FilterWithSpaceAdapter<>(activity,
                R.layout.dropdown_item, R.id.dropdown_text, dataSorted);
    }

    public static String getPlayerIdFromSearchView(View view) {
        String fullStr = ((TextView)view.findViewById(R.id.dropdown_text)).getText().toString().split(Constants.RANKINGS_LIST_DELIMITER)[1];
        String[] playerArr = fullStr.split(Constants.LINE_BREAK);
        String[] posAndTeam = playerArr[1].split(Constants.POS_TEAM_DELIMITER);
        String name = playerArr[0];
        String pos = posAndTeam[0];
        String team = posAndTeam[1];
        return name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos;
    }
}
