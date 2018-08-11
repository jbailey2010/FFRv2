package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class InjuryProneTag extends Tag {
    public InjuryProneTag(int count) {
        super(count, Constants.INJURY_PRONE_TAG, Constants.INJURY_PRONE_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
