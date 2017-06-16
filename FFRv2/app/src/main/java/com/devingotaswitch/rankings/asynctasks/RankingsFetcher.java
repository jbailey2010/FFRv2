package com.devingotaswitch.rankings.asynctasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Rankings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingsFetcher {

    public class RanksAggregator extends AsyncTask<Object, String, Rankings> {
        private ProgressDialog pdia;
        private RankingsHome act;
        private LeagueSettings leagueSettings;
        private Rankings rankings;
        private long start;
        private long all;

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
            LocalSettingsHelper.saveRankingsFetched(act, true);
            act.displayRankings(result);
        }

        @Override
        protected Rankings doInBackground(Object... data) {
            Map<String, List<String>> fa = new HashMap<String, List<String>>();
            Map<String, String> draftClasses = new HashMap<String, String>();
            if (holder.isRegularSeason) {
                fa = holder.fa;
                draftClasses = holder.draftClasses;
            }
            Roster r = ReadFromFile.readRoster(cont);
            if (!holder.isRegularSeason || holder.players.size() < 100
                    || draftIter >= 8) {
                holder.players.clear();
                holder.parsedPlayers.clear();
                Scoring s = ReadFromFile.readScoring(cont);
                all = System.nanoTime();
                System.out.println("Before WF");
                try {
                    ParseWF.wfRankings(holder, s, r);
                } catch (ArrayIndexOutOfBoundsException ee) {
                    ee.printStackTrace();
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e15) {

                }
                publishProgress("Please wait, fetching the rankings...(1/30)");
                System.out.println("Before CBS");
                try {
                    ParseCBS.cbsRankings(holder, s);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e14) {
                }
                System.out.println("Before ESPN ADV");
                publishProgress("Please wait, fetching the rankings...(3/30)");
                try {
                    ParseESPNadv.parseESPNAggregate(holder);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e13) {
                    // TODO Auto-generated catch block
                } catch (XPatherException e13) {
                    // TODO Auto-generated catch block
                    e13.printStackTrace();
                }
                publishProgress("Please wait, fetching the rankings...(4/30)");
                System.out.println("Before FFTB");
                try {
                    ParseFFTB.parseFFTBRankingsWrapper(holder);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (MalformedURLException e12) {
                    // TODO Auto-generated catch block
                    e12.printStackTrace();
                } catch (IOException e12) {
                } catch (XPatherException e12) {
                    // TODO Auto-generated catch block
                    e12.printStackTrace();
                }
                publishProgress("Please wait, fetching the rankings...(6/30)");
                System.out.println("Before Yahoo");
                try {
                    ParseYahoo.parseYahooWrapper(holder);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e9) {
                }
                publishProgress("Please wait, fetching the rankings...(8/30)");
                System.out.println("Before Fantasy Sharks");
                try {
                    ParseFantasySharks.getFantasySharksAuctionValues(holder);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e8) {
                }

                System.out.println("Before MFL");
                try {
                    ParseMFL.getMFLAAVs(holder);
                } catch (HttpStatusException e2) {
                    System.out.println(e2.getStatusCode() + ", " + e2.getUrl());
                } catch (IOException e8) {
                }

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

            return null;
        }

        @Override
        public void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pdia.setMessage((String) values[0]);
        }
    }
}
