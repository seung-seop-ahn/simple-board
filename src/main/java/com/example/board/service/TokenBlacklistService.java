package com.example.board.service;

import com.example.board.entity.TokenBlacklist;
import com.example.board.repository.TokenBlacklistRepository;
import com.example.board.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class TokenBlacklistService {
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository, JwtUtil jwtUtil) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtUtil = jwtUtil;
    }

    public void addBlacklist(String token, LocalDateTime expireDate, String username) {
        TokenBlacklist tokenBlacklist = new TokenBlacklist();
        tokenBlacklist.setToken(token);
        tokenBlacklist.setExpireDate(expireDate);
        tokenBlacklist.setUsername(username);
        this.tokenBlacklistRepository.save(tokenBlacklist);
    }

    public boolean isTokenBlacklisted(String token) {
        Optional<TokenBlacklist> blacklistedToken = this.tokenBlacklistRepository.findByToken(token);
        return blacklistedToken.isPresent() && blacklistedToken.get().getExpireDate().isAfter(LocalDateTime.now());
    }

    public boolean isUserTokenBlacklisted(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        Optional<TokenBlacklist> blacklistedToken = this.tokenBlacklistRepository.findByUsernameOrderByExpireDateDesc(username);
        if (blacklistedToken.isEmpty()) {
            return false;
        }

        LocalDateTime tokenExpireDate = jwtUtil
                .getExpirationDateFromToken(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .minusMinutes(60);

        return blacklistedToken.get().getExpireDate().isAfter(tokenExpireDate);
    }
}
