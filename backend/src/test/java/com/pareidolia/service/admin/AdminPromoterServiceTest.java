package com.pareidolia.service.admin;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.util.TestImageGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = AdminPromoterServiceTest.Initializer.class)
public class AdminPromoterServiceTest {

	@TempDir
	static File tempDir;
	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";
	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";
	@Autowired
	private AdminPromoterService adminPromoterService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	private Account adminAccount;
	private Account promoterAccount;
	private PromoterInfo promoterInfo;

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

		// Create promoter account
		promoterAccount = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("Test")
			.surname("Promoter")
			.phone("+39987654321")
			.referenceType(Account.Type.PROMOTER)
			.build());

		promoterInfo = promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(promoterAccount.getId())
			.presentation("Test presentation")
			.build());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreate() {
		// Arrange
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setEmail("new.promoter@test.com");
		registrationDTO.setPassword("NewPassword123#");
		registrationDTO.setName("New");
		registrationDTO.setSurname("Promoter");
		registrationDTO.setPhone("+39111222333");

		// Act
		PromoterDTO createdDTO = adminPromoterService.create(registrationDTO);

		// Assert
		assertNotNull(createdDTO);
		assertNotNull(createdDTO.getId());
		assertEquals(registrationDTO.getEmail(), createdDTO.getEmail());
		assertEquals(registrationDTO.getName(), createdDTO.getName());
		assertEquals(registrationDTO.getSurname(), createdDTO.getSurname());
		assertEquals(registrationDTO.getPhone(), createdDTO.getPhone());
		assertEquals(Account.Type.PROMOTER.name(), createdDTO.getReferenceType());

		// Verify PromoterInfo was created
		assertTrue(promoterInfoRepository.findByIdPromoter(createdDTO.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdate() {
		// Arrange
		PromoterDTO updateDTO = new PromoterDTO();
		updateDTO.setId(promoterAccount.getId());
		updateDTO.setEmail("updated.promoter@test.com");
		updateDTO.setName("Updated");
		updateDTO.setSurname("Promoter");
		updateDTO.setPhone("+39444555666");
		updateDTO.setPresentation("Updated presentation");
		updateDTO.setReferenceType(Account.Type.PROMOTER.name());

		// Act
		PromoterDTO updatedDTO = adminPromoterService.update(updateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(updateDTO.getId(), updatedDTO.getId());
		assertEquals(updateDTO.getEmail(), updatedDTO.getEmail());
		assertEquals(updateDTO.getName(), updatedDTO.getName());
		assertEquals(updateDTO.getSurname(), updatedDTO.getSurname());
		assertEquals(updateDTO.getPhone(), updatedDTO.getPhone());
		assertEquals(updateDTO.getPresentation(), updatedDTO.getPresentation());
		assertEquals(Account.Type.PROMOTER.name(), updatedDTO.getReferenceType());

		// Verify database update
		Account updatedAccount = accountRepository.findById(promoterAccount.getId()).orElseThrow();
		PromoterInfo updatedInfo = promoterInfoRepository.findByIdPromoter(promoterAccount.getId()).orElseThrow();
		assertEquals(updateDTO.getEmail(), updatedAccount.getEmail());
		assertEquals(updateDTO.getName(), updatedAccount.getName());
		assertEquals(updateDTO.getPresentation(), updatedInfo.getPresentation());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminPromoterService.delete(promoterAccount.getId());

		// Assert
		assertFalse(accountRepository.findById(promoterAccount.getId()).isPresent());
		assertFalse(promoterInfoRepository.findByIdPromoter(promoterAccount.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateWithInvalidId() {
		// Arrange
		PromoterDTO invalidDTO = new PromoterDTO();
		invalidDTO.setId(999L);
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Promoter");
		invalidDTO.setPhone("+39999888777");
		invalidDTO.setPresentation("Invalid presentation");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.update(invalidDTO)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateWithNullId() {
		// Arrange
		PromoterDTO invalidDTO = new PromoterDTO();
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Promoter");
		invalidDTO.setPhone("+39999888777");
		invalidDTO.setPresentation("Invalid presentation");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.update(invalidDTO)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.delete(999L)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithNullId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.delete(null)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateImage() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test-image.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			TestImageGenerator.generateTestImage()
		);

		// Act
		PromoterDTO updatedPromoter = adminPromoterService.updateImage(promoterAccount.getId(), imageFile);

		// Assert
		assertNotNull(updatedPromoter);
		assertNotNull(updatedPromoter.getPhoto());

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(updatedPromoter.getId()).orElseThrow();
		assertNotNull(promoterInfo.getPhoto());
		assertThat(promoterInfo.getPhoto(), CoreMatchers.startsWith("TEST_URL/thumbnail-"));
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteImage() {
		// Arrange
		promoterInfo.setPhoto("test-photo.jpg");
		promoterInfoRepository.save(promoterInfo);

		// Act
		PromoterDTO updatedPromoter = adminPromoterService.deleteImage(promoterAccount.getId());

		// Assert
		assertNotNull(updatedPromoter);
		assertNull(updatedPromoter.getPhoto());

		PromoterInfo updatedInfo = promoterInfoRepository.findByIdPromoter(promoterAccount.getId()).orElseThrow();
		assertNull(updatedInfo.getPhoto());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateImageForNonExistentPromoter() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			TestImageGenerator.generateTestImage()
		);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.updateImage(999L, imageFile)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteImageForNonExistentPromoter() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.deleteImage(999L)
		);
		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateImageWithInvalidFile() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test image content".getBytes()
		);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminPromoterService.updateImage(promoterAccount.getId(), imageFile)
		);
		assertEquals("Invalid image file", exception.getMessage());
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(@NotNull ConfigurableApplicationContext context) {
			TestPropertyValues.of("app.upload.dir=" + tempDir).applyTo(context);
		}
	}
}