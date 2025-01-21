package com.pareidolia.controller.generic;

import com.pareidolia.dto.LoginDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.service.generic.AccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AccessControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private AccessService accessService;

    @BeforeEach
    public void setup() {
        // Setup if needed
    }

    @Test
    public void register() {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        String expectedToken = "test-token";
        
        given(accessService.register(any(RegistrationDTO.class))).willReturn(expectedToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/generic/access/register",
                HttpMethod.POST,
                new HttpEntity<>(registrationDTO),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedToken, response.getBody());
    }

    @Test
    public void login() {
        LoginDTO loginDTO = new LoginDTO();
        String expectedToken = "test-token";
        
        given(accessService.login(any(LoginDTO.class))).willReturn(expectedToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/generic/access/login",
                HttpMethod.POST,
                new HttpEntity<>(loginDTO),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedToken, response.getBody());
    }

    @Test
    public void forgotPassword() {
        String email = "test@example.com";

        ResponseEntity<Void> response = restTemplate.exchange(
                "/generic/access/forgotPassword?email={email}",
                HttpMethod.POST,
                new HttpEntity<>(null),
                Void.class,
                email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
} 