package hattabi.youness.ebanking_chatbot.banking;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

public class BankingApiModels {
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginResponse {
        private String token;
        private String username;
        private String roles;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Customer {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankAccount {
        private String id;
        private double balance;
        private String status;
        private String type;
        private CustomerSummary customerDTO;
        private Double overDraft;
        private Double interestRate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountOperation {
        private Long id;
        private String operationDate;
        private double amount;
        private String type;
        private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountHistory {
        private String accountId;
        private double balance;
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private List<AccountOperation> accountOperationDTOS;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DebitRequest {
        private String accountId;
        private double amount;
        private String description;

        public DebitRequest(String accountId,
                double amount,
                String description) {
            this.accountId = accountId;
            this.amount = amount;
            this.description = description;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditRequest {
        private String accountId;
        private double amount;
        private String description;

        public CreditRequest(String accountId,
                double amount,
                String description) {
            this.accountId = accountId;
            this.amount = amount;
            this.description = description;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransferRequest {
        private String accountSource;
        private String accountDestination;
        private double amount;
        private String description;

        public TransferRequest(String accountSource,
                String accountDestination,
                double amount,
                String description) {
            this.accountSource = accountSource;
            this.accountDestination = accountDestination;
            this.amount = amount;
            this.description = description;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiError {
        private String error;
        private String message;
    }
}
