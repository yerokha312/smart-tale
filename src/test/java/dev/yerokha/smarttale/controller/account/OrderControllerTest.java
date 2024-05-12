package dev.yerokha.smarttale.controller.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(5)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderControllerTest {

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
    void getOrders_Active() throws Exception {
        Thread.sleep(1000);
        login("existing2@example.com");
        MvcResult result = mockMvc.perform(get("/v1/account/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "active"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(24)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].date");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) > 0;
        }

    }

    @Test
    @Order(1)
    void getOrders_Completed() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "not_active"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(6))
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].date");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) >= 0;
        }

    }

    @Test
    void getOrder() throws Exception {
        mockMvc.perform(get("/v1/account/orders/100011")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.title").value("Order 2")
                );

    }
}