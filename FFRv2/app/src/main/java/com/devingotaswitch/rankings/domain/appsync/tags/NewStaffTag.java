package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class NewStaffTag extends Tag {
    public NewStaffTag(int count) {
        super(count, "New Coaching Staff", new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
