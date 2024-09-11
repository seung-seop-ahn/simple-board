package com.example.board.repository;

import com.example.board.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId ORDER BY a.createdDate DESC LIMIT 10")
    List<Article> findTop10ByBoardIdOrderByCreatedDateDesc(@Param("boardId") Long boardId);

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id < :articleId ORDER BY a.createdDate DESC LIMIT 10")
    List<Article> findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(@Param("boardId") Long boardId, @Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a WHERE a.board.id = :boardId AND a.id > :articleId ORDER BY a.createdDate DESC LIMIT 10")
    List<Article> findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(@Param("boardId") Long boardId, @Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a JOIN User u ON a.author.id = u.id AND u.username = :username ORDER BY a.createdDate DESC LIMIT 1")
    Article findLatestArticleByAuthorUsernameOrderByCreatedDateDesc(@Param("username") String username);

    @Query("SELECT a FROM Article a JOIN User u ON a.author.id = u.id AND u.username = :username ORDER BY a.updatedDate DESC LIMIT 1")
    Article findLatestEditedArticleByAuthorUsernameOrderByUpdatedDateDesc(@Param("username") String username);
}
