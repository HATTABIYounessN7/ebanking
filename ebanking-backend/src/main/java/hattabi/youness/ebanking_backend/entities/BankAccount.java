package hattabi.youness.ebanking_backend.entities;

import java.time.LocalDateTime;
import java.util.List;

import hattabi.youness.ebanking_backend.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", length = 2)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BankAccount {

    @Id
    private String id;

    private double balance;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @ManyToOne
    private Customer customer;

    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY)
    private List<AccountOperation> accountOperations;
}
