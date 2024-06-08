package dev.yerokha.smarttale.controller.organization;

import dev.yerokha.smarttale.dto.CreateOrgRequest;
import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.EmployeeTasksResponse;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.InviterInvitation;
import dev.yerokha.smarttale.dto.Job;
import dev.yerokha.smarttale.dto.JobSummary;
import dev.yerokha.smarttale.dto.OrderAccepted;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.dto.PositionDto;
import dev.yerokha.smarttale.dto.PositionSummary;
import dev.yerokha.smarttale.dto.UpdateEmployeeRequest;
import dev.yerokha.smarttale.dto.UpdateJobRequest;
import dev.yerokha.smarttale.service.AdvertisementService;
import dev.yerokha.smarttale.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getOrgIdFromAuthToken;
import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;
import static dev.yerokha.smarttale.util.ImageValidator.validateImage;

@Tag(name = "Organization", description = "Organization controller EPs")
@RestController
@RequestMapping("/v1/organization")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final AdvertisementService advertisementService;

    @Autowired
    public OrganizationController(OrganizationService organizationService, AdvertisementService advertisementService) {
        this.organizationService = organizationService;
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "Get own Org", tags = {"get", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Org not found", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<Organization> getOrganization(Authentication authentication) {
        return ResponseEntity.ok(organizationService.getOrganization(getUserIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "Create organization", tags = {"post", "organization"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "User is not subscribed or already in Organization"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PostMapping
    public ResponseEntity<String> createOrganization(@RequestPart("dto") @Valid CreateOrgRequest request,
                                                     @RequestPart(value = "logo", required = false) MultipartFile file,
                                                     Authentication authentication) {

        if (file != null) {
            validateImage(file);
        }

        organizationService.createOrganization(request, file, getUserIdFromAuthToken(authentication));

        return new ResponseEntity<>("Organization created", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update organization", tags = {"put", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "User is not an owner"),
                    @ApiResponse(responseCode = "404", description = "Org not found")
            }
    )
    @PutMapping
    public ResponseEntity<String> updateOrganization(@RequestPart("dto") @Valid CreateOrgRequest request,
                                                     @RequestPart(value = "logo", required = false) MultipartFile file,
                                                     Authentication authentication) {

        if (file != null) {
            validateImage(file);
        }

        organizationService.updateOrganization(request, file, getOrgIdFromAuthToken(authentication));

        return ResponseEntity.ok("Organization updated");
    }


    @Operation(
            summary = "Get order history", description = "Get all orders of organization",
            tags = {"organization", "get", "order", "monitoring"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "active", description = "true, null or false"),
                    @Parameter(name = "dateType", description = "accepted, deadline, completed"),
                    @Parameter(name = "dateFrom", description = "If dateType is not null, then dateFrom is required"),
                    @Parameter(name = "dateTo", description = "If dateType is not null, then dateTo is required"),
                    @Parameter(name = "page", description = "Page number. Default 0"),
                    @Parameter(name = "size", description = "Page size. Default 6"),
                    @Parameter(name = "[sort]", description = "Sorting property. Equals to object field. Can be multiple" +
                                                              "sorting properties. Default \"acceptedAt\"",
                            examples = {
                                    @ExampleObject(name = "deadlineAt", value = "asc", description = "\"sorting param=asc/desc\"")
                            })
            }
    )
    @GetMapping("/orders")
    public ResponseEntity<CustomPage<OrderAccepted>> getOrders(Authentication authentication,
                                                               @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getOrders(getOrgIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get employees", description = "Get all employees and invitees of organization",
            tags = {"organization", "get", "employee", "user", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number. Default 0"),
                    @Parameter(name = "size", description = "Page size. Default 10"),
                    @Parameter(name = "name", description = "Sorts by name. name=asc/desc"),
                    @Parameter(name = "orders", description = "Sorts by active orders number"),
                    @Parameter(name = "status", description = "Not a property, needs front implementation"),
                    @Parameter(name = "[sort]", description = "Sorting property. Equals to object field. Can be multiple" +
                                                              "sorting properties. Default \"name\""
                    )
            }
    )
    @GetMapping("/employees")
    public ResponseEntity<CustomPage<Employee>> getEmployees(Authentication authentication,
                                                             @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getEmployees(
                getOrgIdFromAuthToken(authentication),
                params));
    }

    @Operation(
            summary = "Get one employee", description = "Get employee and paged list of orders",
            tags = {"get", "employee", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number. Default 0"),
                    @Parameter(name = "size", description = "Page size. Default 10"),
                    @Parameter(name = "active", description = "true or false, default true. Returns nested paged list of orders"),
            }
    )
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeeTasksResponse> getEmployee(@PathVariable Long employeeId,
                                                             Authentication authentication,
                                                             @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getEmployee(getOrgIdFromAuthToken(authentication), employeeId, params));
    }

    @Operation(
            summary = "Positions dropdown in invite", description = "Get a list of positions to which user can invite. " +
                                                                    "Drop down request. Evaluates requesting user's permissions",
            tags = {"organization", "get", "position", "account", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "No permission", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            }
    )
    @GetMapping("/positions-dropdown")
    @PreAuthorize("hasPermission(#authentication, 'INVITE_EMPLOYEE')")
    public ResponseEntity<List<PositionSummary>> getPositionsDropdown(Authentication authentication) {
        return ResponseEntity.ok(organizationService.getPositionsDropdown(getUserIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "All positions", description = "Get all positions of organization",
            tags = {"organization", "get", "position", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "No permission", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            }
    )
    @GetMapping("/positions")
    public ResponseEntity<List<PositionSummary>> getAllPositions(Authentication authentication) {
        return ResponseEntity.ok(organizationService.getAllPositions(getOrgIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "Get position", description = "Get position by id",
            tags = {"organization", "get", "position", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "No permission", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User, position or organization not found", content = @Content)
            }
    )
    @GetMapping("/positions/{positionId}")
    public ResponseEntity<PositionDto> getOnePosition(Authentication authentication,
                                                      @PathVariable Long positionId) {
        return ResponseEntity.ok(organizationService.getOnePosition(getOrgIdFromAuthToken(authentication), positionId));
    }

    @Operation(
            summary = "Create position", description = "Evaluates hierarchy and authorities then creates. " +
                                                       "Position id should be empty or 0",
            tags = {"post", "position", "organization"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "User or org not found"),
            }
    )
    @PostMapping("/positions")
    @PreAuthorize("hasPermission(#position, 'CREATE_POSITION')")
    public ResponseEntity<String> createPosition(Authentication authentication, @Valid @RequestBody Position position) {
        organizationService.createPosition(getOrgIdFromAuthToken(authentication), position);

        return new ResponseEntity<>("Position created", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update position", description = "Evaluates hierarchy and authorities then updates," +
                                                       "positionId shouldn't be null",
            tags = {"put", "position", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "User, org or position not found"),
            }
    )
    @PutMapping("/positions")
    @PreAuthorize("hasPermission(#position, 'UPDATE_POSITION')")
    public ResponseEntity<String> updatePosition(Authentication authentication, @Valid @RequestBody Position position) {
        organizationService.updatePosition(getOrgIdFromAuthToken(authentication), position);

        return new ResponseEntity<>("Position updated", HttpStatus.OK);
    }

    @Operation(
            summary = "Invite employee", description = "Sends email to invited person's address",
            tags = {"post", "account", "user", "employee", "organization"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Invite successful"),
                    @ApiResponse(responseCode = "400", description = "Bad request. Email is validated"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User, org or position not found")
            }
    )
    @PostMapping("/employees")
    @PreAuthorize("hasPermission(#request.positionId(), 'PositionEntity', 'INVITE_EMPLOYEE')")
    public ResponseEntity<String> inviteEmployee(Authentication authentication, @RequestBody @Valid InviteRequest request) {
        organizationService.inviteEmployee(getUserIdFromAuthToken(authentication), request);
        return new ResponseEntity<>(String.format("Invite sent to %s", request.email()), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get invitations", description = "Returns inv-s sent by current org",
            tags = {"get", "invitation", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Has no role EMPLOYEE")
            }
    )
    @GetMapping("/invitations")
    public ResponseEntity<CustomPage<InviterInvitation>> getInvitations(Authentication authentication,
                                                                        @RequestParam(required = false,
                                                                                defaultValue = "0") int page,
                                                                        @RequestParam(required = false,
                                                                                defaultValue = "5") int size) {
        return ResponseEntity.ok(organizationService.getInvitations(getOrgIdFromAuthToken(authentication), page, size));
    }

    @Operation(
            summary = "Revoke invitation", description = "Deletes sent invitation. Requires DELETE_EMPLOYEE permission",
            tags = {"delete", "invitation", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deletion success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Has no permission"),
                    @ApiResponse(responseCode = "404", description = "Not found or does not belong to current org"),
            }
    )
    @DeleteMapping("/invitations/{invId}")
    @PreAuthorize("hasPermission(#invId, 'Invitation', 'DELETE_EMPLOYEE')")
    public ResponseEntity<String> deleteInvitation(Authentication authentication, @PathVariable Long invId) {
        organizationService.deleteInvitation(getOrgIdFromAuthToken(authentication), invId);
        return ResponseEntity.ok("Invitation revoked");
    }

    @Operation(
            summary = "Delete employee", tags = {"delete", "organization", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    @DeleteMapping("/employees/{employeeId}")
    @PreAuthorize("hasPermission(#employeeId, 'Employee', 'DELETE_EMPLOYEE')")
    public ResponseEntity<String> deleteEmployee(Authentication authentication,
                                                 @PathVariable Long employeeId) {

        organizationService.deleteEmployee(getOrgIdFromAuthToken(authentication), employeeId);

        return ResponseEntity.ok("Employee deleted from organization");

    }

    @Operation(
            summary = "Delete position", tags = {"delete", "position", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "Position not found")
            }
    )
    @DeleteMapping("/positions/{positionId}")
    @PreAuthorize("hasPermission(#positionId, 'PositionEntity', 'DELETE_POSITION')")
    public ResponseEntity<String> deletePosition(Authentication authentication,
                                                 @PathVariable Long positionId) {
        organizationService.deletePosition(getOrgIdFromAuthToken(authentication), positionId);

        return ResponseEntity.ok("Position deleted");
    }

    @Operation(
            summary = "Change employee position", tags = {"put", "position", "organization", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "Employee or Position not found")
            }
    )
    @PutMapping(value = "/employees")
    @PreAuthorize("hasPermission(#request.employeeId(), #request.positionId(), 'UPDATE_EMPLOYEE')")
    public ResponseEntity<String> updateEmployeePosition(Authentication authentication,
                                                         @RequestBody @Valid UpdateEmployeeRequest request) {

        organizationService.updateEmployee(getOrgIdFromAuthToken(authentication), request.employeeId(), request.positionId());

        return ResponseEntity.ok("Employee position updated");
    }

    @Operation(
            summary = "Get job ads of organization", description = "Returns all job ads of organization",
            tags = {"get", "advertisement", "job", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Has no EMPLOYEE role"),
                    @ApiResponse(responseCode = "404", description = "Organization not found")
            },
            parameters = {
                    @Parameter(name = "page", description = "Default 0"),
                    @Parameter(name = "size", description = "Default 5"),
            }
    )
    @GetMapping("/advertisements")
    public ResponseEntity<CustomPage<JobSummary>> getAdvertisements(Authentication authentication,
                                                                    @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getJobAds(getOrgIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get one job ad", tags = {"get", "job", "organization", "advertisement"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "User has no EMPLOYEE role"),
                    @ApiResponse(responseCode = "404", description = "Not found"),
            }
    )
    @GetMapping("/advertisements/{advertisementId}")
    public ResponseEntity<Job> getAdvertisement(Authentication authentication,
                                                @PathVariable Long advertisementId) {
        return ResponseEntity.ok(organizationService.getOneJobAd(getOrgIdFromAuthToken(authentication), advertisementId));
    }

    @Operation(
            summary = "Update job", tags = {"put", "job", "organization", "advertisement"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @PutMapping("/advertisements")
    @PreAuthorize("hasPermission(#request.jobId(), 'JobEntity', 'INVITE_EMPLOYEE')")
    public ResponseEntity<String> updateAdvertisement(Authentication authentication,
                                                      @Valid @RequestPart("dto") UpdateJobRequest request,
                                                      @RequestPart(value = "images", required = false) List<MultipartFile> files) {

        if (files != null && !files.isEmpty()) {
            for (var file : files) {
                validateImage(file);
            }
        }

        advertisementService.updateAd(getOrgIdFromAuthToken(authentication), request, files);
        return ResponseEntity.ok("Job ad updated");
    }

}
