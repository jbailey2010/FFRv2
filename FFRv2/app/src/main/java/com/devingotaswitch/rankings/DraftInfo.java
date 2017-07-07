package com.devingotaswitch.rankings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DraftInfo extends AppCompatActivity {

    private Rankings rankings;

    private LinearLayout baseLayout;
    private MenuItem viewTeam;
    private MenuItem undraftPlayers;

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_draft_info);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Current Draft");
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
        rankings.getDraft().resetDraft(this);
        Toast.makeText(this, "Draft cleared", Toast.LENGTH_SHORT).show();
        displayTeam();
    }

    private void init() {
        baseLayout = (LinearLayout) findViewById(R.id.draft_info_base);
        displayTeam();
    }

    private void displayTeam() {
        View view = clearAndAddView(R.layout.content_draft_info_team);
        TextView teamView = (TextView)view.findViewById(R.id.base_textview_team);
        StringBuilder teamOutput = new StringBuilder(getTeamStr());
        if (rankings.getLeagueSettings().isAuction()) {
            teamOutput.append(getAuctionValue());
        }
        teamView.setText(teamOutput.toString());

        TextView paaLeft = (TextView)view.findViewById(R.id.base_textview_paa_left);
        paaLeft.setText(getPAALeft());
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
        return new StringBuilder("Value: ")
                .append(df.format(rankings.getDraft().getDraftValue()))
                .toString();
    }

    private String getTeamStr() {
        StringBuilder teamOutput = new StringBuilder();
        RosterSettings roster = rankings.getLeagueSettings().getRosterSettings();
        if (roster.isPositionValid(Constants.QB)) {
            teamOutput.append(Constants.QB)
                    .append("s: ")
                    .append(getPosString(rankings.getDraft().getMyQbs(), rankings.getDraft().getQBPAA()));
        }
        if (roster.isPositionValid(Constants.RB)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.RB)
                    .append("s: ")
                    .append(getPosString(rankings.getDraft().getMyRbs(), rankings.getDraft().getRBPAA()));
        }
        if (roster.isPositionValid(Constants.WR)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.WR)
                    .append("s: ")
                    .append(getPosString(rankings.getDraft().getMyWrs(), rankings.getDraft().getWRPAA()));
        }
        if (roster.isPositionValid(Constants.TE)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.TE)
                    .append("s: ")
                    .append(getPosString(rankings.getDraft().getMyTes(), rankings.getDraft().getTEPAA()));
        }
        if (roster.isPositionValid(Constants.DST)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.DST)
                    .append("s: ")
                    .append(getPosString(rankings.getDraft().getMyDsts(), rankings.getDraft().getDSTPAA()));
        }
        if (roster.isPositionValid(Constants.K)) {
            teamOutput.append(Constants.LINE_BREAK)
                    .append(Constants.K)
                    .append("s: ")
                    .append(getPosString(rankings.getDraft().getMyKs(), rankings.getDraft().getKPAA()));
        }
        return teamOutput.append(Constants.LINE_BREAK)
                .append("Total PAA: ")
                .append(df.format(rankings.getDraft().getTotalPAA()))
                .append(Constants.LINE_BREAK)
                .toString();
    }

    private String getPosString(List<Player> players, double posPAA) {
        if (players.size() == 0) {
            return "None";
        }
        StringBuilder posStr = new StringBuilder();
        for (Player player : players) {
            posStr.append(player.getName())
                    .append(", ");
        }
        String playerStr = posStr.toString();
        return new StringBuilder(playerStr.substring(0, playerStr.length() - 2))
                .append(" (PAA: ")
                .append(df.format(posPAA))
                .append(")")
                .toString();
    }

    private void displayPlayers() {
        View view = clearAndAddView(R.layout.content_draft_info_undraft);
        ListView listview = (ListView)view.findViewById(R.id.base_list);
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
        rankings.getDraft().unDraftPlayer(player);
        Toast.makeText(this, player.getName() + " undrafted", Toast.LENGTH_SHORT).show();
        LocalSettingsHelper.saveDraft(this, rankings.getDraft());
        displayPlayers();
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

    private View clearAndAddView(int viewId) {
        baseLayout.removeAllViews();
        View child = getLayoutInflater().inflate(viewId, null);
        baseLayout.addView(child);
        return child;
    }
}
