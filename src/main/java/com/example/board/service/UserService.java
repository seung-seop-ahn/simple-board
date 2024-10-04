package com.example.board.service;

import com.example.board.dto.SignUpDto;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserNotificationHistoryService userNotificationHistoryService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserNotificationHistoryService userNotificationHistoryService) {
        this.userRepository = userRepository;
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

    public void getNotification(String historyId) {
        this.userNotificationHistoryService.readNotification(historyId.toString());
    }
}
