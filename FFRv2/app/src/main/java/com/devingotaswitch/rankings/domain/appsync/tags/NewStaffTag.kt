package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class NewStaffTag(count: Int) : Tag(count, Constants.NEW_STAFF_TAG, Constants.NEW_STAFF_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))