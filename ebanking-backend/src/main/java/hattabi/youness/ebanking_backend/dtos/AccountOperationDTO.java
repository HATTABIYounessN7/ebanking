package hattabi.youness.ebanking_backend.dtos;

import java.util.Date;

import hattabi.youness.ebanking_backend.enums.OperationType;
import lombok.Data;

@Data
public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private OperationType type;
    private String description;
}
