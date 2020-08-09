package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class InjuryBounceBackTag extends Tag {
    public InjuryBounceBackTag(int count) {
        super(count, Constants.INJURY_BOUNCE_BACK_TAG, Constants.INJURY_BOUNCE_BACK_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
