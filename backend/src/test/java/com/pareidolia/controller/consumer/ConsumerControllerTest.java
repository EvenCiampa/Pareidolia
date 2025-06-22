package com.pareidolia.controller.consumer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.consumer.ConsumerService;
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
public class ConsumerControllerTest {

	private final HttpHeaders headers = new HttpHeaders();

	private final Account.Type accountType = Account.Type.CONSUMER;
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
	private ConsumerService consumerService;

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
		ConsumerDTO consumerDTO = new ConsumerDTO();
		given(consumerService.getData()).willReturn(consumerDTO);

		ResponseEntity<ConsumerDTO> response = restTemplate.exchange(
			"/consumer/data",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			ConsumerDTO.class);

		verify(consumerService).getData();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, consumerService);
	}

	@Test
	public void update() {
		ConsumerDTO consumerDTO = new ConsumerDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		given(consumerService.update(any(ConsumerDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/consumer/update",
			HttpMethod.POST,
			new HttpEntity<>(consumerDTO, headers),
			AccountLoginDTO.class);

		verify(consumerService).update(any(ConsumerDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, consumerService);
	}

	@Test
	public void updatePassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		given(consumerService.updatePassword(any(PasswordUpdateDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/consumer/update/password",
			HttpMethod.POST,
			new HttpEntity<>(passwordUpdateDTO, headers),
			AccountLoginDTO.class);

		verify(consumerService).updatePassword(any(PasswordUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, consumerService);
	}
}
