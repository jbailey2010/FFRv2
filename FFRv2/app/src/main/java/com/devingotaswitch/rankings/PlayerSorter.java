package com.devingotaswitch.rankings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.RosterSettings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.rankings.extras.MultiSelectionSpinner;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerSorter extends AppCompatActivity {

    private Rankings rankings;
    private RankingsDBWrapper rankingsDB;

    private MenuItem graphItem;
    private final List<Player> players = new ArrayList<>();
    private String factor = null;
    private int sortMax;

    private int posIndex = 0;
    private int sortIndex = 0;
    private Set<String> factorStrings = new HashSet<>(Collections.singletonList(Constants.SORT_DEFAULT_STRING));

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
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                onBackPressed();
            }
        });

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_sort_menu, menu);
        graphItem = menu.findItem(R.id.graph_sort);
        graphItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();
        switch(menuItem) {
            case R.id.graph_sort:
                graphSort();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        init();
    }

    private void init() {
        final Spinner positions = (Spinner)findViewById(R.id.sort_players_position);
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

        final Spinner factors = (Spinner)findViewById(R.id.sort_players_factor);
        List<String> factorList = new ArrayList<>();
        factorList.add(Constants.SORT_ALL);
        factorList.add(Constants.SORT_ECR);
        factorList.add(Constants.SORT_ADP);
        factorList.add(Constants.SORT_UNDERDRAFTED);
        factorList.add(Constants.SORT_OVERDRAFTED);
        factorList.add(Constants.SORT_AUCTION);
        factorList.add(Constants.SORT_PROJECTION);
        factorList.add(Constants.SORT_PAA);
        factorList.add(Constants.SORT_PAA_SCALED);
        factorList.add(Constants.SORT_PAAPD);
        factorList.add(Constants.SORT_XVAL);
        factorList.add(Constants.SORT_XVAL_SCALED);
        factorList.add(Constants.SORT_XVALPD);
        factorList.add(Constants.SORT_VOLS);
        factorList.add(Constants.SORT_VOLS_SCALED);
        factorList.add(Constants.SORT_VOLSPD);
        factorList.add(Constants.SORT_RISK);
        factorList.add(Constants.SORT_SOS);
        factorList.add(Constants.SORT_TIERS);
        ArrayAdapter<String> factorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, factorList);
        factors.setAdapter(factorAdapter);

        final MultiSelectionSpinner spinner=(MultiSelectionSpinner)findViewById(R.id.sort_players_additional_factors);
        List<String> list = new ArrayList<>();
        list.add(Constants.SORT_HIDE_DRAFTED);
        list.add(Constants.SORT_ONLY_HEALTHY);
        list.add(Constants.SORT_EASY_SOS);
        list.add(Constants.SORT_ONLY_WATCHED);
        list.add(Constants.SORT_UNDER_30);
        list.add(Constants.SORT_IGNORE_LATE);
        spinner.setItems(list, Constants.SORT_DEFAULT_STRING);

        positions.setSelection(posIndex);
        factors.setSelection(sortIndex);
        spinner.setSelection(new ArrayList<>(factorStrings));

        final CheckBox reverse = (CheckBox)findViewById(R.id.sort_players_reverse);

        final EditText numberShown = (EditText) findViewById(R.id.sort_players_number_shown);

        Button submit = (Button)findViewById(R.id.sort_players_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPosition = ((TextView)positions.getSelectedView()).getText().toString();
                List<String> filteredIds = new ArrayList<>(rankings.getOrderedIds());
                if (!Constants.ALL_POSITIONS.equals(currentPosition)) {
                    filteredIds = rankings.getPlayersByPosition(filteredIds, currentPosition);
                }
                factor = ((TextView)factors.getSelectedView()).getText().toString();
                posIndex = positions.getSelectedItemPosition();
                sortIndex = factors.getSelectedItemPosition();
                factorStrings = spinner.getSelectedStrings();

                String numberShownStr = numberShown.getText().toString();
                sortMax = 1000;
                if (!StringUtils.isBlank(numberShownStr) && GeneralUtils.isInteger(numberShownStr)) {
                    sortMax = Integer.parseInt(numberShownStr);
                }
                sortPlayers(filteredIds, spinner.getSelectedStrings(), reverse.isChecked());
                graphItem.setVisible(true);
            }
        });
    }

    private void sortPlayers(List<String> playerIds, Set<String> booleanFactors, boolean reversePlayers) {
        Comparator<Player> comparator = null;
        if (Constants.SORT_ECR.equals(factor)) {
            comparator = getECRComparator();
        } else if (Constants.SORT_ADP.equals(factor)) {
            comparator = getADPComparator();
        } else if (Constants.SORT_UNDERDRAFTED.equals(factor)) {
            comparator = getUnderdraftedComparator();
        } else if (Constants.SORT_OVERDRAFTED.equals(factor)) {
            comparator = getOverdraftedComparator();
        } else if (Constants.SORT_AUCTION.equals(factor)) {
            comparator = getAuctionComparator();
        } else if (Constants.SORT_PROJECTION.equals(factor)) {
            comparator = getProjectionComparator();
        } else if (Constants.SORT_PAA.equals(factor)) {
            comparator = getPAAComparator();
        } else if (Constants.SORT_PAA_SCALED.equals(factor)) {
            comparator = getPAAScaledComparator();
        } else if (Constants.SORT_PAAPD.equals(factor)) {
            comparator = getPAAPDComparator();
        } else if (Constants.SORT_XVAL.equals(factor)) {
            comparator = getXValComparator();
        } else if (Constants.SORT_XVAL_SCALED.equals(factor)) {
            comparator = getXValScaledComparator();
        } else if (Constants.SORT_XVALPD.equals(factor)) {
            comparator = getXvalPDComparator();
        } else if (Constants.SORT_VOLS.equals(factor)) {
            comparator = getVoLSComparator();
        } else if (Constants.SORT_VOLS_SCALED.equals(factor)) {
            comparator = getVoLSScaledComparator();
        } else if (Constants.SORT_VOLSPD.equals(factor)) {
            comparator = getVoLSPDComparator();
        } else if (Constants.SORT_RISK.equals(factor)) {
            comparator = getRiskComparator();
        } else if (Constants.SORT_SOS.equals(factor)) {
            comparator = getSOSComparator();
        } else if (Constants.SORT_TIERS.equals(factor)) {
            comparator = getTiersComparator();
            playerIds = rankings.getPlayersByPosition(playerIds, Constants.QBRBWRTE);
        }

        players.clear();
        for (String id : playerIds) {
            Player player = rankings.getPlayer(id);
            if (booleanFactors.contains(Constants.SORT_HIDE_DRAFTED) && rankings.getDraft().isDrafted(player)) {
                continue;
            }
            if (booleanFactors.contains(Constants.SORT_EASY_SOS)) {
                Team team = rankings.getTeam(player);
                if (team == null || (team.getSosForPosition(player.getPosition()) > Constants.SORT_EASY_SOS_THRESHOLD)) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_ONLY_HEALTHY)) {
                if (!StringUtils.isBlank(player.getInjuryStatus())) {
                    continue;
                }
            }
            if  (booleanFactors.contains(Constants.SORT_ONLY_WATCHED)) {
                if (!player.isWatched()) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_UNDER_30)) {
                if (player.getAge() == 0 || player.getAge() >= Constants.SORT_YOUNG_THRESHOLD) {
                    continue;
                }
            }
            if (booleanFactors.contains(Constants.SORT_IGNORE_LATE)) {
                if (player.getAuctionValue() < Constants.SORT_IGNORE_LATE_THRESHOLD) {
                    continue;
                }
            }

            players.add(player);
        }
        if (comparator != null) {
            // If it's null, it was default, which means the already ordered list
            Collections.sort(players, comparator);
        }
        if (reversePlayers) {
            Collections.reverse(players);
        }
        displayResults(players);
    }

    private void displayResults(List<Player> players) {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);

        final ListView listview = (ListView) findViewById(R.id.sort_players_output);
        listview.setAdapter(null);
        final List<Map<String, String>> data = new ArrayList<>();
        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_item_layout,
                new String[] { Constants.PLAYER_BASIC, Constants.PLAYER_INFO, Constants.PLAYER_STATUS, Constants.PLAYER_TIER },
                new int[] { R.id.player_basic, R.id.player_info,
                        R.id.player_status, R.id.player_tier });
        listview.setAdapter(adapter);
        int displayedCount = 0;
        for (Player player : players) {
            if (rankings.getLeagueSettings().getRosterSettings().isPositionValid(player.getPosition())) {
                if (displayedCount >= sortMax) {
                    break;
                }
                Map<String, String> datum = new HashMap<>(3);
                datum.put(Constants.PLAYER_BASIC, getMainTextForFactor(player));
                datum.put(Constants.PLAYER_INFO, getSubTextForFactor(player, df));
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
                displayedCount++;
            }
        }
        adapter.notifyDataSetChanged();
        final Context context = this;
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String playerKey = getPlayerKeyFromListViewItem(view);
                final ImageView playerStatus = (ImageView)view.findViewById(R.id.player_status);
                final Player player = rankings.getPlayer(playerKey);
                if (player.isWatched()) {
                    player.setWatched(false);
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            player.setWatched(true);
                            playerStatus.setImageResource(R.drawable.star);
                            rankingsDB.updatePlayerWatchedStatus(context, player);
                        }
                    };
                    Snackbar.make(listview, player.getName() + " removed from watch list", Snackbar.LENGTH_LONG)
                            .setAction("Undo", listener).show();
                    playerStatus.setImageResource(0);
                } else {
                    player.setWatched(true);
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            player.setWatched(false);
                            playerStatus.setImageResource(0);
                            rankingsDB.updatePlayerWatchedStatus(context, player);
                        }
                    };
                    Snackbar.make(listview, player.getName() + " added to watch list", Snackbar.LENGTH_LONG)
                            .setAction("Undo", listener).show();
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
                listview.setSelection(position);
                displayPlayerInfo(playerKey);
            }
        });
        (findViewById(R.id.main_toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listview.smoothScrollToPosition(0);
            }
        });

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void displayPlayerInfo(String playerKey) {
        Intent intent = new Intent(this, PlayerInfo.class);
        intent.putExtra(Constants.PLAYER_ID, playerKey);
        startActivity(intent);
    }

    private Comparator<Player> getECRComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getEcr() > b.getEcr()) {
                    return 1;
                }
                if (a.getEcr() < b.getEcr()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getADPComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getAdp() > b.getAdp()) {
                    return 1;
                }
                if (a.getAdp() < b.getAdp()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getUnderdraftedComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double diffA = a.getEcr() - a.getAdp();
                double diffB = b.getEcr() - b.getAdp();
                if (diffA > diffB) {
                    return 1;
                }
                if (diffA < diffB) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getOverdraftedComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double diffA = a.getEcr() - a.getAdp();
                double diffB = b.getEcr() - b.getAdp();
                if (diffA < diffB) {
                    return 1;
                }
                if (diffA > diffB) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getAuctionComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getAuctionValue() > b.getAuctionValue()) {
                    return -1;
                }
                if (a.getAuctionValue() < b.getAuctionValue()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getProjectionComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getProjection() > b.getProjection()) {
                    return -1;
                }
                if (a.getProjection() < b.getProjection()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPAAComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPaa() > b.getPaa()) {
                    return -1;
                }
                if (a.getPaa() < b.getPaa()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPAAScaledComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getScaledPAA(rankings) > b.getScaledPAA(rankings)) {
                    return -1;
                }
                if (a.getScaledPAA(rankings) < b.getScaledPAA(rankings)) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getPAAPDComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double paapdA = getPAAPD(a);
                double paapdB = getPAAPD(b);
                if (paapdA > paapdB) {
                    return -1;
                }
                if (paapdA < paapdB) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private double getPAAPD(Player a) {
        double paapdA = a.getPaa() / a.getAuctionValueCustom(rankings);
        if (a.getAuctionValue() == 0.0) {
            paapdA = 0.0;
        }
        return paapdA;
    }

    private double getXvalPD(Player a) {
        double xvalpdA = a.getxVal() / a.getAuctionValueCustom(rankings);
        if (a.getAuctionValue() == 0) {
            xvalpdA = 0.0;
        }
        return xvalpdA;
    }

    private double getVoLSPD(Player a) {
        double volspdA = a.getvOLS() / a.getAuctionValueCustom(rankings);
        if (a.getAuctionValue() == 0) {
            volspdA = 0.0;
        }
        return volspdA;
    }

    private Comparator<Player> getXValComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getxVal() > b.getxVal()) {
                    return -1;
                }
                if (a.getxVal() < b.getxVal()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getVoLSComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getvOLS() > b.getvOLS()) {
                    return -1;
                }
                if (a.getvOLS() < b.getvOLS()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getXvalPDComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double xvalpdA = getXvalPD(a);
                double xvalpdB = getXvalPD(b);
                if (xvalpdA > xvalpdB) {
                    return -1;
                }
                if (xvalpdA < xvalpdB) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getVoLSPDComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                double volspdA = getVoLSPD(a);
                double volspdB = getVoLSPD(b);
                if (volspdA > volspdB) {
                    return -1;
                }
                if (volspdA < volspdB) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getXValScaledComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getScaledXVal(rankings) > b.getScaledXVal(rankings)) {
                    return -1;
                }
                if (a.getScaledXVal(rankings) < b.getScaledXVal(rankings)) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getVoLSScaledComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getScaledVoLS(rankings) > b.getScaledVoLS(rankings)) {
                    return -1;
                }
                if (a.getScaledVoLS(rankings) < b.getScaledVoLS(rankings)) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getRiskComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getRisk() > b.getRisk()) {
                    return 1;
                }
                if (a.getRisk() < b.getRisk()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getTiersComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                if (a.getPositionalTier() > b.getPositionalTier()) {
                    return 1;
                }
                if (a.getPositionalTier() < b.getPositionalTier()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private Comparator<Player> getSOSComparator() {
        return new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                int sosA = getSOS(a);
                int sosB = getSOS(b);
                if (sosA > sosB) {
                    return 1;
                }
                if (sosA < sosB) {
                    return -1;
                }
                return 0;
            }
        };
    }

    private int getSOS(Player player) {
        Team team = rankings.getTeam(player);
        if (team == null) {
            return 32;
        }
        return team.getSosForPosition(player.getPosition());
    }

    private String getMainTextForFactor(Player player) {
        String prefix = getMainTextPrefixForPlayer(player);

        return new StringBuilder(prefix)
                .append(Constants.RANKINGS_LIST_DELIMITER)
                .append(player.getName())
                .toString();
    }

    private String getMainTextPrefixForPlayer(Player player) {
        DecimalFormat df = new DecimalFormat(Constants.NUMBER_FORMAT);
        if (Constants.SORT_ALL.equals(factor)) {
            if (rankings.getLeagueSettings().isAuction()) {
                return df.format(player.getAuctionValueCustom(rankings));
            } else {
                return String.valueOf(player.getEcr());
            }
        } else if (Constants.SORT_ECR.equals(factor)) {
            return String.valueOf(player.getEcr());
        } else if (Constants.SORT_ADP.equals(factor)) {
            return String.valueOf(player.getAdp());
        } else if (Constants.SORT_UNDERDRAFTED.equals(factor) || Constants.SORT_OVERDRAFTED.equals(factor)) {
            return df.format(player.getEcr() - player.getAdp());
        } else if (Constants.SORT_AUCTION.equals(factor)) {
            return df.format(player.getAuctionValueCustom(rankings));
        } else if (Constants.SORT_PROJECTION.equals(factor)) {
            return df.format(player.getProjection());
        } else if (Constants.SORT_PAA.equals(factor)) {
            return df.format(player.getPaa());
        } else if (Constants.SORT_PAA_SCALED.equals(factor)) {
            return df.format(player.getScaledPAA(rankings));
        } else if (Constants.SORT_PAAPD.equals(factor)) {
            return df.format(getPAAPD(player));
        } else if (Constants.SORT_XVAL.equals(factor)) {
            return df.format(player.getxVal());
        } else if (Constants.SORT_XVAL_SCALED.equals(factor)) {
            return df.format(player.getScaledXVal(rankings));
        } else if (Constants.SORT_XVALPD.equals(factor)) {
            return df.format(getXvalPD(player));
        } else if (Constants.SORT_VOLS.equals(factor)) {
            return df.format(player.getvOLS());
        } else if (Constants.SORT_VOLS_SCALED.equals(factor)) {
            return df.format(player.getScaledVoLS(rankings));
        } else if (Constants.SORT_VOLSPD.equals(factor)) {
            return df.format(getVoLSPD(player));
        } else if (Constants.SORT_RISK.equals(factor)) {
            return String.valueOf(player.getRisk());
        } else if (Constants.SORT_SOS.equals(factor)) {
            return String.valueOf(getSOS(player));
        } else if (Constants.SORT_TIERS.equals(factor)) {
            return String.valueOf(player.getPositionalTier());
        }
        return "";
    }

    private String getSubTextForFactor(Player player, DecimalFormat df) {
        StringBuilder subtextBuilder = new StringBuilder(generateOutputSubtext(player));
        if (!Constants.SORT_PROJECTION.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Projection: ")
                    .append(player.getProjection());
        }
        if (Constants.SORT_UNDERDRAFTED.equals(factor) || Constants.SORT_OVERDRAFTED.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("ECR: ")
                    .append(player.getEcr())
                    .append(Constants.LINE_BREAK)
                    .append("ADP: ")
                    .append(player.getAdp());
        }
        boolean isAuction = rankings.getLeagueSettings().isAuction();
        if (isAuction && !Constants.SORT_AUCTION.equals(factor) && !Constants.SORT_ALL.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("Auction Value: ")
                    .append(df.format(player.getAuctionValueCustom(rankings)));
        } else if (!isAuction && !Constants.SORT_ECR.equals(factor) && !Constants.SORT_ALL.equals(factor)) {
            subtextBuilder.append(Constants.LINE_BREAK)
                    .append("ECR: ")
                    .append(String.valueOf(player.getEcr()));
        }
        return subtextBuilder.toString();
    }


    private String generateOutputSubtext(Player player) {
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

    private void graphSort() {
        LayoutInflater li = LayoutInflater.from(this);
        View graphView = li.inflate(R.layout.sort_graph_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(graphView);
        LineChart lineGraph = (LineChart) graphView.findViewById(R.id.sort_graph);
        List<Entry> entries = new ArrayList<>();
        List<Entry> qbs = new ArrayList<>();
        List<Entry> rbs = new ArrayList<>();
        List<Entry> wrs = new ArrayList<>();
        List<Entry> tes = new ArrayList<>();
        List<Entry> dsts = new ArrayList<>();
        List<Entry> ks = new ArrayList<>();
        for (int i = 0; i < Math.min(sortMax, players.size()); i++) {
            Player player = players.get(i);
            double value = Double.parseDouble(getMainTextPrefixForPlayer(player));
            entries.add(new Entry(i, (int) value));
            if (Constants.QB.equals(player.getPosition())) {
                qbs.add(new Entry(qbs.size(), (int) value));
            } else if (Constants.RB.equals(player.getPosition())) {
                rbs.add(new Entry(rbs.size(), (int) value));
            } else if (Constants.WR.equals(player.getPosition())) {
                wrs.add(new Entry(wrs.size(), (int) value));
            } else if (Constants.TE.equals(player.getPosition())) {
                tes.add(new Entry(tes.size(), (int) value));
            } else if (Constants.DST.equals(player.getPosition())) {
                dsts.add(new Entry(dsts.size(), (int) value));
            } else if (Constants.K.equals(player.getPosition())) {
                ks.add(new Entry(ks.size(), (int) value));
            }
        }
        LineDataSet allPositions = getLineDataSet(entries, "All Positions", "blue");
        LineData lineData = new LineData();

        conditionallyAddData(lineData, qbs, "QBs", "green");
        conditionallyAddData(lineData, rbs, "RBs", "red");
        conditionallyAddData(lineData, wrs, "WRs", "purple");
        conditionallyAddData(lineData, tes, "TEs", "yellow");
        conditionallyAddData(lineData, dsts, "DSTs", "black");
        conditionallyAddData(lineData, ks, "Ks", "grey");

        if (lineData.getDataSetCount() > 1) {
            lineData.addDataSet(allPositions);
        }

        lineGraph.setData(lineData);
        Description description = new Description();
        description.setText(factor);
        description.setTextSize(12f);
        lineGraph.setDescription(description);
        lineGraph.invalidate();
        lineGraph.setTouchEnabled(true);
        lineGraph.setPinchZoom(true);
        lineGraph.setDragEnabled(true);
        lineGraph.animateX(1500);
        lineGraph.animateY(1500);

        alertDialogBuilder
                .setNegativeButton("Dismiss",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void conditionallyAddData(LineData lineData, List<Entry> entries, String label, String color) {
        if (entries.size() > 0) {
            lineData.addDataSet(getLineDataSet(entries, label, color));
        }
    }

    private LineDataSet getLineDataSet(List<Entry> entries, String label, String color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.parseColor(color));
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        return dataSet;
    }
}
