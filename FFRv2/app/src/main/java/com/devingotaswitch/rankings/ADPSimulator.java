package com.devingotaswitch.rankings;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.ScoringSettings;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;
import com.devingotaswitch.utils.ParsingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_adp_simulator);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("ADP Simulator");
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
        searchInput = (AutoCompleteTextView)findViewById(R.id.adp_player_selection);
        searchInput.setAdapter(null);

        final List<Map<String, String>> data = new ArrayList<>();
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !StringUtils.isBlank(player.getTeamName()) && player.getTeamName().length() > 3 &&
                    !Constants.DST.equals(player.getPosition())) {

                Map<String, String> datum = new HashMap<>();
                datum.put(Constants.DROPDOWN_MAIN, player.getName());
                datum.put(Constants.DROPDOWN_SUB, player.getPosition() + Constants.POS_TEAM_DELIMITER + player.getTeamName());
                data.add(datum);
            }
        }
        List<Map<String, String>> dataSorted = GeneralUtils.sortData(data);
        final SimpleAdapter mAdapter = new SimpleAdapter(this, dataSorted,
                android.R.layout.simple_list_item_2, new String[] { Constants.DROPDOWN_MAIN,
                Constants.DROPDOWN_SUB }, new int[] { android.R.id.text1,
                android.R.id.text2 });
        searchInput.setAdapter(mAdapter);

        searchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                String posAndTeam = ((TextView)view.findViewById(android.R.id.text2)).getText().toString();
                String pos = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[0];
                String team = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[1];
                setPlayerToBeChecked(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos);
            }
        });

        result = (TextView)findViewById(R.id.adp_output_view);
        roundInput = (EditText)findViewById(R.id.adp_pick_round);
        pickInput = (EditText)findViewById(R.id.adp_pick_in_round);
        Button submit = (Button)findViewById(R.id.adp_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerToSearch == null) {
                    Toast.makeText(getApplicationContext(),
                            "Invalid player input, please use the dropdown to help pick a player", Toast.LENGTH_LONG).show();
                    return;
                }
                String roundStr = roundInput.getText().toString();
                String pickStr = pickInput.getText().toString();
                if (!GeneralUtils.isInteger(roundStr) || !GeneralUtils.isInteger(pickStr)) {
                    Toast.makeText(getApplicationContext(),
                            "Invalid round/pick info given, both must be given numbers", Toast.LENGTH_SHORT).show();
                    return;
                }
                int round = Integer.parseInt(roundStr);
                int pick = Integer.parseInt(pickStr);
                if (pick > rankings.getLeagueSettings().getTeamCount()) {
                    Toast.makeText(getApplicationContext(),
                            "Invalid pick given, pick was more than number of teams configured", Toast.LENGTH_SHORT).show();
                    return;
                }
                int overallPick = ((round - 1) * rankings.getLeagueSettings().getTeamCount()) + pick;
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
        clearInputs();
    }

    private void clearInputs() {
        playerToSearch = null;
        searchInput.setText("");
        pickInput.setText("");
        roundInput.setText("");
    }

    private class ParseADPOdds extends AsyncTask<Object, Void, String> {
        private ProgressDialog pdia;
        private Rankings rankings;

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
            // TODO Tool seems to be broken for this. When fixed, add back in.
            /*if (roster.getQbCount() > 1 || (roster.getFlex() != null && roster.getQbCount() > 0 &&
                    roster.getFlex().getQbrbwrteCount() > 0)) {
                type = "2qb";
            } else */if (scoring.getReceptions() > 0.0) {
                type = "ppr";
            }
            String teams = "8";
            int numTeams = rankings.getLeagueSettings().getTeamCount();
            if (numTeams >= 14) {
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
            List<String> td = JsoupUtils.handleLists(url,
                    "table.scenario-calculator td");
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