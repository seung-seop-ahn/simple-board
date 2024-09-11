package com.example.board.controller;

import com.example.board.entity.Article;
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

@RestController
@RequestMapping("/api/boards")
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(@PathVariable Long boardId, @Valid @RequestBody PostArticleDto dto) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Article article = this.articleService.writeArticle(userDetails.getUsername(), boardId, dto);
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
}
