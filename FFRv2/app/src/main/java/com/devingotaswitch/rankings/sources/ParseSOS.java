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
                "https://www.fantasypros.com/nfl/strength-of-schedule.php",
                "table.table-striped tbody tr td");

        for (int i = 0; i < allArr.size(); i+=13) {
            String teamName = ParsingUtils.normalizeTeams(allArr.get(i));
            Team currentTeam = rankings.getTeam(teamName);
            currentTeam.setQbSos(Integer.parseInt(allArr.get(i+1)));
            currentTeam.setRbSos(Integer.parseInt(allArr.get(i+3)));
            currentTeam.setWrSos(Integer.parseInt(allArr.get(i+5)));
            currentTeam.setTeSos(Integer.parseInt(allArr.get(i+7)));
            currentTeam.setkSos(Integer.parseInt(allArr.get(i+9)));
            currentTeam.setDstSos(Integer.parseInt(allArr.get(i+11)));
        }
    }
}
