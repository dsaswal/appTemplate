package dev.dsa.dto;

import dev.dsa.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Status is required")
    private Account.AccountStatus status;
}
