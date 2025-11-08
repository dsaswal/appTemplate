package dev.dsa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_ref", columnList = "accountRef"),
    @Index(name = "idx_customer_id", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account reference is required")
    @Column(nullable = false, unique = true, length = 50, name = "account_ref")
    private String accountRef;

    @NotBlank(message = "Currency is required")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotBlank(message = "Account name is required")
    @Column(nullable = false, length = 100, name = "account_name")
    private String accountName;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        CLOSED,
        SUSPENDED
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountRef='" + accountRef + '\'' +
                ", currency='" + currency + '\'' +
                ", accountName='" + accountName + '\'' +
                ", status=" + status +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                '}';
    }
}
