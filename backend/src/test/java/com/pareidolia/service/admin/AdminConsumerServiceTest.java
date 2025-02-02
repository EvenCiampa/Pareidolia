package com.pareidolia.service.admin;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AdminConsumerServiceTest {

	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";
	private final String consumerEmail = "test@test.com";
	private final String consumerPassword = "TestPassword123#";
	@Autowired
	private AdminConsumerService adminConsumerService;
	@Autowired
	private AccountRepository accountRepository;
	private Account adminAccount;
	private Account consumerAccount;

	@BeforeEach
	void setUp() {
		// Create admin account
		adminAccount = accountRepository.save(Account.builder()
			.email(adminEmail)
			.password(DigestUtils.sha3_256Hex(adminPassword))
			.name("Test")
			.surname("Admin")
			.phone("+39123456789")
			.referenceType(Account.Type.ADMIN)
			.build());

		// Create consumer account
		consumerAccount = accountRepository.save(Account.builder()
			.email(consumerEmail)
			.password(DigestUtils.sha3_256Hex(consumerPassword))
			.name("Test")
			.surname("Consumer")
			.phone("+39987654321")
			.referenceType(Account.Type.CONSUMER)
			.build());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetData() {
		// Act
		ConsumerDTO consumerDTO = adminConsumerService.getData(consumerAccount.getId());

		// Assert
		assertNotNull(consumerDTO);
		assertEquals(consumerAccount.getId(), consumerDTO.getId());
		assertEquals(consumerAccount.getEmail(), consumerDTO.getEmail());
		assertEquals(consumerAccount.getName(), consumerDTO.getName());
		assertEquals(consumerAccount.getSurname(), consumerDTO.getSurname());
		assertEquals(consumerAccount.getPhone(), consumerDTO.getPhone());
		assertEquals(Account.Type.CONSUMER.name(), consumerDTO.getReferenceType());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetConsumers() {
		// Act
		Page<ConsumerDTO> consumers = adminConsumerService.getConsumers(0, 10);

		// Assert
		assertNotNull(consumers);
		assertFalse(consumers.isEmpty());
		assertEquals(1, consumers.getTotalElements());
		assertEquals(consumerAccount.getId(), consumers.getContent().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdate() {
		// Arrange
		ConsumerDTO updateDTO = new ConsumerDTO();
		updateDTO.setId(consumerAccount.getId());
		updateDTO.setEmail("updated@test.com");
		updateDTO.setName("Updated");
		updateDTO.setSurname("Consumer");
		updateDTO.setPhone("+39111222333");
		updateDTO.setReferenceType(Account.Type.CONSUMER.name());

		// Act
		ConsumerDTO updatedDTO = adminConsumerService.update(updateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(updateDTO.getId(), updatedDTO.getId());
		assertEquals(updateDTO.getEmail(), updatedDTO.getEmail());
		assertEquals(updateDTO.getName(), updatedDTO.getName());
		assertEquals(updateDTO.getSurname(), updatedDTO.getSurname());
		assertEquals(updateDTO.getPhone(), updatedDTO.getPhone());
		assertEquals(Account.Type.CONSUMER.name(), updatedDTO.getReferenceType());

		// Verify database update
		Account updatedAccount = accountRepository.findById(consumerAccount.getId()).orElseThrow();
		assertEquals(updateDTO.getEmail(), updatedAccount.getEmail());
		assertEquals(updateDTO.getName(), updatedAccount.getName());
		assertEquals(updateDTO.getSurname(), updatedAccount.getSurname());
		assertEquals(updateDTO.getPhone(), updatedAccount.getPhone());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminConsumerService.delete(consumerAccount.getId());

		// Assert
		assertFalse(accountRepository.findById(consumerAccount.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetDataWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminConsumerService.getData(999L)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetDataWithNullId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminConsumerService.getData(null)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateWithInvalidId() {
		// Arrange
		ConsumerDTO invalidDTO = new ConsumerDTO();
		invalidDTO.setId(999L);
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Consumer");
		invalidDTO.setPhone("+39999888777");
		invalidDTO.setReferenceType(Account.Type.CONSUMER.name());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminConsumerService.update(invalidDTO)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateWithNullId() {
		// Arrange
		ConsumerDTO invalidDTO = new ConsumerDTO();
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Consumer");
		invalidDTO.setPhone("+39999888777");
		invalidDTO.setReferenceType(Account.Type.CONSUMER.name());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminConsumerService.update(invalidDTO)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminConsumerService.delete(999L)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithNullId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminConsumerService.delete(null)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}
}