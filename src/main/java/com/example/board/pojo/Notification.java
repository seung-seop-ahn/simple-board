package com.example.board.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public abstract class Notification implements Serializable {
    private String type;
    private Long userId;
    private Long articleId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}