package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class BustTag extends Tag {
    public BustTag(int count) {
        super(count, "Bust", new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
