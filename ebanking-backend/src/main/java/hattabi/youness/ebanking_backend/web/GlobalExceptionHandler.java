package hattabi.youness.ebanking_backend.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import hattabi.youness.ebanking_backend.exceptions.BalanceNotSufficientException;
import hattabi.youness.ebanking_backend.exceptions.BankAccountNotFoundException;
import hattabi.youness.ebanking_backend.exceptions.CustomerNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCustomerNotFound(CustomerNotFoundException e) {
        return Map.of(
                "error", "Customer Not Found",
                "message", e.getMessage());
    }

    @ExceptionHandler(BankAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBankAccountNotFound(BankAccountNotFoundException e) {
        return Map.of(
                "error", "Bank Account Not Found",
                "message", e.getMessage());
    }

    @ExceptionHandler(BalanceNotSufficientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBalanceNotSufficient(BalanceNotSufficientException e) {
        return Map.of(
                "error", "Balance Not Sufficient",
                "message", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenericException(Exception e) {
        return Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage());
    }
}
