package com.devingotaswitch.rankings.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RankingsFetcher {
    private static final String TAG = "RankingsFetcher";

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

            Log.i(TAG, "Cleaning up duplicate players");
            dedupPlayers();

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

                // Only get ECR auction value if it parses, so we don't get dummy values
                Log.i(TAG, "Getting auction values from ecr");
                ParseMath.getECRAuctionValue(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to parse ECR/risk", e);
            }
            Log.i(TAG, "Getting ADP rankings");
            try {
                ParseFantasyPros.parseADPWrapper(rankings);

                // Only get ADP auction values if it passes, so we don't get dummy values
                Log.i(TAG, "Getting auction values from adp");
                ParseMath.getADPAuctionValue(rankings);
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

            Log.i(TAG, "Getting positional tiers");
            publishProgress("Getting player positional tiers...");
            try {
                ParseMath.getTiers(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get tiers", e);
            }

            Log.i(TAG, "Getting paa calculations");
            publishProgress("Calculating PAA...");
            try {
                ParseMath.setPlayerPAA(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate PAA", e);
            }

            Log.i(TAG, "Getting VoRP calculations");
            publishProgress("Calculating VoRP...");
            try {
                ParseMath.setPlayerVoLS(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to calculate VoRP", e);
            }

            Log.i(TAG, "Setting player xvals");
            publishProgress("Calculating xVal...");
            try {
                ParseMath.setPlayerXval(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate Xval", e);
            }

            Log.i(TAG, "Getting PAA rankings");
            ParseMath.getPAAAuctionValue(rankings);
            ParseMath.getPAAAuctionValue(rankings);
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

            Log.i(TAG, "Ordering players for display");
            rankings.setOrderedIds(getOrderedIds());
            setWatchedPlayers();

            return rankings;
        }

        private void dedupPlayers() {
            // Get players with at least one value set (meaning, probably real) and those with none (meaning, possibly have an old team).
            Map<String, Player> realPlayers = new HashMap<>();
            Set<Player> possiblyFake = new HashSet<>();
            for (String key : rankings.getPlayers().keySet()) {
                Player player = rankings.getPlayer(key);
                if (player.getProjection() == 0.0 && player.getEcr() == 300.0 && player.getAdp() == 300.0) {
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
        private void setWatchedPlayers() {
            RankingsDBWrapper rankingsDB = new RankingsDBWrapper();
            List<Player> watchList = rankingsDB.getWatchList(act);
            for (Player player : watchList) {
                rankings.getPlayer(player.getUniqueId()).setWatched(true);
            }
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
