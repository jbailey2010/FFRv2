package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;
import com.adroitandroid.chipcloud.FlowLayout;
import com.amazonaws.util.StringUtils;
import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.appsync.comments.Comment;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.PlayerNews;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.domain.appsync.tags.Tag;
import com.devingotaswitch.rankings.extras.CommentAdapter;
import com.devingotaswitch.rankings.extras.PlayerInfoSwipeDetector;
import com.devingotaswitch.rankings.sources.ParseMath;
import com.devingotaswitch.rankings.sources.ParsePlayerNews;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.devingotaswitch.youruserpools.CUPHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
    private boolean sortByUpvotes = false;

    private List<Map<String, String>> data;
    private SimpleAdapter adapter;
    private List<Map<String, String>> commentData;
    private CommentAdapter commentAdapter;
    private ListView infoList;
    private ListView commentList;
    private MenuItem addWatch;
    private MenuItem removeWatch;
    private MenuItem draftMe;
    private MenuItem draftOther;
    private MenuItem undraft;
    private MenuItem commentSortDate;
    private MenuItem commentSortTop;
    private ChipCloud chipCloud;

    private static String playerId;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    private List<Comment> comments = new ArrayList<>();

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

        sortByUpvotes = Constants.COMMENT_SORT_TOP.equals(LocalSettingsHelper.getCommentSortType(this));

        // Kick off the thread to get news
        ParsePlayerNews.startNews(player.getName(), player.getTeamName(), this);

        // Kick off the thread to get comments
        AppSyncHelper.getCommentsForPlayer(this, player.getUniqueId(), null, sortByUpvotes);

        // Kick off the thread to get player metadata
        AppSyncHelper.getOrCreatePlayerMetadataAndIncrementViewCount(this, player.getUniqueId());
    }

    @Override
    public void onResume() {
        super.onResume();

        Player mostlyFleshedPlayer = rankings.getPlayer(playerId);
        player = rankingsDB.getPlayer(this, mostlyFleshedPlayer.getName(), mostlyFleshedPlayer.getTeamName(), mostlyFleshedPlayer.getPosition());

        try {
            init();
        } catch (Exception e) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e);
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            onBackPressed();
        }
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
        commentSortDate = menu.findItem(R.id.player_info_sort_comments_date);
        commentSortTop = menu.findItem(R.id.player_info_sort_comments_top);
        hideMenuItemOnWatchStatus();
        hideMenuItemsOnDraftStatus();
        hideMenuItemsOnCommentSort();
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
            case R.id.player_info_compare:
                comparePlayer();
                return true;
            case R.id.player_info_simulate_adp:
                simulateAdp();
                return true;
            case R.id.player_info_sort_comments_date:
                sortCommentsByDate();
                return true;
            case R.id.player_info_sort_comments_top:
                sortCommentsByUpvotes();
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

    public void swipeLeftToRight() {
        if (View.VISIBLE == findViewById(R.id.ranks_button_selected).getVisibility()) {
            displayComments();
        } else if (View.VISIBLE == findViewById(R.id.player_info_button_selected).getVisibility()) {
            displayRanks();
        } else if (View.VISIBLE == findViewById(R.id.team_info_button_selected).getVisibility()) {
            displayInfo();
        } else if (View.VISIBLE == findViewById(R.id.news_button_selected).getVisibility()) {
            displayTeam();
        } else if (View.VISIBLE == findViewById(R.id.comment_button_selected).getVisibility()) {
            displayNews();
        }
    }

    public void swipeRightToLeft() {
        if (View.VISIBLE == findViewById(R.id.ranks_button_selected).getVisibility()) {
            displayInfo();
        } else if (View.VISIBLE == findViewById(R.id.player_info_button_selected).getVisibility()) {
            displayTeam();
        } else if (View.VISIBLE == findViewById(R.id.team_info_button_selected).getVisibility()) {
            displayNews();
        } else if (View.VISIBLE == findViewById(R.id.news_button_selected).getVisibility()) {
            displayComments();
        } else if (View.VISIBLE == findViewById(R.id.comment_button_selected).getVisibility()) {
            displayRanks();
        }
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

    private void sortCommentsByDate() {
        comments.clear();
        commentData.clear();
        sortByUpvotes = false;
        AppSyncHelper.getCommentsForPlayer(this, player.getUniqueId(), null, sortByUpvotes);
        LocalSettingsHelper.saveCommentSortType(this, Constants.COMMENT_SORT_DATE);
        hideMenuItemsOnCommentSort();
    }

    private void sortCommentsByUpvotes() {
        comments.clear();
        commentData.clear();
        sortByUpvotes = true;
        AppSyncHelper.getCommentsForPlayer(this, player.getUniqueId(), null, true);
        LocalSettingsHelper.saveCommentSortType(this, Constants.COMMENT_SORT_TOP);
        hideMenuItemsOnCommentSort();
    }

    private void hideMenuItemsOnCommentSort() {
        if (sortByUpvotes) {
            commentSortDate.setVisible(true);
            commentSortTop.setVisible(false);
        } else {
            commentSortDate.setVisible(false);
            commentSortTop.setVisible(true);
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

    private void comparePlayer() {
        Intent intent = new Intent(this, PlayerComparator.class);
        intent.putExtra(Constants.PLAYER_ID, player.getUniqueId());
        startActivity(intent);
    }

    private void simulateAdp() {
        Intent intent = new Intent(this, ADPSimulator.class);
        intent.putExtra(Constants.PLAYER_ID, player.getUniqueId());
        startActivity(intent);
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
        if (player.getAge() != null && player.getAge() > 0 && !Constants.DST.equals(player.getPosition()))  {
            StringBuilder expBuilder = new StringBuilder()
                    .append("Age: ")
                    .append(player.getAge());
            if (player.getExperience() >= 0) {
                expBuilder.append(Constants.LINE_BREAK)
                        .append("Exp: ")
                        .append(player.getExperience());
            }
            headerLeft.setText(expBuilder.toString());
        } else if (Constants.DST.equals(player.getPosition())) {
            headerLeft.setText("Age: N/A");
        }
        if (rankings.getTeam(player) != null) {
            headerRight.setText("Bye:" + Constants.LINE_BREAK + rankings.getTeam(player).getBye());
        }
        headerMiddle.setText(player.getTeamName() + Constants.LINE_BREAK + player.getPosition());

        infoList = findViewById(R.id.player_info_list);
        commentList = findViewById(R.id.player_info_comment_list);
        data = new ArrayList<>();
        commentData = new ArrayList<>();
        adapter = new SimpleAdapter(this, data,
                R.layout.list_item_player_info_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO},
                new int[] { R.id.player_basic, R.id.player_info});
        commentAdapter = new CommentAdapter(this, commentData,
                R.layout.list_item_comment_layout,
                new String[] {Constants.COMMENT_AUTHOR, Constants.COMMENT_CONTENT, Constants.COMMENT_TIMESTAMP, Constants.COMMENT_ID,
                Constants.COMMENT_UPVOTE_IMAGE, Constants.COMMENT_UPVOTE_COUNT, Constants.COMMENT_DOWNVOTE_IMAGE, Constants.COMMENT_DOWNVOTE_COUNT},
                new int[] { R.id.comment_author, R.id.comment_content, R.id.comment_timestamp, R.id.comment_id, R.id.comment_upvoted_icon,
                R.id.comment_upvote_count, R.id.comment_downvoted_icon, R.id.comment_downvote_count});
        infoList.setAdapter(adapter);
        commentList.setAdapter(commentAdapter);
        PlayerInfoSwipeDetector detector = new PlayerInfoSwipeDetector(this);
        infoList.setOnTouchListener(detector);
        commentList.setOnTouchListener(detector);
        RelativeLayout footerView = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tag_footer_view, null);
        chipCloud = footerView.findViewById(R.id.chip_cloud);
        infoList.addFooterView(footerView);

        ImageButton ranks = findViewById(R.id.player_info_ranks);
        ImageButton info =  findViewById(R.id.player_info_about);
        ImageButton team =  findViewById(R.id.player_info_team);
        ImageButton news =  findViewById(R.id.player_info_news);
        ImageButton comments = findViewById(R.id.player_info_comments);
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
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayComments();
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
        commentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String author = ((TextView)view.findViewById(R.id.comment_author)).getText().toString();
                if (CUPHelper.getCurrUser().equals(author)) {
                    String id = ((TextView)view.findViewById(R.id.comment_id)).getText().toString();
                    confirmCommentDeletion(id);
                }
                return true;
            }
        });

        displayRanks();
    }

    public void setAggregatePlayerMetadata(int viewCount, int watchCount, int draftCount, List<Tag> tags) {
        this.watchCount = watchCount;
        this.viewCount = viewCount;
        this.draftCount = draftCount;

        setTags(tags);
    }

    public void setTags(final List<Tag> tags) {
        String[] tagArr = new String[tags.size()];
        List<Integer> taggedIndices = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            tagArr[i] = tag.getTagText();
            if (LocalSettingsHelper.isPlayerTagged(this, playerId, tag.getTitle())) {
                taggedIndices.add(i);
            }
        }

        new ChipCloud.Configure()
                .chipCloud(chipCloud)
                .selectedColor(Color.parseColor("#329AD6"))
                .selectedFontColor(Color.parseColor("#ffffff"))
                .deselectedColor(Color.parseColor("#e1e1e1"))
                .deselectedFontColor(Color.parseColor("#333333"))
                .selectTransitionMS(500)
                .deselectTransitionMS(250)
                .labels(tagArr)
                .mode(ChipCloud.Mode.MULTI)
                .allCaps(false)
                .gravity(ChipCloud.Gravity.STAGGERED)
                .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
                .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
                .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                .chipListener(new ChipListener() {
                    @Override
                    public void chipSelected(int index) {
                        String text = tags.get(index).getTitle();
                        if (!LocalSettingsHelper.isPlayerTagged(getApplication(), playerId, text)) {
                            LocalSettingsHelper.tagPlayer(getApplication(), playerId, text);
                        }
                        //...
                    }
                    @Override
                    public void chipDeselected(int index) {
                        String text = tags.get(index).getTitle();
                        if (LocalSettingsHelper.isPlayerTagged(getApplication(), playerId, text)) {
                            LocalSettingsHelper.untagPlayer(getApplication(), playerId, text);
                        }
                        //...
                    }
                })
                .build();

        for (Integer index : taggedIndices) {
            chipCloud.setSelectedChip(index);
        }
    }

    private void confirmCommentDeletion(final String commentId) {
        LayoutInflater li = LayoutInflater.from(this);
        View noteView = li.inflate(R.layout.user_input_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(noteView);
        final EditText userInput = noteView
                .findViewById(R.id.user_input_popup_input);
        userInput.setVisibility(View.GONE);

        TextView title = noteView.findViewById(R.id.user_input_popup_title);
        title.setText("Are you sure you want to delete this comment?");
        final Activity activity = this;
        alertDialogBuilder
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                AppSyncHelper.deleteComment(activity, commentId);
                                hideComment(commentId);
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void hideComment(String commentId) {
        for (Map<String, String> datum : commentData) {
            if (datum.get(Constants.COMMENT_ID).equals(commentId)) {
                commentData.remove(datum);
                commentAdapter.notifyDataSetChanged();
                break;
            }
        }
        Iterator<Comment> iterator = comments.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(commentId)) {
                iterator.remove();
            }
        }

        if (commentData.size() == 0) {
            displayComments();
        }
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
        commentData.clear();
        commentList.setVisibility(View.GONE);
        infoList.setVisibility(View.VISIBLE);
        chipCloud.setVisibility(View.GONE);

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View team = findViewById(R.id.team_info_button_selected);
        View news = findViewById(R.id.news_button_selected);
        View comments = findViewById(R.id.comment_button_selected);
        findViewById(R.id.comment_input_base).setVisibility(View.GONE);
        ranks.setVisibility(View.VISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        team.setVisibility(View.INVISIBLE);
        news.setVisibility(View.INVISIBLE);
        comments.setVisibility(View.INVISIBLE);

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
        auc.put(Constants.PLAYER_BASIC, "Auction Value: $" + df.format(player.getAuctionValueCustom(rankings)));
        int aucRank = getAuc(null, player.getAuctionValue());
        int aucPos = getAuc(player.getPosition(), player.getAuctionValue());
        String auctionSub = getRankingSub(aucRank, aucPos) +
                Constants.LINE_BREAK +
                getLeverage();
        auc.put(Constants.PLAYER_INFO, auctionSub);
        data.add(auc);

        Map<String, String> dynasty = new HashMap<>();
        dynasty.put(Constants.PLAYER_BASIC, "Dynasty/Keeper Ranking: " + player.getDynastyRank());
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

        if (!StringUtils.isBlank(player.getStats())) {
            Map<String, String> stats = new HashMap<>();
            stats.put(Constants.PLAYER_BASIC, Constants.LAST_YEAR_KEY + " stats");
            stats.put(Constants.PLAYER_INFO, player.getStats());
            data.add(stats);
        }

        adapter.notifyDataSetChanged();
    }

    private void displayInfo() {
        data.clear();
        commentData.clear();
        commentList.setVisibility(View.GONE);
        infoList.setVisibility(View.VISIBLE);
        chipCloud.setVisibility(View.VISIBLE);

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View teamInfo = findViewById(R.id.team_info_button_selected);
        View news = findViewById(R.id.news_button_selected);
        View comments = findViewById(R.id.comment_button_selected);
        findViewById(R.id.comment_input_base).setVisibility(View.GONE);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.VISIBLE);
        teamInfo.setVisibility(View.INVISIBLE);
        news.setVisibility(View.INVISIBLE);
        comments.setVisibility(View.INVISIBLE);

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
        String playerStatus = playerSub.toString();
        if (playerStatus.endsWith(Constants.LINE_BREAK)) {
            playerStatus = playerStatus.substring(0, playerStatus.length() - 1);
        }
        context.put(Constants.PLAYER_INFO, playerStatus);
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

        Map<String, String> injury = new HashMap<>();
        injury.put(Constants.PLAYER_BASIC, "Injury status");
        if (!StringUtils.isBlank(player.getInjuryStatus())) {
            injury.put(Constants.PLAYER_INFO, player.getInjuryStatus());
        } else {
            injury.put(Constants.PLAYER_INFO, "Healthy");
        }
        data.add(injury);

        if (viewCount > 0) {
            Map<String, String> activityData = new HashMap<>();
            activityData.put(Constants.PLAYER_BASIC, "Player popularity");
            String activityString = "" +
                    viewCount +
                    (viewCount > 1 ? " views" : " view") +
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
        commentData.clear();
        commentList.setVisibility(View.GONE);
        infoList.setVisibility(View.VISIBLE);
        chipCloud.setVisibility(View.GONE);

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View teamInfo = findViewById(R.id.team_info_button_selected);
        View news = findViewById(R.id.news_button_selected);
        View comments = findViewById(R.id.comment_button_selected);
        findViewById(R.id.comment_input_base).setVisibility(View.GONE);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        teamInfo.setVisibility(View.VISIBLE);
        news.setVisibility(View.INVISIBLE);
        comments.setVisibility(View.INVISIBLE);

        Team team = rankings.getTeam(player);
        if (team == null || Constants.NO_TEAM.equals(player.getTeamName())) {
            Map<String, String> datum = new HashMap<>();
            datum.put(Constants.PLAYER_BASIC, "No info available for this team.");
            datum.put(Constants.PLAYER_INFO, "Please try another player, or refresh your rankings.");
            data.add(datum);
        } else {

            if (!StringUtils.isBlank(team.getDraftClass()) && team.getDraftClass().length() > 4) {
                Map<String, String> draft = new HashMap<>();
                draft.put(Constants.PLAYER_BASIC, "Draft recap");
                draft.put(Constants.PLAYER_INFO, team.getDraftClass());
                data.add(draft);
            } else {
                Log.d(TAG, "No draft class to display");
            }

            if (!StringUtils.isBlank(team.getFaClass()) && team.getFaClass().length() > 4) {
                Map<String, String> fa = new HashMap<>();
                fa.put(Constants.PLAYER_BASIC, "Free agency recap");
                fa.put(Constants.PLAYER_INFO, team.getFaClass());
                data.add(fa);
            } else {
                Log.d(TAG, "No FA class to display");
            }

            if (!StringUtils.isBlank(team.getoLineRanks()) && team.getoLineRanks().length() > 4) {
                Map<String, String> oline = new HashMap<>();
                oline.put(Constants.PLAYER_BASIC, "Offensive line grades");
                oline.put(Constants.PLAYER_INFO, team.getoLineRanks());
                data.add(oline);
            } else {
                Log.d(TAG, "No oline ranks to display");
            }

            Map<String, String> schedule = new HashMap<>();
            schedule.put(Constants.PLAYER_BASIC, Constants.YEAR_KEY + " schedule");
            schedule.put(Constants.PLAYER_INFO, team.getSchedule() + Constants.LINE_BREAK + Constants.LINE_BREAK
                    + "Positional SOS: " + team.getSosForPosition(player.getPosition()) + Constants.LINE_BREAK
                    + "1 is easiest, 32 hardest");
            data.add(schedule);
        }
        adapter.notifyDataSetChanged();
    }

    private void displayNews() {
        data.clear();
        commentData.clear();
        commentList.setVisibility(View.GONE);
        infoList.setVisibility(View.VISIBLE);
        chipCloud.setVisibility(View.GONE);

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View team = findViewById(R.id.team_info_button_selected);
        View newsInfo = findViewById(R.id.news_button_selected);
        View comments = findViewById(R.id.comment_button_selected);
        findViewById(R.id.comment_input_base).setVisibility(View.GONE);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        team.setVisibility(View.INVISIBLE);
        newsInfo.setVisibility(View.VISIBLE);
        comments.setVisibility(View.INVISIBLE);

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

    private void displayComments() {
        data.clear();
        commentData.clear();
        commentList.setVisibility(View.VISIBLE);
        infoList.setVisibility(View.GONE);
        chipCloud.setVisibility(View.GONE);

        View ranks = findViewById(R.id.ranks_button_selected);
        View playerSelected = findViewById(R.id.player_info_button_selected);
        View team = findViewById(R.id.team_info_button_selected);
        View newsInfo = findViewById(R.id.news_button_selected);
        View commentsView = findViewById(R.id.comment_button_selected);
        findViewById(R.id.comment_input_base).setVisibility(View.VISIBLE);
        ranks.setVisibility(View.INVISIBLE);
        playerSelected.setVisibility(View.INVISIBLE);
        team.setVisibility(View.INVISIBLE);
        newsInfo.setVisibility(View.INVISIBLE);
        commentsView.setVisibility(View.VISIBLE);

        for (Comment comment : comments) {
            Map<String, String> commentMap = new HashMap<>();
            commentMap.put(Constants.COMMENT_AUTHOR, comment.getAuthor());
            commentMap.put(Constants.COMMENT_CONTENT, comment.getContent());
            commentMap.put(Constants.COMMENT_TIMESTAMP, comment.getTime());
            commentMap.put(Constants.COMMENT_ID, comment.getId());
            commentMap.put(Constants.COMMENT_UPVOTE_COUNT, String.valueOf(comment.getUpvotes()));
            commentMap.put(Constants.COMMENT_DOWNVOTE_COUNT, String.valueOf(comment.getDownvotes()));
            if (LocalSettingsHelper.isPostUpvoted(this, comment.getId())) {
                commentMap.put(Constants.COMMENT_UPVOTE_IMAGE, Integer.toString(R.drawable.upvoted));
                commentMap.put(Constants.COMMENT_DOWNVOTE_IMAGE, Integer.toString(R.drawable.not_downvoted));
            } else if (!LocalSettingsHelper.isPostDownvoted(this, comment.getId())
                    && comment.getAuthor().equals(CUPHelper.getCurrUser())) {
                LocalSettingsHelper.upvotePost(this, comment.getId());
                commentMap.put(Constants.COMMENT_UPVOTE_IMAGE, Integer.toString(R.drawable.upvoted));
                commentMap.put(Constants.COMMENT_DOWNVOTE_IMAGE, Integer.toString(R.drawable.not_downvoted));
            } else if (LocalSettingsHelper.isPostDownvoted(this, comment.getId())) {
                commentMap.put(Constants.COMMENT_UPVOTE_IMAGE, Integer.toString(R.drawable.not_upvoted));
                commentMap.put(Constants.COMMENT_DOWNVOTE_IMAGE, Integer.toString(R.drawable.downvoted));
            } else {
                commentMap.put(Constants.COMMENT_UPVOTE_IMAGE, Integer.toString(R.drawable.not_upvoted));
                commentMap.put(Constants.COMMENT_DOWNVOTE_IMAGE, Integer.toString(R.drawable.not_downvoted));
            }

            commentData.add(commentMap);
        }
        if (comments.size() == 0) {
            Map<String, String> emptyMap = new HashMap<>();
            emptyMap.put(Constants.COMMENT_CONTENT, "No comments exist for this player. Be the first to post!");
            commentData.add(emptyMap);
        }

        final EditText input = findViewById(R.id.player_info_comment_input);
        final ImageButton submit = findViewById(R.id.player_info_comment_submit);
        final Activity activity = this;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentContent = input.getText().toString();
                if (!StringUtils.isBlank(commentContent)) {
                    input.setText("");
                    AppSyncHelper.createComment(activity, commentContent, player.getUniqueId());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        });

        commentAdapter.notifyDataSetChanged();
    }

    public void conditionallyUpvoteComment(String commentId) {
        if (!LocalSettingsHelper.isPostUpvoted(this, commentId)) {
            for (Map<String, String> datum : commentData) {
                if (datum.get(Constants.COMMENT_ID).equals(commentId)) {
                    datum.put(Constants.COMMENT_UPVOTE_IMAGE, Integer.toString(R.drawable.upvoted));
                    datum.put(Constants.COMMENT_DOWNVOTE_IMAGE, Integer.toString(R.drawable.not_downvoted));
                    if (LocalSettingsHelper.isPostDownvoted(this, commentId)) {
                        int downvotes = Integer.parseInt(datum.get(Constants.COMMENT_DOWNVOTE_COUNT));
                        datum.put(Constants.COMMENT_DOWNVOTE_COUNT, String.valueOf(--downvotes));
                    }
                    int upvotes = Integer.parseInt(datum.get(Constants.COMMENT_UPVOTE_COUNT));
                    datum.put(Constants.COMMENT_UPVOTE_COUNT, String.valueOf(++upvotes));
                    commentAdapter.notifyDataSetChanged();
                    break;
                }
            }
            AppSyncHelper.upvoteComment(this, commentId, LocalSettingsHelper.isPostDownvoted(this, commentId));
            LocalSettingsHelper.upvotePost(this, commentId);
        }
    }

    public void conditionallyDownvoteComment(String commentId) {
        if (!LocalSettingsHelper.isPostDownvoted(this, commentId)) {
            for (Map<String, String> datum : commentData) {
                if (datum.get(Constants.COMMENT_ID).equals(commentId)) {
                    datum.put(Constants.COMMENT_UPVOTE_IMAGE, Integer.toString(R.drawable.not_upvoted));
                    datum.put(Constants.COMMENT_DOWNVOTE_IMAGE, Integer.toString(R.drawable.downvoted));
                    if (LocalSettingsHelper.isPostUpvoted(this, commentId)) {
                        int upvotes = Integer.parseInt(datum.get(Constants.COMMENT_UPVOTE_COUNT));
                        datum.put(Constants.COMMENT_UPVOTE_COUNT, String.valueOf(--upvotes));
                    }
                    int downvotes = Integer.parseInt(datum.get(Constants.COMMENT_DOWNVOTE_COUNT));
                    datum.put(Constants.COMMENT_DOWNVOTE_COUNT, String.valueOf(++downvotes));
                    commentAdapter.notifyDataSetChanged();
                    break;
                }
            }
            AppSyncHelper.downvoteComment(this, commentId, LocalSettingsHelper.isPostUpvoted(this, commentId));
            LocalSettingsHelper.downvotePost(this, commentId);
        }

    }

    public void updateVoteCount(String commentId, int upvotes, int downvotes) {
        for (Map<String, String> comment : commentData) {
            if (comment.get(Constants.COMMENT_ID).equals(commentId)) {
                comment.put(Constants.COMMENT_UPVOTE_COUNT, String.valueOf(upvotes));
                comment.put(Constants.COMMENT_DOWNVOTE_COUNT, String.valueOf(downvotes));
                commentAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void populateNews(List<PlayerNews> fetchedNews) {
        this.playerNews = fetchedNews;
        View newsView = findViewById(R.id.news_button_selected);
        if (View.VISIBLE == newsView.getVisibility()) {
            displayNews();
        }
    }

    public void addComments(Collection<Comment> comments, String nextToken) {
        this.comments.addAll(comments);
        if (sortByUpvotes) {
            Collections.sort(this.comments,new Comparator<Comment>() {
                @Override
                public int compare(Comment a, Comment b) {
                    return b.getUpvotes().compareTo(a.getUpvotes());
                }
            });
        }
        View commentsView = findViewById(R.id.comment_button_selected);
        if (View.VISIBLE == commentsView.getVisibility()) {
            displayComments();
        }

        if (!StringUtils.isBlank(nextToken)) {
            AppSyncHelper.getCommentsForPlayer(this, player.getUniqueId(), nextToken, sortByUpvotes);
        }
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
