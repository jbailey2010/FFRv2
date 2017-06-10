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
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;

import java.util.Map;

public class LeagueSettingsActivity extends AppCompatActivity {
    private final String TAG="LeagueSettings";

    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private RankingsDBWrapper rankingsDB;

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
        rankingsDB = new RankingsDBWrapper();

        init();
    }

    private void init() {
        String currentLeagueId = LocalSettingsHelper.getCurrentLeagueId(this);
        Map<String, LeagueSettings> leagues = rankingsDB.getLeagues(this);
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currLeague = leagues.get(currentLeagueId);
            displayLeague();
        } else {
            displayNoLeague();
        }
    }

    private void displayLeague() {
        // TODO: this
    }

    private void displayNoLeague() {
        // TODO: this
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
