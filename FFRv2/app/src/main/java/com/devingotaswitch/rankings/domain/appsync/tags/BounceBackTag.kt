package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class BounceBackTag(count: Int) : Tag(count, Constants.BOUNCE_BACK_TAG, Constants.BOUNCE_BACK_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))