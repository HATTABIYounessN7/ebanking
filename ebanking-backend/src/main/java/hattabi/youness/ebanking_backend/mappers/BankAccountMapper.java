package hattabi.youness.ebanking_backend.mappers;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

@Service
public class BankAccountMapper {

    public CustomerDTO fromCustomer(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        return customerDTO;
    }

    public Customer fromCustomerDTO(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        return customer;
    }

    public CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentAccount) {
        CurrentBankAccountDTO currentBankAccountDTO = new CurrentBankAccountDTO();
        BeanUtils.copyProperties(currentAccount, currentBankAccountDTO);
        currentBankAccountDTO.setCustomerDTO(fromCustomer(currentAccount.getCustomer()));
        currentBankAccountDTO.setType(currentAccount.getClass().getSimpleName());
        return currentBankAccountDTO;
    }

    public CurrentAccount fromCurrentBanckAccountDTO(CurrentBankAccountDTO currentBankAccountDTO) {
        CurrentAccount currentAccount = new CurrentAccount();
        BeanUtils.copyProperties(currentBankAccountDTO, currentAccount);
        currentAccount.setCustomer(fromCustomerDTO(currentBankAccountDTO.getCustomerDTO()));
        currentAccount.setStatus(
                currentBankAccountDTO.getStatus() != null ? currentBankAccountDTO.getStatus() : AccountStatus.CREATED);
        return currentAccount;
    }

    public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount) {
        SavingBankAccountDTO savingBankAccountDTO = new SavingBankAccountDTO();
        BeanUtils.copyProperties(savingAccount, savingBankAccountDTO);
        savingBankAccountDTO.setCustomerDTO(fromCustomer(savingAccount.getCustomer()));
        savingBankAccountDTO.setType(savingAccount.getClass().getSimpleName());
        return savingBankAccountDTO;
    }

    public SavingAccount fromSavingBankAccountDTO(SavingBankAccountDTO savingBankAccountDTO) {
        SavingAccount savingAccount = new SavingAccount();
        BeanUtils.copyProperties(savingBankAccountDTO, savingAccount);
        savingAccount.setCustomer(fromCustomerDTO(savingBankAccountDTO.getCustomerDTO()));
        savingAccount.setStatus(
                savingBankAccountDTO.getStatus() != null ? savingBankAccountDTO.getStatus() : AccountStatus.CREATED);
        return savingAccount;
    }

    public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation) {
        AccountOperationDTO accountOperationDTO = new AccountOperationDTO();
        BeanUtils.copyProperties(accountOperation, accountOperationDTO);
        return accountOperationDTO;
    }

    public BankAccountDTO fromBankAccount(BankAccount bankAccount) {
        if (bankAccount instanceof SavingAccount savingAccount) {
            return fromSavingBankAccount(savingAccount);
        } else if (bankAccount instanceof CurrentAccount currentAccount) {
            return fromCurrentBankAccount(currentAccount);
        }

        throw new IllegalArgumentException("Unknown Bank Account Type : " + bankAccount.getClass().getSimpleName());
    }
}
