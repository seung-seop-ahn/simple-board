package com.example.board.repository;

import com.example.board.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);

    @Query(value = "SELECT * FROM token_blacklist WHERE username = :username ORDER BY expire_date DESC LIMIT 1", nativeQuery = true)
    Optional<TokenBlacklist> findByUsernameOrderByExpireDateDesc(@Param("username") String username);
}
