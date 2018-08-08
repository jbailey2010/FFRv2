package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class HandcuffTag extends Tag {
    public HandcuffTag(int count) {
        super(count, "Handcuff", new HashSet<>(Arrays.asList(Constants.RB, Constants.WR)));
    }
}
