package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class BounceBackTag extends Tag {
    public BounceBackTag(int count) {
        super(count, Constants.BOUNCE_BACK_TAG, Constants.BOUNCE_BACK_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
