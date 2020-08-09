package com.devingotaswitch.rankings.extras;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

public class RankingsListView extends RecyclerView {
    private Context context;
    private long lastRefreshTime = 0l;
    private boolean refreshRanksOnOverscroll = false;

    public RankingsListView(Context context) {
        super(context);
        this.context = context;
    }

    public RankingsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public RankingsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setRefreshRanksOnOverscroll(boolean refreshRanksOnOverscroll) {
        this.refreshRanksOnOverscroll = refreshRanksOnOverscroll;
    }

    @Override
    public boolean overScrollBy (int deltaX,
                                 int deltaY,
                                 int scrollX,
                                 int scrollY,
                                 int scrollRangeX,
                                 int scrollRangeY,
                                 int maxOverScrollX,
                                 int maxOverScrollY,
                                 boolean isTouchEvent) {
        if (isTouchEvent && deltaY < -25) {
            long deltaSeconds = GeneralUtils.getLatency(lastRefreshTime);
            if (deltaSeconds > Constants.OVERSCROLL_REFRESH_THRESHOLD && refreshRanksOnOverscroll) {
                lastRefreshTime = System.currentTimeMillis();
                ((RankingsHome)context).refreshRanks();
            }
        }

        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                maxOverScrollX, maxOverScrollY, isTouchEvent);
    }
}