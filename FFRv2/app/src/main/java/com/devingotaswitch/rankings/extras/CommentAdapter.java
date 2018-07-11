package com.devingotaswitch.rankings.extras;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.PlayerInfo;

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
        LayoutInflater inflater = playerInfo.getLayoutInflater();
        //View row = inflater.inflate(R.layout.list_item_comment_layout, parent, false);
        View row = super.getView(position, convertView, parent);
        ImageView upvote = row.findViewById(R.id.comment_upvoted_icon);
        ImageView downvote = row.findViewById(R.id.comment_downvoted_icon);
        final String id = ((TextView)row.findViewById(R.id.comment_id)).getText().toString();

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
