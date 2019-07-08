package com.devingotaswitch.rankings.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.devingotaswitch.appsync.AppSyncHelper;
import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.fileio.RankingsDBWrapper;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.rankings.domain.DailyProjection;
import com.devingotaswitch.rankings.domain.Draft;
import com.devingotaswitch.rankings.domain.LeagueSettings;
import com.devingotaswitch.rankings.domain.Player;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;
import com.devingotaswitch.utils.GeneralUtils;

import java.util.List;
import java.util.Map;

public class RankingsLoader {
    private static final String TAG = "RankingLoader";

    public class RanksLoader extends AsyncTask<Object, String, Rankings> {
        private final ProgressDialog pdia;
        private final RankingsHome act;
        private long start;
        private final RankingsDBWrapper rankingsDB;

        public RanksLoader(RankingsHome activity, RankingsDBWrapper rankingsDB) {
            this.pdia = new ProgressDialog(activity);
            pdia.setCancelable(false);
            this.act = activity;
            this.rankingsDB = rankingsDB;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia.setMessage("Please wait, loading the rankings...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(Rankings result) {
            super.onPostExecute(result);
            pdia.dismiss();
            Log.d(TAG, GeneralUtils.getLatency(start) + " to load from file");
            act.processNewRankings(result, false);
        }

        @Override
        protected Rankings doInBackground(Object... data) {
            start = System.currentTimeMillis();
            LeagueSettings currentLeague = rankingsDB.getLeague(act, LocalSettingsHelper.getCurrentLeagueName(act));
            Map<String, Player> players = rankingsDB.getPlayers(act);
            Map<String, Team> teams = rankingsDB.getTeams(act);
            List<String> orderedIds = rankingsDB.getPlayersSorted(act, currentLeague);
            Draft draft = LocalSettingsHelper.loadDraft(act, currentLeague.getTeamCount(), currentLeague.getAuctionBudget(),
                    currentLeague.getName(), players);
            Map<String, List<DailyProjection>> playerProjectionHistory = rankingsDB.getPlayerProjectionHistory(act);
            return Rankings.init(teams, players, orderedIds, currentLeague, draft, playerProjectionHistory);
        }
    }

    public class RanksSaver extends AsyncTask<Object, String, Void> {
        private final ProgressDialog pdia;
        private final RankingsHome act;
        private long start;
        private final RankingsDBWrapper rankingsDB;

        public RanksSaver(RankingsHome activity, RankingsDBWrapper rankingsDB) {
            this.pdia = new ProgressDialog(activity);
            pdia.setCancelable(false);
            this.act = activity;
            this.rankingsDB = rankingsDB;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia.setMessage("Please wait, saving the rankings...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdia.dismiss();
            Log.d(TAG, GeneralUtils.getLatency(start) + " to save to file");
        }

        @Override
        protected Void doInBackground(Object... data) {
            start = System.currentTimeMillis();
            Map<String, Player> players = (Map<String, Player>) data[0];
            Map<String, Team> teams = (Map<String, Team>) data[1];
            rankingsDB.saveTeams(act, teams.values());
            rankingsDB.savePlayers(act, players.values());
            return null;
        }
    }
}
