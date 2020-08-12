package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class LotteryTicketTag(count: Int) : Tag(count, Constants.LOTTERY_TICKET_TAG, Constants.LOTTERY_TICKET_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))