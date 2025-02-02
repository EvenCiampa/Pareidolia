package com.pareidolia.service.consumer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
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
public class ConsumerService {

	private final JWTService jwtService;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;

	private Account getAccountAndValidate() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			User user = ((User) authentication.getPrincipal());

			if (!user.getAuthorities().contains(new SimpleGrantedAuthority(Account.Type.CONSUMER.name()))) {
				throw new JWTService.TokenVerificationException();
			}
			return accountRepository.findByEmail(user.getUsername())
				.orElseThrow(JWTService.TokenVerificationException::new);
		}
		throw new JWTService.TokenVerificationException();
	}

	public ConsumerDTO getData() {
		return AccountMapper.entityToConsumerDTO(getAccountAndValidate());
	}

	public AccountLoginDTO update(ConsumerDTO consumerDTO) {
		if (consumerDTO.getId() == null || !consumerDTO.getId().equals(getAccountAndValidate().getId())) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getConsumerAndValidateUpdate(consumerDTO);
		boolean updateAuthToken = !Objects.equals(account.getEmail(), consumerDTO.getEmail());

		AccountMapper.updateEntityWithConsumerDTO(account, consumerDTO);

		account = accountRepository.save(account);

		String authToken = null;
		if (updateAuthToken) {
			authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());
		}

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

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
