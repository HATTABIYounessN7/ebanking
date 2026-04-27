package hattabi.youness.ebanking_backend.entities;

import java.util.Date;

import hattabi.youness.ebanking_backend.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date operationDate;
    private double amount;
    private String description;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    private BankAccount bankAccount;
}
