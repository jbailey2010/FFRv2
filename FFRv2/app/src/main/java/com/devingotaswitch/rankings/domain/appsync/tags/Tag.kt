package com.devingotaswitch.rankings.domain.appsync.tags

import com.devingotaswitch.utils.Constants

abstract class Tag(val count: Int, val title: String, val remoteTitle: String, private val validPositions: Set<String>) {

    fun isValidForPosition(pos: String?): Boolean {
        return validPositions.contains(pos)
    }

    val tagText: String
        get() = title + Constants.TAG_TEXT_DELIMITER + count

}