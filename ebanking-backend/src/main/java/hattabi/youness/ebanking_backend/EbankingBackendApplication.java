package hattabi.youness.ebanking_backend;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import hattabi.youness.ebanking_backend.dtos.BankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CurrentBankAccountDTO;
import hattabi.youness.ebanking_backend.dtos.CustomerDTO;
import hattabi.youness.ebanking_backend.dtos.SavingBankAccountDTO;
import hattabi.youness.ebanking_backend.exceptions.BalanceNotSufficientException;
import hattabi.youness.ebanking_backend.exceptions.BankAccountNotFoundException;
import hattabi.youness.ebanking_backend.exceptions.CustomerNotFoundException;
import hattabi.youness.ebanking_backend.services.BankAccountService;

@SpringBootApplication
public class EbankingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(BankAccountService bankAccountService) {
		return args -> {
			Stream.of("User1", "User2", "User3", "User4", "User5")
					.forEach(name -> {
						CustomerDTO customer = new CustomerDTO();
						customer.setName(name);
						customer.setEmail(name.toLowerCase() + "@gmail.com");
						bankAccountService.saveCustomer(customer);
					});

			bankAccountService.listCustomers().forEach(customer -> {
				try {
					bankAccountService.saveCurrentBankAccount(Math.random() * 90000, 9000, customer.getId());
					bankAccountService.saveSavingBankAccount(Math.random() * 120000, 5.5, customer.getId());
				} catch (CustomerNotFoundException e) {
					System.err.println("Customer Not Found: ");
				}
			});

			List<BankAccountDTO> bankAccounts = bankAccountService.listBankAccounts();

			for (BankAccountDTO bankAccount : bankAccounts) {
				String accountId = switch (bankAccount) {
					case CurrentBankAccountDTO ca -> ca.getId();
					case SavingBankAccountDTO sa -> sa.getId();
					default -> throw new IllegalStateException(
							"Unknown account type: " + bankAccount.getClass().getSimpleName());
				};

				for (int i = 0; i < 10; i++) {
					try {
						if (Math.random() > .3) {
							bankAccountService.credit(accountId, 10000 + Math.random() * 120000,
									"Credit payment #" + (i + 1));
						}
						bankAccountService.debit(accountId, 1000 + Math.random() * 9000,
								"Debit Withdrawal #" + (i + 1));
					} catch (BankAccountNotFoundException | BalanceNotSufficientException e) {
						System.err.println("Operation failed: " + e.getMessage());
					}
				}
			}

			System.out.println("UI: http://localhost:8085/swagger-ui.html");
		};
	}
}
