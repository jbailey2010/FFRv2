package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class NewTeamTag extends Tag {
    public NewTeamTag(int count) {
        super(count, Constants.NEW_TEAM_TAG, Constants.NEW_TEAM_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
