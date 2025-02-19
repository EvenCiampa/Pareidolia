package com.pareidolia.service.generic;

import com.pareidolia.configuration.security.jwt.JWTAuthenticationService;
import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.LoginDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AccessService {

	private final JWTService jwtService;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final JWTAuthenticationService jwtAuthenticationService;

	/**
	 * Registra un nuovo account utilizzando i dati forniti e restituisce un DTO contenente l'account registrato con un token di autenticazione.
	 * @param registrationDTO DTO contenente i dati di registrazione.
	 * @return AccountLoginDTO DTO dell'account registrato con token di autenticazione.
	 * @throws IllegalArgumentException Se i dati di registrazione non sono validi.
	 */
	public AccountLoginDTO register(RegistrationDTO registrationDTO) {
		accountValidator.createAccountValidator(registrationDTO);

		Account account = AccountMapper.registrationDTOToEntity(registrationDTO, Account.Type.CONSUMER);
		account = accountRepository.save(account);

		String authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Autentica un utente basandosi sull'email e sulla password fornite, restituisce un DTO dell'account con un token di autenticazione.
	 * @param loginDTO DTO contenente email e password dell'utente.
	 * @return AccountLoginDTO DTO dell'account autenticato con token di autenticazione.
	 * @throws BadCredentialsException Se le credenziali fornite non sono valide.
	 */
	public AccountLoginDTO login(LoginDTO loginDTO) {
		Account account = accountRepository.findByEmailAndPassword(loginDTO.getEmail(), DigestUtils.sha3_256Hex(loginDTO.getPassword()))
			.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

		String authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Gestisce la richiesta di reset della password per un'email fornita.
	 * @param email L'email per cui resettare la password.
	 * @throws BadCredentialsException Se non esiste un account con l'email fornita.
	 */
	public void forgotPassword(String email) {
		jwtAuthenticationService.reset(email);
	}
}
