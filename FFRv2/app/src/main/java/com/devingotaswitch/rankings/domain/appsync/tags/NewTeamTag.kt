package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants
import java.util.*

class NewTeamTag(count: Int) : Tag(count, Constants.NEW_TEAM_TAG, Constants.NEW_TEAM_TAG_REMOTE, HashSet(listOf(Constants.QB, Constants.RB, Constants.WR, Constants.TE)))