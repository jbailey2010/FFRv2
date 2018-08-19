package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class EfficientTag extends Tag {
    public EfficientTag(int count) {
        super(count, Constants.EFFICIENT_TAG, Constants.EFFICIENT_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
