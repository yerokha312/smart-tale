package dev.yerokha.smarttale.controller.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.PurchaseRequest;
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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(7)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MarketplaceControllerTest {

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
        Mockito.verify(mailService).sendEmailVerification(
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
    void getProducts_Authenticated() throws Exception {
        Thread.sleep(1000);
        login("existing3@example.com");
        MvcResult result = mockMvc.perform(get("/v1/market?type=products")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(5))
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<String> dates = JsonPath.read(content, "$.content[*].publishedAt");

        for (int i = 1; i < dates.size(); i++) {
            assert dates.get(i - 1).compareTo(dates.get(i)) >= 0;
        }
    }

    @Test
    @Order(1)
    void getProducts() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/market?type=products"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(5))
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<String> dates = JsonPath.read(content, "$.content[*].publishedAt");

        for (int i = 1; i < dates.size(); i++) {
            assert dates.get(i - 1).compareTo(dates.get(i)) >= 0;
        }
    }

    @Test
    @Order(2)
    void getAd() throws Exception {
        mockMvc.perform(get("/v1/market/100009"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.purchasedAt").value(nullValue())
                );
    }

    @Test
    @Order(2)
    void getAd_Should404() throws Exception {
        mockMvc.perform(get("/v1/market/100001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void purchase() throws Exception {
        mockMvc.perform(post("/v1/market/100009")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk()
                );

        ArgumentCaptor<PurchaseRequest> captor = ArgumentCaptor.forClass(PurchaseRequest.class);
        Mockito.verify(mailService).sendPurchaseRequest(
                eq("existing3@example.com"),
                captor.capture()
        );

        PurchaseRequest request = captor.getValue();

        assert request.title().equals("Product 10");
        assert request.requesterEmail().equals("existing3@example.com");
        assert request.requesterPhoneNumber().equals("+777712345690");

    }

    @Test
    @Order(3)
    void purchase_Should410() throws Exception {
        mockMvc.perform(post("/v1/market/100001")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isGone());
    }

    @Test
    @Order(3)
    void purchase_Should401() throws Exception {
        mockMvc.perform(post("/v1/market/100001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void getProducts_AfterPurchase() throws Exception {
        mockMvc.perform(get("/v1/market?type=products"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(4))
                );
    }

    @Test
    @Order(5)
    void getPurchases_AfterPurchase() throws  Exception {
        mockMvc.perform(get("/v1/account/purchases")
                .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(6))
                );
    }

}