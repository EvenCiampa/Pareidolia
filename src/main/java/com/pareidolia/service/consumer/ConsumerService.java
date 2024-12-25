package com.pareidolia.service.consumer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ConsumerService {

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

	public ConsumerDTO update(ConsumerDTO consumerDTO) {
		if (consumerDTO.getId() == null || !consumerDTO.getId().equals(getAccountAndValidate().getId())) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getConsumerAndValidateUpdate(consumerDTO);

		AccountMapper.updateEntityWithConsumerDTO(account, consumerDTO);

		return AccountMapper.entityToConsumerDTO(accountRepository.save(account));

	}
}
