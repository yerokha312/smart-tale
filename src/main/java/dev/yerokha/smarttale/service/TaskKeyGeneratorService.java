package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
public class TaskKeyGeneratorService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String generateTaskKey(OrganizationEntity organization) {
        String prefix = "T";

        LocalDate registeredDate = organization.getRegisteredAt();
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(registeredDate, currentDate);
        int years = period.getYears() + 1;

        int taskCount = getOrInitCounter(organization);

        return String.format("%s-%d-%d", prefix, years, taskCount);
    }

    private int getOrInitCounter(OrganizationEntity organization) {
        String queryStr = "SELECT max(o.taskKey) FROM OrderEntity o WHERE o.acceptedBy = :organization";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("organization", organization);

        String maxTaskId = (String) query.getSingleResult();

        if (maxTaskId != null) {
            int index = maxTaskId.lastIndexOf('-');
            int counter = Integer.parseInt(maxTaskId.substring(index + 1));
            return counter + 1;
        } else {
            return 1;
        }
    }

}
