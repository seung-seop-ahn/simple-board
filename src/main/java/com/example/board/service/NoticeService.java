package com.example.board.service;

import com.example.board.dto.PostNoticeDto;
import com.example.board.entity.Notice;
import com.example.board.entity.User;
import com.example.board.entity.UserNotificationHistory;
import com.example.board.repository.NoticeRepository;
import com.example.board.repository.UserNotificationHistoryRepository;
import com.example.board.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoticeService {
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    @Autowired
    public NoticeService(UserRepository userRepository, NoticeRepository noticeRepository, UserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.userRepository = userRepository;
        this.noticeRepository = noticeRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    @Transactional
    public Notice postArticle(String username, PostNoticeDto dto) throws BadRequestException {
        User author = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContents(dto.getContents());
        notice.setAuthor(author);

        return this.noticeRepository.save(notice);
    }

    public Notice getNotice(Long noticeId) throws BadRequestException {
        Notice notice = this.noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BadRequestException("Notice not found."));

        UserNotificationHistory userNotificationHistory = new UserNotificationHistory();
        userNotificationHistory.setTitle("Notice:" + notice.getTitle());
        userNotificationHistory.setContents(notice.getContents());
        userNotificationHistory.setUserId(notice.getAuthor().getId());
        userNotificationHistory.setIsRead(true);

        this.userNotificationHistoryRepository.save(userNotificationHistory);

        return notice;
    }
}
