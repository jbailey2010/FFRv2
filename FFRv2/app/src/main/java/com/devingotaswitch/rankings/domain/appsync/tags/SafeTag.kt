package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class SafeTag(count: Int) : Tag(count, Constants.SAFE_TAG, Constants.SAFE_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))