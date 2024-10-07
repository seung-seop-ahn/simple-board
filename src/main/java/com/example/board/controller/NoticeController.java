package com.example.board.controller;

import com.example.board.dto.PostNoticeDto;
import com.example.board.entity.Notice;
import com.example.board.service.NoticeService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeService noticeService;

    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping()
    public ResponseEntity<Notice> postNotice(@Valid @RequestBody PostNoticeDto dto) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Notice notice = this.noticeService.postArticle(userDetails.getUsername(), dto);
        return ResponseEntity.ok(notice);
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<Notice> getNotice(@PathVariable Long noticeId) throws BadRequestException {
        Notice notice = this.noticeService.getNotice(noticeId);
        return ResponseEntity.ok(notice);
    }
}
