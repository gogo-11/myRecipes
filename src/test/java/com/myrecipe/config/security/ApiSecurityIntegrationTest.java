package com.myrecipe.config.security;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiSecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    public void loginReturnsBearerTokenForActiveUser() throws Exception {
        createUser("login-success@mail.com", "secret123", true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login-success@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value(not(emptyOrNullString())))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.user.email").value("login-success@mail.com"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.emailConfirmationToken").doesNotExist())
                .andExpect(jsonPath("$.user.passwordResetToken").doesNotExist());
    }

    @Test
    public void loginReturnsUnauthorizedForBadCredentials() throws Exception {
        createUser("bad-credentials@mail.com", "secret123", true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad-credentials@mail.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    public void loginReturnsUnauthorizedForDisabledUser() throws Exception {
        createUser("disabled-user@mail.com", "secret123", false);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"disabled-user@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    public void protectedApiReturnsJsonUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.details").value("Authentication is required"));
    }

    @Test
    public void protectedApiReturnsJsonUnauthorizedWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.details").value("Authentication is required"));
    }

    @Test
    public void publicRecipeListIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk());
    }

    @Test
    public void unsafeRecipeWriteIsDeniedEvenWithToken() throws Exception {
        createUser("write-denied@mail.com", "secret123", true);
        String token = tokenFor("write-denied@mail.com");

        mockMvc.perform(post("/api/v1/recipes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    public void legacyUserRestRouteIsDenied() throws Exception {
        mockMvc.perform(get("/user/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void legacyCommentsRestRouteIsDenied() throws Exception {
        mockMvc.perform(get("/comments/waiting"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void corsAllowsAngularDevelopmentOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/recipes")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    public void corsRejectsOtherOrigins() throws Exception {
        mockMvc.perform(options("/api/v1/recipes")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void mvcFormLoginPageRemainsAvailable() throws Exception {
        mockMvc.perform(get("/login_form"))
                .andExpect(status().isOk());
    }

    private Users createUser(String email, String password, boolean activated) {
        Users user = new Users();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(RolesEn.USER);
        user.setActivated(activated);
        return usersRepository.save(user);
    }

    private String tokenFor(String email) {
        return jwtTokenService.generateToken(new User(
                email,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
