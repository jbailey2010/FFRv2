package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class InefficientTag(count: Int) : Tag(count, Constants.INEFFICIENT_TAG, Constants.INEFFICIENT_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))