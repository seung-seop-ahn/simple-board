package com.example.board.repository;

import com.example.board.entity.UserNotificationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserNotificationHistoryRepository extends MongoRepository<UserNotificationHistory, String> {
    List<UserNotificationHistory> findByUserIdAndCreatedDateAfter(Long userId, LocalDateTime date);
}
