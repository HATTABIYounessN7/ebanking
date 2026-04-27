package hattabi.youness.ebanking_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hattabi.youness.ebanking_backend.entities.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    List<BankAccount> findByCustomerId(Long customerId);
}
