package com.pareidolia.service.consumer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ConsumerServiceTest {

	private final String consumerEmail = "consumer@test.com";
	private final String consumerPassword = "TestPassword123#";
	private final String authToken = "TEST_TOKEN";
	@Autowired
	private ConsumerService consumerService;
	@Autowired
	private AccountRepository accountRepository;
	@MockitoSpyBean
	private JWTService jwtService;
	private Account consumerAccount;

	@BeforeEach
	void setUp() {
		// Create consumer account
		consumerAccount = accountRepository.save(Account.builder()
			.email(consumerEmail)
			.password(DigestUtils.sha3_256Hex(consumerPassword))
			.name("Test")
			.surname("Consumer")
			.phone("+39123456789")
			.referenceType(Account.Type.CONSUMER)
			.build());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testGetData() {
		// Act
		ConsumerDTO consumerDTO = consumerService.getData();

		// Assert
		assertNotNull(consumerDTO);
		assertEquals(consumerAccount.getId(), consumerDTO.getId());
		assertEquals(consumerEmail, consumerDTO.getEmail());
		assertEquals(consumerAccount.getName(), consumerDTO.getName());
		assertEquals(consumerAccount.getSurname(), consumerDTO.getSurname());
		assertEquals(consumerAccount.getPhone(), consumerDTO.getPhone());
		assertEquals(Account.Type.CONSUMER.name(), consumerDTO.getReferenceType());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testUpdate() {
		// Arrange
		ConsumerDTO updateDTO = new ConsumerDTO();
		updateDTO.setId(consumerAccount.getId());
		updateDTO.setEmail("updated@test.com");
		updateDTO.setName("Updated");
		updateDTO.setSurname("Consumer");
		updateDTO.setPhone("+39987654321");
		updateDTO.setReferenceType(Account.Type.CONSUMER.name());

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO updatedDTO = consumerService.update(updateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(updateDTO.getId(), updatedDTO.getId());
		assertEquals(updateDTO.getEmail(), updatedDTO.getEmail());
		assertEquals(updateDTO.getName(), updatedDTO.getName());
		assertEquals(updateDTO.getSurname(), updatedDTO.getSurname());
		assertEquals(updateDTO.getPhone(), updatedDTO.getPhone());
		assertEquals(Account.Type.CONSUMER.name(), updatedDTO.getReferenceType());
		assertEquals(authToken, updatedDTO.getAuthToken());

		verify(jwtService).create(eq(Account.Type.CONSUMER), eq(updateDTO.getEmail()), eq(DigestUtils.sha3_256Hex(consumerPassword)));
		verifyNoMoreInteractions(jwtService);

		// Verify database update
		Account updatedAccount = accountRepository.findById(consumerAccount.getId()).orElseThrow();
		assertEquals(updateDTO.getEmail(), updatedAccount.getEmail());
		assertEquals(updateDTO.getName(), updatedAccount.getName());
		assertEquals(updateDTO.getSurname(), updatedAccount.getSurname());
		assertEquals(updateDTO.getPhone(), updatedAccount.getPhone());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testUpdateWithInvalidId() {
		// Arrange
		ConsumerDTO invalidDTO = new ConsumerDTO();
		invalidDTO.setId(999L);
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Consumer");
		invalidDTO.setPhone("+39999888777");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			consumerService.update(invalidDTO)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testUpdateWithNullId() {
		// Arrange
		ConsumerDTO invalidDTO = new ConsumerDTO();
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Consumer");
		invalidDTO.setPhone("+39999888777");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			consumerService.update(invalidDTO)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}
}