package com.devingotaswitch.rankings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeagueSettingsActivity extends AppCompatActivity {
    private final String TAG="LeagueSettings";

    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private RankingsDBWrapper rankingsDB;
    private LinearLayout baseLayout;

    Map<String, LeagueSettings> leagues;
    private LeagueSettings currLeague;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.toolbar_league_settings);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("League Settings");
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
        rankingsDB = new RankingsDBWrapper();
        baseLayout = (LinearLayout) findViewById(R.id.league_settings_base);
        String currentLeagueId = LocalSettingsHelper.getCurrentLeagueId(this);
        leagues = rankingsDB.getLeagues(this);
        initializeLeagueSpinner();
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currLeague = leagues.get(currentLeagueId);
            displayLeague(currLeague);
            Log.d(TAG, currentLeagueId);
        } else {
            displayNoLeague();
        }
    }

    private void initializeLeagueSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.league_settings_spinner);
        if (leagues.isEmpty()) {
            spinner.setVisibility(View.INVISIBLE);
            return;
        }
        spinner.setVisibility(View.VISIBLE);
        List<String> leagueNames = new ArrayList<>();
        int currLeagueIndex = 0;
        int leagueCount = 0;
        for (String leagueName : leagues.keySet()) {
            leagueNames.add(leagueName);
            if (leagueName.equals(currLeague.getName())) {
                currLeagueIndex = leagueCount;
            }
            leagueCount++;
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, leagueNames);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(currLeagueIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                displayLeague(leagues.get(adapterView.getItemAtPosition(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Nada
            }
        });
    }

    private void displayLeague(LeagueSettings currentLeague) {
        View view = initializeLeagueSettingsBase(View.GONE);
        EditText leagueName = (EditText)view.findViewById(R.id.league_settings_name);
        leagueName.setText(currentLeague.getName());
        EditText teamCount = (EditText)view.findViewById(R.id.league_settings_team_count);
        teamCount.setText(currentLeague.getTeamCount());
        RadioButton isAuction = (RadioButton)view.findViewById(R.id.league_settings_auction);
        RadioButton isSnake = (RadioButton)findViewById(R.id.league_settings_snake);
        if (currentLeague.isAuction()) {
            isAuction.setSelected(true);
        } else {
            isSnake.setSelected(true);
        }
        LocalSettingsHelper.saveCurrentLeagueId(this, currentLeague.getId());
    }

    private void displayNoLeague() {
        initializeLeagueSettingsBase(View.VISIBLE);
    }

    private View initializeLeagueSettingsBase(int defaultButtonVisibility) {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(R.layout.league_settings_base, null);
        Button defaults = (Button) child.findViewById(R.id.league_settings_create_default);
        defaults.setVisibility(defaultButtonVisibility);
        baseLayout.addView(child);
        // TODO: the rest of this
        return child;
    }

    private void saveNewLeague(LeagueSettings league) {
        rankingsDB.insertLeague(this, league);
        setCurrentLeague(league);
    }

    private void setCurrentLeague(LeagueSettings league) {
        LocalSettingsHelper.saveCurrentLeagueId(this, league.getId());
    }

    private void deleteLeague(LeagueSettings league) {
        rankingsDB.deleteLeague(this, league);
    }

    private void updateLeague(Map<String, String> scoringUpdates, Map<String, String> rosterUpdates,
                              Map<String, String> leagueUpdates, LeagueSettings league) {
        rankingsDB.updateLeague(this, leagueUpdates, rosterUpdates, scoringUpdates, league);
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG,"Dialog dismiss failed");
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }
}
