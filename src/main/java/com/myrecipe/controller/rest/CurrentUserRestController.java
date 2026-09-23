package com.myrecipe.controller.rest;

import java.security.Principal;

import com.myrecipe.entities.Users;
import com.myrecipe.entities.responses.CurrentUserResponse;
import com.myrecipe.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserRestController {
    private final AuthService authService;

    public CurrentUserRestController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(Principal principal) {
        Users user = authService.getCurrentUser(principal.getName());
        return new ResponseEntity<>(toCurrentUserResponse(user), HttpStatus.OK);
    }

    private CurrentUserResponse toCurrentUserResponse(Users user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getRoleName());
    }
}
