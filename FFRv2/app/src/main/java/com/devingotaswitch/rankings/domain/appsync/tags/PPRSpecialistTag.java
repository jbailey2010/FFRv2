package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class PPRSpecialistTag extends Tag {
    public PPRSpecialistTag(int count) {
        super(count, "PPR Specialist", new HashSet<>(Arrays.asList(Constants.RB, Constants.WR, Constants.TE)));
    }
}
