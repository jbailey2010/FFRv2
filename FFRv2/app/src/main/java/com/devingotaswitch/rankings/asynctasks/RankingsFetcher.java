package com.devingotaswitch.rankings.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.rankings.sources.ParseCBS;
import com.devingotaswitch.rankings.sources.ParseESPN;
import com.devingotaswitch.rankings.sources.ParseFFTB;
import com.devingotaswitch.rankings.sources.ParseMFL;
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
            publishProgress("Fetching rankings... 1/7");


            /*
            // TODO: see if values show up later?
            Log.i(TAG, "Getting CBS rankings");
            try {
                ParseCBS.cbsRankings(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse CBS", e);
            }
            publishProgress("Fetching rankings... 2/7");

            // TODO: This shit won't connect
            Log.i(TAG, "Getting ESPN ADV rankings");
            try {
                ParseESPN.parseESPNAggregate(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse ESPN", e);
            }
            publishProgress("Fetching rankings... 3/7");
            */

            Log.i(TAG, "Getting FFTB rankings");
            try {
                ParseFFTB.parseFFTBRankingsWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse FFTB", e);
            }
            publishProgress("Fetching rankings... 4/7");

            Log.i(TAG, "Getting Yahoo rankings");
            try {
                ParseYahoo.parseYahooWrapper(rankings);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse Yahoo", e);
            }
            publishProgress("Fetching rankings... 6/7");

            Log.i(TAG, "Getting MFL rankings");
            try {
                ParseMFL.getMFLAAVs(rankings);
            } catch(Exception e) {
                Log.e(TAG, "Failed to parse MFL", e);
            }
            publishProgress("Fetching rankings... 7/7");

            return null;
            /*
                System.out.println("Before Razzball");
                try {
                    ParseRazzball.getRazzballRankings(holder, r, s);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e8) {
                }
                publishProgress("Please wait, fetching the rankings...(17/30)");
                System.out.println("Before NFL AAV");
                try {
                    ParseNFL.parseNFLAAVWrapper(holder);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e3) {
                }
                System.out.println("Before Draft Wizard Rankings");
                try {
                    ParseDraftWizardRanks.parseRanksWrapper(holder, s, r);
                    publishProgress("Please wait, fetching the rankings...(23/30)");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            publishProgress("Please wait, getting projected points...");
            try {
                HighLevel.projPointsWrapper(holder, cont);
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e1) {
            }
            if (holder.maxProj() < 70.0) {
                holder.isRegularSeason = true;
            } else {
                holder.isRegularSeason = false;
            }
            System.out.println("Is regular season: " + holder.isRegularSeason);

            publishProgress("Please wait, normalizing projections...");
            MathUtils.getPAA(holder, cont);
            publishProgress("Please wait, calculating relative risk...");
            try {
                HighLevel.parseECRWrapper(holder, cont);
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e1) {
            }
            if (!holder.isRegularSeason) {
                ParseMath.convertPAA(holder, r);
                ParseMath.convertPAA(holder, r);
                publishProgress("Please wait, fetching the rankings...(26/30)");
                ParseMath.convertECR(holder);
                publishProgress("Please wait, fetching the rankings...(28/30)");
                ParseMath.convertADP(holder);
                publishProgress("Please wait, fetching the rankings...(30/30)");
            }

            publishProgress("Please wait, normalizing auction values...");
            double auctionFactor = ReadFromFile.readAucFactor(cont);
            for (PlayerObject player : holder.players) {
                Values.normVals(player.values);
                player.values.secWorth = player.values.worth / auctionFactor;
            }

            start = System.nanoTime();
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

            publishProgress("Please wait, fetching positional SOS...");

            try {
                if (!holder.isRegularSeason) {
                    HighLevel.getSOS(holder);
                } else {
                    ParseFFTB.parseSOSInSeason(holder);
                }
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e1) {
            }

            publishProgress("Please wait, fetching player contract status...");
            try {
                HighLevel.setContractStatus(holder);
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e1) {
            }

            publishProgress("Please wait, setting specific player info...");
            try {
                HighLevel.parseSpecificData(holder, cont);
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e1) {
            }

            publishProgress("Please wait, getting advanced line stats...");
            try {
                ParseOLineAdvanced.parsePFOLineData(holder);
            } catch (HttpStatusException e2) {
                System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
            } catch (IOException e1) {
            }

            if (holder.isRegularSeason) {
                publishProgress("Please wait, getting rest of season rankings...");
                try {
                    HighLevel.getROSRankingsWrapper(holder, cont);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e1) {
                }
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