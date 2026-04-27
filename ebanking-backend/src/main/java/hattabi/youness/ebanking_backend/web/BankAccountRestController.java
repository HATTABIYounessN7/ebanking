package hattabi.youness.ebanking_backend.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hattabi.youness.ebanking_backend.dtos.AccountHistoryDTO;
import hattabi.youness.ebanking_backend.dtos.AccountOperationDTO;
import hattabi.youness.ebanking_backend.dtos.BankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CreditDTO;
import hattabi.youness.ebanking_backend.dtos.CurrentBankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.DebitDTO;
import hattabi.youness.ebanking_backend.dtos.SavingBankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.TransferRequestDTO;
import hattabi.youness.ebanking_backend.exceptions.BalanceNotSufficientException;
import hattabi.youness.ebanking_backend.exceptions.BankAccountNotFoundException;
import hattabi.youness.ebanking_backend.exceptions.CustomerNotFoundException;
import hattabi.youness.ebanking_backend.services.BankAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api")
@CrossOrigin("*")
public class BankAccountRestController {
    private final BankAccountService bankAccountService;

    @GetMapping("/accounts")
    public List<BankAccountDTO> listAccounts() {
        return bankAccountService.listBankAccounts();
    }

    @GetMapping("/accounts/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.geBankAccount(accountId);
    }

    @GetMapping("/customers/{customerId}/accounts")
    public List<BankAccountDTO> getCustomerAccounts(@PathVariable Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomerAccounts(customerId);
    }

    @PostMapping("/accounts/current")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentBankAccountDTO saveCurrentAccount(@RequestParam double initialBalance, @RequestParam double overDraft,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        return bankAccountService.saveCurrentBankAccount(initialBalance, overDraft, customerId);
    }

    @PostMapping("/accounts/saving")
    @ResponseStatus(HttpStatus.CREATED)
    public SavingBankAccountDTO saveSavingAccount(@RequestParam double initialBalance,
            @RequestParam double interestRate, @RequestParam Long customerId) throws CustomerNotFoundException {
        return bankAccountService.saveSavingBankAccount(initialBalance, interestRate, customerId);
    }

    @PostMapping("/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
        return debitDTO;
    }

    @PostMapping("/accounts/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription());
        return creditDTO;
    }

    @PostMapping("/accounts/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.transfer(transferRequestDTO.getAccountSource(), transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }

    @GetMapping("/accounts/{accountId}/operations")
    public List<AccountOperationDTO> getAccountHistory(@PathVariable String accountId) {
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountPageHistory(@PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size)
            throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(accountId, page, size);
    }

}
