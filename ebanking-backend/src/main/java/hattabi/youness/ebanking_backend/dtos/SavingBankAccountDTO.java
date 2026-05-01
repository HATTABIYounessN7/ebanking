package hattabi.youness.ebanking_backend.dtos;

import java.time.LocalDateTime;

import hattabi.youness.ebanking_backend.enums.AccountStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SavingBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private LocalDateTime createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;
}
