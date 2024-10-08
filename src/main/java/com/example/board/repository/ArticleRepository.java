package com.example.board.repository;

import com.example.board.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.isDeleted = false ORDER BY a.createdDate DESC LIMIT 10")
    List<Article> findTop10ByBoardIdOrderByCreatedDateDesc(@Param("boardId") Long boardId);

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id < :articleId AND a.isDeleted = false ORDER BY a.createdDate DESC LIMIT 10")
    List<Article> findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(@Param("boardId") Long boardId, @Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id > :articleId AND a.isDeleted = false ORDER BY a.createdDate DESC LIMIT 10")
    List<Article> findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(@Param("boardId") Long boardId, @Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a JOIN User u ON a.author.id = u.id AND u.username = :username WHERE a.isDeleted = false ORDER BY a.createdDate DESC LIMIT 1")
    Optional<Article> findLatestArticleByAuthorUsernameOrderByCreatedDateDesc(@Param("username") String username);

    @Query("SELECT a FROM Article a JOIN User u ON a.author.id = u.id AND u.username = :username WHERE a.isDeleted = false ORDER BY a.updatedDate DESC LIMIT 1")
    Optional<Article> findLatestEditedArticleByAuthorUsernameOrderByUpdatedDateDesc(@Param("username") String username);

    @Query("SELECT a FROM Article a JOIN User u ON a.author.id = u.id WHERE a.isDeleted = false AND a.id IN :ids ORDER BY a.updatedDate DESC")
    List<Article> findAllByIds(@Param("ids") List<Long> ids);

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.createdDate >= :startDate AND a.createdDate < :endDate AND a.isDeleted = false ORDER BY a.viewCount DESC LIMIT 1")
    Article findHotArticle(@Param("boardId") Long boardId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
