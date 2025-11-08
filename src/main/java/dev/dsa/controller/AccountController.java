package dev.dsa.controller;

import dev.dsa.dto.AccountSearchRequest;
import dev.dsa.dto.AccountUpdateRequest;
import dev.dsa.entity.Account;
import dev.dsa.entity.Customer;
import dev.dsa.service.AccountService;
import dev.dsa.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public String listAccounts(@ModelAttribute AccountSearchRequest searchRequest, Model model) {
        List<Account> accounts;

        if (searchRequest == null || searchRequest.isEmpty()) {
            accounts = accountService.getAllAccounts();
        } else {
            accounts = accountService.searchAccounts(searchRequest);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("searchRequest", searchRequest != null ? searchRequest : new AccountSearchRequest());
        model.addAttribute("statuses", Account.AccountStatus.values());
        return "accounts/list";
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public String searchAccounts(@ModelAttribute AccountSearchRequest searchRequest, Model model) {
        return listAccounts(searchRequest, model);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public String listAccountsByCustomer(@PathVariable Long customerId, Model model) {
        Customer customer = customerService.getCustomerById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accountService.getAccountsByCustomerId(customerId));
        return "accounts/customer-accounts";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public String newAccountForm(@RequestParam(required = false) Long customerId, Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("statuses", Account.AccountStatus.values());
        return "accounts/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public String createAccount(@Valid @ModelAttribute Account account,
                               BindingResult result,
                               @RequestParam Long customerId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("selectedCustomerId", customerId);
            model.addAttribute("statuses", Account.AccountStatus.values());
            return "accounts/form";
        }

        try {
            accountService.createAccount(account, customerId);
            redirectAttributes.addFlashAttribute("success", "Account created successfully");
            return "redirect:/accounts/customer/" + customerId;
        } catch (Exception e) {
            log.error("Error creating account", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/new?customerId=" + customerId;
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public String editAccountForm(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id)
            .orElseThrow(() -> new RuntimeException("Account not found"));

        // Create DTO from existing account
        AccountUpdateRequest updateRequest = AccountUpdateRequest.builder()
            .accountName(account.getAccountName())
            .currency(account.getCurrency())
            .status(account.getStatus())
            .build();

        model.addAttribute("account", account);
        model.addAttribute("accountUpdate", updateRequest);
        model.addAttribute("statuses", Account.AccountStatus.values());
        return "accounts/edit";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public String updateAccount(@PathVariable Long id,
                               @Valid @ModelAttribute("accountUpdate") AccountUpdateRequest accountUpdate,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Account account = accountService.getAccountById(id).orElseThrow();
            model.addAttribute("account", account);
            model.addAttribute("statuses", Account.AccountStatus.values());
            return "accounts/edit";
        }

        try {
            // Get existing account to preserve customer relationship
            Account existingAccount = accountService.getAccountById(id).orElseThrow();

            // Create account object with updated fields
            Account accountToUpdate = Account.builder()
                .accountName(accountUpdate.getAccountName())
                .currency(accountUpdate.getCurrency())
                .status(accountUpdate.getStatus())
                .build();

            accountService.updateAccount(id, accountToUpdate);
            redirectAttributes.addFlashAttribute("success", "Account updated successfully");
            return "redirect:/accounts/customer/" + existingAccount.getCustomer().getId();
        } catch (Exception e) {
            log.error("Error updating account", e);
            Account account = accountService.getAccountById(id).orElse(null);
            if (account != null) {
                model.addAttribute("account", account);
                model.addAttribute("statuses", Account.AccountStatus.values());
                model.addAttribute("error", e.getMessage());
                return "accounts/edit";
            }
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ACCOUNT_DELETE')")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.getAccountById(id).orElseThrow();
            Long customerId = account.getCustomer().getId();
            accountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("success", "Account deleted successfully");
            return "redirect:/accounts/customer/" + customerId;
        } catch (Exception e) {
            log.error("Error deleting account", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ACCOUNT_READ', 'ACCOUNT_WRITE')")
    public String viewAccount(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("account", account);
        return "accounts/view";
    }
}
