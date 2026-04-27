package hattabi.youness.ebanking_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("CA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAccount extends BankAccount {
    private double overDraft;
}
