package com.pareidolia.validator;


import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AccountValidator {

	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	public void createAccountValidator(RegistrationDTO dto) {
		emailValidation(dto.getEmail());
		passwordValidation(dto.getPassword());
		phoneValidation(dto.getPhone());

		emailNotExistsValidation(dto.getEmail());
	}

	public Account getAdminAndValidateUpdate(AdminDTO dto) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		accountTypeValidation(account.getReferenceType(), Account.Type.ADMIN);
		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return account;
	}

	public Account getConsumerAndValidateUpdate(ConsumerDTO dto) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		accountTypeValidation(account.getReferenceType(), Account.Type.CONSUMER);
		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return account;
	}

	public Pair<Account, PromoterInfo> getPromoterAndValidateUpdate(PromoterDTO dto) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		accountTypeValidation(account.getReferenceType(), Account.Type.PROMOTER);

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter not found"));

		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return Pair.of(account, promoterInfo);
	}

	void accountTypeValidation(Account.Type referenceType, Account.Type expectedReferenceType) {
		if (referenceType != expectedReferenceType) {
			throw new IllegalArgumentException("Invalid Account Type");
		}
	}

	private void phoneValidation(String phone) {
		String regex = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$";
		if (phone == null || !Pattern.matches(regex, phone)) {
			throw new IllegalArgumentException("Invalid phone number");
		}
	}

	private void emailValidation(String email) {
		String regex = "^(.+)@(\\\\S+)$";
		if (email == null || Pattern.matches(regex, email)) {
			throw new IllegalArgumentException("Invalid email");
		}
	}

	private void passwordValidation(String password) {
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
		if (password == null || Pattern.matches(regex, password)) {
			throw new IllegalArgumentException("Invalid password");
		}
	}

	private void emailNotExistsValidation(String email) {
		if (accountRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("Invalid email");
		}
	}

}
