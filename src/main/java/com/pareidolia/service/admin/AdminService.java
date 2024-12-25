package com.pareidolia.service.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.RegistrationDTO;
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

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminService {

	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;

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

	public AdminDTO getData() {
		return AccountMapper.entityToAdminDTO(getAccountAndValidate());
	}

	public AdminDTO getData(Long id) {
		Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
		return AccountMapper.entityToAdminDTO(account);
	}

	public AdminDTO create(RegistrationDTO registrationDTO) {
		accountValidator.createAccountValidator(registrationDTO);

		Account account = AccountMapper.registrationDTOToEntity(registrationDTO, Account.Type.ADMIN);
		accountRepository.save(account);

		return AccountMapper.entityToAdminDTO(account);
	}

	public AdminDTO update(AdminDTO adminDTO) {
		if (adminDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getAdminAndValidateUpdate(adminDTO);

		AccountMapper.updateEntityWithAdminDTO(account, adminDTO);

		return AccountMapper.entityToAdminDTO(accountRepository.save(account));
	}

	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (account.getReferenceType() != Account.Type.ADMIN || Objects.equals(account.getId(), getAccountAndValidate().getId())) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		accountRepository.deleteById(id);
	}
}
