package com.test.naming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.naming.entity.OAuth;
import com.test.naming.entity.User;

@Repository
public interface OAuthRepository extends JpaRepository<OAuth, Long> {
    OAuth findByUserAndProvider(User user, String provider);
    boolean existsByUserAndProvider(User user, String provider);
}