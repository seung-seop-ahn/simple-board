package com.example.board.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentNotification extends Notification {
    private String type;
    private Long userId;
    private Long commentId;
}
