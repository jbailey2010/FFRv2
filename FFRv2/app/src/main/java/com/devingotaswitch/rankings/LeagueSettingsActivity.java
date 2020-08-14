package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueSettingsActivity extends AppCompatActivity {
    private final String TAG="LeagueSettings";
    private final String CREATE_NEW_LEAGUE_SPINNER_ITEM = "Create New League";

    private RankingsDBWrapper rankingsDB;
    private LinearLayout baseLayout;
    private Rankings rankings;
    private TextView main_title;
    private boolean rankingsUpdated;

    private Map<String, LeagueSettings> leagues;
    private LeagueSettings currLeague;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        Toolbar toolbar =  findViewById(R.id.toolbar_league_settings);
        toolbar.setTitle("");
        main_title =  findViewById(R.id.main_toolbar_title);
        main_title.setText("League Settings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        rankingsUpdated = false;

        final Activity localCopy = this;
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RankingsHome.class);
            intent.putExtra(Constants.RANKINGS_UPDATED, rankingsUpdated);
            localCopy.startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            init();
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            onBackPressed();
        }
    }

    private void init() {
        if (rankingsDB == null) {
            rankingsDB = new RankingsDBWrapper();
        }
        rankings = Rankings.init();
        baseLayout =  findViewById(R.id.league_settings_base);
        initLeagues();
        GeneralUtils.hideKeyboard(this);
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
        NiceSpinner spinner =  findViewById(R.id.league_settings_spinner);
        if (leagues.isEmpty()) {
            spinner.setVisibility(View.GONE);
            return;
        }
        spinner.setVisibility(View.VISIBLE);
        final List<String> leagueNames = new ArrayList<>();
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
        spinner.attachDataSource(leagueNames);
        spinner.setSelectedIndex(currLeagueIndex);
        spinner.setBackgroundColor(Color.parseColor("#FAFAFA"));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (CREATE_NEW_LEAGUE_SPINNER_ITEM.equals(leagueNames.get(i))) {
                    displayNoLeague();
                } else {
                    displayLeague(leagues.get(leagueNames.get(i)));
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
        main_title.setText("League Settings");
        final EditText leagueName = view.findViewById(R.id.league_settings_name);
        leagueName.setText(currentLeague.getName());
        leagueName.setVisibility(View.GONE);

        final EditText teamCount = view.findViewById(R.id.league_settings_team_count);
        teamCount.setText(String.valueOf(currentLeague.getTeamCount()));
        final EditText auctionBudget = view.findViewById(R.id.league_settings_auction_budget);
        GeneralUtils.hideKeyboard(this);

        final RadioButton isAuction = view.findViewById(R.id.league_settings_auction);
        final RadioButton isSnake = view.findViewById(R.id.league_settings_snake);
        final RadioButton isDynasty = view.findViewById(R.id.league_settings_dynasty_startup);
        final RadioButton isRookie = view.findViewById(R.id.league_settings_dynasty_rookie);
        final RadioButton isBestBall = view.findViewById(R.id.league_settings_best_ball);

        if (currentLeague.isAuction()) {
            isAuction.setSelected(true);
            isAuction.setChecked(true);
            auctionBudget.setText(String.valueOf(currentLeague.getAuctionBudget()));
        } else if (currentLeague.isSnake()){
            isSnake.setSelected(true);
            isSnake.setChecked(true);
        } else if (currentLeague.isDynasty()) {
            isDynasty.setSelected(true);
            isDynasty.setChecked(true);
        } else if (currentLeague.isRookie()) {
            isRookie.setSelected(true);
            isRookie.setChecked(true);
        } else if (currentLeague.isBestBall()) {
            isBestBall.setSelected(true);
            isBestBall.setChecked(true);
        }
        Button delete = findViewById(R.id.league_settings_delete_league);
        delete.setVisibility(View.VISIBLE);
        Button save =  view.findViewById(R.id.league_settings_create_default);
        save.setText("Update");
        Button advanced =  view.findViewById(R.id.league_settings_advanced_settings);
        final Activity act = this;
        save.setOnClickListener(view12 -> {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                return;
            }
            Map<String, String> updates = getLeagueUpdates(currentLeague, leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget);
            if (updates == null) {
                GeneralUtils.hideKeyboard(act);
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show();
                return;
            }
            updateLeague(null, null, updates, currentLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", currentLeague.getName() + " updated", Flashbar.Gravity.BOTTOM)
                    .show();
        });
        advanced.setOnClickListener(view1 -> {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                return;
            }
            Map<String, String> updates = getLeagueUpdates(currentLeague, leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget);
            displayRoster(currentLeague, updates);
        });
        delete.setOnClickListener(v -> {
            if (leagues.size() > 1) {
                deleteLeague(currentLeague);
            } else {
                GeneralUtils.hideKeyboard(act);
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "Can't delete league, none would remain", Flashbar.Gravity.BOTTOM)
                        .show();
            }
        });

        setCurrentLeague(currentLeague);
    }

    private void displayNoLeague() {
        View view = initializeLeagueSettingsBase();
        main_title.setText("League Settings");
        final Button advanced = view.findViewById(R.id.league_settings_advanced_settings);
        final Button save =  view.findViewById(R.id.league_settings_create_default);
        Button delete =  view.findViewById(R.id.league_settings_delete_league);
        delete.setVisibility(View.GONE);
        final EditText leagueName = view.findViewById(R.id.league_settings_name);
        leagueName.setVisibility(View.VISIBLE);
        final EditText teamCount = view.findViewById(R.id.league_settings_team_count);
        final EditText auctionBudget = view.findViewById(R.id.league_settings_auction_budget);
        final RadioButton isAuction = view.findViewById(R.id.league_settings_auction);
        final RadioButton isSnake = view.findViewById(R.id.league_settings_snake);
        final RadioButton isDynasty = view.findViewById(R.id.league_settings_dynasty_startup);
        final RadioButton isRookie = view.findViewById(R.id.league_settings_dynasty_rookie);
        final RadioButton isBestBall = view.findViewById(R.id.league_settings_best_ball);
        isSnake.setChecked(true);

        leagueName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // don't care
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().length()==0) {
                    deactivateButton(advanced);
                    deactivateButton(save);
                } else if (GeneralUtils.isInteger(teamCount.getText().toString())){
                    activateButton(advanced);
                    activateButton(save);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Don't care
            }
        });

        teamCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Don't care
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!GeneralUtils.isInteger(charSequence.toString())) {
                    deactivateButton(save);
                    deactivateButton(advanced);
                } else if (!StringUtils.isBlank(leagueName.getText().toString())){
                    activateButton(save);
                    activateButton(advanced);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Don't care
            }
        });
        deactivateButton(save);
        deactivateButton(advanced);

        final Activity act = this;
        save.setOnClickListener(view12 -> {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                return;
            }
            LeagueSettings defaults = getLeagueSettingsFromFirstPage(leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget);
            saveNewLeague(defaults);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", defaults.getName() + " saved", Flashbar.Gravity.BOTTOM)
                    .show();
        });
        advanced.setOnClickListener(view1 -> {
            if (validateLeagueInputs(leagueName, teamCount, auctionBudget, isAuction)) {
                return;
            }
            LeagueSettings defaults = getLeagueSettingsFromFirstPage(leagueName, teamCount, isAuction, isSnake,
                    isDynasty, isRookie, isBestBall, auctionBudget);
            displayRosterNoLeague(defaults);
        });
    }

    private void deactivateButton(Button button) {
        button.setClickable(false);
        button.setEnabled(false);
        button.setBackgroundColor(0xFFE1E1E1);
    }

    private void activateButton(Button button) {
        button.setClickable(true);
        button.setEnabled(true);
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default));
    }

    private boolean validateLeagueInputs(EditText name, EditText teamCount, EditText auctionBudget, RadioButton isAuction) {
        String givenName = name.getText().toString();
        String givenTeamCount = teamCount.getText().toString();
        String givenAuctionBudget = auctionBudget.getText().toString();
        if (StringUtils.isBlank(givenName)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "League name can't be empty", Flashbar.Gravity.TOP)
                    .show();
            return true;
        } if (StringUtils.isBlank(givenTeamCount) ||
                !GeneralUtils.isInteger(givenTeamCount)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Team count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        int teamCountInt = Integer.parseInt(givenTeamCount);
        if (teamCountInt < 1 || teamCountInt > 32) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Invalid team count given", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }

        if (isAuction.isChecked()) {
            if (StringUtils.isBlank(givenAuctionBudget) || !GeneralUtils.isInteger(givenAuctionBudget)) {
                FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Auction budget not provided", Flashbar.Gravity.TOP)
                        .show();
                return true;
            }
            int auctionBudgetInt = Integer.parseInt(givenAuctionBudget);
            if (auctionBudgetInt < 1) {
                FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Auction budget must be a positive number", Flashbar.Gravity.TOP)
                        .show();
                return true;
            }
        }
        return false;
    }

    private Map<String, String> getLeagueUpdates(LeagueSettings league, EditText name, EditText teamCount,
                                                 RadioButton isAuction, RadioButton isSnake, RadioButton isDynasty,
                                                 RadioButton isRookie, RadioButton isBestBall, EditText auctionBudget) {
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
        if (isSnake.isChecked() != league.isSnake()) {
            updates.put(Constants.IS_SNAKE_COLUMN, isSnake.isChecked() ? "1" : "0");
            league.setSnake(isSnake.isChecked());
        }
        if (isDynasty.isChecked() != league.isDynasty()) {
            updates.put(Constants.IS_DYNASTY_STARTUP_COLUMN, isDynasty.isChecked() ? "1" : "0");
            league.setDynasty(isDynasty.isChecked());
        }
        if(isRookie.isChecked() != league.isRookie()) {
            updates.put(Constants.IS_DYNASTY_ROOKIE_COLUMN, isRookie.isChecked() ? "1" : "0");
            league.setRookie(isRookie.isChecked());
        }
        if (isBestBall.isChecked() != league.isBestBall()) {
            updates.put(Constants.IS_BEST_BALL_COLUMN, isBestBall.isChecked() ? "1" : "0");
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
                                                          RadioButton isSnake, RadioButton isDynasty, RadioButton isRookie,
                                                          RadioButton isBestBall, EditText auctionBudget) {

        int realBudget = 200;
        if (GeneralUtils.isInteger(auctionBudget.getText().toString())) {
            realBudget = Integer.parseInt(auctionBudget.getText().toString());
        }
        return new LeagueSettings(leagueName.getText().toString(),
                Integer.parseInt(teamCount.getText().toString()), isSnake.isChecked(), isAuction.isChecked(),
                isDynasty.isChecked(), isRookie.isChecked(), isBestBall.isChecked(), realBudget);
    }

    private View initializeLeagueSettingsBase() {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(R.layout.league_settings_base, null);
        baseLayout.addView(child);

        // Hide auction budget on snake selection
        final EditText auctionBudget = child.findViewById(R.id.league_settings_auction_budget);
        final TextView auctionBudgetHeader = child.findViewById(R.id.league_settings_auction_budget_header);
        RadioButton isAuction = child.findViewById(R.id.league_settings_auction);
        isAuction.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                auctionBudget.setVisibility(View.VISIBLE);
                auctionBudgetHeader.setVisibility(View.VISIBLE);
            } else {
                auctionBudget.setVisibility(View.GONE);
                auctionBudgetHeader.setVisibility(View.GONE);
            }
        });
        return child;
    }

    private void displayRoster(final LeagueSettings currentLeague, final Map<String, String> leagueUpdates) {
        View view = initializeLeagueSettingsRoster();
        main_title.setText("Roster Settings");
        Button update =  view.findViewById(R.id.league_roster_create_default);
        update.setText("Update");
        Button advanced =  view.findViewById(R.id.league_roster_advanced_settings);
        RosterSettings roster = currentLeague.getRosterSettings();
        final EditText qbs = view.findViewById(R.id.league_settings_qbs);
        qbs.setText(String.valueOf(roster.getQbCount()));
        final EditText rbs = view.findViewById(R.id.league_settings_rbs);
        rbs.setText(String.valueOf(roster.getRbCount()));
        final EditText wrs = view.findViewById(R.id.league_settings_wrs);
        wrs.setText(String.valueOf(roster.getWrCount()));
        final EditText tes = view.findViewById(R.id.league_settings_tes);
        tes.setText(String.valueOf(roster.getTeCount()));
        final EditText dsts = view.findViewById(R.id.league_settings_dsts);
        dsts.setText(String.valueOf(roster.getDstCount()));
        final EditText ks = view.findViewById(R.id.league_settings_ks);
        ks.setText(String.valueOf(roster.getKCount()));
        final EditText bench = view.findViewById(R.id.league_settings_bench);
        bench.setText(String.valueOf(roster.getBenchCount()));
        LinearLayout dstskHeader = findViewById(R.id.league_roster_space2);
        LinearLayout benchHeader = findViewById(R.id.league_roster_space3);
        if (currentLeague.isRookie()) {
            dsts.setVisibility(View.GONE);
            ks.setVisibility(View.GONE);
            bench.setVisibility(View.GONE);
            dsts.setText(String.valueOf(0));
            ks.setText(String.valueOf(0));
            bench.setText(String.valueOf(0));
            dstskHeader.setVisibility(View.GONE);
            benchHeader.setVisibility(View.GONE);
        } else {
            dsts.setVisibility(View.VISIBLE);
            ks.setVisibility(View.VISIBLE);
            bench.setVisibility(View.VISIBLE);
            dstskHeader.setVisibility(View.VISIBLE);
            benchHeader.setVisibility(View.VISIBLE);
        }

        final Activity act = this;
        update.setOnClickListener(view1 -> {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return;
            }
            Map<String, String> rosterUpdates = getRosterUpdates(qbs, rbs, wrs, tes, dsts, ks, bench, currentLeague);
            if (rosterUpdates == null && leagueUpdates == null) {
                GeneralUtils.hideKeyboard(act);
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show();
                return;
            }
            updateLeague(null, rosterUpdates, leagueUpdates, currentLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", currentLeague.getName() + " updated", Flashbar.Gravity.BOTTOM)
                    .show();
        });
        advanced.setOnClickListener(v -> {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return;
            }
            Map<String, String> rosterUpdates = getRosterUpdates(qbs, rbs, wrs, tes, dsts, ks, bench, currentLeague);
            displayFlex(currentLeague, leagueUpdates, rosterUpdates);
        });
    }

    private void displayRosterNoLeague(final LeagueSettings newLeague) {
        View view = initializeLeagueSettingsRoster();
        main_title.setText("Roster Settings");
        final EditText qbs = view.findViewById(R.id.league_settings_qbs);
        final EditText rbs = view.findViewById(R.id.league_settings_rbs);
        final EditText wrs = view.findViewById(R.id.league_settings_wrs);
        final EditText tes = view.findViewById(R.id.league_settings_tes);
        final EditText dsts = view.findViewById(R.id.league_settings_dsts);
        final EditText ks = view.findViewById(R.id.league_settings_ks);
        final EditText bench = view.findViewById(R.id.league_settings_bench);
        LinearLayout dstskHeader = findViewById(R.id.league_roster_space2);
        LinearLayout benchHeader = findViewById(R.id.league_roster_space3);
        if (newLeague.isRookie()) {
            dsts.setVisibility(View.GONE);
            ks.setVisibility(View.GONE);
            bench.setVisibility(View.GONE);
            dsts.setText(String.valueOf(0));
            ks.setText(String.valueOf(0));
            bench.setText(String.valueOf(0));
            dstskHeader.setVisibility(View.GONE);
            benchHeader.setVisibility(View.GONE);
        } else {
            dsts.setVisibility(View.VISIBLE);
            ks.setVisibility(View.VISIBLE);
            bench.setVisibility(View.VISIBLE);
            dstskHeader.setVisibility(View.VISIBLE);
            benchHeader.setVisibility(View.VISIBLE);
            dsts.setText("1");
            ks.setText("1");
            bench.setText("6");
        }
        Button save =  view.findViewById(R.id.league_roster_create_default);
        Button advanced =  view.findViewById(R.id.league_roster_advanced_settings);

        final Activity act = this;
        save.setOnClickListener(view12 -> {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return;
            }
            RosterSettings defaults = getRosterSettingsFromFirstPage(qbs, rbs, wrs, tes, dsts, ks, bench);
            newLeague.setRosterSettings(defaults);
            saveNewLeague(newLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", newLeague.getName() + " saved", Flashbar.Gravity.BOTTOM)
                    .show();
        });
        advanced.setOnClickListener(view1 -> {
            if (validateRosterInputs(qbs, rbs, wrs, tes, dsts, ks, bench)) {
                return;
            }
            RosterSettings defaults = getRosterSettingsFromFirstPage(qbs, rbs, wrs, tes, dsts, ks, bench);
            newLeague.setRosterSettings(defaults);
            displayFlexNoLeague(newLeague);
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
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "QB count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(rbStr) || !GeneralUtils.isInteger(rbStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "RB count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(wrStr) || !GeneralUtils.isInteger(wrStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "WR count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(teStr) || !GeneralUtils.isInteger(teStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "TE count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(dstStr) || !GeneralUtils.isInteger(dstStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "DST count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(kStr) || !GeneralUtils.isInteger(kStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "K count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(benchStr) || !GeneralUtils.isInteger(benchStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Bench count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        return false;
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
        if (kTotal != roster.getKCount()) {
            rosterUpdates.put(Constants.K_COUNT_COLUMN, ks.getText().toString());
            roster.setKCount(kTotal);
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
        main_title.setText("Flex Settings");
        final EditText rbwr = view.findViewById(R.id.league_flex_rbwr);
        final EditText rbte = view.findViewById(R.id.league_flex_rbte);
        final EditText rbwrte = view.findViewById(R.id.league_flex_rbwrte);
        final EditText wrte = view.findViewById(R.id.league_flex_wrte);
        final EditText op = view.findViewById(R.id.league_flex_op);
        rbwr.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getRbwrCount()));
        rbte.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getRbteCount()));
        rbwrte.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getRbwrteCount()));
        wrte.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getWrteCount()));
        op.setText(String.valueOf(currentLeague.getRosterSettings().getFlex().getQbrbwrteCount()));
        Button update = findViewById(R.id.league_flex_create_default);
        update.setText("Update");
        Button advanced = findViewById(R.id.league_flex_advanced_settings);

        final Activity act = this;
        update.setOnClickListener(view1 -> {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return;
            }
            Map<String, String> rosterUpdates = getFlexUpdates(rbwr, rbte, rbwrte, wrte, op, baseRosterUpdates, currentLeague);
            if (rosterUpdates == null && leagueUpdates == null) {
                GeneralUtils.hideKeyboard(act);
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                    .show();
                return;
            }
            updateLeague(null, rosterUpdates, leagueUpdates, currentLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", currentLeague.getName() + " updated", Flashbar.Gravity.BOTTOM)
                    .show();
        });
        advanced.setOnClickListener(v -> {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return;
            }
            Map<String, String> rosterUpdates = getFlexUpdates(rbwr, rbte, rbwrte, wrte, op, baseRosterUpdates, currentLeague);
            displayScoring(currentLeague, leagueUpdates, rosterUpdates);
        });
    }

    private void displayFlexNoLeague(final LeagueSettings newLeague) {
        View view = initializeLeagueSettingsFlex();
        main_title.setText("Flex Settings");
        final EditText rbwr = view.findViewById(R.id.league_flex_rbwr);
        final EditText rbte = view.findViewById(R.id.league_flex_rbte);
        final EditText rbwrte = view.findViewById(R.id.league_flex_rbwrte);
        final EditText wrte = view.findViewById(R.id.league_flex_wrte);
        final EditText op = view.findViewById(R.id.league_flex_op);
        Button advanced = findViewById(R.id.league_flex_advanced_settings);
        Button save = findViewById(R.id.league_flex_create_default);

        final Activity act = this;
        save.setOnClickListener(view12 -> {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return;
            }
            RosterSettings.Flex defaults = getFlexSettingsFromFirstPage(rbwr, rbte, rbwrte, wrte, op);
            newLeague.getRosterSettings().setFlex(defaults);
            saveNewLeague(newLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", newLeague.getName() + " saved", Flashbar.Gravity.BOTTOM)
                    .show();
        });
        advanced.setOnClickListener(view1 -> {
            if (validateFlexInputs(rbwr, rbte, rbwrte, wrte, op)) {
                return;
            }
            RosterSettings.Flex defaults = getFlexSettingsFromFirstPage(rbwr, rbte, rbwrte, wrte, op);
            newLeague.getRosterSettings().setFlex(defaults);
            displayScoringNoTeam(newLeague);
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
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "RB/WR count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(rbteStr) || !GeneralUtils.isInteger(rbteStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "RB/TE count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(rbwrteStr) || !GeneralUtils.isInteger(rbwrteStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "RB/WR/TE count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(wrteStr) || !GeneralUtils.isInteger(wrteStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "WR/TE count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(opStr) || !GeneralUtils.isInteger(opStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "QB/RB/WR/TE count not provided", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        return false;
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
        main_title.setText("Scoring Settings");
        final EditText passTds =  view.findViewById(R.id.league_scoring_passing_tds);
        final EditText rushTds =  view.findViewById(R.id.league_scoring_rushing_tds);
        final EditText recTds =  view.findViewById(R.id.league_scoring_receiving_tds);
        final EditText passYds =  view.findViewById(R.id.league_scoring_yds_per_passing_pt);
        final EditText rushYds =  view.findViewById(R.id.league_scoring_yds_per_rushing_point);
        final EditText recYds =  view.findViewById(R.id.league_scoring_yds_per_receiving_point);
        final EditText ints =  view.findViewById(R.id.league_scoring_ints);
        final EditText fumbles =  view.findViewById(R.id.league_scoring_fumbles);
        final EditText ppr =  view.findViewById(R.id.league_scoring_ppr);
        final Button save = view.findViewById(R.id.league_scoring_save);

        final Activity act = this;
        save.setOnClickListener(view1 -> {
            if (validateScoringInputs(passTds, rushTds, recTds, passYds, rushYds, recYds,
                    ints, fumbles, ppr)) {
                return;
            }
            ScoringSettings scoring = getScoringSettingsFromFirstPage(passTds, rushTds, recTds, passYds, rushYds, recYds,
                    ints, fumbles, ppr);
            newLeague.setScoringSettings(scoring);
            saveNewLeague(newLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", newLeague.getName() + " saved", Flashbar.Gravity.BOTTOM)
                    .show();
        });
    }

    private void displayScoring(final LeagueSettings currentLeague, final Map<String, String> leagueUpdates,
                             final Map<String, String> rosterUpdates) {
        View view = initializeLeagueSettingsScoring();
        main_title.setText("Scoring Settings");
        final EditText passTds =  view.findViewById(R.id.league_scoring_passing_tds);
        final EditText rushTds =  view.findViewById(R.id.league_scoring_rushing_tds);
        final EditText recTds =  view.findViewById(R.id.league_scoring_receiving_tds);
        final EditText passYds =  view.findViewById(R.id.league_scoring_yds_per_passing_pt);
        final EditText rushYds =  view.findViewById(R.id.league_scoring_yds_per_rushing_point);
        final EditText recYds =  view.findViewById(R.id.league_scoring_yds_per_receiving_point);
        final EditText ints =  view.findViewById(R.id.league_scoring_ints);
        final EditText fumbles =  view.findViewById(R.id.league_scoring_fumbles);
        final EditText ppr =  view.findViewById(R.id.league_scoring_ppr);
        final Button update = view.findViewById(R.id.league_scoring_save);
        update.setText("Update");
        passTds.setText(String.valueOf(currentLeague.getScoringSettings().getPassingTds()));
        rushTds.setText(String.valueOf(currentLeague.getScoringSettings().getRushingTds()));
        recTds.setText(String.valueOf(currentLeague.getScoringSettings().getReceivingTds()));
        passYds.setText(String.valueOf(currentLeague.getScoringSettings().getPassingYards()));
        rushYds.setText(String.valueOf(currentLeague.getScoringSettings().getRushingYards()));
        recYds.setText(String.valueOf(currentLeague.getScoringSettings().getReceivingYards()));
        ints.setText(String.valueOf(currentLeague.getScoringSettings().getInterceptions()));
        fumbles.setText(String.valueOf(currentLeague.getScoringSettings().getFumbles()));
        ppr.setText(String.valueOf(currentLeague.getScoringSettings().getReceptions()));

        final Activity act = this;
        update.setOnClickListener(view1 -> {
            if (validateScoringInputs(passTds, rushTds, recTds, passYds, rushYds, recYds,
                    ints, fumbles, ppr)) {
                return;
            }
            Map<String, String> scoringUpdates = getScoringUpdates(passTds, rushTds, recTds, passYds, rushYds, recYds,
                    ints, fumbles, ppr, currentLeague);
            if (rosterUpdates == null && leagueUpdates == null && scoringUpdates == null) {
                GeneralUtils.hideKeyboard(act);
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "No updates given", Flashbar.Gravity.BOTTOM)
                        .show();
                return;
            }
            updateLeague(scoringUpdates, rosterUpdates, leagueUpdates, currentLeague);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", currentLeague.getName() + " updated", Flashbar.Gravity.BOTTOM)
                    .show();
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
        double intsTotal = Double.parseDouble(ints.getText().toString());
        double fumblesTotal = Double.parseDouble(fumbles.getText().toString());
        double receptionsTotal = Double.parseDouble(receptions.getText().toString());
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
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Pts/passing td must be an integer", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(ruTdsStr) || !GeneralUtils.isInteger(ruTdsStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Pts/rushing td must be an integer", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(reTdsStr) || !GeneralUtils.isInteger(reTdsStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Pts/receiving td must be an integer", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(pYdsStr) || !GeneralUtils.isInteger(pYdsStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Passing yards/point must be an integer", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(ruYdsStr) || !GeneralUtils.isInteger(ruYdsStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Rushing yards/point must be an integer", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(reYdsStr) || !GeneralUtils.isInteger(reYdsStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Receiving yards/point must be an integer", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(intStr) || !GeneralUtils.isDouble(intStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Points/int must be a number (decimals allowed)", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(fumblesStr) || !GeneralUtils.isDouble(fumblesStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Points/fumble must be a number (decimals allowed)", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        if (StringUtils.isBlank(recStr) || !GeneralUtils.isDouble(recStr)) {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Points/reception must be a number (decimals allowed)", Flashbar.Gravity.TOP)
                    .show();
            return true;
        }
        return false;
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
        double intsTotal = Double.parseDouble(ints.getText().toString());
        double fumblesTotal = Double.parseDouble(fumbles.getText().toString());
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
        displayLeague(currLeague);
        FlashbarFactory.generateTextOnlyFlashbar(this, "Success!", league.getName() + " deleted", Flashbar.Gravity.BOTTOM)
                .show();
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

        if (((leagueUpdates != null && leagueUpdates.containsKey(Constants.TEAM_COUNT_COLUMN)) ||
                scoringUpdates != null || rosterUpdates != null) && !rankings.getPlayers().isEmpty()) {
            Log.d(TAG, "Updating some set");
            boolean updateProjections = false;
            if (scoringUpdates != null) {
                Log.d(TAG, "Projections to be updated, too.");
                updateProjections = true;
                rankingsUpdated = true;
            }
            rankings.updateProjectionsAndVBD(this, league, updateProjections, rankingsDB);
        }
    }
}
