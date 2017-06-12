package com.devingotaswitch.rankings;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueSettingsActivity extends AppCompatActivity {
    private final String TAG="LeagueSettings";
    private final String CREATE_NEW_LEAGUE_SPINNER_ITEM = "Create New League";

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
        initLeagues();
    }

    private void initLeagues() {
        String currentLeagueId = LocalSettingsHelper.getCurrentLeagueName(this);
        leagues = rankingsDB.getLeagues(this);
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currLeague = leagues.get(currentLeagueId);
            displayLeague(currLeague);
        } else {
            displayNoLeague();
        }
        initializeLeagueSpinner();
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
        leagueNames.add(CREATE_NEW_LEAGUE_SPINNER_ITEM);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, leagueNames);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(currLeagueIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (CREATE_NEW_LEAGUE_SPINNER_ITEM.equals(adapterView.getItemAtPosition(i))) {
                    displayNoLeague();
                } else {
                    displayLeague(leagues.get(adapterView.getItemAtPosition(i)));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Nada
            }
        });
    }

    private void displayLeague(final LeagueSettings currentLeague) {
        View view = initializeLeagueSettingsBase();
        final EditText leagueName = (EditText)view.findViewById(R.id.league_settings_name);
        leagueName.setText(currentLeague.getName());

        final EditText teamCount = (EditText)view.findViewById(R.id.league_settings_team_count);
        teamCount.setText(String.valueOf(currentLeague.getTeamCount()));
        final EditText auctionBudget = (EditText)view.findViewById(R.id.league_settings_auction_budget);

        final RadioButton isAuction = (RadioButton)view.findViewById(R.id.league_settings_auction);
        RadioButton isSnake = (RadioButton)view.findViewById(R.id.league_settings_snake);

        if (currentLeague.isAuction()) {
            isAuction.setSelected(true);
            isAuction.setChecked(true);
            auctionBudget.setText(String.valueOf(currentLeague.getAuctionBudget()));
        } else {
            isSnake.setSelected(true);
            isSnake.setChecked(true);
        }
        Button save = (Button) view.findViewById(R.id.league_settings_create_default);
        save.setText("Update");
        Button advanced = (Button) view.findViewById(R.id.league_settings_advanced_settings);
        final Context localCopy = this;
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                    return;
                }
                Map<String, String> updates = getLeagueUpdates(currentLeague, leagueName, teamCount, isAuction, auctionBudget);
                if (updates == null) {
                    Toast.makeText(localCopy, "No updates given", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateLeague(null, null, updates, currentLeague);
                Toast.makeText(localCopy, currentLeague.getName() + " updated", Toast.LENGTH_SHORT).show();
            }
        });
        // TODO: proceed part

        setCurrentLeague(currentLeague);
    }

    private boolean validateLeagueInputs(EditText name, EditText teamCount, EditText auctionBudget, RadioButton isAuction) {
        String givenName = name.getText().toString();
        String givenTeamCount = teamCount.getText().toString();
        String givenAuctionBudget = auctionBudget.getText().toString();
        if (StringUtils.isBlank(givenName)) {
            Toast.makeText(this, "League name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        } if (StringUtils.isBlank(givenTeamCount) ||
                !GeneralUtils.isInteger(givenTeamCount)) {
            Toast.makeText(this, "Team count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        int teamCountInt = Integer.parseInt(givenTeamCount);
        if (teamCountInt < 1 || teamCountInt > 32) {
            Toast.makeText(this, "Invalid team count, must be between 1 and 32", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isAuction.isChecked()) {
            if (StringUtils.isBlank(givenAuctionBudget) || !GeneralUtils.isInteger(givenAuctionBudget)) {
                Toast.makeText(this, "Auction budget must be an integer", Toast.LENGTH_SHORT).show();
                return false;
            }
            int auctionBudgetInt = Integer.parseInt(givenAuctionBudget);
            if (auctionBudgetInt < 1) {
                Toast.makeText(this, "Invalid auction budget, must be a positive number", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    private Map<String, String> getLeagueUpdates(LeagueSettings league, EditText name, EditText teamCount,
                                  RadioButton isAuction, EditText auctionBudget) {
        Map<String, String> updates = new HashMap<>();
        if (!league.getName().equals(name.getText().toString())) {
            updates.put(Constants.NAME_COLUMN, name.getText().toString());
            league.setName(name.getText().toString());
        }
        if (league.getTeamCount() != Integer.parseInt(teamCount.getText().toString())) {
            updates.put(Constants.TEAM_COUNT_COLUMN, teamCount.getText().toString());
            league.setTeamCount(Integer.parseInt(teamCount.getText().toString()));
        }
        if (isAuction.isChecked() != league.isAuction()) {
            updates.put(Constants.IS_AUCTION_COLUMN, Boolean.toString(isAuction.isChecked()));
            league.setAuction(isAuction.isChecked());
        }
        if (isAuction.isChecked() && league.getAuctionBudget() != Integer.parseInt(auctionBudget.getText().toString())) {
            updates.put(Constants.AUCTION_BUDGET_COLUMN, auctionBudget.getText().toString());
            league.setAuctionBudget(Integer.parseInt(auctionBudget.getText().toString()));
        }
        if (updates.size() == 0) {
            return null;
        }
        return updates;
    }

    private void displayNoLeague() {
        View view = initializeLeagueSettingsBase();
        Button advanced = (Button) view.findViewById(R.id.league_settings_advanced_settings);
        Button save = (Button) view.findViewById(R.id.league_settings_create_default);
        final EditText leagueName = (EditText)view.findViewById(R.id.league_settings_name);
        final EditText teamCount = (EditText)view.findViewById(R.id.league_settings_team_count);
        final EditText auctionBudget = (EditText)view.findViewById(R.id.league_settings_auction_budget);
        final RadioButton isAuction = (RadioButton)view.findViewById(R.id.league_settings_auction);
        RadioButton isSnake = (RadioButton)view.findViewById(R.id.league_settings_snake);
        final Context localCopy = this;
        //TODO: advanced button
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                    return;
                }
                LeagueSettings defaults = getLeagueSettingsFromFirstPage(leagueName, teamCount, isAuction, auctionBudget);
                saveNewLeague(defaults);
                Toast.makeText(localCopy, defaults.getName() + " saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private LeagueSettings getLeagueSettingsFromFirstPage(EditText leagueName, EditText teamCount, RadioButton isAuction,
                                                          EditText auctionBudget) {

        int realBudget = 200;
        if (GeneralUtils.isInteger(auctionBudget.getText().toString())) {
            realBudget = Integer.parseInt(auctionBudget.getText().toString());
        }
        return new LeagueSettings(leagueName.getText().toString(),
                Integer.parseInt(teamCount.getText().toString()), isAuction.isChecked(),
                realBudget);
    }

    private View initializeLeagueSettingsBase() {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(R.layout.league_settings_base, null);
        baseLayout.addView(child);

        // Hide auction budget on snake selection
        final EditText auctionBudget = (EditText)child.findViewById(R.id.league_settings_auction_budget);
        RadioButton isSnake = (RadioButton)child.findViewById(R.id.league_settings_snake);
        isSnake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    auctionBudget.setVisibility(View.GONE);
                } else {
                    auctionBudget.setVisibility(View.VISIBLE);
                }
            }
        });
        return child;
    }

    private void saveNewLeague(LeagueSettings league) {
        rankingsDB.insertLeague(this, league);
        setCurrentLeague(league);
        initLeagues();
    }

    private void setCurrentLeague(LeagueSettings league) {
        LocalSettingsHelper.saveCurrentLeagueName(this, league.getName());
    }

    private void deleteLeague(LeagueSettings league) {
        rankingsDB.deleteLeague(this, league);
    }

    private void updateLeague(Map<String, String> scoringUpdates, Map<String, String> rosterUpdates,
                              Map<String, String> leagueUpdates, LeagueSettings league) {
        rankingsDB.updateLeague(this, leagueUpdates, rosterUpdates, scoringUpdates, league);
        setCurrentLeague(league);
        initLeagues();
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
