package dev.yerokha.smarttale.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.Action;
import dev.yerokha.smarttale.dto.EditImage;
import dev.yerokha.smarttale.dto.UpdateAdRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.service.ImageService;
import dev.yerokha.smarttale.service.MailService;
import org.hamcrest.collection.IsCollectionWithSize;
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
import java.util.ArrayList;
import java.util.List;

import static dev.yerokha.smarttale.controller.AuthenticationControllerTest.extractToken;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(3)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdvertisementControllerTest {

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
    void getAllAds() throws Exception {
        login("existing2@example.com");
        MvcResult result = mockMvc.perform(get("/v1/advertisements")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", IsCollectionWithSize.hasSize(10))
                ).andReturn();

        // assert that all ads are sorted by publishedAt in descending order
        String content = result.getResponse().getContentAsString();
        List<String> publishedDates = JsonPath.read(content, "$.content[*].publishedAt");
        for (int i = 1; i < 10; i++) {
            assert publishedDates.get(i - 1).compareTo(publishedDates.get(i)) > 0;
        }
    }

    @Test
    @Order(2)
    void getOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/advertisements?q=orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", IsCollectionWithSize.hasSize(10)),
                        jsonPath("$.content[*].orderId").exists(),
                        jsonPath("$.content[*].productId").doesNotExist()
                ).andReturn();

        // assert that all ads are sorted by publishedAt in descending order
        String content = result.getResponse().getContentAsString();
        List<String> publishedDates = JsonPath.read(content, "$.content[*].publishedAt");
        for (int i = 1; i < 10; i++) {
            assert publishedDates.get(i - 1).compareTo(publishedDates.get(i)) > 0;
        }
    }

    @Test
    @Order(2)
    void getProducts() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/advertisements?q=products")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", IsCollectionWithSize.hasSize(10)),
                        jsonPath("$.content[*].productId").exists(),
                        jsonPath("$.content[*].orderId").doesNotExist()
                ).andReturn();

        // assert that all ads are sorted by publishedAt in descending order
        String content = result.getResponse().getContentAsString();
        List<String> publishedDates = JsonPath.read(content, "$.content[*].publishedAt");
        for (int i = 1; i < 10; i++) {
            assert publishedDates.get(i - 1).compareTo(publishedDates.get(i)) > 0;
        }
    }

    @Test
    @Order(3)
    void getOneAd() throws Exception {
        mockMvc.perform(get("/v1/advertisements/100019")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.orderId").exists(),
                        jsonPath("$.title").value("Order 10")
                );
    }

    @Test
    @Order(4)
    void deleteAd() throws Exception {
        mockMvc.perform(delete("/v1/advertisements/100012/3")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        content().string("Ad deleted")
                );

        // TODO assert that deleted status is true

    }

    @Test
    @Order(5)
    void updateAd() throws Exception {
        List<EditImage> editImageList = new ArrayList<>();
        editImageList.add(new EditImage(0, 0, Action.ADD, 0));
        editImageList.add(new EditImage(0, 1, Action.ADD, 1));
        editImageList.add(new EditImage(0, 2, Action.ADD, 2));
        editImageList.add(new EditImage(0, 3, Action.ADD, 3));
        UpdateAdRequest request = new UpdateAdRequest(
                100010L,
                "Updated Title",
                "Updated Description",
                BigDecimal.valueOf(100_000),
                "10x10",
                LocalDate.of(2025, 12, 31),
                editImageList
        );

        MockMultipartFile part = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "image data 1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "image2.jpg", "image/jpeg", "image data 2".getBytes());
        MockMultipartFile image3 = new MockMultipartFile("images", "image3.jpg", "image/jpeg", "image data 3".getBytes());
        MockMultipartFile image4 = new MockMultipartFile("images", "image4.jpg", "image/jpeg", "image data 4".getBytes());

        mockMvc.perform(multipart(PUT, "/v1/advertisements")
                        .file(part)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .file(image4)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        content().string("Advertisement updated successfully!")
                );

    }

}