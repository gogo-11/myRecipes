package com.myrecipe.repository;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Query("select t from EmailConfirmationToken t where t.user.id = :userId")
    Optional<EmailConfirmationToken> findOptionalByUserId(@Param("userId") Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from EmailConfirmationToken t join fetch t.user where t.token = :token")
    Optional<EmailConfirmationToken> findForUpdateByToken(@Param("token") String token);
}
