package dev.yerokha.smarttale.controller.other;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.service.ImageService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(11)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OtherControllerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    MailService mailService;
    @MockBean
    ImageService imageService;
    @Autowired
    UserRepository userRepository;
    final String APP_JSON = "application/json";
    public static String accessToken;

    private void login(String email) throws Exception {
        Thread.sleep(1000);
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(email));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendLoginCode(
                anyString(),
                confirmationUrlCaptor.capture()
        );

        String verificationCode = confirmationUrlCaptor.getValue();

        VerificationRequest request = new VerificationRequest(
                email,
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
    void getAllInvitations() throws Exception {
        login("existing8@example.com");
        mockMvc.perform(get("/v1/account/profile/invitations")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(1)
                );
    }

    @Test
    @Order(20)
    void acceptInvitation_Should403() throws Exception {
        mockMvc.perform(post("/v1/account/profile/invitations/100003")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(
                        status().isForbidden());
    }

    @Test
    @Order(21)
    void acceptInvitation_Should401() throws Exception {
        mockMvc.perform(post("/v1/account/profile/invitations/100003"))
                .andExpect(
                        status().isUnauthorized());
    }

    @Test
    @Order(29)
    void acceptInvitation_Should404() throws Exception {
        login("invited1@example.com");
        mockMvc.perform(post("/v1/account/profile/invitations/100003")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(
                        status().isNotFound());
    }

    @Test
    @Order(30)
    void acceptInvitation() throws Exception {
        mockMvc.perform(post("/v1/account/profile/invitations/100000")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(
                        status().isOk());
    }

    @Test
    @Order(31)
    void checkInvitations() throws Exception {
        mockMvc.perform(get("/v1/account/profile/invitations")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(0)
                );
    }
}
