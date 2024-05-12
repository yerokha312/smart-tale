package dev.yerokha.smarttale.controller.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.CreateOrgRequest;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.repository.PositionRepository;
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
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Comparator;
import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @Autowired
    PositionRepository positionRepository;

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
    void getOrganizations_Public() throws Exception {
        mockMvc.perform(get("/v1/organizations"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.totalElements").value(2));
    }

    @Test
    @Order(1)
    void getOrganization_Public() throws Exception {
        mockMvc.perform(get("/v1/organizations/100000"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value("First Organization"));
    }

    @Test
    @Order(1)
    void getOrders_NoParam() throws Exception {
        login("existing4@example.com");
        MvcResult result = mockMvc.perform(get("/v1/organization/orders")
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
    @Order(2)
    void getOrders_SortedByTitleAsc() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/organization/orders?active=true&title=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(19)
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
        MvcResult result = mockMvc.perform(get("/v1/organization/orders?active=true&title=desc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(19)
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
        MvcResult result = mockMvc.perform(get("/v1/organization/orders?active=true&title=desc&acceptedAt=Asc")
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
            assert acceptedDates.get(i - 1).compareTo(acceptedDates.get(i)) <= 0;
        }
    }

    @Test
    @Order(3)
    void getEmployees() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/organization/employees")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(4))
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
        MvcResult result = mockMvc.perform(get("/v1/organization/employees?orders=desc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(4))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<List<OrderSummary>> orders = JsonPath.read(content, "$.content[*].orderList");
        for (int i = 1; i < orders.size(); i++) {
            assert orders.get(i - 1).size() >= orders.get(i).size();
        }
    }

    @Test
    @Order(4)
    void inviteEmployee_NotExisting() throws Exception {
        InviteRequest request = new InviteRequest(
                null,
                null,
                null,
                "test@example.com",
                "+996 123456789",
                100001L

        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/v1/organization/employees")
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
                eq("First Organization"),
                eq("Position 2"),
                linkCaptor.capture());
    }

    @Test
    @Order(5)
    void inviteEmployee_Existing() throws Exception {
        InviteRequest request = new InviteRequest(
                null,
                null,
                null,
                "existing8@example.com",
                "+996757483939",
                100001L

        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/v1/organization/employees")
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
                eq("First Organization"),
                eq("Position 2"),
                linkCaptor.capture());

    }

    @Test
    @Order(6)
    void getEmployees_AfterInvite() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/organization/employees?name=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(6),
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
        MvcResult result = mockMvc.perform(get("/v1/organization/employees?position=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(6),
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
    @Order(7)
    void getEmployee_ActiveOrders() throws Exception {
        mockMvc.perform(get("/v1/organization/employees/100005")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.employee.name").value("Sixth Existing Profile"),
                        jsonPath("$.tasks.totalElements").value(4)
                );

    }

    @Test
    @Order(7)
    void getEmployee_CompletedOrders() throws Exception {
        mockMvc.perform(get("/v1/organization/employees/100005?active=false")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.employee.name").value("Sixth Existing Profile"),
                        jsonPath("$.tasks.totalElements").value(1)
                );

    }

    @Test
    @Order(8)
    void getPositions() throws Exception {
        mockMvc.perform(get("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray()
                );
    }

    @Test
    @Order(9)
    void getOrganization() throws Exception {
        mockMvc.perform(get("/v1/organization")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value("First Organization"),
                        jsonPath("$.ownerName").value("Fourth Existing Profile")
                );
    }

    @Test
    @Order(10)
    void getOrganization_Should401() throws Exception {
        mockMvc.perform(get("/v1/organization"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(11)
    void createOrganization() throws Exception {
        Thread.sleep(1000);
        login("existing2@example.com");
        CreateOrgRequest request = new CreateOrgRequest(
                "Third Organization",
                "No description"
        );

        MockMultipartFile textPart = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/v1/organization")
                        .file(textPart)
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpectAll(
                        status().isCreated(),
                        content().string("Organization created")
                );
    }

    @Test
    @Order(11)
    void createOrganization_Should403() throws Exception {
        CreateOrgRequest request = new CreateOrgRequest(
                "Fourth Organization",
                "No description"
        );

        MockMultipartFile textPart = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile logo = new MockMultipartFile("logo", "image1.jpg", "image/jpeg", "image data 1".getBytes());

        mockMvc.perform(multipart("/v1/organization")
                        .file(textPart)
                        .file(logo)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    void updateOrganization() throws Exception {
        CreateOrgRequest request = new CreateOrgRequest(
                "Fourth Organization Update",
                "No description"
        );

        MockMultipartFile textPart = new MockMultipartFile(
                "dto", null, APP_JSON, objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile logo = new MockMultipartFile("logo", "image1.jpg", "image/jpeg", "image data 1".getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/v1/organization")
                        .file(textPart)
                        .file(logo)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    void createPosition() throws Exception {
        Thread.sleep(1000);
        login("existing2@example.com");
        Position position = new Position(
                null,
                "Test position",
                1,
                List.of("CREATE_POSITION"),
                1L
        );

        String json = objectMapper.writeValueAsString(position);

        mockMvc.perform(post("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

}/*

public record Position(
        Long positionId,
        @NotNull @Length(min = 2)
        String title,
        @NotNull @PositiveOrZero
        Integer hierarchy,
        @NotNull @Size(min = 1)
        List<String> authorities,
        @NotNull
        Long organizationId
) {
}
*/