package com.devingotaswitch.appsync;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.fetcher.ResponseFetcher;
import com.devingotaswitch.graphqlstuff.GetTeamQuery;
import com.devingotaswitch.graphqlstuff.ListTeamsQuery;
import com.devingotaswitch.rankings.domain.Rankings;
import com.devingotaswitch.rankings.domain.Team;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

class TeamActivity extends AppCompatActivity {
    private static final String TAG = "TeamActivity";

    void getTeams(String leagueId, AWSAppSyncClient appSync) {
        GraphQLCall.Callback<ListTeamsQuery.Data> listTeamsCallback = new GraphQLCall.Callback <ListTeamsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to list teams", e);
            }
        };

        appSync.query(ListTeamsQuery.builder().leagueId(leagueId).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_FIRST)
                .enqueue(listTeamsCallback);
    }

    private void setTeams(Response<ListTeamsQuery.Data> response) {
        for (ListTeamsQuery.ListTeam team : response.data().listTeams()) {
            Team domainTeam = new Team();
            domainTeam.setName(team.name());
            domainTeam.setBye(team.bye());
            domainTeam.setDraftClass(team.draftClass());
            domainTeam.setQbSos(team.qbSos());
            domainTeam.setRbSos(team.rbSos());
            domainTeam.setWrSos(team.wrSos());
            domainTeam.setTeSos(team.teSos());
            domainTeam.setDstSos(team.dstSos());
            domainTeam.setkSos(team.kSos());
            domainTeam.setFaClass(team.faClass());
            domainTeam.setoLineRanks(team.oLineRanks());

            // TODO: act on this
        }
    }
}
