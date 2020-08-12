package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class OvervaluedTag(count: Int) : Tag(count, Constants.OVERVALUED_TAG, Constants.OVERVALUED_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))