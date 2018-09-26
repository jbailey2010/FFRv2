package com.devingotaswitch.rankings;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.utils.Constants;

import org.angmarch.views.NiceSpinner;

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
    private TextView helpRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar_rankings_help);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
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
        helpLeague = findViewById(R.id.help_league_body);
        helpRankings = findViewById(R.id.help_rankings_body);
        helpPlayerInfo = findViewById(R.id.help_info_body);
        helpDrafting = findViewById(R.id.help_draft_body);
        helpADPSimulator = findViewById(R.id.help_adp_body);
        helpCompare = findViewById(R.id.help_compare_body);
        helpSort = findViewById(R.id.help_sort_body);
        helpNews = findViewById(R.id.help_news_body);
        helpExport = findViewById(R.id.help_export_body);
        helpProfile = findViewById(R.id.help_profile_body);
        helpStats = findViewById(R.id.help_stats_body);
        helpRefresh = findViewById(R.id.help_refresh_body);

        final NiceSpinner spinner = findViewById(R.id.help_topics);
        spinner.setBackgroundColor(Color.parseColor("#FAFAFA"));
        final List<String> posList = new ArrayList<>();
        posList.add(Constants.HELP_LEAGUE);
        posList.add(Constants.HELP_RANKINGS);
        posList.add(Constants.HELP_PLAYER_INFO);
        posList.add(Constants.HELP_DRAFTING);
        posList.add(Constants.HELP_SORT_PLAYERS);
        posList.add(Constants.HELP_COMPARE_PLAYERS);
        posList.add(Constants.HELP_ADP_SIMULARTOR);
        posList.add(Constants.HELP_NEWS);
        posList.add(Constants.HELP_EXPORT);
        posList.add(Constants.HELP_PROFILE);
        posList.add(Constants.HELP_STATS);
        posList.add(Constants.HELP_REFRESH);

        spinner.attachDataSource(posList);

        Button submit = findViewById(R.id.help_selection_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selected = posList.get(spinner.getSelectedIndex());
                updateLayout(selected);
            }
        });
    }

    private void updateLayout(String selection) {
        hideAll();
        switch (selection) {
            case Constants.HELP_LEAGUE:
                makeVisible(helpLeague);
                break;
            case Constants.HELP_RANKINGS:
                makeVisible(helpRankings);
                break;
            case Constants.HELP_PLAYER_INFO:
                makeVisible(helpPlayerInfo);
                break;
            case Constants.HELP_DRAFTING:
                makeVisible(helpDrafting);
                break;
            case Constants.HELP_ADP_SIMULARTOR:
                makeVisible(helpADPSimulator);
                break;
            case Constants.HELP_COMPARE_PLAYERS:
                makeVisible(helpCompare);
                break;
            case Constants.HELP_SORT_PLAYERS:
                makeVisible(helpSort);
                break;
            case Constants.HELP_NEWS:
                makeVisible(helpNews);
                break;
            case Constants.HELP_EXPORT:
                makeVisible(helpExport);
                break;
            case Constants.HELP_PROFILE:
                makeVisible(helpProfile);
                break;
            case Constants.HELP_STATS:
                makeVisible(helpStats);
                break;
            case Constants.HELP_REFRESH:
                makeVisible(helpRefresh);
                break;
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
