package com.devingotaswitch.rankings

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.adroitandroid.chipcloud.ChipCloud
import com.adroitandroid.chipcloud.ChipCloud.Configure
import com.adroitandroid.chipcloud.ChipListener
import com.adroitandroid.chipcloud.FlowLayout
import com.amazonaws.util.StringUtils
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.appsync.AppSyncHelper.createComment
import com.devingotaswitch.appsync.AppSyncHelper.decrementTagCount
import com.devingotaswitch.appsync.AppSyncHelper.deleteComment
import com.devingotaswitch.appsync.AppSyncHelper.downvoteComment
import com.devingotaswitch.appsync.AppSyncHelper.getCommentsForPlayer
import com.devingotaswitch.appsync.AppSyncHelper.getOrCreatePlayerMetadataAndIncrementViewCount
import com.devingotaswitch.appsync.AppSyncHelper.incrementTagCount
import com.devingotaswitch.appsync.AppSyncHelper.upvoteComment
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.fileio.LocalSettingsHelper
import com.devingotaswitch.fileio.RankingsDBWrapper
import com.devingotaswitch.rankings.domain.Player
import com.devingotaswitch.rankings.domain.PlayerNews
import com.devingotaswitch.rankings.domain.Rankings
import com.devingotaswitch.rankings.domain.appsync.comments.Comment
import com.devingotaswitch.rankings.domain.appsync.tags.Tag
import com.devingotaswitch.rankings.extras.CommentAdapter
import com.devingotaswitch.rankings.extras.PlayerInfoSwipeDetector
import com.devingotaswitch.rankings.sources.ParseMath
import com.devingotaswitch.rankings.sources.ParsePlayerNews
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.DraftUtils.AuctionCostInterface
import com.devingotaswitch.utils.DraftUtils.getAuctionCostDialog
import com.devingotaswitch.utils.FlashbarFactory.generateFlashbarWithUndo
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.utils.GraphUtils.getLineDataSet
import com.devingotaswitch.youruserpools.CUPHelper.currUser
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import java.util.*
import kotlin.math.abs

class PlayerInfo : AppCompatActivity() {
    private lateinit var rankings: Rankings
    private lateinit var player: Player
    private var playerNews: List<PlayerNews>? = null
    private var rankingsDB: RankingsDBWrapper? = null
    private var viewCount = -1
    private var watchCount = -1
    private var draftCount = -1
    private var userTags: MutableList<String> = ArrayList()
    private var sortByUpvotes = false
    private var doUpdateImage = false
    private var replyId = Constants.COMMENT_NO_REPLY_ID
    private var replyDepth = 0
    private var data: MutableList<Map<String, String?>>? = null
    private var adapter: SimpleAdapter? = null
    private var commentData: MutableList<MutableMap<String?, String?>?>? = null
    private var commentAdapter: CommentAdapter? = null
    private var infoList: ListView? = null
    private var commentList: ListView? = null
    private var addWatch: MenuItem? = null
    private var removeWatch: MenuItem? = null
    private var draftMe: MenuItem? = null
    private var draftOther: MenuItem? = null
    private var undraft: MenuItem? = null
    private var commentSortDate: MenuItem? = null
    private var commentSortTop: MenuItem? = null
    private var chipCloud: ChipCloud? = null
    private var playerTags: List<Tag>? = null
    private var chipsDisplayed = false
    private val comments: MutableList<Comment> = ArrayList()
    private val replyMap: MutableMap<String?, MutableList<Comment>?> = HashMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_info)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        rankingsDB = RankingsDBWrapper()
        rankings = Rankings.init()
        playerId = intent.getStringExtra(Constants.PLAYER_ID)!!
        val mostlyFleshedPlayer = rankings.getPlayer(playerId)
        player = rankingsDB!!.getPlayer(this, mostlyFleshedPlayer.name, mostlyFleshedPlayer.teamName, mostlyFleshedPlayer.position)

        // Set toolbar for this screen
        val toolbar = findViewById<Toolbar>(R.id.toolbar_player_info)
        toolbar.title = ""
        val mainTitle = findViewById<TextView>(R.id.main_toolbar_title)
        mainTitle.text = player.name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val activity: Activity = this
        toolbar.setNavigationOnClickListener {
            hideKeyboard(activity)
            onBackPressed()
        }
        sortByUpvotes = Constants.COMMENT_SORT_TOP == LocalSettingsHelper.getCommentSortType(this)

        // Kick off the thread to get news
        ParsePlayerNews.startNews(player.name, player.teamName, this)

        // Kick off the thread to get comments
        getCommentsForPlayer(this, player.uniqueId, null, sortByUpvotes)

        // Kick off the thread to get player metadata
        getOrCreatePlayerMetadataAndIncrementViewCount(this, player.uniqueId)
    }

    public override fun onResume() {
        super.onResume()
        val mostlyFleshedPlayer = rankings.getPlayer(playerId)
        player = rankingsDB!!.getPlayer(this, mostlyFleshedPlayer.name, mostlyFleshedPlayer.teamName, 
                mostlyFleshedPlayer.position)
        try {
            init()
        } catch (e: Exception) {
            Log.d(TAG, "Failure setting up activity, falling back to Rankings", e)
            hideKeyboard(this)
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_player_info_menu, menu)
        addWatch = menu.findItem(R.id.player_info_add_watched)
        removeWatch = menu.findItem(R.id.player_info_remove_watched)
        draftMe = menu.findItem(R.id.player_info_draft_me)
        draftOther = menu.findItem(R.id.player_info_draft_someone)
        undraft = menu.findItem(R.id.player_info_undraft)
        commentSortDate = menu.findItem(R.id.player_info_sort_comments_date)
        commentSortTop = menu.findItem(R.id.player_info_sort_comments_top)
        hideMenuItemOnWatchStatus()
        hideMenuItemsOnDraftStatus()
        hideMenuItemsOnCommentSort()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Find which menu item was selected
        return when (item.itemId) {
            R.id.player_info_add_watched -> {
                addWatched()
                true
            }
            R.id.player_info_remove_watched -> {
                removeWatched()
                true
            }
            R.id.player_info_draft_me -> {
                if (rankings.leagueSettings.isAuction) {
                    auctionCost
                } else {
                    draftByMe(0)
                }
                true
            }
            R.id.player_info_draft_someone -> {
                draftBySomeone()
                true
            }
            R.id.player_info_undraft -> {
                undraftPlayer()
                true
            }
            R.id.player_info_compare -> {
                comparePlayer()
                true
            }
            R.id.player_info_simulate_adp -> {
                simulateAdp()
                true
            }
            R.id.player_info_sort_comments_date -> {
                sortCommentsByDate()
                true
            }
            R.id.player_info_sort_comments_top -> {
                sortCommentsByUpvotes()
                true
            }
            R.id.player_info_projection_history -> {
                showProjectionHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addWatched() {
        val removeWatch = object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                bar.dismiss()
                removeWatched()
            }
        }
        generateFlashbarWithUndo(this, "Success!", player.name + " added to watch list", 
                Flashbar.Gravity.BOTTOM, removeWatch).show()
        rankings.togglePlayerWatched(this, player.uniqueId)
        conditionallyUpdatePlayerStatus()
        hideMenuItemOnWatchStatus()
    }

    private fun removeWatched() {
        val addWatch = object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                bar.dismiss()
                addWatched()
            }
        }
        generateFlashbarWithUndo(this, "Success!", player.name + " removed from watch list",
                Flashbar.Gravity.BOTTOM, addWatch).show()
        rankings.togglePlayerWatched(this, player.uniqueId)
        hideMenuItemOnWatchStatus()
        conditionallyUpdatePlayerStatus()
    }

    fun swipeLeftToRight() {
        when (View.VISIBLE) {
            findViewById<View>(R.id.ranks_button_selected).visibility -> {
                displayComments()
            }
            findViewById<View>(R.id.player_info_button_selected).visibility -> {
                displayRanks()
            }
            findViewById<View>(R.id.team_info_button_selected).visibility -> {
                displayInfo()
            }
            findViewById<View>(R.id.news_button_selected).visibility -> {
                displayTeam()
            }
            findViewById<View>(R.id.comment_button_selected).visibility -> {
                displayNews()
            }
        }
    }

    fun swipeRightToLeft() {
        when (View.VISIBLE) {
            findViewById<View>(R.id.ranks_button_selected).visibility -> {
                displayInfo()
            }
            findViewById<View>(R.id.player_info_button_selected).visibility -> {
                displayTeam()
            }
            findViewById<View>(R.id.team_info_button_selected).visibility -> {
                displayNews()
            }
            findViewById<View>(R.id.news_button_selected).visibility -> {
                displayComments()
            }
            findViewById<View>(R.id.comment_button_selected).visibility -> {
                displayRanks()
            }
        }
    }

    private fun hideMenuItemOnWatchStatus() {
        if (rankings.isPlayerWatched(player.uniqueId)) {
            addWatch!!.isVisible = false
            removeWatch!!.isVisible = true
        } else {
            addWatch!!.isVisible = true
            removeWatch!!.isVisible = false
        }
    }

    private fun sortCommentsByDate() {
        comments.clear()
        replyMap.clear()
        commentData!!.clear()
        sortByUpvotes = false
        getCommentsForPlayer(this, player.uniqueId, null, sortByUpvotes)
        LocalSettingsHelper.saveCommentSortType(this, Constants.COMMENT_SORT_DATE)
        hideMenuItemsOnCommentSort()
    }

    private fun sortCommentsByUpvotes() {
        comments.clear()
        replyMap.clear()
        commentData!!.clear()
        sortByUpvotes = true
        getCommentsForPlayer(this, player.uniqueId, null, true)
        LocalSettingsHelper.saveCommentSortType(this, Constants.COMMENT_SORT_TOP)
        hideMenuItemsOnCommentSort()
    }

    private fun showProjectionHistory() {
        val li = LayoutInflater.from(this)
        val graphView = li.inflate(R.layout.sort_graph_popup, null)
        val alertDialogBuilder = AlertDialog.Builder(
                this)
        alertDialogBuilder.setView(graphView)
        val lineGraph: LineChart = graphView.findViewById(R.id.sort_graph)
        val projectionDays: MutableList<Entry?> = ArrayList()
        val projections = rankings.playerProjectionHistory[player.uniqueId]
        if (projections != null) {
            for (i in projections.indices) {
                val projection = projections[i]
                projectionDays.add(Entry(i.toFloat(), projection.getProjection(rankings.leagueSettings.scoringSettings).toFloat()))
            }
        } else {
            // If a league scoring setting change was made, the data will clear, so we'll just take the current projection.
            projectionDays.add(Entry(1f, player.projection.toFloat()))
        }
        val projectionHistoryDataset = getLineDataSet(projectionDays,
                player.name + " Projections", "blue")
        projectionHistoryDataset.fillColor = Color.BLUE
        if (projectionDays.size == 1) {
            projectionHistoryDataset.setDrawCircles(true)
        }
        val lineData = LineData()
        lineData.addDataSet(projectionHistoryDataset)
        lineGraph.data = lineData
        lineGraph.setDrawBorders(true)
        lineGraph.setNoDataText("No projections are available for " + player.name)
        val description = Description()
        description.text = ""
        lineGraph.description = description
        lineGraph.invalidate()
        lineGraph.setTouchEnabled(true)
        lineGraph.setPinchZoom(true)
        lineGraph.isDragEnabled = true
        lineGraph.animateX(1500)
        lineGraph.animateY(1500)
        val x = lineGraph.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawAxisLine(false)
        x.setDrawLabels(false)
        x.setDrawGridLines(false)
        val yR = lineGraph.axisRight
        yR.setDrawAxisLine(false)
        yR.setDrawGridLines(false)
        yR.setDrawLabels(false)
        val yL = lineGraph.axisLeft
        yL.setDrawLabels(true)
        yL.setDrawGridLines(true)
        yL.setDrawAxisLine(false)
        alertDialogBuilder
                .setNegativeButton("Dismiss"
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun hideMenuItemsOnCommentSort() {
        if (sortByUpvotes) {
            commentSortDate!!.isVisible = true
            commentSortTop!!.isVisible = false
        } else {
            commentSortDate!!.isVisible = false
            commentSortTop!!.isVisible = true
        }
    }

    private val auctionCost: Unit
        get() {
            val localCopy: Activity = this
            val callback: AuctionCostInterface = object : AuctionCostInterface {
                override fun onValidInput(cost: Int?) {
                    draftByMe(cost!!)
                }

                override fun onInvalidInput() {
                    generateTextOnlyFlashbar(localCopy, "No can do", "Must provide a number for cost",
                            Flashbar.Gravity.TOP).show()
                }

                override fun onCancel() {}
            }
            val alertDialog = getAuctionCostDialog(this, player, callback)
            alertDialog.show()
        }

    private fun draftByMe(cost: Int) {
        val listener = object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                undraftPlayer()
            }
        }
        rankings.draft.draftByMe(rankings, player, this, cost, listener)
        hideMenuItemsOnDraftStatus()
        displayRanks()
    }

    private fun draftBySomeone() {
        val listener = object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                undraftPlayer()
            }
        }
        rankings.draft.draftBySomeone(rankings, player, this, listener)
        hideMenuItemsOnDraftStatus()
        displayRanks()
    }

    private fun undraftPlayer() {
        rankings.draft.undraft(rankings, player, this)
        hideMenuItemsOnDraftStatus()
        conditionallyUpdatePlayerStatus()
    }

    private fun conditionallyUpdatePlayerStatus() {
        if (View.VISIBLE == chipCloud!!.visibility) {
            displayInfo()
        }
    }

    private fun comparePlayer() {
        val intent = Intent(this, PlayerComparator::class.java)
        intent.putExtra(Constants.PLAYER_ID, player.uniqueId)
        startActivity(intent)
    }

    private fun simulateAdp() {
        val intent = Intent(this, ADPSimulator::class.java)
        intent.putExtra(Constants.PLAYER_ID, player.uniqueId)
        startActivity(intent)
    }

    private fun hideMenuItemsOnDraftStatus() {
        if (rankings.draft.isDrafted(player)) {
            draftMe!!.isVisible = false
            draftOther!!.isVisible = false
            undraft!!.isVisible = true
        } else {
            draftMe!!.isVisible = true
            draftOther!!.isVisible = true
            undraft!!.isVisible = false
        }
    }

    private fun init() {
        val headerLeft = findViewById<Button>(R.id.dummy_btn_left)
        val headerRight = findViewById<Button>(R.id.dummy_btn_right)
        val headerMiddle = findViewById<Button>(R.id.dummy_btn_center)
        if (player.age != null && player.age!! > 0 && Constants.DST != player.position) {
            val expBuilder = StringBuilder()
                    .append("Age: ")
                    .append(player.age)
            if (player.experience >= 0) {
                expBuilder.append(Constants.LINE_BREAK)
                        .append("Exp: ")
                        .append(player.experience)
            }
            headerLeft.text = expBuilder.toString()
        } else if (Constants.DST == player.position) {
            headerLeft.text = "Age: N/A"
        }
        if (rankings.getTeam(player) != null && "0" != rankings.getTeam(player)!!.bye) {
            headerRight.text = "Bye:" + Constants.LINE_BREAK + rankings.getTeam(player)!!.bye
        } else if (rankings.getTeam(player) == null || "0" == rankings.getTeam(player)!!.bye) {
            headerRight.text = "Bye: N/A"
        }
        headerMiddle.text = player.teamName + Constants.LINE_BREAK + player.position
        infoList = findViewById(R.id.player_info_list)
        commentList = findViewById(R.id.player_info_comment_list)
        data = ArrayList()
        commentData = ArrayList()
        adapter = SimpleAdapter(this, data,
                R.layout.list_item_player_info_layout, arrayOf(Constants.PLAYER_BASIC, Constants.PLAYER_INFO),
                intArrayOf(R.id.player_basic, R.id.player_info))
        commentAdapter = CommentAdapter(this, commentData,
                R.layout.list_item_comment_layout, arrayOf(Constants.COMMENT_AUTHOR, Constants.COMMENT_CONTENT,
                Constants.COMMENT_TIMESTAMP, Constants.COMMENT_ID, Constants.COMMENT_REPLY_ID,
                Constants.COMMENT_REPLY_DEPTH, Constants.COMMENT_UPVOTE_IMAGE, Constants.COMMENT_UPVOTE_COUNT,
                Constants.COMMENT_DOWNVOTE_IMAGE, Constants.COMMENT_DOWNVOTE_COUNT),
                intArrayOf(R.id.comment_author, R.id.comment_content, R.id.comment_timestamp, R.id.comment_id, R.id.comment_reply_id,
                R.id.comment_reply_depth, R.id.comment_upvoted_icon, R.id.comment_upvote_count, R.id.comment_downvoted_icon,
                R.id.comment_downvote_count))
        infoList!!.adapter = adapter
        commentList!!.adapter = commentAdapter
        val detector = PlayerInfoSwipeDetector(this)
        infoList!!.setOnTouchListener(detector)
        commentList!!.setOnTouchListener(detector)
        val footerView = LayoutInflater.from(this).inflate(R.layout.tag_footer_view, null) as RelativeLayout
        chipCloud = footerView.findViewById(R.id.chip_cloud)
        infoList!!.addFooterView(footerView)

        // to handle a race condition, we'll try to set the chip cloud if tags have been fetched but it hasn't yet
        if (!chipsDisplayed && playerTags != null) {
            setChipCloud()
        }
        val ranks = findViewById<ImageButton>(R.id.player_info_ranks)
        val info = findViewById<ImageButton>(R.id.player_info_about)
        val team = findViewById<ImageButton>(R.id.player_info_team)
        val news = findViewById<ImageButton>(R.id.player_info_news)
        val comments = findViewById<ImageButton>(R.id.player_info_comments)
        ranks.setOnClickListener { displayRanks() }
        info.setOnClickListener { displayInfo() }
        team.setOnClickListener { displayTeam() }
        news.setOnClickListener { displayNews() }
        comments.setOnClickListener { displayComments() }
        infoList!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
            val subView = view.findViewById<TextView>(R.id.player_info)
            if (Constants.NOTE_SUB == subView.text.toString()) {
                val existing = (view.findViewById<View>(R.id.player_basic) as TextView).text.toString()
                getNote(existing)
            }
        }
        infoList!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
            val subView = view.findViewById<TextView>(R.id.player_info)
            if (Constants.NOTE_SUB == subView.text.toString()) {
                setNoteAndDisplayIt("")
            }
            true
        }
        commentList!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
            val author = (view.findViewById<View>(R.id.comment_author) as TextView).text.toString()
            if (currUser == author) {
                val id = (view.findViewById<View>(R.id.comment_id) as TextView).text.toString()
                confirmCommentDeletion(id)
            }
            true
        }
        displayRanks()
    }

    fun setAggregatePlayerMetadata(viewCount: Int, watchCount: Int, draftCount: Int, tags: List<Tag>?,
                                   userTags: MutableList<String>) {
        this.watchCount = watchCount
        this.viewCount = viewCount
        this.draftCount = draftCount
        this.userTags = userTags
        setTags(tags)
    }

    fun setTags(tags: List<Tag>?) {
        playerTags = tags
        if (chipCloud != null) {
            // There's a race condition that could cause an npe here if chip cloud hasn't been looked up yet.
            // So, we only display if it has been, and set a flag to true at the end. When the view is fetched,
            // that flag is checked, and this called if it's not true.
            setChipCloud()
        }
    }

    private fun setChipCloud() {
        val tagArr = arrayOfNulls<String>(playerTags!!.size)
        val taggedIndices: MutableList<Int> = ArrayList()
        for (i in playerTags!!.indices) {
            val tag = playerTags!![i]
            tagArr[i] = tag.tagText
            if (userTags.contains(tag.title)) {
                taggedIndices.add(i)
            }
        }
        val activity: Activity = this
        Configure()
                .chipCloud(chipCloud)
                .selectedColor(Color.parseColor("#329AD6"))
                .selectedFontColor(Color.parseColor("#ffffff"))
                .deselectedColor(Color.parseColor("#f1f1f1"))
                .deselectedFontColor(Color.parseColor("#333333"))
                .selectTransitionMS(0)
                .deselectTransitionMS(250)
                .labels(tagArr)
                .mode(ChipCloud.Mode.MULTI)
                .allCaps(false)
                .gravity(FlowLayout.Gravity.STAGGERED)
                .textSize(resources.getDimensionPixelSize(R.dimen.default_textsize))
                .verticalSpacing(resources.getDimensionPixelSize(R.dimen.vertical_spacing))
                .minHorizontalSpacing(resources.getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                .chipListener(object : ChipListener {
                    override fun chipSelected(index: Int) {
                        val tag = playerTags!![index]
                        val text = tag.title
                        if (!userTags.contains(text)) {
                            userTags.add(text)
                            incrementTagCount(activity, playerId!!, tag, userTags)
                        }
                    }

                    override fun chipDeselected(index: Int) {
                        val tag = playerTags!![index]
                        val text = tag.title
                        if (userTags.contains(text)) {
                            userTags.remove(text)
                            decrementTagCount(activity, playerId!!, tag, userTags)
                        }
                    }
                })
                .build()
        for (index in taggedIndices) {
            chipCloud!!.setSelectedChip(index)
        }
        chipsDisplayed = true
    }

    private fun confirmCommentDeletion(commentId: String) {
        val li = LayoutInflater.from(this)
        val noteView = li.inflate(R.layout.user_input_popup, null)
        val alertDialogBuilder = AlertDialog.Builder(
                this)
        alertDialogBuilder.setView(noteView)
        val userInput = noteView
                .findViewById<EditText>(R.id.user_input_popup_input)
        userInput.visibility = View.GONE
        val title = noteView.findViewById<TextView>(R.id.user_input_popup_title)
        title.text = "Are you sure you want to delete this comment?"
        val activity: Activity = this
        alertDialogBuilder
                .setPositiveButton("Yes"
                ) { _: DialogInterface?, _: Int ->
                    deleteComment(activity, commentId)
                    hideComment(commentId)
                }
                .setNegativeButton("No"
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun hideComment(commentId: String) {
        for (datum in commentData!!) {
            if (datum!![Constants.COMMENT_ID] == commentId) {
                commentData!!.remove(datum)
                commentAdapter!!.notifyDataSetChanged()
                break
            }
        }
        val iterator = comments.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == commentId) {
                iterator.remove()
            }
        }
        if (commentData!!.size == 0) {
            displayComments()
        }
    }

    private fun getNote(existing: String) {
        val li = LayoutInflater.from(this)
        val noteView = li.inflate(R.layout.user_input_popup, null)
        val alertDialogBuilder = AlertDialog.Builder(
                this)
        alertDialogBuilder.setView(noteView)
        val userInput = noteView
                .findViewById<EditText>(R.id.user_input_popup_input)
        userInput.hint = "Player note"
        if (Constants.DEFAULT_NOTE != existing) {
            userInput.setText(existing)
        }
        val title = noteView.findViewById<TextView>(R.id.user_input_popup_title)
        title.text = "Input a note for " + player.name
        val localCopy: Activity = this
        alertDialogBuilder
                .setPositiveButton("Save"
                ) { dialog: DialogInterface, _: Int ->
                    val newNote = userInput.text.toString()
                    if (StringUtils.isBlank(newNote)) {
                        generateTextOnlyFlashbar(localCopy, "No can do", "No note given", Flashbar.Gravity.TOP)
                                .show()
                    } else {
                        setNoteAndDisplayIt(newNote)
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Cancel"
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun setNoteAndDisplayIt(newNote: String) {
        rankings.updatePlayerNote(this, player.uniqueId, newNote)
        displayInfo()
    }

    private fun displayRanks() {
        data!!.clear()
        commentData!!.clear()
        commentList!!.visibility = View.GONE
        infoList!!.visibility = View.VISIBLE
        chipCloud!!.visibility = View.GONE
        val ranks = findViewById<View>(R.id.ranks_button_selected)
        val playerSelected = findViewById<View>(R.id.player_info_button_selected)
        val team = findViewById<View>(R.id.team_info_button_selected)
        val news = findViewById<View>(R.id.news_button_selected)
        val comments = findViewById<View>(R.id.comment_button_selected)
        findViewById<View>(R.id.comment_input_base).visibility = View.GONE
        ranks.visibility = View.VISIBLE
        playerSelected.visibility = View.INVISIBLE
        team.visibility = View.INVISIBLE
        news.visibility = View.INVISIBLE
        comments.visibility = View.INVISIBLE
        val ecr: MutableMap<String, String?> = HashMap()
        ecr[Constants.PLAYER_BASIC] = "ECR: " + if (player.ecr == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.ecr
        if (player.ecr != Constants.DEFAULT_RANK) {
            val ecrRank = getEcr(null, player.ecr)
            val ecrRankPos = getEcr(player.position, player.ecr)
            var ecrSub = getRankingSub(ecrRank, ecrRankPos)
            if (player.risk != null && (rankings.leagueSettings.isAuction || rankings.leagueSettings.isSnake)) {
                ecrSub += Constants.LINE_BREAK + "Risk: " + player.risk
            }
            ecr[Constants.PLAYER_INFO] = ecrSub
        }
        data!!.add(ecr)
        val adp: MutableMap<String, String?> = HashMap()
        adp[Constants.PLAYER_BASIC] = "ADP: " + if (player.adp == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.adp
        var adpSub = StringBuilder()
        if (player.adp != Constants.DEFAULT_RANK) {
            val adpRank = getAdp(null, player.adp)
            val adpPos = getAdp(player.position, player.adp)
            adpSub = StringBuilder(getRankingSub(adpRank, adpPos))
        }
        val draftedPlayers = rankings.draft.draftedPlayers.size
        if (draftedPlayers > 0) {
            if (adpSub.isNotEmpty()) {
                adpSub.append(Constants.LINE_BREAK)
            }
            adpSub.append("Current draft position: ")
                    .append(draftedPlayers + 1)
        }
        adp[Constants.PLAYER_INFO] = adpSub.toString()
        data!!.add(adp)
        val auc: MutableMap<String, String?> = HashMap()
        auc[Constants.PLAYER_BASIC] = "Auction Value: $" + Constants.DECIMAL_FORMAT.format(player.getAuctionValueCustom(rankings))
        val aucRank = getAuc(null, player.auctionValue)
        val aucPos = getAuc(player.position, player.auctionValue)
        val auctionSub = getRankingSub(aucRank, aucPos) +
                Constants.LINE_BREAK +
                leverage
        auc[Constants.PLAYER_INFO] = auctionSub
        data!!.add(auc)
        val dynasty: MutableMap<String, String?> = HashMap()
        dynasty[Constants.PLAYER_BASIC] = "Dynasty/Keeper Ranking: " + if (player.dynastyRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.dynastyRank
        if (player.dynastyRank != Constants.DEFAULT_RANK) {
            val dynRank = getDynasty(null, player.dynastyRank)
            val dynRankPos = getDynasty(player.position, player.dynastyRank)
            var dynSub = getRankingSub(dynRank, dynRankPos)
            if (player.risk != null && rankings.leagueSettings.isDynasty) {
                dynSub += Constants.LINE_BREAK + "Risk: " + player.risk
            }
            dynasty[Constants.PLAYER_INFO] = dynSub
        }
        data!!.add(dynasty)
        if (player.rookieRank != Constants.DEFAULT_RANK) {
            // 300.0 is the default, so this is basically 'is it set?'
            val rookie: MutableMap<String, String?> = HashMap()
            rookie[Constants.PLAYER_BASIC] = "Rookie Rankings: " + player.rookieRank
            val rookieRank = getRookie(null, player.rookieRank)
            val rookieRankPos = getRookie(player.position, player.rookieRank)
            var rookieSub = getRankingSub(rookieRank, rookieRankPos)
            if (player.risk != null && rankings.leagueSettings.isRookie) {
                rookieSub += Constants.LINE_BREAK + "Risk: " + player.risk
            }
            rookie[Constants.PLAYER_INFO] = rookieSub
            data!!.add(rookie)
        }
        val bestBall: MutableMap<String, String?> = HashMap()
        bestBall[Constants.PLAYER_BASIC] = "Best Ball Ranking: " + if (player.bestBallRank == Constants.DEFAULT_RANK) Constants.DEFAULT_DISPLAY_RANK_NOT_SET else player.bestBallRank
        if (player.bestBallRank != Constants.DEFAULT_RANK) {
            val bbRank = getBestBall(null, player.bestBallRank)
            val bbRankPos = getBestBall(player.position, player.bestBallRank)
            var bbSub = getRankingSub(bbRank, bbRankPos)
            if (player.risk != null && rankings.leagueSettings.isBestBall) {
                bbSub += Constants.LINE_BREAK + "Risk: " + player.risk
            }
            bestBall[Constants.PLAYER_INFO] = bbSub
        }
        data!!.add(bestBall)
        if (player.projection != null) {
            val proj: MutableMap<String, String?> = HashMap()
            proj[Constants.PLAYER_BASIC] = "Projection: " + player.projection
            val projRank = getProj(null, player.projection)
            val projPos = getProj(player.position, player.projection)
            val projectionBreakdown = player.playerProjection.getDisplayString(player.position)
            val rankingSub = getRankingSub(projRank, projPos) +
                    (if (projectionBreakdown.isNotEmpty()) Constants.LINE_BREAK else "") +
                    (if (projectionBreakdown.isNotEmpty()) Constants.LINE_BREAK else "") +
                    projectionBreakdown
            proj[Constants.PLAYER_INFO] = rankingSub
            data!!.add(proj)
            val paa: MutableMap<String, String?> = HashMap()
            paa[Constants.PLAYER_BASIC] = "PAA: " + Constants.DECIMAL_FORMAT.format(player.paa)
            val paaRank = getPaa(null, player.paa)
            val paaPos = getPaa(player.position, player.paa)
            var subRank = getRankingSub(paaRank, paaPos)
            if (player.getAuctionValueCustom(rankings) > 0.0) {
                subRank += Constants.LINE_BREAK + "PAA/$: " + Constants.DECIMAL_FORMAT.format(player.paa / player.getAuctionValueCustom(rankings))
            }
            subRank += Constants.LINE_BREAK + "Scaled PAA: " + Constants.DECIMAL_FORMAT.format(player.getScaledPAA(rankings))
            paa[Constants.PLAYER_INFO] = subRank
            data!!.add(paa)
            val xVal: MutableMap<String, String?> = HashMap()
            xVal[Constants.PLAYER_BASIC] = "X Value: " + Constants.DECIMAL_FORMAT.format(player.xval)
            val xValRank = getXVal(null, player.xval)
            val xValPos = getXVal(player.position, player.xval)
            var xValSub = getRankingSub(xValRank, xValPos)
            if (player.getAuctionValueCustom(rankings) > 0.0) {
                xValSub += Constants.LINE_BREAK + "X Value/$: " + Constants.DECIMAL_FORMAT.format(player.xval / player.getAuctionValueCustom(rankings))
            }
            xValSub += Constants.LINE_BREAK + "Scaled X Value: " + Constants.DECIMAL_FORMAT.format(player.getScaledXVal(rankings))
            xVal[Constants.PLAYER_INFO] = xValSub
            data!!.add(xVal)
            val voLS: MutableMap<String, String?> = HashMap()
            voLS[Constants.PLAYER_BASIC] = "VOLS: " + Constants.DECIMAL_FORMAT.format(player.vols)
            val voLSRank = getVoLSRank(null, player.vols)
            val voLSPos = getVoLSRank(player.position, player.vols)
            var voLSSub = getRankingSub(voLSRank, voLSPos)
            if (player.getAuctionValueCustom(rankings) > 0.0) {
                voLSSub += Constants.LINE_BREAK + "VOLS/$: " + Constants.DECIMAL_FORMAT.format(player.vols / player.getAuctionValueCustom(rankings))
            }
            voLSSub += Constants.LINE_BREAK + "Scaled VOLS: " + Constants.DECIMAL_FORMAT.format(player.getScaledVOLS(rankings))
            voLS[Constants.PLAYER_INFO] = voLSSub
            data!!.add(voLS)
        }
        if (!StringUtils.isBlank(player.stats)) {
            val stats: MutableMap<String, String?> = HashMap()
            stats[Constants.PLAYER_BASIC] = Constants.LAST_YEAR_KEY + " Stats"
            stats[Constants.PLAYER_INFO] = player.stats
            data!!.add(stats)
        }
        val lastUpdated = LocalSettingsHelper.getLastRankingsFetchedDate(this)
        if (Constants.NOT_SET_KEY != lastUpdated) {
            val lastUpdatedMap: MutableMap<String, String?> = HashMap()
            lastUpdatedMap[Constants.PLAYER_BASIC] = "Rankings Freshness"
            lastUpdatedMap[Constants.PLAYER_INFO] = "Last updated $lastUpdated"
            data!!.add(lastUpdatedMap)
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun displayInfo() {
        data!!.clear()
        commentData!!.clear()
        commentList!!.visibility = View.GONE
        infoList!!.visibility = View.VISIBLE
        chipCloud!!.visibility = View.VISIBLE
        infoList!!.setSelection(0)
        val ranks = findViewById<View>(R.id.ranks_button_selected)
        val playerSelected = findViewById<View>(R.id.player_info_button_selected)
        val teamInfo = findViewById<View>(R.id.team_info_button_selected)
        val news = findViewById<View>(R.id.news_button_selected)
        val comments = findViewById<View>(R.id.comment_button_selected)
        findViewById<View>(R.id.comment_input_base).visibility = View.GONE
        ranks.visibility = View.INVISIBLE
        playerSelected.visibility = View.VISIBLE
        teamInfo.visibility = View.INVISIBLE
        news.visibility = View.INVISIBLE
        comments.visibility = View.INVISIBLE
        val context: MutableMap<String, String?> = HashMap()
        context[Constants.PLAYER_BASIC] = "Current status"
        val playerSub = StringBuilder()
        if (rankings.isPlayerWatched(player.uniqueId)) {
            playerSub.append("In your watch list").append(Constants.LINE_BREAK)
        }
        if (rankings.draft.isDrafted(player)) {
            if (rankings.draft.isDraftedByMe(player)) {
                playerSub.append("On your team")
            } else {
                playerSub.append("On another team")
            }
        } else {
            playerSub.append("Available")
                    .append(Constants.LINE_BREAK)
            if (rankings.draft.myPlayers.isNotEmpty()) {
                // TODO: should this print more specific info? Names?
                val sameBye = rankings.draft.getPlayersWithSameBye(player, rankings)
                if (sameBye.size != 1) {
                    playerSub.append("Same bye as ")
                            .append(sameBye.size)
                            .append(" players on your team")
                } else {
                    playerSub.append("Same bye as ")
                            .append(sameBye.size)
                            .append(" player on your team")
                }
                if (sameBye.isNotEmpty()) {
                    // No sense printing that it's the same as no players AND no <position>s
                    playerSub.append(Constants.LINE_BREAK)
                    val sameByeAndPos = rankings.draft.getPlayersWithSameByeAndPos(player, rankings)
                    if (sameByeAndPos.size > 1) {
                        playerSub.append("Same bye as ")
                                .append(sameByeAndPos.size)
                                .append(" ")
                                .append(player.position)
                                .append("s on your team")
                    } else {
                        playerSub.append("Same bye as ")
                                .append(sameByeAndPos.size)
                                .append(" ")
                                .append(player.position)
                                .append(" on your team")
                    }
                }
            }
        }
        var playerStatus = playerSub.toString()
        if (playerStatus.endsWith(Constants.LINE_BREAK)) {
            playerStatus = playerStatus.substring(0, playerStatus.length - 1)
        }
        context[Constants.PLAYER_INFO] = playerStatus
        data!!.add(context)
        if (StringUtils.isBlank(rankings.getPlayerNote(player.uniqueId))) {
            val note: MutableMap<String, String?> = HashMap()
            note[Constants.PLAYER_BASIC] = Constants.DEFAULT_NOTE
            note[Constants.PLAYER_INFO] = Constants.NOTE_SUB
            data!!.add(note)
        } else {
            val note: MutableMap<String, String?> = HashMap()
            note[Constants.PLAYER_BASIC] = rankings.getPlayerNote(player.uniqueId)
            note[Constants.PLAYER_INFO] = Constants.NOTE_SUB
            data!!.add(note)
        }
        val injury: MutableMap<String, String?> = HashMap()
        injury[Constants.PLAYER_BASIC] = "Player availability"
        if (!StringUtils.isBlank(player.injuryStatus)) {
            injury[Constants.PLAYER_INFO] = player.injuryStatus
        } else {
            injury[Constants.PLAYER_INFO] = "Healthy"
        }
        data!!.add(injury)
        if (Constants.DEFAULT_RANK != player.adp) {
            val nearbyPlayers = playersDraftedNearby
            val nearbyData: MutableMap<String, String?> = HashMap()
            nearbyData[Constants.PLAYER_BASIC] = "Players drafted nearby"
            var nearbyString = StringBuilder()
            for (nearPlayer in nearbyPlayers) {
                nearbyString = nearbyString
                        .append(if (nearPlayer.adp < player.adp) "" else "+")
                        .append(
                                Constants.DECIMAL_FORMAT.format(nearPlayer.adp - player.adp))
                        .append(": ")
                        .append(nearPlayer.name)
                        .append(" - ")
                        .append(nearPlayer.position)
                        .append(", ")
                        .append(nearPlayer.teamName)
                        .append(Constants.LINE_BREAK)
            }
            nearbyData[Constants.PLAYER_INFO] = nearbyString.toString()
            data!!.add(nearbyData)
        }
        if (viewCount > 0) {
            val activityData: MutableMap<String, String?> = HashMap()
            activityData[Constants.PLAYER_BASIC] = "Player popularity"
            val activityString = "" +
                    viewCount +
                    (if (viewCount > 1) " views" else " view") +
                    Constants.LINE_BREAK +
                    "In " +
                    watchCount +
                    (if (watchCount == 1) " watch list" else " watch lists") +
                    Constants.LINE_BREAK +
                    "Drafted " +
                    draftCount +
                    if (draftCount == 1) " time" else " times"
            activityData[Constants.PLAYER_INFO] = activityString
            data!!.add(activityData)
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun displayTeam() {
        data!!.clear()
        commentData!!.clear()
        commentList!!.visibility = View.GONE
        infoList!!.visibility = View.VISIBLE
        chipCloud!!.visibility = View.GONE
        infoList!!.setSelection(0)
        val ranks = findViewById<View>(R.id.ranks_button_selected)
        val playerSelected = findViewById<View>(R.id.player_info_button_selected)
        val teamInfo = findViewById<View>(R.id.team_info_button_selected)
        val news = findViewById<View>(R.id.news_button_selected)
        val comments = findViewById<View>(R.id.comment_button_selected)
        findViewById<View>(R.id.comment_input_base).visibility = View.GONE
        ranks.visibility = View.INVISIBLE
        playerSelected.visibility = View.INVISIBLE
        teamInfo.visibility = View.VISIBLE
        news.visibility = View.INVISIBLE
        comments.visibility = View.INVISIBLE
        val team = rankings.getTeam(player)
        if (team == null || Constants.NO_TEAM == player.teamName) {
            val datum: MutableMap<String, String?> = HashMap()
            datum[Constants.PLAYER_BASIC] = "No info available for this team."
            datum[Constants.PLAYER_INFO] = "Please try another player, or refresh your rankings."
            data!!.add(datum)
        } else {
            if (!StringUtils.isBlank(team.draftClass) && team.draftClass!!.length > 4) {
                val draft: MutableMap<String, String?> = HashMap()
                draft[Constants.PLAYER_BASIC] = "Draft recap"
                draft[Constants.PLAYER_INFO] = team.draftClass
                data!!.add(draft)
            } else {
                Log.d(TAG, "No draft class to display")
            }
            if (!StringUtils.isBlank(team.oLineRanks) && team.oLineRanks!!.length > 4) {
                val oline: MutableMap<String, String?> = HashMap()
                oline[Constants.PLAYER_BASIC] = "Offensive line grades"
                oline[Constants.PLAYER_INFO] = team.oLineRanks
                data!!.add(oline)
            } else {
                Log.d(TAG, "No o line ranks to display")
            }
            val schedule: MutableMap<String, String?> = HashMap()
            schedule[Constants.PLAYER_BASIC] = Constants.YEAR_KEY + " schedule"
            schedule[Constants.PLAYER_INFO] = (team.schedule + Constants.LINE_BREAK + Constants.LINE_BREAK
                    + "Positional SOS: " + team.getSosForPosition(player.position) + Constants.LINE_BREAK
                    + "A lower SOS reflects a harder schedule")
            data!!.add(schedule)
        }
        if (!StringUtils.isBlank(team!!.faClass) && team.faClass!!.length > 4) {
            val incomingFa: MutableMap<String, String?> = HashMap()
            incomingFa[Constants.PLAYER_BASIC] = "Incoming Players"
            incomingFa[Constants.PLAYER_INFO] = team.incomingFA
            data!!.add(incomingFa)
            val outgoingFa: MutableMap<String, String?> = HashMap()
            outgoingFa[Constants.PLAYER_BASIC] = "Outgoing Players"
            outgoingFa[Constants.PLAYER_INFO] = team.outgoingFA
            data!!.add(outgoingFa)
        } else {
            Log.d(TAG, "No FA class to display")
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun displayNews() {
        data!!.clear()
        commentData!!.clear()
        commentList!!.visibility = View.GONE
        infoList!!.visibility = View.VISIBLE
        chipCloud!!.visibility = View.GONE
        infoList!!.setSelection(0)
        val ranks = findViewById<View>(R.id.ranks_button_selected)
        val playerSelected = findViewById<View>(R.id.player_info_button_selected)
        val team = findViewById<View>(R.id.team_info_button_selected)
        val newsInfo = findViewById<View>(R.id.news_button_selected)
        val comments = findViewById<View>(R.id.comment_button_selected)
        findViewById<View>(R.id.comment_input_base).visibility = View.GONE
        ranks.visibility = View.INVISIBLE
        playerSelected.visibility = View.INVISIBLE
        team.visibility = View.INVISIBLE
        newsInfo.visibility = View.VISIBLE
        comments.visibility = View.INVISIBLE
        if (playerNews == null || playerNews!!.isEmpty()) {
            val news: MutableMap<String, String?> = HashMap()
            news[Constants.PLAYER_BASIC] = "No news available for this player"
            news[Constants.PLAYER_INFO] = "Try a different player, or ensure you have a valid internet connection"
            data!!.add(news)
        } else {
            for (newsItem in playerNews!!) {
                val news: MutableMap<String, String?> = HashMap()
                news[Constants.PLAYER_BASIC] = newsItem.news
                news[Constants.PLAYER_INFO] = newsItem.impact
                data!!.add(news)
            }
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun displayComments() {
        data!!.clear()
        commentData!!.clear()
        // A useless line of code, but Android gets pissy thanks to use of the footerview in the other tab
        adapter!!.notifyDataSetChanged()
        commentList!!.visibility = View.VISIBLE
        infoList!!.visibility = View.GONE
        chipCloud!!.visibility = View.GONE
        if (doUpdateImage) {
            (findViewById<View>(R.id.player_info_comments) as ImageButton).setImageResource(R.drawable.comment_white)
            doUpdateImage = false
        }
        commentList!!.setSelection(0)
        val ranks = findViewById<View>(R.id.ranks_button_selected)
        val playerSelected = findViewById<View>(R.id.player_info_button_selected)
        val team = findViewById<View>(R.id.team_info_button_selected)
        val newsInfo = findViewById<View>(R.id.news_button_selected)
        val commentsView = findViewById<View>(R.id.comment_button_selected)
        findViewById<View>(R.id.comment_input_base).visibility = View.VISIBLE
        ranks.visibility = View.INVISIBLE
        playerSelected.visibility = View.INVISIBLE
        team.visibility = View.INVISIBLE
        newsInfo.visibility = View.INVISIBLE
        commentsView.visibility = View.VISIBLE
        for (comment in comments) {
            commentData!!.add(getCommentDatum(comment))
        }
        if (comments.size == 0) {
            val emptyMap: MutableMap<String?, String?> = HashMap()
            emptyMap[Constants.COMMENT_CONTENT] = "No comments exist for this player. Be the first to post!"
            commentData!!.add(emptyMap)
        }
        var moreFound: Boolean
        val seenComments: MutableSet<String?> = HashSet()
        do {
            moreFound = false
            for (i in commentData!!.indices) {
                val datum: Map<String?, String?>? = commentData!![i]
                val commentId = datum!![Constants.COMMENT_ID]
                if (replyMap.containsKey(commentId) && !seenComments.contains(commentId)) {
                    seenComments.add(commentId)
                    moreFound = true
                    val replies: List<Comment>? = replyMap[commentId]
                    var newIndex = i + 1
                    for (comment in replies!!) {
                        commentData!!.add(newIndex++, getCommentDatum(comment))
                    }
                }
            }
        } while (moreFound)
        commentAdapter!!.notifyDataSetChanged()
        val input = findViewById<EditText>(R.id.player_info_comment_input)
        val submit = findViewById<ImageButton>(R.id.player_info_comment_submit)
        val activity: Activity = this
        submit.setOnClickListener {
            val commentContent = input.text.toString()
            if (!StringUtils.isBlank(commentContent)) {
                input.setText("")
                createComment(activity, commentContent, player.uniqueId, replyId, replyDepth)
                hideKeyboard(activity)
            }
        }
        input.setOnLongClickListener {
            input.setText("")
            resetReplyContext()
            hideKeyboard(activity)
            true
        }
    }

    private fun getCommentDatum(comment: Comment): MutableMap<String?, String?> {
        val commentMap: MutableMap<String?, String?> = HashMap()
        commentMap[Constants.COMMENT_AUTHOR] = comment.author
        commentMap[Constants.COMMENT_CONTENT] = comment.content
        commentMap[Constants.COMMENT_TIMESTAMP] = comment.time
        commentMap[Constants.COMMENT_ID] = comment.id
        commentMap[Constants.COMMENT_REPLY_DEPTH] = comment.replyDepth.toString()
        commentMap[Constants.COMMENT_REPLY_ID] = comment.replyToId
        commentMap[Constants.COMMENT_UPVOTE_COUNT] = comment.upvotes.toString()
        commentMap[Constants.COMMENT_DOWNVOTE_COUNT] = comment.downvotes.toString()
        when {
            comment.isUpvoted -> {
                commentMap[Constants.COMMENT_UPVOTE_IMAGE] = R.drawable.upvoted.toString()
                commentMap[Constants.COMMENT_DOWNVOTE_IMAGE] = R.drawable.not_downvoted.toString()
            }
            comment.isDownvoted -> {
                commentMap[Constants.COMMENT_UPVOTE_IMAGE] = R.drawable.not_upvoted.toString()
                commentMap[Constants.COMMENT_DOWNVOTE_IMAGE] = R.drawable.downvoted.toString()
            }
            else -> {
                commentMap[Constants.COMMENT_UPVOTE_IMAGE] = R.drawable.not_upvoted.toString()
                commentMap[Constants.COMMENT_DOWNVOTE_IMAGE] = R.drawable.not_downvoted.toString()
            }
        }
        return commentMap
    }

    fun updateReplyContext(replyDepth: Int, replyId: String, newHint: String?) {
        this.replyId = replyId
        this.replyDepth = replyDepth
        val input = findViewById<EditText>(R.id.player_info_comment_input)
        input.hint = newHint
    }

    private fun resetReplyContext() {
        updateReplyContext(0, Constants.COMMENT_NO_REPLY_ID, "Comment")
    }

    fun conditionallyUpvoteComment(commentId: String) {
        val domainComment = findDomainComment(commentId, comments)
        for (datum in commentData!!) {
            if (datum!![Constants.COMMENT_ID] == commentId && !domainComment!!.isUpvoted) {
                datum[Constants.COMMENT_UPVOTE_IMAGE] = R.drawable.upvoted.toString()
                datum[Constants.COMMENT_DOWNVOTE_IMAGE] = R.drawable.not_downvoted.toString()
                if (domainComment.isDownvoted) {
                    var downvotes = datum[Constants.COMMENT_DOWNVOTE_COUNT]!!.toInt()
                    downvotes -= 1
                    datum[Constants.COMMENT_DOWNVOTE_COUNT] = downvotes.toString()
                }
                var upvotes = datum[Constants.COMMENT_UPVOTE_COUNT]!!.toInt()
                upvotes += 1
                datum[Constants.COMMENT_UPVOTE_COUNT] = upvotes.toString()
                commentAdapter!!.notifyDataSetChanged()
                upvoteComment(this, commentId, domainComment.isDownvoted)
                domainComment.isUpvoted = true
                domainComment.isDownvoted = false
                break
            }
        }
    }

    private fun findDomainComment(commentId: String, comments: List<Comment>): Comment? {
        // bfs recursive traversal down the comment tree
        val nextIteration: MutableList<Comment> = ArrayList()
        for (comment in comments) {
            if (comment.id == commentId) {
                return comment
            } else if (replyMap.containsKey(comment.id)) {
                replyMap[comment.id]?.let { nextIteration.addAll(it) }
            }
        }
        return if (nextIteration.isEmpty()) {
            Log.d(TAG, "Comment wasn't found, returning null (this will go boom)")
            null
        } else {
            findDomainComment(commentId, nextIteration)
        }
    }

    fun conditionallyDownvoteComment(commentId: String) {
        val domainComment = findDomainComment(commentId, comments)
        for (datum in commentData!!) {
            if (datum!![Constants.COMMENT_ID] == commentId && !domainComment!!.isDownvoted) {
                datum[Constants.COMMENT_UPVOTE_IMAGE] = R.drawable.not_upvoted.toString()
                datum[Constants.COMMENT_DOWNVOTE_IMAGE] = R.drawable.downvoted.toString()
                if (domainComment.isUpvoted) {
                    var upvotes = datum[Constants.COMMENT_UPVOTE_COUNT]!!.toInt()
                    upvotes -= 1
                    datum[Constants.COMMENT_UPVOTE_COUNT] = upvotes.toString()
                }
                var downvotes = datum[Constants.COMMENT_DOWNVOTE_COUNT]!!.toInt()
                downvotes += 1
                datum[Constants.COMMENT_DOWNVOTE_COUNT] = downvotes.toString()
                commentAdapter!!.notifyDataSetChanged()
                downvoteComment(this, commentId, domainComment.isUpvoted)
                domainComment.isUpvoted = false
                domainComment.isDownvoted = true
                break
            }
        }
    }

    fun updateVoteCount(commentId: String, upvotes: Int, downvotes: Int) {
        for (comment in commentData!!) {
            if (comment!![Constants.COMMENT_ID] == commentId) {
                comment[Constants.COMMENT_UPVOTE_COUNT] = upvotes.toString()
                comment[Constants.COMMENT_DOWNVOTE_COUNT] = downvotes.toString()
                commentAdapter!!.notifyDataSetChanged()
                break
            }
        }
    }

    fun populateNews(fetchedNews: List<PlayerNews>) {
        playerNews = fetchedNews
        val newsView = findViewById<View>(R.id.news_button_selected)
        if (View.VISIBLE == newsView.visibility) {
            displayNews()
        }
    }

    fun addComments(comments: List<Comment>, nextToken: String?) {
        val newComments: MutableList<Comment> = ArrayList()
        var maxDepth = 0
        for (comment in comments) {
            if (comment.replyDepth!! > maxDepth) {
                maxDepth = comment.replyDepth!!
            }
            if (comment.replyDepth!! > 0) {
                if (replyMap.containsKey(comment.replyToId)) {
                    replyMap[comment.replyToId]!!.add(comment)
                } else {
                    val repliesList: MutableList<Comment> = ArrayList()
                    repliesList.add(comment)
                    replyMap[comment.replyToId] = repliesList
                }
            } else {
                newComments.add(comment)
            }
        }
        this.comments.addAll(newComments)
        if (sortByUpvotes) {
            this.comments.sortBy { it.upvotes!! }

            for (replies in replyMap.values) {
                replies?.sortBy { it.upvotes!! }
            }
        }
        val commentsView = findViewById<View>(R.id.comment_button_selected)
        if (View.VISIBLE == commentsView.visibility) {
            displayComments()
        }
        if (comments.size > LocalSettingsHelper.getNumberOfCommentsOnPlayer(this, player.uniqueId)) {
            (findViewById<View>(R.id.player_info_comments) as ImageButton).setImageResource(R.drawable.new_comment_white)
            doUpdateImage = true
            LocalSettingsHelper.setNumberOfCommentsOnPlayer(this, player.uniqueId, comments.size)
        }
        if (!StringUtils.isBlank(nextToken)) {
            getCommentsForPlayer(this, player.uniqueId, nextToken, sortByUpvotes)
        }
    }

    fun giveCommentInputFocus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private val leverage: String
        get() = "Leverage: " +
                ParseMath.getLeverage(player, rankings)

    private fun getRankingSub(rank: Int, posRank: Int): String {
        return "Ranked " +
                posRank +
                " positionally, " +
                rank +
                " overall"
    }

    private fun getEcr(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.ecr < source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getAdp(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.adp < source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getDynasty(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.dynastyRank < source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getBestBall(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.bestBallRank < source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getRookie(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.rookieRank < source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getAuc(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.auctionValue > source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getProj(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.projection > source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getPaa(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.paa > source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getXVal(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.xval > source) {
                    rank++
                }
            }
        }
        return rank
    }

    private fun getVoLSRank(pos: String?, source: Double): Int {
        var rank = 1
        for (key in rankings.players.keys) {
            val player = rankings.getPlayer(key)
            if (pos == null || pos == player.position) {
                if (player.vols > source) {
                    rank++
                }
            }
        }
        return rank
    }

    // First, sort all players by nearest adp to player
    private val playersDraftedNearby: List<Player>
        get() {

            // First, sort all players by nearest adp to player
            val comparator = Comparator { a: Player, b: Player ->
                val diffA = abs((a.adp - player.adp).toInt())
                val diffB = abs((b.adp - player.adp).toInt())
                diffA.compareTo(diffB)
            }
            val allPlayers: List<Player> = ArrayList(rankings.players.values)
            Collections.sort(allPlayers, comparator)

            // Next, we want to get the x nearest, instead of hundreds.
            var listSize = 0
            val nearestPlayers: MutableList<Player> = ArrayList()
            for (i in allPlayers.indices) {
                val possiblePlayer = allPlayers[i]
                if (listSize == MAX_NEARBY_PLAYERS) {
                    break
                } else if (possiblePlayer.uniqueId == player.uniqueId ||
                        !rankings.leagueSettings.rosterSettings.isPositionValid(possiblePlayer.position)) {
                    continue
                }
                nearestPlayers.add(possiblePlayer)
                listSize++
            }

            // Finally, we want to sort these by adp overall.
            val adpComparator = Comparator { a: Player, b: Player -> a.adp.compareTo(b.adp) }
            Collections.sort(nearestPlayers, adpComparator)
            return nearestPlayers
        }

    companion object {
        private const val TAG = "PlayerInfo"
        private lateinit var playerId: String
        private const val MAX_NEARBY_PLAYERS = 6
    }
}