package com.devingotaswitch.rankings;

import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.youruserpools.CIBHelper;
import com.devingotaswitch.youruserpools.CUPHelper;
import com.devingotaswitch.youruserpools.ChangePasswordActivity;
import com.devingotaswitch.youruserpools.UserActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingsHome extends AppCompatActivity {
    private final String TAG="RankingsActivity";

    // Cognito user objects
    private CognitoUser user;
    private CognitoUserSession session;
    private CognitoUserDetails details;

    // User details
    private String username;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
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
        getMenuInflater().inflate(R.menu.activity_user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();

        return super.onOptionsItemSelected(item);
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

    // Initialize this activit1
    private void init() {
        // Rankings stuff
        rankingsDB = new RankingsDBWrapper();
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
            rankings = new Rankings(currentLeague);
        } else {
            // If neither of the above, there's a league but no ranks. Tell the user.
            clearAndAddView(R.layout.content_rankings_no_ranks);
            rankings = new Rankings(currentLeague);
        }
    }

    public void processNewRankings(Rankings newRankings, boolean saveRanks) {
        rankings = newRankings;
        displayRankings();
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

    private void displayRankings() {
        View view = clearAndAddView(R.layout.content_rankings_display);
        String playerBasic = "main";
        String playerInfo = "info";
        String playerStatus = "status";
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);

        ListView listview = (ListView) view.findViewById(R.id.rankings_list);
        listview.setAdapter(null);
        List<Map<String, String>> data = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { playerBasic, playerInfo, playerStatus },
                new int[] { R.id.player_basic, R.id.player_info,
                R.id.player_status });
        listview.setAdapter(adapter);
        for (String playerKey : rankings.getOrderedIds()) {
            Player player = rankings.getPlayer(playerKey);
            String playerBasicContent;
            if (rankings.getLeagueSettings().isAuction()) {
                playerBasicContent = new StringBuilder(String.valueOf(df.format(player.getAuctionValue())))
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
            datum.put(playerBasic, playerBasicContent);
            datum.put(playerInfo, generateOutputSubtext(player, df));
            if (player.isWatched()) {
                datum.put(playerStatus, Constants.WATCHED_FLAG);
            }
            data.add(datum);
        }
        adapter.notifyDataSetChanged();
        // TODO: this
    }

    private String generateOutputSubtext(Player player, DecimalFormat df) {
        StringBuilder sub = new StringBuilder(player.getPosition())
                .append(" - ")
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
