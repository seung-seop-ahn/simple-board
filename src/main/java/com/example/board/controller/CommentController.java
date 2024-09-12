package com.example.board.controller;

import com.example.board.dto.PostCommentDto;
import com.example.board.dto.PutCommentDto;
import com.example.board.entity.Comment;
import com.example.board.service.CommentService;
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
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{boardId}/articles/{articleId}/comments")
    public ResponseEntity<Comment> postComment(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @Valid @RequestBody PostCommentDto dto
    ) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Comment comment = this.commentService.postComment(userDetails.getUsername(), boardId, articleId, dto);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{boardId}/articles/{articleId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long boardId, @PathVariable Long articleId) throws BadRequestException {
        List<Comment> list = this.commentService.getComments(boardId, articleId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{boardId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Comment> putArticle(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @Valid @RequestBody PutCommentDto dto) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Comment comment = this.commentService.putComment(userDetails.getUsername(), boardId, articleId, commentId, dto);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{boardId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> putArticle(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @PathVariable Long commentId
    ) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        this.commentService.deleteComment(userDetails.getUsername(), boardId, articleId, commentId);
        return ResponseEntity.noContent().build();
    }
}
