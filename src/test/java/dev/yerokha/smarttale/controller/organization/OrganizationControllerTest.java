package dev.yerokha.smarttale.controller.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.yerokha.smarttale.dto.CreateOrgRequest;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.dto.UpdateEmployeeRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.repository.PositionRepository;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static dev.yerokha.smarttale.controller.account.AuthenticationControllerTest.extractToken;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(9)
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
    public static String refreshToken;


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
        refreshToken = extractToken(responseContent, "refreshToken");
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
        Thread.sleep(1000);
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
                        jsonPath("$.totalElements").value(6))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> names = JsonPath.read(content, "$.content[*].name");
        List<String> statuses = JsonPath.read(content, "$.content[*].status");
        for (int i = 1; i < names.size(); i++) {
            assert names.get(i - 1).compareTo(names.get(i)) <= 0 || statuses.get(i).equals("Invited");
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
                        jsonPath("$.totalElements").value(6))
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
                eq("Fourth Existing Profile"),
                any(),
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
                eq("Fourth Existing Profile"),
                any(),
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
                        jsonPath("$.totalElements").value(7),
                        jsonPath("$.content[*].status").value(hasItem("Invited")))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<String> names = JsonPath.read(content, "$.content[*].name");
        List<String> statuses = JsonPath.read(content, "$.content[*].status");

        for (int i = 1; i < names.size(); i++) {
            assert names.get(i - 1).compareTo(names.get(i)) <= 0 || statuses.get(i).equals("Invited");
        }

        long invitedCount = statuses.stream().filter(status -> status.equals("Invited")).count();

        assertEquals(3, invitedCount);
    }

    @Test
    @Order(7)
    void getEmployees_SortByPosition() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/organization/employees?position=asc")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.totalElements").value(7),
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
        mockMvc.perform(get("/v1/organization/employees/100006")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.employee.name").value("Seventh Existing Profile"),
                        jsonPath("$.tasks.totalElements").value(5)
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
    void getPositionsDropdown() throws Exception {
        mockMvc.perform(get("/v1/organization/positions-dropdown")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$", hasSize(3))
                );
    }

    @Test
    @Order(8)
    void getAllPositions() throws Exception {
        mockMvc.perform(get("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(5))
                );
    }

    @Test
    @Order(8)
    void getOnePosition() throws Exception {
        mockMvc.perform(get("/v1/organization/positions/100000")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.authorities", hasSize(9))
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
    @Order(12)
    void getEmployees_BeforeDeletion() throws Exception {
        mockMvc.perform(get("/v1/organization/employees")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.totalElements").value(7)
                );
    }

    @Test
    @Order(15)
    void deleteEmployee() throws Exception {
        mockMvc.perform(delete("/v1/organization/employees/100004")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(16)
    void getEmployees_AfterDeletion() throws Exception {
        mockMvc.perform(get("/v1/organization/employees")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.totalElements").value(6)
                );
    }

    @Test
    @Order(16)
    void checkFormerEmployeeRoles() {
        UserEntity user = userRepository.findById(100004L).get();

        assertFalse(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("EMPLOYEE")));
    }

    @Test
    @Order(17)
    void deletePosition() throws Exception {
        mockMvc.perform(delete("/v1/organization/positions/100006")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(17)
    void deletePosition_Should403() throws Exception {
        mockMvc.perform(delete("/v1/organization/positions/100002")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(17)
    void deletePosition_DeleteNotOwnPosition_Should404() throws Exception {
        mockMvc.perform(delete("/v1/organization/positions/100005")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(18)
    void getAllPositions_AfterDeletion() throws Exception {
        mockMvc.perform(get("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(4))
                );
    }

    @Test
    @Order(19)
    void updateEmployeePosition() throws Exception {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                100005L,
                100003L
        );

        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/v1/organization/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @Order(29)
    void createOrganization() throws Exception {
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
    @Order(30)
    void createPosition() throws Exception {
        Thread.sleep(1000);
        MvcResult result = mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = extractToken(result.getResponse().getContentAsString(), "accessToken");

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

    @Test
    @Order(31)
    void createPosition_Should403() throws Exception {
        Position position = new Position(
                null,
                "Test position",
                1,
                List.of("CREATE_POSITION"),
                100000L
        );

        String json = objectMapper.writeValueAsString(position);

        mockMvc.perform(post("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(32)
    void updatePosition_Should403_Hierarchy() throws Exception {
        Position position = new Position(
                1L,
                "Test position update",
                2,
                List.of("CREATE_POSITION"),
                1L
        );

        String json = objectMapper.writeValueAsString(position);

        mockMvc.perform(put("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(32)
    void updatePosition() throws Exception {
        Position position = new Position(
                2L,
                "Test position update",
                2,
                List.of("CREATE_POSITION"),
                1L
        );

        String json = objectMapper.writeValueAsString(position);

        mockMvc.perform(put("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @Order(33)
    void updatePosition_Should403() throws Exception {
        Position position = new Position(
                100003L,
                "Test position update",
                3,
                List.of("CREATE_POSITION"),
                100000L
        );

        String json = objectMapper.writeValueAsString(position);

        mockMvc.perform(put("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(31)
    void createPosition_Should403_CannotChoose() throws Exception {
        Position position = new Position(
                null,
                "Position for update position without create position permission",
                1,
                List.of("UPDATE_POSITION"),
                1L
        );

        String json = objectMapper.writeValueAsString(position);

        mockMvc.perform(post("/v1/organization/positions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(APP_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

}