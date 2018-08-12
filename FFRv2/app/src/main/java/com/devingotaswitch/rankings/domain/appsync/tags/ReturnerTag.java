package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReturnerTag extends Tag {
    public ReturnerTag(int count) {
        super(count, Constants.RETURNER_TAG, Constants.RETURNER_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.RB, Constants.WR)));
    }
}
