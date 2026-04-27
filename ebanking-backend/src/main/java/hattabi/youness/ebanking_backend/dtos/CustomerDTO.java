package hattabi.youness.ebanking_backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;

    @NotEmpty(message = "Name is required")
    private String name;

    @Email(message = "Invalid Email")
    @NotEmpty(message = "Email is required")
    private String email;
}
