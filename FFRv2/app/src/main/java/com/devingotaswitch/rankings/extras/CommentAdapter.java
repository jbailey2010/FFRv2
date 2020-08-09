package com.devingotaswitch.rankings.extras;


import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.PlayerInfo;
import com.devingotaswitch.utils.GeneralUtils;

import java.util.List;
import java.util.Map;

public class CommentAdapter extends SimpleAdapter {
    PlayerInfo playerInfo;

    public CommentAdapter(PlayerInfo playerInfo, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(playerInfo, data, resource, from, to);
        this.playerInfo = playerInfo;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        ImageView upvote = row.findViewById(R.id.comment_upvoted_icon);
        ImageView downvote = row.findViewById(R.id.comment_downvoted_icon);
        final String id = ((TextView)row.findViewById(R.id.comment_id)).getText().toString();
        String depthString = ((TextView)row.findViewById(R.id.comment_reply_depth)).getText().toString();

        if (GeneralUtils.isInteger(depthString)) {
            final Integer replyDepth = Integer.parseInt(depthString);
            float scale = playerInfo.getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (6 * replyDepth * scale + 0.5f);
            row.setPadding(dpAsPixels, 0, 0, 0);

            if (replyDepth < 3) {
                ImageView reply = row.findViewById(R.id.comment_reply);
                reply.setVisibility(View.VISIBLE);
                final String author = ((TextView)row.findViewById(R.id.comment_author)).getText().toString();
                reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playerInfo.updateReplyContext(replyDepth +1, id, "Reply to " + author);
                        playerInfo.giveCommentInputFocus();
                    }
                });
            } else {
                ImageView reply = row.findViewById(R.id.comment_reply);
                reply.setVisibility(View.GONE);
            }
        }

        upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerInfo.conditionallyUpvoteComment(id);
            }
        });

        downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerInfo.conditionallyDownvoteComment(id);
            }
        });

        return row;
    }
}
