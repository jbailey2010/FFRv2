package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class BoomOrBustTag(count: Int) : Tag(count, Constants.BOOM_OR_BUST_TAG, Constants.BOOM_OR_BUST_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))