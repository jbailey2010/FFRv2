package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class EfficientTag(count: Int) : Tag(count, Constants.EFFICIENT_TAG, Constants.EFFICIENT_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))