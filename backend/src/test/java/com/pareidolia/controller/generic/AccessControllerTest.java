package com.pareidolia.controller.generic;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.LoginDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.generic.AccessService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AccessControllerTest {

	private final HttpHeaders headers = new HttpHeaders();

	private final String accountEmail = "account@mail.it";

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;
	@MockitoBean
	private AccessService accessService;
	@MockitoBean
	private AccountRepository accountRepository;

	@Test
	public void register() {
		RegistrationDTO registrationDTO = new RegistrationDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();

		given(accessService.register(any(RegistrationDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/generic/access/register",
			HttpMethod.POST,
			new HttpEntity<>(registrationDTO, headers),
			AccountLoginDTO.class);

		verify(accessService).register(any(RegistrationDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verifyNoMoreInteractions(jwtService, accountRepository, accessService);
	}

	@Test
	public void login() {
		LoginDTO loginDTO = new LoginDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();

		given(accessService.login(any(LoginDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/generic/access/login",
			HttpMethod.POST,
			new HttpEntity<>(loginDTO, headers),
			AccountLoginDTO.class);

		verify(accessService).login(any(LoginDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verifyNoMoreInteractions(jwtService, accountRepository, accessService);
	}

	@Test
	public void forgotPassword() {
		ResponseEntity<Void> response = restTemplate.exchange(
			"/generic/access/forgotPassword?email={email}",
			HttpMethod.POST,
			new HttpEntity<>(null, headers),
			Void.class,
			accountEmail);

		verify(accessService).forgotPassword(anyString());
		assertEquals(HttpStatus.OK, response.getStatusCode());

		verifyNoMoreInteractions(jwtService, accountRepository, accessService);
	}

}
