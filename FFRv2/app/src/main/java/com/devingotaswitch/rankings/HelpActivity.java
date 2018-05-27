package com.devingotaswitch.rankings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    private TextView helpLeague;
    private TextView helpRankings;
    private TextView helpPlayerInfo;
    private TextView helpDrafting;
    private TextView helpADPSimulator;
    private TextView helpCompare;
    private TextView helpSort;
    private TextView helpNews;
    private TextView helpExport;
    private TextView helpProfile;
    private TextView helpStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_rankings_help);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Help");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        init();
    }

    private void init() {
        helpLeague = (TextView)findViewById(R.id.help_league_body);
        helpRankings = (TextView)findViewById(R.id.help_rankings_body);
        helpPlayerInfo = (TextView)findViewById(R.id.help_info_body);
        helpDrafting = (TextView)findViewById(R.id.help_draft_body);
        helpADPSimulator = (TextView)findViewById(R.id.help_adp_body);
        helpCompare = (TextView)findViewById(R.id.help_compare_body);
        helpSort = (TextView)findViewById(R.id.help_sort_body);
        helpNews = (TextView)findViewById(R.id.help_news_body);
        helpExport = (TextView)findViewById(R.id.help_export_body);
        helpProfile = (TextView)findViewById(R.id.help_profile_body);
        helpStats = (TextView)findViewById(R.id.help_stats_body);

        final Spinner spinner = (Spinner)findViewById(R.id.help_topics);
        List<String> posList = new ArrayList<>();
        posList.add(Constants.HELP_LEAGUE);
        posList.add(Constants.HELP_RANKINGS);
        posList.add(Constants.HELP_PLAYER_INFO);
        posList.add(Constants.HELP_DRAFTING);
        posList.add(Constants.HELP_ADP_SIMULARTOR);
        posList.add(Constants.HELP_COMPARE_PLAYERS);
        posList.add(Constants.HELP_SORT_PLAYERS);
        posList.add(Constants.HELP_NEWS);
        posList.add(Constants.HELP_EXPORT);
        posList.add(Constants.HELP_PROFILE);
        posList.add(Constants.HELP_STATS);

        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, posList);
        spinner.setAdapter(positionAdapter);

        Button submit = (Button)findViewById(R.id.help_selection_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selected = spinner.getSelectedItem().toString();
                updateLayout(selected);
            }
        });
    }

    private void updateLayout(String selection) {
        hideAll();
        if (Constants.HELP_LEAGUE.equals(selection)) {
            makeVisible(helpLeague);
        } else if (Constants.HELP_RANKINGS.equals(selection)) {
            makeVisible(helpRankings);
        } else if (Constants.HELP_PLAYER_INFO.equals(selection)) {
            makeVisible(helpPlayerInfo);
        } else if (Constants.HELP_DRAFTING.equals(selection)) {
            makeVisible(helpDrafting);
        } else if (Constants.HELP_ADP_SIMULARTOR.equals(selection)) {
            makeVisible(helpADPSimulator);
        } else if (Constants.HELP_COMPARE_PLAYERS.equals(selection)) {
            makeVisible(helpCompare);
        } else if (Constants.HELP_SORT_PLAYERS.equals(selection)) {
            makeVisible(helpSort);
        } else if (Constants.HELP_NEWS.equals(selection)) {
            makeVisible(helpNews);
        } else if (Constants.HELP_EXPORT.equals(selection)) {
            makeVisible(helpExport);
        } else if (Constants.HELP_PROFILE.equals(selection)) {
            makeVisible(helpProfile);
        } else if (Constants.HELP_STATS.equals(selection)) {
            makeVisible(helpStats);
        }
    }

    private void makeVisible(TextView selected) {
        selected.setVisibility(View.VISIBLE);
    }

    private void hideAll() {
        helpLeague.setVisibility(View.GONE);
        helpRankings.setVisibility(View.GONE);
        helpPlayerInfo.setVisibility(View.GONE);
        helpDrafting.setVisibility(View.GONE);
        helpADPSimulator.setVisibility(View.GONE);
        helpCompare.setVisibility(View.GONE);
        helpSort.setVisibility(View.GONE);
        helpNews.setVisibility(View.GONE);
        helpExport.setVisibility(View.GONE);
        helpProfile.setVisibility(View.GONE);
        helpStats.setVisibility(View.GONE);
    }
}
