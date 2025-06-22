package com.pareidolia.service.reviewer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.validator.AccountValidator;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewerServiceTest {

	private static final String TEST_EMAIL = "reviewer@test.com";
	private static final String TEST_PASSWORD = "password123";
	private static final String TEST_TOKEN = "test.jwt.token";
	@Mock
	private JWTService jwtService;
	@Mock
	private AccountValidator accountValidator;
	@Mock
	private AccountRepository accountRepository;
	@Mock
	private PromoterInfoRepository promoterInfoRepository;
	@Mock
	private Authentication authentication;
	@Mock
	private SecurityContext securityContext;
	@InjectMocks
	private ReviewerService reviewerService;
	private Account testAccount;
	private ReviewerDTO testReviewerDTO;
	private User userDetails;

	@BeforeEach
	void setUp() {
		testAccount = new Account();
		testAccount.setId(1L);
		testAccount.setEmail(TEST_EMAIL);
		testAccount.setPassword(DigestUtils.sha3_256Hex(TEST_PASSWORD));
		testAccount.setReferenceType(Account.Type.REVIEWER);

		testReviewerDTO = new ReviewerDTO();
		testReviewerDTO.setId(1L);
		testReviewerDTO.setEmail(TEST_EMAIL);

		userDetails = new User(TEST_EMAIL, TEST_PASSWORD,
			Collections.singletonList(new SimpleGrantedAuthority(Account.Type.REVIEWER.name())));

		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void testGetDataSuccess() {
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));

		ReviewerDTO result = reviewerService.getData();

		assertNotNull(result);
		assertEquals(testAccount.getId(), result.getId());
		assertEquals(testAccount.getEmail(), result.getEmail());
		verify(accountRepository).findByEmail(TEST_EMAIL);
	}

	@Test
	void testGetDataWithInvalidAuthority() {
		User invalidUser = new User(TEST_EMAIL, TEST_PASSWORD,
			Collections.singletonList(new SimpleGrantedAuthority(Account.Type.CONSUMER.name())));
		when(authentication.getPrincipal()).thenReturn(invalidUser);

		assertThrows(JWTService.TokenVerificationException.class,
			() -> reviewerService.getData());
	}

	@Test
	void testGetDataWithNoAuthentication() {
		when(securityContext.getAuthentication()).thenReturn(null);

		assertThrows(JWTService.TokenVerificationException.class,
			() -> reviewerService.getData());
	}

	@Test
	void testUpdateSuccess() {
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		when(accountValidator.getReviewerAndValidateUpdate(any())).thenReturn(testAccount);
		when(accountRepository.save(any())).thenReturn(testAccount);

		AccountLoginDTO result = reviewerService.update(testReviewerDTO);

		assertNotNull(result);
		assertEquals(testAccount.getEmail(), result.getEmail());
		verify(accountRepository).save(any(Account.class));
	}

	@Test
	void testUpdateWithEmailChange() {
		String newEmail = "new.email@test.com";
		testReviewerDTO.setEmail(newEmail);

		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		when(accountValidator.getReviewerAndValidateUpdate(any())).thenReturn(testAccount);
		when(accountRepository.save(any())).thenReturn(testAccount);
		when(jwtService.create(any(), any(), any())).thenReturn(TEST_TOKEN);

		AccountLoginDTO result = reviewerService.update(testReviewerDTO);

		assertNotNull(result);
		assertNotNull(result.getAuthToken());
		assertEquals(TEST_TOKEN, result.getAuthToken());
		verify(jwtService).create(any(), any(), any());
	}

	@Test
	void testUpdateWithInvalidId() {
		testReviewerDTO.setId(2L);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));

		assertThrows(IllegalArgumentException.class,
			() -> reviewerService.update(testReviewerDTO));
	}

	@Test
	void testUpdatePasswordSuccess() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		passwordUpdateDTO.setCurrentPassword(TEST_PASSWORD);
		passwordUpdateDTO.setNewPassword("newPassword123");

		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		when(accountRepository.save(any())).thenReturn(testAccount);
		when(jwtService.create(any(), any(), any())).thenReturn(TEST_TOKEN);

		AccountLoginDTO result = reviewerService.updatePassword(passwordUpdateDTO);

		assertNotNull(result);
		assertNotNull(result.getAuthToken());
		assertEquals(TEST_TOKEN, result.getAuthToken());
		verify(accountRepository).save(any(Account.class));
		verify(jwtService).create(any(), any(), any());
	}

	@Test
	void testUpdatePasswordWithIncorrectCurrentPassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		passwordUpdateDTO.setCurrentPassword("wrongPassword");
		passwordUpdateDTO.setNewPassword("newPassword123");

		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));

		assertThrows(IllegalArgumentException.class,
			() -> reviewerService.updatePassword(passwordUpdateDTO));
	}

	@Test
	void testUpdatePasswordWithNullCurrentPassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		passwordUpdateDTO.setNewPassword("newPassword123");

		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));

		assertThrows(IllegalArgumentException.class,
			() -> reviewerService.updatePassword(passwordUpdateDTO));
	}

	@Test
	void testUpdatePasswordWithNullNewPassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		passwordUpdateDTO.setCurrentPassword(TEST_PASSWORD);

		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));

		assertThrows(IllegalArgumentException.class,
			() -> reviewerService.updatePassword(passwordUpdateDTO));
	}

	@Test
	void testUpdatePasswordWithInvalidNewPassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		passwordUpdateDTO.setCurrentPassword(TEST_PASSWORD);
		passwordUpdateDTO.setNewPassword("weak");

		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testAccount));
		doThrow(new IllegalArgumentException("Invalid password"))
			.when(accountValidator).passwordValidation(anyString());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerService.updatePassword(passwordUpdateDTO));
	}
} 