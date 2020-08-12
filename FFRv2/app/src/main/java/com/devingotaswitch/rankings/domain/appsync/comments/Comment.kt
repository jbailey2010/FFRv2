package com.devingotaswitch.rankings.domain.appsync.comments

class Comment {
    var author: String? = null
    var time: String? = null
    var content: String? = null
    var id: String? = null
    var playerId: String? = null
    var upvotes: Int? = null
    var downvotes: Int? = null
    var replyToId: String? = null
    var replyDepth: Int? = null
    var isUpvoted = false
    var isDownvoted = false

}