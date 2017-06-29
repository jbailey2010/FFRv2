package com.devingotaswitch.rankings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.sources.ParsePlayerNews;
import com.devingotaswitch.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerInfo extends AppCompatActivity {

    private Rankings rankings;
    private Player player;
    private List<PlayerNews> playerNews;

    private List<Map<String, String>> data;
    private SimpleAdapter adapter;
    private ListView infoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankings = Rankings.init();
        String playerId = getIntent().getStringExtra(Constants.PLAYER_ID);
        player = rankings.getPlayer(playerId);

        // Set toolbar for this screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_player_info);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText(player.getName());
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

    private void init() {
        Button headerLeft = (Button)findViewById(R.id.dummy_btn_left);
        Button headerRight = (Button)findViewById(R.id.dummy_btn_right);
        Button headerMiddle = (Button)findViewById(R.id.dummy_btn_center);
        if (player.getAge() != null) {
            headerLeft.setText("Age:\n" + player.getAge());
        }
        if (rankings.getTeam(player) != null) {
            headerRight.setText("Bye:\n" + rankings.getTeam(player).getBye());
        }
        headerMiddle.setText(player.getTeamName() + Constants.LINE_BREAK + player.getPosition());

        // Kick off the thread to get news
        ParsePlayerNews.startNews(player.getName(), player.getTeamName(), this);

        infoList = (ListView)findViewById(R.id.player_info_list);
        data = new ArrayList<>();
        adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS },
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status });
        infoList.setAdapter(adapter);

        Button ranks = (Button)findViewById(R.id.player_info_ranks);
        Button info = (Button) findViewById(R.id.player_info_about);
        Button team = (Button) findViewById(R.id.player_info_team);
        Button news = (Button) findViewById(R.id.player_info_news);
        ranks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayRanks();
            }
        });
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayInfo();
            }
        });
        team.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTeam();
            }
        });
        news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayNews();
            }
        });
    }

    private void displayRanks() {
        data.clear();
        // TODO: populate this
        adapter.notifyDataSetChanged();
    }

    private void displayInfo() {
        data.clear();
        // TODO: populate this
        adapter.notifyDataSetChanged();
    }

    private void displayTeam() {
        data.clear();
        Team team = rankings.getTeam(player);
        if (team == null) {
            Map<String, String> datum = new HashMap<>();
            datum.put(Constants.PLAYER_BASIC, "No info available for this team.");
            datum.put(Constants.PLAYER_INFO, "Please try another player, or refresh your rankings.");
            data.add(datum);
        } else {

            Map<String, String> draft = new HashMap<>();
            draft.put(Constants.PLAYER_BASIC, "Draft recap");
            draft.put(Constants.PLAYER_INFO, team.getDraftClass());
            data.add(draft);

            Map<String, String> fa = new HashMap<>();
            fa.put(Constants.PLAYER_BASIC, "Free agency recap");
            fa.put(Constants.PLAYER_INFO, team.getFaClass());
            data.add(fa);

            Map<String, String> oline = new HashMap<>();
            oline.put(Constants.PLAYER_BASIC, "Offensive line grades");
            oline.put(Constants.PLAYER_INFO, team.getoLineRanks());
            data.add(oline);
        }
        adapter.notifyDataSetChanged();
    }

    private void displayNews() {
        data.clear();
        if (playerNews == null || playerNews.isEmpty()) {
            Map<String, String> news = new HashMap<>();
            news.put(Constants.PLAYER_BASIC, "No news available for this player");
            news.put(Constants.PLAYER_INFO, "Try a different player, or ensure you have a valid internet connection");
            data.add(news);
        } else {
            for (PlayerNews newsItem : playerNews) {
                Map<String, String> news = new HashMap<>();
                news.put(Constants.PLAYER_BASIC, newsItem.getNews());
                news.put(Constants.PLAYER_INFO, newsItem.getImpact());
                data.add(news);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void populateNews(List<PlayerNews> fetchedNews) {
        this.playerNews = fetchedNews;
    }
}
