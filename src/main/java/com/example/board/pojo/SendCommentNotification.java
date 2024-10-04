package com.example.board.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendCommentNotification extends Notification {
    private String type = "write_comment";
    private Long userId;
    private Long commentId;
}
