package com.devingotaswitch.rankings;

import android.app.Activity;
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

public class SettingsActivity extends AppCompatActivity {
    private final String TAG="SettingsActivity";

    CheckBox dSearch;
    CheckBox rSearch;
    CheckBox dsOutput;
    CheckBox rsOutput;
    CheckBox dcSuggestion;
    CheckBox rcSuggestion;
    CheckBox overscrollRefresh;

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
        dSearch = findViewById(R.id.hide_search_drafted);
        rSearch = findViewById(R.id.hide_search_rankless);
        dsOutput = findViewById(R.id.hide_sort_output_drafted);
        rsOutput = findViewById(R.id.hide_sort_output_rankless);
        dcSuggestion = findViewById(R.id.hide_comparator_input_drafted);
        rcSuggestion = findViewById(R.id.hide_comparator_input_rankless);
        overscrollRefresh = findViewById(R.id.general_refresh_on_overscroll);

        AppSyncHelper.getUserSettings(this);

        dSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });
        dsOutput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });
        dcSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });

        rSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });
        rsOutput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });
        rcSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });

        overscrollRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateUserSettings();
            }
        });
    }

    private void updateUserSettings() {
        AppSyncHelper.updateUserSettings(this, rSearch.isChecked(), rsOutput.isChecked(), rcSuggestion.isChecked(),
                dSearch.isChecked(), dsOutput.isChecked(), dcSuggestion.isChecked(), overscrollRefresh.isChecked());
    }

    public void updateUserSettings(boolean hideIrrelevantSearch, boolean hideIrrelevantSort,
                                   boolean hideIrrelevantComparator, boolean hideDraftedSearch, boolean hideDraftedSort,
                                   boolean hideDraftedComparator, boolean refreshOnOverscroll) {
        if (rSearch != null && rsOutput != null && rcSuggestion != null && dSearch != null && dsOutput != null
                && dcSuggestion != null && overscrollRefresh != null) {
            // Useless if statement to ensure we don't get an NPE on rapid activity swap
            rSearch.setChecked(hideIrrelevantSearch);
            rsOutput.setChecked(hideIrrelevantSort);
            rcSuggestion.setChecked(hideIrrelevantComparator);
            dSearch.setChecked(hideDraftedSearch);
            dsOutput.setChecked(hideDraftedSort);
            dcSuggestion.setChecked(hideDraftedComparator);
            overscrollRefresh.setChecked(refreshOnOverscroll);
        }
    }
}
