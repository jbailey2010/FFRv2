package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Draft;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DraftInfo extends AppCompatActivity {

    private static final String TAG = "DraftInfo";

    private Rankings rankings;

    private LinearLayout baseLayout;
    private MenuItem viewTeam;
    private MenuItem undraftPlayers;

    private final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        Toolbar toolbar = findViewById(R.id.toolbar_draft_info);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("Current Draft");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            init();
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_draft_info_menu, menu);
        viewTeam = menu.findItem(R.id.draft_info_team);
        undraftPlayers = menu.findItem(R.id.draft_info_players);
        viewTeam.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();
        switch(menuItem) {
            case R.id.draft_info_clear:
                clearDraft();
                return true;
            case R.id.draft_info_team:
                displayTeam();
                viewTeam.setVisible(false);
                undraftPlayers.setVisible(true);
                return true;
            case R.id.draft_info_players:
                displayPlayers();
                viewTeam.setVisible(true);
                undraftPlayers.setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearDraft() {
        rankings.getDraft().resetDraft(this, rankings.getLeagueSettings().getName());
        (findViewById(R.id.team_graph)).setVisibility(View.GONE);
        Snackbar.make(baseLayout, "Draft cleared", Snackbar.LENGTH_SHORT).show();
        displayTeam();
    }

    private void init() {
        baseLayout = findViewById(R.id.draft_info_base);
        displayTeam();
    }

    private void displayTeam() {
        View view = clearAndAddView(R.layout.content_draft_info_team);
        TextView teamView = view.findViewById(R.id.base_textview_team);
        StringBuilder teamOutput = new StringBuilder(getTeamStr());
        if (rankings.getLeagueSettings().isAuction()) {
            teamOutput.append(getAuctionValue());
        }
        teamView.setText(teamOutput.toString());

        graphTeam();

        TextView paaLeft = view.findViewById(R.id.base_textview_paa_left);
        paaLeft.setText(getPAALeft());

        TextView playersDrafted = view.findViewById(R.id.base_textview_players_drafted);
        playersDrafted.setText("Total players drafted: " + rankings.getDraft().getDraftedPlayers().size());

        graphPAALeft();
    }

    private String getPAALeft() {
        StringBuilder paaLeft = new StringBuilder();
        RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
        if (roster.isPositionValid(Constants.QB)) {
            paaLeft.append(rankings.getDraft().getPAALeft(Constants.QB, rankings))
                    .append(Constants.LINE_BREAK);
        }
        if (roster.isPositionValid(Constants.RB)) {
            paaLeft.append(rankings.getDraft().getPAALeft(Constants.RB, rankings))
                    .append(Constants.LINE_BREAK);
        }
        if (roster.isPositionValid(Constants.WR)) {
            paaLeft.append(rankings.getDraft().getPAALeft(Constants.WR, rankings))
                    .append(Constants.LINE_BREAK);
        }
        if (roster.isPositionValid(Constants.TE)) {
            paaLeft.append(rankings.getDraft().getPAALeft(Constants.TE, rankings))
                    .append(Constants.LINE_BREAK);
        }
        if (roster.isPositionValid(Constants.DST)) {
            paaLeft.append(rankings.getDraft().getPAALeft(Constants.DST, rankings))
                    .append(Constants.LINE_BREAK);
        }
        if (roster.isPositionValid(Constants.K)) {
            paaLeft.append(rankings.getDraft().getPAALeft(Constants.K, rankings))
                    .append(Constants.LINE_BREAK);
        }
        return paaLeft.toString();
    }

    private String getAuctionValue() {
        return "Value: " +
                df.format(rankings.getDraft().getDraftValue());
    }

    private String getTeamStr() {
        StringBuilder teamOutput = new StringBuilder();
        RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
        Draft draft = rankings.getDraft();
        if (roster.isPositionValid(Constants.QB)) {
            teamOutput.append(Constants.QB)
                    .append("s: ")
                    .append(getPosString(draft.getMyQbs(), draft.getQBPAA(), draft.getQBXval(), draft.getQBVoLS()));
        }
        if (roster.isPositionValid(Constants.RB)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.RB)
                    .append("s: ")
                    .append(getPosString(draft.getMyRbs(), draft.getRBPAA(), draft.getRBXval(), draft.getRBVoLS()));
        }
        if (roster.isPositionValid(Constants.WR)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.WR)
                    .append("s: ")
                    .append(getPosString(draft.getMyWrs(), draft.getWRPAA(), draft.getWRXval(), draft.getWRVoLS()));
        }
        if (roster.isPositionValid(Constants.TE)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.TE)
                    .append("s: ")
                    .append(getPosString(draft.getMyTes(), draft.getTEPAA(), draft.getTEXval(), draft.getTEVoLS()));
        }
        if (roster.isPositionValid(Constants.DST)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.DST)
                    .append("s: ")
                    .append(getPosString(draft.getMyDsts(), draft.getDSTPAA(), draft.getDSTXval(), draft.getDSTVoLS()));
        }
        if (roster.isPositionValid(Constants.K)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.K)
                    .append("s: ")
                    .append(getPosString(draft.getMyKs(), draft.getKPAA(), draft.getKXval(), draft.getKVoLS()));
        }
        return teamOutput.append(Constants.LINE_BREAK)
                .append("Total PAA: ")
                .append(df.format(draft.getTotalPAA()))
                .append(Constants.LINE_BREAK)
                .append("Total XVal: ")
                .append(df.format(draft.getTotalXVal()))
                .append(Constants.LINE_BREAK)
                .append("Total VoLS: ")
                .append(df.format(draft.getTotalVoLS()))
                .append(Constants.LINE_BREAK)
                .toString();
    }

    private String getPosString(List<Player> players, double posPAA, double posXVal, double posVoLS) {
        if (players.size() == 0) {
            return "None";
        }
        StringBuilder posStr = new StringBuilder();
        for (Player player : players) {
            posStr.append(player.getName())
                    .append(", ");
        }
        String playerStr = posStr.toString();
        return playerStr.substring(0, playerStr.length() - 2) +
                " (" +
                df.format(posPAA) +
                ", " +
                df.format(posXVal) +
                ", " +
                df.format(posVoLS) +
                ")";
    }

    private void displayPlayers() {
        View view = clearAndAddView(R.layout.content_draft_info_undraft);
        ListView listview = view.findViewById(R.id.base_list);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS },
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status });
        listview.setAdapter(adapter);
        for (String playerKey : rankings.getDraft().getDraftedPlayers()) {
            Player player = rankings.getPlayer(playerKey);
            String playerBasicContent;
            if (rankings.getLeagueSettings().isAuction()) {
                playerBasicContent = String.valueOf(df.format(player.getAuctionValueCustom(rankings))) +
                        Constants.RANKINGS_LIST_DELIMITER +
                        player.getName();
            } else if (rankings.getLeagueSettings().isDynasty()) {
                playerBasicContent = String.valueOf(player.getDynastyRank()) +
                        Constants.RANKINGS_LIST_DELIMITER +
                        player.getName();
            } else if (rankings.getLeagueSettings().isRookie()) {
                playerBasicContent = String.valueOf(player.getRookieRank()) +
                        Constants.RANKINGS_LIST_DELIMITER +
                        player.getName();
            } else {
                playerBasicContent = String.valueOf(player.getEcr()) +
                        Constants.RANKINGS_LIST_DELIMITER +
                        player.getName();
            }
            Map<String, String> datum = new HashMap<>(3);
            datum.put(Constants.PLAYER_BASIC, playerBasicContent);
            datum.put(Constants.PLAYER_INFO, generateOutputSubtext(player, df));
            data.add(datum);
        }
        adapter.notifyDataSetChanged();

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                undraftPlayer(view);
                return true;
            }
        });
    }

    private void undraftPlayer(View view) {
        String key = getPlayerKeyFromListViewItem(view);
        Player player = rankings.getPlayer(key);
        rankings.getDraft().undraft(rankings, player, this, baseLayout);
        displayPlayers();
    }

    private String getPlayerKeyFromListViewItem(View view) {
        TextView playerMain = view.findViewById(R.id.player_basic);
        TextView playerInfo = view.findViewById(R.id.player_info);
        String name = playerMain.getText().toString().split(Constants.RANKINGS_LIST_DELIMITER)[1];
        String teamPosBye = playerInfo.getText().toString().split(Constants.LINE_BREAK)[0];
        String teamPos = teamPosBye.split(" \\(")[0];
        String team = teamPos.split(Constants.POS_TEAM_DELIMITER)[1];
        String pos = teamPos.split(Constants.POS_TEAM_DELIMITER)[0];

        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos;
    }

    private String generateOutputSubtext(Player player, DecimalFormat df) {
        StringBuilder sub = new StringBuilder(player.getPosition())
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.getTeamName());
        Team team = rankings.getTeam(player);
        if (team != null) {
            sub = sub.append(" (Bye: ")
                    .append(team.getBye())
                    .append(")");
        }
        return sub.append(Constants.LINE_BREAK)
                .append("Projection: ")
                .append(df.format(player.getProjection()))
                .toString();
    }

    private View clearAndAddView(int viewId) {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(viewId, null);
        baseLayout.addView(child);
        return child;
    }

    private void graphPAALeft() {
        BarChart barChart = findViewById(R.id.paa_left_graph);
        BarData barData = new BarData();

        conditionallyGraphPosition(barData, Constants.QB);
        conditionallyGraphPosition(barData, Constants.RB);
        conditionallyGraphPosition(barData, Constants.WR);
        conditionallyGraphPosition(barData, Constants.TE);
        conditionallyGraphPosition(barData, Constants.DST);
        conditionallyGraphPosition(barData, Constants.K);

        barChart.setData(barData);
        YAxis left = barChart.getAxisLeft();
        left.setDrawZeroLine(true);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setEnabled(false);
        barChart.setDescription(null);
        barChart.invalidate();
        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setDragEnabled(true);
        barChart.getLegend().setCustom(new ArrayList<LegendEntry>());
        barChart.animateX(1500);
        barChart.animateY(1500);
    }

    private void conditionallyGraphPosition(BarData barData, String position) {
        if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(position)) {
            List<BarEntry> entries = new ArrayList<>();
            List<Player> players = rankings.getDraft().getSortedAvailablePlayersForPosition(position, rankings);

            double threeBack = rankings.getDraft().getPAANAvailablePlayersBack(players, 3);
            double fiveBack = rankings.getDraft().getPAANAvailablePlayersBack(players, 5);
            double tenBack = rankings.getDraft().getPAANAvailablePlayersBack(players, 10);
            BarEntry stackedEntry = new BarEntry(barData.getDataSetCount(),
                    new float[] {(float)threeBack, (float)fiveBack, (float)tenBack});
            stackedEntry.setData(position);

            entries.add(stackedEntry);
            BarDataSet barDataSet = new BarDataSet(entries, position);
            barDataSet.setColors(Color.BLUE, Color.GREEN, Color.CYAN);
            barDataSet.setDrawValues(false);
            barData.addDataSet(barDataSet);
        }
    }

    private void graphTeam() {
        Draft draft = rankings.getDraft();
        if (draft.getMyPlayers().size() == 0) {
            return;
        }
        BarChart teamPAA = findViewById(R.id.team_graph);
        BarData barData = new BarData();

        conditionallyAddData(draft.getMyQbs(), barData.getDataSetCount(), draft.getQBPAA(), barData, "QBs", "green");
        conditionallyAddData(draft.getMyRbs(), barData.getDataSetCount(), draft.getRBPAA(), barData, "RBs", "red");
        conditionallyAddData(draft.getMyWrs(), barData.getDataSetCount(), draft.getWRPAA(), barData, "WRs", "blue");
        conditionallyAddData(draft.getMyTes(), barData.getDataSetCount(), draft.getTEPAA(), barData, "TEs", "yellow");
        conditionallyAddData(draft.getMyKs(), barData.getDataSetCount(), draft.getKPAA(), barData, "DSTs", "black");
        conditionallyAddData(draft.getMyDsts(), barData.getDataSetCount(), draft.getDSTPAA(), barData, "Ks", "grey");

        if (barData.getDataSetCount() > 1) {
            List<BarEntry> entries = new ArrayList<>();
            BarEntry entry = new BarEntry(barData.getDataSetCount(), (int) draft.getTotalPAA());
            entries.add(entry);
            barData.addDataSet(getBarDataSet(entries, "All", "purple"));
        }

        teamPAA.setData(barData);
        Description description = new Description();
        description.setText("\nPAA");
        description.setTextSize(12f);
        teamPAA.setDescription(description);
        teamPAA.invalidate();
        teamPAA.setTouchEnabled(true);
        teamPAA.setPinchZoom(true);
        teamPAA.setDragEnabled(true);
        teamPAA.animateX(1500);
        teamPAA.animateY(1500);

        YAxis left = teamPAA.getAxisLeft();
        left.setDrawAxisLine(false);
        left.setDrawGridLines(false);
        left.setDrawLabels(false);
        left.setDrawZeroLine(true);
        teamPAA.getAxisRight().setEnabled(false);
        teamPAA.getXAxis().setEnabled(false);
        Legend legend = teamPAA.getLegend();
        legend.setTextSize(10f);
        legend.setDrawInside(false);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        teamPAA.setVisibility(View.VISIBLE);
    }

    private void conditionallyAddData(List<Player> players, int xIndex, double posPaa,
                                      BarData barData, String label, String color) {
        List<BarEntry> entries = new ArrayList<>();
        if (players.size() > 0) {
            BarEntry entry = new BarEntry(xIndex, (int) posPaa);
            entries.add(entry);
            barData.addDataSet(getBarDataSet(entries, label, color));
            xIndex++;
        }
    }

    private BarDataSet getBarDataSet(List<BarEntry> entries, String label, String color) {
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(Color.parseColor(color));
        dataSet.setValueTextSize(10f);
        //dataSet.setDrawValues(false);
        return dataSet;
    }
}
