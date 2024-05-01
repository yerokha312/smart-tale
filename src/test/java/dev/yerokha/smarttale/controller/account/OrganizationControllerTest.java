package dev.yerokha.smarttale.controller.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.service.ImageService;
import dev.yerokha.smarttale.service.MailService;
import org.assertj.core.api.Assertions;
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

import java.util.Comparator;
import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(6)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrganizationControllerTest {

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
    void getOrders() throws Exception {
        login("existing4@example.com");
        MvcResult result = mockMvc.perform(get("/v1/account/organization/orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(12)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].acceptedAt");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) >= 0;
        }
    }

    @Test
    @Order(2)
    void getOrders_SortedByTitleAsc() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/orders?title=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(12)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.read(content, "$.content[*].title");
        for (int i = 1; i < titles.size(); i++) {
            assert titles.get(i - 1).compareTo(titles.get(i)) <= 0;
        }
    }

    @Test
    @Order(2)
    void getOrders_SortedByTitleDesc() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/orders?title=desc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(12)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> titles = JsonPath.read(content, "$.content[*].title");
        for (int i = 1; i < titles.size(); i++) {
            assert titles.get(i - 1).compareTo(titles.get(i)) >= 0;
        }
    }

    @Test
    @Order(2)
    void getOrders_SortedByTitleDescAndByAcceptedDateAsc() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/orders?title=desc&acceptedAt=Asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(12)
                )
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> acceptedDates = JsonPath.read(content, "$.content[*].acceptedAt");
        for (int i = 1; i < acceptedDates.size(); i++) {
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) <= 0;
        }
    }

    @Test
    @Order(3)
    void getEmployees() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/employees")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(4)))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> names = JsonPath.read(content, "$.content[*].name");
        for (int i = 1; i < names.size(); i++) {
            assert names.get(i - 1).compareTo(names.get(i)) <= 0;
        }
    }

    @Test
    @Order(3)
    void getEmployees_SortByOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/employees?orders=desc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(4)))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<List<CurrentOrder>> orders = JsonPath.read(content, "$.content[*].orderList");
        for (int i = 1; i < orders.size(); i++) {
            assert orders.get(i - 1).size() >= orders.get(i).size();
        }
    }

    @Test
    @Order(4)
    void inviteEmployee_NotExisting() throws Exception {
        InviteRequest request = new InviteRequest(
                "test@example.com",
                100001L

        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/v1/account/organization/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(json)
                        .contentType(APP_JSON))
                .andExpectAll(
                        status().isCreated(),
                        content().string("Invite sent to test@example.com")
                );

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendInvitation(
                eq("test@example.com"),
                eq(null),
                eq("Test Org"),
                eq("Position 2"),
                linkCaptor.capture());
    }

    @Test
    @Order(5)
    void inviteEmployee_Existing() throws Exception {
        InviteRequest request = new InviteRequest(
                "existing8@example.com",
                100001L

        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/v1/account/organization/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .content(json)
                        .contentType(APP_JSON))
                .andExpectAll(
                        status().isCreated(),
                        content().string("Invite sent to existing8@example.com")
                );

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mailService).sendInvitation(
                eq("existing8@example.com"),
                eq("ZEighth Existing Profile"),
                eq("Test Org"),
                eq("Position 2"),
                linkCaptor.capture());

    }

    @Test
    @Order(6)
    void getEmployees_AfterInvite() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/employees?name=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(6)),
                        jsonPath("$.content[*].status").value(hasItem("Invited")))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> names = JsonPath.read(content, "$.content[*].name");

        Comparator<String> nameComparator = (name1, name2) -> {
            if (name1.isEmpty() && !name2.isEmpty()) {
                return 1; // name1 (empty) should be considered greater
            } else if (!name1.isEmpty() && name2.isEmpty()) {
                return -1; // name1 should be considered less than name2 (empty)
            } else {
                return name1.compareTo(name2); // Compare non-empty strings as usual
            }
        };

        Assertions.assertThat(names).isSortedAccordingTo(nameComparator);

        List<String> statuses = JsonPath.read(content, "$.content[*].status");

        long invitedCount = statuses.stream().filter(status -> status.equals("Invited")).count();

        assertEquals(2, invitedCount);
    }

    @Test
    @Order(7)
    void getEmployees_SortByPosition() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/account/organization/employees?position=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content", hasSize(6)),
                        jsonPath("$.content[*].status").value(hasItem("Invited")))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> statuses = JsonPath.read(content, "$.content[*].status");
        List<String> positions = JsonPath.read(content, "$.content[*].position");

        for (int i = 1; i < positions.size(); i++) {
            assert statuses.get(i).equals("Invited") || positions.get(i - 1).compareTo(positions.get(i)) <= 0;
        }
    }

    @Test
    @Order(8)
    void getPositions() throws Exception {
        mockMvc.perform(get("/v1/account/organization/positions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray()
                );
    }
}