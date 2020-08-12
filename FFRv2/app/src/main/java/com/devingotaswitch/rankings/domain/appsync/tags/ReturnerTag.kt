package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class ReturnerTag(count: Int) : Tag(count, Constants.RETURNER_TAG, Constants.RETURNER_TAG_REMOTE, HashSet(Arrays.asList(Constants.RB, Constants.WR)))