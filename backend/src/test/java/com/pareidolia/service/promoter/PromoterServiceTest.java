package com.pareidolia.service.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.util.TestImageGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
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
@ContextConfiguration(initializers = PromoterServiceTest.Initializer.class)
public class PromoterServiceTest {

	@TempDir
	static File tempDir;
	private final Account.Type accountType = Account.Type.PROMOTER;
	private final String accountEmail = "account@mail.it";
	private final String accountPassword = "TestPassword123#";
	private final String accountName = "Even";
	private final String accountSurname = "Ciampa";
	private final String accountPhone = "+39123456789";
	private final String promoterPresentation = "Test presentation";
	private final String authToken = "TEST_TOKEN";
	@Autowired
	private PromoterService promoterService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@MockitoSpyBean
	private JWTService jwtService;

	@BeforeEach
	void setUp() {
		Account account = accountRepository.save(Account.builder()
			.email(accountEmail)
			.password(DigestUtils.sha3_256Hex(accountPassword))
			.name(accountName)
			.surname(accountSurname)
			.phone(accountPhone)
			.referenceType(accountType)
			.build());
		promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(account.getId())
			.presentation(promoterPresentation)
			.build());
	}

	@Test
	@WithMockUser(username = accountEmail, password = accountPassword, authorities = {"PROMOTER"})
	public void testGetData() {
		// Arrange
		Account account = accountRepository.findAll().get(0);
		PromoterDTO expectedPromoterDTO = new PromoterDTO();
		expectedPromoterDTO.setId(account.getId());
		expectedPromoterDTO.setEmail(accountEmail);
		expectedPromoterDTO.setName(accountName);
		expectedPromoterDTO.setSurname(accountSurname);
		expectedPromoterDTO.setPhone(accountPhone);
		expectedPromoterDTO.setReferenceType(accountType.name());
		expectedPromoterDTO.setPresentation(promoterPresentation);

		// Act
		PromoterDTO promoterDTO = promoterService.getData();

		// Assert
		assertNotNull(promoterDTO.getCreationTime());
		promoterDTO.setCreationTime(null);

		assertEquals(expectedPromoterDTO, promoterDTO);
		assertEquals(1, accountRepository.count());
		Account currentAccount = accountRepository.findAll().get(0);
		assertEquals(account, currentAccount);

		verifyNoMoreInteractions(jwtService);
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdate() {
		// Arrange
		PromoterDTO updateDTO = new PromoterDTO();
		updateDTO.setId(accountRepository.findAll().get(0).getId());
		updateDTO.setEmail("updated@test.com");
		updateDTO.setName("Updated");
		updateDTO.setSurname("Name");
		updateDTO.setPhone("+39987654321");
		updateDTO.setPresentation("Updated presentation");
		updateDTO.setReferenceType(Account.Type.PROMOTER.name());

		doReturn(authToken).when(jwtService).create(any(), any(), any());

		// Act
		AccountLoginDTO updatedDTO = promoterService.update(updateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(updateDTO.getId(), updatedDTO.getId());
		assertEquals(updateDTO.getEmail(), updatedDTO.getEmail());
		assertEquals(updateDTO.getName(), updatedDTO.getName());
		assertEquals(updateDTO.getSurname(), updatedDTO.getSurname());
		assertEquals(updateDTO.getPhone(), updatedDTO.getPhone());
		assertEquals(Account.Type.PROMOTER.name(), updatedDTO.getReferenceType());
		assertEquals(authToken, updatedDTO.getAuthToken());

		verify(jwtService).create(any(), eq(updateDTO.getEmail()), eq(DigestUtils.sha3_256Hex(accountPassword)));
		verifyNoMoreInteractions(jwtService);

		// Verify database update
		Account updatedAccount = accountRepository.findById(accountRepository.findAll().get(0).getId()).orElseThrow();
		PromoterInfo updatedInfo = promoterInfoRepository.findByIdPromoter(accountRepository.findAll().get(0).getId()).orElseThrow();
		assertEquals(updateDTO.getEmail(), updatedAccount.getEmail());
		assertEquals(updateDTO.getName(), updatedAccount.getName());
		assertEquals(updateDTO.getSurname(), updatedAccount.getSurname());
		assertEquals(updateDTO.getPhone(), updatedAccount.getPhone());
		assertEquals(updateDTO.getPresentation(), updatedInfo.getPresentation());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateWithInvalidId() {
		// Arrange
		PromoterDTO invalidDTO = new PromoterDTO();
		invalidDTO.setId(999L); // Non-existent ID
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Update");
		invalidDTO.setPhone("+39987654321");
		invalidDTO.setPresentation("Invalid presentation");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterService.update(invalidDTO)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateWithNullId() {
		// Arrange
		PromoterDTO invalidDTO = new PromoterDTO();
		invalidDTO.setEmail("invalid@test.com");
		invalidDTO.setName("Invalid");
		invalidDTO.setSurname("Update");
		invalidDTO.setPhone("+39987654321");
		invalidDTO.setPresentation("Invalid presentation");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterService.update(invalidDTO)
		);
		assertEquals("Invalid ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateImage() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test-image.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			TestImageGenerator.generateTestImage()
		);

		// Act
		PromoterDTO updatedPromoter = promoterService.updateImage(imageFile);

		// Assert
		assertNotNull(updatedPromoter);
		assertNotNull(updatedPromoter.getPhoto());

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(updatedPromoter.getId()).orElseThrow();
		assertNotNull(promoterInfo.getPhoto());
		assertThat(promoterInfo.getPhoto(), CoreMatchers.startsWith("TEST_URL/thumbnail-"));
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testDeleteImage() {
		// Arrange
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(
			accountRepository.findByEmail(accountEmail).orElseThrow().getId()
		).orElseThrow();
		promoterInfo.setPhoto("test-photo.jpg");
		promoterInfoRepository.save(promoterInfo);

		// Act
		PromoterDTO updatedPromoter = promoterService.deleteImage();

		// Assert
		assertNotNull(updatedPromoter);
		assertNull(updatedPromoter.getPhoto());

		PromoterInfo updatedInfo = promoterInfoRepository.findByIdPromoter(updatedPromoter.getId()).orElseThrow();
		assertNull(updatedInfo.getPhoto());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateImageWithInvalidFile() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test.gif",
			MediaType.IMAGE_GIF_VALUE,
			"test image content".getBytes()
		);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterService.updateImage(imageFile)
		);
		assertEquals("Invalid file extension. Allowed: png, jpg, jpeg", exception.getMessage());
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(@NotNull ConfigurableApplicationContext context) {
			TestPropertyValues.of("app.upload.dir=" + tempDir).applyTo(context);
		}
	}
}