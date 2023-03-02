package com.trophate.redisinaction.dto;

public class VoteDTO {

    /**
     * 投票人id
     */
    private Integer voterId;
    /**
     * 文章id
     */
    private Integer articleId;

    public Integer getVoterId() {
        return voterId;
    }

    public void setVoterId(Integer voterId) {
        this.voterId = voterId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }
}
