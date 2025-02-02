package com.pareidolia.service.admin;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
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
public class AdminServiceTest {

	private final String authToken = "TEST_TOKEN";
	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";
	private final String consumerEmail = "test@test.com";
	private final String consumerPassword = "TestPassword123#";
	@Autowired
	private AdminService adminService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@MockitoSpyBean
	private JWTService jwtService;
	private Account adminAccount;
	private Account testAccount;

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

		// Create test account
		testAccount = accountRepository.save(Account.builder()
			.email(consumerEmail)
			.password(DigestUtils.sha3_256Hex(consumerPassword))
			.name("Test")
			.surname("User")
			.phone("+39987654321")
			.referenceType(Account.Type.CONSUMER)
			.build());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetData() {
		// Act
		AdminDTO adminDTO = adminService.getData();

		// Assert
		assertNotNull(adminDTO);
		assertEquals(adminAccount.getId(), adminDTO.getId());
		assertEquals(adminEmail, adminDTO.getEmail());
		assertEquals(Account.Type.ADMIN.name(), adminDTO.getReferenceType());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetAccountData() {
		// Act
		AccountDTO accountDTO = adminService.getData(testAccount.getId());

		// Assert
		assertNotNull(accountDTO);
		assertEquals(testAccount.getId(), accountDTO.getId());
		assertEquals(testAccount.getEmail(), accountDTO.getEmail());
		assertEquals(Account.Type.CONSUMER.name(), accountDTO.getReferenceType());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetAdmins() {
		// Act
		Page<AdminDTO> admins = adminService.getAdmins(0, 10);

		// Assert
		assertNotNull(admins);
		assertFalse(admins.isEmpty());
		assertEquals(1, admins.getTotalElements());
		assertEquals(adminAccount.getId(), admins.getContent().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateAccount() {
		// Arrange
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setEmail("new@test.com");
		registrationDTO.setPassword("NewPassword123#");
		registrationDTO.setName("New");
		registrationDTO.setSurname("User");
		registrationDTO.setPhone("+39111222333");

		// Act
		AccountDTO createdDTO = adminService.createAccount(registrationDTO, Account.Type.CONSUMER);

		// Assert
		assertNotNull(createdDTO);
		assertNotNull(createdDTO.getId());
		assertEquals(registrationDTO.getEmail(), createdDTO.getEmail());
		assertEquals(registrationDTO.getName(), createdDTO.getName());
		assertEquals(Account.Type.CONSUMER.name(), createdDTO.getReferenceType());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateAccount() {
		// Arrange
		AccountDTO updateDTO = new AccountDTO();
		updateDTO.setId(testAccount.getId());
		updateDTO.setEmail("updated@test.com");
		updateDTO.setName("Updated");
		updateDTO.setSurname("User");
		updateDTO.setPhone("+39999888777");
		updateDTO.setReferenceType(Account.Type.PROMOTER.name());

		// Act
		AccountDTO updatedDTO = adminService.updateAccount(updateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(updateDTO.getId(), updatedDTO.getId());
		assertEquals(updateDTO.getEmail(), updatedDTO.getEmail());
		assertEquals(updateDTO.getName(), updatedDTO.getName());
		assertEquals(Account.Type.PROMOTER.name(), updatedDTO.getReferenceType());

		// Verify PromoterInfo was created
		assertTrue(promoterInfoRepository.findByIdPromoter(updatedDTO.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdatePromoter() {
		// Arrange
		// Create a promoter first
		String promoterPassword = "TestPassword123";

		Account promoterAccount = accountRepository.save(Account.builder()
			.email("promoter@test.com")
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("Test")
			.surname("Promoter")
			.phone("+39444555666")
			.referenceType(Account.Type.PROMOTER)
			.build());

		promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(promoterAccount.getId())
			.presentation("Test presentation")
			.build());

		PromoterDTO updateDTO = new PromoterDTO();
		updateDTO.setId(promoterAccount.getId());
		updateDTO.setEmail("updated.promoter@test.com");
		updateDTO.setName("Updated");
		updateDTO.setSurname("Promoter");
		updateDTO.setPhone("+39666777888");
		updateDTO.setPresentation("Updated presentation");
		updateDTO.setReferenceType(Account.Type.PROMOTER.name());

		// Act
		PromoterDTO updatedDTO = adminService.updatePromoter(updateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(updateDTO.getId(), updatedDTO.getId());
		assertEquals(updateDTO.getEmail(), updatedDTO.getEmail());
		assertEquals(updateDTO.getPresentation(), updatedDTO.getPresentation());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdatePassword() {
		// Arrange
		PasswordUpdateDTO request = new PasswordUpdateDTO();
		request.setCurrentPassword(adminPassword);
		request.setNewPassword("TestPassword123!");

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO updatedDTO = adminService.updatePassword(request);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(adminAccount.getName(), updatedDTO.getName());
		assertEquals(adminAccount.getSurname(), updatedDTO.getSurname());
		assertEquals(adminAccount.getPhone(), updatedDTO.getPhone());
		assertEquals(adminAccount.getEmail(), updatedDTO.getEmail());
		assertEquals(adminAccount.getReferenceType().name(), updatedDTO.getReferenceType());
		assertEquals(authToken, updatedDTO.getAuthToken());

		verify(jwtService).create(eq(Account.Type.ADMIN), eq(adminAccount.getEmail()), eq(DigestUtils.sha3_256Hex(request.getNewPassword())));
		verifyNoMoreInteractions(jwtService);
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdatePasswordWithWrongCurrentPassword() {
		// Arrange
		PasswordUpdateDTO request = new PasswordUpdateDTO();
		request.setCurrentPassword("wrongPass123!");
		request.setNewPassword("newPass123!");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adminService.updatePassword(request));
		assertEquals("Current password is incorrect", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdatePasswordWithInvalidNewPassword() {
		// Arrange
		PasswordUpdateDTO request = new PasswordUpdateDTO();
		request.setCurrentPassword("oldPass123!");
		request.setNewPassword("weak");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adminService.updatePassword(request));
		assertEquals("Current password is incorrect", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminService.delete(testAccount.getId());

		// Assert
		assertFalse(accountRepository.findById(testAccount.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteSelf() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminService.delete(adminAccount.getId())
		);
		assertEquals("Invalid Account Type", exception.getMessage());
	}
}