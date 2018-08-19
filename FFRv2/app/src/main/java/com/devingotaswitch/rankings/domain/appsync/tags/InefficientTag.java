package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class InefficientTag extends Tag {
    public InefficientTag(int count) {
        super(count, Constants.INEFFICIENT_TAG, Constants.INEFFICIENT_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
