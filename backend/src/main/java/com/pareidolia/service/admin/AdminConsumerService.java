package com.pareidolia.service.admin;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminConsumerService {

	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;

	public ConsumerDTO getData(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (account.getReferenceType() != Account.Type.CONSUMER) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		return AccountMapper.entityToConsumerDTO(account);
	}

	public ConsumerDTO update(ConsumerDTO consumerDTO) {
		if (consumerDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getConsumerAndValidateUpdate(consumerDTO);

		AccountMapper.updateEntityWithConsumerDTO(account, consumerDTO);

		return AccountMapper.entityToConsumerDTO(accountRepository.save(account));
	}

	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (account.getReferenceType() != Account.Type.CONSUMER) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		accountRepository.deleteById(id);
	}
}
