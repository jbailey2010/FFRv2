package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class HandcuffTag(count: Int) : Tag(count, Constants.HANDCUFF_TAG, Constants.HANDCUFF_TAG_REMOTE, HashSet(Arrays.asList(Constants.RB, Constants.WR)))