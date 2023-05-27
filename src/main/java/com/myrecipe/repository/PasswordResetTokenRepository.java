package com.myrecipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrecipe.entities.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    @Query(value = "SELECT * FROM password_reset_token WHERE token = :token", nativeQuery = true)
    PasswordResetToken findByToken(@Param("token") String token);

    @Query(value = "SELECT * FROM password_reset_token WHERE user_id = :userId", nativeQuery = true)
    PasswordResetToken findByUser(@Param("userId") Integer userId);
}
