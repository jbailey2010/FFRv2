package com.devingotaswitch.rankings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.youruserpools.CIBHelper;
import com.devingotaswitch.youruserpools.CUPHelper;
import com.devingotaswitch.youruserpools.ChangePasswordActivity;
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
    private CognitoUserSession session;
    private CognitoUserDetails details;
    private String username;

    private MenuItem filterItem;
    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;

    private RankingsDBWrapper rankingsDB;
    private LeagueSettings currentLeague;
    private Rankings rankings;
    private LinearLayout rankingsBase;
    private RelativeLayout searchBase;
    private LinearLayout buttonBase;
    private int maxPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        toolbar.setElevation(0);
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Rankings");
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = (DrawerLayout) findViewById(R.id.user_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = (NavigationView) findViewById(R.id.nav_view);
        setNavDrawer();
        init();
        View navigationHeader = nDrawer.getHeaderView(0);
        TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(R.id.textViewNavUserSub);
        navHeaderSubTitle.setText(username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_rankings_menu, menu);
        filterItem = menu.findItem(R.id.filter_rankings);
        filterItem.setVisible(false);
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
        initRankingsContext();
    }

    private void toggleFilterView() {
        final LinearLayout filterBase = (LinearLayout)findViewById(R.id.rankings_filter_base);
        if (filterBase.getVisibility() == View.GONE) {
            filterBase.setVisibility(View.VISIBLE);
        } else {
            filterBase.setVisibility(View.GONE);
        }
        // In place to quickly revert filterings on closing and
        // prevent a weird issue where the watched star would
        // be out of date on visibility change.
        displayRankings(rankings.getOrderedIds());
        final Spinner teams = (Spinner)filterBase.findViewById(R.id.rankings_filter_teams);
        List<String> teamList = new ArrayList<>();
        teamList.addAll(rankings.getTeams().keySet());
        Collections.sort(teamList);
        teamList.add(0, Constants.ALL_TEAMS);
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamList);
        teams.setAdapter(teamAdapter);

        final Spinner positions = (Spinner)filterBase.findViewById(R.id.rankings_filter_positions);
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
        if (roster.getRbCount() > 0 && roster.getWrCount() > 0) {
            posList.add(Constants.RBWR);
        }
        if (roster.getRbCount() > 0 && roster.getTeCount() > 0) {
            posList.add(Constants.RBTE);
        }
        if (roster.getRbCount() > 0 && roster.getWrCount() > 0 && roster.getTeCount() > 0) {
            posList.add(Constants.RBWRTE);
        }
        if (roster.getWrCount() > 0 && roster.getTeCount() > 0) {
            posList.add(Constants.WRTE);
        }
        if (roster.getQbCount() > 0 && roster.getRbCount() > 0 && roster.getWrCount() > 0 && roster.getTeCount() > 0) {
            posList.add(Constants.QBRBWRTE);
        }
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, posList);
        positions.setAdapter(positionAdapter);

        final CheckBox watched = (CheckBox)filterBase.findViewById(R.id.rankings_filter_watched);
        final EditText maxPlayersField = (EditText)filterBase.findViewById(R.id.max_players_visible);
        maxPlayersField.setText(String.valueOf(maxPlayers));

        Button submit = (Button)filterBase.findViewById(R.id.rankings_filter_submit);
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
                displayRankings(filteredIds);
            }
        });
    }

    private void init() {
        // Rankings stuff
        rankingsDB = new RankingsDBWrapper();
        searchBase = (RelativeLayout)findViewById(R.id.rankings_search_base);
        buttonBase = (LinearLayout) findViewById(R.id.rankings_button_bar);
        maxPlayers = LocalSettingsHelper.getNumVisiblePlayers(this);
        Button adpSimulator = (Button)buttonBase.findViewById(R.id.rankings_simulator);
        adpSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAdpSimulator();
            }
        });
        initRankingsContext();

        // Cogneato stuff
        Bundle extras = getIntent().getExtras();
        username = CUPHelper.getCurrUser();
        user = CUPHelper.getPool().getUser(username);
        getDetails();
    }

    private void initRankingsContext() {
        String currentLeagueId = LocalSettingsHelper.getCurrentLeagueName(this);
        if (LocalSettingsHelper.wasPresent(currentLeagueId)) {
            currentLeague = rankingsDB.getLeague(this, currentLeagueId);
        }
        rankingsBase = (LinearLayout)findViewById(R.id.rankings_base_layout);
        establishLayout();
    }

    private void establishLayout() {
        if (LocalSettingsHelper.wereRankingsFetched(this)) {
            // If rankings are saved, load (and ultimately display) them
            Rankings.loadRankings(this, rankingsDB);
        } else if (!LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
            // Otherwise, if no league is set up, display that message
            clearAndAddView(R.layout.content_rankings_no_league);
            rankings = Rankings.initWithDefaults(currentLeague);
            searchBase.setVisibility(View.GONE);
            buttonBase.setVisibility(View.GONE);
        } else {
            // If neither of the above, there's a league but no ranks. Tell the user.
            clearAndAddView(R.layout.content_rankings_no_ranks);
            rankings = Rankings.initWithDefaults(currentLeague);
            searchBase.setVisibility(View.GONE);
            buttonBase.setVisibility(View.GONE);
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

    private View clearAndAddView(int viewId) {
        rankingsBase.removeAllViews();
        View child = getLayoutInflater().inflate(viewId, null);
        rankingsBase.addView(child);
        return child;
    }

    private void displayRankings(List<String> orderedIds) {
        searchBase.setVisibility(View.VISIBLE);
        buttonBase.setVisibility(View.VISIBLE);
        filterItem.setVisible(true);
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);

        ListView listview = (ListView) findViewById(R.id.rankings_list);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS },
                new int[] { R.id.player_basic, R.id.player_info,
                R.id.player_status });
        listview.setAdapter(adapter);
        int displayedPlayers = 0;
        for (String playerKey : orderedIds) {
            Player player = rankings.getPlayer(playerKey);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition()) &&
                    !rankings.getDraft().isDrafted(player)) {
                String playerBasicContent;
                if (rankings.getLeagueSettings().isAuction()) {
                    playerBasicContent = new StringBuilder(String.valueOf(df.format(player.getAuctionValueCustom(rankings))))
                            .append(Constants.RANKINGS_LIST_DELIMITER)
                            .append(player.getName())
                            .toString();
                } else {
                    playerBasicContent = new StringBuilder(String.valueOf(player.getEcr()))
                            .append(Constants.RANKINGS_LIST_DELIMITER)
                            .append(player.getName())
                            .toString();
                }
                Map<String, String> datum = new HashMap<>(3);
                datum.put(Constants.PLAYER_BASIC, playerBasicContent);
                datum.put(Constants.PLAYER_INFO, generateOutputSubtext(player, df));
                if (player.isWatched()) {
                    datum.put(Constants.PLAYER_STATUS, Integer.toString(R.drawable.star));
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
                ImageView playerStatus = (ImageView)view.findViewById(R.id.player_status);
                Player player = rankings.getPlayer(playerKey);
                if (player.isWatched()) {
                    player.setWatched(false);
                    Toast.makeText(context, player.getName() + " removed from watch list.", Toast.LENGTH_SHORT).show();
                    playerStatus.setImageResource(0);
                } else {
                    player.setWatched(true);
                    Toast.makeText(context, player.getName() + " added to watch list.", Toast.LENGTH_SHORT).show();
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
                displayPlayerInfo(playerKey);
            }
        });

        setSearchAutocomplete();
    }

    private void setSearchAutocomplete() {
        final AutoCompleteTextView searchInput = (AutoCompleteTextView) searchBase.findViewById(R.id.ranking_search);
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

        final AutoCompleteTextView localCopy = searchInput;
        searchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                String posAndTeam = ((TextView)view.findViewById(android.R.id.text2)).getText().toString();
                String pos = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[0];
                String team = posAndTeam.split(Constants.POS_TEAM_DELIMITER)[1];
                localCopy.setText("");
                displayPlayerInfo(name + Constants.PLAYER_ID_DELIMITER + team + Constants.PLAYER_ID_DELIMITER + pos);
            }
        });

        searchInput.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                localCopy.setText("");
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

    private String getPlayerKeyFromListViewItem(View view) {
        TextView playerMain = (TextView)view.findViewById(R.id.player_basic);
        TextView playerInfo = (TextView)view.findViewById(R.id.player_info);
        String name = playerMain.getText().toString().split(Constants.RANKINGS_LIST_DELIMITER)[1];
        String teamPosBye = playerInfo.getText().toString().split(Constants.LINE_BREAK)[0];
        String teamPos = teamPosBye.split(" \\(")[0];
        String team = teamPos.split(Constants.POS_TEAM_DELIMITER)[1];
        String pos = teamPos.split(Constants.POS_TEAM_DELIMITER)[0];

        return new StringBuilder(name)
                .append(Constants.PLAYER_ID_DELIMITER)
                .append(team)
                .append(Constants.PLAYER_ID_DELIMITER)
                .append(pos)
                .toString();
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
            case R.id.nav_league_settings:
                // Set league settings
                leagueSettings();
                break;
            case R.id.nav_refresh_ranks:
                // Refresh ranks
                refreshRanks();
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

    private void leagueSettings() {
        Intent leagueSettingsActivity = new Intent(this, LeagueSettingsActivity.class);
        startActivity(leagueSettingsActivity);
    }

    private void refreshRanks() {
        // Don't let the user refresh if there's no saved league
        if (GeneralUtils.confirmInternet(this)) {
            if (LocalSettingsHelper.wasPresent(LocalSettingsHelper.getCurrentLeagueName(this))) {
                rankings.refreshRankings(this);
            } else {
                Toast.makeText(this, "Please set up a league before getting rankings", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
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
        CIBHelper.signOut();
        exit();
    }

    private void getDetails() {
        CUPHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler);
    }

    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            CUPHelper.setUserDetails(cognitoUserDetails);
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Could not fetch user details!", CUPHelper.formatException(exception), true);
        }
    };

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit) {
                        exit();
                    }
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG,"Dialog dismiss failed");
                    if(exit) {
                        exit();
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void exit () {
        Intent intent = new Intent();
        if(username == null)
            username = "";
        intent.putExtra("name",username);
        setResult(RESULT_OK, intent);
        finish();
    }
}
