package com.pareidolia.service.generic;

import com.pareidolia.configuration.mail.CustomMailSender;
import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.LoginDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AccessServiceTest {

	@Autowired
	private AccessService accessService;

	@Autowired
	private AccountRepository accountRepository;

	@MockitoSpyBean
	private JWTService jwtService;

	@MockitoBean
	private CustomMailSender customMailSender;

	private final String testEmail = "test@example.com";
	private final String testPassword = "password123";
	private final String testName = "Even";
	private final String testSurname = "Ciampa";
	private final String testPhone = "+39123456789";
	private final String testToken = "test.jwt.token";

	@BeforeEach
	void setUp() {
		reset(jwtService);
	}

	@Test
	public void testRegister() {
		// Arrange
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setEmail(testEmail);
		registrationDTO.setPassword(testPassword);
		registrationDTO.setName(testName);
		registrationDTO.setSurname(testSurname);
		registrationDTO.setPhone(testPhone);

		doReturn(testToken).when(jwtService).create(any(), any(), any());

		// Act
		String result = accessService.register(registrationDTO);

		// Assert
		assertEquals(testToken, result);
		String shaPassword = DigestUtils.sha3_256Hex(testPassword);
		verify(jwtService).create(any(), eq(testEmail), eq(shaPassword));
		assertEquals(1, accountRepository.count());
		Account account = accountRepository.findAll().get(0);
		assertEquals(testEmail, account.getEmail());
		assertEquals(shaPassword, account.getPassword());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
	}

	@Test
	public void testLogin_Success() {
		// Arrange
		String shaPassword = DigestUtils.sha3_256Hex(testPassword);

		accountRepository.save(Account.builder()
			.email(testEmail)
			.password(shaPassword)
			.name(testName)
			.surname(testSurname)
			.phone(testPhone)
			.referenceType(Account.Type.CONSUMER)
			.build());

		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(testEmail);
		loginDTO.setPassword(testPassword);

		doReturn(testToken).when(jwtService).create(any(), any(), any());

		// Act
		String result = accessService.login(loginDTO);

		// Assert
		assertEquals(testToken, result);
		verify(jwtService).create(any(), eq(testEmail), eq(shaPassword));
		assertEquals(1, accountRepository.count());
		Account account = accountRepository.findAll().get(0);
		assertEquals(testEmail, account.getEmail());
		assertEquals(shaPassword, account.getPassword());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
	}

	@Test
	public void testLogin_Failure() {
		// Arrange
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(testEmail);
		loginDTO.setPassword(testPassword);

		// Act & Assert
		assertThrows(BadCredentialsException.class, () -> accessService.login(loginDTO));
		verify(jwtService, never()).create(any(), any(), any());
	}

	@Test
	public void testForgotPassword() {
		// Arrange
		String shaPassword = DigestUtils.sha3_256Hex(testPassword);

		accountRepository.save(Account.builder()
			.email(testEmail)
			.password(testPassword)
			.name(testName)
			.surname(testSurname)
			.phone(testPhone)
			.referenceType(Account.Type.CONSUMER)
			.build());

		doReturn(true).when(customMailSender).sendResetConsumer(eq(testEmail), any());

		// Act
		accessService.forgotPassword(testEmail);

		// Assert
		ArgumentCaptor<String> newPassword = ArgumentCaptor.forClass(String.class);
		verify(customMailSender).sendResetConsumer(eq(testEmail), newPassword.capture());
		String newShaPassword = DigestUtils.sha3_256Hex(newPassword.getValue());
		assertNotEquals(shaPassword, newShaPassword);
		assertEquals(1, accountRepository.count());
		Account account = accountRepository.findAll().get(0);
		assertEquals(testEmail, account.getEmail());
		assertEquals(newShaPassword, account.getPassword());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
	}
} 