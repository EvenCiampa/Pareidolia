package com.pareidolia.controller.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.promoter.PromoterService;
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
public class PromoterControllerTest {

	private final HttpHeaders headers = new HttpHeaders();

	private final Account.Type accountType = Account.Type.PROMOTER;
	private final String accountEmail = "account@mail.it";
	private final String accountPassword = "TestPassword123#";
	private final String authToken = "TEST_TOKEN";

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;
	@MockitoBean
	private PromoterService promoterService;
	@MockitoBean
	private AccountRepository accountRepository;

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
	public void getData() {
		PromoterDTO promoterDTO = new PromoterDTO();
		given(promoterService.getData()).willReturn(promoterDTO);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/promoter/data",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			PromoterDTO.class);

		verify(promoterService).getData();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterService);
	}

	@Test
	public void update() {
		PromoterDTO promoterDTO = new PromoterDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		given(promoterService.update(any(PromoterDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/promoter/update",
			HttpMethod.POST,
			new HttpEntity<>(promoterDTO, headers),
			AccountLoginDTO.class);

		verify(promoterService).update(any(PromoterDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterService);
	}

	@Test
	public void updatePassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		given(promoterService.updatePassword(any(PasswordUpdateDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/promoter/update/password",
			HttpMethod.POST,
			new HttpEntity<>(passwordUpdateDTO, headers),
			AccountLoginDTO.class);

		verify(promoterService).updatePassword(any(PasswordUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterService);
	}

	@Test
	public void updateImage() {
		PromoterDTO promoterDTO = new PromoterDTO();
		given(promoterService.updateImage(any())).willReturn(promoterDTO);

		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/promoter/update/image",
			HttpMethod.POST,
			new HttpEntity<>(createMultipartBody(file), headers),
			PromoterDTO.class);

		verify(promoterService).updateImage(any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterService);
	}

	@Test
	public void deleteImage() {
		PromoterDTO promoterDTO = new PromoterDTO();
		given(promoterService.deleteImage()).willReturn(promoterDTO);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/promoter/image",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			PromoterDTO.class);

		verify(promoterService).deleteImage();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterService);
	}

	private MultiValueMap<String, Object> createMultipartBody(MultipartFile file) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", file.getResource());
		return body;
	}
}
