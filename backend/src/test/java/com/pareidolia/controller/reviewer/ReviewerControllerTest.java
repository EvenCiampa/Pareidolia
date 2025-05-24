package com.pareidolia.controller.reviewer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.reviewer.ReviewerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
class ReviewerControllerTest {

	private final HttpHeaders headers = new HttpHeaders();
	private final Account.Type accountType = Account.Type.REVIEWER;
	private final String accountEmail = "reviewer@mail.it";
	private final String accountPassword = "TestPassword123#";
	private final String authToken = "TEST_TOKEN";

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;

	@MockitoBean
	private AccountRepository accountRepository;

	@MockitoBean
	private ReviewerService reviewerService;

	@BeforeEach
	void setUp() {
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
	void testGetData() {
		ReviewerDTO reviewerDTO = new ReviewerDTO();
		reviewerDTO.setId(1L);
		reviewerDTO.setEmail("reviewer@test.com");
		reviewerDTO.setName("Test Reviewer");

		given(reviewerService.getData()).willReturn(reviewerDTO);

		ResponseEntity<ReviewerDTO> response = restTemplate.exchange(
			"/reviewer/data",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			ReviewerDTO.class);

		verify(reviewerService).getData();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(reviewerDTO.getId(), response.getBody().getId());
		assertEquals(reviewerDTO.getEmail(), response.getBody().getEmail());
		assertEquals(reviewerDTO.getName(), response.getBody().getName());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerService);
	}

	@Test
	void testUpdate() {
		ReviewerDTO reviewerDTO = new ReviewerDTO();
		reviewerDTO.setId(1L);
		reviewerDTO.setEmail("reviewer@test.com");
		reviewerDTO.setName("Updated Name");

		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		accountLoginDTO.setId(1L);
		accountLoginDTO.authToken = "new-token";

		given(reviewerService.update(any(ReviewerDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/reviewer/update",
			HttpMethod.POST,
			new HttpEntity<>(reviewerDTO, headers),
			AccountLoginDTO.class);

		verify(reviewerService).update(any(ReviewerDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(accountLoginDTO.getId(), response.getBody().getId());
		assertEquals(accountLoginDTO.authToken, response.getBody().authToken);

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerService);
	}

	@Test
	void testUpdatePassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		passwordUpdateDTO.setCurrentPassword("oldpass");
		passwordUpdateDTO.setNewPassword("newpass");

		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		accountLoginDTO.setId(1L);
		accountLoginDTO.authToken = "new-token";

		given(reviewerService.updatePassword(any(PasswordUpdateDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/reviewer/update/password",
			HttpMethod.POST,
			new HttpEntity<>(passwordUpdateDTO, headers),
			AccountLoginDTO.class);

		verify(reviewerService).updatePassword(any(PasswordUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(accountLoginDTO.getId(), response.getBody().getId());
		assertEquals(accountLoginDTO.authToken, response.getBody().authToken);

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerService);
	}
} 