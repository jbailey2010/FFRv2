package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class OvervaluedTag extends Tag {
    public OvervaluedTag(int count) {
        super(count, Constants.OVERVALUED_TAG, Constants.OVERVALUED_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
