package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;

public class LotteryTicketTag extends Tag {
    public LotteryTicketTag(int count) {
        super(count, Constants.LOTTERY_TICKET_TAG, Constants.LOTTERY_TICKET_TAG_REMOTE, new HashSet<>(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)));
    }
}
