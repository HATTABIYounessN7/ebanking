package hattabi.youness.ebanking_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("SA")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccount extends BankAccount {
    private double interestRate;
}
