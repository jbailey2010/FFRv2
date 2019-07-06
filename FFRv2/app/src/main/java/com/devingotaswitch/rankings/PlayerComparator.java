package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.DailyProjection;
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter;
import com.devingotaswitch.rankings.sources.ParseMath;
import com.devingotaswitch.rankings.sources.ParsePlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.DisplayUtils;
import com.devingotaswitch.utils.DraftUtils;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.utils.GraphUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerComparator extends AppCompatActivity {

    private Rankings rankings;
    private Player playerA;
    private Player playerB;

    private AutoCompleteTextView inputA;
    private AutoCompleteTextView inputB;
    private LineChart projGraph;
    private ScrollView comparatorScroller;
    private RecyclerView inputList;
    private List<Map<String, String>> data = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private RankingsDBWrapper rankingsDB;

    private boolean hideDraftedSuggestion = false;
    private boolean hideRanklessSuggestion = false;

    private static final String TAG = "PlayerComparator";
    private static final String BETTER_COLOR = "#F3F3F3";
    private static final String WORSE_COLOR = "#FAFAFA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_comparator);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        // Set toolbar for this screen
        Toolbar toolbar = findViewById(R.id.player_comparator_toolbar);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("Compare Players");
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
        GeneralUtils.hideKeyboard(this);

        try {
            init();

            if (getIntent().hasExtra(Constants.PLAYER_ID)) {
                String playerId = getIntent().getStringExtra(Constants.PLAYER_ID);
                playerA = rankings.getPlayer(playerId);
                inputA.setText(playerA.getName());
                inputB.requestFocus();
            }
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            GeneralUtils.hideKeyboard(this);
            onBackPressed();
        }
    }

    public void setUserSettings(boolean hideDraftedSuggestion, boolean hideRanklessSuggestion) {
        this.hideDraftedSuggestion = hideDraftedSuggestion;
        this.hideRanklessSuggestion = hideRanklessSuggestion;

        setSuggestionAdapter();
        displayOptions();
    }

    private void setSuggestionAdapter() {
        inputA =  findViewById(R.id.comparator_input_a);
        inputB =  findViewById(R.id.comparator_input_b);

        final FilterWithSpaceAdapter mAdapter = GeneralUtils.getPlayerSearchAdapter(rankings, this,
                hideDraftedSuggestion, hideRanklessSuggestion);
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
    }

    private void init() {
        rankingsDB = new RankingsDBWrapper();
        projGraph = findViewById(R.id.comparator_graph);
        comparatorScroller = findViewById(R.id.comparator_output_scroller);
        inputList = findViewById(R.id.comparator_input_list);

        setSuggestionAdapter();

        if (playerA != null && playerB != null) {
            displayResults(playerA, playerB);
        } else {
            displayOptions();
        }
        final Activity localCopy = this;

        inputA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playerA = getPlayerFromView(view);
                inputA.setText(playerA.getName());
                if (playerB != null && playerA.getUniqueId().equals(playerB.getUniqueId())) {
                    FlashbarFactory.generateTextOnlyFlashbar(localCopy, "No can do", "Select two different players",
                            Flashbar.Gravity.TOP)
                            .show();
                } else if (playerB != null) {
                    displayResults(playerA, playerB);
                } else {
                    toggleListItemStar(playerA, true);
                }
            }
        });
        inputB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playerB = getPlayerFromView(view);
                inputB.setText(playerB.getName());
                if (playerA != null && playerA.getUniqueId().equals(playerB.getUniqueId())) {
                    FlashbarFactory.generateTextOnlyFlashbar(localCopy, "No can do", "Select two different players",
                            Flashbar.Gravity.TOP)
                            .show();
                } else if (playerA != null) {
                    displayResults(playerA, playerB);
                } else {
                    toggleListItemStar(playerB, true);
                }
            }
        });

        inputA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (playerA != null && !playerA.getName().equals(editable.toString())) {
                    toggleListItemStar(playerA, false);
                    playerA = null;
                }
            }
        });

        inputB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (playerB != null && !playerB.getName().equals(editable.toString())) {
                    toggleListItemStar(playerB, false);
                    playerB = null;
                }
            }
        });

        AppSyncHelper.getUserSettings(this);
    }

    private void toggleListItemStar(Player player, boolean doStar) {
        for (Map<String, String> datum : data) {
            String basic = datum.get(Constants.PLAYER_BASIC);
            String teamPos = datum.get(Constants.PLAYER_INFO);
            if (basic.contains(player.getName()) && teamPos.contains(player.getTeamName()) && teamPos.contains(player.getPosition())) {
                if (doStar) {
                    datum.put(Constants.PLAYER_STATUS, Integer.toString(R.drawable.star));
                } else {
                    datum.put(Constants.PLAYER_STATUS, null);
                }
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void displayOptions() {
        inputList.setVisibility(View.VISIBLE);
        comparatorScroller.setVisibility(View.GONE);
        inputList.setAdapter(null);
        data.clear();
        adapter = new RecyclerViewAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO,
                        Constants.PLAYER_ADDITIONAL_INFO_2},
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status, R.id.player_more_info, R.id.player_additional_info_2 });
        Map<String, Integer> posRankMap = DisplayUtils.getPositionRankMap();
        for (int i = 0; i < Math.min(Constants.COMPARATOR_LIST_MAX, rankings.getOrderedIds().size()); i++) {
            Player player = rankings.getPlayer(rankings.getOrderedIds().get(i));
            if ((rankings.getDraft().isDrafted(player)  && hideDraftedSuggestion) || (hideRanklessSuggestion &&
                            Constants.DEFAULT_DISPLAY_RANK_NOT_SET.equals(player.getDisplayValue(rankings)))) {
                continue;
            }
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition())) {
                if (rankings.getLeagueSettings().isRookie() && player.getRookieRank() == Constants.DEFAULT_RANK) {
                    // the constant is 'not set', so skip these. No sense showing a 10 year vet in rookie ranks.
                    continue;
                }
                Map<String, String> datum = DisplayUtils.getDatumForPlayer(rankings, player, false, posRankMap);
                data.add(datum);
            }
        }

        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Player clickedPlayer = rankings.getPlayer(DisplayUtils.getPlayerKeyFromListViewItem(((RelativeLayout)view)));
                if (playerA != null && playerA.getUniqueId().equals(clickedPlayer.getUniqueId())) {
                    playerA = null;
                    inputA.setText(null);
                    Map<String, String> datum = data.get(position);
                    datum.put(Constants.PLAYER_STATUS, null);
                    adapter.notifyDataSetChanged();
                } else {
                    if (playerA == null && playerB == null) {
                        playerA = clickedPlayer;
                        inputA.setText(playerA.getName());
                        inputA.clearFocus();
                        Map<String, String> datum = data.get(position);
                        datum.put(Constants.PLAYER_STATUS, Integer.toString(R.drawable.star));
                        adapter.notifyDataSetChanged();
                    } else if (playerA == null && playerB != null) {
                        playerA = clickedPlayer;
                        inputA.setText(playerA.getName());
                        inputA.clearFocus();
                        displayResults(playerA, playerB);
                    } else {
                        playerB = clickedPlayer;
                        inputB.setText(playerB.getName());
                        inputB.clearFocus();
                        displayResults(playerA, playerB);
                    }
                }
            }
        });
        inputList.setLayoutManager(new LinearLayoutManager(this));
        inputList.addItemDecoration(DisplayUtils.getVerticalDividerDecoration(this));
        inputList.setAdapter(adapter);
    }

    private Player getPlayerFromView(View view) {
        return rankings.getPlayer(GeneralUtils.getPlayerIdFromSearchView(view));
    }

    private void clearInputs() {
        playerA = null;
        playerB = null;
        inputA.setText("");
        inputB.setText("");
        projGraph.setVisibility(View.GONE);
        findViewById(R.id.note_output_row).setVisibility(View.GONE);
        GeneralUtils.hideKeyboard(this);
        displayOptions();
    }

    private void displayResults(Player a, Player b) {
        GeneralUtils.hideKeyboard(this);
        comparatorScroller.setVisibility(View.VISIBLE);
        inputList.setVisibility(View.GONE);
        ParseFP parseFP = new ParseFP(this, playerA, playerB);
        parseFP.execute();

        LinearLayout outputBase = findViewById(R.id.comparator_output_base);
        outputBase.setVisibility(View.VISIBLE);
        inputA.clearFocus();
        inputB.clearFocus();
        final Player playerA = rankingsDB.getPlayer(this, a.getName(), a.getTeamName(), a.getPosition());
        final Player playerB = rankingsDB.getPlayer(this, b.getName(), b.getTeamName(), b.getPosition());

        // Name
        final TextView nameA = findViewById(R.id.comparator_name_a);
        final TextView nameB = findViewById(R.id.comparator_name_b);
        String titleA = playerA.getName();
        String titleB = playerB.getName();
        if (rankings.getDraft().isDrafted(playerA)) {
            titleA += Constants.COMPARATOR_DRAFTED_SUFFIX;
        }
        if (rankings.getDraft().isDrafted(playerB)) {
            titleB += Constants.COMPARATOR_DRAFTED_SUFFIX;
        }
        nameA.setText(titleA);
        nameB.setText(titleB);
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
        nameA.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!rankings.getDraft().isDrafted(playerA)) {
                    if (rankings.getLeagueSettings().isAuction()) {
                        getAuctionCost(playerA, nameA);
                    } else {
                        draftPlayer(playerA, nameA, 0);
                    }
                    return true;
                }
                return false;
            }
        });
        nameB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!rankings.getDraft().isDrafted(playerB)) {
                    if (rankings.getLeagueSettings().isAuction()) {
                        getAuctionCost(playerB, nameB);
                    } else {
                        draftPlayer(playerB, nameB, 0);
                    }
                    return true;
                }
                return false;
            }
        });

        // Age
        TextView ageA = findViewById(R.id.comparator_age_a);
        TextView ageB = findViewById(R.id.comparator_age_b);
        ageA.setText(playerA.getAge() > 0 ? String.valueOf(playerA.getAge()) : "?");
        ageB.setText(playerB.getAge() > 0 ? String.valueOf(playerB.getAge()) : "?");

        TextView byeA = findViewById(R.id.comparator_bye_a);
        TextView byeB = findViewById(R.id.comparator_bye_b);
        Team teamA = rankings.getTeam(playerA);
        Team teamB = rankings.getTeam(playerB);
        byeA.setText(teamA != null ? teamA.getBye() : "?");
        byeB.setText(teamB != null ? teamB.getBye() : "?");

        // Note
        if (!StringUtils.isBlank(playerA.getNote()) || !StringUtils.isBlank(playerB.getNote())) {
            findViewById(R.id.note_output_row).setVisibility(View.VISIBLE);
            TextView noteA = findViewById(R.id.comparator_note_a);
            TextView noteB = findViewById(R.id.comparator_note_b);
            noteA.setText(playerA.getNote() == null ? "" : playerA.getNote());
            noteB.setText(playerB.getNote() == null ? "" : playerB.getNote());
        }

        // Expert's selection percentages (default to hidden)
        LinearLayout ecrRow = findViewById(R.id.expert_output_row);
        ecrRow.setVisibility(View.GONE);

        // ECR val
        TextView ecrA = findViewById(R.id.comparator_ecr_val_a);
        TextView ecrB =  findViewById(R.id.comparator_ecr_val_b);
        ecrA.setText(String.valueOf(playerA.getEcr()));
        ecrB.setText(String.valueOf(playerB.getEcr()));
        if (playerA.getEcr() < playerB.getEcr()) {
            setColors(ecrA, ecrB);
        } else if (playerA.getEcr() > playerB.getEcr()){
            setColors(ecrB, ecrA);
        } else {
            clearColors(ecrA, ecrB);
        }

        //ADP
        TextView adpA = findViewById(R.id.comparator_adp_a);
        TextView adpB = findViewById(R.id.comparator_adp_b);
        adpA.setText(String.valueOf(playerA.getAdp()));
        adpB.setText(String.valueOf(playerB.getAdp()));
        if (playerA.getAdp() < playerB.getAdp()) {
            setColors(adpA, adpB);
        } else if (playerA.getAdp() > playerB.getAdp()){
            setColors(adpB, adpA);
        } else {
            clearColors(adpA, adpB);
        }

        // Dynasty/keeper ranks
        TextView dynA = findViewById(R.id.comparator_dynasty_a);
        TextView dynB = findViewById(R.id.comparator_dynasty_b);
        dynA.setText(String.valueOf(playerA.getDynastyRank()));
        dynB.setText(String.valueOf(playerB.getDynastyRank()));
        if (playerA.getDynastyRank() < playerB.getDynastyRank()) {
            setColors(dynA, dynB);
        } else if (playerB.getDynastyRank() < playerA.getDynastyRank()) {
            setColors(dynB, dynA);
        } else {
            clearColors(dynA, dynB);
        }

        // Rookie ranks
        LinearLayout rookieRow = findViewById(R.id.rookie_output_row);
        if (rankings.getLeagueSettings().isRookie() && (playerA.getRookieRank() < 300.0 || playerB.getRookieRank() < 300.0)) {
            rookieRow.setVisibility(View.VISIBLE);
            TextView rookA = findViewById(R.id.comparator_rookie_a);
            TextView rookB = findViewById(R.id.comparator_rookie_b);

            if (playerA.getRookieRank() == 300.0 && playerB.getRookieRank() < 300.0) {
                rookA.setText("N/A");
                rookB.setText(String.valueOf(playerB.getRookieRank()));
                clearColors(rookB, rookA);

            } else if (playerA.getRookieRank() < 300.0 && playerB.getRookieRank() == 300.0) {
                rookA.setText(String.valueOf(playerA.getRookieRank()));
                rookB.setText("N/A");
                clearColors(rookA, rookB);
            } else {
                rookA.setText(String.valueOf(playerA.getRookieRank()));
                rookB.setText(String.valueOf(playerB.getRookieRank()));
                if (playerA.getRookieRank() < playerB.getRookieRank()) {
                    setColors(rookA, rookB);
                } else if (playerB.getRookieRank() < playerA.getRookieRank()) {
                    setColors(rookB, rookA);
                } else {
                    clearColors(rookA, rookB);
                }
            }
        } else {
            rookieRow.setVisibility(View.GONE);
        }

        // Best ball rank
        TextView bbA = findViewById(R.id.comparator_best_ball_a);
        TextView bbB = findViewById(R.id.comparator_best_ball_b);
        bbA.setText(String.valueOf(playerA.getBestBallRank()));
        bbB.setText(String.valueOf(playerB.getBestBallRank()));
        if (playerA.getBestBallRank() < playerB.getBestBallRank()) {
            setColors(bbA, bbB);
        } else if (playerB.getBestBallRank() < playerA.getBestBallRank()) {
            setColors(bbB, bbA);
        } else {
            clearColors(bbA, bbB);
        }

        // Auction value
        TextView aucA = findViewById(R.id.comparator_auc_a);
        TextView aucB = findViewById(R.id.comparator_auc_b);
        aucA.setText(Constants.DECIMAL_FORMAT.format(playerA.getAuctionValueCustom(rankings)));
        aucB.setText(Constants.DECIMAL_FORMAT.format(playerB.getAuctionValueCustom(rankings)));
        if (playerA.getAuctionValue() > playerB.getAuctionValue()) {
            setColors(aucA, aucB);
        } else if (playerA.getAuctionValue() < playerB.getAuctionValue()) {
            setColors(aucB, aucA);
        } else {
            clearColors(aucA, aucB);
        }

        // Leverage
        TextView levA = findViewById(R.id.comparator_lev_a);
        TextView levB = findViewById(R.id.comparator_lev_b);
        double levAVal = ParseMath.getLeverage(playerA, rankings);
        double levBVal = ParseMath.getLeverage(playerB, rankings);
        levA.setText(String.valueOf(levAVal));
        levB.setText(String.valueOf(levBVal));
        if (levAVal > levBVal) {
            setColors(levA, levB);
        } else if (levAVal < levBVal) {
            setColors(levB, levA);
        } else {
            clearColors(levA, levB);
        }

        // SOS
        TextView sosA = findViewById(R.id.comparator_sos_a);
        TextView sosB = findViewById(R.id.comparator_sos_b);
        int sosForA = rankings.getTeam(playerA).getSosForPosition(playerA.getPosition());
        int sosForB = rankings.getTeam(playerB).getSosForPosition(playerB.getPosition());
        sosA.setText(String.valueOf(sosForA));
        sosB.setText(String.valueOf(sosForB));
        if (sosForA < sosForB) {
            setColors(sosA, sosB);
        } else if (sosForA > sosForB){
            setColors(sosB, sosA);
        } else {
            clearColors(sosA, sosB);
        }

        // Projection
        TextView projA = findViewById(R.id.comparator_proj_a);
        TextView projB = findViewById(R.id.comparator_proj_b);
        projA.setText(Constants.DECIMAL_FORMAT.format(playerA.getProjection()));
        projB.setText(Constants.DECIMAL_FORMAT.format(playerB.getProjection()));
        if (playerA.getProjection() > playerB.getProjection()) {
            setColors(projA, projB);
        } else if (playerA.getProjection() < playerB.getProjection()){
            setColors(projB, projA);
        } else {
            clearColors(projA, projB);
        }

        // PAA
        TextView paaA = findViewById(R.id.comparator_paa_a);
        TextView paaB = findViewById(R.id.comparator_paa_b);
        paaA.setText(Constants.DECIMAL_FORMAT.format(playerA.getPaa()) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerA.getScaledPAA(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX);
        paaB.setText(Constants.DECIMAL_FORMAT.format(playerB.getPaa()) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerB.getScaledPAA(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX);
        if (playerA.getScaledPAA(rankings) > playerB.getScaledPAA(rankings)) {
            setColors(paaA, paaB);
        } else if (playerA.getScaledPAA(rankings) < playerB.getScaledPAA(rankings)){
            setColors(paaB, paaA);
        } else {
            clearColors(paaA, paaB);
        }

        // XVal
        TextView xvalA = findViewById(R.id.comparator_xval_a);
        TextView xvalB = findViewById(R.id.comparator_xval_b);
        xvalA.setText(Constants.DECIMAL_FORMAT.format(playerA.getxVal()) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerA.getScaledXVal(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX);
        xvalB.setText(Constants.DECIMAL_FORMAT.format(playerB.getxVal()) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerB.getScaledXVal(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX);
        if (playerA.getScaledXVal(rankings) > playerB.getScaledXVal(rankings)) {
            setColors(xvalA, xvalB);
        } else if (playerA.getScaledXVal(rankings) < playerB.getScaledXVal(rankings)){
            setColors(xvalB, xvalA);
        } else {
            clearColors(xvalA, xvalB);
        }

        // VoLS
        TextView volsA = findViewById(R.id.comparator_vols_a);
        TextView volsB = findViewById(R.id.comparator_vols_b);
        volsA.setText(Constants.DECIMAL_FORMAT.format(playerA.getVOLS()) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerA.getScaledVOLS(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX);
        volsB.setText(Constants.DECIMAL_FORMAT.format(playerB.getVOLS()) + Constants.COMPARATOR_SCALED_PREFIX +
                Constants.DECIMAL_FORMAT.format(playerB.getScaledVOLS(rankings)) + Constants.COMPARATOR_SCALED_SUFFIX);
        if (playerA.getScaledVOLS(rankings) > playerB.getScaledVOLS(rankings)) {
            setColors(volsA, volsB);
        } else if (playerA.getScaledVOLS(rankings) < playerB.getScaledVOLS(rankings)) {
            setColors(volsB, volsA);
        } else {
            clearColors(volsA, volsB);
        }


        // Risk
        TextView riskA = findViewById(R.id.comparator_risk_a);
        TextView riskB = findViewById(R.id.comparator_risk_b);
        riskA.setText(String.valueOf(playerA.getRisk()));
        riskB.setText(String.valueOf(playerB.getRisk()));
        if (playerA.getRisk() < playerB.getRisk()) {
            setColors(riskA, riskB);
        } else if (playerA.getRisk() > playerB.getRisk()) {
            setColors(riskB, riskA);
        } else {
            clearColors(riskA, riskB);
        }

        // Graph
        projGraph = findViewById(R.id.comparator_graph);
        List<DailyProjection> historyA = rankings.getPlayerProjectionHistory().get(playerA.getUniqueId());
        List<DailyProjection> historyB = rankings.getPlayerProjectionHistory().get(playerB.getUniqueId());
        if (historyA != null && historyA.size() >= 2 && historyB != null && historyB.size() >= 2) {
            projGraph.setVisibility(View.VISIBLE);
            graphPlayers(projGraph, playerA, playerB, historyA, historyB);
        } else {
            projGraph.setVisibility(View.GONE);
        }

        GeneralUtils.hideKeyboard(this);
    }

    private void clearColors(TextView playerA, TextView playerB) {
        playerA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        playerB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
    }

    private void setColors(TextView winner, TextView loser) {
        winner.setBackgroundColor(Color.parseColor(BETTER_COLOR));
        loser.setBackgroundColor(Color.parseColor(WORSE_COLOR));
    }

    private void graphPlayers(LineChart lineGraph, Player playerA, Player playerB, List<DailyProjection> historyA, List<DailyProjection> historyB) {
        LineDataSet dataSetA = getLineDataSet(playerA, historyA, "blue");
        LineDataSet dataSetB = getLineDataSet(playerB, historyB, "green");
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetA);
        lineData.addDataSet(dataSetB);

        lineGraph.setData(lineData);
        lineGraph.setDrawBorders(true);
        Description description = new Description();
        description.setText("");
        lineGraph.setDescription(description);
        lineGraph.invalidate();
        lineGraph.setTouchEnabled(true);
        lineGraph.setPinchZoom(true);
        lineGraph.setDragEnabled(true);
        lineGraph.animateX(1500);
        lineGraph.animateY(1500);

        XAxis x = lineGraph.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawAxisLine(false);
        x.setDrawLabels(false);
        x.setDrawGridLines(false);

        YAxis yR = lineGraph.getAxisRight();
        yR.setDrawAxisLine(false);
        yR.setDrawGridLines(false);
        yR.setDrawLabels(false);

        YAxis yL = lineGraph.getAxisLeft();
        yL.setDrawLabels(true);
        yL.setDrawGridLines(true);
        yL.setDrawAxisLine(false);
    }

    private LineDataSet getLineDataSet(Player player, List<DailyProjection> projections, String color) {
        List<Entry> projectionDays = new ArrayList<>();
        for (int i = 0; i < projections.size(); i++) {
            DailyProjection projection = projections.get(i);
            projectionDays.add(new Entry((float) i, (float) projection.getProjection(rankings.getLeagueSettings().getScoringSettings())));
        }
        LineDataSet projectionHistoryDataset = GraphUtils.getLineDataSet(projectionDays,
                player.getName() + " Projections", color);
        return projectionHistoryDataset;
    }

    private void goToPlayerInfo(Player player) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, player.getUniqueId());
        startActivity(intent);
    }

    private void getAuctionCost(final Player player, final TextView title) {
        final Activity localCopy = this;
        DraftUtils.AuctionCostInterface callback = new DraftUtils.AuctionCostInterface() {
            @Override
            public void onValidInput(Integer cost) {
                draftPlayer(player, title, cost);
            }

            @Override
            public void onInvalidInput() {

                FlashbarFactory.generateTextOnlyFlashbar(localCopy, "No can do", "Must provide a number for cost",
                        Flashbar.Gravity.TOP)
                        .show();
            }

            @Override
            public void onCancel() {

            }
        };
        AlertDialog alertDialog = DraftUtils.getAuctionCostDialog(this, player, callback);
        alertDialog.show();
    }

    private void draftPlayer(final Player player, final TextView title, int cost) {
        Flashbar.OnActionTapListener listener = new Flashbar.OnActionTapListener() {
            @Override
            public void onActionTapped(Flashbar flashbar) {
                undraftPlayer(player, title);
            }
        };
        rankings.getDraft().draftByMe(rankings, player, this, cost, title, listener);
        title.setText(player.getName() + Constants.COMPARATOR_DRAFTED_SUFFIX);
    }

    private void undraftPlayer(Player player, TextView title) {
        rankings.getDraft().undraft(rankings, player, this, title);
        title.setText(player.getName());
    }

    private void displayECR(Map<String, String> ecrResults, Player playerA, Player playerB) {
        LinearLayout ecrRow = findViewById(R.id.expert_output_row);
        ecrRow.setVisibility(View.VISIBLE);

        TextView ecrA = findViewById(R.id.comparator_ecr_a);
        TextView ecrB = findViewById(R.id.comparator_ecr_b);
        String percentStrA = ecrResults.get(playerA.getUniqueId());
        String percentStrB = ecrResults.get(playerB.getUniqueId());
        ecrA.setText(percentStrA);
        ecrB.setText(percentStrB);
        String trimmedA = percentStrA.substring(0, percentStrA.length() - 1);
        String trimmedB = percentStrB.substring(0, percentStrB.length() - 1);
        int ecrValA = Integer.parseInt(trimmedA);
        int ecrValB = Integer.parseInt(trimmedB);
        if (ecrValA > ecrValB) {
            setColors(ecrA, ecrB);
        } else if (ecrValA < ecrValB) {
            setColors(ecrB, ecrA);
        } else {
            clearColors(ecrA, ecrB);
        }
    }

    private void clearECR() {
        LinearLayout ecrRow = findViewById(R.id.expert_output_row);
        ecrRow.setVisibility(View.GONE);

        TextView ecrA = findViewById(R.id.comparator_ecr_a);
        TextView ecrB = findViewById(R.id.comparator_ecr_b);
        ecrA.setBackgroundColor(Color.parseColor(WORSE_COLOR));
        ecrB.setBackgroundColor(Color.parseColor(WORSE_COLOR));
    }

    private class ParseFP extends AsyncTask<Object, Void, Map<String, String>> {
        private final ProgressDialog pdia;
        private final PlayerComparator act;
        private final Player playerA;
        private final Player playerB;

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
            if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 1.0) {
                baseURL += "?scoring=PPR";
            } else if (rankings.getLeagueSettings().getScoringSettings().getReceptions() > 0) {
                baseURL += "?scoring=HALF";
            }
            Map<String, String> results = new HashMap<>();
            try {
                Document doc = Jsoup.connect(baseURL).get();
                Elements elems = doc.select("div.pick-percent");
                String percentOne = elems.get(0).text();
                String percentTwo = elems.get(1).text();
                Element tableElem = elems.get(0).parent().parent().parent().parent();
                String nameOne = tableElem.child(1).child(1).child(1).child(0).child(0).child(0).text();
                String nameTwo = tableElem.child(1).child(1).child(2).child(0).child(0).child(0).text();
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
