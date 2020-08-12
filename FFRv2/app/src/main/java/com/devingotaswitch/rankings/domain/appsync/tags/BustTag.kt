package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class BustTag(count: Int) : Tag(count, Constants.BUST_TAG, Constants.BUST_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))