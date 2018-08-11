package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class SleeperTag extends Tag {
    public SleeperTag(int count) {
        super(count, Constants.SLEEPER_TAG, Constants.SLEEPER_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
