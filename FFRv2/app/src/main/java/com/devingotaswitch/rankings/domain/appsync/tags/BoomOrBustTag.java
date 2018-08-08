package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class BoomOrBustTag extends Tag {
    public BoomOrBustTag(int count) {
        super(count, "Boom or Bust", new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
