package com.devingotaswitch.rankings.extras

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.utils.Constants
import com.devingotaswitch.utils.GeneralUtils.getLatency

class RankingsListView : RecyclerView {
    private val viewContext: Context

    constructor(context: Context) : super(context) {
        this.viewContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.viewContext = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.viewContext = context
    }

    fun setRefreshRanksOnOverscroll(refreshRanksOnOverscroll: Boolean) {
        super.setLayoutManager(RankingsLayoutManager(viewContext, refreshRanksOnOverscroll))
    }

    class RankingsLayoutManager(private var context: Context,
                                private var refreshRanksOnOverscroll: Boolean) : LinearLayoutManager(context) {
        private var lastRefreshTime = 0L

        override fun scrollVerticallyBy(dx: Int, recycler: Recycler?,
                                        state: State?): Int {
            val scrollRange = super.scrollVerticallyBy(dx, recycler, state)
            val overscroll = dx - scrollRange
            // Below zero means overscroll at the top, but a lower number is used so it isn't so sensitive.
            if (overscroll < Constants.OVERSCROLL_DEPTH_THRESHOLD) {
                val deltaSeconds = getLatency(lastRefreshTime)
                if (deltaSeconds > Constants.OVERSCROLL_REFRESH_THRESHOLD && refreshRanksOnOverscroll) {
                    lastRefreshTime = System.currentTimeMillis()
                    (context as RankingsHome).refreshRanks()
                }
            }
            return scrollRange
        }
    }
}