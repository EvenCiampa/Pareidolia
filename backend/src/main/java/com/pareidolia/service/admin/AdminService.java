package com.pareidolia.service.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountDTO;
import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.validator.AccountValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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
	private final PromoterInfoRepository promoterInfoRepository;

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

	public AccountDTO getData(Long id) {
		Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
		return AccountMapper.entityToAccountDTO(account);
	}

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

	public AccountDTO updateAccount(AccountDTO accountDTO) {
		if (accountDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountValidator.getAccountAndValidateUpdate(accountDTO, true);
		Account.Type referenceType = Account.Type.valueOf(accountDTO.getReferenceType());

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

		return AccountMapper.entityToAdminDTO(accountRepository.save(account));
	}

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
