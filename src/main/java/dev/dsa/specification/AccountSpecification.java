package dev.dsa.specification;

import dev.dsa.dto.AccountSearchRequest;
import dev.dsa.entity.Account;
import dev.dsa.entity.Customer;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AccountSpecification {

    public static Specification<Account> withSearchCriteria(AccountSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text search with LIKE (case-insensitive)
            if (searchRequest.getAccountRef() != null && !searchRequest.getAccountRef().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("accountRef")),
                    "%" + searchRequest.getAccountRef().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getAccountName() != null && !searchRequest.getAccountName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("accountName")),
                    "%" + searchRequest.getAccountName().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getCurrency() != null && !searchRequest.getCurrency().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("currency")),
                    "%" + searchRequest.getCurrency().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getCreatedBy() != null && !searchRequest.getCreatedBy().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("createdBy")),
                    "%" + searchRequest.getCreatedBy().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getUpdatedBy() != null && !searchRequest.getUpdatedBy().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("updatedBy")),
                    "%" + searchRequest.getUpdatedBy().toLowerCase() + "%"
                ));
            }

            // Customer name search (join)
            if (searchRequest.getCustomerName() != null && !searchRequest.getCustomerName().trim().isEmpty()) {
                Join<Account, Customer> customerJoin = root.join("customer", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(customerJoin.get("name")),
                    "%" + searchRequest.getCustomerName().toLowerCase() + "%"
                ));
            }

            // Status exact match
            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
            }

            // Account ID range
            if (searchRequest.getAccountIdFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("id"),
                    searchRequest.getAccountIdFrom()
                ));
            }

            if (searchRequest.getAccountIdTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("id"),
                    searchRequest.getAccountIdTo()
                ));
            }

            // Customer ID range
            if (searchRequest.getCustomerIdFrom() != null) {
                Join<Account, Customer> customerJoin = root.join("customer", JoinType.INNER);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    customerJoin.get("id"),
                    searchRequest.getCustomerIdFrom()
                ));
            }

            if (searchRequest.getCustomerIdTo() != null) {
                Join<Account, Customer> customerJoin = root.join("customer", JoinType.INNER);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    customerJoin.get("id"),
                    searchRequest.getCustomerIdTo()
                ));
            }

            // Created date range
            if (searchRequest.getCreatedAtFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    searchRequest.getCreatedAtFrom()
                ));
            }

            if (searchRequest.getCreatedAtTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    searchRequest.getCreatedAtTo()
                ));
            }

            // Updated date range
            if (searchRequest.getUpdatedAtFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"),
                    searchRequest.getUpdatedAtFrom()
                ));
            }

            if (searchRequest.getUpdatedAtTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"),
                    searchRequest.getUpdatedAtTo()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
