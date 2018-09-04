package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG="SettingsActivity";

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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        init();
    }

    private void init() {
        CheckBox dSearch = findViewById(R.id.hide_search_drafted);
        CheckBox rSearch = findViewById(R.id.hide_search_rankless);
        CheckBox dsOutput = findViewById(R.id.hide_sort_output_drafted);
        CheckBox rsOutput = findViewById(R.id.hide_sort_output_rankless);
        CheckBox dcSuggestion = findViewById(R.id.hide_comparator_input_drafted);
        CheckBox rcSuggestion = findViewById(R.id.hide_comparator_input_rankless);
        CheckBox dcList = findViewById(R.id.hide_comparator_list_drafted);
        CheckBox rcList = findViewById(R.id.hide_comparator_list_rankless);

        dSearch.setChecked(LocalSettingsHelper.hideDraftedSearch(this));
        dsOutput.setChecked(LocalSettingsHelper.hideDraftedSortOutput(this));
        dcSuggestion.setChecked(LocalSettingsHelper.hideDraftedComparatorSuggestion(this));
        dcList.setChecked(LocalSettingsHelper.hideDraftedComparatorList(this));

        rSearch.setChecked(LocalSettingsHelper.hideRanklessSearch(this));
        rsOutput.setChecked(LocalSettingsHelper.hideRanklessSortOutput(this));
        rcSuggestion.setChecked(LocalSettingsHelper.hideRanklessComparatorSuggestion(this));
        rcList.setChecked(LocalSettingsHelper.hideRanklessComparatorList(this));

        final Activity act = this;
        dSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideDraftedSearch(act, b);
            }
        });
        dsOutput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideDraftedSortOutput(act, b);
            }
        });
        dcSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideDraftedComparatorSuggestion(act, b);
            }
        });
        dcList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideDraftedComparatorList(act, b);
            }
        });

        rSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideRanklessSearch(act, b);
            }
        });
        rsOutput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideRanklessSortOutput(act, b);
            }
        });
        rcSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideRanklessComparatorSuggestion(act, b);
            }
        });
        rcList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                LocalSettingsHelper.setHideRanklessComparatorList(act, b);
            }
        });
    }
}
