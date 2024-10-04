package com.example.board.service;

import com.example.board.config.elasticsearch.ElasticSearchService;
import com.example.board.dto.PostArticleDto;
import com.example.board.dto.PutArticleDto;
import com.example.board.entity.Article;
import com.example.board.entity.Board;
import com.example.board.entity.User;
import com.example.board.pojo.ArticleNotification;
import com.example.board.pojo.Notification;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class ArticleService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final RabbitMQService rabbitMQService;

    // ElasticSearch
    private final ElasticSearchService elasticSearchService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ArticleService(BoardRepository boardRepository, UserRepository userRepository, ArticleRepository articleRepository, RabbitMQService rabbitMQService, ElasticSearchService elasticSearchService, ObjectMapper objectMapper) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.rabbitMQService = rabbitMQService;
        this.elasticSearchService = elasticSearchService;
        this.objectMapper = objectMapper;
    }

    public Article postArticle(String username, Long boardId, PostArticleDto dto) throws BadRequestException, JsonProcessingException {
        Boolean isAvailable = this.isUserArticlePostingAvailable(username);
        if (!isAvailable) {
            throw new BadRequestException("User posting rate limit exceeded.");
        }

        User author = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        Board board = this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = new Article();
        article.setBoard(board);
        article.setAuthor(author);
        article.setTitle(dto.getTitle());
        article.setContents(dto.getContents());

        Article savedArticle = this.articleRepository.save(article);
        this.indexArticle(savedArticle);

        Notification notification = new ArticleNotification();
        notification.setType("write_article");
        notification.setUserId(author.getId());
        notification.setArticleId(savedArticle.getId());

        this.rabbitMQService.send(notification);

        return savedArticle;
    }

    public List<Article> getTop10ArticleListByFirstId(Long boardId, Long firstId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, firstId);
    }

    public List<Article> getTop10ArticleListByLastId(Long boardId, Long lastId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, lastId);
    }

    public List<Article> getTop10ArticleList(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> search(Long boardId, String keyword) throws ExecutionException, InterruptedException {
        List<Long> ids = this.elasticSearchService.search("article", keyword);
        return this.articleRepository.findAllByIds(ids);
    }

    public Article putArticle(String username, Long boardId, Long articleId, PutArticleDto dto) throws BadRequestException, JsonProcessingException {
        Boolean isAvailable = this.isUserArticleEditingAvailable(username);
        if (!isAvailable) {
            throw new BadRequestException("User editing rate limit exceeded.");
        }

        this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        if (dto.getTitle() != null) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getContents() != null) {
            article.setContents(dto.getContents());
        }

        Article savedArticle = this.articleRepository.save(article);
        this.indexArticle(savedArticle);

        return savedArticle;
    }

    public void deleteArticle(String username, Long boardId, Long articleId) throws BadRequestException, JsonProcessingException {
        Boolean isAvailable = this.isUserArticleEditingAvailable(username);
        if (!isAvailable) {
            throw new BadRequestException("User editing rate limit exceeded.");
        }

        User author = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        if (!Objects.equals(author.getId(), article.getAuthor().getId())) {
            throw new BadRequestException("User is not article author.");
        }

        // Hard Deletion
//        this.articleRepository.delete(article);

        // Soft Deletion
        article.setIsDeleted(true);

        Article savedArticle = this.articleRepository.save(article);
        this.indexArticle(savedArticle);
    }

    private Boolean isUserArticlePostingAvailable(String username) {
        Optional<Article> article = this.articleRepository.findLatestArticleByAuthorUsernameOrderByCreatedDateDesc(username);
        if (article.isEmpty()) {
            return true;
        }
        LocalDateTime articleCreatedDate = article.get().getCreatedDate();

        Duration duration = Duration.between(articleCreatedDate, LocalDateTime.now());

        return duration.toMinutes() >= 1;
    }

    private Boolean isUserArticleEditingAvailable(String username) {
        Optional<Article> article = this.articleRepository.findLatestEditedArticleByAuthorUsernameOrderByUpdatedDateDesc(username);
        if (article.isEmpty()) {
            return true;
        }
        LocalDateTime articleCreatedDate = article.get().getUpdatedDate();

        Duration duration = Duration.between(articleCreatedDate, LocalDateTime.now());

        return duration.toMinutes() >= 1;
    }

    private void indexArticle(Article article) throws JsonProcessingException {
        String json = this.objectMapper.writeValueAsString(article);
        this.elasticSearchService.indexDocument("article", article.getId().toString(), json).block();
    }
}
