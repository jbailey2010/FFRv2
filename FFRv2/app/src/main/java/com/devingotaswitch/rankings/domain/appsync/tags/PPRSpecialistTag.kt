package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class PPRSpecialistTag(count: Int) : Tag(count, Constants.PPR_SPECIALIST_TAG, Constants.PPR_SPECIALIST_TAG_REMOTE, HashSet(Arrays.asList(Constants.RB, Constants.WR, Constants.TE)))