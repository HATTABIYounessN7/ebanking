package hattabi.youness.ebanking_backend.dtos;

import java.util.Date;

import hattabi.youness.ebanking_backend.enums.AccountStatus;
import lombok.Data;

@Data
public class SavingBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;
}
