package hattabi.youness.ebanking_chatbot.banking;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankingApiClient {
    private final WebClient webClient;

    @Value("${banking.api.username}")
    private String apiUsername;

    @Value("${banking.api.password}")
    private String apiPassword;

    private String jwtToken;

    @PostConstruct
    public void authenticate() {
        log.info("Authenticating chatbot with banking API...");
        try {
            BankingApiModels.LoginResponse response = webClient.post()
                    .uri("/auth/login")
                    .bodyValue(new BankingApiModels.LoginRequest(
                            apiUsername, apiPassword))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> Mono.error(new RuntimeException("Auth failed")))
                    .bodyToMono(BankingApiModels.LoginResponse.class)
                    .block();

            if (response != null) {
                this.jwtToken = response.getToken();
                log.info("✅ Chatbot authenticated successfully");
            }
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            log.warn("Bot will start without authentication — " +
                    "banking API calls will fail until API is available");
        }
    }

    private <T> T executeWithAuth(
            Supplier<T> apiCall) {
        try {
            return apiCall.get();
        } catch (Exception e) {
            if (e.getMessage() != null &&
                    e.getMessage().contains("401")) {
                log.warn("Token expired, re-authenticating...");
                authenticate();
                return apiCall.get();
            }
            throw e;
        }
    }

    public List<BankingApiModels.Customer> getAllCustomers() {
        return executeWithAuth(() -> webClient.get()
                .uri("/customers")
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .bodyToFlux(BankingApiModels.Customer.class)
                .collectList()
                .block());
    }

    public Optional<BankingApiModels.Customer> getCustomer(Long id) {
        return executeWithAuth(() -> {
            try {
                BankingApiModels.Customer customer = webClient.get()
                        .uri("/customers/{id}", id)
                        .header("Authorization", "Bearer " + jwtToken)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                res -> Mono.error(new RuntimeException("Customer not found")))
                        .bodyToMono(BankingApiModels.Customer.class)
                        .block();
                return Optional.ofNullable(customer);
            } catch (Exception e) {
                log.warn("Customer {} not found: {}", id, e.getMessage());
                return Optional.empty();
            }
        });
    }

    public List<BankingApiModels.Customer> searchCustomers(
            String keyword) {
        return executeWithAuth(() -> webClient.get()
                .uri("/customers/search?keyword={keyword}", keyword)
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .bodyToFlux(BankingApiModels.Customer.class)
                .collectList()
                .block());
    }

    public List<BankingApiModels.BankAccount> getAllAccounts() {
        return executeWithAuth(() -> webClient.get()
                .uri("/accounts")
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .bodyToFlux(BankingApiModels.BankAccount.class)
                .collectList()
                .block());
    }

    public Optional<BankingApiModels.BankAccount> getAccount(
            String accountId) {
        return executeWithAuth(() -> {
            try {
                BankingApiModels.BankAccount account = webClient.get()
                        .uri("/accounts/{id}", accountId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                res -> Mono.error(new RuntimeException("Account not found")))
                        .bodyToMono(BankingApiModels.BankAccount.class)
                        .block();
                return Optional.ofNullable(account);
            } catch (Exception e) {
                log.warn("Account {} not found: {}", accountId,
                        e.getMessage());
                return Optional.empty();
            }
        });
    }

    public List<BankingApiModels.BankAccount> getCustomerAccounts(
            Long customerId) {
        return executeWithAuth(() -> webClient.get()
                .uri("/customers/{id}/accounts", customerId)
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .bodyToFlux(BankingApiModels.BankAccount.class)
                .collectList()
                .block());
    }

    public Optional<BankingApiModels.AccountHistory> getAccountHistory(
            String accountId, int page, int size) {
        return executeWithAuth(() -> {
            try {
                BankingApiModels.AccountHistory history = webClient.get()
                        .uri("/accounts/{id}/pageOperations?page={page}&size={size}",
                                accountId, page, size)
                        .header("Authorization", "Bearer " + jwtToken)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                res -> Mono.error(new RuntimeException("Account not found")))
                        .bodyToMono(BankingApiModels.AccountHistory.class)
                        .block();
                return Optional.ofNullable(history);
            } catch (Exception e) {
                log.warn("History not found for {}: {}",
                        accountId, e.getMessage());
                return Optional.empty();
            }
        });
    }

    public String debit(String accountId,
            double amount,
            String description) {
        return executeWithAuth(() -> {
            try {
                webClient.post()
                        .uri("/accounts/debit")
                        .header("Authorization", "Bearer " + jwtToken)
                        .bodyValue(new BankingApiModels.DebitRequest(
                                accountId, amount, description))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(BankingApiModels.ApiError.class)
                                .flatMap(err -> Mono.error(
                                        new RuntimeException(err.getMessage()))))
                        .toBodilessEntity()
                        .block();
                return "success";
            } catch (Exception e) {
                log.error("Debit failed: {}", e.getMessage());
                return "error:" + e.getMessage();
            }
        });
    }

    public String credit(String accountId,
            double amount,
            String description) {
        return executeWithAuth(() -> {
            try {
                webClient.post()
                        .uri("/accounts/credit")
                        .header("Authorization", "Bearer " + jwtToken)
                        .bodyValue(new BankingApiModels.CreditRequest(
                                accountId, amount, description))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(BankingApiModels.ApiError.class)
                                .flatMap(err -> Mono.error(
                                        new RuntimeException(err.getMessage()))))
                        .toBodilessEntity()
                        .block();
                return "success";
            } catch (Exception e) {
                log.error("Credit failed: {}", e.getMessage());
                return "error:" + e.getMessage();
            }
        });
    }

    public String transfer(String sourceId,
            String destinationId,
            double amount,
            String description) {
        return executeWithAuth(() -> {
            try {
                webClient.post()
                        .uri("/accounts/transfer")
                        .header("Authorization", "Bearer " + jwtToken)
                        .bodyValue(new BankingApiModels.TransferRequest(
                                sourceId, destinationId, amount, description))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(BankingApiModels.ApiError.class)
                                .flatMap(err -> Mono.error(
                                        new RuntimeException(err.getMessage()))))
                        .toBodilessEntity()
                        .block();
                return "success";
            } catch (Exception e) {
                log.error("Transfer failed: {}", e.getMessage());
                return "error:" + e.getMessage();
            }
        });
    }

    public String formatAccountsAsText(
            List<BankingApiModels.BankAccount> accounts) {
        if (accounts == null || accounts.isEmpty())
            return "No accounts found.";

        StringBuilder sb = new StringBuilder();
        accounts.forEach(acc -> sb.append("""
                Account ID: %s
                Type: %s
                Balance: %.2f MAD
                Status: %s
                Owner: %s
                %s
                ---
                """.formatted(
                acc.getId(),
                acc.getType(),
                acc.getBalance(),
                acc.getStatus(),
                acc.getCustomerDTO() != null
                        ? acc.getCustomerDTO().getName()
                        : "N/A",
                acc.getOverDraft() != null
                        ? "Overdraft limit: " + acc.getOverDraft() + " MAD"
                        : acc.getInterestRate() != null
                                ? "Interest rate: " + acc.getInterestRate() + "%"
                                : "")));
        return sb.toString();
    }

    public String formatCustomersAsText(
            List<BankingApiModels.Customer> customers) {
        if (customers == null || customers.isEmpty())
            return "No customers found.";

        StringBuilder sb = new StringBuilder();
        customers.forEach(c -> sb.append("""
                ID: %d | Name: %s | Email: %s
                """.formatted(c.getId(), c.getName(), c.getEmail())));
        return sb.toString();
    }

    public String formatHistoryAsText(
            BankingApiModels.AccountHistory history) {
        if (history == null)
            return "No history found.";

        StringBuilder sb = new StringBuilder();
        sb.append("Account: ").append(history.getAccountId()).append("\n");
        sb.append("Balance: ").append(
                String.format("%.2f MAD", history.getBalance())).append("\n\n");
        sb.append("Recent operations:\n");

        history.getAccountOperationDTOS().forEach(op -> sb.append("- ")
                .append(op.getType())
                .append(" | ")
                .append(String.format("%.2f MAD", op.getAmount()))
                .append(" | ")
                .append(op.getDescription())
                .append("\n"));
        return sb.toString();
    }
}
