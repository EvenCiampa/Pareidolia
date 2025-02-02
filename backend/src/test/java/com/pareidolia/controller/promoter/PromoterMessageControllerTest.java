package com.pareidolia.controller.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.promoter.PromoterMessageService;
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
public class PromoterMessageControllerTest {

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
	private AccountRepository accountRepository;
	@MockitoBean
	private PromoterMessageService promoterMessageService;

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
	public void getEventDraftMessages() {
		Long idEventDraft = 1L;
		MessageDTO messageDTO = new MessageDTO();
		Page<MessageDTO> messagePage = new PageImpl<>(List.of(messageDTO), PageRequest.of(0, 10), 1);
		given(promoterMessageService.getEventDraftMessages(any(), any(), any())).willReturn(messagePage);

		ParameterizedTypeReference<Page<MessageDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<MessageDTO>> response = restTemplate.exchange(
			"/promoter/message/{idEventDraft}/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type,
			idEventDraft);

		verify(promoterMessageService).getEventDraftMessages(any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(messagePage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(messagePage.getContent(), response.getBody().getContent());
		assertEquals(messagePage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterMessageService);
	}

	@Test
	public void create() {
		Long idEventDraft = 1L;
		String message = "Test message";
		MessageDTO messageDTO = new MessageDTO();
		given(promoterMessageService.create(anyLong(), any(String.class))).willReturn(messageDTO);

		headers.setContentType(MediaType.TEXT_PLAIN);

		ResponseEntity<MessageDTO> response = restTemplate.exchange(
			"/promoter/message/{idEventDraft}/create",
			HttpMethod.POST,
			new HttpEntity<>(message, headers),
			MessageDTO.class,
			idEventDraft);

		verify(promoterMessageService).create(anyLong(), any(String.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterMessageService);
	}

}
