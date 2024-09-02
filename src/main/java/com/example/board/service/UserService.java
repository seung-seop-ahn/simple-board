package com.example.board.service;

import com.example.board.controller.CreateUserDto;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUserList() {
        return this.userRepository.findAll();
    }

    public User createUser(CreateUserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        this.userRepository.deleteById(userId);
    }

}
