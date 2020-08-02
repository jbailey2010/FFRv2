package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.util.List;

public class ADPSimulator extends AppCompatActivity {

    private Rankings rankings;

    private Player playerToSearch;

    private static final String TAG = "ADPSimulator";

    private AutoCompleteTextView searchInput;
    private EditText roundInput;
    private EditText pickInput;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adp_simulator);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        Toolbar toolbar = findViewById(R.id.toolbar_adp_simulator);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("ADP Simulator");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Activity act = this;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.hideKeyboard(act);
                onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            init();

            if (getIntent().hasExtra(Constants.PLAYER_ID)) {
                setPlayerToBeChecked(getIntent().getStringExtra(Constants.PLAYER_ID));
            }
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            GeneralUtils.hideKeyboard(this);
            onBackPressed();
        }
    }

    private void init() {
        searchInput = findViewById(R.id.adp_player_selection);
        searchInput.setAdapter(null);
        final FilterWithSpaceAdapter mAdapter = GeneralUtils.getPlayerSearchAdapter(rankings, this, false, false);
        searchInput.setAdapter(mAdapter);

        searchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setPlayerToBeChecked(GeneralUtils.getPlayerIdFromSearchView(view));
            }
        });

        result = findViewById(R.id.adp_output_view);
        roundInput = findViewById(R.id.adp_pick_round);
        pickInput = findViewById(R.id.adp_pick_in_round);
        Button submit = findViewById(R.id.adp_submit_button);
        final Activity act = this;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerToSearch == null) {
                    FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "Invalid player, use the dropdown to pick", Flashbar.Gravity.TOP)
                            .show();
                    return;
                }
                String roundStr = roundInput.getText().toString();
                String pickStr = pickInput.getText().toString();
                if (!GeneralUtils.isInteger(roundStr) || !GeneralUtils.isInteger(pickStr)) {
                    FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "Pick/round must be provided as numbers", Flashbar.Gravity.TOP)
                            .show();
                    return;
                }
                int round = Integer.parseInt(roundStr);
                int pick = Integer.parseInt(pickStr);
                if (pick > rankings.getLeagueSettings().getTeamCount()) {
                    FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "Pick can't be higher than current league team count", Flashbar.Gravity.TOP)
                            .show();
                    return;
                }
                int overallPick = ((round - 1) * rankings.getLeagueSettings().getTeamCount()) + pick;

                GeneralUtils.hideKeyboard(act);
                getADPOddsForInput(overallPick);
            }
        });
    }

    private void getADPOddsForInput(int pick) {
        ParseADPOdds oddsParser = new ParseADPOdds(this, rankings);
        oddsParser.execute(pick, playerToSearch);
    }

    private void setPlayerToBeChecked(String id) {
        playerToSearch = rankings.getPlayer(id);
        searchInput.setText(playerToSearch.getName() + ": " + playerToSearch.getPosition() + ", " + playerToSearch.getTeamName());
    }

    private void displayResult(String output) {
        result.setText(output);
        final Activity localCopy = this;
        final String playerId = playerToSearch.getUniqueId();
        result.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(localCopy, PlayerInfo.class);
                intent.putExtra(Constants.PLAYER_ID, playerId);
                startActivity(intent);
                return true;
            }
        });
        clearInputs();
    }

    private void clearInputs() {
        playerToSearch = null;
        searchInput.setText("");
        pickInput.setText("");
        roundInput.setText("");
    }

    private class ParseADPOdds extends AsyncTask<Object, Void, String> {
        private final ProgressDialog pdia;
        private final Rankings rankings;

        ParseADPOdds(ADPSimulator activity, Rankings rankings) {
            pdia = new ProgressDialog(activity);
            pdia.setCancelable(false);
            this.rankings = rankings;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia.setMessage("Please wait, doing fancy math...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdia.dismiss();
            displayResult(result);
        }

        @Override
        protected String doInBackground(Object... data) {
            int pick = (Integer) data[0];
            Player player = (Player) data[1];
            String type = "standard";
            ScoringSettings scoring = rankings.getLeagueSettings().getScoringSettings();
            RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
            if (roster.getQbCount() > 1 || (roster.getFlex() != null && roster.getQbCount() > 0 &&
                    roster.getFlex().getQbrbwrteCount() > 0)) {
                type = "2qb";
            } else if (scoring.getReceptions() > 0.0) {
                type = "ppr";
            }
            String teams = "8";
            int numTeams = rankings.getLeagueSettings().getTeamCount();
            if (numTeams >= 14 && !"2qb".equals(type)) {
                teams = "14";
            } else if (numTeams >= 12) {
                teams = "12";
            } else if (numTeams >= 10) {
                teams = "10";
            }

            String url = "https://fantasyfootballcalculator.com/scenario-calculator?format="
                    + type + "&num_teams=" + teams + "&draft_pick=" + pick;
            Log.d(TAG, url);
            ParsingUtils.init();
            String first = getPlayerADPOdds(url, player, pick);
            if (scoring.getReceptions() > 0.0 && first.contains("error") && url.contains("ppr")) {
                first = getPlayerADPOdds(url.replace("ppr", "standard"), player, pick);
            }
            return first;
        }
    }

    private String getPlayerADPOdds(String url, Player player, int pick) {
        try {
            List<String> td = JsoupUtils.parseURLWithUA(url,
                    "table.table td");
            for (int i = 0; i < td.size(); i+=6) {
                String possibleName = ParsingUtils.normalizeNames(td.get(i));
                String possiblePos = td.get(i+1);
                String possibleTeam = ParsingUtils.normalizeTeams(td.get(i+2));
                if (possiblePos.equals(player.getPosition()) && possibleTeam.equals(player.getTeamName()) &&
                        (possibleName.equals(player.getName().replaceAll("\\.", "")) ||
                        possibleName.equals(player.getName()))) {
                    return "Odds " + player.getName() + " is available at pick " + pick
                            + ": " + td.get(i + 4);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get player adp likelihood", e);
        }
        return "An error occurred. Either the data is unavailable, or the internet may have dropped.";
    }
}
