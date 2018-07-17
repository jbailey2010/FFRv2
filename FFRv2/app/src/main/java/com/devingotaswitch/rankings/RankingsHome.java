package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.util.StringUtils;
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.extras.SwipeDismissTouchListener;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.youruserpools.CUPHelper;
import com.devingotaswitch.youruserpools.ChangePasswordActivity;
import com.devingotaswitch.youruserpools.MainActivity;
import com.devingotaswitch.youruserpools.UserActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingsHome extends AppCompatActivity {
    private final String TAG="RankingsActivity";

    // Cognito user objects
    private CognitoUser user;
    private String username;
    private ProgressDialog waitDialog;

    private MenuItem filterItem;
    private NavigationView nDrawer;
    private DrawerLayout mDrawer;

    private RankingsDBWrapper rankingsDB;
    private LeagueSettings currentLeague;
    private Rankings rankings;
    private LinearLayout rankingsBase;
    private RelativeLayout searchBase;
    private LinearLayout buttonBase;
    private int maxPlayers;
    private boolean loadRanks;
    private static Integer selectedIndex = 0;
    private boolean ranksDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Relying on onResume to display stuff.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_rankings_menu, menu);
        filterItem = menu.findItem(R.id.filter_rankings);
        setFilterItemVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();
        switch(menuItem) {
            case R.id.filter_rankings:
                toggleFilterView();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        try {
            initApp();
            init();
        } catch(Exception e) {
            try {
                initApp();
                init();
            } catch (Exception e2) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    private void setFilterItemVisibility() {
        if (rankings == null) {
            rankings = Rankings.initWithDefaults(currentLeague);
        }
        if (rankings.getLeagueSettings() != null && rankings.getPlayers().size() > 0) {
            filterItem.setVisible(true);
        } else {
            filterItem.setVisible(false);
        }
    }

    private void initApp() {
        // Set toolbar for this screen
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        toolbar.setElevation(0);
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText("Rankings");
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = findViewById(R.id.user_drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = findViewById(R.id.nav_view);
        setNavDrawer();
        rankingsDB = new RankingsDBWrapper();
        String currentLeagueId = LocalSettingsHelper.getCurrentLeagueName(this);
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currentLeague = rankingsDB.getLeague(this, currentLeagueId);
        }
        loadRanks = getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Constants.RANKINGS_UPDATED);
        if (!StringUtils.isBlank(CUPHelper.getCurrUser())) {
            View navigationHeader = nDrawer.getHeaderView(0);
            TextView navHeaderSubTitle = navigationHeader.findViewById(R.id.textViewNavUserSub);
            navHeaderSubTitle.setText(CUPHelper.getCurrUser());
        }
    }

    private void toggleFilterView() {
        final LinearLayout filterBase = findViewById(R.id.rankings_filter_base);
        if (filterBase.getVisibility() == View.GONE) {
            filterBase.setVisibility(View.VISIBLE);
        } else {
            filterBase.setVisibility(View.GONE);
        }
        // In place to quickly revert filterings on closing and
        // prevent a weird issue where the watched star would
        // be out of date on visibility change.
        displayRankings(rankings.getOrderedIds());
        final Spinner teams = filterBase.findViewById(R.id.rankings_filter_teams);
        List<String> teamList = new ArrayList<>(rankings.getTeams().keySet());
        Collections.sort(teamList);
        teamList.add(0, Constants.ALL_TEAMS);
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamList);
        teams.setAdapter(teamAdapter);

        final Spinner positions = filterBase.findViewById(R.id.rankings_filter_positions);
        List<String> posList = new ArrayList<>();
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
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, posList);
        positions.setAdapter(positionAdapter);

        final CheckBox watched = filterBase.findViewById(R.id.rankings_filter_watched);
        final EditText maxPlayersField = filterBase.findViewById(R.id.max_players_visible);
        maxPlayersField.setText(String.valueOf(maxPlayers));

        Button submit = filterBase.findViewById(R.id.rankings_filter_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTeam = ((TextView)teams.getSelectedView()).getText().toString();
                String currentPosition = ((TextView)positions.getSelectedView()).getText().toString();
                boolean isWatched = watched.isChecked();
                List<String> filteredIds = rankings.getOrderedIds();
                if (!Constants.ALL_POSITIONS.equals(currentPosition)) {
                    filteredIds = rankings.getPlayersByPosition(filteredIds, currentPosition);
                }
                if (!Constants.ALL_TEAMS.equals(currentTeam)) {
                    filteredIds = rankings.getPlayersByTeam(filteredIds, currentTeam);
                }
                if (isWatched) {
                    filteredIds = rankings.getWatchedPlayers(filteredIds);
                }
                String maxPlayersInput = maxPlayersField.getText().toString();
                if (GeneralUtils.isInteger(maxPlayersInput)) {
                    maxPlayers = Integer.parseInt(maxPlayersInput);
                    LocalSettingsHelper.saveNumVisiblePlayers(getApplication(), maxPlayers);
                }
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(filterBase.getWindowToken(), 0);
                displayRankings(filteredIds);
            }
        });
    }

    private void init() {
        // Rankings stuff
        searchBase = findViewById(R.id.rankings_search_base);
        buttonBase = findViewById(R.id.rankings_button_bar);
        maxPlayers = LocalSettingsHelper.getNumVisiblePlayers(this);
        ImageButton adpSimulator = buttonBase.findViewById(R.id.rankings_simulator);
        adpSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAdpSimulator();
            }
        });
        ImageButton draftInfo = buttonBase.findViewById(R.id.rankings_draft_info);
        draftInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDraftInfo();
            }
        });
        ImageButton comparePlayers = buttonBase.findViewById(R.id.rankings_comparator);
        comparePlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comparePlayers();
            }
        });
        ImageButton sortPlayers = buttonBase.findViewById(R.id.rankings_sort);
        sortPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortPlayers();
            }
        });
        initRankingsContext();

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        // Cogneato stuff
        username = CUPHelper.getCurrUser();
        user = CUPHelper.getPool().getUser(username);
        getDetails();
    }

    private void initRankingsContext() {
        String currentLeagueId = LocalSettingsHelper.getCurrentLeagueName(this);
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currentLeague = rankingsDB.getLeague(this, currentLeagueId);
        }
        rankingsBase = findViewById(R.id.rankings_base_layout);
        establishLayout();
    }

    private void establishLayout() {
        if (LocalSettingsHelper.wereRankingsFetched(this)) {
            nDrawer.getMenu().findItem(R.id.nav_refresh_ranks).setVisible(true);
            nDrawer.getMenu().findItem(R.id.nav_export_rankings).setVisible(true);
            // If rankings are saved, load (and ultimately display) them
            if (rankings == null || rankings.getPlayers().size() == 0 || loadRanks) {
                Rankings.loadRankings(this, rankingsDB);
            } else {
                boolean nullPlayer = false;
                for (String key : rankings.getPlayers().keySet()) {
                    if (rankings.getPlayer(key) == null) {
                        nullPlayer = true;
                    }
                }
                if (nullPlayer) {
                    Log.d(TAG, "Null value was found, re-loading rankings.");
                    Rankings.loadRankings(this, rankingsDB);
                } else {
                    processNewRankings(rankings, false);
                }
            }
        } else if (!LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
            // Otherwise, if no league is set up, display that message
            clearAndAddView(R.layout.content_rankings_no_league);
            rankings = Rankings.initWithDefaults(currentLeague);
            searchBase.setVisibility(View.GONE);
            buttonBase.setVisibility(View.GONE);
            nDrawer.getMenu().findItem(R.id.nav_refresh_ranks).setVisible(false);
            nDrawer.getMenu().findItem(R.id.nav_export_rankings).setVisible(false);
        } else {
            // If neither of the above, there's a league but no ranks. Tell the user.
            clearAndAddView(R.layout.content_rankings_no_ranks);
            rankings = Rankings.initWithDefaults(currentLeague);
            searchBase.setVisibility(View.GONE);
            buttonBase.setVisibility(View.GONE);
            nDrawer.getMenu().findItem(R.id.nav_refresh_ranks).setVisible(true);
            nDrawer.getMenu().findItem(R.id.nav_export_rankings).setVisible(false);
        }
    }

    public void processNewRankings(Rankings newRankings, boolean saveRanks) {
        rankings = newRankings;
        clearAndAddView(R.layout.content_rankings_display);
        displayRankings(rankings.getOrderedIds());
        if (saveRanks) {
            // Don't save again if we're just displaying rankings we just loaded
            rankings.saveRankings(this, rankingsDB);
        }
    }

    private void clearAndAddView(int viewId) {
        rankingsBase.removeAllViews();
        View child = getLayoutInflater().inflate(viewId, null);
        rankingsBase.addView(child);
    }

    private void displayRankings(List<String> orderedIds) {
        ranksDisplayed = false;
        searchBase.setVisibility(View.VISIBLE);
        buttonBase.setVisibility(View.VISIBLE);
        nDrawer.getMenu().findItem(R.id.nav_refresh_ranks).setVisible(true);
        nDrawer.getMenu().findItem(R.id.nav_export_rankings).setVisible(true);
        if (filterItem != null) {
            filterItem.setVisible(true);
        }
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);

        final ListView listview =  findViewById(R.id.rankings_list);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_ADDITIONAL_INFO,
                Constants.PLAYER_ADDITIONAL_INFO_2},
                new int[] { R.id.player_basic, R.id.player_info,
                R.id.player_status, R.id.player_more_info, R.id.player_additional_info_2 });
        listview.setAdapter(adapter);
        int displayedPlayers = 0;
        for (String playerKey : orderedIds) {
            Player player = rankings.getPlayer(playerKey);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !rankings.getDraft().isDrafted(player)) {
                if (rankings.getLeagueSettings().isRookie() && player.getRookieRank() == 300.0) {
                    // 300 is 'not set', so skip these. No sense showing a 10 year vet in rookie ranks.
                    continue;
                }
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
                if (player.isWatched()) {
                    datum.put(Constants.PLAYER_STATUS, Integer.toString(R.drawable.star));
                }
                if (player.getAge() != null  && !Constants.DST.equals(player.getPosition())) {
                    datum.put(Constants.PLAYER_ADDITIONAL_INFO, "Age: " + player.getAge());
                }
                if (player.getExperience() != null && player.getExperience() >= 0 && !Constants.DST.equals(player.getPosition())) {
                    datum.put(Constants.PLAYER_ADDITIONAL_INFO_2, "Exp: " + player.getExperience());
                }
                data.add(datum);
                displayedPlayers++;
                if (displayedPlayers == maxPlayers) {
                    break;
                }
            }
        }
        adapter.notifyDataSetChanged();
        final Context context = this;

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String playerKey = getPlayerKeyFromListViewItem(view);
                final ImageView playerStatus = view.findViewById(R.id.player_status);
                final Player player = rankings.getPlayer(playerKey);
                if (player.isWatched()) {
                    player.setWatched(false);
                    View.OnClickListener add = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            player.setWatched(true);
                            playerStatus.setImageResource(R.drawable.star);
                            rankingsDB.updatePlayerWatchedStatus(context, player);
                        }
                    };
                    Snackbar.make(buttonBase, player.getName() + " removed from watch list", Snackbar.LENGTH_LONG).setAction("Undo", add).show();
                    playerStatus.setImageResource(0);
                } else {
                    player.setWatched(true);
                    View.OnClickListener remove = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            player.setWatched(false);
                            playerStatus.setImageResource(0);
                            rankingsDB.updatePlayerWatchedStatus(context, player);
                        }
                    };
                    Snackbar.make(buttonBase, player.getName() + " added to watch list", Snackbar.LENGTH_LONG).setAction("Undo", remove).show();
                    playerStatus.setImageResource(R.drawable.star);
                }
                rankingsDB.updatePlayerWatchedStatus(context, player);
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playerKey = getPlayerKeyFromListViewItem(view);
                selectedIndex = position;
                displayPlayerInfo(playerKey);
            }
        });
        final Activity localCopy = this;
        final SwipeDismissTouchListener swipeListener = new SwipeDismissTouchListener(listview,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView,
                                          int[] reverseSortedPositions,
                                          boolean rightDismiss) {
                        for (final int position : reverseSortedPositions) {
                            final Map<String, String> datum = data.get(position);
                            String name = datum.get(Constants.PLAYER_BASIC).split(Constants.RANKINGS_LIST_DELIMITER)[1];
                            String posAndTeam = datum.get(Constants.PLAYER_INFO).split("\n")[0].split(" \\(")[0];
                            String pos = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[0];
                            String team = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[1];
                            final Player player  = rankings.getPlayer(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos);
                            View.OnClickListener listener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    rankings.getDraft().undraft(rankings, player, localCopy, findViewById(R.id.user_drawer_layout));
                                    data.add(position, datum);
                                    adapter.notifyDataSetChanged();
                                }
                            };
                            if (!rightDismiss) {
                                rankings.getDraft().draftBySomeone(rankings, player, localCopy, findViewById(R.id.user_drawer_layout), listener);
                            } else {
                                if (rankings.getLeagueSettings().isAuction()) {
                                    getAuctionCost(player, position, data, datum, adapter, listener);
                                } else {
                                    draftByMe(player, 0, listener);
                                }
                            }
                            data.remove(position);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        listview.setOnTouchListener(swipeListener);
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                swipeListener.setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (ranksDisplayed) {
                    // A flag is used to green light this set, otherwise onScroll is set to 0 on initial display
                    selectedIndex = firstVisibleItem;
                }
            }
        });
        listview.setSelection(selectedIndex);
        ranksDisplayed = true;
        findViewById(R.id.main_toolbar_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.smoothScrollToPosition(0);
            }
        });

        setSearchAutocomplete();
    }

    private void getAuctionCost(final Player player, final int position, final List<Map<String, String>> data,
                                final Map<String, String> datum, final SimpleAdapter adapter, final View.OnClickListener listener) {
        LayoutInflater li = LayoutInflater.from(this);
        View noteView = li.inflate(R.layout.user_input_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(noteView);
        final EditText userInput = noteView
                .findViewById(R.id.user_input_popup_input);
        userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        userInput.setHint("Auction cost");

        TextView title = noteView.findViewById(R.id.user_input_popup_title);
        title.setText("How much did " + player.getName() + " cost?");
        alertDialogBuilder
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String costStr = userInput.getText().toString();
                                if (StringUtils.isBlank(costStr) || !GeneralUtils.isInteger(costStr)) {
                                    Snackbar.make(findViewById(R.id.user_drawer_layout), "Must provide a number for cost", Snackbar.LENGTH_SHORT).show();
                                    data.add(position, datum);
                                    adapter.notifyDataSetChanged();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                } else {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    draftByMe(player, Integer.parseInt(costStr), listener);
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                data.add(position, datum);
                                adapter.notifyDataSetChanged();
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void draftByMe(Player player, int cost, View.OnClickListener listener) {
        rankings.getDraft().draftByMe(rankings, player, this, cost, findViewById(R.id.user_drawer_layout), listener);
    }

    private void setSearchAutocomplete() {
        final AutoCompleteTextView searchInput = searchBase.findViewById(R.id.ranking_search);
        searchInput.setAdapter(null);
        final FilterWithSpaceAdapter mAdapter = GeneralUtils.getPlayerSearchAdapter(rankings, this);
        searchInput.setAdapter(mAdapter);

        searchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchInput.setText("");
                displayPlayerInfo(GeneralUtils.getPlayerIdFromSearchView(view));
            }
        });

        searchInput.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                searchInput.setText("");
                return true;
            }
        });
    }

    private void displayPlayerInfo(String playerKey) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, playerKey);
        startActivity(intent);
    }

    private void getAdpSimulator() {
        Intent intent = new Intent(this, ADPSimulator.class);
        startActivity(intent);
    }

    private void getDraftInfo() {
        Intent intent = new Intent(this, DraftInfo.class);
        startActivity(intent);
    }

    private void comparePlayers() {
        Intent intent = new Intent(this, PlayerComparator.class);
        startActivity(intent);
    }

    private void sortPlayers() {
        Intent intent = new Intent(this, PlayerSorter.class);
        startActivity(intent);
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

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();

        // Find which item was selected
        switch(item.getItemId()) {
            case R.id.nav_player_news:
                // See player news
                playerNews();
                break;
            case R.id.nav_league_settings:
                // Set league settings
                leagueSettings();
                break;
            case R.id.nav_refresh_ranks:
                // Refresh ranks
                refreshRanks();
                break;
            case R.id.nav_export_rankings:
                // Export rankings
                exportRanks();
                break;
            case R.id.nav_rankings_help:
                // Help info
                getHelp();
                break;
            case R.id.nav_user_profile:
                // See profile
                viewProfile();
                break;
            case R.id.nav_user_change_password:
                // Change password
                changePassword();
                break;
            case R.id.nav_user_sign_out:
                // Sign out from this account
                signOut();
                break;
        }
    }

    private void playerNews() {
        Intent playerNews = new Intent(this, FantasyNews.class);
        startActivity(playerNews);
    }

    private void leagueSettings() {
        Intent leagueSettingsActivity = new Intent(this, LeagueSettingsActivity.class);
        startActivity(leagueSettingsActivity);
    }

    private void exportRanks() {
        if (rankings.getPlayers().size() > 0) {
            Intent exportRanksActivity = new Intent(this, ExportRankings.class);
            startActivity(exportRanksActivity);
        } else {
            Snackbar.make(buttonBase, "No rankings saved to export", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void getHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    private void refreshRanks() {
        // Don't let the user refresh if there's no saved league
        if (GeneralUtils.confirmInternet(this)) {
            if (LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
                rankings.refreshRankings(this);
            } else {
                Snackbar.make(buttonBase, "Set up a league before getting rankings", Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(buttonBase, "No internet connection", Snackbar.LENGTH_LONG).show();
        }
    }

    private void viewProfile() {
        Intent userActivity = new Intent(this, UserActivity.class);
        startActivity(userActivity);
    }

    private void changePassword() {
        Intent changePssActivity = new Intent(this, ChangePasswordActivity.class);
        startActivity(changePssActivity);
    }

    private void signOut() {
        user.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void getDetails() {
        CUPHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler);
    }

    private final GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            CUPHelper.setUserDetails(cognitoUserDetails);
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            Log.d(TAG, "Failed to get user: " + CUPHelper.formatException(exception));
            Snackbar.make(buttonBase, "Can't validate account, please sign in again", Snackbar.LENGTH_SHORT).show();
            signOut();
        }
    };

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception ignored) {
        }
    }

    private void exit () {
        finishAffinity();
    }
}
