package dev.dsa.api.controller;

import dev.dsa.api.dto.ApiResponse;
import dev.dsa.dto.AccountSearchRequest;
import dev.dsa.entity.Account;
import dev.dsa.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "Account management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountApiController {

    private final AccountService accountService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccounts() {
        log.info("API: Getting all accounts");
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public ResponseEntity<ApiResponse<Page<Account>>> searchAccounts(
        @RequestBody AccountSearchRequest searchRequest,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        log.info("API: Searching accounts with criteria: {}", searchRequest);
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountService.searchAccountsWithPagination(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public ResponseEntity<ApiResponse<Account>> getAccountById(@PathVariable Long id) {
        log.info("API: Getting account by id: {}", id);
        Account account = accountService.getAccountById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("Account", id));
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByCustomer(@PathVariable Long customerId) {
        log.info("API: Getting accounts for customer: {}", customerId);
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public ResponseEntity<ApiResponse<Account>> createAccount(
        @Valid @RequestBody Account account,
        @RequestParam Long customerId) {

        log.info("API: Creating account for customer: {}", customerId);
        Account created = accountService.createAccount(account, customerId);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public ResponseEntity<ApiResponse<Account>> updateAccount(
        @PathVariable Long id,
        @Valid @RequestBody Account account) {

        log.info("API: Updating account: {}", id);
        Account updated = accountService.updateAccount(id, account);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        log.info("API: Deleting account: {}", id);
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
