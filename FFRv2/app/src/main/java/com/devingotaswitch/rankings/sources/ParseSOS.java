package com.devingotaswitch.rankings.sources;

import android.util.Log;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ParseSOS {

    public static void getSOS(Rankings rankings) throws IOException {
        List<String> allArr = JsoupUtils.handleLists(
                "http://www.fftoolbox.com/football/strength_of_schedule.cfm",
                "tr.c");
        String[][] team = new String[allArr.size()][];

        for (int i = 0; i < allArr.size(); i++) {
            team[i] = allArr.get(i).split(" ");
            String teamName = ParsingUtils.normalizeTeams(team[i][0]);
            Team currentTeam = rankings.getTeam(teamName);
            currentTeam.setQbSos(Integer.parseInt(cleanRanking(team[i][1])));
            currentTeam.setRbSos(Integer.parseInt(cleanRanking(team[i][2])));
            currentTeam.setWrSos(Integer.parseInt(cleanRanking(team[i][3])));
            currentTeam.setTeSos(Integer.parseInt(cleanRanking(team[i][4])));
            currentTeam.setkSos(Integer.parseInt(cleanRanking(team[i][5])));
            currentTeam.setDstSos(Integer.parseInt(cleanRanking(team[i][6])));
        }
    }

    private static String cleanRanking(String input) {
        return input.replaceAll("rd", "").replaceAll("st", "")
                .replaceAll("nd", "").replaceAll("th", "");
    }
}
