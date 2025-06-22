package com.pareidolia.service.reviewer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReviewerService {

	private final JWTService jwtService;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	/**
	 * Ottiene e valida l'account del reviewer autenticato, assicurando che abbia l'autorità appropriata.
	 */
	private Account getAccountAndValidate() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			User user = ((User) authentication.getPrincipal());

			if (!user.getAuthorities().contains(new SimpleGrantedAuthority(Account.Type.REVIEWER.name()))) {
				throw new JWTService.TokenVerificationException();
			}
			return accountRepository.findByEmail(user.getUsername())
				.orElseThrow(JWTService.TokenVerificationException::new);
		}
		throw new JWTService.TokenVerificationException();
	}

	/**
	 * Recupera i dati del reviewer autenticato.
	 * @return ReviewerDTO DTO contenente i dati del reviewer.
	 */
	public ReviewerDTO getData() {
		return AccountMapper.entityToReviewerDTO(getAccountAndValidate());
	}

	/**
	 *
	 * @param reviewerDTO DTO del reviewer con i dati aggiornati.
	 * @return AccountLoginDTO DTO del conto aggiornato, includendo un nuovo token di autenticazione se l'email è stata modificata.
	 */
	public AccountLoginDTO update(ReviewerDTO reviewerDTO) {
		if (reviewerDTO.getId() == null || !reviewerDTO.getId().equals(getAccountAndValidate().getId())) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getReviewerAndValidateUpdate(reviewerDTO);
		boolean updateAuthToken = !Objects.equals(account.getEmail(), reviewerDTO.getEmail());

		AccountMapper.updateEntityWithReviewerDTO(account, reviewerDTO);

		account = accountRepository.save(account);

		String authToken = null;
		if (updateAuthToken) {
			authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());
		}

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Aggiorna la password per l'account del reviewer autenticato. Convalida la password attuale e applica la nuova password se valida.
	 * @param passwordUpdateDTO Il DTO contenente le password attuale e nuova.
	 * @return AccountLoginDTO Il DTO che include le credenziali dell'account con un nuovo token di autenticazione che riflette il cambiamento della password.
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
}
