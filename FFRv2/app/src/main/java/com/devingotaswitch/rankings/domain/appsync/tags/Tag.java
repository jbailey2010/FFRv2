package com.devingotaswitch.rankings.domain.appsync.tags;

import com.devingotaswitch.utils.Constants;

import java.util.Set;

public abstract class Tag {
    private final int count;
    private final String title;
    private final Set<String> validPositions;

    public Tag(int count, String title, Set<String> validPositions) {
        this.count = count;
        this.title = title;
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
}