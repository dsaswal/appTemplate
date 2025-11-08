package dev.dsa.api.controller;

import dev.dsa.api.dto.ApiResponse;
import dev.dsa.entity.Customer;
import dev.dsa.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerApiController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        log.info("API: Getting all customers");
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<Page<Customer>>> getCustomersPaged(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        log.info("API: Getting customers paged - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customers = customerService.getAllCustomersWithPagination(pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<List<Customer>>> getActiveCustomers() {
        log.info("API: Getting active customers");
        List<Customer> customers = customerService.getActiveCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        log.info("API: Getting customer by id: {}", id);
        Customer customer = customerService.getCustomerById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("Customer", id));
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<List<Customer>>> searchCustomers(@RequestParam String name) {
        log.info("API: Searching customers by name: {}", name);
        List<Customer> customers = customerService.searchCustomersByName(name);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@Valid @RequestBody Customer customer) {
        log.info("API: Creating customer: {}", customer.getName());
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.ok(ApiResponse.success("Customer created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
        @PathVariable Long id,
        @Valid @RequestBody Customer customer) {

        log.info("API: Updating customer: {}", id);
        Customer updated = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        log.info("API: Deleting customer: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }
}
