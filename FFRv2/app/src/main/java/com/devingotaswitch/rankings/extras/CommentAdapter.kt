package com.devingotaswitch.rankings.extras

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.PlayerInfo
import com.devingotaswitch.utils.GeneralUtils.isInteger

class CommentAdapter(private val playerInfo: PlayerInfo, data: List<MutableMap<String?, *>?>?, resource: Int, from: Array<String?>?, to: IntArray?) : SimpleAdapter(playerInfo, data, resource, from, to) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val upvote = row.findViewById<ImageView>(R.id.comment_upvoted_icon)
        val downvote = row.findViewById<ImageView>(R.id.comment_downvoted_icon)
        val id = (row.findViewById<View>(R.id.comment_id) as TextView).text.toString()
        val depthString = (row.findViewById<View>(R.id.comment_reply_depth) as TextView).text.toString()
        if (isInteger(depthString)) {
            val replyDepth = depthString.toInt()
            val scale = playerInfo.resources.displayMetrics.density
            val dpAsPixels = (6 * replyDepth * scale + 0.5f).toInt()
            row.setPadding(dpAsPixels, 0, 0, 0)
            if (replyDepth < 3) {
                val reply = row.findViewById<ImageView>(R.id.comment_reply)
                reply.visibility = View.VISIBLE
                val author = (row.findViewById<View>(R.id.comment_author) as TextView).text.toString()
                reply.setOnClickListener {
                    playerInfo.updateReplyContext(replyDepth + 1, id, "Reply to $author")
                    playerInfo.giveCommentInputFocus()
                }
            } else {
                val reply = row.findViewById<ImageView>(R.id.comment_reply)
                reply.visibility = View.GONE
            }
        }
        upvote.setOnClickListener { playerInfo.conditionallyUpvoteComment(id) }
        downvote.setOnClickListener { playerInfo.conditionallyDownvoteComment(id) }
        return row
    }
}