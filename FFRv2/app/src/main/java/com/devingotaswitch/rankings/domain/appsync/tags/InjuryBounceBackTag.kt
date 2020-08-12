package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class InjuryBounceBackTag(count: Int) : Tag(count, Constants.INJURY_BOUNCE_BACK_TAG, Constants.INJURY_BOUNCE_BACK_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))