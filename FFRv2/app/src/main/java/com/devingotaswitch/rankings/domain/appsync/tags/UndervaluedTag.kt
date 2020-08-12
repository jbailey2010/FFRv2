package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class UndervaluedTag(count: Int) : Tag(count, Constants.UNDERVALUED_TAG, Constants.UNDERVALUED_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))