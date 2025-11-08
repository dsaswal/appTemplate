package dev.dsa.controller;

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

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public String listAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "accounts/list";
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public String listAccountsByCustomer(@PathVariable Long customerId, Model model) {
        Customer customer = customerService.getCustomerById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accountService.getAccountsByCustomerId(customerId));
        return "accounts/customer-accounts";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String newAccountForm(@RequestParam(required = false) Long customerId, Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("statuses", Account.AccountStatus.values());
        return "accounts/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
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
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String editAccountForm(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("account", account);
        model.addAttribute("statuses", Account.AccountStatus.values());
        return "accounts/edit";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String updateAccount(@PathVariable Long id,
                               @Valid @ModelAttribute Account account,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", Account.AccountStatus.values());
            return "accounts/edit";
        }

        try {
            Account existingAccount = accountService.getAccountById(id).orElseThrow();
            accountService.updateAccount(id, account);
            redirectAttributes.addFlashAttribute("success", "Account updated successfully");
            return "redirect:/accounts/customer/" + existingAccount.getCustomer().getId();
        } catch (Exception e) {
            log.error("Error updating account", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
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
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public String viewAccount(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("account", account);
        return "accounts/view";
    }
}
