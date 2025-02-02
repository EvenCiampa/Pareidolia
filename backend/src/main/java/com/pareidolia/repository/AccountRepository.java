package com.pareidolia.repository;

import com.pareidolia.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByEmailAndPassword(String email, String password);

	Optional<Account> findByEmail(String email);

	Page<Account> findAllByReferenceType(Account.Type referenceType, Pageable pageable);
}
