package com.example.board.service;

import com.example.board.entity.Article;
import com.example.board.entity.Comment;
import com.example.board.entity.UserNotificationHistory;
import com.example.board.repository.UserNotificationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserNotificationHistoryService {

    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    @Autowired
    public UserNotificationHistoryService(UserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    public void insertArticleNotification(Long userId, Article article) {
        UserNotificationHistory history = new UserNotificationHistory();
        history.setTitle("Posted:" + article.getTitle());
        history.setContents(article.getContents());
        history.setUserId(userId);
        history.setIsRead(false);

        this.userNotificationHistoryRepository.save(history);
    }

    public void insertCommentNotification(Long userId, Comment comment) {
        UserNotificationHistory history = new UserNotificationHistory();
        history.setTitle("Commented");
        history.setContents(comment.getContents());
        history.setUserId(userId);
        history.setIsRead(false);

        this.userNotificationHistoryRepository.save(history);
    }

    public void readNotification(String id) {
        Optional<UserNotificationHistory> history = this.userNotificationHistoryRepository.findById(id);
        if (history.isEmpty()) {
            return;
        }

        history.get().setIsRead(true);
        history.get().setUpdatedDate(LocalDateTime.now());

        this.userNotificationHistoryRepository.save(history.get());
    }
}
