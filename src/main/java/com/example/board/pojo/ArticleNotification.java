package com.example.board.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleNotification extends Notification {
    private String type = "write_article";
    private Long userId;
    private Long articleId;
}
