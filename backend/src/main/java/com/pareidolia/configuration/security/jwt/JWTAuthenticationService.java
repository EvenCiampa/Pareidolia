package com.pareidolia.configuration.security.jwt;

import com.pareidolia.configuration.mail.CustomMailSender;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JWTAuthenticationService {
	private final JWTService jwtService;
	private final CustomMailSender customMailSender;
	private final AccountRepository accountRepository;

	public void reset(String email) throws BadCredentialsException {
		accountRepository
			.findByEmail(email)
			.ifPresentOrElse(
				account -> {
					if (!List.of(Account.Type.CONSUMER, Account.Type.PROMOTER).contains(account.getReferenceType())) {
						throw new RuntimeException("Something went wrong with reset");
					}
					String newPassword = RandomStringUtils.secureStrong().nextAlphanumeric(8);
					account.setPassword(DigestUtils.sha3_256Hex(newPassword));
					accountRepository.save(account);
					if (!customMailSender.sendResetConsumer(email, newPassword)) {
						throw new RuntimeException("Something went wrong with email");
					}
				},
				() -> {
					throw new BadCredentialsException("Invalid email");
				});
	}

	public Account authenticateByToken(String token) {
		try {
			Map<String, Object> data = jwtService.verify(token);
			Account.Type referenceType = Account.Type.valueOf(String.valueOf(data.get("referenceType")));
			String username = String.valueOf(data.get("username"));
			String password = String.valueOf(data.get("password"));
			Account account = accountRepository.findByEmailAndPassword(username, password)
				.orElseThrow(() -> new UsernameNotFoundException("Authentication fail"));
			if (referenceType != account.getReferenceType()) {
				throw new BadCredentialsException("Invalid token");
			}
			return account;
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid token");
		}
	}
}