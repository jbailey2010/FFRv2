package com.devingotaswitch.appsync;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.devingotaswitch.graphqlstuff.CreateCommentMutation;
import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.rankings.domain.Comment;
import com.devingotaswitch.utils.AWSClientFactory;
import com.devingotaswitch.youruserpools.CUPHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class CommentActivity extends AppSyncActivity {

    private static final String TAG = "CommentActivity";

    void createComment(final Activity activity, final String comment, final String playerId) {
        final String id = UUID.randomUUID().toString();
        final String time = getCurrentTime();
        GraphQLCall.Callback<CreateCommentMutation.Data> callback = new GraphQLCall
                .Callback<CreateCommentMutation.Data>() {

            @Override
            public void onResponse(@Nonnull Response<CreateCommentMutation.Data> response) {
                if (!response.errors().isEmpty()) {
                    for (Error error : response.errors()) {
                        Log.e(TAG, "Create comment failed: " + error.message());
                    }
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            Comment newComment = new Comment();
                            newComment.setContent(comment);
                            newComment.setAuthor(CUPHelper.getCurrUser());
                            newComment.setDownvotes(0);
                            newComment.setUpvotes(1);
                            newComment.setPlayerId(playerId);
                            newComment.setTime(time);
                            newComment.setId(id);
                            List<Comment> dummyList = new ArrayList<>();
                            dummyList.add(newComment);
                            ((PlayerInfo) activity).addComments(dummyList);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "Failed to create comment.", e);
            }
        };

        AWSClientFactory.getAppSyncInstance(activity).mutate(
                CreateCommentMutation.builder()
                        .playerId(getPlayerId(playerId))
                        .id(id)
                        .author(CUPHelper.getCurrUser())
                        .content(comment)
                        .date(time)
                .build()
        ).enqueue(callback);
    }
}
