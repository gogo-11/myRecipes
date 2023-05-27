package com.myrecipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrecipe.entities.EmailConfirmationToken;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository< EmailConfirmationToken, Integer> {
    @Query(value = "SELECT * FROM email_confirmation_token WHERE token = :token", nativeQuery = true)
    EmailConfirmationToken findByToken(@Param("token") String token);

    @Query(value = "SELECT * FROM email_confirmation_token WHERE user_id = :userId", nativeQuery = true)
    EmailConfirmationToken findByUser(@Param("userId") Integer userId);
}
