package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class ConsistentTag extends Tag {
    public ConsistentTag(int count) {
        super(count, Constants.CONSISTENT_TAG, Constants.CONSISTENT_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
