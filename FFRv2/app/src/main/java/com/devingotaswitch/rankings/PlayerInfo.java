package com.devingotaswitch.rankings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.sources.ParsePlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerInfo extends AppCompatActivity {

    private Rankings rankings;
    private Player player;
    private List<PlayerNews> playerNews;
    private RankingsDBWrapper rankingsDB;

    private List<Map<String, String>> data;
    private SimpleAdapter adapter;
    private ListView infoList;
    private MenuItem addWatch;
    private MenuItem removeWatch;
    private MenuItem draftMe;
    private MenuItem draftOther;
    private MenuItem undraft;

    private static DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankingsDB = new RankingsDBWrapper();
        rankings = Rankings.init();
        String playerId = getIntent().getStringExtra(Constants.PLAYER_ID);
        Player mostlyFleshedPlayer = rankings.getPlayer(playerId);
        player = rankingsDB.getPlayer(this, mostlyFleshedPlayer.getName(), mostlyFleshedPlayer.getPosition());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_player_info_menu, menu);
        addWatch = menu.findItem(R.id.player_info_add_watched);
        removeWatch = menu.findItem(R.id.player_info_remove_watched);
        draftMe = menu.findItem(R.id.player_info_draft_me);
        draftOther = menu.findItem(R.id.player_info_draft_someone);
        undraft = menu.findItem(R.id.player_info_undraft);
        hideMenuItemOnWatchStatus();
        hideMenuItemsOnDraftStatus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();
        switch(menuItem) {
            case R.id.player_info_add_watched:
                addWatched();
                return true;
            case R.id.player_info_remove_watched:
                removeWatched();
                return true;
            case R.id.player_info_draft_me:
                if (rankings.getLeagueSettings().isAuction()) {
                    getAuctionCost();
                } else {
                    draftByMe(0);
                }
                return true;
            case R.id.player_info_draft_someone:
                draftBySomeone();
                return true;
            case R.id.player_info_undraft:
                undraftPlayer();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void addWatched() {
        player.setWatched(true);
        Toast.makeText(this, player.getName() + " added to watch list", Toast.LENGTH_SHORT).show();
        rankingsDB.updatePlayerWatchedStatus(this, player);
        hideMenuItemOnWatchStatus();
    }

    private void removeWatched() {
        player.setWatched(false);
        Toast.makeText(this, player.getName() + " removed from watch list", Toast.LENGTH_SHORT).show();
        rankingsDB.updatePlayerWatchedStatus(this, player);
        hideMenuItemOnWatchStatus();
    }

    private void hideMenuItemOnWatchStatus() {
        if (player.isWatched()) {
            addWatch.setVisible(false);
            removeWatch.setVisible(true);
        } else {
            addWatch.setVisible(true);
            removeWatch.setVisible(false);
        }
    }

    private void getAuctionCost() {
        LayoutInflater li = LayoutInflater.from(this);
        View noteView = li.inflate(R.layout.user_input_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(noteView);
        final EditText userInput = (EditText) noteView
                .findViewById(R.id.user_input_popup_input);
        userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        userInput.setHint("Auction cost");

        TextView title = (TextView)noteView.findViewById(R.id.user_input_popup_title);
        title.setText("How much did " + player.getName() + " cost?");
        final Context localCopy = this;
        alertDialogBuilder
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String costStr = userInput.getText().toString();
                                if (StringUtils.isBlank(costStr) || !GeneralUtils.isInteger(costStr)) {
                                    Toast.makeText(localCopy, "Must provide a number for cost", Toast.LENGTH_SHORT).show();
                                } else {
                                    draftByMe(Integer.parseInt(costStr));
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void draftByMe(int cost) {
        rankings.getDraft().draftPlayer(player, true, cost);
        Toast.makeText(this, player.getName() + " drafted by you", Toast.LENGTH_SHORT).show();
        saveDraft();
        hideMenuItemsOnDraftStatus();
        displayRanks();
    }

    private void draftBySomeone() {
        rankings.getDraft().draftPlayer(player, false, 0);
        Toast.makeText(this, player.getName() + " marked as drafted", Toast.LENGTH_SHORT).show();
        saveDraft();
        hideMenuItemsOnDraftStatus();
        displayRanks();
    }

    private void undraftPlayer() {
        rankings.getDraft().unDraftPlayer(player);
        Toast.makeText(this, player.getName() + " undrafted", Toast.LENGTH_SHORT).show();
        saveDraft();
        hideMenuItemsOnDraftStatus();
    }

    private void saveDraft() {
        LocalSettingsHelper.saveDraft(this, rankings.getDraft());
    }

    private void hideMenuItemsOnDraftStatus() {
        if (rankings.getDraft().isDrafted(player)) {
            draftMe.setVisible(false);
            draftOther.setVisible(false);
            undraft.setVisible(true);
        } else {
            draftMe.setVisible(true);
            draftOther.setVisible(true);
            undraft.setVisible(false);
        }
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

        infoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView subView = (TextView)view.findViewById(R.id.player_info);
                String existing = ((TextView)view.findViewById(R.id.player_basic)).getText().toString();
                if (Constants.NOTE_SUB.equals(subView.getText().toString())) {
                    getNote(existing);
                }
            }
        });
        infoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView subView = (TextView)view.findViewById(R.id.player_info);
                if (Constants.NOTE_SUB.equals(subView.getText().toString())) {
                    setNoteAndDisplayIt("");
                }
                return true;
            }
        });

        displayRanks();
    }

    private void getNote(String existing) {
        LayoutInflater li = LayoutInflater.from(this);
        View noteView = li.inflate(R.layout.user_input_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(noteView);
        final EditText userInput = (EditText) noteView
                .findViewById(R.id.user_input_popup_input);
        userInput.setHint("Player note");
        if (!Constants.DEFAULT_NOTE.equals(existing)) {
            userInput.setText(existing);
        }

        TextView title = (TextView)noteView.findViewById(R.id.user_input_popup_title);
        title.setText("Input a note for " + player.getName());
        final Context localCopy = this;
        alertDialogBuilder
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String newNote = userInput.getText().toString();
                                if (StringUtils.isBlank(newNote)) {
                                    Toast.makeText(localCopy, "No note given", Toast.LENGTH_SHORT).show();
                                } else {
                                    setNoteAndDisplayIt(newNote);
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setNoteAndDisplayIt(String newNote) {
        player.setNote(newNote);
        rankingsDB.updatePlayerNote(this, player, newNote);
        displayInfo();
    }

    private void displayRanks() {
        data.clear();

        Map<String, String> ecr = new HashMap<>();
        ecr.put(Constants.PLAYER_BASIC, "ECR: " + player.getEcr());
        int ecrRank = getEcr(null, player.getEcr());
        int ecrRankPos = getEcr(player.getPosition(), player.getEcr());
        String ecrSub = getRankingSub(ecrRank, ecrRankPos);
        if (player.getRisk() != null) {
            ecrSub += Constants.LINE_BREAK + "Risk: " + player.getRisk();
        }
        ecr.put(Constants.PLAYER_INFO, ecrSub);
        data.add(ecr);

        Map<String, String> adp = new HashMap<>();
        adp.put(Constants.PLAYER_BASIC, "ADP: " + player.getAdp());
        int adpRank = getAdp(null, player.getAdp());
        int adpPos = getAdp(player.getPosition(), player.getAdp());
        adp.put(Constants.PLAYER_INFO, getRankingSub(adpRank, adpPos));
        data.add(adp);

        Map<String, String> auc = new HashMap<>();
        auc.put(Constants.PLAYER_BASIC, "Auction Value: $" + df.format(player.getAuctionValue()));
        int aucRank = getAuc(null, player.getAuctionValue());
        int aucPos = getAuc(player.getPosition(), player.getAuctionValue());
        auc.put(Constants.PLAYER_INFO, getRankingSub(aucRank, aucPos));
        data.add(auc);

        if (player.getProjection() != null) {
            Map<String, String> proj = new HashMap<>();
            proj.put(Constants.PLAYER_BASIC, "Projection: " + player.getProjection());
            int projRank = getProj(null, player.getProjection());
            int projPos = getProj(player.getPosition(), player.getProjection());
            proj.put(Constants.PLAYER_INFO, getRankingSub(projRank, projPos));
            data.add(proj);

            Map<String, String> paa = new HashMap<>();
            paa.put(Constants.PLAYER_BASIC, "PAA: " + df.format(player.getPaa()));
            int paaRank = getPaa(null, player.getPaa());
            int paaPos = getPaa(player.getPosition(), player.getPaa());
            paa.put(Constants.PLAYER_INFO, getRankingSub(paaRank, paaPos));
            data.add(paa);

            Map<String, String> xVal = new HashMap<>();
            xVal.put(Constants.PLAYER_BASIC, "X Value: " + df.format(player.getxVal()));
            int xValRank = getXVal(null, player.getxVal());
            int xValPos = getXVal(player.getPosition(), player.getxVal());
            xVal.put(Constants.PLAYER_INFO, getRankingSub(xValRank, xValPos));
            data.add(xVal);
        }

        adapter.notifyDataSetChanged();
    }

    private void displayInfo() {
        data.clear();

        Map<String, String> context = new HashMap<>();
        context.put(Constants.PLAYER_BASIC, "Current status");
        StringBuilder playerSub = new StringBuilder();
        if (player.isWatched()) {
            playerSub.append("In your watch list").append(Constants.LINE_BREAK);
        }
        if (rankings.getDraft().isDrafted(player)) {
            if (rankings.getDraft().isDraftedByMe(player)) {
                playerSub.append("On your team");
            } else {
                playerSub.append("On another team");
            }
        } else {
            playerSub.append("Currently available");
        }
        context.put(Constants.PLAYER_INFO, playerSub.toString());
        data.add(context);

        if (StringUtils.isBlank(player.getNote())) {
            Map<String, String> note = new HashMap<>();
            note.put(Constants.PLAYER_BASIC, Constants.DEFAULT_NOTE);
            note.put(Constants.PLAYER_INFO, Constants.NOTE_SUB);
            data.add(note);
        } else {
            Map<String, String> note = new HashMap<>();
            note.put(Constants.PLAYER_BASIC, player.getNote());
            note.put(Constants.PLAYER_INFO, Constants.NOTE_SUB);
            data.add(note);
        }

        if (!StringUtils.isBlank(player.getInjuryStatus())) {
            Map<String, String> injury = new HashMap<>();
            injury.put(Constants.PLAYER_BASIC, "Injury status");
            injury.put(Constants.PLAYER_INFO, player.getInjuryStatus());
            data.add(injury);
        }

        if (!StringUtils.isBlank(player.getStats())) {
            Map<String, String> stats = new HashMap<>();
            stats.put(Constants.PLAYER_BASIC, "2016 stats");
            stats.put(Constants.PLAYER_INFO, player.getStats());
            data.add(stats);
        }

        Team team = rankings.getTeam(player);
        if (team != null) {
            int sos = team.getSosForPosition(player.getPosition());
            if (sos > 0)  {
                Map<String, String> sosData = new HashMap<>();
                sosData.put(Constants.PLAYER_BASIC, "Positional SOS: " + sos);
                sosData.put(Constants.PLAYER_INFO, "1 is easiest, 32 is hardest");
                data.add(sosData);
            }
        }
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


    private String getRankingSub(int rank, int posRank) {
        return new StringBuilder("Ranked ")
                .append(posRank)
                .append(" positionally, ")
                .append(rank)
                .append(" overall")
                .toString();
    }

    private int getEcr(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getEcr() < source) {
                    rank++;
                }
            }
        }
        return rank;
    }

    private int getAdp(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getAdp() < source) {
                    rank++;
                }
            }
        }
        return rank;
    }

    private int getAuc(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getAuctionValue() > source) {
                    rank++;
                }
            }
        }
        return rank;
    }

    private int getProj(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getProjection() > source) {
                    rank++;
                }
            }
        }
        return rank;
    }

    private int getPaa(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getPaa() > source) {
                    rank++;
                }
            }
        }
        return rank;
    }

    private int getXVal(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getxVal() > source) {
                    rank++;
                }
            }
        }
        return rank;
    }

}
