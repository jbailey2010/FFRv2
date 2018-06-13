package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.sources.ParseMath;
import com.devingotaswitch.rankings.sources.ParsePlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerInfo extends AppCompatActivity {
    private static final String TAG = "PlayerInfo";

    private Rankings rankings;
    private Player player;
    private List<PlayerNews> playerNews;
    private RankingsDBWrapper rankingsDB;

    private int viewCount = -1;
    private int watchCount= -1;
    private int draftCount = -1;

    private List<Map<String, String>> data;
    private SimpleAdapter adapter;
    private ListView infoList;
    private MenuItem addWatch;
    private MenuItem removeWatch;
    private MenuItem draftMe;
    private MenuItem draftOther;
    private MenuItem undraft;

    private static String playerId;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rankingsDB = new RankingsDBWrapper();
        rankings = Rankings.init();
        playerId = getIntent().getStringExtra(Constants.PLAYER_ID);
        Player mostlyFleshedPlayer = rankings.getPlayer(playerId);
        player = rankingsDB.getPlayer(this, mostlyFleshedPlayer.getName(), mostlyFleshedPlayer.getTeamName(), mostlyFleshedPlayer.getPosition());

        // Set toolbar for this screen
        Toolbar toolbar = findViewById(R.id.toolbar_player_info);
        toolbar.setTitle("");
        TextView main_title = findViewById(R.id.main_toolbar_title);
        main_title.setText(player.getName());
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                onBackPressed();
            }
        });

        init();
        AppSyncHelper.getOrCreatePlayerMetadataAndIncrementViewCount(this, player.getUniqueId());
    }

    @Override
    public void onResume() {
        super.onResume();

        Player mostlyFleshedPlayer = rankings.getPlayer(playerId);
        player = rankingsDB.getPlayer(this, mostlyFleshedPlayer.getName(), mostlyFleshedPlayer.getTeamName(), mostlyFleshedPlayer.getPosition());

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
        rankings.getPlayer(player.getUniqueId()).setWatched(true);
        final View.OnClickListener removeWatch = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeWatched();
            }
        };
        Snackbar.make(infoList, player.getName() + " added to watch list", Snackbar.LENGTH_LONG).setAction("Undo", removeWatch).show();
        rankingsDB.updatePlayerWatchedStatus(this, player);
        hideMenuItemOnWatchStatus();
    }

    private void removeWatched() {
        player.setWatched(false);
        rankings.getPlayer(player.getUniqueId()).setWatched(false);
        final View.OnClickListener addWatch = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWatched();
            }
        };
        Snackbar.make(infoList, player.getName() + " removed from watch list", Snackbar.LENGTH_LONG).setAction("Undo", addWatch).show();
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
        final EditText userInput =  noteView
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
                                    Snackbar.make(infoList, "Must provide a number for cost", Snackbar.LENGTH_SHORT).show();
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
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undraftPlayer();
            }
        };
        rankings.getDraft().draftByMe(rankings, player, this, cost, infoList, listener);
        hideMenuItemsOnDraftStatus();
        displayRanks();
    }

    private void draftBySomeone() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undraftPlayer();
            }
        };
        rankings.getDraft().draftBySomeone(rankings, player, this, infoList, listener);
        hideMenuItemsOnDraftStatus();
        displayRanks();
    }

    private void undraftPlayer() {
        rankings.getDraft().undraft(rankings, player, this, infoList);
        hideMenuItemsOnDraftStatus();
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
        Button headerLeft = findViewById(R.id.dummy_btn_left);
        Button headerRight = findViewById(R.id.dummy_btn_right);
        Button headerMiddle = findViewById(R.id.dummy_btn_center);
        if (player.getAge() != null) {
            headerLeft.setText("Age:" + Constants.LINE_BREAK +  player.getAge());
        }
        if (rankings.getTeam(player) != null) {
            headerRight.setText("Bye:" + Constants.LINE_BREAK + rankings.getTeam(player).getBye());
        }
        headerMiddle.setText(player.getTeamName() + Constants.LINE_BREAK + player.getPosition());

        // Kick off the thread to get news
        ParsePlayerNews.startNews(player.getName(), player.getTeamName(), this);

        infoList = findViewById(R.id.player_info_list);
        data = new ArrayList<>();
        adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS },
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status });
        infoList.setAdapter(adapter);

        ImageButton ranks = findViewById(R.id.player_info_ranks);
        ImageButton info =  findViewById(R.id.player_info_about);
        ImageButton team =  findViewById(R.id.player_info_team);
        ImageButton news =  findViewById(R.id.player_info_news);
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
                TextView subView = view.findViewById(R.id.player_info);
                String existing = ((TextView)view.findViewById(R.id.player_basic)).getText().toString();
                if (Constants.NOTE_SUB.equals(subView.getText().toString())) {
                    getNote(existing);
                }
            }
        });
        infoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView subView = view.findViewById(R.id.player_info);
                if (Constants.NOTE_SUB.equals(subView.getText().toString())) {
                    setNoteAndDisplayIt("");
                }
                return true;
            }
        });

        displayRanks();
    }

    public void setAggregatePlayerMetadata(int viewCount, int watchCount, int draftCount) {
        this.watchCount = watchCount;
        this.viewCount = viewCount;
        this.draftCount = draftCount;
    }

    private void getNote(String existing) {
        LayoutInflater li = LayoutInflater.from(this);
        View noteView = li.inflate(R.layout.user_input_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(noteView);
        final EditText userInput = noteView
                .findViewById(R.id.user_input_popup_input);
        userInput.setHint("Player note");
        if (!Constants.DEFAULT_NOTE.equals(existing)) {
            userInput.setText(existing);
        }

        TextView title = noteView.findViewById(R.id.user_input_popup_title);
        title.setText("Input a note for " + player.getName());
        alertDialogBuilder
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String newNote = userInput.getText().toString();
                                if (StringUtils.isBlank(newNote)) {
                                    Snackbar.make(infoList, "No note given", Snackbar.LENGTH_SHORT).show();
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

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View team = findViewById(R.id.team_info_button_selected);
        View news = findViewById(R.id.news_button_selected);
        ranks.setVisibility(View.VISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        team.setVisibility(View.INVISIBLE);
        news.setVisibility(View.INVISIBLE);

        Map<String, String> ecr = new HashMap<>();
        ecr.put(Constants.PLAYER_BASIC, "ECR: " + player.getEcr());
        int ecrRank = getEcr(null, player.getEcr());
        int ecrRankPos = getEcr(player.getPosition(), player.getEcr());
        String ecrSub = getRankingSub(ecrRank, ecrRankPos);
        if (!Constants.K.equals(player.getPosition()) && !Constants.DST.equals(player.getPosition())) {
            ecrSub += Constants.LINE_BREAK + "Positional Tier: " + player.getPositionalTier();
        }
        if (player.getRisk() != null) {
            ecrSub += Constants.LINE_BREAK + "Risk: " + player.getRisk();
        }
        ecr.put(Constants.PLAYER_INFO, ecrSub);
        data.add(ecr);

        Map<String, String> adp = new HashMap<>();
        adp.put(Constants.PLAYER_BASIC, "ADP: " + player.getAdp());
        int adpRank = getAdp(null, player.getAdp());
        int adpPos = getAdp(player.getPosition(), player.getAdp());
        StringBuilder adpSub = new StringBuilder(getRankingSub(adpRank, adpPos));
        int draftedPlayers = rankings.getDraft().getDraftedPlayers().size();
        if (draftedPlayers > 0) {
            adpSub.append(Constants.LINE_BREAK)
                    .append("Current draft position: ")
                    .append(draftedPlayers + 1);
        }
        adp.put(Constants.PLAYER_INFO, adpSub.toString());
        data.add(adp);

        Map<String, String> auc = new HashMap<>();
        auc.put(Constants.PLAYER_BASIC, "Auction Value: $" + df.format(player.getAuctionValue()));
        int aucRank = getAuc(null, player.getAuctionValue());
        int aucPos = getAuc(player.getPosition(), player.getAuctionValue());
        String auctionSub = getRankingSub(aucRank, aucPos) +
                Constants.LINE_BREAK +
                getLeverage();
        auc.put(Constants.PLAYER_INFO, auctionSub);
        data.add(auc);

        Map<String, String> dynasty = new HashMap<>();
        dynasty.put(Constants.PLAYER_BASIC, "Dynasty/Keeper Rankings: " + player.getDynastyRank());
        int dynRank = getDynasty(null, player.getDynastyRank());
        int dynRankPos = getDynasty(player.getPosition(), player.getDynastyRank());
        String dynSub = getRankingSub(dynRank, dynRankPos);
        dynasty.put(Constants.PLAYER_INFO, dynSub);
        data.add(dynasty);

        if (player.getRookieRank() < 300.0) {
            // 300.0 is the default, so this is basically 'is it set?'
            Map<String, String> rookie = new HashMap<>();
            rookie.put(Constants.PLAYER_BASIC, "Rookie Rankings: " + player.getRookieRank());
            int rookieRank = getRookie(null, player.getRookieRank());
            int rookieRankPos = getRookie(player.getPosition(), player.getRookieRank());
            String rookieSub = getRankingSub(rookieRank, rookieRankPos);
            rookie.put(Constants.PLAYER_INFO, rookieSub);
            data.add(rookie);
        }

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
            String subRank = getRankingSub(paaRank, paaPos);
            if (player.getAuctionValueCustom(rankings) > 0.0) {
                subRank += Constants.LINE_BREAK + "PAA/$: " + df.format(player.getPaa() / player.getAuctionValueCustom(rankings));
            }
            subRank += Constants.LINE_BREAK + "Scaled PAA: " + df.format(player.getScaledPAA(rankings));
            paa.put(Constants.PLAYER_INFO, subRank);
            data.add(paa);

            Map<String, String> xVal = new HashMap<>();
            xVal.put(Constants.PLAYER_BASIC, "X Value: " + df.format(player.getxVal()));
            int xValRank = getXVal(null, player.getxVal());
            int xValPos = getXVal(player.getPosition(), player.getxVal());
            String xValSub = getRankingSub(xValRank, xValPos);
            if (player.getAuctionValueCustom(rankings) > 0.0) {
                xValSub  += Constants.LINE_BREAK + "X Value/$: " + df.format(player.getxVal() / player.getAuctionValueCustom(rankings));
            }
            xValSub += Constants.LINE_BREAK + "Scaled X Value: " + df.format(player.getScaledXVal(rankings));
            xVal.put(Constants.PLAYER_INFO, xValSub);
            data.add(xVal);

            Map<String, String> voLS = new HashMap<>();
            voLS.put(Constants.PLAYER_BASIC, "VoLS: " + df.format(player.getvOLS()));
            int voLSRank = getVoLSRank(null, player.getvOLS());
            int voLSPos = getVoLSRank(player.getPosition(), player.getvOLS());
            String voLSSub = getRankingSub(voLSRank, voLSPos);
            if (player.getAuctionValueCustom(rankings) > 0.0) {
                voLSSub += Constants.LINE_BREAK + "VoLS/$: " + df.format(player.getvOLS() / player.getAuctionValueCustom(rankings));
            }
            voLSSub += Constants.LINE_BREAK + "Scaled VoLS: " + df.format(player.getScaledVoLS(rankings));
            voLS.put(Constants.PLAYER_INFO, voLSSub);
            data.add(voLS);
        }

        adapter.notifyDataSetChanged();
    }

    private void displayInfo() {
        data.clear();

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View teamInfo = findViewById(R.id.team_info_button_selected);
        View news = findViewById(R.id.news_button_selected);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.VISIBLE);
        teamInfo.setVisibility(View.INVISIBLE);
        news.setVisibility(View.INVISIBLE);

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
            playerSub.append("Available")
                    .append(Constants.LINE_BREAK);

            if (rankings.getDraft().getMyPlayers().size() > 0) {
                // TODO: should this print more specific info? Names?
                List<Player> sameBye = rankings.getDraft().getPlayersWithSameBye(player, rankings);
                if (sameBye.size() > 1 || sameBye.size() == 0) {
                    playerSub.append("Same bye as ")
                            .append(sameBye.size())
                            .append(" players on your team");
                } else {
                    playerSub.append("Same bye as ")
                            .append(sameBye.size())
                            .append(" player on your team");
                }
                if (sameBye.size() > 0) {
                    // No sense printing that it's the same as no players AND no <position>s
                    playerSub.append(Constants.LINE_BREAK);
                    List<Player> sameByeAndPos = rankings.getDraft().getPlayersWithSameByeAndPos(player, rankings);
                    if (sameByeAndPos.size() > 1) {
                        playerSub.append("Same bye as ")
                                .append(sameByeAndPos.size())
                                .append(" ")
                                .append(player.getPosition())
                                .append("s on your team");
                    } else {
                        playerSub.append("Same bye as ")
                                .append(sameByeAndPos.size())
                                .append(" ")
                                .append(player.getPosition())
                                .append(" on your team");
                    }
                }
            }
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
            stats.put(Constants.PLAYER_BASIC, Constants.LAST_YEAR_KEY + " stats");
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

        if (viewCount > 0) {
            Map<String, String> activityData = new HashMap<>();
            activityData.put(Constants.PLAYER_BASIC, "Player popularity");
            String activityString = "" +
                    viewCount +
                    (viewCount > 1 ? " views" : "view") +
                    Constants.LINE_BREAK +
                    "In " +
                    watchCount +
                    (watchCount == 1 ? " watch list" : " watch lists") +
                    Constants.LINE_BREAK +
                    "Drafted " +
                    draftCount +
                    (draftCount == 1 ? " time" : " times");
            activityData.put(Constants.PLAYER_INFO, activityString);
            data.add(activityData);
        }
        adapter.notifyDataSetChanged();
    }

    private void displayTeam() {
        data.clear();

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View teamInfo = findViewById(R.id.team_info_button_selected);
        View news = findViewById(R.id.news_button_selected);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        teamInfo.setVisibility(View.VISIBLE);
        news.setVisibility(View.INVISIBLE);

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

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View team = findViewById(R.id.team_info_button_selected);
        View newsInfo = findViewById(R.id.news_button_selected);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        team.setVisibility(View.INVISIBLE);
        newsInfo.setVisibility(View.VISIBLE);

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

    private String getLeverage() {
        return "Leverage: " +
                ParseMath.getLeverage(player, rankings);
    }

    private String getRankingSub(int rank, int posRank) {
        return "Ranked " +
                posRank +
                " positionally, " +
                rank +
                " overall";
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

    private int getDynasty(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getDynastyRank() < source) {
                    rank++;
                }
            }
        }
        return rank;
    }

    private int getRookie(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getRookieRank() < source) {
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

    private int getVoLSRank(String pos, double source) {
        int rank = 1;
        for (String key : rankings.getPlayers().keySet()) {
            Player player = rankings.getPlayer(key);
            if (pos == null || pos.equals(player.getPosition())) {
                if (player.getvOLS() > source) {
                    rank++;
                }
            }
        }
        return rank;
    }
}
