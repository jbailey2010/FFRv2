package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class AgingTag(count: Int) : Tag(count, Constants.AGING_TAG, Constants.AGING_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))