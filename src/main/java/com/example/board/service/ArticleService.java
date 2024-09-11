package com.example.board.service;

import com.example.board.controller.PostArticleDto;
import com.example.board.entity.Article;
import com.example.board.entity.Board;
import com.example.board.entity.User;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArticleService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleService(BoardRepository boardRepository, UserRepository userRepository, ArticleRepository articleRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    public Article writeArticle(String username, Long boardId, PostArticleDto dto) throws BadRequestException {
        User author = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        Board board = this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = new Article();
        article.setBoard(board);
        article.setAuthor(author);
        article.setTitle(dto.getTitle());
        article.setContents(dto.getContents());

        return articleRepository.save(article);
    }
}
