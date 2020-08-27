package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class PostHypeSleeperTag(count: Int) : Tag(count, Constants.POST_HYPE_SLEEPER_TAG, Constants.POST_HYPE_SLEEPER_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))