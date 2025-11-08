package dev.dsa.dto;

import dev.dsa.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchRequest {

    // Text search fields
    private String accountRef;
    private String accountName;
    private String currency;
    private String customerName;
    private String createdBy;
    private String updatedBy;

    // Status filter
    private Account.AccountStatus status;

    // Customer ID range
    private Long customerIdFrom;
    private Long customerIdTo;

    // Account ID range
    private Long accountIdFrom;
    private Long accountIdTo;

    // Date range filters
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
    private LocalDateTime updatedAtFrom;
    private LocalDateTime updatedAtTo;

    // Helper method to check if search is empty
    public boolean isEmpty() {
        return isBlankOrNull(accountRef) && isBlankOrNull(accountName) && isBlankOrNull(currency) &&
               isBlankOrNull(customerName) && isBlankOrNull(createdBy) && isBlankOrNull(updatedBy) &&
               status == null && customerIdFrom == null && customerIdTo == null &&
               accountIdFrom == null && accountIdTo == null &&
               createdAtFrom == null && createdAtTo == null &&
               updatedAtFrom == null && updatedAtTo == null;
    }

    private boolean isBlankOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }
}
