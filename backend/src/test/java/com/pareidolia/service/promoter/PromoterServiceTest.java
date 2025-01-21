package com.pareidolia.service.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.validator.AccountValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PromoterServiceTest {

    @Autowired
    private PromoterService promoterService;

    @MockitoBean
    private AccountValidator accountValidator;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PromoterInfoRepository promoterInfoRepository;

    private Account testAccount;
    private PromoterInfo testPromoterInfo;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setReferenceType(Account.Type.PROMOTER);

        testPromoterInfo = new PromoterInfo();
        testPromoterInfo.setIdPromoter(1L);

        // Mock SecurityContext
        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        User user = new User("test@example.com", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("PROMOTER")));
        when(authentication.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    public void testGetData() {
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(promoterInfoRepository.findByIdPromoter(1L)).thenReturn(Optional.of(testPromoterInfo));

        PromoterDTO result = promoterService.getData();

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getEmail(), result.getEmail());
    }

    @Test
    public void testGetData_NotFound() {
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(JWTService.TokenVerificationException.class, () -> promoterService.getData());
    }

    @Test
    public void testUpdate() {
        PromoterDTO updateDTO = new PromoterDTO();
        updateDTO.setId(1L);
        updateDTO.setEmail("test@example.com");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(accountValidator.getPromoterAndValidateUpdate(any()))
            .thenReturn(Pair.of(testAccount, testPromoterInfo));
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(promoterInfoRepository.save(any())).thenReturn(testPromoterInfo);

        PromoterDTO result = promoterService.update(updateDTO);

        assertNotNull(result);
        assertEquals(updateDTO.getId(), result.getId());
        verify(accountRepository).save(any(Account.class));
        verify(promoterInfoRepository).save(any(PromoterInfo.class));
    }

    @Test
    public void testUpdate_InvalidId() {
        PromoterDTO updateDTO = new PromoterDTO();
        updateDTO.setId(2L); // Different from authenticated user

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> promoterService.update(updateDTO));
    }
} 