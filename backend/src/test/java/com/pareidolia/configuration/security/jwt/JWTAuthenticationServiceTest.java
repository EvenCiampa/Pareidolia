package com.pareidolia.configuration.security.jwt;

import com.pareidolia.configuration.mail.CustomMailSender;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationServiceTest {

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "password123";
	private static final String TEST_TOKEN = "test.jwt.token";
	@Mock
	private JWTService jwtService;
	@Mock
	private CustomMailSender customMailSender;
	@Mock
	private AccountRepository accountRepository;
	@InjectMocks
	private JWTAuthenticationService authenticationService;
	@Captor
	private ArgumentCaptor<Account> accountCaptor;
	private Account testAccount;

	@BeforeEach
	void setUp() {
		testAccount = new Account();
		testAccount.setEmail(TEST_EMAIL);
		testAccount.setPassword(DigestUtils.sha3_256Hex(TEST_PASSWORD));
		testAccount.setReferenceType(Account.Type.CONSUMER);
	}

	@Test
	void testAuthenticateByTokenSuccess() {
		Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("referenceType", Account.Type.CONSUMER.name());
		tokenData.put("username", TEST_EMAIL);
		tokenData.put("password", testAccount.getPassword());

		when(jwtService.verify(TEST_TOKEN)).thenReturn(tokenData);
		when(accountRepository.findByEmailAndPassword(TEST_EMAIL, testAccount.getPassword()))
			.thenReturn(Optional.of(testAccount));

		Account result = authenticationService.authenticateByToken(TEST_TOKEN);

		assertNotNull(result);
		assertEquals(testAccount.getEmail(), result.getEmail());
		assertEquals(testAccount.getPassword(), result.getPassword());
		assertEquals(testAccount.getReferenceType(), result.getReferenceType());
	}

	@Test
	void testAuthenticateByTokenInvalidCredentials() {
		Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("referenceType", Account.Type.CONSUMER.name());
		tokenData.put("username", TEST_EMAIL);
		tokenData.put("password", "wrongpassword");

		when(jwtService.verify(TEST_TOKEN)).thenReturn(tokenData);
		when(accountRepository.findByEmailAndPassword(anyString(), anyString()))
			.thenReturn(Optional.empty());

		assertThrows(BadCredentialsException.class, () ->
			authenticationService.authenticateByToken(TEST_TOKEN)
		);
	}

	@Test
	void testAuthenticateByTokenInvalidReferenceType() {
		Map<String, Object> tokenData = new HashMap<>();
		tokenData.put("referenceType", Account.Type.PROMOTER.name());  // Different from account type
		tokenData.put("username", TEST_EMAIL);
		tokenData.put("password", testAccount.getPassword());

		when(jwtService.verify(TEST_TOKEN)).thenReturn(tokenData);
		when(accountRepository.findByEmailAndPassword(TEST_EMAIL, testAccount.getPassword()))
			.thenReturn(Optional.of(testAccount));

		assertThrows(BadCredentialsException.class, () ->
			authenticationService.authenticateByToken(TEST_TOKEN)
		);
	}

	@Test
	void testAuthenticateByTokenVerificationFailure() {
		when(jwtService.verify(TEST_TOKEN)).thenThrow(new JWTService.TokenVerificationException("Invalid token"));

		assertThrows(BadCredentialsException.class, () ->
			authenticationService.authenticateByToken(TEST_TOKEN)
		);
	}

	@Test
	void testResetPasswordForConsumer() {
		String oldPassword = testAccount.getPassword();
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		when(customMailSender.sendResetConsumer(eq(TEST_EMAIL), anyString())).thenReturn(true);
		when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

		assertDoesNotThrow(() -> authenticationService.reset(TEST_EMAIL));

		verify(accountRepository).save(accountCaptor.capture());
		Account savedAccount = accountCaptor.getValue();
		assertNotEquals(oldPassword, savedAccount.getPassword());
		verify(customMailSender).sendResetConsumer(eq(TEST_EMAIL), anyString());
	}

	@Test
	void testResetPasswordForPromoter() {
		testAccount.setReferenceType(Account.Type.PROMOTER);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		when(customMailSender.sendResetConsumer(eq(TEST_EMAIL), anyString())).thenReturn(true);
		when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

		assertDoesNotThrow(() -> authenticationService.reset(TEST_EMAIL));

		verify(accountRepository).save(accountCaptor.capture());
		verify(customMailSender).sendResetConsumer(eq(TEST_EMAIL), anyString());
	}

	@Test
	void testResetPasswordForAdmin() {
		testAccount.setReferenceType(Account.Type.ADMIN);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));

		assertThrows(RuntimeException.class, () ->
			authenticationService.reset(TEST_EMAIL)
		);

		verify(accountRepository, never()).save(any(Account.class));
		verify(customMailSender, never()).sendResetConsumer(anyString(), anyString());
	}

	@Test
	void testResetPasswordEmailNotFound() {
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

		assertThrows(BadCredentialsException.class, () ->
			authenticationService.reset(TEST_EMAIL)
		);

		verify(accountRepository, never()).save(any(Account.class));
		verify(customMailSender, never()).sendResetConsumer(anyString(), anyString());
	}

	@Test
	void testResetPasswordEmailSendingFailure() {
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		when(customMailSender.sendResetConsumer(eq(TEST_EMAIL), anyString())).thenReturn(false);

		assertThrows(RuntimeException.class, () ->
			authenticationService.reset(TEST_EMAIL)
		);

		verify(accountRepository).save(any(Account.class));
		verify(customMailSender).sendResetConsumer(eq(TEST_EMAIL), anyString());
	}
} 