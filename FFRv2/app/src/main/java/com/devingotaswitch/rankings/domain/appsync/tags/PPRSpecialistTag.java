package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class PPRSpecialistTag extends Tag {
    public PPRSpecialistTag(int count) {
        super(count, Constants.PPR_SPECIALIST_TAG, Constants.PPR_SPECIALIST_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.RB, Constants.WR, Constants.TE)));
    }
}
