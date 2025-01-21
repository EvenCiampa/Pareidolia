package com.pareidolia.controller.admin;

import com.pareidolia.dto.AccountDTO;
import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.service.admin.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AdminControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private AdminService adminService;

    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        headers.clear();
        headers.add("Authorization", "Bearer test-token");
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void create() {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        AccountDTO accountDTO = new AccountDTO();
        given(adminService.createAccount(any(RegistrationDTO.class), any(Account.Type.class)))
                .willReturn(accountDTO);

        ResponseEntity<AccountDTO> response = restTemplate.exchange(
                "/admin/create/{referenceType}",
                HttpMethod.POST,
                new HttpEntity<>(registrationDTO, headers),
                AccountDTO.class,
                Account.Type.ADMIN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void update() {
        AccountDTO accountDTO = new AccountDTO();
        given(adminService.updateAccount(any(AccountDTO.class))).willReturn(accountDTO);

        ResponseEntity<AccountDTO> response = restTemplate.exchange(
                "/admin/update",
                HttpMethod.POST,
                new HttpEntity<>(accountDTO, headers),
                AccountDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void updatePromoter() {
        PromoterDTO promoterDTO = new PromoterDTO();
        given(adminService.updatePromoter(any(PromoterDTO.class))).willReturn(promoterDTO);

        ResponseEntity<PromoterDTO> response = restTemplate.exchange(
                "/admin/update/promoter",
                HttpMethod.POST,
                new HttpEntity<>(promoterDTO, headers),
                PromoterDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
} 