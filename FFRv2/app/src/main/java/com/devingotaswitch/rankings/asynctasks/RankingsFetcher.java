package com.devingotaswitch.rankings.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.rankings.sources.ParseCBS;
import com.devingotaswitch.rankings.sources.ParseDraftWizard;
import com.devingotaswitch.rankings.sources.ParseECR;
import com.devingotaswitch.rankings.sources.ParseESPN;
import com.devingotaswitch.rankings.sources.ParseFFTB;
import com.devingotaswitch.rankings.sources.ParseInjuries;
import com.devingotaswitch.rankings.sources.ParseMFL;
import com.devingotaswitch.rankings.sources.ParseMath;
import com.devingotaswitch.rankings.sources.ParseNFL;
import com.devingotaswitch.rankings.sources.ParsePFO;
import com.devingotaswitch.rankings.sources.ParseProjections;
import com.devingotaswitch.rankings.sources.ParseSOS;
import com.devingotaswitch.rankings.sources.ParseWalterFootball;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.sources.ParseYahoo;
import com.devingotaswitch.utils.GeneralUtils;

public class RankingsFetcher {
    private static final String TAG = "RankingsFetcher";

    public class RanksAggregator extends AsyncTask<Object, String, Rankings> {
        private ProgressDialog pdia;
        private RankingsHome act;
        private LeagueSettings leagueSettings;
        private Rankings rankings;
        private long start;

        public RanksAggregator(RankingsHome activity, Rankings rankings, LeagueSettings leagueSettings) {
            this.pdia = new ProgressDialog(activity);
            pdia.setCancelable(false);
            this.act = activity;
            this.rankings = rankings;
            this.leagueSettings = leagueSettings;
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
            //LocalSettingsHelper.saveRankingsFetched(act, true);
            //act.displayRankings(result);
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


            /*
            // TODO: see if values show up later?
            Log.i(TAG, "Getting CBS rankings");
            try {
                ParseCBS.cbsRankings(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse CBS", e);
            }
            publishProgress("Fetching rankings... 2/14");

            // TODO: This shit won't connect
            Log.i(TAG, "Getting ESPN ADV rankings");
            try {
                ParseESPN.parseESPNAggregate(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse ESPN", e);
            }
            publishProgress("Fetching rankings... 3/14");
            */

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

            Log.i(TAG, "Getting MFL rankings");
            try {
                ParseMFL.getMFLAAVs(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to parse MFL", e);
            }
            publishProgress("Fetching rankings... 7/14");

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

            publishProgress("Getting projections...");
            try {
                ParseProjections.projPointsWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse projections", e);
            }

            // TODO: decide isRegularSeasoon here

            publishProgress("Getting adp...");
            try {
                ParseECR.parseECRWrapper(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to parse ecr/adp/risk", e);
            }

            Log.i(TAG, "Getting ECR/ADP rankings");
            ParseMath.getADPAuctionValue(rankings);
            ParseMath.getECRAuctionValue(rankings);
            publishProgress("Fetching rankings... 12/14");

            publishProgress("Calculating PAA...");
            try {
                ParseMath.setPlayerPAA(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to calculate PAA", e);
            }

            Log.i(TAG, "Getting PAA rankings");
            ParseMath.getPAAAuctionValue(rankings);
            ParseMath.getPAAAuctionValue(rankings);
            publishProgress("Fetching rankings... 14/14");

            publishProgress("Getting positional SOS...");
            try {
                ParseSOS.getSOS(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse SOS", e);
            }

            publishProgress("Getting advanced line stats...");
            try {
                ParsePFO.parsePFOLineData(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse PFO line data", e);
            }

            publishProgress("Getting injury status...");
            try {
                ParseInjuries.parsePlayerInjuries(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse player injuries");
            }

            return null;
            /*
            publishProgress("Please wait, fetching player stats...");
            try {
                HighLevel.setStats(holder, cont);
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e) {
            }

            publishProgress("Please wait, fetching team data...");
            if (!holder.isRegularSeason
                    || (holder.isRegularSeason && (fa.size() < 5 || draftClasses
                    .size() < 5))) {
                try {
                    HighLevel.setTeamInfo(holder, cont);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e1) {
                }
            } else {
                holder.fa = fa;
                holder.draftClasses = draftClasses;
            }
            */
        }

        @Override
        public void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pdia.setMessage(values[0]);
        }
    }
}
