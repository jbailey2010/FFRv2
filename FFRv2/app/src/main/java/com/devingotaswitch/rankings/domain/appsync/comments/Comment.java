package com.devingotaswitch.rankings.domain.appsync.comments;

public class Comment {
    private String author;
    private String time;
    private String content;
    private String id;
    private String playerId;
    private Integer upvotes;
    private Integer downvotes;
    private String replyToId;
    private Integer replyDepth;
    private boolean isUpvoted;
    private boolean isDownvoted;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public Integer getReplyDepth() {
        return replyDepth;
    }

    public void setReplyDepth(Integer replyDepth) {
        this.replyDepth = replyDepth;
    }

    public boolean isUpvoted() {
        return isUpvoted;
    }

    public void setUpvoted(boolean upvoted) {
        isUpvoted = upvoted;
    }

    public boolean isDownvoted() {
        return isDownvoted;
    }

    public void setDownvoted(boolean downvoted) {
        isDownvoted = downvoted;
    }
}
