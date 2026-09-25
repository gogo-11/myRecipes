package com.myrecipe.config.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myrecipe.entities.EmailConfirmationToken;
import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.repository.EmailConfirmationTokenRepository;
import com.myrecipe.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthRegistrationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    public void registerCreatesInactiveUserWithUserRoleEncodedPasswordAndConfirmationEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\" Ivan \",\"lastName\":\" Petrov \","
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
        org.assertj.core.api.Assertions.assertThat(user.getFirstName()).isEqualTo("Ivan");
        org.assertj.core.api.Assertions.assertThat(user.getLastName()).isEqualTo("Petrov");
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
                        .content("{\"firstName\":\"Ivan\",\"lastName\":\"Petrov\","
                                + "\"email\":\"duplicate-register@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicate record"))
                .andExpect(jsonPath("$.details").value("An account with this email already exists!"));
    }

    @Test
    public void registerAcceptsInternationalNames() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jose\\u0301 Maria\",\"lastName\":\"O'Connor-Smith\","
                                + "\"email\":\"international-name@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(
                        "Registration successful. Please confirm your email before logging in."));

        Users user = usersRepository.findByEmail("international-name@mail.com");
        org.assertj.core.api.Assertions.assertThat(user).isNotNull();
        org.assertj.core.api.Assertions.assertThat(user.getFirstName()).isEqualTo("Jose\u0301 Maria");
        org.assertj.core.api.Assertions.assertThat(user.getLastName()).isEqualTo("O'Connor-Smith");
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
    public void registerRejectsDigitsAndSymbolsInNames() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John2\",\"lastName\":\"O@Connor\","
                                + "\"email\":\"bad-name@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.fieldErrors.firstName").value(
                        "Name must contain only letters, spaces, apostrophes or hyphens"))
                .andExpect(jsonPath("$.fieldErrors.lastName").value(
                        "Name must contain only letters, spaces, apostrophes or hyphens"));
    }

    @Test
    public void registerRejectsNamesOutsideAllowedLength() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"J\",\"lastName\":\""
                                + "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName\","
                                + "\"email\":\"bad-size@mail.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.fieldErrors.firstName").value(
                        "First name must be between 2 and 50 characters"))
                .andExpect(jsonPath("$.fieldErrors.lastName").value(
                        "Last name must be between 2 and 50 characters"));
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
}

