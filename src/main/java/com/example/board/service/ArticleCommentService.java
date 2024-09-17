package com.example.board.service;

import com.example.board.entity.Article;
import com.example.board.entity.Comment;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ArticleCommentService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public ArticleCommentService(UserRepository userRepository, BoardRepository boardRepository, ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    public CompletableFuture<Article> execute(String username, Long boardId, Long articleId) throws BadRequestException {
        CompletableFuture<Article> articleFuture = this.getArticle(username, boardId, articleId);
        CompletableFuture<List<Comment>> commentsFuture = this.getComments(articleId);

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(articleFuture, commentsFuture);
        return combinedFuture.thenApply(voidResult -> {
            try {
                Article article = articleFuture.get();
                List<Comment> comments = commentsFuture.get();

                article.setComments(comments);

                return article;
            } catch(InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Async
    @Transactional
    protected CompletableFuture<Article> getArticle(String username, Long boardId, Long articleId) throws BadRequestException {
        this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        if (article.getIsDeleted()) {
            throw new BadRequestException("Article is deleted.");
        }

        article.setViewCount(article.getViewCount() + 1);
        this.articleRepository.save(article);

        return CompletableFuture.completedFuture(article);
    }

    @Async
    protected CompletableFuture<List<Comment>> getComments(Long articleId) throws BadRequestException {
        List<Comment> comments =  this.commentRepository.findByArticleIdAndIsDeletedFalse(articleId);
        return CompletableFuture.completedFuture(comments);
    }
}
