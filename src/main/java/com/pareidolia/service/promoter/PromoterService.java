package com.pareidolia.service.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.PromoterDTO;
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

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PromoterService {

	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	private Account getAccountAndValidate() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			User user = ((User) authentication.getPrincipal());

			if (!user.getAuthorities().contains(new SimpleGrantedAuthority(Account.Type.PROMOTER.name()))) {
				throw new JWTService.TokenVerificationException();
			}
			return accountRepository.findByEmail(user.getUsername())
				.orElseThrow(JWTService.TokenVerificationException::new);
		}
		throw new JWTService.TokenVerificationException();
	}

	public PromoterDTO getData() {
		Account account = getAccountAndValidate();
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter information not found"));
		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	public PromoterDTO update(PromoterDTO promoterDTO) {
		if (promoterDTO.getId() == null || !promoterDTO.getId().equals(getAccountAndValidate().getId())) {
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
}
