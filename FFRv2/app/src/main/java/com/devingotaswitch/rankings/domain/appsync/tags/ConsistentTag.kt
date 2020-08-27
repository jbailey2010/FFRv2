package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class ConsistentTag(count: Int) : Tag(count, Constants.CONSISTENT_TAG, Constants.CONSISTENT_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))