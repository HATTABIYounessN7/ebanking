package hattabi.youness.ebanking_backend.services;

import java.util.List;

import hattabi.youness.ebanking_backend.dtos.AccountHistoryDTO;
import hattabi.youness.ebanking_backend.dtos.AccountOperationDTO;
import hattabi.youness.ebanking_backend.dtos.BankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CurrentBankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CustomerDTO;
import hattabi.youness.ebanking_backend.dtos.SavingBankAccountDTO;
import hattabi.youness.ebanking_backend.exceptions.BalanceNotSufficientException;
import hattabi.youness.ebanking_backend.exceptions.BankAccountNotFoundException;
import hattabi.youness.ebanking_backend.exceptions.CustomerNotFoundException;

public interface BankAccountService {
        CustomerDTO saveCustomer(CustomerDTO customerDTO);

        CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

        CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException;

        void deleteCustomer(Long customerId) throws CustomerNotFoundException;

        List<CustomerDTO> listCustomers();

        List<CustomerDTO> searchCustomers(String keyword);

        CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
                        throws CustomerNotFoundException;

        SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
                        throws CustomerNotFoundException;

        BankAccountDTO geBankAccount(String bankAccountId) throws BankAccountNotFoundException;

        List<BankAccountDTO> listBankAccounts();

        List<BankAccountDTO> getCustomerAccounts(Long customerId) throws CustomerNotFoundException;

        void debit(String accountId, double amount, String description)
                        throws BankAccountNotFoundException, BalanceNotSufficientException;

        void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;

        void transfer(String accountIdSource, String accountIdDestination, double amount)
                        throws BankAccountNotFoundException, BalanceNotSufficientException;

        List<AccountOperationDTO> accountHistory(String accountId);

        AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;
}
