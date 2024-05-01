package dev.yerokha.smarttale.controller.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.service.MailService;
import org.json.JSONException;
import org.json.JSONObject;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(1)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    MailService mailService;
    @Autowired
    UserRepository userRepository;
    final String APP_JSON = "application/json";

    private static String initialVerificationCode;
    private static String verificationCode;
    private static String initialAccessToken;
    public static String accessToken;
    private static String refreshToken;

    @Test
    @Order(1)
    void registerCustomer() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Doe",
                "Father",
                "johndoe@example.com"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .contentType(APP_JSON)
                        .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("johndoe@example.com")));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendEmailVerificationCode(
                eq("johndoe@example.com"),
                confirmationUrlCaptor.capture()
        );

        verificationCode = confirmationUrlCaptor.getValue();
        initialVerificationCode = verificationCode;
    }

    @Test
    @Order(1)
    void register_ShortName() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "J",
                "D",
                "F",
                "regular@email.com"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name must be between 2 and 20 characters"));
    }

    @Test
    @Order(1)
    void register_NameFieldsWithDifferentLanguage() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John",
                "Доу",
                "Максимович",
                "regular@email.com"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name fields should be either all Latin or all Cyrillic"));
    }

    @Test
    @Order(1)
    void register_NameWithNumbers() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John 1",
                "Doe",
                "Fred",
                "regular@email.com"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("First name must be either Latin or Cyrillic, not a mix"));
    }

    @Test
    @Order(1)
    void register_BadEmail() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "John 1",
                "Doe",
                "Fred",
                "regular@emailcom"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/registration")
                        .content(json)
                        .contentType(APP_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    void checkAvailable_True() throws Exception {
        mockMvc.perform(post("/v1/auth/email-available")
                        .contentType(APP_JSON)
                        .content("available@example.com"))
                .andExpect(content().string("true"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    void checkAvailable_False() throws Exception {
        mockMvc.perform(post("/v1/auth/email-available")
                        .contentType(APP_JSON)
                        .content("existing@example.com"))
                .andExpect(content().string("false"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void resend_NotExistingEmail() throws Exception {
        String email = "notexisting@example.com";
        mockMvc.perform(post("/v1/auth/resend-verification")
                        .contentType(APP_JSON)
                        .content(email)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("notexisting@example.com")));
    }

    @Test
    @Order(3)
    void resend_InvalidEmail() throws Exception {
        String email = "johndoeexample.com";
        mockMvc.perform(post("/v1/auth/resend-verification")
                        .contentType(APP_JSON)
                        .content(email)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void resend() throws Exception {
        String email = "johndoe@example.com";
        mockMvc.perform(post("/v1/auth/resend-verification")
                        .contentType(APP_JSON)
                        .content(email)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("johndoe@example.com")));

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendEmailVerificationCode(
                eq("johndoe@example.com"),
                confirmationUrlCaptor.capture()
        );

        verificationCode = confirmationUrlCaptor.getValue();
    }

    @Test
    @Order(4)
    void verifyEmail_OldCodeShouldFail() throws Exception {
        VerificationRequest request = new VerificationRequest(
                "johndoe@example.com",
                initialVerificationCode
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/verification")
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid code, please try again"));
    }

    @Test
    @Order(5)
    void verifyEmail() throws Exception {
        VerificationRequest request = new VerificationRequest(
                "johndoe@example.com",
                verificationCode
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/v1/auth/verification")
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        accessToken = extractToken(responseContent, "accessToken");
        initialAccessToken = accessToken;
        refreshToken = extractToken(responseContent, "refreshToken");
    }

    @Test
    @Order(5)
    void checkAvailable_False2() throws Exception {
        mockMvc.perform(post("/v1/auth/email-available")
                        .contentType(APP_JSON)
                        .content("johndoe@example.com"))
                .andExpect(content().string("false"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void refreshToken() throws Exception {
        Thread.sleep(1000);
        MvcResult result = mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(APP_JSON)
                        .content("Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(initialAccessToken)))
                .andReturn();

        accessToken = extractToken(result.getResponse().getContentAsString(), "accessToken");
    }

    @Test
    @Order(6)
    void refreshToken_InvalidToken() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(APP_JSON)
                        .content("Bearer " + "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoiZXJib2xhdHRAbGl2ZS5jb20iLCJzY29wZXMiOiJVU0VSIiwiZXhwIjoxNzEwODQ1NTcyLCJ0b2tlblR5cGUiOiJSRUZSRVNIIiwiaWF0IjoxNzEwMjQwNzcyfQ.kkbdqPjcrut98KUWu2q6ah3LEflUiW7KLIHMjJsw9VLi6HVerIkIYwgm4c0qs4yPhiaW2YOU1e6u5afr18Iw5DsDdivHhLugEW83cC-lskruRrAmJKFbvyplL7bpxNFvKuEowlT_bLrNzjzKmutLr-5eYeEQahFap6YkEwm4XDo7MSeOfNtD3zvhsmZEQ05VKlxFnjL59-JuW_8tc8U4lHXIYIyCt4sJ8xozRYj2p2kco-ojNVZXXqKbEZpJ-81lqExxoC4VTVN7aamjqmpktNE58o2IakiA-IZVEs4riSBg3sB3VWp7fPLXDymqaMvHf2GOExM16KGUAg-K3X2NNA"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    void revoke() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                        .contentType(APP_JSON)
                        .content("Bearer " + refreshToken)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout success"));
    }

    @Test
    @Order(7)
    void revoke_OldToken() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                        .header("Authorization", "Bearer " + initialAccessToken)
                        .content("Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    void refreshToken_RevokedShouldFail() throws Exception {
        Thread.sleep(600);
        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(APP_JSON)
                        .content("Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token is revoked"));
    }

    @Test
    @Order(9)
    void login() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(APP_JSON)
                        .content("johndoe@example.com"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendLoginCode(
                eq("johndoe@example.com"),
                confirmationUrlCaptor.capture()
        );

        verificationCode = confirmationUrlCaptor.getValue();
    }

    @Test
    @Order(10)
    void verify_Login() throws Exception {
        VerificationRequest request = new VerificationRequest(
                "johndoe@example.com",
                verificationCode
        );

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/v1/auth/verification")
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        accessToken = extractToken(responseContent, "accessToken");
        initialAccessToken = accessToken;
        refreshToken = extractToken(responseContent, "refreshToken");

    }


    public static String extractToken(String responseContent, String tokenName) throws JSONException {
        JSONObject jsonResponse = new JSONObject(responseContent);
        return jsonResponse.getString(tokenName);
    }
}