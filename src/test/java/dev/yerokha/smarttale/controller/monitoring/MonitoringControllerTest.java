package dev.yerokha.smarttale.controller.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
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

import java.util.Arrays;
import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(8)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MonitoringControllerTest {

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
    @Autowired
    UserDetailsRepository userDetailsRepository;

    final String APP_JSON = "application/json";
    public static String accessToken;

    private void login(String email) throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                .contentType(APP_JSON)
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
    void getDashboard() throws Exception {
        Thread.sleep(1000);
        login("existing4@example.com");

        MvcResult result = mockMvc.perform(get("/v1/monitoring")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        List<DashboardOrder> orders = Arrays.asList(new ObjectMapper()
                .readValue(result.getResponse().getContentAsString(), DashboardOrder[].class));

        for (int i = 1; i < orders.size(); i++) {
            assert orders.get(i - 1).status().compareTo(orders.get(i).status()) <= 0;
        }
    }

    @Test
    @Order(2)
    void getOrder() throws Exception {
        mockMvc.perform(get("/v1/monitoring/100021")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.title").value("Order 12"),
                        jsonPath("$.status").value("IN_PROGRESS")
                );
    }

    @Test
    @Order(3)
    void getOrder_Should404() throws Exception {
        mockMvc.perform(get("/v1/monitoring/100038")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void changeStatus() throws Exception {
        mockMvc.perform(put("/v1/monitoring/100021")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("checking")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void getOrder_AfterStatusChange() throws Exception {
        mockMvc.perform(get("/v1/monitoring/100021")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.title").value("Order 12"),
                        jsonPath("$.status").value("CHECKING")
                );
    }

    @Test
    @Order(6)
    void changeStatus_Should403() throws Exception {
        mockMvc.perform(put("/v1/monitoring/100022")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("dispatched")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void changeStatus_Should404() throws Exception {
        mockMvc.perform(put("/v1/monitoring/100036")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("dispatched")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

}