package com.example.board.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_notification_history")
@Getter
@Setter
@NoArgsConstructor
public class UserNotificationHistory {

    @Id
    private String id;

    private String title;

    private String contents;

    private Long userId;

    private Boolean isRead = false;

    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate = LocalDateTime.now();
}
