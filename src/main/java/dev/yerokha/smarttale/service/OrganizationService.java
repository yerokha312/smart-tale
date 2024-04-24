package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.enums.OrderStatus.ARRIVED;
import static dev.yerokha.smarttale.enums.OrderStatus.CANCELED;
import static java.lang.Integer.parseInt;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrderRepository orderRepository;

    public OrganizationService(OrganizationRepository organizationRepository, OrderRepository orderRepository) {
        this.organizationRepository = organizationRepository;
        this.orderRepository = orderRepository;
    }

    public Page<CurrentOrder> getOrders(Long ownerId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                getSort(params)
        );

        List<OrderStatus> inactiveStatuses = Arrays.asList(ARRIVED, CANCELED);

        return orderRepository
                .findAllByAcceptedByOrganizationOwnerUserIdAndStatusNotIn(
                        ownerId,
                        inactiveStatuses,
                        pageable)
                .map(AdMapper::toCurrentOrder);
    }

    private Sort getSort(Map<String, String> params) {
        List<Sort.Order> orders = new ArrayList<>();
        params.forEach((key, value) -> {
            if (!(key.startsWith("page") || key.startsWith("size"))) {
                Sort.Direction direction = value.equalsIgnoreCase("asc") ?
                        Sort.Direction.ASC : Sort.Direction.DESC;
                orders.add(new Sort.Order(direction, key));
            }
        });
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    public Page<Employee> getEmployees(Long ownerId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                getSort(params)
        );

        OrganizationEntity organization = organizationRepository.findByOwnerUserId(ownerId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        List<OrderStatus> inactiveStatuses = Arrays.asList(ARRIVED, CANCELED);

        List<Employee> employees = organization.getEmployees().stream()
                .map(employee -> {
                    String name = employee.getLastName() + " " + employee.getFirstName() + " " + employee.getMiddleName();
                    return new Employee(
                            employee.getUserId(),
                            name,
                            employee.getEmail(),
                            employee.getAcceptedOrders().stream()
                                    .filter(order -> !inactiveStatuses.contains(order.getStatus()))
                                    .map(AdMapper::toCurrentOrder)
                                    .toList(),
                            employee.getPosition().getTitle(),
                            null
                    );
                })
                .collect(Collectors.toList());

        return new PageImpl<>(employees, pageable, organization.getEmployees().size());

    }
}
