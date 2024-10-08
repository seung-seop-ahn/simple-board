package com.example.board.service;

import com.example.board.dto.SignUpDto;
import com.example.board.entity.Notice;
import com.example.board.entity.User;
import com.example.board.entity.UserNotificationHistory;
import com.example.board.repository.NoticeRepository;
import com.example.board.repository.UserNotificationHistoryRepository;
import com.example.board.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

    private final PasswordEncoder passwordEncoder;

    // todo
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;
    private final UserNotificationHistoryService userNotificationHistoryService;

    @Autowired
    public UserService(UserRepository userRepository, NoticeRepository noticeRepository, UserNotificationHistoryRepository userNotificationHistoryRepository, PasswordEncoder passwordEncoder, UserNotificationHistoryService userNotificationHistoryService) {
        this.userRepository = userRepository;
        this.noticeRepository = noticeRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.userNotificationHistoryService = userNotificationHistoryService;
    }

    public List<User> getUserList() {
        return this.userRepository.findAll();
    }

    public User signUp(SignUpDto dto) {
        String encodedPassword = this.passwordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        this.userRepository.deleteById(userId);
    }

    public void patchNotification(String historyId) {
        this.userNotificationHistoryService.readNotification(historyId.toString());
    }

    public List<UserNotificationHistory> getNotificationList(String username) throws BadRequestException {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(7);

        List<UserNotificationHistory> userNotificationHistoryList = this.userNotificationHistoryRepository.findByUserIdAndCreatedDateAfter(user.getId(), oneWeekAgo);
        List<UserNotificationHistory> result = new ArrayList<>(userNotificationHistoryList);
        List<Notice> noticeList = this.noticeRepository.findAllByCreatedDate(oneWeekAgo);

        HashMap<Long, UserNotificationHistory> hashMap = new HashMap<>();
        for (UserNotificationHistory userNotificationHistory : userNotificationHistoryList) {
            hashMap.put(userNotificationHistory.getNoticeId(), userNotificationHistory);
        }
        for (Notice notice : noticeList) {
            if (hashMap.get(notice.getId()) != null) {
                continue;
            }

            UserNotificationHistory userNotificationHistory = new UserNotificationHistory();
            userNotificationHistory.setTitle("Notice:" + notice.getTitle());
            userNotificationHistory.setContents(notice.getContents());
            userNotificationHistory.setUserId(notice.getAuthor().getId());
            userNotificationHistory.setNoticeId(notice.getId());
            userNotificationHistory.setIsRead(false);
            userNotificationHistory.setCreatedDate(notice.getCreatedDate());

            result.add(userNotificationHistory);
        }

        return result;
    }
}
