package com.devingotaswitch.rankings.sources;

import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseSOS {

    public static void getSOS(Rankings rankings) throws IOException {
        Document doc = JsoupUtils.getDocument(
                "https://www.fantasypros.com/nfl/strength-of-schedule.php");
        Elements elems = doc.select("table.table-striped tbody tr td");
        List<String> allArr = new ArrayList<>();
        for (int i = 0; i < elems.size(); i+=7) {
            allArr.add(elems.get(i).text());
            for (int j = i+1; j < i+7; j++) {
                allArr.add(elems.get(j).attr("data-raw-stars"));
            }
        }
        for (int i = 0; i < allArr.size(); i+=7) {
            String teamName = ParsingUtils.normalizeTeams(allArr.get(i));
            Team currentTeam = rankings.getTeam(teamName);
            currentTeam.setQbSos(Double.parseDouble(allArr.get(i+1)));
            currentTeam.setRbSos(Double.parseDouble(allArr.get(i+2)));
            currentTeam.setWrSos(Double.parseDouble(allArr.get(i+3)));
            currentTeam.setTeSos(Double.parseDouble(allArr.get(i+4)));
            currentTeam.setKSos(Double.parseDouble(allArr.get(i+5)));
            currentTeam.setDstSos(Double.parseDouble(allArr.get(i+6)));
        }
    }
}
