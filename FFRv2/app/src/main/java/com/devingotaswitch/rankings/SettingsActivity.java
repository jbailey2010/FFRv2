package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.UserSettings;
import com.devingotaswitch.utils.Constants;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG="SettingsActivity";

    CheckBox dSearch;
    CheckBox rSearch;
    CheckBox dsOutput;
    CheckBox rsOutput;
    CheckBox dcSuggestion;
    CheckBox rcSuggestion;
    CheckBox noteSort;
    CheckBox noteRanks;
    CheckBox overscrollRefresh;
    CheckBox sortWatchListByTime;

    boolean isRankingsReloadNeeded = false;

    private Rankings rankings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar_rankings_settings);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("Settings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Activity localCopy = this;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RankingsHome.class);
                intent.putExtra(Constants.RANKINGS_LIST_RELOAD_NEEDED, isRankingsReloadNeeded);
                localCopy.startActivity(intent);

                onBackPressed();
            }
        });

        rankings = Rankings.init();
        init();
    }

    private void init() {
        dSearch = findViewById(R.id.hide_search_drafted);
        rSearch = findViewById(R.id.hide_search_rankless);
        dsOutput = findViewById(R.id.hide_sort_output_drafted);
        rsOutput = findViewById(R.id.hide_sort_output_rankless);
        dcSuggestion = findViewById(R.id.hide_comparator_input_drafted);
        rcSuggestion = findViewById(R.id.hide_comparator_input_rankless);
        noteRanks = findViewById(R.id.show_note_ranks);
        noteSort = findViewById(R.id.show_note_sort);
        overscrollRefresh = findViewById(R.id.general_refresh_on_overscroll);
        sortWatchListByTime = findViewById(R.id.general_sort_watch_list_by_time);

        final UserSettings settings = rankings.getUserSettings();
        dSearch.setChecked(settings.isHideDraftedSearch());
        rSearch.setChecked(settings.isHideRanklessSearch());
        dsOutput.setChecked(settings.isHideDraftedSort());
        rsOutput.setChecked(settings.isHideRanklessSort());
        dcSuggestion.setChecked(settings.isHideDraftedComparator());
        rcSuggestion.setChecked(settings.isHideRanklessComparator());
        noteRanks.setChecked(settings.isShowNoteRank());
        noteSort.setChecked(settings.isShowNoteSort());
        overscrollRefresh.setChecked(settings.isRefreshOnOverscroll());
        sortWatchListByTime.setChecked(settings.isSortWatchListByTime());

        dSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRankingsReloadNeeded = true;
                settings.setHideDraftedSearch(b);
                updateUserSettings(settings);
            }
        });
        dsOutput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setHideDraftedSort(b);
                updateUserSettings(settings);
            }
        });
        dcSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setHideDraftedComparator(b);
                updateUserSettings(settings);
            }
        });

        rSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRankingsReloadNeeded = true;
                settings.setHideRanklessSearch(b);
                updateUserSettings(settings);
            }
        });
        rsOutput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setHideRanklessSort(b);
                updateUserSettings(settings);
            }
        });
        rcSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setHideRanklessComparator(b);
                updateUserSettings(settings);
            }
        });

        noteRanks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setShowNoteRank(b);
                updateUserSettings(settings);
            }
        });
        noteSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setShowNoteSort(b);
                updateUserSettings(settings);
            }
        });

        overscrollRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setRefreshOnOverscroll(b);
                isRankingsReloadNeeded = true;
                updateUserSettings(settings);
            }
        });
        sortWatchListByTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setSortWatchListByTime(isChecked);
                updateUserSettings(settings);
            }
        });
    }

    private void updateUserSettings(UserSettings userSettings) {
        Rankings.setUserSettings(userSettings);

        AppSyncHelper.updateUserSettings(this, userSettings);
    }
}
