package dev.dsa.service;

import dev.dsa.dto.AccountSearchRequest;
import dev.dsa.entity.Account;
import dev.dsa.entity.Customer;
import dev.dsa.exception.BusinessException;
import dev.dsa.exception.ResourceNotFoundException;
import dev.dsa.repository.AccountRepository;
import dev.dsa.repository.CustomerRepository;
import dev.dsa.specification.AccountSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Transactional
    public Account createAccount(Account account, Long customerId) {
        log.info("Creating account: {} for customer: {}", account.getAccountRef(), customerId);

        if (accountRepository.existsByAccountRef(account.getAccountRef())) {
            throw new BusinessException("Account reference already exists: " + account.getAccountRef());
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        account.setCustomer(customer);
        Account savedAccount = accountRepository.save(account);

        auditService.logAction("CREATE", "Account", savedAccount.getId(),
            "Created account: " + savedAccount.getAccountRef() + " for customer: " + customer.getName(),
            null, savedAccount.toString());

        return savedAccount;
    }

    @Transactional
    public Account updateAccount(Long id, Account accountDetails) {
        log.info("Updating account: {}", id);

        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        String oldValue = account.toString();

        account.setAccountName(accountDetails.getAccountName());
        account.setCurrency(accountDetails.getCurrency());
        account.setStatus(accountDetails.getStatus());

        Account updatedAccount = accountRepository.save(account);

        auditService.logAction("UPDATE", "Account", updatedAccount.getId(),
            "Updated account: " + updatedAccount.getAccountRef(),
            oldValue, updatedAccount.toString());

        return updatedAccount;
    }

    @Transactional
    public void deleteAccount(Long id) {
        log.info("Deleting account: {}", id);

        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        String accountRef = account.getAccountRef();
        accountRepository.delete(account);

        auditService.logAction("DELETE", "Account", id,
            "Deleted account: " + accountRef, account.toString(), null);
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByStatus(Account.AccountStatus status) {
        return accountRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccountByRef(String accountRef) {
        return accountRepository.findByAccountRef(accountRef);
    }

    @Transactional(readOnly = true)
    public List<Account> searchAccounts(AccountSearchRequest searchRequest) {
        log.info("Searching accounts with criteria: {}", searchRequest);

        if (searchRequest == null || searchRequest.isEmpty()) {
            return getAllAccounts();
        }

        Specification<Account> specification = AccountSpecification.withSearchCriteria(searchRequest);
        return accountRepository.findAll(specification);
    }
}
