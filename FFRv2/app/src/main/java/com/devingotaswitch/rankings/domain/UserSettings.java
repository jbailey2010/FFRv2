package com.devingotaswitch.rankings.domain;

public class UserSettings {

    private boolean hideDraftedSearch = false;
    private boolean hideDraftedSort = false;
    private boolean hideDraftedComparator = false;

    private boolean hideRanklessSearch = false;
    private boolean hideRanklessSort = false;
    private boolean hideRanklessComparator = false;

    private boolean showNoteRank = true;
    private boolean showNoteSort = true;

    private boolean refreshOnOverscroll = false;
    private boolean sortWatchListByTime = false;

    public boolean isHideDraftedSearch() {
        return hideDraftedSearch;
    }

    public void setHideDraftedSearch(boolean hideDraftedSearch) {
        this.hideDraftedSearch = hideDraftedSearch;
    }

    public boolean isHideDraftedSort() {
        return hideDraftedSort;
    }

    public void setHideDraftedSort(boolean hideDraftedSort) {
        this.hideDraftedSort = hideDraftedSort;
    }

    public boolean isHideDraftedComparator() {
        return hideDraftedComparator;
    }

    public void setHideDraftedComparator(boolean hideDraftedComparator) {
        this.hideDraftedComparator = hideDraftedComparator;
    }

    public boolean isHideRanklessSearch() {
        return hideRanklessSearch;
    }

    public void setHideRanklessSearch(boolean hideRanklessSearch) {
        this.hideRanklessSearch = hideRanklessSearch;
    }

    public boolean isHideRanklessSort() {
        return hideRanklessSort;
    }

    public void setHideRanklessSort(boolean hideRanklessSort) {
        this.hideRanklessSort = hideRanklessSort;
    }

    public boolean isHideRanklessComparator() {
        return hideRanklessComparator;
    }

    public void setHideRanklessComparator(boolean hideRanklessComparator) {
        this.hideRanklessComparator = hideRanklessComparator;
    }

    public boolean isShowNoteRank() {
        return showNoteRank;
    }

    public void setShowNoteRank(boolean showNoteRank) {
        this.showNoteRank = showNoteRank;
    }

    public boolean isShowNoteSort() {
        return showNoteSort;
    }

    public void setShowNoteSort(boolean showNoteSort) {
        this.showNoteSort = showNoteSort;
    }

    public boolean isRefreshOnOverscroll() {
        return refreshOnOverscroll;
    }

    public void setRefreshOnOverscroll(boolean refreshOnOverscroll) {
        this.refreshOnOverscroll = refreshOnOverscroll;
    }

    public boolean isSortWatchListByTime() {
        return sortWatchListByTime;
    }

    public void setSortWatchListByTime(boolean sortByTime) {
        this.sortWatchListByTime = sortByTime;
    }
}
