package com.pareidolia.service.generic;

import com.pareidolia.configuration.mail.CustomMailSender;
import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
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

	private final String consumerEmail = "consumer@test.com";
	private final String promoterEmail = "promoter@test.com";
	private final String adminEmail = "admin@test.com";
	private final String password = "TestPassword123#";
	private final String name = "Test";
	private final String surname = "User";
	private final String phone = "+39123456789";
	private final String authToken = "TEST_TOKEN";
	@Autowired
	private AccessService accessService;
	@Autowired
	private AccountRepository accountRepository;
	@MockitoSpyBean
	private JWTService jwtService;
	@MockitoBean
	private CustomMailSender customMailSender;

	@BeforeEach
	void setUp() {
		// Create accounts for different roles
		accountRepository.save(Account.builder()
			.email(consumerEmail)
			.password(DigestUtils.sha3_256Hex(password))
			.name(name)
			.surname(surname)
			.phone(phone)
			.referenceType(Account.Type.CONSUMER)
			.build());

		accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(password))
			.name(name)
			.surname(surname)
			.phone(phone)
			.referenceType(Account.Type.PROMOTER)
			.build());

		accountRepository.save(Account.builder()
			.email(adminEmail)
			.password(DigestUtils.sha3_256Hex(password))
			.name(name)
			.surname(surname)
			.phone(phone)
			.referenceType(Account.Type.ADMIN)
			.build());
	}

	@Test
	void testRegister() {
		// Arrange
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setEmail("new@test.com");
		registrationDTO.setPassword(password);
		registrationDTO.setName(name);
		registrationDTO.setSurname(surname);
		registrationDTO.setPhone(phone);

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO result = accessService.register(registrationDTO);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals(registrationDTO.getName(), result.getName());
		assertEquals(registrationDTO.getSurname(), result.getSurname());
		assertEquals(registrationDTO.getPhone(), result.getPhone());
		assertEquals(registrationDTO.getEmail(), result.getEmail());
		assertEquals(Account.Type.CONSUMER.name(), result.getReferenceType());
		assertEquals(authToken, result.getAuthToken());

		verify(jwtService).create(any(), eq(registrationDTO.getEmail()), eq(DigestUtils.sha3_256Hex(registrationDTO.getPassword())));
		verifyNoMoreInteractions(jwtService);

		assertEquals(4, accountRepository.count()); // 3 from setup + 1 new
	}

	@Test
	void testRegisterWithExistingEmail() {
		// Arrange
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setEmail(consumerEmail); // Already exists
		registrationDTO.setPassword(password);
		registrationDTO.setName(name);
		registrationDTO.setSurname(surname);
		registrationDTO.setPhone(phone);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> accessService.register(registrationDTO));
		assertEquals("Invalid email", exception.getMessage());
	}

	@Test
	void testLoginAsConsumer() {
		// Arrange
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(consumerEmail);
		loginDTO.setPassword(password);

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO result = accessService.login(loginDTO);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals(name, result.getName());
		assertEquals(surname, result.getSurname());
		assertEquals(phone, result.getPhone());
		assertEquals(consumerEmail, result.getEmail());
		assertEquals(Account.Type.CONSUMER.name(), result.getReferenceType());
		assertEquals(authToken, result.getAuthToken());

		verify(jwtService).create(any(), eq(consumerEmail), eq(DigestUtils.sha3_256Hex(password)));
		verifyNoMoreInteractions(jwtService);
	}

	@Test
	void testLoginAsPromoter() {
		// Arrange
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(promoterEmail);
		loginDTO.setPassword(password);

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO result = accessService.login(loginDTO);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals(name, result.getName());
		assertEquals(surname, result.getSurname());
		assertEquals(phone, result.getPhone());
		assertEquals(promoterEmail, result.getEmail());
		assertEquals(Account.Type.PROMOTER.name(), result.getReferenceType());
		assertEquals(authToken, result.getAuthToken());

		verify(jwtService).create(eq(Account.Type.PROMOTER), eq(promoterEmail), eq(DigestUtils.sha3_256Hex(password)));
		verifyNoMoreInteractions(jwtService);
	}

	@Test
	void testLoginAsAdmin() {
		// Arrange
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(adminEmail);
		loginDTO.setPassword(password);

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO result = accessService.login(loginDTO);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals(name, result.getName());
		assertEquals(surname, result.getSurname());
		assertEquals(phone, result.getPhone());
		assertEquals(adminEmail, result.getEmail());
		assertEquals(Account.Type.ADMIN.name(), result.getReferenceType());
		assertEquals(authToken, result.getAuthToken());

		verify(jwtService).create(eq(Account.Type.ADMIN), eq(adminEmail), eq(DigestUtils.sha3_256Hex(password)));
		verifyNoMoreInteractions(jwtService);
	}

	@Test
	void testLoginWithInvalidCredentials() {
		// Arrange
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(consumerEmail);
		loginDTO.setPassword("WrongPassword");

		// Act & Assert
		BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> accessService.login(loginDTO));
		assertEquals("Invalid username or password", exception.getMessage());
	}

	@Test
	void testForgotPassword() {
		// Arrange
		doReturn(true).when(customMailSender).sendResetConsumer(eq(consumerEmail), any());

		// Act
		accessService.forgotPassword(consumerEmail);

		// Assert
		ArgumentCaptor<String> newPassword = ArgumentCaptor.forClass(String.class);
		verify(customMailSender).sendResetConsumer(eq(consumerEmail), newPassword.capture());

		Account account = accountRepository.findByEmail(consumerEmail).orElseThrow();
		assertEquals(DigestUtils.sha3_256Hex(newPassword.getValue()), account.getPassword());
	}

	@Test
	void testForgotPasswordWithInvalidEmail() {
		// Act & Assert
		BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
			accessService.forgotPassword("nonexistent@test.com")
		);
		assertEquals("Invalid email", exception.getMessage());
	}
}