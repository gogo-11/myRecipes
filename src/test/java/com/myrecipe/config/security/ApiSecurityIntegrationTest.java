package com.myrecipe.config.security;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
import com.myrecipe.security.EmailConfirmationResendThrottle;
import com.myrecipe.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
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

    @Autowired
    private EmailConfirmationResendThrottle resendThrottle;

    @MockBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    public void resetMocks() {
        reset(javaMailSender);
        resendThrottle.clear();
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

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(messageCaptor.getValue().getText())
                .contains("http://localhost:4200/confirm-email/" + token.getToken());
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
    public void confirmEmailActivatesUserAndDeletesTokenWithoutJwt() throws Exception {
        Users user = createUser("confirm-success@mail.com", "secret123", false);
        createEmailToken(user, "confirm-success-token");

        mockMvc.perform(post("/api/v1/auth/email-confirmations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"confirm-success-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmed successfully."));

        Users confirmedUser = usersRepository.findByEmail("confirm-success@mail.com");
        org.assertj.core.api.Assertions.assertThat(confirmedUser.isActivated()).isTrue();
        org.assertj.core.api.Assertions.assertThat(
                emailConfirmationTokenRepository.findByToken("confirm-success-token")).isNull();
    }

    @Test
    public void confirmEmailReturnsBadRequestForUnknownToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-confirmations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"unknown-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid confirmation token."));
    }

    @Test
    public void confirmEmailReturnsBadRequestForAlreadyUsedToken() throws Exception {
        Users user = createUser("confirm-repeat@mail.com", "secret123", false);
        createEmailToken(user, "confirm-repeat-token");

        mockMvc.perform(post("/api/v1/auth/email-confirmations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"confirm-repeat-token\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/email-confirmations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"confirm-repeat-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid confirmation token."));
    }

    @Test
    public void resendConfirmationReturnsAcceptedForUnknownEmailWithoutSending() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing-resend@mail.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "If the account exists and still needs confirmation, a confirmation email will be sent."));

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void resendConfirmationReturnsAcceptedForActiveUserWithoutSending() throws Exception {
        createUser("active-resend@mail.com", "secret123", true);

        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"active-resend@mail.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "If the account exists and still needs confirmation, a confirmation email will be sent."));

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void resendConfirmationReusesExistingTokenForInactiveUser() throws Exception {
        Users user = createUser("inactive-existing-token@mail.com", "secret123", false);
        createEmailToken(user, "existing-resend-token");

        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inactive-existing-token@mail.com\"}"))
                .andExpect(status().isAccepted());

        EmailConfirmationToken token = emailConfirmationTokenRepository.findByUser(user.getId());
        org.assertj.core.api.Assertions.assertThat(token.getToken()).isEqualTo("existing-resend-token");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(messageCaptor.getValue().getText())
                .contains("http://localhost:4200/confirm-email/existing-resend-token");
    }

    @Test
    public void resendConfirmationCreatesTokenForInactiveUserWithoutToken() throws Exception {
        Users user = createUser("inactive-no-token@mail.com", "secret123", false);

        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inactive-no-token@mail.com\"}"))
                .andExpect(status().isAccepted());

        EmailConfirmationToken token = emailConfirmationTokenRepository.findByUser(user.getId());
        org.assertj.core.api.Assertions.assertThat(token).isNotNull();
        org.assertj.core.api.Assertions.assertThat(token.getToken()).isNotBlank();

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(messageCaptor.getValue().getText())
                .contains("http://localhost:4200/confirm-email/" + token.getToken());
    }

    @Test
    public void resendConfirmationMailFailureKeepsExistingToken() throws Exception {
        Users user = createUser("inactive-mail-failure-existing@mail.com", "secret123", false);
        createEmailToken(user, "old-working-token");
        doThrow(new MailSendException("mail down")).when(javaMailSender).send(any(SimpleMailMessage.class));

        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inactive-mail-failure-existing@mail.com\"}"))
                .andExpect(status().isAccepted());

        EmailConfirmationToken token = emailConfirmationTokenRepository.findByUser(user.getId());
        org.assertj.core.api.Assertions.assertThat(token.getToken()).isEqualTo("old-working-token");
    }

    @Test
    public void resendConfirmationMailFailureWithoutExistingTokenStoresTokenForLaterReuse() throws Exception {
        Users user = createUser("inactive-mail-failure-new@mail.com", "secret123", false);
        doThrow(new MailSendException("mail down")).when(javaMailSender).send(any(SimpleMailMessage.class));

        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inactive-mail-failure-new@mail.com\"}"))
                .andExpect(status().isAccepted());

        EmailConfirmationToken token = emailConfirmationTokenRepository.findByUser(user.getId());
        org.assertj.core.api.Assertions.assertThat(token).isNotNull();
        String storedToken = token.getToken();

        reset(javaMailSender);
        mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inactive-mail-failure-new@mail.com\"}"))
                .andExpect(status().isAccepted());

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(messageCaptor.getValue().getText())
                .contains("http://localhost:4200/confirm-email/" + storedToken);
        org.assertj.core.api.Assertions.assertThat(emailConfirmationTokenRepository.findByUser(user.getId()).getToken())
                .isEqualTo(storedToken);
    }

    @Test
    public void resendConfirmationThrottledRequestsReturnAcceptedWithoutMoreEmail() throws Exception {
        Users user = createUser("inactive-throttled@mail.com", "secret123", false);
        createEmailToken(user, "throttled-token");

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/v1/auth/email-confirmations/resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"inactive-throttled@mail.com\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value(
                            "If the account exists and still needs confirmation, a confirmation email will be sent."));
        }

        verify(javaMailSender, org.mockito.Mockito.times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void existingMvcConfirmationLinkStillActivatesUser() throws Exception {
        Users user = createUser("mvc-confirm@mail.com", "secret123", false);
        createEmailToken(user, "mvc-confirm-token");

        mockMvc.perform(get("/confirm-email/mvc-confirm-token"))
                .andExpect(status().isOk());

        Users confirmedUser = usersRepository.findByEmail("mvc-confirm@mail.com");
        org.assertj.core.api.Assertions.assertThat(confirmedUser.isActivated()).isTrue();
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

    private EmailConfirmationToken createEmailToken(Users user, String tokenValue) {
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken(tokenValue);
        token.setUser(user);
        return emailConfirmationTokenRepository.save(token);
    }

    private String tokenFor(String email) {
        return jwtTokenService.generateToken(new User(
                email,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
