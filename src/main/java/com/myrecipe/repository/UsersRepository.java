package com.myrecipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrecipe.entities.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    @Query(value = "SELECT * FROM users WHERE email = :userEmail", nativeQuery = true)
    Users findByEmail(@Param("userEmail") String userEmail);

    @Query(value = "SELECT * FROM users WHERE email = :userEmail AND password = :userPass", nativeQuery = true)
    Users findByUserCredentials(@Param("userEmail") String userEmail, @Param("userPass") String password);
}
