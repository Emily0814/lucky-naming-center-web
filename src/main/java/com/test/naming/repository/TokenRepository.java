package com.test.naming.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.naming.entity.Token;
import com.test.naming.entity.User;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUser(User user);
    Optional<Token> findByRefreshToken(String refreshToken);
    void deleteByUser(User user);
}