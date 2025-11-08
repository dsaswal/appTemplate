package dev.dsa.repository;

import dev.dsa.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByAccountRef(String accountRef);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByStatus(Account.AccountStatus status);

    List<Account> findByCurrency(String currency);

    boolean existsByAccountRef(String accountRef);
}
