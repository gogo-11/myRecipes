package com.myrecipe.config.repository;

import java.util.ArrayList;

import com.myrecipe.entities.PasswordResetToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.repository.UsersRepository;

@DataJpaTest
public class UsersRepositoryTest {
    @Autowired
    private UsersRepository userRepo;

//    @Test
//    public void addNewUserTest () {
//        Users user = userRepo.save(new Users(
//                1,
//                "John",
//                "Doe",
//                "example@mail.com",
//                "123456",
//                RolesEn.USER,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new PasswordResetToken()));
//
//        assertThat(user).hasFieldOrPropertyWithValue("firstName", "John");
//        assertThat(user).hasFieldOrPropertyWithValue("lastName", "Doe");
//    }

//    @Test
//    public void deleteUserTest () {
//        Users user = userRepo.save(new Users(
//                1,
//                "John",
//                "Doe",
//                "example@mail.com",
//                "123456",
//                RolesEn.USER,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new PasswordResetToken()));
//
//        userRepo.deleteAll();
//
//        assertThat(userRepo.findAll()).isEmpty();
//    }

//    @Test
//    public void findUserByEmailTest () {
//        Users user = userRepo.save(new Users(
//                1,
//                "John",
//                "Doe",
//                "example@mail.com",
//                "123456",
//                RolesEn.USER,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new PasswordResetToken()));
//
//        Users userEx = userRepo.findByEmail("example@mail.com");
//
//        assertThat(userEx.equals(user));
//    }
}
