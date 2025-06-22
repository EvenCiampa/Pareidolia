package com.pareidolia.controller.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.admin.AdminConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AdminConsumerControllerTest {

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
	private AdminConsumerService adminConsumerService;

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
		Long consumerId = 1L;
		ConsumerDTO consumerDTO = new ConsumerDTO();
		given(adminConsumerService.getData(anyLong())).willReturn(consumerDTO);

		ResponseEntity<ConsumerDTO> response = restTemplate.exchange(
			"/admin/consumer/{id}/data",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			ConsumerDTO.class,
			consumerId);

		verify(adminConsumerService).getData(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminConsumerService);
	}

	@Test
	public void getConsumers() {
		ConsumerDTO consumerDTO = new ConsumerDTO();
		Page<ConsumerDTO> consumerPage = new PageImpl<>(List.of(consumerDTO), PageRequest.of(0, 10), 1);
		given(adminConsumerService.getConsumers(any(), any())).willReturn(consumerPage);

		ParameterizedTypeReference<Page<ConsumerDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<ConsumerDTO>> response = restTemplate.exchange(
			"/admin/consumer/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type);

		verify(adminConsumerService).getConsumers(any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(consumerPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(consumerPage.getContent(), response.getBody().getContent());
		assertEquals(consumerPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminConsumerService);
	}

	@Test
	public void update() {
		ConsumerDTO consumerDTO = new ConsumerDTO();
		given(adminConsumerService.update(any(ConsumerDTO.class))).willReturn(consumerDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<ConsumerDTO> response = restTemplate.exchange(
			"/admin/consumer/update",
			HttpMethod.POST,
			new HttpEntity<>(consumerDTO, headers),
			ConsumerDTO.class);

		verify(adminConsumerService).update(any(ConsumerDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminConsumerService);
	}

	@Test
	public void delete() {
		Long consumerId = 1L;

		ResponseEntity<Void> response = restTemplate.exchange(
			"/admin/consumer/{id}",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			Void.class,
			consumerId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(adminConsumerService).delete(anyLong());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminConsumerService);
	}
}
