package com.example.board.repository;

import com.example.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT a FROM Comment a JOIN User u ON a.author.id = u.id AND u.username = :username WHERE a.isDeleted = false ORDER BY a.createdDate DESC LIMIT 1")
    Optional<Comment> findLatestCommentByAuthorUsernameOrderByCreatedDateDesc(@Param("username") String username);

    @Query("SELECT a FROM Comment a JOIN User u ON a.author.id = u.id AND u.username = :username WHERE a.isDeleted = false ORDER BY a.updatedDate DESC LIMIT 1")
    Optional<Comment> findLatestEditedCommentByAuthorUsernameOrderByUpdatedDateDesc(@Param("username") String username);

    List<Comment> findByArticleIdAndIsDeletedFalse(Long articleId);
}
