package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class InjuryProneTag(count: Int) : Tag(count, Constants.INJURY_PRONE_TAG, Constants.INJURY_PRONE_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))