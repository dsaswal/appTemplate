package dev.dsa.controller;

import dev.dsa.entity.Customer;
import dev.dsa.service.CustomerService;
import dev.dsa.service.UserProfileService;
import dev.dsa.util.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public String listCustomers(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) Integer size,
                               Model model) {
        // Get user's preferred page size
        int userPageSize = userProfileService.getCurrentUserPageSize();

        // Create pageable (developer can override by passing size parameter)
        Pageable pageable = PaginationUtil.createPageableWithUserPreference(page, userPageSize, size);

        // Get customers with pagination
        Page<Customer> customerPage = customerService.getAllCustomersWithPagination(pageable);

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("page", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalElements", customerPage.getTotalElements());
        return "customers/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String createCustomer(@Valid @ModelAttribute Customer customer,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customers/form";
        }

        try {
            customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("success", "Customer created successfully");
        } catch (Exception e) {
            log.error("Error creating customer", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        return "customers/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String updateCustomer(@PathVariable Long id,
                                @Valid @ModelAttribute Customer customer,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customers/form";
        }

        try {
            customerService.updateCustomer(id, customer);
            redirectAttributes.addFlashAttribute("success", "Customer updated successfully");
        } catch (Exception e) {
            log.error("Error updating customer", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public String viewCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        return "customers/view";
    }
}
