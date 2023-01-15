package com.myrecipe.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.UsersResponse;
import com.myrecipe.entities.Users;

@Component
public interface UsersService {
    UsersResponse createUser(UsersRequest userRequest);
    Users getById(Integer id);
    Users getByUserCredentials(UsersRequest usersRequest);
    Optional<Users> userUpdate(Integer id, UsersRequest userRequest);
    void deleteUser (UsersRequest userRequest);
}
