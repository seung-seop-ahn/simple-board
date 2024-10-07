package com.example.board.scheduler;

import com.example.board.entity.Article;
import com.example.board.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DailyHotArticle {
    private static final String REDIS_KEY = "HotArticle";

    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public DailyHotArticle(ArticleRepository articleRepository, RedisTemplate<String, Object> redisTemplate) {
        this.articleRepository = articleRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void pickYesterdayHotArticle() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        // todo: boardId
        Article result = this.articleRepository.findHotArticle(1L, startDate, endDate);
        if (result == null) {
            return;
        }

        HotArticle hotArticle = new HotArticle();
        hotArticle.setId(result.getId());
        hotArticle.setTitle(result.getTitle());
        hotArticle.setContents(result.getContents());
        hotArticle.setAuthorName(result.getAuthor().getUsername());
        hotArticle.setCreatedDate(result.getCreatedDate());
        hotArticle.setUpdatedDate(result.getUpdatedDate());
        hotArticle.setViewCount(result.getViewCount());

        this.redisTemplate.opsForHash().put("Yesterday:" + REDIS_KEY, hotArticle.getId(), hotArticle);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void pickWeeklyHotArticle() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(8);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        // todo: boardId
        Article result = this.articleRepository.findHotArticle(1L, startDate, endDate);
        if (result == null) {
            return;
        }

        HotArticle hotArticle = new HotArticle();
        hotArticle.setId(result.getId());
        hotArticle.setTitle(result.getTitle());
        hotArticle.setContents(result.getContents());
        hotArticle.setAuthorName(result.getAuthor().getUsername());
        hotArticle.setCreatedDate(result.getCreatedDate());
        hotArticle.setUpdatedDate(result.getUpdatedDate());
        hotArticle.setViewCount(result.getViewCount());

        this.redisTemplate.opsForHash().put("Weekly:" + REDIS_KEY, hotArticle.getId(), hotArticle);
    }
}
