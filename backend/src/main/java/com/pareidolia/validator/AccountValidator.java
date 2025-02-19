package com.pareidolia.validator;

import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AccountValidator {

	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	/**
	 * Valida i dati di registrazione di un nuovo account.
	 * @param dto DTO di registrazione che contiene i dati dell'account da validare.
	 */
	public void createAccountValidator(RegistrationDTO dto) {
		emailValidation(dto.getEmail());
		passwordValidation(dto.getPassword());
		phoneValidation(dto.getPhone());

		emailNotExistsValidation(dto.getEmail());
	}

	/**
	 * Recupera e valida un account esistente per l'aggiornamento, assicurandosi che corrisponda ai tipi di account richiesti.
	 * @param dto DTO dell'account con i dati aggiornati.
	 * @param skipTypeValidation Se saltare la validazione del tipo di account.
	 * @return Account aggiornato e validato.
	 */
	public Account getAccountAndValidateUpdate(AccountDTO dto, boolean skipTypeValidation) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (!skipTypeValidation) {
			accountTypeValidation(account.getReferenceType(), List.of(Account.Type.ADMIN, Account.Type.CONSUMER));
		}
		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return account;
	}

	/**
	 * Valida un account consumer esistente per un aggiornamento.
	 * @param dto DTO del consumatore con i dati aggiornati.
	 * @return Account consumatore aggiornato e validato.
	 */
	public Account getConsumerAndValidateUpdate(ConsumerDTO dto) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		accountTypeValidation(account.getReferenceType(), Account.Type.CONSUMER);
		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return account;
	}

	public Account getReviewerAndValidateUpdate(ReviewerDTO dto) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		accountTypeValidation(account.getReferenceType(), Account.Type.REVIEWER);
		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return account;
	}

	public Pair<Account, PromoterInfo> getPromoterAndValidateUpdate(PromoterDTO dto) {
		return getPromoterAndValidateUpdate(dto, false);
	}

	/**
	 * Recupera e valida un promotore esistente per un aggiornamento, con opzione di saltare la validazione del tipo di account.
	 * @param dto DTO del promotore con i dati aggiornati.
	 * @param skipTypeValidation Se saltare la validazione del tipo di account.
	 * @return Pair<Account, PromoterInfo> Coppia contenente l'account del promotore e le sue informazioni aggiornate.
	 */
	public Pair<Account, PromoterInfo> getPromoterAndValidateUpdate(PromoterDTO dto, boolean skipTypeValidation) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (!skipTypeValidation) {
			accountTypeValidation(account.getReferenceType(), Account.Type.PROMOTER);
		}

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter not found"));

		phoneValidation(dto.getPhone());
		emailValidation(dto.getEmail());

		return Pair.of(account, promoterInfo);
	}

	/**
	 * Recupera e valida un promotore, assicurandosi che il tipo di account corrisponda a quello atteso come promotore.
	 * @param dto DTO del promotore da validare.
	 * @return Pair<Account, PromoterInfo> Coppia contenente l'account del promotore e le sue informazioni.
	 */
	public Pair<Account, PromoterInfo> getPromoterAndValidate(PromoterDTO dto) {
		Account account = accountRepository.findById(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		accountTypeValidation(account.getReferenceType(), Account.Type.PROMOTER);

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(dto.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter not found"));

		return Pair.of(account, promoterInfo);
	}

	/**
	 * Convalida il tipo di account rispetto a un tipo atteso o una lista di tipi validi.
	 * @param referenceType Tipo di riferimento dell'account da validare.
	 * @param expectedReferenceType Tipo di account atteso.
	 */
	void accountTypeValidation(Account.Type referenceType, Account.Type expectedReferenceType) {
		if (referenceType != expectedReferenceType) {
			throw new IllegalArgumentException("Invalid Account Type");
		}
	}

	void accountTypeValidation(Account.Type referenceType, List<Account.Type> validReferenceTypes) {
		if (!validReferenceTypes.contains(referenceType)) {
			throw new IllegalArgumentException("Invalid Account Type");
		}
	}

	/**
	 * Valida il numero di telefono fornito, assicurandosi che corrisponda a un formato valido.
	 * @param phone Numero di telefono da validare.
	 */
	private void phoneValidation(String phone) {
		String regex = "^[+]?[0-9 \\-().]{5,32}$";
		if (phone == null || !Pattern.matches(regex, phone)) {
			throw new IllegalArgumentException("Invalid phone number");
		}
	}

	/**
	 * Valida l'indirizzo email fornito, assicurandosi che sia valido secondo i criteri di EmailValidator.
	 * @param email Indirizzo email da validare.
	 */
	private void emailValidation(String email) {
		if (email == null || !EmailValidator.getInstance().isValid(email)) {
			throw new IllegalArgumentException("Invalid email");
		}
	}

	/**
	 * Valida la password fornita, assicurandosi che soddisfi i criteri di complessità specificati.
	 * @param password Password da validare.
	 */
	public void passwordValidation(String password) {
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$";
		if (password == null || !Pattern.matches(regex, password)) {
			throw new IllegalArgumentException("Invalid password");
		}
	}

	/**
	 * Verifica che un indirizzo email non sia già presente nel database.
	 * @param email Email da verificare.
	 */
	private void emailNotExistsValidation(String email) {
		if (accountRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("Invalid email");
		}
	}

}
