package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class StudTag(count: Int) : Tag(count, Constants.STUD_TAG, Constants.STUD_TAG_REMOTE, HashSet(Arrays.asList(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))