package com.devingotaswitch.rankings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.devingotaswitch.rankings.extras.FilterWithSpaceAdapter;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
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
        GeneralUtils.hideKeyboard(this);

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
        main_title.setVisibility(View.VISIBLE);
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
                String currentTeam = teamList.get(teams.getSelectedIndex());;
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

        // Cogneato stuff
        username = CUPHelper.getCurrUser();
        user = CUPHelper.getPool().getUser(username);
        refreshTokens();
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
            findViewById(R.id.main_toolbar_title).setVisibility(View.GONE);
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
            findViewById(R.id.main_toolbar_title).setVisibility(View.VISIBLE);
            clearAndAddView(R.layout.content_rankings_no_league);
            rankings = Rankings.initWithDefaults(currentLeague);
            searchBase.setVisibility(View.GONE);
            buttonBase.setVisibility(View.GONE);
            nDrawer.getMenu().findItem(R.id.nav_refresh_ranks).setVisible(false);
            nDrawer.getMenu().findItem(R.id.nav_export_rankings).setVisible(false);
        } else {
            // If neither of the above, there's a league but no ranks. Tell the user.
            clearAndAddView(R.layout.content_rankings_no_ranks);
            findViewById(R.id.main_toolbar_title).setVisibility(View.VISIBLE);
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

        final ListView listview =  findViewById(R.id.rankings_list);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = DisplayUtils.getDisplayAdapter(this, data);
        listview.setAdapter(adapter);
        for (int i = 0; i < Math.min(orderedIds.size(), maxPlayers); i++) {
            Player player = rankings.getPlayer(orderedIds.get(i));
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !rankings.getDraft().isDrafted(player)) {
                if (rankings.getLeagueSettings().isRookie() && player.getRookieRank() == Constants.DEFAULT_RANK) {
                    // the constant is 'not set', so skip these. No sense showing a 10 year vet in rookie ranks.
                    continue;
                }
                Map<String, String> datum = DisplayUtils.getDatumForPlayer(rankings, player, true);
                data.add(datum);
            }
        }
        adapter.notifyDataSetChanged();
        final Activity act = this;

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String playerKey = DisplayUtils.getPlayerKeyFromListViewItem(view);
                final ImageView playerStatus = view.findViewById(R.id.player_status);
                final Player player = rankings.getPlayer(playerKey);
                if (player.isWatched()) {
                    player.setWatched(false);
                    Flashbar.OnActionTapListener add = new Flashbar.OnActionTapListener() {
                        @Override
                        public void onActionTapped(@NotNull Flashbar flashbar) {
                            flashbar.dismiss();
                            player.setWatched(true);
                            playerStatus.setImageResource(R.drawable.star);
                            rankingsDB.updatePlayerWatchedStatus(act, player);
                        }
                    };
                    FlashbarFactory.generateFlashbarWithUndo(act, "Success!", player.getName() + " removed from watch list",
                            Flashbar.Gravity.BOTTOM, add)
                            .show();
                    playerStatus.setImageResource(0);
                } else {
                    player.setWatched(true);
                    Flashbar.OnActionTapListener remove = new Flashbar.OnActionTapListener() {
                        @Override
                        public void onActionTapped(@NotNull Flashbar flashbar) {
                            flashbar.dismiss();
                            player.setWatched(false);
                            playerStatus.setImageResource(0);
                            rankingsDB.updatePlayerWatchedStatus(act, player);
                        }
                    };
                    FlashbarFactory.generateFlashbarWithUndo(act, "Success!", player.getName() + " added to watch list",
                            Flashbar.Gravity.BOTTOM, remove)
                            .show();
                    playerStatus.setImageResource(R.drawable.star);
                }
                rankingsDB.updatePlayerWatchedStatus(act, player);
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playerKey = DisplayUtils.getPlayerKeyFromListViewItem(view);
                selectedIndex = position;
                displayPlayerInfo(playerKey);
            }
        });
        final Activity localCopy = this;
        final SwipeDismissTouchListener swipeListener = new SwipeDismissTouchListener(listview,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(View view) {
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
                            Flashbar.OnActionTapListener listener = DraftUtils.getUndraftListener(localCopy, rankings, player, findViewById(R.id.user_drawer_layout),
                                    adapter, data, datum, position, true);
                            if (!rightDismiss) {
                                rankings.getDraft().draftBySomeone(rankings, player, localCopy, findViewById(R.id.user_drawer_layout), listener);
                                if (LocalSettingsHelper.hideDraftedSearch(localCopy)) {
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
                listview.setSelectionAfterHeaderView();
                return true;
            }
        });

        setSearchAutocomplete();
    }

    private void getAuctionCost(final Player player, final int position, final List<Map<String, String>> data,
                                final Map<String, String> datum, final SimpleAdapter adapter, final Flashbar.OnActionTapListener listener) {
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
        if (LocalSettingsHelper.hideDraftedSearch(this)) {
            setSearchAutocomplete();
        }
    }

    private void setSearchAutocomplete() {
        final AutoCompleteTextView searchInput = searchBase.findViewById(R.id.ranking_search);
        searchInput.setAdapter(null);
        final FilterWithSpaceAdapter mAdapter = GeneralUtils.getPlayerSearchAdapter(rankings, this,
                LocalSettingsHelper.hideDraftedSearch(this), LocalSettingsHelper.hideRanklessSearch(this));
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

