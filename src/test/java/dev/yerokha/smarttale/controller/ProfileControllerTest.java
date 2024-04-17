package dev.yerokha.smarttale.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.UpdateProfileRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.service.MailService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static dev.yerokha.smarttale.controller.AuthenticationControllerTest.extractToken;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(2)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    MailService mailService;
    @Autowired
    UserRepository userRepository;
    final String APP_JSON = "application/json";

    public static String accessToken;

    private void login() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                .contentType(APP_JSON)
                .content("existing@example.com"));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendEmailVerification(
                eq("existing@example.com"),
                eq("Existing Example"),
                confirmationUrlCaptor.capture()
        );

        String verificationCode = confirmationUrlCaptor.getValue();

        VerificationRequest request = new VerificationRequest(
                "existing@example.com",
                verificationCode
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/v1/auth/verification")
                        .contentType(APP_JSON)
                        .content(json))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        accessToken = extractToken(responseContent, "accessToken");
    }

    @Test
    @Order(1)
    void getProfile() throws Exception {
        login();
        mockMvc.perform(get("/v1/account")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.firstName", is("Existing")),
                        jsonPath("$.lastName", is("Profile")),
                        jsonPath("$.email", is("existing@example.com"))
                );
    }

    @Test
    @Order(1)
    void getProfile_NotAuthorized() throws Exception {
        mockMvc.perform(get("/v1/account"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void updateProfile_DifferentAlphabet() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Test",
                "Обновление",
                "Profile",
                "updatetest@example.com",
                "+7999999999"
        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/v1/account")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    void updateProfile_NotAuthorized() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Test",
                "Update",
                "Profile",
                "updatetest@example.com",
                "+7999999999"
        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/v1/account")
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void updateProfile_EmailExists() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Test",
                "Update",
                "Profile",
                "existing2@example.com",
                "+7999999999"
        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/v1/account")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(2)
    void updateProfile_PhoneNumberExists() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Test",
                "Update",
                "Profile",
                "existing@example.com",
                "+77771234567"
        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/v1/account")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    void updateProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Test",
                "Update",
                "Profile",
                "updatetest@example.com",
                "+7999999999"
        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/v1/account")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.firstName").value("Test"),
                        jsonPath("$.lastName").value("Update"),
                        jsonPath("$.fatherName").value("Profile"),
                        jsonPath("$.email").value("updatetest@example.com"),
                        jsonPath("$.phoneNumber").value("+7999999999"),
                        jsonPath("$.avatarUrl").value(nullValue()),
                        jsonPath("$.subscriptionEndDate").value(nullValue())
                );
    }
}

