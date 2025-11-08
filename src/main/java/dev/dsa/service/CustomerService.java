package dev.dsa.service;

import dev.dsa.entity.Customer;
import dev.dsa.exception.ResourceNotFoundException;
import dev.dsa.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Creating customer: {}", customer.getName());

        // createdBy and updatedBy are set automatically by JPA auditing
        Customer savedCustomer = customerRepository.save(customer);

        auditService.logAction("CREATE", "Customer", savedCustomer.getId(),
            "Created customer: " + savedCustomer.getName(), null, savedCustomer.toString());

        return savedCustomer;
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails) {
        log.info("Updating customer: {}", id);

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        String oldValue = customer.toString();

        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setActive(customerDetails.getActive());
        // updatedBy is set automatically by JPA auditing

        Customer updatedCustomer = customerRepository.save(customer);

        auditService.logAction("UPDATE", "Customer", updatedCustomer.getId(),
            "Updated customer: " + updatedCustomer.getName(), oldValue, updatedCustomer.toString());

        return updatedCustomer;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer: {}", id);

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        String customerName = customer.getName();
        customerRepository.delete(customer);

        auditService.logAction("DELETE", "Customer", id,
            "Deleted customer: " + customerName, customer.toString(), null);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomersWithPagination(Pageable pageable) {
        log.info("Getting customers with pagination: {}", pageable);
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getActiveCustomersWithPagination(Pageable pageable) {
        log.info("Getting active customers with pagination: {}", pageable);
        return customerRepository.findByActive(true, pageable);
    }
}
