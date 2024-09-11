package com.example.board.service;

import com.example.board.dto.PostArticleDto;
import com.example.board.dto.PutArticleDto;
import com.example.board.entity.Article;
import com.example.board.entity.Board;
import com.example.board.entity.User;
import com.example.board.repository.ArticleRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Article postArticle(String username, Long boardId, PostArticleDto dto) throws BadRequestException {
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

    public List<Article> getTop10ArticleListByFirstId(Long boardId, Long firstId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, firstId);
    }

    public List<Article> getTop10ArticleListByLastId(Long boardId, Long lastId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, lastId);
    }

    public List<Article> getTop10ArticleList(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public Article putArticle(String username, Long boardId, Long articleId, PutArticleDto dto) throws BadRequestException {
        this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException("Board not found"));

        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new BadRequestException("Article not found"));

        if(dto.getTitle() != null) {
            article.setTitle(dto.getTitle());
        }
        if(dto.getContents() != null) {
            article.setContents(dto.getContents());
        }

        return this.articleRepository.save(article);
    }
}
