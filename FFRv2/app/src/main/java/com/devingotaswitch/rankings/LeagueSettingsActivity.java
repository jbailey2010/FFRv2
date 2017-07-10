package com.devingotaswitch.rankings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueSettingsActivity extends AppCompatActivity {
    private final String TAG="LeagueSettings";
    private final String CREATE_NEW_LEAGUE_SPINNER_ITEM = "Create New League";

    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private RankingsDBWrapper rankingsDB;
    private LinearLayout baseLayout;
    private boolean rankingsUpdated;

    Map<String, LeagueSettings> leagues;
    private LeagueSettings currLeague;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_league_settings);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("League Settings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RankingsHome.class);
                intent.putExtra(Constants.RANKINGS_UPDATED, rankingsUpdated);
                getApplication().startActivity(intent);
            }
        });
        rankingsUpdated = false;

        init();
    }

    private void init() {
        if (rankingsDB == null) {
            rankingsDB = new RankingsDBWrapper();
        }
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
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
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
        leagueName.setVisibility(View.GONE);

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
        Button delete = (Button)findViewById(R.id.league_settings_delete_league);
        delete.setVisibility(View.VISIBLE);
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
                    Toast.makeText(localCopy, "No updates given.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateLeague(null, null, updates, currentLeague);
                Toast.makeText(localCopy, currentLeague.getName() + " updated", Toast.LENGTH_SHORT).show();
            }
        });
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                    return;
                }
                Map<String, String> updates = getLeagueUpdates(currentLeague, leagueName, teamCount, isAuction, auctionBudget);
                displayRoster(currentLeague, updates);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leagues.size() > 1) {
                    deleteLeague(currentLeague);
                } else {
                    Toast.makeText(localCopy, "Can't delete league, none would remain", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setCurrentLeague(currentLeague);
    }

    private void displayNoLeague() {
        View view = initializeLeagueSettingsBase();
        Button advanced = (Button) view.findViewById(R.id.league_settings_advanced_settings);
        Button save = (Button) view.findViewById(R.id.league_settings_create_default);
        Button delete = (Button) view.findViewById(R.id.league_settings_delete_league);
        delete.setVisibility(View.GONE);
        final EditText leagueName = (EditText)view.findViewById(R.id.league_settings_name);
        leagueName.setVisibility(View.VISIBLE);
        final EditText teamCount = (EditText)view.findViewById(R.id.league_settings_team_count);
        final EditText auctionBudget = (EditText)view.findViewById(R.id.league_settings_auction_budget);
        final RadioButton isAuction = (RadioButton)view.findViewById(R.id.league_settings_auction);
        final Context localCopy = this;
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
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                    return;
                }
                LeagueSettings defaults = getLeagueSettingsFromFirstPage(leagueName, teamCount, isAuction, auctionBudget);
                displayRosterNoLeague(defaults);
            }
        });
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
            updates.put(Constants.IS_AUCTION_COLUMN, isAuction.isChecked() ? "1" : "0");
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
        final TextView auctionBudgetHeader = (TextView)child.findViewById(R.id.league_settings_auction_budget_header);
        RadioButton isSnake = (RadioButton)child.findViewById(R.id.league_settings_snake);
        isSnake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    auctionBudget.setVisibility(View.GONE);
                    auctionBudgetHeader.setVisibility(View.GONE);
                } else {
                    auctionBudget.setVisibility(View.VISIBLE);
                    auctionBudgetHeader.setVisibility(View.VISIBLE);
                }
            }
        });
        return child;
    }

    private void displayRoster(final LeagueSettings currentLeague, final Map<String, String> leagueUpdates) {
        View view = initializeLeagueSettingsRoster();
        Button update = (Button) view.findViewById(R.id.league_roster_create_default);
        update.setText("Update");
        Button advanced = (Button) view.findViewById(R.id.league_roster_advanced_settings);
        RosterSettings roster = currentLeague.getRosterSettings();
        final EditText qbs = (EditText)view.findViewById(R.id.league_settings_qbs);
        qbs.setText(String.valueOf(roster.getQbCount()));
        final EditText rbs = (EditText)view.findViewById(R.id.league_settings_rbs);
        rbs.setText(String.valueOf(roster.getRbCount()));
        final EditText wrs = (EditText)view.findViewById(R.id.league_settings_wrs);
        wrs.setText(String.valueOf(roster.getWrCount()));
        final EditText tes = (EditText)view.findViewById(R.id.league_settings_tes);
        tes.setText(String.valueOf(roster.getTeCount()));
        final EditText dsts = (EditText)view.findViewById(R.id.league_settings_dsts);
        dsts.setText(String.valueOf(roster.getDstCount()));
        final EditText ks = (EditText)view.findViewById(R.id.league_settings_ks);
        ks.setText(String.valueOf(roster.getkCount()));
        final EditText bench = (EditText)view.findViewById(R.id.league_settings_bench);
        bench.setText(String.valueOf(roster.getBenchCount()));
        final Context localCopy = this;

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                    return;
                }
                Map<String, String> rosterUpdates = getRosterUpdates(qbs, rbs, wrs, tes, dsts, ks, bench, currentLeague);
                if (rosterUpdates == null && leagueUpdates == null) {
                    Toast.makeText(localCopy, "No updates given.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateLeague(null, rosterUpdates, leagueUpdates, currentLeague);
                Toast.makeText(localCopy, currentLeague.getName() + " updated", Toast.LENGTH_SHORT).show();
            }
        });
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                    return;
                }
                Map<String, String> rosterUpdates = getRosterUpdates(qbs, rbs, wrs, tes, dsts, ks, bench, currentLeague);
                displayFlex(currentLeague, leagueUpdates, rosterUpdates);
            }
        });
    }

    private void displayRosterNoLeague(final LeagueSettings newLeague) {
        View view = initializeLeagueSettingsRoster();
        final EditText qbs = (EditText)view.findViewById(R.id.league_settings_qbs);
        final EditText rbs = (EditText)view.findViewById(R.id.league_settings_rbs);
        final EditText wrs = (EditText)view.findViewById(R.id.league_settings_wrs);
        final EditText tes = (EditText)view.findViewById(R.id.league_settings_tes);
        final EditText dsts = (EditText)view.findViewById(R.id.league_settings_dsts);
        final EditText ks = (EditText)view.findViewById(R.id.league_settings_ks);
        final EditText bench = (EditText)view.findViewById(R.id.league_settings_bench);
        Button save = (Button) view.findViewById(R.id.league_roster_create_default);
        Button advanced = (Button) view.findViewById(R.id.league_roster_advanced_settings);
        final Context localCopy = this;

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                    return;
                }
                RosterSettings defaults = getRosterSettingsFromFirstPage(qbs, rbs, wrs, tes, dsts, ks, bench);
                newLeague.setRosterSettings(defaults);
                saveNewLeague(newLeague);
                Toast.makeText(localCopy, newLeague.getName() + " saved", Toast.LENGTH_SHORT).show();
            }
        });
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                    return;
                }
                RosterSettings defaults = getRosterSettingsFromFirstPage(qbs, rbs, wrs, tes, dsts, ks, bench);
                newLeague.setRosterSettings(defaults);
                displayFlexNoLeague(newLeague);
            }
        });
    }

    private View initializeLeagueSettingsRoster() {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(R.layout.league_settings_roster, null);
        baseLayout.addView(child);
        return child;
    }

    private RosterSettings getRosterSettingsFromFirstPage(EditText qbs, EditText rbs, EditText wrs, EditText tes, EditText dsts,
                                                          EditText ks, EditText bench) {
        int qbTotal = Integer.parseInt(qbs.getText().toString());
        int rbTotal = Integer.parseInt(rbs.getText().toString());
        int wrTotal = Integer.parseInt(wrs.getText().toString());
        int teTotal = Integer.parseInt(tes.getText().toString());
        int dstTotal = Integer.parseInt(dsts.getText().toString());
        int kTotal = Integer.parseInt(ks.getText().toString());
        int benchTotal = Integer.parseInt(bench.getText().toString());
        return new RosterSettings(qbTotal, rbTotal, wrTotal, teTotal, dstTotal, kTotal, benchTotal);
    }

    private boolean validateRosterInputs(EditText qbs, EditText rbs, EditText wrs, EditText tes, EditText dsts,
                                         EditText ks, EditText bench) {
        String qbStr = qbs.getText().toString();
        String rbStr = rbs.getText().toString();
        String wrStr = wrs.getText().toString();
        String teStr = tes.getText().toString();
        String dstStr = dsts.getText().toString();
        String kStr = ks.getText().toString();
        String benchStr = bench.getText().toString();
        if (StringUtils.isBlank(qbStr) || !GeneralUtils.isInteger(qbStr)) {
            Toast.makeText(this, "QB count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(rbStr) || !GeneralUtils.isInteger(rbStr)) {
            Toast.makeText(this, "RB count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(wrStr) || !GeneralUtils.isInteger(wrStr)) {
            Toast.makeText(this, "WR count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(teStr) || !GeneralUtils.isInteger(teStr)) {
            Toast.makeText(this, "TE count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(dstStr) || !GeneralUtils.isInteger(dstStr)) {
            Toast.makeText(this, "DST count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(kStr) || !GeneralUtils.isInteger(kStr)) {
            Toast.makeText(this, "K count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(benchStr) || !GeneralUtils.isInteger(benchStr)) {
            Toast.makeText(this, "Bench count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Map<String, String> getRosterUpdates(EditText qbs, EditText rbs, EditText wrs, EditText tes, EditText dsts,
                                                 EditText ks, EditText bench, LeagueSettings league) {
        int qbTotal = Integer.parseInt(qbs.getText().toString());
        int rbTotal = Integer.parseInt(rbs.getText().toString());
        int wrTotal = Integer.parseInt(wrs.getText().toString());
        int teTotal = Integer.parseInt(tes.getText().toString());
        int dstTotal = Integer.parseInt(dsts.getText().toString());
        int kTotal = Integer.parseInt(ks.getText().toString());
        int benchTotal = Integer.parseInt(bench.getText().toString());
        RosterSettings roster = league.getRosterSettings();
        Map<String, String> rosterUpdates = new HashMap<>();

        if (qbTotal != roster.getQbCount()) {
            rosterUpdates.put(Constants.QB_COUNT_COLUMN, qbs.getText().toString());
            roster.setQbCount(qbTotal);
        }
        if (rbTotal != roster.getRbCount()) {
            rosterUpdates.put(Constants.RB_COUNT_COLUMN, rbs.getText().toString());
            roster.setRbCount(rbTotal);
        }
        if (wrTotal != roster.getWrCount()) {
            rosterUpdates.put(Constants.WR_COUNT_COLUMN, wrs.getText().toString());
            roster.setWrCount(wrTotal);
        }
        if (teTotal != roster.getTeCount()) {
            rosterUpdates.put(Constants.TE_COUNT_COLUMN, tes.getText().toString());
            roster.setTeCount(teTotal);
        }
        if (dstTotal != roster.getDstCount()) {
            rosterUpdates.put(Constants.DST_COUNT_COLUMN, dsts.getText().toString());
            roster.setDstCount(dstTotal);
        }
        if (kTotal != roster.getkCount()) {
            rosterUpdates.put(Constants.K_COUNT_COLUMN, ks.getText().toString());
            roster.setkCount(kTotal);
        }
        if (benchTotal != roster.getBenchCount()) {
            rosterUpdates.put(Constants.BENCH_COUNT_COLUMN, bench.getText().toString());
            roster.setBenchCount(benchTotal);
        }
        if (rosterUpdates.size() == 0) {
            return null;
        }
        league.setRosterSettings(roster);
        return rosterUpdates;
    }

    private void displayFlex(final LeagueSettings currentLeague, final Map<String, String> leagueUpdates,
                             final Map<String, String> baseRosterUpdates) {
        View view = initializeLeagueSettingsFlex();
        final EditText rbwr = (EditText)view.findViewById(R.id.league_flex_rbwr);
        final EditText rbte = (EditText)view.findViewById(R.id.league_flex_rbte);
        final EditText rbwrte = (EditText)view.findViewById(R.id.league_flex_rbwrte);
        final EditText wrte = (EditText)view.findViewById(R.id.league_flex_wrte);
        final EditText op = (EditText)view.findViewById(R.id.league_flex_op);
        rbwr.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getRbwrCount()));
        rbte.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getRbteCount()));
        rbwrte.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getRbwrteCount()));
        wrte.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getWrteCount()));
        op.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getQbrbwrteCount()));
        Button update = (Button)findViewById(R.id.league_flex_create_default);
        update.setText("Update");
        Button advanced = (Button)findViewById(R.id.league_flex_advanced_settings);
        final Context localCopy = this;

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                    return;
                }
                Map<String, String> rosterUpdates = getFlexUpdates(rbwr, rbte, rbwrte, wrte, op, baseRosterUpdates, currentLeague);
                if (rosterUpdates == null && leagueUpdates == null) {
                    Toast.makeText(localCopy, "No updates given.", Toast.LENGTH_SHORT).show();
                }
                updateLeague(null, rosterUpdates, leagueUpdates, currentLeague);
                Toast.makeText(localCopy, currentLeague.getName() + " updated", Toast.LENGTH_SHORT).show();
            }
        });
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                    return;
                }
                Map<String, String> rosterUpdates = getFlexUpdates(rbwr, rbte, rbwrte, wrte, op, baseRosterUpdates, currentLeague);
                displayScoring(currentLeague, leagueUpdates, rosterUpdates);
            }
        });
    }

    private void displayFlexNoLeague(final LeagueSettings newLeague) {
        View view = initializeLeagueSettingsFlex();
        final EditText rbwr = (EditText)view.findViewById(R.id.league_flex_rbwr);
        final EditText rbte = (EditText)view.findViewById(R.id.league_flex_rbte);
        final EditText rbwrte = (EditText)view.findViewById(R.id.league_flex_rbwrte);
        final EditText wrte = (EditText)view.findViewById(R.id.league_flex_wrte);
        final EditText op = (EditText)view.findViewById(R.id.league_flex_op);
        Button advanced = (Button)findViewById(R.id.league_flex_advanced_settings);
        Button save = (Button)findViewById(R.id.league_flex_create_default);
        final Context localCopy = this;

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                    return;
                }
                RosterSettings.Flex defaults = getFlexSettingsFromFirstPage(rbwr, rbte, rbwrte, wrte, op);
                newLeague.getRosterSettings().setFlex(defaults);
                saveNewLeague(newLeague);
                Toast.makeText(localCopy, newLeague.getName() + " saved", Toast.LENGTH_SHORT).show();
            }
        });
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                    return;
                }
                RosterSettings.Flex defaults = getFlexSettingsFromFirstPage(rbwr, rbte, rbwrte, wrte, op);
                newLeague.getRosterSettings().setFlex(defaults);
                displayScoringNoTeam(newLeague);
            }
        });
    }

    private View initializeLeagueSettingsFlex() {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(R.layout.league_settings_flex, null);
        baseLayout.addView(child);
        return child;
    }

    private RosterSettings.Flex getFlexSettingsFromFirstPage(EditText rbwr, EditText rbte, EditText rbwrte, EditText wrte,
                                                        EditText op) {
        int rbwrTotal = Integer.parseInt(rbwr.getText().toString());
        int rbteTotal = Integer.parseInt(rbte.getText().toString());
        int rbwrteTotal = Integer.parseInt(rbwrte.getText().toString());
        int wrteTotal = Integer.parseInt(wrte.getText().toString());
        int opTotal = Integer.parseInt(op.getText().toString());
        return new RosterSettings.Flex(rbwrTotal, rbteTotal, rbwrteTotal, wrteTotal, opTotal);
    }

    private boolean validateFlexInputs(EditText rbwr, EditText rbte, EditText rbwrte, EditText wrte,
                                       EditText op) {
        String rbwrStr = rbwr.getText().toString();
        String rbteStr = rbte.getText().toString();
        String rbwrteStr = rbwrte.getText().toString();
        String wrteStr = wrte.getText().toString();
        String opStr = op.getText().toString();
        if (StringUtils.isBlank(rbwrStr) || !GeneralUtils.isInteger(rbwrStr)) {
            Toast.makeText(this, "RB/WR count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(rbteStr) || !GeneralUtils.isInteger(rbteStr)) {
            Toast.makeText(this, "RB/TE count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(rbwrteStr) || !GeneralUtils.isInteger(rbwrteStr)) {
            Toast.makeText(this, "RB/WR/TE count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(wrteStr) || !GeneralUtils.isInteger(wrteStr)) {
            Toast.makeText(this, "WR/TE count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(opStr) || !GeneralUtils.isInteger(opStr)) {
            Toast.makeText(this, "DST count must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Map<String, String> getFlexUpdates(EditText rbwr, EditText rbte, EditText rbwrte, EditText wrte,
                                               EditText op, Map<String, String> rosterUpdates, LeagueSettings league) {
        int rbwrTotal = Integer.parseInt(rbwr.getText().toString());
        int rbteTotal = Integer.parseInt(rbte.getText().toString());
        int rbwrteTotal = Integer.parseInt(rbwrte.getText().toString());
        int wrteTotal = Integer.parseInt(wrte.getText().toString());
        int opTotal = Integer.parseInt(op.getText().toString());
        RosterSettings roster = league.getRosterSettings();
        RosterSettings.Flex flex = roster.getFlex();
        if (rosterUpdates == null) {
            rosterUpdates = new HashMap<>();
        }

        if (rbwrTotal != flex.getRbwrCount()) {
            rosterUpdates.put(Constants.RBWR_COUNT_COLUMN, rbwr.getText().toString());
            flex.setRbwrCount(rbwrTotal);
        }
        if (rbteTotal != flex.getRbteCount()) {
            rosterUpdates.put(Constants.RBTE_COUNT_COLUMN, rbte.getText().toString());
            flex.setRbteCount(rbteTotal);
        }
        if (rbwrteTotal != flex.getRbwrteCount()) {
            rosterUpdates.put(Constants.RBWRTE_COUNT_COLUMN, rbwrte.getText().toString());
            flex.setRbwrteCount(rbwrteTotal);
        }
        if (wrteTotal != flex.getWrteCount()) {
            rosterUpdates.put(Constants.WRTE_COUNT_COLUMN, wrte.getText().toString());
            flex.setWrteCount(wrteTotal);
        }
        if (opTotal != flex.getQbrbwrteCount()) {
            rosterUpdates.put(Constants.QBRBWRTE_COUNT_COLUMN, op.getText().toString());
            flex.setQbrbwrteCount(opTotal);
        }
        if (rosterUpdates.size() == 0) {
            return null;
        }
        roster.setFlex(flex);
        league.setRosterSettings(roster);
        return rosterUpdates;
    }

    private void displayScoringNoTeam(final LeagueSettings newLeague) {
        View view = initializeLeagueSettingsScoring();
        final EditText passTds = (EditText) view.findViewById(R.id.league_scoring_passing_tds);
        final EditText rushTds = (EditText) view.findViewById(R.id.league_scoring_rushing_tds);
        final EditText recTds = (EditText) view.findViewById(R.id.league_scoring_receiving_tds);
        final EditText passYds = (EditText) view.findViewById(R.id.league_scoring_yds_per_passing_pt);
        final EditText rushYds = (EditText) view.findViewById(R.id.league_scoring_yds_per_rushing_point);
        final EditText recYds = (EditText) view.findViewById(R.id.league_scoring_yds_per_receiving_point);
        final EditText ints = (EditText) view.findViewById(R.id.league_scoring_ints);
        final EditText fumbles = (EditText) view.findViewById(R.id.league_scoring_fumbles);
        final EditText ppr = (EditText) view.findViewById(R.id.league_scoring_ppr);
        final Button save = (Button)view.findViewById(R.id.league_scoring_save);
        final Context localCopy = this;

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateScoringInputs(passTds, rushTds, recTds, passYds, rushYds, recYds,
                        ints, fumbles, ppr)) {
                    return;
                }
                ScoringSettings scoring = getScoringSettingsFromFirstPage(passTds, rushTds, recTds, passYds, rushYds, recYds,
                        ints, fumbles, ppr);
                newLeague.setScoringSettings(scoring);
                saveNewLeague(newLeague);
                Toast.makeText(localCopy, newLeague.getName() + " saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayScoring(final LeagueSettings currentLeague, final Map<String, String> leagueUpdates,
                             final Map<String, String> rosterUpdates) {
        View view = initializeLeagueSettingsScoring();
        final EditText passTds = (EditText) view.findViewById(R.id.league_scoring_passing_tds);
        final EditText rushTds = (EditText) view.findViewById(R.id.league_scoring_rushing_tds);
        final EditText recTds = (EditText) view.findViewById(R.id.league_scoring_receiving_tds);
        final EditText passYds = (EditText) view.findViewById(R.id.league_scoring_yds_per_passing_pt);
        final EditText rushYds = (EditText) view.findViewById(R.id.league_scoring_yds_per_rushing_point);
        final EditText recYds = (EditText) view.findViewById(R.id.league_scoring_yds_per_receiving_point);
        final EditText ints = (EditText) view.findViewById(R.id.league_scoring_ints);
        final EditText fumbles = (EditText) view.findViewById(R.id.league_scoring_fumbles);
        final EditText ppr = (EditText) view.findViewById(R.id.league_scoring_ppr);
        final Button update = (Button)view.findViewById(R.id.league_scoring_save);
        update.setText("Update");
        final Context localCopy = this;
        passTds.setText(String.valueOf(currentLeague.getScoringSettings().getPassingTds()));
        rushTds.setText(String.valueOf(currentLeague.getScoringSettings().getRushingTds()));
        recTds.setText(String.valueOf(currentLeague.getScoringSettings().getReceivingTds()));
        passYds.setText(String.valueOf(currentLeague.getScoringSettings().getPassingYards()));
        rushYds.setText(String.valueOf(currentLeague.getScoringSettings().getRushingYards()));
        recYds.setText(String.valueOf(currentLeague.getScoringSettings().getReceivingYards()));
        ints.setText(String.valueOf(currentLeague.getScoringSettings().getInterceptions()));
        fumbles.setText(String.valueOf(currentLeague.getScoringSettings().getFumbles()));
        ppr.setText(String.valueOf(currentLeague.getScoringSettings().getReceptions()));

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateScoringInputs(passTds, rushTds, recTds, passYds, rushYds, recYds,
                        ints, fumbles, ppr)) {
                    return;
                }
                Map<String, String> scoringUpdates = getScoringUpdates(passTds, rushTds, recTds, passYds, rushYds, recYds,
                        ints, fumbles, ppr, currentLeague);
                if (rosterUpdates == null && leagueUpdates == null && scoringUpdates == null) {
                    Toast.makeText(localCopy, "No updates given.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateLeague(scoringUpdates, rosterUpdates, leagueUpdates, currentLeague);
                Toast.makeText(localCopy, currentLeague.getName() + " updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View initializeLeagueSettingsScoring() {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(R.layout.league_settings_scoring, null);
        baseLayout.addView(child);
        return child;
    }

    private ScoringSettings getScoringSettingsFromFirstPage(EditText passTds, EditText rushTds, EditText recTds, EditText passYds,
                                                         EditText rushYds, EditText recYds, EditText ints, EditText fumbles,
                                                            EditText receptions) {
        int passTdsTotal = Integer.parseInt(passTds.getText().toString());
        int rushTdsTotal = Integer.parseInt(rushTds.getText().toString());
        int recTdsTotal = Integer.parseInt(recTds.getText().toString());
        int passYdsTotal = Integer.parseInt(passYds.getText().toString());
        int rushYdsTotal = Integer.parseInt(rushYds.getText().toString());
        int recYdsTotal = Integer.parseInt(recYds.getText().toString());
        int intsTotal = Integer.parseInt(ints.getText().toString());
        int fumblesTotal = Integer.parseInt(fumbles.getText().toString());
        int receptionsTotal = Integer.parseInt(receptions.getText().toString());
        return new ScoringSettings(passTdsTotal, rushTdsTotal, recTdsTotal, fumblesTotal, intsTotal, passYdsTotal,
                rushYdsTotal, recYdsTotal, receptionsTotal);
    }

    private boolean validateScoringInputs(EditText passTds, EditText rushTds, EditText recTds, EditText passYds,
                                          EditText rushYds, EditText recYds, EditText ints, EditText fumbles,
                                          EditText receptions) {
        String pTdsStr = passTds.getText().toString();
        String ruTdsStr = rushTds.getText().toString();
        String reTdsStr = recTds.getText().toString();
        String pYdsStr = passYds.getText().toString();
        String ruYdsStr = rushYds.getText().toString();
        String reYdsStr = recYds.getText().toString();
        String intStr = ints.getText().toString();
        String fumblesStr = fumbles.getText().toString();
        String recStr = receptions.getText().toString();
        if (StringUtils.isBlank(pTdsStr) || !GeneralUtils.isInteger(pTdsStr)) {
            Toast.makeText(this, "Pts/passing td must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(ruTdsStr) || !GeneralUtils.isInteger(ruTdsStr)) {
            Toast.makeText(this, "Pts/rushing td must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(reTdsStr) || !GeneralUtils.isInteger(reTdsStr)) {
            Toast.makeText(this, "Pts/receiving td must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(pYdsStr) || !GeneralUtils.isInteger(pYdsStr)) {
            Toast.makeText(this, "Passing yards/point must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(ruYdsStr) || !GeneralUtils.isInteger(ruYdsStr)) {
            Toast.makeText(this, "Rushing yards/point must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(reYdsStr) || !GeneralUtils.isInteger(reYdsStr)) {
            Toast.makeText(this, "Receiving yards/point must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(intStr) || !GeneralUtils.isInteger(intStr)) {
            Toast.makeText(this, "Points/int must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(fumblesStr) || !GeneralUtils.isInteger(fumblesStr)) {
            Toast.makeText(this, "Points/fumble must be an integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isBlank(recStr) || !GeneralUtils.isDouble(recStr)) {
            Toast.makeText(this, "Points/reception must be an number (decimals allowed)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Map<String, String> getScoringUpdates(EditText passTds, EditText rushTds, EditText recTds, EditText passYds,
                                                 EditText rushYds, EditText recYds, EditText ints, EditText fumbles,
                                                 EditText receptions, LeagueSettings league) {
        int passTdsTotal = Integer.parseInt(passTds.getText().toString());
        int rushTdsTotal = Integer.parseInt(rushTds.getText().toString());
        int recTdsTotal = Integer.parseInt(recTds.getText().toString());
        int passYdsTotal = Integer.parseInt(passYds.getText().toString());
        int rushYdsTotal = Integer.parseInt(rushYds.getText().toString());
        int recYdsTotal = Integer.parseInt(recYds.getText().toString());
        int intsTotal = Integer.parseInt(ints.getText().toString());
        int fumblesTotal = Integer.parseInt(fumbles.getText().toString());
        double receptionsTotal = Double.parseDouble(receptions.getText().toString());
        Map<String, String> scoringUpdates = new HashMap<>();
        ScoringSettings scoring = league.getScoringSettings();

        if (passTdsTotal != scoring.getPassingTds()) {
            scoringUpdates.put(Constants.PASSING_TDS_COLUMN, passTds.getText().toString());
            scoring.setPassingTds(passTdsTotal);
        }
        if (rushTdsTotal != scoring.getRushingTds()) {
            scoringUpdates.put(Constants.RUSHING_TDS_COLUMN, rushTds.getText().toString());
            scoring.setRushingTds(rushTdsTotal);
        }
        if (recTdsTotal != scoring.getReceivingTds()) {
            scoringUpdates.put(Constants.RECEIVING_TDS_COLUMN, recTds.getText().toString());
            scoring.setReceivingTds(recTdsTotal);
        }
        if (passYdsTotal != scoring.getPassingYards()) {
            scoringUpdates.put(Constants.PASSING_YARDS_COLUMN, passYds.getText().toString());
            scoring.setPassingYards(passYdsTotal);
        }
        if (rushYdsTotal != scoring.getRushingYards()) {
            scoringUpdates.put(Constants.RUSHING_YARDS_COLUMN, rushYds.getText().toString());
            scoring.setRushingYards(rushYdsTotal);
        }
        if (recYdsTotal != scoring.getReceivingYards()) {
            scoringUpdates.put(Constants.RECEIVING_YARDS_COLUMN, recYds.getText().toString());
            scoring.setReceivingYards(recYdsTotal);
        }
        if (intsTotal != scoring.getInterceptions()) {
            scoringUpdates.put(Constants.INTERCEPTIONS_COLUMN, ints.getText().toString());
            scoring.setInterceptions(intsTotal);
        }
        if (fumblesTotal != scoring.getFumbles()) {
            scoringUpdates.put(Constants.FUMBLES_COLUMN, fumbles.getText().toString());
            scoring.setFumbles(fumblesTotal);
        }
        if (receptionsTotal != scoring.getReceptions()) {
            scoringUpdates.put(Constants.RECEPTIONS_COLUMN, receptions.getText().toString());
            scoring.setReceptions(receptionsTotal);
        }
        if (scoringUpdates.size() == 0) {
            return null;
        }
        league.setScoringSettings(scoring);
        return scoringUpdates;
    }

    private void saveNewLeague(LeagueSettings league) {
        rankingsDB.insertLeague(this, league);
        setCurrentLeague(league);
        initLeagues();
        rankingsUpdated = true;
    }

    private void setCurrentLeague(LeagueSettings league) {
        LocalSettingsHelper.saveCurrentLeagueName(this, league.getName());
    }

    private void deleteLeague(LeagueSettings league) {
        rankingsDB.deleteLeague(this, league);
        leagues.remove(league.getName());
        currLeague = leagues.get(leagues.keySet().iterator().next());
        initializeLeagueSpinner();
        Toast.makeText(this, league.getName() + " deleted", Toast.LENGTH_SHORT).show();
        rankingsUpdated = true;
    }

    private void updateLeague(Map<String, String> scoringUpdates, Map<String, String> rosterUpdates,
                              Map<String, String> leagueUpdates, LeagueSettings league) {
        rankingsDB.updateLeague(this, leagueUpdates, rosterUpdates, scoringUpdates, league);
        setCurrentLeague(league);
        initLeagues();
        if (leagueUpdates != null && (leagueUpdates.containsKey(Constants.IS_AUCTION_COLUMN) ||
                        leagueUpdates.containsKey(Constants.AUCTION_BUDGET_COLUMN))) {
            rankingsUpdated = true;
        }
    }
}
