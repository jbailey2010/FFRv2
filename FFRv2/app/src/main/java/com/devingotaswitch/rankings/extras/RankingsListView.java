package com.devingotaswitch.rankings.extras;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

import com.devingotaswitch.fileio.LocalSettingsHelper;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.utils.Constants;
import com.devingotaswitch.utils.GeneralUtils;

public class RankingsListView extends ListView {
    private Context context;
    private long lastRefreshTime = 0l;

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

    public RankingsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
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
            if (deltaSeconds > Constants.OVERSCROLL_REFRESH_THRESHOLD && LocalSettingsHelper.refreshRanksOnOverscroll(context)) {
                lastRefreshTime = System.currentTimeMillis();
                ((RankingsHome)context).refreshRanks();
            }
        }

        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                maxOverScrollX, maxOverScrollY, isTouchEvent);
    }
}