package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.sources.ParsePlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.JsoupUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerComparator extends AppCompatActivity {

    private Rankings rankings;
    private RankingsDBWrapper rankingsDB;
    private Player playerA;
    private Player playerB;

    private AutoCompleteTextView inputA;
    private AutoCompleteTextView inputB;

    private static final String TAG = "PlayerComparator";
    private static final String BETTER_COLOR = "#F3F3F3";
    private static final String WORSE_COLOR = "#FAFAFA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_comparator);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankingsDB = new RankingsDBWrapper();
        rankings = Rankings.init();

        // Set toolbar for this screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.player_comparator_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Compare Players");
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
        final List<Map<String, String>> data = new ArrayList<>();
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !StringUtils.isBlank(player.getTeamName()) && player.getTeamName().length() > 3 &&
                    !Constants.DST.equals(player.getPosition()) && !Constants.K.equals(player.getPosition())) {

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

        inputA = (AutoCompleteTextView) findViewById(R.id.comparator_input_a);
        inputB = (AutoCompleteTextView) findViewById(R.id.comparator_input_b);
        inputA.setAdapter(mAdapter);
        inputB.setAdapter(mAdapter);

        inputA.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearInputs();
                return true;
            }
        });
        inputB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearInputs();
                return true;
            }
        });
        inputA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playerA = getPlayerFromView(view);
                inputA.setText(playerA.getName());
                if (playerB != null && playerA.getUniqueId().equals(playerB.getUniqueId())) {
                    Toast.makeText(getApplicationContext(), "Please select two different players", Toast.LENGTH_SHORT).show();
                } else if (playerB != null) {
                    displayResults(playerA, playerB);
                } else {
                    Toast.makeText(getApplicationContext(), playerA.getName() + " selected, please pick a second player", Toast.LENGTH_SHORT).show();
                }
            }
        });
        inputB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playerB = getPlayerFromView(view);
                inputB.setText(playerB.getName());
                if (playerA != null && playerA.getUniqueId().equals(playerB.getUniqueId())) {
                    Toast.makeText(getApplicationContext(), "Please select two different players", Toast.LENGTH_SHORT).show();
                } else if (playerA != null) {
                    displayResults(playerA, playerB);
                } else {
                    Toast.makeText(getApplicationContext(), playerB.getName() + " selected, please pick a second player", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Player getPlayerFromView(View view) {
        String name = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
        String posAndTeam = ((TextView)view.findViewById(android.R.id.text2)).getText().toString();
        String pos = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[0];
        String team = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[1];
        return rankings.getPlayer(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos);
    }

    private void clearInputs() {
        playerA = null;
        playerB = null;
        inputA.setText("");
        inputB.setText("");
    }

    private void displayResults(final Player playerA, final Player playerB) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        clearInputs();
        ParseFP parseFP = new ParseFP(this, playerA, playerB);
        parseFP.execute();

        LinearLayout outputBase = (LinearLayout)findViewById(R.id.comparator_output_base);
        outputBase.setVisibility(View.VISIBLE);

        // Name
        TextView nameA = (TextView)findViewById(R.id.comparator_name_a);
        TextView nameB = (TextView)findViewById(R.id.comparator_name_b);
        nameA.setText(playerA.getName());
        nameB.setText(playerB.getName());
        nameA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPlayerInfo(playerA);
            }
        });
        nameB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPlayerInfo(playerB);
            }
        });

        // Age
        TextView ageA = (TextView)findViewById(R.id.comparator_age_a);
        TextView ageB = (TextView)findViewById(R.id.comparator_age_b);
        ageA.setText(playerA.getAge() > 0 ? String.valueOf(playerA.getAge()) : "?");
        ageB.setText(playerA.getAge() > 0 ? String.valueOf(playerB.getAge()) : "?");

        // ECR (default to hidden)
        LinearLayout ecrRow = (LinearLayout)findViewById(R.id.expert_output_row);
        ecrRow.setVisibility(View.GONE);

        // ECR val
        TextView ecrA = (TextView)findViewById(R.id.comparator_ecr_val_a);
        TextView ecrB = (TextView) findViewById(R.id.comparator_ecr_val_b);
        ecrA.setText(String.valueOf(playerA.getEcr()));
        ecrB.setText(String.valueOf(playerB.getEcr()));
        if (playerA.getEcr() < playerB.getEcr()) {
            ecrA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            ecrB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            ecrA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            ecrB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }

        //ADP
        TextView adpA = (TextView)findViewById(R.id.comparator_adp_a);
        TextView adpB = (TextView)findViewById(R.id.comparator_adp_b);
        adpA.setText(String.valueOf(playerA.getAdp()));
        adpB.setText(String.valueOf(playerB.getAdp()));
        if (playerA.getAdp() < playerB.getAdp()) {
            adpA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            adpB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            adpA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            adpB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }

        // SOS
        TextView sosA = (TextView)findViewById(R.id.comparator_sos_a);
        TextView sosB = (TextView)findViewById(R.id.comparator_sos_b);
        int sosForA = rankings.getTeam(playerA).getSosForPosition(playerA.getPosition());
        int sosForB = rankings.getTeam(playerB).getSosForPosition(playerB.getPosition());
        sosA.setText(String.valueOf(sosForA));
        sosB.setText(String.valueOf(sosForB));
        if (sosForA < sosForB) {
            sosA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            sosB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            sosA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            sosB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }

        // Projection
        DecimalFormat df = new DecimalFormat("#.##");
        TextView projA = (TextView)findViewById(R.id.comparator_proj_a);
        TextView projB = (TextView)findViewById(R.id.comparator_proj_b);
        projA.setText(df.format(playerA.getProjection()));
        projB.setText(df.format(playerB.getProjection()));
        if (playerA.getProjection() > playerB.getProjection()) {
            projA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            projB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            projA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            projB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }

        // PAA
        TextView paaA = (TextView)findViewById(R.id.comparator_paa_a);
        TextView paaB = (TextView)findViewById(R.id.comparator_paa_b);
        paaA.setText(df.format(playerA.getPaa()));
        paaB.setText(df.format(playerB.getPaa()));
        if (playerA.getPaa() > playerB.getPaa()) {
            paaA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            paaB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            paaA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            paaB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }

        // XVal
        TextView xvalA = (TextView)findViewById(R.id.comparator_xval_a);
        TextView xvalB = (TextView)findViewById(R.id.comparator_xval_b);
        xvalA.setText(df.format(playerA.getxVal()));
        xvalB.setText(df.format(playerB.getxVal()));
        if (playerA.getxVal() > playerB.getxVal()) {
            xvalA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            xvalB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            xvalA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            xvalB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }
    }

    private void goToPlayerInfo(Player player) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, player.getUniqueId());
        startActivity(intent);
    }

    private void displayECR(Map<String, String> ecrResults, Player playerA, Player playerB) {
        LinearLayout ecrRow = (LinearLayout)findViewById(R.id.expert_output_row);
        ecrRow.setVisibility(View.VISIBLE);

        TextView ecrA = (TextView)findViewById(R.id.comparator_ecr_a);
        TextView ecrB = (TextView)findViewById(R.id.comparator_ecr_b);
        String percentStrA = ecrResults.get(playerA.getUniqueId());
        String percentStrB = ecrResults.get(playerB.getUniqueId());
        ecrA.setText(percentStrA);
        ecrB.setText(percentStrB);
        String trimmedA = percentStrA.substring(0, percentStrA.length() - 1);
        String trimmedB = percentStrB.substring(0, percentStrB.length() - 1);
        int ecrValA = Integer.parseInt(trimmedA);
        int ecrValB = Integer.parseInt(trimmedB);
        if (ecrValA > ecrValB) {
            ecrA.setBackgroundColor(Color.parseColor(BETTER_COLOR));
            ecrB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        } else {
            ecrA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
            ecrB.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        }
    }

    private void clearECR() {
        LinearLayout ecrRow = (LinearLayout)findViewById(R.id.expert_output_row);
        ecrRow.setVisibility(View.GONE);

        TextView ecrA = (TextView)findViewById(R.id.comparator_ecr_a);
        TextView ecrB = (TextView)findViewById(R.id.comparator_ecr_b);
        ecrA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        ecrB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
    }

    private class ParseFP extends AsyncTask<Object, Void, Map<String, String>> {
        private ProgressDialog pdia;
        private PlayerComparator act;
        private Player playerA;
        private Player playerB;

        ParseFP(PlayerComparator activity, Player playerA, Player playerB) {
            pdia = new ProgressDialog(activity);
            pdia.setCancelable(false);
            act = activity;
            this.playerA = playerA;
            this.playerB = playerB;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia.setMessage("Please wait, trying to get the ECR starting numbers...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            super.onPostExecute(result);
            pdia.dismiss();
            if (result != null) {
                act.displayECR(result, playerA, playerB);
            } else {
                act.clearECR();
            }
        }

        @Override
        protected Map<String, String> doInBackground(Object... data) {
            String baseURL = "http://www.fantasypros.com/nfl/draft/";
            baseURL += ParsePlayerNews.playerNameUrl(playerA.getName(), playerA.getTeamName()) + "-"
                    + ParsePlayerNews.playerNameUrl(playerB.getName(), playerB.getTeamName()) + ".php";
            if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
                baseURL += "?scoring=PPR";
            }
            Map<String, String> results = new HashMap<>();
            try {
                Document doc = Jsoup.connect(baseURL).get();
                Elements elems = doc.select("div.pick-percent");
                String percentOne = elems.get(0).text();
                String percentTwo = elems.get(1).text();
                Element tableElem = elems.get(0).parent().parent().parent().parent();
                String nameOne = tableElem.child(1).child(1).child(1).child(0).text();
                String nameTwo = tableElem.child(1).child(1).child(2).child(0).text();
                if (playerA.getName().equals(nameOne)) {
                    results.put(playerA.getUniqueId(), percentOne);
                    results.put(playerB.getUniqueId(), percentTwo);
                } else if (playerA.getName().equals(nameTwo)) {
                    results.put(playerA.getUniqueId(), percentTwo);
                    results.put(playerB.getUniqueId(), percentOne);
                } else {
                    Log.d(TAG, "Failed to get unique id: " + nameOne + ", " + nameTwo + ": "
                            + playerA.getUniqueId() + ", " + playerB.getUniqueId());
                    return null;
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to get ecr numbers", e);
                return null;
            }
            return results;
        }
    }
}
