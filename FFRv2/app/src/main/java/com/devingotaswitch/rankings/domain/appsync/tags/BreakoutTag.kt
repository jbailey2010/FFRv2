package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class BreakoutTag(count: Int) : Tag(count, Constants.BREAKOUT_TAG, Constants.BREAKOUT_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))