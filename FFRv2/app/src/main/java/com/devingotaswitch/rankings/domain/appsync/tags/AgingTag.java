package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AgingTag extends Tag {
    public AgingTag(int count) {
        super(count, Constants.AGING_TAG, Constants.AGING_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
