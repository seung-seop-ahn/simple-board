package com.example.board.controller;

import com.example.board.dto.PostArticleDto;
import com.example.board.dto.PutArticleDto;
import com.example.board.entity.Article;
import com.example.board.service.ArticleCommentService;
import com.example.board.service.ArticleService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/boards")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleCommentService articleCommentService;

    @Autowired
    public ArticleController(ArticleService articleService, ArticleCommentService articleCommentService) {
        this.articleService = articleService;
        this.articleCommentService = articleCommentService;
    }

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> postArticle(@PathVariable Long boardId, @Valid @RequestBody PostArticleDto dto) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Article article = this.articleService.postArticle(userDetails.getUsername(), boardId, dto);
        return ResponseEntity.ok(article);
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<List<Article>> getTop10ArticleList(
            @PathVariable Long boardId,
            @RequestParam(required = false) Long firstId,
            @RequestParam(required = false) Long lastId
    ) {
        if (firstId != null) {
            List<Article> list = this.articleService.getTop10ArticleListByFirstId(boardId, firstId);
            return ResponseEntity.ok(list);
        }
        if (lastId != null) {
            List<Article> list = this.articleService.getTop10ArticleListByLastId(boardId, lastId);
            return ResponseEntity.ok(list);
        }

        List<Article> list = this.articleService.getTop10ArticleList(boardId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> getArticleWithComments(@PathVariable Long boardId, @PathVariable Long articleId) throws BadRequestException, ExecutionException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        CompletableFuture<Article> article = this.articleCommentService.execute(userDetails.getUsername(), boardId, articleId);
        return ResponseEntity.ok(article.get());
    }

    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> putArticle(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @Valid @RequestBody PutArticleDto dto) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Article article = this.articleService.putArticle(userDetails.getUsername(), boardId, articleId, dto);
        return ResponseEntity.ok(article);
    }

    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Void> putArticle(
            @PathVariable Long boardId,
            @PathVariable Long articleId
    ) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        this.articleService.deleteArticle(userDetails.getUsername(), boardId, articleId);
        return ResponseEntity.noContent().build();
    }
}
