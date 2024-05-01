package dev.yerokha.smarttale.controller.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.CreateAdRequest;
import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.PurchaseRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.exception.NotFoundException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void getMarketProducts() throws Exception {
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
    void getPurchases_AfterPurchase() throws Exception {
        mockMvc.perform(get("/v1/account/purchases")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(6))
                );
    }

    @Test
    @Order(6)
    void getMarketOrders() throws Exception {
        mockMvc.perform(get("/v1/market?type=orders"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(1))
                );
    }

    @Test
    @Order(7)
    void accept_Unauthorized() throws Exception {
        mockMvc.perform(put("/v1/market/100040"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    void accept() throws Exception {
        login("existing4@example.com");
        mockMvc.perform(put("/v1/market/100040")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void accept_Should410() throws Exception {
        mockMvc.perform(put("/v1/market/100040")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isGone());
    }

    @Test
    @Order(10)
    void testActiveOrdersCount() {
        UserDetailsEntity user = userDetailsRepository.findById(100003L)
                .orElseThrow(() -> new NotFoundException("User not found"));

        assertThat(user.getActiveOrdersCount()).isEqualTo(1);
    }

    @Test
    @Order(10)
    void getOrders_Active_AfterAccept() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "active"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(1))
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].date");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) > 0;
        }
    }

    @Test
    @Order(10)
    void getOrders_Organization_AfterAccept() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(13)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].acceptedAt");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) >= 0;
        }
    }

    @Test
    @Order(11)
    void getEmployees_SortByOrders_AfterAccept() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/employees?orders=desc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<List<CurrentOrder>> orders = JsonPath.read(content, "$.content[*].orderList");

        for (int i = 1; i < orders.size(); i++) {
            if (orders.get(i - 1) == null || orders.get(i) == null) {
                continue;
            }
            assert orders.get(i - 1).size() >= orders.get(i).size();
        }

    }

    @Test
    @Order(12)
    void placeOrder() throws Exception {
        CreateAdRequest request = new CreateAdRequest(
                "order",
                "Created Order",
                "Description of created Order",
                BigDecimal.valueOf(2000),
                "Regular size",
                LocalDate.parse("2024-10-10")
        );

        MockMultipartFile textPart = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "image data 1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "image2.jpg", "image/jpeg", "image data 2".getBytes());
        MockMultipartFile image3 = new MockMultipartFile("images", "image3.jpg", "image/jpeg", "image data 3".getBytes());

        mockMvc.perform(multipart("/v1/market")
                        .file(textPart)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @Order(12)
    void placeProduct() throws Exception {
        CreateAdRequest request = new CreateAdRequest(
                "product",
                "Created Product",
                "Description of created Product",
                BigDecimal.valueOf(200_000),
                null,
                null
        );

        MockMultipartFile textPart = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "image data 1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "image2.jpg", "image/jpeg", "image data 2".getBytes());
        MockMultipartFile image3 = new MockMultipartFile("images", "image3.jpg", "image/jpeg", "image data 3".getBytes());

        mockMvc.perform(multipart("/v1/market")
                        .file(textPart)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @Order(13)
    void getMarketProducts_AfterPlaceProduct() throws Exception {
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
    @Order(13)
    void getMarketOrders_AfterPlaceOrder() throws Exception {
        mockMvc.perform(get("/v1/market?type=orders"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(1))
                );
    }
}