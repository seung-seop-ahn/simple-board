package com.example.board.service;

import com.example.board.entity.Article;
import com.example.board.entity.Comment;
import com.example.board.pojo.ArticleNotification;
import com.example.board.pojo.CommentNotification;
import com.example.board.pojo.SendCommentNotification;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.CommentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserNotificationHistoryService userNotificationHistoryService;

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public RabbitMQService(RabbitTemplate rabbitTemplate, UserNotificationHistoryService userNotificationHistoryService, ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.userNotificationHistoryService = userNotificationHistoryService;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    public void send(ArticleNotification notification) {
        this.rabbitTemplate.convertAndSend("board-notification", notification.toJson());
    }

    public void send(CommentNotification notification) {
        this.rabbitTemplate.convertAndSend("board-notification", notification.toJson());
    }

    public void send(SendCommentNotification notification) {
        this.rabbitTemplate.convertAndSend("board-notification", notification.toJson());
    }

    @RabbitListener(queues = "board-notification")
    public void receive(String message) {
        if (message.contains("write_article")) {
            this.articleHistory(message);
        }
        if (message.contains("write_comment_ready")) {
            this.comment(message);
        }
        if (message.contains("write_comment")) {
            this.commentHistory(message);
        }
        System.out.println(message);
    }

    private void comment(String message) {
        try {
            CommentNotification notification = objectMapper.readValue(message, CommentNotification.class);

            Optional<Comment> comment = this.commentRepository.findById(notification.getCommentId());
            if (comment.isEmpty()) {
                return;
            }

            Long commentUserId = notification.getUserId();
            Long articleUserId = comment.get().getArticle().getAuthor().getId();

            SendCommentNotification receiver = new SendCommentNotification();
            receiver.setType("write_comment");
            receiver.setCommentId(notification.getCommentId());
            receiver.setUserId(commentUserId);

            // * to comment user
            this.send(receiver);

            // * to article user
            if (!Objects.equals(commentUserId, articleUserId)) {
                receiver.setUserId(articleUserId);
                this.send(receiver);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert json to string list:", e);
        }
    }

    private void articleHistory(String message) {
        try {
            ArticleNotification notification = objectMapper.readValue(message, ArticleNotification.class);
            Optional<Article> article = this.articleRepository.findById(notification.getArticleId());
            if (article.isEmpty()) {
                return;
            }

            this.userNotificationHistoryService.insertArticleNotification(notification.getUserId(), article.get());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert json to string list:", e);
        }
    }

    private void commentHistory(String message) {
        try {
            SendCommentNotification notification = objectMapper.readValue(message, SendCommentNotification.class);
            Optional<Comment> comment = this.commentRepository.findById(notification.getCommentId());
            if (comment.isEmpty()) {
                return;
            }

            this.userNotificationHistoryService.insertCommentNotification(notification.getUserId(), comment.get());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert json to string list:", e);
        }
    }
}
