package com.myrecipe.config.security;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.EmailConfirmationToken;
import com.myrecipe.repository.EmailConfirmationTokenRepository;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    public void resetMocks() {
        reset(javaMailSender);
    }

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
    public void registerCreatesInactiveUserWithUserRoleEncodedPasswordAndConfirmationEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\" Иван \",\"lastName\":\" Петров \","
                                + "\"email\":\" New.User@Mail.COM \",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(
                        "Registration successful. Please confirm your email before logging in."))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.emailConfirmationToken").doesNotExist())
                .andExpect(jsonPath("$.passwordResetToken").doesNotExist());

        Users user = usersRepository.findByEmail("new.user@mail.com");
        org.assertj.core.api.Assertions.assertThat(user).isNotNull();
        org.assertj.core.api.Assertions.assertThat(user.getFirstName()).isEqualTo("Иван");
        org.assertj.core.api.Assertions.assertThat(user.getLastName()).isEqualTo("Петров");
        org.assertj.core.api.Assertions.assertThat(user.getRole()).isEqualTo(RolesEn.USER);
        org.assertj.core.api.Assertions.assertThat(user.isActivated()).isFalse();
        org.assertj.core.api.Assertions.assertThat(user.getPassword()).isNotEqualTo("secret123");
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("secret123", user.getPassword())).isTrue();

        EmailConfirmationToken token = emailConfirmationTokenRepository.findByUser(user.getId());
        org.assertj.core.api.Assertions.assertThat(token).isNotNull();
        org.assertj.core.api.Assertions.assertThat(token.getToken()).isNotBlank();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void registerDuplicateEmailReturnsExistingJsonConflict() throws Exception {
        createUser("duplicate-register@mail.com", "secret123", true);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Иван\",\"lastName\":\"Петров\","
                                + "\"email\":\"duplicate-register@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicate record"))
                .andExpect(jsonPath("$.details").value("An account with this email already exists!"));
    }

    @Test
    public void registerInvalidFieldsReturnsJsonBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"\",\"email\":\"not-email\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
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
    public void currentUserReturnsJsonUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.details").value("Authentication is required"));
    }

    @Test
    public void currentUserReturnsSafeDtoForTokenUser() throws Exception {
        Users user = createUser("me-user@mail.com", "secret123", true);
        String token = tokenFor("me-user@mail.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("me-user@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.emailConfirmationToken").doesNotExist())
                .andExpect(jsonPath("$.passwordResetToken").doesNotExist())
                .andExpect(jsonPath("$.recipe").doesNotExist())
                .andExpect(jsonPath("$.commentsList").doesNotExist());
    }

    @Test
    public void currentUserIgnoresClientSuppliedUserSelectors() throws Exception {
        Users tokenUser = createUser("token-user@mail.com", "secret123", true);
        createUser("other-user@mail.com", "secret123", true);
        String token = tokenFor("token-user@mail.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .param("email", "other-user@mail.com")
                        .param("userId", "999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tokenUser.getId()))
                .andExpect(jsonPath("$.email").value("token-user@mail.com"));
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
