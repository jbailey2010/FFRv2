package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amazonaws.util.StringUtils;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.DecrementPlayerDraftedCountMutation;
import com.devingotaswitch.graphqlstuff.DecrementPlayerWatchedCountMutation;
import com.devingotaswitch.graphqlstuff.DecrementTagCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementPlayerDraftedCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementPlayerViewCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementPlayerWatchedCountMutation;
import com.devingotaswitch.graphqlstuff.IncrementTagCountMutation;
import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.rankings.domain.appsync.tags.AgingTag;
import com.devingotaswitch.rankings.domain.appsync.tags.BoomOrBustTag;
import com.devingotaswitch.rankings.domain.appsync.tags.BounceBackTag;
import com.devingotaswitch.rankings.domain.appsync.tags.BreakoutTag;
import com.devingotaswitch.rankings.domain.appsync.tags.BustTag;
import com.devingotaswitch.rankings.domain.appsync.tags.ConsistentTag;
import com.devingotaswitch.rankings.domain.appsync.tags.EfficientTag;
import com.devingotaswitch.rankings.domain.appsync.tags.HandcuffTag;
import com.devingotaswitch.rankings.domain.appsync.tags.InefficientTag;
import com.devingotaswitch.rankings.domain.appsync.tags.InjuryBounceBackTag;
import com.devingotaswitch.rankings.domain.appsync.tags.InjuryProneTag;
import com.devingotaswitch.rankings.domain.appsync.tags.LotteryTicketTag;
import com.devingotaswitch.rankings.domain.appsync.tags.NewStaffTag;
import com.devingotaswitch.rankings.domain.appsync.tags.NewTeamTag;
import com.devingotaswitch.rankings.domain.appsync.tags.OvervaluedTag;
import com.devingotaswitch.rankings.domain.appsync.tags.PPRSpecialistTag;
import com.devingotaswitch.rankings.domain.appsync.tags.PostHypeSleeperTag;
import com.devingotaswitch.rankings.domain.appsync.tags.ReturnerTag;
import com.devingotaswitch.rankings.domain.appsync.tags.RiskyTag;
import com.devingotaswitch.rankings.domain.appsync.tags.SafeTag;
import com.devingotaswitch.rankings.domain.appsync.tags.SleeperTag;
import com.devingotaswitch.rankings.domain.appsync.tags.StudTag;
import com.devingotaswitch.rankings.domain.appsync.tags.Tag;
import com.devingotaswitch.rankings.domain.appsync.tags.UndervaluedTag;
import com.devingotaswitch.utils.AWSClientFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

class PlayerMetadataActivity extends AppSyncActivity {

    private static final String TAG = "PlayerMetadataActivity";

    void incrementWatchCount(final Context context, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerWatchedCountMutation.Data> callback = new GraphQLCall
                .Callback<IncrementPlayerWatchedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<IncrementPlayerWatchedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player watch count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully incremented player watched count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to increment watched count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(
                IncrementPlayerWatchedCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .build())
                .enqueue(callback);
    }

    void incrementDraftCount(final Context context, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerDraftedCountMutation.Data> callback = new GraphQLCall
                .Callback<IncrementPlayerDraftedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<IncrementPlayerDraftedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player draft count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully incremented player draft count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to increment draft count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(
                IncrementPlayerDraftedCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .build())
                .enqueue(callback);
    }

    void decrementWatchCount(final Context context, final String playerId) {
        GraphQLCall.Callback<DecrementPlayerWatchedCountMutation.Data> callback = new GraphQLCall
                .Callback<DecrementPlayerWatchedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<DecrementPlayerWatchedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Decrement player watch count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully decremented player watched count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to decrement watched count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context)
                .mutate(DecrementPlayerWatchedCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .build())
                .enqueue(callback);
    }

    void decrementDraftCount(final Context context, final String playerId) {
        GraphQLCall.Callback<DecrementPlayerDraftedCountMutation.Data> callback = new GraphQLCall
                .Callback<DecrementPlayerDraftedCountMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<DecrementPlayerDraftedCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Decrement player draft count failed: " + error.message());
                    }
                } else {
                    Log.i(TAG, "Successfully decremented player draft count.");
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to decrement draft count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(context).mutate(
                DecrementPlayerDraftedCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .build())
                .enqueue(callback);
    }

    void incrementViewCount(final Activity activity, final String playerId) {
        GraphQLCall.Callback<IncrementPlayerViewCountMutation.Data> incrementViewCallback = new GraphQLCall
                .Callback <IncrementPlayerViewCountMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<IncrementPlayerViewCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player view count failed: " + error.message());
                    }
                } else if (response.data().incrementPlayerViewCount() != null &&
                        response.data().incrementPlayerViewCount().viewCount() != null) {
                    final IncrementPlayerViewCountMutation.IncrementPlayerViewCount metadata = response.data().incrementPlayerViewCount();
                    final List<Tag> tags = getTags(playerId, metadata.aging(), metadata.boomOrBust(), metadata.bounceBack(), metadata.breakout(), metadata.bust(),
                            metadata.consistentScorer(), metadata.efficient(), metadata.handcuff(), metadata.inefficient(), metadata.injuryBounceBack(), metadata.injuryProne(), metadata.lotteryTicket(), metadata.newStaff(),
                            metadata.newTeam(), metadata.overvalued(), metadata.postHypeSleeper(), metadata.pprSpecialist(), metadata.returner(), metadata.risky(), metadata.safe(), metadata.sleeper(),
                            metadata.stud(), metadata.undervalued());
                    Type listType = new TypeToken<ArrayList<String>>(){}.getType();
                    final List<String> userTags = getUserTags(metadata.userTags());
                    runOnUiThread(() -> ((PlayerInfo)activity).setAggregatePlayerMetadata(metadata.viewCount(),
                            metadata.watchCount() == null ? 0 : metadata.watchCount(),
                            metadata.draftCount() == null ? 0 : metadata.draftCount(),
                            tags, userTags));
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform increment player view count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                IncrementPlayerViewCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .build())
                .enqueue(incrementViewCallback);
    }

    private List<String> getUserTags(String serializedTags) {
        if (!StringUtils.isBlank(serializedTags)) {
            // We have to do a really hacky parse as AppSync doesn't support ProjectionExpression
            // and the only way to get only the tags field would be to have a whole separate query.
            return GSON.fromJson(serializedTags.split("userTags=")[1].split(", ")[0],
                    List.class);
        }
        return new ArrayList<>();
    }

    void incrementTagCount(final Activity activity, final String playerId, final String tagName, final List<String> tags) {
        GraphQLCall.Callback<IncrementTagCountMutation.Data> incrementTagCallback = new GraphQLCall
                .Callback <IncrementTagCountMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<IncrementTagCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Increment player tag " + tagName + " count failed: " + error.message());
                    }
                } else if (response.data().incrementTagCount() != null) {
                    final IncrementTagCountMutation.IncrementTagCount metadata = response.data().incrementTagCount();
                    final List<Tag> tags = getTags(playerId, metadata.aging(), metadata.boomOrBust(), metadata.bounceBack(), metadata.breakout(), metadata.bust(),
                            metadata.consistentScorer(), metadata.efficient(), metadata.handcuff(), metadata.inefficient(), metadata.injuryBounceBack(), metadata.injuryProne(), metadata.lotteryTicket(), metadata.newStaff(),
                            metadata.newTeam(), metadata.overvalued(), metadata.postHypeSleeper(), metadata.pprSpecialist(), metadata.returner(), metadata.risky(), metadata.safe(), metadata.sleeper(),
                            metadata.stud(), metadata.undervalued());
                    runOnUiThread(() -> ((PlayerInfo)activity).setTags(tags));
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform increment player tag " + tagName + " count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                IncrementTagCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .tagName(tagName)
                        .userTags(GSON.toJson(tags).replaceAll("\"", "\\\\\""))
                        .build())
                .enqueue(incrementTagCallback);
    }

    void decrementTagCount(final Activity activity, final String playerId, final String tagName, final List<String> tags) {
        GraphQLCall.Callback<DecrementTagCountMutation.Data> decrementTagCallback = new GraphQLCall
                .Callback <DecrementTagCountMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<DecrementTagCountMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Decrement player tag " + tagName + " count failed: " + error.message());
                    }
                } else if (response.data().decrementTagCount() != null) {
                    final DecrementTagCountMutation.DecrementTagCount metadata = response.data().decrementTagCount();
                    final List<Tag> tags = getTags(playerId, metadata.aging(), metadata.boomOrBust(), metadata.bounceBack(), metadata.breakout(), metadata.bust(),
                            metadata.consistentScorer(), metadata.efficient(), metadata.handcuff(), metadata.inefficient(), metadata.injuryBounceBack(), metadata.injuryProne(), metadata.lotteryTicket(), metadata.newStaff(),
                            metadata.newTeam(), metadata.overvalued(), metadata.postHypeSleeper(), metadata.pprSpecialist(), metadata.returner(), metadata.risky(), metadata.safe(), metadata.sleeper(),
                            metadata.stud(), metadata.undervalued());
                    runOnUiThread(() -> ((PlayerInfo)activity).setTags(tags));
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to perform decrement player tag " + tagName + " count.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                DecrementTagCountMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .tagName(tagName)
                        .userTags(GSON.toJson(tags).replaceAll("\"", "\\\\\""))
                        .build())
                .enqueue(decrementTagCallback);
    }
    
    private List<Tag> getTags(String playerId, Integer aging, Integer boomOrBust, Integer bounceBack, Integer breakout, Integer bust,
                          Integer consistent, Integer efficient, Integer handcuff, Integer inefficient, Integer injuryBounceBack, Integer injuryProne, Integer lotteryTicket, Integer newStaff,
                          Integer newTeam, Integer overvalued, Integer postHypeSleeper, Integer pprSpecialist, Integer returner, Integer risky,
                          Integer safe, Integer sleeper, Integer stud, Integer undervalued) {
        List<Tag> tags = new ArrayList<>();
        tags.add(new AgingTag(aging == null ? 0 : aging));
        tags.add(new BoomOrBustTag(boomOrBust == null ? 0 : boomOrBust));
        tags.add(new BounceBackTag(bounceBack == null ? 0 : bounceBack));
        tags.add(new BreakoutTag(breakout == null ? 0 : breakout));
        tags.add(new BustTag(bust == null ? 0 : bust));
        tags.add(new ConsistentTag(consistent == null ? 0 : consistent));
        tags.add(new EfficientTag(efficient == null ? 0 : efficient));
        tags.add(new HandcuffTag(handcuff == null ? 0 : handcuff));
        tags.add(new InefficientTag(inefficient == null ? 0 : inefficient));
        tags.add(new InjuryBounceBackTag(injuryBounceBack == null ? 0 : injuryBounceBack));
        tags.add(new InjuryProneTag(injuryProne == null ? 0 : injuryProne));
        tags.add(new LotteryTicketTag(lotteryTicket == null ? 0 : lotteryTicket));
        tags.add(new NewStaffTag(newStaff == null ? 0 : newStaff));
        tags.add(new NewTeamTag(newTeam == null ? 0 : newTeam));
        tags.add(new OvervaluedTag(overvalued == null ? 0 : overvalued));
        tags.add(new PostHypeSleeperTag(postHypeSleeper == null ? 0 : postHypeSleeper));
        tags.add(new PPRSpecialistTag(pprSpecialist == null ? 0 : pprSpecialist));
        tags.add(new ReturnerTag(returner == null ? 0 : returner));
        tags.add(new RiskyTag(risky == null ? 0 : risky));
        tags.add(new SafeTag(safe == null ? 0 : safe));
        tags.add(new SleeperTag(sleeper == null ? 0 : sleeper));
        tags.add(new StudTag(stud == null ? 0 : stud));
        tags.add(new UndervaluedTag(undervalued == null ? 0 : undervalued));
        return filterTagsByPositionToArray(tags, playerId);        
    }

    private List<Tag> filterTagsByPositionToArray(List<Tag> tags, String playerId) {
        String pos = getPosFromPlayerId(playerId);
        Iterator<Tag> iterator = tags.iterator();
        while (iterator.hasNext()) {
            Tag tag = iterator.next();
            if (!tag.isValidForPosition(pos)) {
                iterator.remove();
            }
        }
        return tags;
    }
}
