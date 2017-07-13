package com.devingotaswitch.rankings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSorter extends AppCompatActivity {

    private Rankings rankings;
    private RankingsDBWrapper rankingsDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_sorter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();
        rankingsDB = new RankingsDBWrapper();

        // Set toolbar for this screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.player_sorter_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Sort Players");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*
        ECR
        ADP
        Underdrafted
        Auc
        Projection
        PAA
        PAAPD
        XVal
        Risk
        Positional SOS
        Tiers
         */
    }


    private void displayResults(List<String> orderedIds) {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);

        ListView listview = (ListView) findViewById(R.id.sort_players_output);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_TIER },
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status, R.id.player_tier });
        listview.setAdapter(adapter);
        for (String playerKey : orderedIds) {
            Player player = rankings.getPlayer(playerKey);
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition())) {
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
                String tierBase = "";
                if (!Constants.K.equals(player.getPosition()) && ! Constants.DST.equals(player.getPosition())) {
                    tierBase = "Tier " + player.getPositionalTier();
                }
                if (rankings.getDraft().isDrafted(player)) {
                    if (tierBase.length() > 3) {
                        tierBase += Constants.LINE_BREAK;
                    }
                    tierBase += "Drafted";
                }
                datum.put(Constants.PLAYER_TIER, tierBase);
                data.add(datum);
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
    }

    private void displayPlayerInfo(String playerKey) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, playerKey);
        startActivity(intent);
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
        return sub.toString();
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
}
