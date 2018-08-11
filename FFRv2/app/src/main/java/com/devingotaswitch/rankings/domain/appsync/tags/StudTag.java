package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StudTag extends Tag {
    public StudTag(int count) {
        super(count, Constants.STUD_TAG, Constants.STUD_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
