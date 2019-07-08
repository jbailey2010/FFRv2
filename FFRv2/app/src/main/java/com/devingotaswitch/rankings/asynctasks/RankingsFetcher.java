package com.devingotaswitch.rankings.asynctasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.domain.DailyProjection;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.sources.ParseDraft;
import com.devingotaswitch.rankings.sources.ParseDraftWizard;
import com.devingotaswitch.rankings.sources.ParseFantasyPros;
import com.devingotaswitch.rankings.sources.ParseESPN;
import com.devingotaswitch.rankings.sources.ParseFA;
import com.devingotaswitch.rankings.sources.ParseFFTB;
import com.devingotaswitch.rankings.sources.ParseInjuries;
import com.devingotaswitch.rankings.sources.ParseMath;
import com.devingotaswitch.rankings.sources.ParseNFL;
import com.devingotaswitch.rankings.sources.ParsePFO;
import com.devingotaswitch.rankings.sources.ParseProjections;
import com.devingotaswitch.rankings.sources.ParseSOS;
import com.devingotaswitch.rankings.sources.ParseStats;
import com.devingotaswitch.rankings.sources.ParseWalterFootball;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.sources.ParseYahoo;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RankingsFetcher {
    private static final String TAG = "RankingsFetcher";

    public class VBDUpdater extends AsyncTask<Object, Void, Void> {
        private final Rankings rankings;
        private final Activity activity;
        private final RankingsDBWrapper rankingsDB;
        private final LeagueSettings league;
        private final boolean updateProjections;

        public VBDUpdater(Rankings rankings, Activity activity, LeagueSettings league, boolean updateProjections,
                          RankingsDBWrapper rankingsDB) {
            this.rankings = rankings;
            this.activity = activity;
            this.rankingsDB = rankingsDB;
            this.league = league;
            this.updateProjections = updateProjections;
        }

        @Override
        protected Void doInBackground(Object... data) {

            if (updateProjections) {
                Log.i(TAG, "Updating projections");
                try {
                    for (Player player : rankings.getPlayers().values()) {
                        player.updateProjection(league.getScoringSettings());
                    }
                } catch(Exception e) {
                    Log.e(TAG, "Failed to update player projections", e);
                }
            }

            Log.i(TAG, "Getting paa calculations");
            try {
                ParseMath.setPlayerPAA(rankings, league);
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate PAA", e);
            }

            Log.i(TAG, "Getting VoRP calculations");
            try {
                ParseMath.setPlayerVoLS(rankings, league);
            } catch (Exception e) {
                Log.e(TAG, "Failed to calculate VoRP", e);
            }

            Log.i(TAG, "Setting player xvals");
            try {
                ParseMath.setPlayerXval(rankings, league);
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate Xval", e);
            }

            rankingsDB.savePlayers(activity, rankings.getPlayers().values());
            return null;
        }
    }

    public class RanksAggregator extends AsyncTask<Object, String, Rankings> {
        private final ProgressDialog pdia;
        private final RankingsHome act;
        private final Rankings rankings;
        private long start;

        public RanksAggregator(RankingsHome activity, Rankings rankings) {
            this.pdia = new ProgressDialog(activity);
            pdia.setCancelable(false);
            this.act = activity;
            this.rankings = rankings;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia.setMessage("Please wait, fetching the rankings...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(Rankings result) {
            super.onPostExecute(result);
            pdia.dismiss();
            Log.d(TAG, GeneralUtils.getLatency(start) + " seconds to fetch rankings");
            LocalSettingsHelper.saveRankingsFetched(act, true);
            act.processNewRankings(result, true);
        }

        @Override
        protected Rankings doInBackground(Object... data) {
            start = System.currentTimeMillis();
            rankings.clearRankings();

            Log.i(TAG, "Getting WF rankings");
            try {
                ParseWalterFootball.wfRankings(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse WF", e);
            }
            publishProgress("Fetching rankings... 1/14");

            Log.i(TAG, "Getting ESPN ADV rankings");
            try {
                ParseESPN.parseESPNAggregate(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse ESPN", e);
            }
            publishProgress("Fetching rankings... 3/14");


            Log.i(TAG, "Getting FFTB rankings");
            try {
                ParseFFTB.parseFFTBRankingsWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse FFTB", e);
            }
            publishProgress("Fetching rankings... 4/14");

            Log.i(TAG, "Getting Yahoo rankings");
            try {
                ParseYahoo.parseYahooWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse Yahoo", e);
            }
            publishProgress("Fetching rankings... 6/14");

            Log.i(TAG, "Getting NFL rankings");
            try {
                ParseNFL.parseNFLAAVWrapper(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to parse NFL", e);
            }
            publishProgress("Fetching rankings... 8/14");

            Log.i(TAG, "Getting Draft Wizard rankings");
            try {
                ParseDraftWizard.parseRanksWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse Draft Wizard", e);
            }
            publishProgress("Fetching rankings... 10/14");

            Log.i(TAG, "Getting projections");
            publishProgress("Getting projections...");
            try {
                ParseProjections.projPointsWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse projections", e);
            }

            publishProgress("Getting consensus rankings...");
            Log.i(TAG, "Getting ECR rankings");
            try {
                ParseFantasyPros.parseECRWrapper(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to parse ECR/risk", e);
            }
            Log.i(TAG, "Getting ADP rankings");
            try {
                ParseFantasyPros.parseADPWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse ADP", e);
            }
            Log.i(TAG, "Getting Dynasty rankings");
            try {
                ParseFantasyPros.parseDynastyWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse dynasty ranks", e);
            }
            Log.i(TAG, "Getting rookie rankings");
            try {
                ParseFantasyPros.parseRookieWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse rookie ranks", e);
            }
            Log.i(TAG, "Getting best ball rankings");
            try {
                ParseFantasyPros.parseBestBallWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse best ball ranks", e);
            }

            Log.i(TAG, "Cleaning up duplicate players");
            dedupPlayers();

            Log.i(TAG, "Getting auction values from adp/ecr");
            boolean adpSet = false;
            boolean ecrSet = false;
            for (Player player : rankings.getPlayers().values()) {
                if (player.getAdp() < 300.0) {
                    adpSet = true;
                }
                if (player.getEcr() < 300.0) {
                    ecrSet = true;
                }
                if (adpSet && ecrSet) {
                    break;
                }
            }
            if (adpSet) {
                ParseMath.getADPAuctionValue(rankings);
            } else {
                Log.d(TAG, "Not setting ADP auction values, ADP not set.");
            }
            if (ecrSet) {
                ParseMath.getECRAuctionValue(rankings);
            } else {
                Log.d(TAG, "Not setting ECR auction values, ECR not set.");
            }
            publishProgress("Fetching rankings... 12/14");

            Log.i(TAG, "Getting paa calculations");
            publishProgress("Calculating PAA...");
            try {
                ParseMath.setPlayerPAA(rankings, rankings.getLeagueSettings());
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate PAA", e);
            }

            Log.i(TAG, "Getting VoRP calculations");
            publishProgress("Calculating VoRP...");
            try {
                ParseMath.setPlayerVoLS(rankings, rankings.getLeagueSettings());
            } catch (Exception e) {
                Log.e(TAG, "Failed to calculate VoRP", e);
            }

            Log.i(TAG, "Setting player xvals");
            publishProgress("Calculating xVal...");
            try {
                ParseMath.setPlayerXval(rankings, rankings.getLeagueSettings());
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate Xval", e);
            }

            Log.i(TAG, "Getting PAA rankings");
            boolean projSet = false;
            for (Player player : rankings.getPlayers().values()) {
                if (player.getProjection() > 0.0) {
                    projSet = true;
                    break;
                }
            }
            if (projSet) {
                ParseMath.getPAAAuctionValue(rankings);
                ParseMath.getPAAAuctionValue(rankings);
            } else {
                Log.d(TAG, "Not setting PAA auction values, no projections are set.");
            }
            publishProgress("Fetching rankings... 14/14");

            Log.i(TAG, "Getting positional sos");
            publishProgress("Getting positional SOS...");
            try {
                ParseSOS.getSOS(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse SOS", e);
            }

            Log.i(TAG, "Getting advanced oline stats");
            publishProgress("Getting advanced line stats...");
            try {
                ParsePFO.parsePFOLineData(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse PFO line data", e);
            }

            Log.i(TAG, "Getting injury statuses");
            publishProgress("Getting injury status...");
            try {
                ParseInjuries.parsePlayerInjuries(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse player injuries");
            }

            Log.i(TAG, "Getting player stats");
            publishProgress("Getting last year's stats...");
            try {
                ParseStats.setStats(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get player stats", e);
            }

            Log.i(TAG, "Getting draft info");
            publishProgress("Getting draft information...");
            try {
                ParseDraft.parseTeamDraft(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get draft info", e);
            }

            Log.i(TAG, "Getting free agency info");
            publishProgress("Getting free agency classes...");
            try {
                ParseFA.parseFAClasses(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get FA info", e);
            }

            Log.i(TAG, "Getting team schedules");
            publishProgress("Getting team schedules...");
            try {
                ParseFantasyPros.parseSchedule(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get schedules", e);
            }

            Log.i(TAG, "Ordering players for display");
            rankings.setOrderedIds(getOrderedIds());

            // If there were no projection histories saved against the rankings, we'll set them here.
            String today = Constants.DATE_FORMAT.format(Calendar.getInstance().getTime());
            if (rankings.getPlayerProjectionHistory().size() == 0) {
                for (Player player : rankings.getPlayers().values()) {
                    DailyProjection proj = new DailyProjection();
                    proj.setPlayerKey(player.getUniqueId());
                    proj.setPlayerProjection(player.getPlayerProjection());
                    proj.setDate(today);
                    List<DailyProjection> projections = new ArrayList<>();
                    projections.add(proj);
                    rankings.getPlayerProjectionHistory().put(player.getUniqueId(), projections);
                }
            } else {
                // If there's no projection history saved for today, set it here
                String topPlayerKey = rankings.getOrderedIds().get(0);
                boolean isNewDay = true;
                for (DailyProjection proj : rankings.getPlayerProjectionHistory().get(topPlayerKey)) {
                    if (proj.getDate().equals(today)) {
                        isNewDay = false;
                    }
                }
                if (isNewDay) {
                    for (Player player : rankings.getPlayers().values()) {
                        DailyProjection proj = new DailyProjection();
                        proj.setPlayerKey(player.getUniqueId());
                        proj.setPlayerProjection(player.getPlayerProjection());
                        proj.setDate(today);
                        if (rankings.getPlayerProjectionHistory().containsKey(player.getUniqueId())) {
                            rankings.getPlayerProjectionHistory().get(player.getUniqueId()).add(proj);
                        } else {
                            List<DailyProjection> newProj = new ArrayList<>();
                            newProj.add(proj);
                            rankings.getPlayerProjectionHistory().put(player.getUniqueId(), newProj);
                        }
                    }
                }
            }

            return rankings;
        }

        private void dedupPlayers() {
            // Get players with at least one value set (meaning, probably real) and those with none (meaning, possibly have an old team).
            Map<String, Player> realPlayers = new HashMap<>();
            Set<Player> possiblyFake = new HashSet<>();
            for (String key : rankings.getPlayers().keySet()) {
                Player player = rankings.getPlayer(key);
                if (player.getProjection() == 0.0 && player.getAge() == null && player.getExperience() == -1) {
                    possiblyFake.add(player);
                } else {
                    realPlayers.put(getDedupKey(player), player);
                }
            }

            // Find matches between the two. Delete 'fake' players, and apply auction values over.
            for (Player player : possiblyFake) {
                if (realPlayers.containsKey(getDedupKey(player))) {
                    rankings.dedupPlayer(player, realPlayers.get(getDedupKey(player)));
                }
            }
        }

        private String getDedupKey(Player player) {
            return player.getName() + Constants.PLAYER_ID_DELIMITER + player.getPosition();
        }

        private List<String> getOrderedIds() {
            List<String> orderedIds = new ArrayList<>();
            Comparator<Player> comparator;
            if (rankings.getLeagueSettings().isAuction()) {
                comparator = new Comparator<Player>() {
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
            } else if (rankings.getLeagueSettings().isDynasty()) {
                comparator = new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getDynastyRank() > b.getDynastyRank()) {
                            return 1;
                        }
                        if (a.getDynastyRank() < b.getDynastyRank()) {
                            return -1;
                        }
                        return 0;
                    }
                };
            } else if (rankings.getLeagueSettings().isRookie()) {
                comparator = new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getRookieRank() > b.getRookieRank()) {
                            return 1;
                        }
                        if (a.getRookieRank() < b.getRookieRank()) {
                            return -1;
                        }
                        return 0;
                    }
                };
            } else if (rankings.getLeagueSettings().isBestBall()) {
                comparator = new Comparator<Player>() {
                    @Override
                    public int compare(Player a, Player b) {
                        if (a.getBestBallRank() > b.getBestBallRank()) {
                            return 1;
                        }
                        if (a.getBestBallRank() < b.getBestBallRank()) {
                            return -1;
                        }
                        return 0;
                    }
                };
            } else {
                comparator = new Comparator<Player>() {
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
            List<Player> playerList = new ArrayList<>(rankings.getPlayers().values());
            Collections.sort(playerList, comparator);
            for (Player player : playerList) {
                orderedIds.add(player.getUniqueId());
            }
            return orderedIds;
        }

        @Override
        public void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pdia.setMessage(values[0]);
        }
    }
}
