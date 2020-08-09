package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import com.devingotaswitch.rankings.extras.RecyclerViewAdapter;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.util.StringUtils;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.rankings.domain.UserSettings;
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.extras.RankingsListView;
import com.devingotaswitch.rankings.extras.SwipeDismissTouchListener;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.DisplayUtils;
import com.devingotaswitch.utils.DraftUtils;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.youruserpools.CUPHelper;
import com.devingotaswitch.youruserpools.ChangePasswordActivity;
import com.devingotaswitch.youruserpools.MainActivity;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RankingsHome extends AppCompatActivity {
    private final String TAG="RankingsActivity";

    // Cognito user objects
    private CognitoUser user;
    private String username;

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

    private boolean settingsNeedRefresh = true;

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
        if (menuItem == R.id.filter_rankings) {
            toggleFilterView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public void onResume() {
        super.onResume();
        GeneralUtils.hideKeyboard(this);

        try {
            initApp();
            init();
        } catch(Exception e) {
            Log.d(TAG, "Error initializing app", e);
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
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = findViewById(R.id.user_drawer_layout);
        final Activity act = this;
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                GeneralUtils.hideKeyboard(act);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                GeneralUtils.hideKeyboard(act);
            }
        };
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

    public void setUserSettings(UserSettings userSettings) {
        Rankings.setUserSettings(userSettings);
        this.settingsNeedRefresh = false;
        setSearchAutocomplete();
        ((RankingsListView)findViewById(R.id.rankings_list))
                .setRefreshRanksOnOverscroll(userSettings.isRefreshOnOverscroll());
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
        final NiceSpinner teams = filterBase.findViewById(R.id.rankings_filter_teams);
        final List<String> teamList = new ArrayList<>(rankings.getTeams().keySet());
        Collections.sort(teamList);
        teamList.add(0, Constants.ALL_TEAMS);
        teams.attachDataSource(teamList);
        teams.setBackgroundColor(Color.parseColor("#FAFAFA"));

        final NiceSpinner positions = filterBase.findViewById(R.id.rankings_filter_positions);
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

        final CheckBox watched = filterBase.findViewById(R.id.rankings_filter_watched);
        final EditText maxPlayersField = filterBase.findViewById(R.id.max_players_visible);
        maxPlayersField.setText(String.valueOf(maxPlayers));

        Button submit = filterBase.findViewById(R.id.rankings_filter_submit);
        final Activity act = this;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTeam = teamList.get(teams.getSelectedIndex());
                String currentPosition = posList.get(positions.getSelectedIndex());
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
                    if (!rankings.getUserSettings().isSortWatchListByTime()) {
                        List<Player> filteredPlayers = new ArrayList<>();
                        for (String id : filteredIds) {
                            filteredPlayers.add(rankings.getPlayer(id));
                        }
                        filteredIds = rankings.orderPlayersByLeagueType(filteredPlayers);
                    }
                }
                String maxPlayersInput = maxPlayersField.getText().toString();
                if (GeneralUtils.isInteger(maxPlayersInput)) {
                    maxPlayers = Integer.parseInt(maxPlayersInput);
                    LocalSettingsHelper.saveNumVisiblePlayers(getApplication(), maxPlayers);
                }
                GeneralUtils.hideKeyboard(act);
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

        GeneralUtils.hideKeyboard(this);

        if (LocalSettingsHelper.wereRankingsFetched(this)) {
            AppSyncHelper.getUserCustomPlayerData(this);
        }

        // Cogneato stuff
        username = CUPHelper.getCurrUser();
        user = CUPHelper.getPool().getUser(username);
        refreshTokens();
    }

    public void setUserCustomData(List<String> watchList, Map<String, String> notes) {
        Rankings.setCustomUserData(watchList, notes);

        if (findViewById(R.id.rankings_list) != null) {
            // Just in case to avoid random activity shifts
            displayRankings(rankings.getOrderedIds());
        }
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
                Log.d(TAG, "Loading rankings");
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
        if (getIntent().getBooleanExtra(Constants.RANKINGS_LIST_RELOAD_NEEDED, false)) {
            setUserSettings(rankings.getUserSettings());
        }
        if (settingsNeedRefresh) {
            AppSyncHelper.getUserSettings(this);
        }
    }

    private void clearAndAddView(int viewId) {
        rankingsBase.removeAllViews();
        View child = getLayoutInflater().inflate(viewId, null);
        rankingsBase.addView(child);
    }

    private void displayRankings(List<String> orderedIds) {
        // First, pre-process the ordered ids to get personal ranks
        Map<String, Integer> playerRanks = DisplayUtils.getPositionalRank(orderedIds, rankings);
        ranksDisplayed = false;
        searchBase.setVisibility(View.VISIBLE);
        buttonBase.setVisibility(View.VISIBLE);
        nDrawer.getMenu().findItem(R.id.nav_refresh_ranks).setVisible(true);
        nDrawer.getMenu().findItem(R.id.nav_export_rankings).setVisible(true);
        if (filterItem != null) {
            filterItem.setVisible(true);
        }

        final RankingsListView listview =  findViewById(R.id.rankings_list);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final RecyclerViewAdapter adapter = DisplayUtils.getDisplayAdapter(this, data);
        listview.setAdapter(adapter);
        for (int i = 0; i < Math.min(orderedIds.size(), maxPlayers); i++) {
            Player player = rankings.getPlayer(orderedIds.get(i));
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !rankings.getDraft().isDrafted(player)) {
                if (rankings.getLeagueSettings().isRookie() && Objects.equals(player.getRookieRank(), Constants.DEFAULT_RANK)) {
                    // the constant is 'not set', so skip these. No sense showing a 10 year vet in rookie ranks.
                    continue;
                }
                Map<String, String> datum = DisplayUtils.getDatumForPlayer(rankings, player,
                        true, playerRanks.get(player.getUniqueId()),
                        rankings.getUserSettings().isShowNoteRank());
                data.add(datum);
            }
        }
        adapter.notifyDataSetChanged();
        final Activity act = this;

        RecyclerViewAdapter.OnItemLongClickListener longClickListener = new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                String playerKey = DisplayUtils.getPlayerKeyFromListViewItem(view);
                final ImageView playerStatus = view.findViewById(R.id.player_status);
                final Player player = rankings.getPlayer(playerKey);
                if (rankings.isPlayerWatched(playerKey)) {
                    rankings.togglePlayerWatched(act, player.getUniqueId());
                    Flashbar.OnActionTapListener add = new Flashbar.OnActionTapListener() {
                        @Override
                        public void onActionTapped(Flashbar flashbar) {
                            flashbar.dismiss();
                            playerStatus.setImageResource(R.drawable.star);
                            rankings.togglePlayerWatched(act, player.getUniqueId());
                        }
                    };
                    FlashbarFactory.generateFlashbarWithUndo(act, "Success!", player.getName() + " removed from watch list",
                            Flashbar.Gravity.BOTTOM, add)
                            .show();
                    playerStatus.setImageResource(0);
                } else {
                    rankings.togglePlayerWatched(act, player.getUniqueId());
                    Flashbar.OnActionTapListener remove = new Flashbar.OnActionTapListener() {
                        @Override
                        public void onActionTapped(Flashbar flashbar) {
                            flashbar.dismiss();
                            playerStatus.setImageResource(0);
                            rankings.togglePlayerWatched(act, player.getUniqueId());
                        }
                    };
                    FlashbarFactory.generateFlashbarWithUndo(act, "Success!", player.getName() + " added to watch list",
                            Flashbar.Gravity.BOTTOM, remove)
                            .show();
                    playerStatus.setImageResource(R.drawable.star);
                }
                return true;
            }
        };
        RecyclerViewAdapter.OnItemClickListener onItemClickListener = new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String playerKey = DisplayUtils.getPlayerKeyFromListViewItem(view);
                selectedIndex = position;
                displayPlayerInfo(playerKey);
            }
        };
        final Activity localCopy = this;
        final SwipeDismissTouchListener swipeListener = new SwipeDismissTouchListener(listview,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(View view) {
                        return true;
                    }

                    @Override
                    public void onDismiss(RecyclerView listView,
                                          int[] reverseSortedPositions,
                                          boolean rightDismiss) {
                        for (final int position : reverseSortedPositions) {
                            final Map<String, String> datum = data.get(position);
                            String name = datum.get(Constants.PLAYER_BASIC).split(Constants.RANKINGS_LIST_DELIMITER)[1];
                            String posAndTeam = datum.get(Constants.PLAYER_INFO).split("\n")[0].split(" \\(")[0];
                            String pos = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[0].replaceAll("\\d","");
                            String team = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[1];
                            final Player player  = rankings.getPlayer(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos);
                            Flashbar.OnActionTapListener listener = DraftUtils.getUndraftListener(localCopy, rankings, player, findViewById(R.id.user_drawer_layout),
                                    adapter, data, datum, position, true);
                            if (!rightDismiss) {
                                rankings.getDraft().draftBySomeone(rankings, player, localCopy, findViewById(R.id.user_drawer_layout), listener);
                                if (rankings.getUserSettings().isHideDraftedSearch()) {
                                    setSearchAutocomplete();
                                }
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
                }, onItemClickListener, longClickListener);
        adapter.setOnTouchListener(swipeListener);
        listview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
                swipeListener.setEnabled(scrollState == RecyclerView.SCROLL_STATE_IDLE);
            }

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (ranksDisplayed) {
                    // A flag is used to green light this set, otherwise onScroll is set to 0 on initial display
                    LinearLayoutManager layoutManager = (LinearLayoutManager) view.getLayoutManager();
                    selectedIndex = layoutManager.findFirstCompletelyVisibleItemPosition();
                }
            }
        });
        listview.getLayoutManager().scrollToPosition(selectedIndex);
        ranksDisplayed = true;
        listview.addItemDecoration(DisplayUtils.getVerticalDividerDecoration(this));

        setSearchAutocomplete();
    }

    private void getAuctionCost(final Player player, final int position, final List<Map<String, String>> data,
                                final Map<String, String> datum, final RecyclerViewAdapter adapter, final Flashbar.OnActionTapListener listener) {
        final Activity act = this;
        DraftUtils.AuctionCostInterface callback = new DraftUtils.AuctionCostInterface() {
            @Override
            public void onValidInput(Integer cost) {
                GeneralUtils.hideKeyboard(act);
                draftByMe(player, cost, listener);
            }

            @Override
            public void onInvalidInput() {
                GeneralUtils.hideKeyboard(act);
                FlashbarFactory.generateTextOnlyFlashbar(act, "No can do", "Must provide a number for cost", Flashbar.Gravity.BOTTOM)
                        .show();
                data.add(position, datum);
                adapter.notifyDataSetChanged();
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

    private void draftByMe(Player player, int cost, Flashbar.OnActionTapListener listener) {
        rankings.getDraft().draftByMe(rankings, player, this, cost, findViewById(R.id.user_drawer_layout), listener);
        if (rankings.getUserSettings().isHideDraftedSearch()) {
            setSearchAutocomplete();
        }
    }

    private void setSearchAutocomplete() {
        final AutoCompleteTextView searchInput = searchBase.findViewById(R.id.ranking_search);
        searchInput.setAdapter(null);
        final FilterWithSpaceAdapter mAdapter = GeneralUtils.getPlayerSearchAdapter(rankings, this,
                rankings.getUserSettings().isHideDraftedSearch(),
                rankings.getUserSettings().isHideRanklessSearch());
        searchInput.setAdapter(mAdapter);

        searchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchInput.setText("");
                displayPlayerInfo(GeneralUtils.getPlayerIdFromSearchView(view));
            }
        });

        final RankingsListView listview =  findViewById(R.id.rankings_list);
        searchInput.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (searchInput.getText().length() > 0) {
                    searchInput.setText("");
                } else {
                    listview.smoothScrollToPosition(0);
                }
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
                playerNews();
                break;
            case R.id.nav_league_settings:
                leagueSettings();
                break;
            case R.id.nav_refresh_ranks:
                refreshRanks();
                break;
            case R.id.nav_export_rankings:
                exportRanks();
                break;
            case R.id.nav_rankings_help:
                getHelp();
                break;
            case R.id.nav_user_settings:
                userSettings();
                break;
            case R.id.nav_user_change_password:
                changePassword();
                break;
            case R.id.nav_user_sign_out:
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
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "No rankings saved to export", Flashbar.Gravity.BOTTOM)
                    .show();
        }
    }

    private void getHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void refreshRanks() {
        // Don't let the user refresh if there's no saved league
        if (GeneralUtils.confirmInternet(this)) {
            if (LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
                rankings.refreshRankings(this);
            } else {
                FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "Set up a league before getting rankings", Flashbar.Gravity.BOTTOM)
                        .show();
            }
        } else {
            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "No internet connection", Flashbar.Gravity.BOTTOM)
                    .show();
        }
    }

    private void userSettings() {
        Intent settingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingsActivity);
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

    private void refreshTokens() {
        Log.d(TAG, "Beginning token refresh check");
        CUPHelper.getPool().getUser(username).getSessionInBackground(new RefreshSessionHandler());
    }

    private class RefreshSessionHandler implements AuthenticationHandler {
        RefreshSessionHandler() {
        }

        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            Log.i(TAG, "Refresh success");
            CUPHelper.setCurrSession(cognitoUserSession);
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            Log.d(TAG, "Get auth details challenge thrown from rankings page, should never happen.");
            signOut();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            Log.d(TAG, "MFA challenge thrown from rankings page, should never happen.");
            signOut();
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            Log.d(TAG, "Authentication challenge thrown from rankings page, should never happen.");
            signOut();
        }

        @Override
        public void onFailure(Exception e) {
            Log.d(TAG, "Failed to refresh token from rankings page, should never happen.", e);
            signOut();
        }
    }

    private void exit () {
        finishAffinity();
    }
}

