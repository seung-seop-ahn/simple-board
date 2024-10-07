package com.example.board.service;

import com.example.board.config.elasticsearch.ElasticSearchService;
import com.example.board.entity.Article;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.UserRepository;
import com.example.board.scheduler.DailyHotArticle;
import com.example.board.scheduler.HotArticle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.data.redis.core.RedisTemplate;
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

    // ElasticSearch
    private final ElasticSearchService elasticSearchService;
    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    public ArticleCommentService(UserRepository userRepository, BoardRepository boardRepository, ArticleRepository articleRepository, CommentRepository commentRepository, ElasticSearchService elasticSearchService, ObjectMapper objectMapper, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.elasticSearchService = elasticSearchService;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public CompletableFuture<Article> execute(String username, Long boardId, Long articleId) throws BadRequestException, JsonProcessingException {
        CompletableFuture<Article> articleFuture = this.getArticle(username, boardId, articleId);
        CompletableFuture<List<Comment>> commentsFuture = this.getComments(articleId);

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(articleFuture, commentsFuture);
        return combinedFuture.thenApply(voidResult -> {
            try {
                Article article = articleFuture.get();
                List<Comment> comments = commentsFuture.get();

                article.setComments(comments);

                return article;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Async
    @Transactional
    protected CompletableFuture<Article> getArticle(String username, Long boardId, Long articleId) throws BadRequestException, JsonProcessingException {
        // * if article is hot article cached
        Object yesterdayHotArticle = this.redisTemplate.opsForHash().get("Yesterday:" + DailyHotArticle.REDIS_KEY, articleId);
        Object weeklyHotArticle = this.redisTemplate.opsForHash().get("Weekly:" + DailyHotArticle.REDIS_KEY, articleId);
        if (yesterdayHotArticle != null || weeklyHotArticle != null) {
            HotArticle hotArticle = (HotArticle) (yesterdayHotArticle != null ? yesterdayHotArticle : weeklyHotArticle);
            Article article = new Article();
            article.setId(hotArticle.getId());
            article.setTitle(hotArticle.getTitle());
            article.setContents(hotArticle.getContents());

            User user = new User();
            user.setUsername(hotArticle.getAuthorName());

            article.setAuthor(user);
            article.setCreatedDate(hotArticle.getCreatedDate());
            article.setUpdatedDate(hotArticle.getUpdatedDate());
            article.setViewCount(hotArticle.getViewCount());
            return CompletableFuture.completedFuture(article);
        }

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
        Article savedArticle = this.articleRepository.save(article);
        this.indexArticle(savedArticle);

        return CompletableFuture.completedFuture(savedArticle);
    }

    @Async
    protected CompletableFuture<List<Comment>> getComments(Long articleId) throws BadRequestException {
        List<Comment> comments = this.commentRepository.findByArticleIdAndIsDeletedFalse(articleId);
        return CompletableFuture.completedFuture(comments);
    }

    private void indexArticle(Article article) throws JsonProcessingException {
        String json = this.objectMapper.writeValueAsString(article);
        this.elasticSearchService.indexDocument("article", article.getId().toString(), json).block();
    }
}
