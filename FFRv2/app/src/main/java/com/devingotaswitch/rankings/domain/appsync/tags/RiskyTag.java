package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RiskyTag extends Tag {
    public RiskyTag(int count) {
        super(count, Constants.RISKY_TAG, Constants.RISKY_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
