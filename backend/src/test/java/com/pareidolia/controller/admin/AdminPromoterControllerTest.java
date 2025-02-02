package com.pareidolia.controller.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.admin.AdminPromoterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AdminPromoterControllerTest {

	private final HttpHeaders headers = new HttpHeaders();

	private final Account.Type accountType = Account.Type.ADMIN;
	private final String accountEmail = "account@mail.it";
	private final String accountPassword = "TestPassword123#";
	private final String authToken = "TEST_TOKEN";

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;
	@MockitoBean
	private AccountRepository accountRepository;
	@MockitoBean
	private AdminPromoterService adminPromoterService;

	@BeforeEach
	public void setup() {
		headers.clear();
		headers.setBearerAuth(authToken);

		doReturn(Map.of(
			"referenceType", accountType.name(),
			"username", accountEmail,
			"password", accountPassword
		)).when(jwtService).verify(eq(authToken));

		doReturn(Optional.of(
			Account.builder()
				.email(accountEmail)
				.password(accountPassword)
				.referenceType(accountType)
				.build()
		)).when(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
	}

	@Test
	public void create() {
		RegistrationDTO registrationDTO = new RegistrationDTO();
		PromoterDTO promoterDTO = new PromoterDTO();
		given(adminPromoterService.create(any(RegistrationDTO.class))).willReturn(promoterDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/admin/promoter/create",
			HttpMethod.POST,
			new HttpEntity<>(registrationDTO, headers),
			PromoterDTO.class);

		verify(adminPromoterService).create(any(RegistrationDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminPromoterService);
	}

	@Test
	public void update() {
		PromoterDTO promoterDTO = new PromoterDTO();
		given(adminPromoterService.update(any(PromoterDTO.class))).willReturn(promoterDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/admin/promoter/update",
			HttpMethod.POST,
			new HttpEntity<>(promoterDTO, headers),
			PromoterDTO.class);

		verify(adminPromoterService).update(any(PromoterDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminPromoterService);
	}

	@Test
	public void delete() {
		Long eventId = 1L;

		ResponseEntity<Void> response = restTemplate.exchange(
			"/admin/promoter/{id}",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			Void.class,
			eventId);

		verify(adminPromoterService).delete(eq(eventId));
		assertEquals(HttpStatus.OK, response.getStatusCode());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminPromoterService);
	}

	@Test
	public void updateImage() {
		Long promoterId = 1L;
		PromoterDTO promoterDTO = new PromoterDTO();
		given(adminPromoterService.updateImage(anyLong(), any())).willReturn(promoterDTO);

		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/admin/promoter/{id}/image",
			HttpMethod.POST,
			new HttpEntity<>(createMultipartBody(file), headers),
			PromoterDTO.class,
			promoterId);

		verify(adminPromoterService).updateImage(eq(promoterId), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminPromoterService);
	}

	@Test
	public void deleteImage() {
		Long promoterId = 1L;
		PromoterDTO promoterDTO = new PromoterDTO();
		given(adminPromoterService.deleteImage(anyLong())).willReturn(promoterDTO);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/admin/promoter/{id}/image",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			PromoterDTO.class,
			promoterId);

		verify(adminPromoterService).deleteImage(eq(promoterId));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminPromoterService);
	}

	private MultiValueMap<String, Object> createMultipartBody(MultipartFile file) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", file.getResource());
		return body;
	}
}
