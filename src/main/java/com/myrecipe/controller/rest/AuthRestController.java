package com.myrecipe.controller.rest;

import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.LoginRequest;
import com.myrecipe.entities.responses.AuthenticatedUserResponse;
import com.myrecipe.entities.responses.LoginResponse;
import com.myrecipe.entities.responses.MyApiErrorResponse;
import com.myrecipe.security.JwtTokenService;
import com.myrecipe.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UsersService usersService;

    public AuthRestController(AuthenticationManager authenticationManager,
                              JwtTokenService jwtTokenService,
                              UsersService usersService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.usersService = usersService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenService.generateToken(userDetails);
            Users user = usersService.getByEmail(userDetails.getUsername());

            return new ResponseEntity<>(
                    new LoginResponse(
                            "Bearer",
                            token,
                            jwtTokenService.getExpiration(token),
                            toAuthenticatedUserResponse(user)),
                    HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    new MyApiErrorResponse("Unauthorized", "Invalid email or password", HttpStatus.UNAUTHORIZED),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    private AuthenticatedUserResponse toAuthenticatedUserResponse(Users user) {
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getRoleName());
    }
}
