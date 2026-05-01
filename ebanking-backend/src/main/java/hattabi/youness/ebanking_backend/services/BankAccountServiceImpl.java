package hattabi.youness.ebanking_backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import hattabi.youness.ebanking_backend.dtos.AccountHistoryDTO;
import hattabi.youness.ebanking_backend.dtos.AccountOperationDTO;
import hattabi.youness.ebanking_backend.dtos.BankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CurrentBankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CustomerDTO;
import hattabi.youness.ebanking_backend.dtos.SavingBankAccountDTO;
import hattabi.youness.ebanking_backend.entities.AccountOperation;
import hattabi.youness.ebanking_backend.entities.BankAccount;
import hattabi.youness.ebanking_backend.entities.CurrentAccount;
import hattabi.youness.ebanking_backend.entities.Customer;
import hattabi.youness.ebanking_backend.entities.SavingAccount;
import hattabi.youness.ebanking_backend.enums.AccountStatus;
import hattabi.youness.ebanking_backend.enums.OperationType;
import hattabi.youness.ebanking_backend.exceptions.BalanceNotSufficientException;
import hattabi.youness.ebanking_backend.exceptions.BankAccountNotFoundException;
import hattabi.youness.ebanking_backend.exceptions.CustomerNotFoundException;
import hattabi.youness.ebanking_backend.mappers.BankAccountMapper;
import hattabi.youness.ebanking_backend.repositories.AccountOperationRepository;
import hattabi.youness.ebanking_backend.repositories.BankAccountRepository;
import hattabi.youness.ebanking_backend.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
        private final CustomerRepository customerRepository;
        private final BankAccountRepository bankAccountRepository;
        private final AccountOperationRepository accountOperationRepository;
        private final BankAccountMapper mapper;

        @Override
        public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
                log.info("Saving new customer: {}", customerDTO.getName());
                Customer customer = mapper.fromCustomerDTO(customerDTO);
                Customer savedCustomer = customerRepository.save(customer);
                return mapper.fromCustomer(savedCustomer);
        }

        @Override
        public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
                Customer customer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with id: " + customerId));
                return mapper.fromCustomer(customer);
        }

        @Override
        public CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
                customerRepository.findById(customerDTO.getId())
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with id: " + customerDTO.getId()));
                Customer customer = mapper.fromCustomerDTO(customerDTO);
                Customer savedCustomer = customerRepository.save(customer);
                return mapper.fromCustomer(savedCustomer);
        }

        @Override
        public void deleteCustomer(Long customerId) throws CustomerNotFoundException {
                customerRepository.findById(customerId)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with id: " + customerId));
                customerRepository.deleteById(customerId);
        }

        @Override
        public List<CustomerDTO> listCustomers() {
                return customerRepository.findAll()
                                .stream().map(mapper::fromCustomer).collect(Collectors.toList());
        }

        @Override
        public List<CustomerDTO> searchCustomers(String keyword) {
                return customerRepository.searchCustomers("%" + keyword + "%")
                                .stream().map(mapper::fromCustomer).collect(Collectors.toList());
        }

        @Override
        public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
                        throws CustomerNotFoundException {
                Customer customer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with id: " + customerId));

                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setCreatedAt(LocalDateTime.now());
                currentAccount.setBalance(initialBalance);
                currentAccount.setOverDraft(overDraft);
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(customer);

                CurrentAccount saved = bankAccountRepository.save(currentAccount);
                log.info("Current Account created for customer : {}", customer.getName());
                return mapper.fromCurrentBankAccount(saved);
        }

        @Override
        public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
                        throws CustomerNotFoundException {
                Customer customer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with id: " + customerId));

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setCreatedAt(LocalDateTime.now());
                savingAccount.setBalance(initialBalance);
                savingAccount.setInterestRate(interestRate);
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(customer);

                SavingAccount saved = bankAccountRepository.save(savingAccount);
                log.info("Saving\'s Account created for customer: {}", customer.getName());
                return mapper.fromSavingBankAccount(saved);
        }

        @Override
        public BankAccountDTO geBankAccount(String bankAccountId) throws BankAccountNotFoundException {
                BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                                .orElseThrow(() -> new BankAccountNotFoundException(
                                                "Bank account not found with id: " + bankAccountId));
                return mapper.fromBankAccount(bankAccount);
        }

        @Override
        public List<BankAccountDTO> listBankAccounts() {
                return bankAccountRepository.findAll()
                                .stream().map(mapper::fromBankAccount).collect(Collectors.toList());
        }

        @Override
        public List<BankAccountDTO> getCustomerAccounts(Long customerId) throws CustomerNotFoundException {
                customerRepository.findById(customerId)
                                .orElseThrow(() -> new CustomerNotFoundException(
                                                "Customer not found with id: " + customerId));

                return bankAccountRepository.findByCustomerId(customerId)
                                .stream().map(mapper::fromBankAccount).collect(Collectors.toList());
        }

        @Override
        public void debit(String accountId, double amount, String description)
                        throws BankAccountNotFoundException, BalanceNotSufficientException {
                BankAccount bankAccount = bankAccountRepository.findById(accountId)
                                .orElseThrow(() -> new BankAccountNotFoundException(
                                                "Bank account not found with id: " + accountId));

                if (bankAccount.getBalance() < amount) {
                        throw new BalanceNotSufficientException(
                                        "Insufficient balance. Available: " + bankAccount.getBalance());
                }

                AccountOperation operation = new AccountOperation();
                operation.setType(OperationType.DEBIT);
                operation.setAmount(amount);
                operation.setDescription(description);
                operation.setOperationDate(LocalDateTime.now());
                operation.setBankAccount(bankAccount);
                accountOperationRepository.save(operation);

                bankAccount.setBalance(bankAccount.getBalance() - amount);
                bankAccountRepository.save(bankAccount);
                log.info("Debit of {} from account {}", amount, accountId);
        }

        @Override
        public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
                BankAccount bankAccount = bankAccountRepository.findById(accountId)
                                .orElseThrow(() -> new BankAccountNotFoundException(
                                                "Bank account not found with id: " + accountId));

                AccountOperation operation = new AccountOperation();
                operation.setType(OperationType.CREDIT);
                operation.setAmount(amount);
                operation.setDescription(description);
                operation.setOperationDate(LocalDateTime.now());
                operation.setBankAccount(bankAccount);
                accountOperationRepository.save(operation);

                bankAccount.setBalance(bankAccount.getBalance() + amount);
                bankAccountRepository.save(bankAccount);
                log.info("Credit of {} to account {}", amount, accountId);
        }

        @Override
        public void transfer(String accountIdSource, String accountIdDestination, double amount)
                        throws BankAccountNotFoundException, BalanceNotSufficientException {
                debit(accountIdSource, amount, "Transfer to " + accountIdDestination);
                credit(accountIdDestination, amount, "Transfer to " + accountIdSource);
                log.info("Transfer of {} from {} to {}", amount, accountIdSource, accountIdDestination);
        }

        @Override
        public List<AccountOperationDTO> accountHistory(String accountId) {
                return accountOperationRepository.findByBankAccountId(accountId)
                                .stream().map(mapper::fromAccountOperation).collect(Collectors.toList());
        }

        @Override
        public AccountHistoryDTO getAccountHistory(String accountId, int page, int size)
                        throws BankAccountNotFoundException {
                BankAccount bankAccount = bankAccountRepository.findById(accountId)
                                .orElseThrow(() -> new BankAccountNotFoundException(
                                                "Bank account not found with id: " + accountId));

                Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId,
                                PageRequest.of(page, size));
                AccountHistoryDTO historyDTO = new AccountHistoryDTO();
                historyDTO.setAccountId(bankAccount.getId());
                historyDTO.setBalance(bankAccount.getBalance());
                historyDTO.setCurrentPage(page);
                historyDTO.setPageSize(size);
                historyDTO.setTotalPages(accountOperations.getTotalPages());
                historyDTO.setAccountOperationDTOs(accountOperations.getContent()
                                .stream().map(mapper::fromAccountOperation).collect(Collectors.toList()));

                return historyDTO;
        }
}
