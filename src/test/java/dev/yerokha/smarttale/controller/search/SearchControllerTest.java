package dev.yerokha.smarttale.controller.search;

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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(10)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchControllerTest {

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
    void search_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/search")
                        .param("q", "product")
                        .param("con", "my_product")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(1)
    void search_My_Products() throws Exception {
        Thread.sleep(1000);
        login("existing2@example.com");

        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "product")
                        .param("con", "my_product")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(5))
                );
    }

    @Test
    @Order(2)
    void search_My_Orders() throws Exception {
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "order")
                        .param("con", "my_order")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(5))
                );
    }

    @Test
    @Order(3)
    void search_My_Orders_iDD_False() throws Exception {
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "ord")
                        .param("con", "my_order")
                        .param("iDD", "false")
                        .param("size", "10")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(10)),
                        jsonPath("$.totalElements").value(30)
                );
    }

    @Test
    @Order(4)
    void search_My_Advertisements_iDD_False() throws Exception {
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "")
                        .param("con", "my_advertisement")
                        .param("iDD", "false")
                        .param("size", "10")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(10)),
                        jsonPath("$.totalElements").value(41)
                );
    }

    @Test
    @Order(5)
    void search_Employees() throws Exception {
        Thread.sleep(1000);
        login("existing4@example.com");
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "sixth")
                        .param("con", "EMPLOYEE")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(1)),
                        jsonPath("$.totalElements").value(1)
                );
    }

    @Test
    @Order(5)
    void search_Org_Orders() throws Exception {
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "order")
                        .param("con", "ORG_order")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(5)),
                        jsonPath("$.totalElements").value(24)
                );
    }

    @Test
    @Order(6)
    void search_Purchases() throws Exception {
        Thread.sleep(1000);
        login("existing3@example.com");
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "product")
                        .param("con", "purchase")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(6)
                );
    }

    @Test
    @Order(7)
    void search_Organizations() throws Exception {
        mockMvc.perform(get("/v1/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "Org")
                        .param("con", "organization")
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(3)
                );
    }
}