package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class BreakoutTag extends Tag {
    public BreakoutTag(int count) {
        super(count, Constants.BREAKOUT_TAG, Constants.BREAKOUT_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
