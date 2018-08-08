package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class PostHypeSleeperTag extends Tag {
    public PostHypeSleeperTag(int count) {
        super(count, "Post-Hype Sleeper", new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
