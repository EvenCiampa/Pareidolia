package com.pareidolia.service.generic;

import com.pareidolia.configuration.security.jwt.JWTAuthenticationService;
import com.pareidolia.configuration.security.jwt.JWTService;
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
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AccessService {

	private final JWTService jwtService;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final JWTAuthenticationService jwtAuthenticationService;

	public String register(RegistrationDTO registrationDTO) {
		accountValidator.createAccountValidator(registrationDTO);

		Account account = AccountMapper.registrationDTOToEntity(registrationDTO, Account.Type.CONSUMER);
		accountRepository.save(account);

		return jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());
	}

	public String login(LoginDTO loginDTO) {
		return jwtAuthenticationService.login(loginDTO.getEmail(), DigestUtils.sha3_256Hex(loginDTO.getPassword()));
	}

	public void forgotPassword(String email) {
		jwtAuthenticationService.reset(email);
	}
}
