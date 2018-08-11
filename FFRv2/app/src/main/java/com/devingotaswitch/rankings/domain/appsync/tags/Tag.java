package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Set;

public abstract class Tag {
    private int count;
    private final String title;
    private final String remoteTitle;
    private final Set<String> validPositions;

    public Tag(int count, String title, String remoteTitle, Set<String> validPositions) {
        this.count = count;
        this.title = title;
        this.remoteTitle = remoteTitle;
        this.validPositions = validPositions;
    }

    public int getCount() {
        return count;
    }

    public String getTitle() {
        return title;
    }

    public boolean isValidForPosition(String pos) {
        return validPositions.contains(pos);
    }

    public String getTagText() {
        return title + Constants.TAG_TEXT_DELIMITER + String.valueOf(getCount());
    }

    public String getRemoteTitle() {
        return remoteTitle;
    }
}
