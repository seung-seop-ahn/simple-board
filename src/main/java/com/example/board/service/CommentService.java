package com.example.board.service;

import com.example.board.dto.PostCommentDto;
import com.example.board.dto.PutCommentDto;
import com.example.board.entity.Article;
import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.pojo.CommentNotification;
import com.example.board.pojo.Notification;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final RabbitMQService rabbitMQService;

    public CommentService(UserRepository userRepository, BoardRepository boardRepository, ArticleRepository articleRepository, CommentRepository commentRepository, RabbitMQService rabbitMQService) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.rabbitMQService = rabbitMQService;
    }

    @Transactional
    public Comment postComment(String username, Long boardId, Long articleId, PostCommentDto dto) throws BadRequestException {
//        Boolean isAvailable = this.isUserCommentPostingAvailable(username);
//        if (!isAvailable) {
//            throw new BadRequestException("Comment posting rate limit exceeded.");
//        }
        User author = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        Board board = this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor(author);
        comment.setContents(dto.getContents());

        Comment savedComment = this.commentRepository.save(comment);

        CommentNotification notification = new CommentNotification();
        notification.setType("write_comment_ready");
        notification.setCommentId(savedComment.getId());
        notification.setUserId(author.getId());

        this.rabbitMQService.send(notification);

        return savedComment;
    }

    public List<Comment> getComments(Long boardId, Long articleId) throws BadRequestException {
        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        return this.commentRepository.findByArticleIdAndIsDeletedFalse(articleId);
    }

    public Comment putComment(String username, Long boardId, Long articleId, Long commentId, PutCommentDto dto) throws BadRequestException {
        Boolean isAvailable = this.isUserCommentEditingAvailable(username);
        if (!isAvailable) {
            throw new BadRequestException("Comment editing rate limit exceeded.");
        }
        this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        Comment comment = this.commentRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("Comment not found"));

        if(comment.getIsDeleted()) {
            throw new BadRequestException("Comment is deleted.");
        }

        comment.setContents(dto.getContents());

        return this.commentRepository.save(comment);
    }

    public void deleteComment(String username, Long boardId, Long articleId, Long commentId) throws BadRequestException {
        Boolean isAvailable = this.isUserCommentEditingAvailable(username);
        if (!isAvailable) {
            throw new BadRequestException("Comment editing rate limit exceeded.");
        }

        this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        Comment comment = this.commentRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("Comment not found"));

        comment.setIsDeleted(true);

        this.commentRepository.save(comment);
    }

    private Boolean isUserCommentPostingAvailable(String username) {
        Optional<Comment> comment = this.commentRepository.findLatestCommentByAuthorUsernameOrderByCreatedDateDesc(username);
        if(comment.isEmpty()){
            return true;
        }
        LocalDateTime articleCreatedDate = comment.get().getCreatedDate();

        Duration duration = Duration.between(articleCreatedDate, LocalDateTime.now());

        return duration.toMinutes() >= 1;
    }

    private Boolean isUserCommentEditingAvailable(String username) {
        Optional<Comment> comment = this.commentRepository.findLatestEditedCommentByAuthorUsernameOrderByUpdatedDateDesc(username);
        if(comment.isEmpty()){
            return true;
        }
        LocalDateTime articleCreatedDate = comment.get().getCreatedDate();

        Duration duration = Duration.between(articleCreatedDate, LocalDateTime.now());

        return duration.toMinutes() >= 1;
    }
}
