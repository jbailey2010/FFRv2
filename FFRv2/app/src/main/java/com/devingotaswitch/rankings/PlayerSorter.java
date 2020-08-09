package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.extras.MultiSelectionSpinner;
import com.devingotaswitch.rankings.extras.RecyclerViewAdapter;
import com.devingotaswitch.rankings.extras.SwipeDismissTouchListener;
import com.devingotaswitch.utils.Constants;
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

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerSorter extends AppCompatActivity {

    private static final String TAG = "PlayerSorter";

    private Rankings rankings;
    private RankingsDBWrapper rankingsDB;

    private MenuItem graphItem;
    private final List<Player> players = new ArrayList<>();
    private String factor = null;
    private String expandedFactor = null;
    private String expandedFactorType = null;
    private int sortMax;

    private int posIndex = 0;
    private int sortIndex = 0;
    private int selectedIndex = 0;
    private double maxVal = 0.0;
    private int lastFactorIndex = 0;
    private Set<String> factorStrings = new HashSet<>(Collections.singletonList(Constants.SORT_DEFAULT_STRING));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_sorter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();
        rankingsDB = new RankingsDBWrapper();

        // Set toolbar for this screen
        Toolbar toolbar =  findViewById(R.id.player_sorter_toolbar);
        toolbar.setTitle("");
        TextView main_title =  findViewById(R.id.main_toolbar_title);
        main_title.setText("Sort Players");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_sort_menu, menu);
        graphItem = menu.findItem(R.id.graph_sort);
        graphItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();
        switch(menuItem) {
            case R.id.graph_sort:
                graphSort();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        try {
            init();
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            GeneralUtils.hideKeyboard(this);
            onBackPressed();
        }
    }

    private MultiSelectionSpinner setSpinnerAdapter() {
        final MultiSelectionSpinner spinner=findViewById(R.id.sort_players_additional_factors);
        List<String> list = new ArrayList<>();
        if (!rankings.getUserSettings().isHideDraftedSort()) {
            list.add(Constants.SORT_HIDE_DRAFTED);
        }
        list.add(Constants.SORT_ONLY_HEALTHY);
        list.add(Constants.SORT_EASY_SOS);
        list.add(Constants.SORT_ONLY_WATCHED);
        list.add(Constants.SORT_ONLY_ROOKIES);
        list.add(Constants.SORT_UNDER_30);
        if (!rankings.getLeagueSettings().isRookie() && !rankings.getLeagueSettings().isDynasty() && !rankings.getLeagueSettings().isBestBall()) {
            list.add(Constants.SORT_IGNORE_EARLY);
            list.add(Constants.SORT_IGNORE_LATE);
        }
        spinner.setItems(list, Constants.SORT_DEFAULT_STRING);
        spinner.setSelection(new ArrayList<>(factorStrings));
        return spinner;
    }

    private void init() {
        AppSyncHelper.getUserSettings(this);

        final NiceSpinner positions = findViewById(R.id.sort_players_position);
        final List<String> posList = new ArrayList<>();
        RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
        posList.add(Constants.ALL_POSITIONS);
        if (roster.getQbCount() > 0) {
            posList.add(Constants.QB);
        }
        if (roster.getRbCount() > 0) {
            posList.add(Constants.RB);
        }
        if (roster.getWrCount() > 0) {
            posList.add(Constants.WR);
        }
        if (roster.getTeCount() > 0) {
            posList.add(Constants.TE);
        }
        if (roster.getDstCount() > 0) {
            posList.add(Constants.DST);
        }
        if (roster.getkCount() > 0) {
            posList.add(Constants.K);
        }
        if (roster.getRbCount() > 0 && roster.getWrCount() > 0 && roster.getNumStartingPositions() > 2) {
            posList.add(Constants.RBWR);
        }
        if (roster.getRbCount() > 0 && roster.getTeCount() > 0 && roster.getNumStartingPositions() > 2) {
            posList.add(Constants.RBTE);
        }
        if (roster.getRbCount() > 0 && roster.getWrCount() > 0 && roster.getTeCount() > 0
                && roster.getNumStartingPositions() > 3) {
            posList.add(Constants.RBWRTE);
        }
        if (roster.getWrCount() > 0 && roster.getTeCount() > 0 && roster.getNumStartingPositions() > 2) {
            posList.add(Constants.WRTE);
        }
        if (roster.getQbCount() > 0 && roster.getRbCount() > 0 && roster.getWrCount() > 0 && roster.getTeCount() > 0
                && roster.getNumStartingPositions() > 4) {
            posList.add(Constants.QBRBWRTE);
        }
        positions.attachDataSource(posList);
        positions.setBackgroundColor(Color.parseColor("#FAFAFA"));

        final NiceSpinner factors = findViewById(R.id.sort_players_factor);
        final List<String> factorList = new ArrayList<>();
        factorList.add(Constants.SORT_ALL);
        factorList.add(Constants.SORT_ECR);
        factorList.add(Constants.SORT_ADP);
        factorList.add(Constants.SORT_UNDERDRAFTED);
        factorList.add(Constants.SORT_OVERDRAFTED);
        factorList.add(Constants.SORT_PROJECTION_EXPANDED);
        factorList.add(Constants.SORT_VBD_EXPANDED);
        factorList.add(Constants.SORT_AUCTION);
        factorList.add(Constants.SORT_DYNASTY);
        factorList.add(Constants.SORT_ROOKIE);
        factorList.add(Constants.SORT_BEST_BALL);
        factorList.add(Constants.SORT_RISK);
        factorList.add(Constants.SORT_SOS);
        factors.attachDataSource(factorList);
        factors.setBackgroundColor(Color.parseColor("#FAFAFA"));

        final Activity act = this;
        factors.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, final int position, long id) {
                String selection = ((TextView)view).getText().toString();
                if (Constants.SORT_VBD_EXPANDED.equals(selection) ||
                        Constants.SORT_PROJECTION_EXPANDED.equals(selection)) {
                    final ListPopupWindow popup = new ListPopupWindow(act);
                    popup.setAnchorView(factors);
                    List<Map<String, String>> data = null;
                    if (Constants.SORT_VBD_EXPANDED.equals(selection)) {
                        data = getVBDOptions();
                    } else if (Constants.SORT_PROJECTION_EXPANDED.equals(selection)) {
                        data = getProjectionOptions();
                    }
                    SimpleAdapter adapter = new SimpleAdapter(act, data,
                            R.layout.nested_spinner_item, new String[] { Constants.NESTED_SPINNER_DISPLAY},
                            new int[] { R.id.text_view_spinner });
                    popup.setAdapter(adapter);
                    popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (Constants.SORT_BACK.equals(((TextView)view).getText().toString())) {
                                popup.dismiss();
                                // Seems to be an annoying bug with the spinner library, can't re-select the same item.
                                // So this is done to work around it.
                                factors.attachDataSource(factorList);
                                factors.showDropDown();
                                if (StringUtils.isBlank(expandedFactor)) {
                                    factors.setSelectedIndex(lastFactorIndex);
                                } else {
                                    factors.setText(expandedFactorType);
                                }
                            } else {
                                expandedFactor = ((TextView) view).getText().toString();
                                factors.setSelectedIndex(position);
                                factors.setText(factors.getText() + " " + expandedFactor);
                                popup.dismiss();
                            }
                        }
                    });
                    popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            if (StringUtils.isBlank(expandedFactor)) {
                                factors.setSelectedIndex(lastFactorIndex);
                            }
                        }
                    });
                    popup.show();
                }
            }
        });

        final MultiSelectionSpinner spinner = setSpinnerAdapter();
        positions.setSelectedIndex(posIndex);
        factors.setSelectedIndex(sortIndex);

        final CheckBox reverse = findViewById(R.id.sort_players_reverse);

        final EditText numberShown =  findViewById(R.id.sort_players_number_shown);

        Button submit = findViewById(R.id.sort_players_submit);
        final PlayerSorter activity = this;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPosition = posList.get(positions.getSelectedIndex());
                List<String> filteredIds = new ArrayList<>(rankings.getOrderedIds());
                if (!Constants.ALL_POSITIONS.equals(currentPosition)) {
                    filteredIds = rankings.getPlayersByPosition(filteredIds, currentPosition);
                }
                String selection = factors.getText().toString();
                if ((!selection.startsWith(Constants.SORT_VBD_EXPANDED) &&
                        !selection.startsWith(Constants.SORT_PROJECTION_EXPANDED)) || StringUtils.isBlank(expandedFactor)) {
                    factor = factorList.get(factors.getSelectedIndex());
                    lastFactorIndex = factors.getSelectedIndex();
                    expandedFactor = null;
                    // Seems to be an annoying bug with the spinner library, can't re-select the same item.
                    // So this is done to work around it.
                    factors.attachDataSource(factorList);
                    factors.setSelectedIndex(lastFactorIndex);
                } else {
                    factor = expandedFactor;
                    expandedFactorType = factors.getText().toString();
                    // Seems to be an annoying bug with the spinner library, can't re-select the same item.
                    // So this is done to work around it.
                    factors.attachDataSource(factorList);
                    factors.setText(expandedFactorType);
                }
                posIndex = positions.getSelectedIndex();
                sortIndex = factors.getSelectedIndex();
                factorStrings = spinner.getSelectedStrings();

                String numberShownStr = numberShown.getText().toString();
                sortMax = 1000;
                if (!StringUtils.isBlank(numberShownStr) && GeneralUtils.isInteger(numberShownStr)) {
                    sortMax = Integer.parseInt(numberShownStr);
                }
                GeneralUtils.hideKeyboard(activity);
                getComparatorForFactor(filteredIds, spinner.getSelectedStrings(), reverse.isChecked());
                graphItem.setVisible(true);
            }
        });
    }

    private List<Map<String,String>> getVBDOptions() {
        List<Map<String, String>> factorList = new ArrayList<>();

        factorList.add(generateSpinnerMap(Constants.SORT_BACK));
        factorList.add(generateSpinnerMap(Constants.SORT_PAA));
        factorList.add(generateSpinnerMap(Constants.SORT_PAA_SCALED));
        factorList.add(generateSpinnerMap(Constants.SORT_PAAPD));
        factorList.add(generateSpinnerMap(Constants.SORT_XVAL));
        factorList.add(generateSpinnerMap(Constants.SORT_XVAL_SCALED));
        factorList.add(generateSpinnerMap(Constants.SORT_XVALPD));
        factorList.add(generateSpinnerMap(Constants.SORT_VOLS));
        factorList.add(generateSpinnerMap(Constants.SORT_VOLS_SCALED));
        factorList.add(generateSpinnerMap(Constants.SORT_VOLSPD));
        factorList.add(generateSpinnerMap(Constants.SORT_VBD_SUGGESTED));
        return factorList;
    }

    private List<Map<String,String>> getProjectionOptions() {
        List<Map<String, String>> factorList = new ArrayList<>();

        factorList.add(generateSpinnerMap(Constants.SORT_BACK));
        factorList.add(generateSpinnerMap(Constants.SORT_PROJECTION));
        factorList.add(generateSpinnerMap(Constants.SORT_PASSING_TDS));
        factorList.add(generateSpinnerMap(Constants.SORT_PASSING_YDS));
        factorList.add(generateSpinnerMap(Constants.SORT_RUSHING_TDS));
        factorList.add(generateSpinnerMap(Constants.SORT_RUSHING_YDS));
        factorList.add(generateSpinnerMap(Constants.SORT_RECEIVING_TDS));
        factorList.add(generateSpinnerMap(Constants.SORT_RECEIVING_YDS));
        factorList.add(generateSpinnerMap(Constants.SORT_RECEPTIONS));
        return factorList;
    }

    private Map<String, String> generateSpinnerMap(String value) {
        Map<String, String> datum = new HashMap<>();
        datum.put(Constants.NESTED_SPINNER_DISPLAY, value);
        return datum;
    }

    private void getComparatorForFactor(List<String> playerIds, Set<String> booleanFactors, boolean reversePlayers) {
        Comparator<Player> comparator = null;
        switch (factor) {
            case Constants.SORT_ECR:
                comparator = getECRComparator();
                break;
            case Constants.SORT_ADP:
                comparator = getADPComparator();
                break;
            case Constants.SORT_UNDERDRAFTED:
                comparator = getUnderdraftedComparator();
                break;
            case Constants.SORT_OVERDRAFTED:
                comparator = getOverdraftedComparator();
                break;
            case Constants.SORT_AUCTION:
                comparator = getAuctionComparator();
                break;
            case Constants.SORT_DYNASTY:
                comparator = getDynastyComparator();
                break;
            case Constants.SORT_ROOKIE:
                comparator = getRookieComparator();
                break;
            case Constants.SORT_BEST_BALL:
                comparator = getBestBallComparator();
                break;
            case Constants.SORT_PROJECTION:
                comparator = getProjectionComparator();
                break;
            case Constants.SORT_PASSING_TDS:
                comparator = getPassingTDsComparator();
                break;
            case Constants.SORT_PASSING_YDS:
                comparator = getPassingYardsComparator();
                break;
            case Constants.SORT_RUSHING_TDS:
                comparator = getRushingTdsComparator();
                break;
            case Constants.SORT_RUSHING_YDS:
                comparator = getRushingYardsComparator();
                break;
            case Constants.SORT_RECEIVING_TDS:
                comparator = getReceivingTdsComparator();
                break;
            case Constants.SORT_RECEIVING_YDS:
                comparator = getReceivingYardsComparator();
                break;
            case Constants.SORT_RECEPTIONS:
                comparator = getReceptionsComparator();
                break;
            case Constants.SORT_PAA:
                comparator = getPAAComparator();
                break;
            case Constants.SORT_PAA_SCALED:
                comparator = getPAAScaledComparator();
                break;
            case Constants.SORT_PAAPD:
                comparator = getPAAPDComparator();
                break;
            case Constants.SORT_XVAL:
                comparator = getXValComparator();
                break;
            case Constants.SORT_XVAL_SCALED:
                comparator = getXValScaledComparator();
                break;
            case Constants.SORT_XVALPD:
                comparator = getXvalPDComparator();
                break;
            case Constants.SORT_VOLS:
                comparator = getVoLSComparator();
                break;
            case Constants.SORT_VOLS_SCALED:
                comparator = getVoLSScaledComparator();
                break;
            case Constants.SORT_VOLSPD:
                comparator = getVoLSPDComparator();
                break;
            case Constants.SORT_VBD_SUGGESTED:
                comparator = getVBDSuggestedComparator();
                break;
            case Constants.SORT_RISK:
                comparator = getRiskComparator();
                break;
            case Constants.SORT_SOS:
                comparator = getSOSComparator();
                break;
        }
        filterAndConditionallySortPlayers(playerIds, booleanFactors, reversePlayers, comparator);
    }

    private void filterAndConditionallySortPlayers(List<String> playerIds, Set<String> booleanFactors, boolean reversePlayers,
                                                   Comparator<Player> comparator) {
        players.clear();
        for (String id : playerIds) {
            Player player = rankings.getPlayer(id);
            if (((Constants.SORT_ALL.equals(factor) && rankings.getLeagueSettings().isRookie()) || Constants.SORT_ROOKIE.equals(factor))
                    && player.getRookieRank().equals(Constants.DEFAULT_RANK)) {
                // Default sort for rookies means only rookies. If it's 'not set',  skip.
                // Also skip if we're looking at rookie rank for someone without one (meaning, not a rookie).
                continue;
            }
            if (Constants.SORT_SOS.equals(factor) && ((rankings.getTeam(player) == null))) {
                // If the player's team isn't a valid team, skip over for sos
                continue;
            }
            if ((Constants.SORT_UNDERDRAFTED.equals(factor) || Constants.SORT_OVERDRAFTED.equals(factor)) &&
                    (player.getEcr().equals(Constants.DEFAULT_RANK) || player.getAdp().equals(Constants.DEFAULT_RANK))) {
                // Don't compare adp to ecr if either is not saved
                continue;
            }


            if ((booleanFactors.contains(Constants.SORT_HIDE_DRAFTED) || rankings.getUserSettings().isHideDraftedSort() ||
                    Constants.SORT_VBD_SUGGESTED.equals(factor)) && rankings.getDraft().isDrafted(player)) {
                continue;
            }
            if (rankings.getUserSettings().isHideRanklessSort() &&
                    Constants.DEFAULT_DISPLAY_RANK_NOT_SET.equals(player.getDisplayValue(rankings))) {
                continue;
            }
            if (booleanFactors.contains(Constants.SORT_EASY_SOS)) {
                Team team = rankings.getTeam(player);
                if (team == null || (team.getSosForPosition(player.getPosition()) > Constants.SORT_EASY_SOS_THRESHOLD)) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_ONLY_HEALTHY)) {
                if (!StringUtils.isBlank(player.getInjuryStatus())) {
                    continue;
                }
            }
            if  (booleanFactors.contains(Constants.SORT_ONLY_WATCHED)) {
                if (!rankings.isPlayerWatched(player.getUniqueId())) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_ONLY_ROOKIES)) {
                if (player.getRookieRank().equals(Constants.DEFAULT_RANK)) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_UNDER_30)) {
                if (player.getAge() == 0 || player.getAge() >= Constants.SORT_YOUNG_THRESHOLD) {
                    continue;
                }
            }
            int teamCount = rankings.getLeagueSettings().getTeamCount();
            if (booleanFactors.contains(Constants.SORT_IGNORE_LATE)) {
                if (player.getEcr() > teamCount * Constants.SORT_IGNORE_LATE_THRESHOLD_ROUNDS) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_IGNORE_EARLY)) {
                if (player.getEcr() < teamCount * Constants.SORT_IGNORE_EARLY_THRESHOLD_ROUNDS) {
                    continue;
                }
            }

            players.add(player);
        }
        if (comparator != null) {
            // If it's null, it was default, which means the already ordered list
            Collections.sort(players, comparator);
        }
        if (reversePlayers) {
            Collections.reverse(players);
        }
        displayResults(players);
    }

    private void displayResults(List<Player> players) {
        final RecyclerView listview =  findViewById(R.id.sort_players_output);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO},
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status, R.id.player_more_info });
        listview.setAdapter(adapter);

        maxVal = 0.0;
        if (Constants.SORT_VBD_SUGGESTED.equals(factor)) {
            for (String key : rankings.getPlayers().keySet()) {
                Player player = rankings.getPlayer(key);
                if (!rankings.getDraft().isDrafted(player)) {
                    double currVal = getVBDSuggestedValue(rankings.getPlayer(key));
                    if (currVal > maxVal) {
                        maxVal = currVal;
                    }
                }
            }
        }

        int displayedCount = 0;
        for (Player player : players) {
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition())) {
                if (displayedCount >= sortMax) {
                    break;
                }
                Map<String, String> datum = new HashMap<>(3);
                datum.put(Constants.PLAYER_BASIC, getMainTextForFactor(player));
                datum.put(Constants.PLAYER_INFO, getSubTextForFactor(player));
                if (rankings.isPlayerWatched(player.getUniqueId())) {
                    datum.put(Constants.PLAYER_STATUS, Integer.toString(R.drawable.star));
                }
                if (rankings.getDraft().isDrafted(player)) {
                    datum.put(Constants.PLAYER_ADDITIONAL_INFO, "Drafted");
                }
                data.add(datum);
                displayedCount++;
            }
        }
        adapter.notifyDataSetChanged();
        final Activity act = this;
        final boolean hideDrafted = factorStrings.contains(Constants.SORT_HIDE_DRAFTED);
        adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                String playerKey = getPlayerKeyFromListViewItem(view);
                final ImageView playerStatus = view.findViewById(R.id.player_status);
                final Player player = rankings.getPlayer(playerKey);
                if (rankings.isPlayerWatched(player.getUniqueId())) {
                    Flashbar.OnActionTapListener listener = new Flashbar.OnActionTapListener() {
                        @Override
                        public void onActionTapped(Flashbar flashbar) {
                            flashbar.dismiss();
                            playerStatus.setImageResource(R.drawable.star);
                            rankings.togglePlayerWatched(act, player.getUniqueId());
                        }
                    };
                    FlashbarFactory.generateFlashbarWithUndo(act, "Success!", player.getName() + " removed from watch list",
                            Flashbar.Gravity.BOTTOM, listener)
                            .show();
                    rankings.togglePlayerWatched(act, player.getUniqueId());
                    playerStatus.setImageResource(0);
                } else {
                    Flashbar.OnActionTapListener listener = new Flashbar.OnActionTapListener() {
                        @Override
                        public void onActionTapped(Flashbar flashbar) {
                            flashbar.dismiss();
                            playerStatus.setImageResource(0);
                            rankings.togglePlayerWatched(act, player.getUniqueId());
                        }
                    };
                    FlashbarFactory.generateFlashbarWithUndo(act, "Success!", player.getName() + " added to watch list",
                            Flashbar.Gravity.BOTTOM, listener)
                            .show();
                    rankings.togglePlayerWatched(act, player.getUniqueId());
                    playerStatus.setImageResource(R.drawable.star);
                }
                return true;
            }
        });
        RecyclerViewAdapter.OnItemClickListener onItemClickListener = new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String playerKey = getPlayerKeyFromListViewItem(view);
                selectedIndex = position;
                displayPlayerInfo(playerKey);
            }
        };
        final Activity localCopy = this;
        final SwipeDismissTouchListener swipeListener = new SwipeDismissTouchListener(listview, hideDrafted,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(View view) {
                        if (((TextView)view.findViewById(R.id.player_more_info)).getText().toString().contains(Constants.DISPLAY_DRAFTED)) {
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void onDismiss(RecyclerView listView, int[] reverseSortedPositions, boolean rightDismiss) {
                        for (final int position : reverseSortedPositions) {
                            final Map<String, String> datum = data.get(position);
                            String playerKey = getPlayerKeyFromPieces(datum.get(Constants.PLAYER_BASIC), datum.get(Constants.PLAYER_INFO));
                            final Player player = rankings.getPlayer(playerKey);
                            Flashbar.OnActionTapListener listener = DraftUtils.getUndraftListener(localCopy, rankings, player, listView,
                                    adapter, data, datum, position, hideDrafted);
                            if (!rightDismiss) {
                                rankings.getDraft().draftBySomeone(rankings, player, localCopy, listView, listener);
                            } else {
                                if (rankings.getLeagueSettings().isAuction()) {
                                    getAuctionCost(listView, player, position, data, datum, adapter, listener);
                                } else {
                                    draftByMe(listView, player, 0, listener);
                                }
                            }
                            if (!hideDrafted) {
                                datum.put(Constants.PLAYER_ADDITIONAL_INFO, Constants.DISPLAY_DRAFTED);
                            } else {
                                data.remove(position);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, onItemClickListener);
        listview.setOnTouchListener(swipeListener);
        listview.addOnScrollListener(new OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
                swipeListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (data.size() > 0) {
                    // A flag is used to green light this set, otherwise onScroll is set to 0 on initial display
                    LinearLayoutManager layoutManager = (LinearLayoutManager) view.getLayoutManager();
                    selectedIndex = layoutManager.findFirstCompletelyVisibleItemPosition();
                }
            }
        });
        listview.getLayoutManager().scrollToPosition(selectedIndex);

        TextView titleView = findViewById(R.id.main_toolbar_title);
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.smoothScrollToPosition(0);
            }
        });
        titleView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listview.scrollToPosition(0);
                return true;
            }
        });

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getAuctionCost(final RecyclerView listView, final Player player, final int position, final List<Map<String, String>> data,
                                final Map<String, String> datum, final RecyclerViewAdapter adapter, final Flashbar.OnActionTapListener listener) {
        final Activity act = this;
        DraftUtils.AuctionCostInterface callback = new DraftUtils.AuctionCostInterface() {
            @Override
            public void onValidInput(Integer cost) {
                GeneralUtils.hideKeyboard(act);
                draftByMe(listView, player, cost, listener);
            }

            @Override
            public void onInvalidInput() {
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "Must provide a number for cost", Flashbar.Gravity.TOP)
                        .show();
                data.add(position, datum);
                adapter.notifyDataSetChanged();
                GeneralUtils.hideKeyboard(act);
            }

            @Override
            public void onCancel() {
                data.add(position, datum);
                adapter.notifyDataSetChanged();
            }
        };
        AlertDialog alertDialog = DraftUtils.getAuctionCostDialog(this, player, callback);
        alertDialog.show();
    }

    private void draftByMe(final View view, Player player, int cost, Flashbar.OnActionTapListener listener) {
        rankings.getDraft().draftByMe(rankings, player, this, cost, view, listener);
    }

    private void displayPlayerInfo(String playerKey) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, playerKey);
        startActivity(intent);
    }

    private Comparator<Player> getECRComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getEcr() > b.getEcr()) {
                    return 1;
                }
                if (a.getEcr() < b.getEcr()) {
                    return -1;
                }
                return 0;
            }
        };
    }
    private Comparator<Player> getPassingTDsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getPassingProjection().getTds() > b.getPlayerProjection().getPassingProjection().getTds()) {
                    return -1;
                }
                if (a.getPlayerProjection().getPassingProjection().getTds() < b.getPlayerProjection().getPassingProjection().getTds()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPassingYardsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getPassingProjection().getYards() > b.getPlayerProjection().getPassingProjection().getYards()) {
                    return -1;
                }
                if (a.getPlayerProjection().getPassingProjection().getYards() < b.getPlayerProjection().getPassingProjection().getYards()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getRushingYardsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getRushingProjection().getYards() > b.getPlayerProjection().getRushingProjection().getYards()) {
                    return -1;
                }
                if (a.getPlayerProjection().getRushingProjection().getYards() < b.getPlayerProjection().getRushingProjection().getYards()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getRushingTdsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getRushingProjection().getTds() > b.getPlayerProjection().getRushingProjection().getTds()) {
                    return -1;
                }
                if (a.getPlayerProjection().getRushingProjection().getTds() < b.getPlayerProjection().getRushingProjection().getTds()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getReceivingYardsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getReceivingProjection().getYards() > b.getPlayerProjection().getReceivingProjection().getYards()) {
                    return -1;
                }
                if (a.getPlayerProjection().getReceivingProjection().getYards() < b.getPlayerProjection().getReceivingProjection().getYards()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getReceivingTdsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getReceivingProjection().getTds() > b.getPlayerProjection().getReceivingProjection().getTds()) {
                    return -1;
                }
                if (a.getPlayerProjection().getReceivingProjection().getTds() < b.getPlayerProjection().getReceivingProjection().getTds()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getReceptionsComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPlayerProjection().getReceivingProjection().getReceptions() > b.getPlayerProjection().getReceivingProjection().getReceptions()) {
                    return -1;
                }
                if (a.getPlayerProjection().getReceivingProjection().getReceptions() < b.getPlayerProjection().getReceivingProjection().getReceptions()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getDynastyComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getDynastyRank() > b.getDynastyRank()) {
                    return 1;
                }
                if (a.getDynastyRank() < b.getDynastyRank()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getRookieComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getRookieRank() > b.getRookieRank()) {
                    return 1;
                }
                if (a.getRookieRank() < b.getRookieRank()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getBestBallComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getBestBallRank() > b.getBestBallRank()) {
                    return 1;
                }
                if (a.getBestBallRank() < b.getBestBallRank()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getADPComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getAdp() > b.getAdp()) {
                    return 1;
                }
                if (a.getAdp() < b.getAdp()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getUnderdraftedComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double diffA = a.getEcr() - a.getAdp();
                double diffB = b.getEcr() - b.getAdp();
                return Double.compare(diffA, diffB);
            }
        };
    }

    private Comparator<Player> getOverdraftedComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double diffA = a.getEcr() - a.getAdp();
                double diffB = b.getEcr() - b.getAdp();
                return Double.compare(diffB, diffA);
            }
        };
    }

    private Comparator<Player> getAuctionComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getAuctionValue() > b.getAuctionValue()) {
                    return -1;
                }
                if (a.getAuctionValue() < b.getAuctionValue()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getProjectionComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getProjection() > b.getProjection()) {
                    return -1;
                }
                if (a.getProjection() < b.getProjection()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPAAComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPaa() > b.getPaa()) {
                    return -1;
                }
                if (a.getPaa() < b.getPaa()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPAAScaledComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getScaledPAA(rankings) > b.getScaledPAA(rankings)) {
                    return -1;
                }
                if (a.getScaledPAA(rankings) < b.getScaledPAA(rankings)) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPAAPDComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double paapdA = getPAAPD(a);
                double paapdB = getPAAPD(b);
                return Double.compare(paapdB, paapdA);
            }
        };
    }

    private double getPAAPD(Player a) {
        double paapdA = a.getPaa() / a.getAuctionValueCustom(rankings);
        if (a.getAuctionValue() == 0.0) {
            paapdA = 0.0;
        }
        return paapdA;
    }

    private double getXvalPD(Player a) {
        double xvalpdA = a.getxVal() / a.getAuctionValueCustom(rankings);
        if (a.getAuctionValue() == 0) {
            xvalpdA = 0.0;
        }
        return xvalpdA;
    }

    private double getVoLSPD(Player a) {
        double volspdA = a.getVOLS() / a.getAuctionValueCustom(rankings);
        if (a.getAuctionValue() == 0) {
            volspdA = 0.0;
        }
        return volspdA;
    }

    private double getVBDSuggestedValue(Player a) {
        return a.getScaledPAA(rankings) + a.getScaledPAA(rankings) + a.getScaledVOLS(rankings);
    }

    private Comparator<Player> getXValComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getxVal() > b.getxVal()) {
                    return -1;
                }
                if (a.getxVal() < b.getxVal()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getVoLSComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getVOLS() > b.getVOLS()) {
                    return -1;
                }
                if (a.getVOLS() < b.getVOLS()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getXvalPDComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double xvalpdA = getXvalPD(a);
                double xvalpdB = getXvalPD(b);
                return Double.compare(xvalpdB, xvalpdA);
            }
        };
    }

    private Comparator<Player> getVoLSPDComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double volspdA = getVoLSPD(a);
                double volspdB = getVoLSPD(b);
                return Double.compare(volspdB, volspdA);
            }
        };
    }

    private Comparator<Player> getVBDSuggestedComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double vbdValA = getVBDSuggestedValue(a);
                double vbdValB = getVBDSuggestedValue(b);
                return Double.compare(vbdValB, vbdValA);
            }
        };
    }

    private Comparator<Player> getXValScaledComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getScaledXVal(rankings) > b.getScaledXVal(rankings)) {
                    return -1;
                }
                if (a.getScaledXVal(rankings) < b.getScaledXVal(rankings)) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getVoLSScaledComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getScaledVOLS(rankings) > b.getScaledVOLS(rankings)) {
                    return -1;
                }
                if (a.getScaledVOLS(rankings) < b.getScaledVOLS(rankings)) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getRiskComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getRisk() > b.getRisk()) {
                    return 1;
                }
                if (a.getRisk() < b.getRisk()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getSOSComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double sosA = getSOS(a);
                double sosB = getSOS(b);
                // Compare returns lower first, we want to reverse that.
                return Double.compare(sosA, sosB) * -1;
            }
        };
    }

    private double getSOS(Player player) {
        Team team = rankings.getTeam(player);
        if (team == null) {
            return 1.0;
        }
        return team.getSosForPosition(player.getPosition());
    }

    private String getMainTextForFactor(Player player) {
        String prefix = getMainTextPrefixForPlayer(player);

        return prefix +
                Constants.RANKINGS_LIST_DELIMITER +
                player.getName();
    }

    private String getMainTextPrefixForPlayer(Player player) {
        switch (factor) {
            case Constants.SORT_ALL:
                return player.getDisplayValue(rankings);
            case Constants.SORT_ECR:
                return String.valueOf(player.getEcr().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getEcr());
            case Constants.SORT_ADP:
                return String.valueOf(player.getAdp().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getAdp());
            case Constants.SORT_UNDERDRAFTED:
            case Constants.SORT_OVERDRAFTED:
                return Constants.DECIMAL_FORMAT.format(player.getEcr() - player.getAdp());
            case Constants.SORT_AUCTION:
                return Constants.DECIMAL_FORMAT.format(player.getAuctionValueCustom(rankings));
            case Constants.SORT_DYNASTY:
                return String.valueOf(player.getDynastyRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getDynastyRank());
            case Constants.SORT_ROOKIE:
                return String.valueOf(player.getRookieRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getRookieRank());
            case Constants.SORT_BEST_BALL:
                return String.valueOf(player.getBestBallRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getBestBallRank());
            case Constants.SORT_PROJECTION:
                return Constants.DECIMAL_FORMAT.format(player.getProjection());
            case Constants.SORT_PASSING_TDS:
                return String.valueOf(player.getPlayerProjection().getPassingProjection().getTds());
            case Constants.SORT_PASSING_YDS:
                return String.valueOf(player.getPlayerProjection().getPassingProjection().getYards());
            case Constants.SORT_RUSHING_TDS:
                return String.valueOf(player.getPlayerProjection().getRushingProjection().getTds());
            case Constants.SORT_RUSHING_YDS:
                return String.valueOf(player.getPlayerProjection().getRushingProjection().getYards());
            case Constants.SORT_RECEIVING_TDS:
                return String.valueOf(player.getPlayerProjection().getReceivingProjection().getTds());
            case Constants.SORT_RECEIVING_YDS:
                return String.valueOf(player.getPlayerProjection().getReceivingProjection().getYards());
            case Constants.SORT_RECEPTIONS:
                return String.valueOf(player.getPlayerProjection().getReceivingProjection().getReceptions());
            case Constants.SORT_PAA:
                return Constants.DECIMAL_FORMAT.format(player.getPaa());
            case Constants.SORT_PAA_SCALED:
                return Constants.DECIMAL_FORMAT.format(player.getScaledPAA(rankings));
            case Constants.SORT_PAAPD:
                return Constants.DECIMAL_FORMAT.format(getPAAPD(player));
            case Constants.SORT_XVAL:
                return Constants.DECIMAL_FORMAT.format(player.getxVal());
            case Constants.SORT_XVAL_SCALED:
                return Constants.DECIMAL_FORMAT.format(player.getScaledXVal(rankings));
            case Constants.SORT_XVALPD:
                return Constants.DECIMAL_FORMAT.format(getXvalPD(player));
            case Constants.SORT_VOLS:
                return Constants.DECIMAL_FORMAT.format(player.getVOLS());
            case Constants.SORT_VOLS_SCALED:
                return Constants.DECIMAL_FORMAT.format(player.getScaledVOLS(rankings));
            case Constants.SORT_VOLSPD:
                return Constants.DECIMAL_FORMAT.format(getVoLSPD(player));
            case Constants.SORT_VBD_SUGGESTED:
                return Constants.DECIMAL_FORMAT.format((getVBDSuggestedValue(player)/maxVal) * 100.0);
            case Constants.SORT_RISK:
                return String.valueOf(player.getRisk());
            case Constants.SORT_SOS:
                return String.valueOf(getSOS(player));
        }
        return "";
    }

    private String getSubTextForFactor(Player player) {
        StringBuilder subtextBuilder = new StringBuilder(generateOutputSubtext(player));
        if (!Constants.SORT_PROJECTION.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Projection: ")
                    .append(player.getProjection());
        }
        if (Constants.SORT_UNDERDRAFTED.equals(factor) || Constants.SORT_OVERDRAFTED.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("ECR: ")
                    .append(player.getEcr())
                    .append(Constants.LINE_BREAK)
                    .append("ADP: ")
                    .append(player.getAdp());
        }  else if (Constants.SORT_VBD_SUGGESTED.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("PAA: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.getPaa()))
                    .append(Constants.LINE_BREAK)
                    .append("XVal: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.getxVal()))
                    .append(Constants.LINE_BREAK)
                    .append("VoLS: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.getVOLS()));
        }
        boolean isAuction = rankings.getLeagueSettings().isAuction();
        if (isAuction && !Constants.SORT_AUCTION.equals(factor) && !Constants.SORT_ALL.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Auction Value: ")
                    .append(Constants.DECIMAL_FORMAT.format(player.getAuctionValueCustom(rankings)));
        } else if (rankings.getLeagueSettings().isSnake() && !Constants.SORT_ECR.equals(factor) && !Constants.SORT_ALL.equals(factor) &&
                !Constants.SORT_UNDERDRAFTED.equals(factor) && !Constants.SORT_OVERDRAFTED.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("ECR: ")
                    .append(player.getEcr().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getEcr());
        } else if (rankings.getLeagueSettings().isDynasty() && !Constants.SORT_DYNASTY.equals(factor) && !Constants.SORT_ALL.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Dynasty/Keeper Rank: ")
                    .append(player.getDynastyRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getDynastyRank());
            if (player.getAge() != null) {
                subtextBuilder.append(Constants.LINE_BREAK)
                        .append("Age: ")
                        .append(player.getAge());
            }
        } else if (rankings.getLeagueSettings().isRookie() && !Constants.SORT_ROOKIE.equals(factor) && !Constants.SORT_ALL.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Rookie Rank: ")
                    .append(player.getRookieRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getRookieRank());
        } else if (rankings.getLeagueSettings().isBestBall() && !Constants.SORT_BEST_BALL.equals(factor) && !Constants.SORT_ALL.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Best Ball Rank: ")
                    .append(player.getBestBallRank().equals(Constants.DEFAULT_RANK) ? Constants.DEFAULT_DISPLAY_RANK_NOT_SET : player.getBestBallRank());
        }
        if (rankings.getUserSettings().isShowNoteSort() &&
                !StringUtils.isBlank(rankings.getPlayerNote(player.getUniqueId()))) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append(rankings.getPlayerNote(player.getUniqueId()));
        }
        return subtextBuilder.toString();
    }


    private String generateOutputSubtext(Player player) {
        StringBuilder sub = new StringBuilder(player.getPosition())
                .append(Constants.POS_TEAM_DELIMITER)
                .append(player.getTeamName());
        Team team = rankings.getTeam(player);
        if (team != null) {
            sub = sub.append(" (Bye: ")
                    .append(team.getBye())
                    .append(")");
        }
        return sub.toString();
    }

    private String getPlayerKeyFromListViewItem(View view) {
        TextView playerMain = view.findViewById(R.id.player_basic);
        TextView playerInfo = view.findViewById(R.id.player_info);
        return getPlayerKeyFromPieces(playerMain.getText().toString(), playerInfo.getText().toString().split(Constants.LINE_BREAK)[0]);
    }

    private String getPlayerKeyFromPieces(String playerMain, String teamPosBye) {
        String name = playerMain.split(Constants.RANKINGS_LIST_DELIMITER)[1];
        String[] teamPos = teamPosBye.split(" \\(")[0].split(Constants.POS_TEAM_DELIMITER);
        String team = teamPos[1];
        String pos = teamPos[0];
        return name +
                Constants.PLAYER_ID_DELIMITER +
                team +
                Constants.PLAYER_ID_DELIMITER +
                pos;
    }

    private void graphSort() {
        LayoutInflater li = LayoutInflater.from(this);
        View graphView = li.inflate(R.layout.sort_graph_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(graphView);
        LineChart lineGraph =  graphView.findViewById(R.id.sort_graph);
        List<Entry> entries = new ArrayList<>();
        List<Entry> qbs = new ArrayList<>();
        List<Entry> rbs = new ArrayList<>();
        List<Entry> wrs = new ArrayList<>();
        List<Entry> tes = new ArrayList<>();
        List<Entry> dsts = new ArrayList<>();
        List<Entry> ks = new ArrayList<>();
        int actualIndex = 0;
        for (int i = 0; i < Math.min(sortMax, players.size()); i++) {
            Player player = players.get(i);
            String prefix = getMainTextPrefixForPlayer(player);
            if (Constants.DEFAULT_DISPLAY_RANK_NOT_SET.equals(prefix)) {
                continue;
            }
            double value = Double.parseDouble(getMainTextPrefixForPlayer(player));
            entries.add(new Entry(actualIndex++, (int) value));
            switch (player.getPosition()) {
                case Constants.QB:
                    qbs.add(new Entry(qbs.size(), (int) value));
                    break;
                case Constants.RB:
                    rbs.add(new Entry(rbs.size(), (int) value));
                    break;
                case Constants.WR:
                    wrs.add(new Entry(wrs.size(), (int) value));
                    break;
                case Constants.TE:
                    tes.add(new Entry(tes.size(), (int) value));
                    break;
                case Constants.DST:
                    dsts.add(new Entry(dsts.size(), (int) value));
                    break;
                case Constants.K:
                    ks.add(new Entry(ks.size(), (int) value));
                    break;
            }
        }
        LineDataSet allPositions = GraphUtils.getLineDataSet(entries, "All Positions", "blue");
        LineData lineData = new LineData();

        GraphUtils.conditionallyAddData(lineData, qbs, "QBs", "green");
        GraphUtils.conditionallyAddData(lineData, rbs, "RBs", "red");
        GraphUtils.conditionallyAddData(lineData, wrs, "WRs", "purple");
        GraphUtils.conditionallyAddData(lineData, tes, "TEs", "yellow");
        GraphUtils.conditionallyAddData(lineData, dsts, "DSTs", "black");
        GraphUtils.conditionallyAddData(lineData, ks, "Ks", "grey");

        if (lineData.getDataSetCount() > 1) {
            lineData.addDataSet(allPositions);
        }

        lineGraph.setData(lineData);
        Description description = new Description();
        description.setText(factor);
        description.setTextSize(12f);
        lineGraph.setDescription(description);
        lineGraph.invalidate();
        lineGraph.setTouchEnabled(true);
        lineGraph.setPinchZoom(true);
        lineGraph.setDragEnabled(true);
        lineGraph.animateX(1500);
        lineGraph.animateY(1500);

        XAxis x = lineGraph.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawAxisLine(true);

        YAxis yR = lineGraph.getAxisRight();
        yR.setDrawAxisLine(true);
        yR.setDrawLabels(false);

        alertDialogBuilder
                .setNegativeButton("Dismiss",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
