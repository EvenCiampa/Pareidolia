package com.pareidolia.service.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminService {

	private final JWTService jwtService;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	/**
	 * Ottiene e valida l'account dell'amministratore autenticato, assicurando che abbia l'autorità appropriata.
	 */
	private Account getAccountAndValidate() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			User user = ((User) authentication.getPrincipal());

			if (!user.getAuthorities().contains(new SimpleGrantedAuthority(Account.Type.ADMIN.name()))) {
				throw new JWTService.TokenVerificationException();
			}
			return accountRepository.findByEmail(user.getUsername())
				.orElseThrow(JWTService.TokenVerificationException::new);
		}
		throw new JWTService.TokenVerificationException();
	}

	/**
	 * Recupera i dati dell'amministratore autenticato.
	 * @return AdminDTO DTO contenente i dati dell'amministratore.
	 */
	public AdminDTO getData() {
		return AccountMapper.entityToAdminDTO(getAccountAndValidate());
	}

	/**
	 * Recupera i dati di un account specifico utilizzando il suo ID.
	 * @param id ID dell'account da recuperare.
	 * @return AccountDTO DTO dell'account.
	 */
	public AccountDTO getData(Long id) {
		Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
		return AccountMapper.entityToAccountDTO(account);
	}

	/**
	 * Recupera una pagina di amministratori.
	 * @return Page<AdminDTO> Pagina contenente i DTO degli amministratori.
	 */
	public Page<AdminDTO> getAdmins(Integer page, Integer size) {
		return accountRepository.findAllByReferenceType(
			Account.Type.ADMIN,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		).map(AccountMapper::entityToAdminDTO);
	}

	/**
	 * Crea un nuovo account nel sistema.
	 * @param registrationDTO DTO contenente i dati di registrazione.
	 * @param referenceType Tipo di account da creare.
	 * @return AccountDTO DTO dell'account creato.
	 */
	public AccountDTO createAccount(RegistrationDTO registrationDTO, Account.Type referenceType) {
		accountValidator.createAccountValidator(registrationDTO);

		Account account = AccountMapper.registrationDTOToEntity(registrationDTO, referenceType);
		accountRepository.save(account);

		if (account.getReferenceType() == Account.Type.PROMOTER) {
			PromoterInfo promoterInfo = new PromoterInfo();
			promoterInfo.setIdPromoter(account.getId());
			promoterInfoRepository.save(promoterInfo);
		}

		return AccountMapper.entityToAccountDTO(account);
	}

	/**
	 * Aggiorna un account esistente nel sistema.
	 * @param accountDTO DTO contenente le informazioni aggiornate dell'account.
	 * @return AccountLoginDTO DTO dell'account aggiornato, includendo un token di autenticazione se necessario.
	 */
	public AccountLoginDTO updateAccount(AccountDTO accountDTO) {
		if (accountDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getAccountAndValidateUpdate(accountDTO, true);
		Account.Type referenceType = Account.Type.valueOf(accountDTO.getReferenceType());
		boolean updateAuthToken = !Objects.equals(account.getEmail(), accountDTO.getEmail());

		if (Objects.equals(account.getId(), getAccountAndValidate().getId()) && referenceType != Account.Type.ADMIN) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		if (referenceType == Account.Type.ADMIN || referenceType == Account.Type.CONSUMER) {
			if (account.getReferenceType() == Account.Type.PROMOTER) {
				PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
					.orElseThrow(() -> new IllegalArgumentException("Promoter not found"));
				promoterInfoRepository.delete(promoterInfo);
			}
		} else if (referenceType == Account.Type.PROMOTER) {
			if (account.getReferenceType() == Account.Type.ADMIN || account.getReferenceType() == Account.Type.CONSUMER) {
				PromoterInfo promoterInfo = new PromoterInfo();
				promoterInfo.setIdPromoter(account.getId());
				promoterInfoRepository.save(promoterInfo);
			}
		} else {
			throw new IllegalArgumentException("Invalid account type");
		}

		account.setReferenceType(referenceType);
		AccountMapper.updateEntityWithAccountDTO(account, accountDTO);

		account = accountRepository.save(account);

		String authToken = null;
		if (updateAuthToken) {
			authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());
		}

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Aggiorna un promotore nel sistema.
	 * @param promoterDTO DTO del promotore con informazioni aggiornate.
	 * @return PromoterDTO DTO del promotore aggiornato.
	 */
	public PromoterDTO updatePromoter(PromoterDTO promoterDTO) {
		if (promoterDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Pair<Account, PromoterInfo> accountWithInfo = accountValidator.getPromoterAndValidateUpdate(promoterDTO);
		Account account = accountWithInfo.getFirst();
		PromoterInfo promoterInfo = accountWithInfo.getSecond();

		AccountMapper.updateEntitiesWithPromoterDTO(account, promoterInfo, promoterDTO);

		account = accountRepository.save(account);
		promoterInfo = promoterInfoRepository.save(promoterInfo);

		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	/**
	 * Aggiorna la password di un account.
	 * @param passwordUpdateDTO DTO contenente la password corrente e la nuova password.
	 * @return AccountLoginDTO DTO dell'account con la nuova password e un token di autenticazione.
	 */
	public AccountLoginDTO updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
		Account account = getAccountAndValidate();

		if (passwordUpdateDTO.getCurrentPassword() == null) {
			throw new IllegalArgumentException("Invalid Current Password");
		}

		String currentPassword = DigestUtils.sha3_256Hex(passwordUpdateDTO.getCurrentPassword());

		if (!Objects.equals(currentPassword, account.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}

		if (passwordUpdateDTO.getNewPassword() == null) {
			throw new IllegalArgumentException("Invalid New Password");
		}

		accountValidator.passwordValidation(passwordUpdateDTO.getNewPassword());

		account.setPassword(DigestUtils.sha3_256Hex(passwordUpdateDTO.getNewPassword()));
		account = accountRepository.save(account);

		String authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Elimina un account specifico dal sistema.
	 * @param id ID dell'account da eliminare.
	 */
	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (Objects.equals(account.getId(), getAccountAndValidate().getId())) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		accountRepository.deleteById(id);
	}
}
