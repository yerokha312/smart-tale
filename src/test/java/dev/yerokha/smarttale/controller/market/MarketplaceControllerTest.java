package dev.yerokha.smarttale.controller.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.CreateJobRequest;
import dev.yerokha.smarttale.dto.CreateOrderRequest;
import dev.yerokha.smarttale.dto.CreateProductRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.PurchaseRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.enums.ContactInfo;
import dev.yerokha.smarttale.enums.JobType;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    private static String code;


    private void login(String email) throws Exception {
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
    void getProducts_Authenticated() throws Exception {
        Thread.sleep(1000);
        login("existing3@example.com");
        MvcResult result = mockMvc.perform(get("/v1/market?type=products")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(10))
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
                        jsonPath("$.content", hasSize(10))
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
    void getPurchases_BeforePurchase() throws Exception {
        mockMvc.perform(get("/v1/account/purchases")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(5))
                );
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
                captor.capture()
        );

        PurchaseRequest request = captor.getValue();

        assert request.title().equals("Product 10");
        assert request.buyerEmail().equals("existing3@example.com");
        assert request.buyerPhoneNumber().equals("+777712345690");

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
                        jsonPath("$.content", hasSize(10))
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
    void getOrders_Organization_BeforeAccept() throws Exception {
        login("existing4@example.com");
        MvcResult result = mockMvc.perform(get("/v1/organization/orders?active=true")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(19)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].acceptedAt");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) >= 0;
        }
    }

    @Test
    @Order(9)
    void accept() throws Exception {
        mockMvc.perform(put("/v1/market/100040")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendAcceptanceRequest(
                eq("existing2@example.com"),
                any(),
                captor.capture()
        );

        code = captor.getValue();
        assert code != null;

    }

    @Test
    @Order(9)
    void accept_Should409() throws Exception {
        mockMvc.perform(put("/v1/market/100040")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(10)
    void confirmOrder() throws Exception {
        Thread.sleep(1000);
        login("existing2@example.com");
        mockMvc.perform(post("/v1/account/orders" + code)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    void getOrders_Organization_AfterAccept() throws Exception {
        Thread.sleep(1000);
        login("existing4@example.com");
        MvcResult result = mockMvc.perform(get("/v1/organization/orders?active=true")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(20)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].acceptedAt");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) >= 0;
        }
    }

    @Test
    @Order(12)
    void getEmployees_SortByOrders_AfterAccept() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/organization/employees?orders=desc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<List<OrderSummary>> orders = JsonPath.read(content, "$.content[*].orderList");

        for (int i = 1; i < orders.size(); i++) {
            if (orders.get(i - 1) == null || orders.get(i) == null) {
                continue;
            }
            assert orders.get(i - 1).size() >= orders.get(i).size();
        }

    }

    @Test
    @Order(13)
    void placeOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                "Created Order",
                "Description of created Order",
                BigDecimal.valueOf(2000),
                "Regular size",
                LocalDate.parse("2024-10-10"),
                ContactInfo.EMAIL_PHONE
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
                .andExpectAll(
                        status().isCreated(),
                        content().string("Order created")
                );
    }

    @Test
    @Order(14)
    void placeProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Created Product",
                "Description of created Product",
                BigDecimal.valueOf(200_000),
                ContactInfo.EMAIL_PHONE
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
                .andExpectAll(
                        status().isCreated(),
                        content().string("Product created")
                );
    }

    @Test
    @Order(15)
    void getMarketProducts_AfterPlaceProduct() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/market?type=products"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(10))
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<String> dates = JsonPath.read(content, "$.content[*].publishedAt");

        for (int i = 1; i < dates.size(); i++) {
            assert dates.get(i - 1).compareTo(dates.get(i)) >= 0;
        }
    }

    @Test
    @Order(16)
    void getMarketOrders_AfterPlaceOrder() throws Exception {
        mockMvc.perform(get("/v1/market?type=orders"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(1))
                );
    }

    @Test
    @Order(30)
    void placeJob() throws Exception {
        CreateJobRequest job = new CreateJobRequest(
                "Job title",
                "Job description",
                BigDecimal.valueOf(1000),
                LocalDate.now().plusDays(1),
                ContactInfo.EMAIL_PHONE,
                JobType.FULL_TIME,
                "Job location"
        );

        MockMultipartFile textPart = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(job)
        );

        mockMvc.perform(multipart("/v1/market")
                        .file(textPart)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isCreated(),
                        content().string("Job created")
                );
    }
}