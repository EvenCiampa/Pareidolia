package com.pareidolia.service.admin;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminConsumerService {

	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;

	/**
	 * Recupera i dati di un consumatore specifico tramite ID.
	 * @param id L'ID del consumatore da recuperare.
	 * @return ConsumerDTO Il DTO contenente i dati del consumatore.
	 */
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

	/**
	 * Ottiene una pagina di consumatori registrati nel sistema.
	 * @return Page<ConsumerDTO> Una pagina di DTO dei consumatori.
	 */
	public Page<ConsumerDTO> getConsumers(Integer page, Integer size) {
		return accountRepository.findAllByReferenceType(
			Account.Type.CONSUMER,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		).map(AccountMapper::entityToConsumerDTO);
	}

	/**
	 * Aggiorna le informazioni di un consumatore esistente.
	 * @param consumerDTO Il DTO del consumatore con le informazioni aggiornate.
	 * @return ConsumerDTO Il DTO aggiornato del consumatore.
	 */
	public ConsumerDTO update(ConsumerDTO consumerDTO) {
		if (consumerDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getConsumerAndValidateUpdate(consumerDTO);

		AccountMapper.updateEntityWithConsumerDTO(account, consumerDTO);

		return AccountMapper.entityToConsumerDTO(accountRepository.save(account));
	}

	/**
	 * Elimina l'account di un consumatore specifico dal sistema.
	 * @param id L'ID del consumatore da eliminare.
	 */
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
