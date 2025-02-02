package com.pareidolia.controller.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.admin.AdminService;
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
public class AdminControllerTest {

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
	private AdminService adminService;

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
		AdminDTO adminDTO = new AdminDTO();
		given(adminService.getData()).willReturn(adminDTO);

		ResponseEntity<AdminDTO> response = restTemplate.exchange(
			"/admin/data",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			AdminDTO.class);

		verify(adminService).getData();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void getDataById() {
		Long accountId = 1L;
		AccountDTO accountDTO = new AccountDTO();
		given(adminService.getData(anyLong())).willReturn(accountDTO);

		ResponseEntity<AccountDTO> response = restTemplate.exchange(
			"/admin/data/{idAccount}",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			AccountDTO.class,
			accountId);

		verify(adminService).getData(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void getAdmins() {
		AdminDTO adminDTO = new AdminDTO();
		Page<AdminDTO> adminPage = new PageImpl<>(List.of(adminDTO), PageRequest.of(0, 10), 1);
		given(adminService.getAdmins(any(), any())).willReturn(adminPage);

		ParameterizedTypeReference<Page<AdminDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<AdminDTO>> response = restTemplate.exchange(
			"/admin/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type);

		verify(adminService).getAdmins(any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(adminPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(adminPage.getContent(), response.getBody().getContent());
		assertEquals(adminPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void create() {
		RegistrationDTO registrationDTO = new RegistrationDTO();
		AccountDTO accountDTO = new AccountDTO();
		given(adminService.createAccount(any(RegistrationDTO.class), any(Account.Type.class)))
			.willReturn(accountDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountDTO> response = restTemplate.exchange(
			"/admin/create/{accountType}",
			HttpMethod.POST,
			new HttpEntity<>(registrationDTO, headers),
			AccountDTO.class,
			Account.Type.ADMIN);

		verify(adminService).createAccount(any(RegistrationDTO.class), any(Account.Type.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void update() {
		AccountDTO accountDTO = new AccountDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		given(adminService.updateAccount(any(AccountDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/admin/update",
			HttpMethod.POST,
			new HttpEntity<>(accountDTO, headers),
			AccountLoginDTO.class);

		verify(adminService).updateAccount(any(AccountDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void updatePassword() {
		PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		given(adminService.updatePassword(any(PasswordUpdateDTO.class))).willReturn(accountLoginDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<AccountLoginDTO> response = restTemplate.exchange(
			"/admin/update/password",
			HttpMethod.POST,
			new HttpEntity<>(passwordUpdateDTO, headers),
			AccountLoginDTO.class);

		verify(adminService).updatePassword(any(PasswordUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void updatePromoter() {
		PromoterDTO promoterDTO = new PromoterDTO();
		given(adminService.updatePromoter(any(PromoterDTO.class))).willReturn(promoterDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/admin/update/promoter",
			HttpMethod.POST,
			new HttpEntity<>(promoterDTO, headers),
			PromoterDTO.class);

		verify(adminService).updatePromoter(any(PromoterDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}

	@Test
	public void delete() {
		Long accountId = 1L;

		ResponseEntity<Void> response = restTemplate.exchange(
			"/admin/{id}",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			Void.class,
			accountId);

		verify(adminService).delete(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminService);
	}
}



