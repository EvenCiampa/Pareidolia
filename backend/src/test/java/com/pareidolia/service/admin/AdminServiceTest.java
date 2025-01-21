package com.pareidolia.service.admin;

import com.pareidolia.dto.AccountDTO;
import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
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
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
public class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @MockitoBean
    private AccountValidator accountValidator;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PromoterInfoRepository promoterInfoRepository;

    private Account testAccount;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setEmail("admin@example.com");
        testAccount.setReferenceType(Account.Type.ADMIN);

        // Mock SecurityContext
        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        User user = new User("admin@example.com", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ADMIN")));
        when(authentication.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    public void testGetData() {
        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAccount));

        AdminDTO result = adminService.getData();

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getEmail(), result.getEmail());
    }

    @Test
    public void testGetData_ById() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        AccountDTO result = adminService.getData(1L);

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
    }

    @Test
    public void testCreateAccount() {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password");

        when(accountRepository.save(any())).thenReturn(testAccount);

        AccountDTO result = adminService.createAccount(registrationDTO, Account.Type.ADMIN);

        assertNotNull(result);
        verify(accountValidator).createAccountValidator(registrationDTO);
        verify(accountRepository).save(any());
    }

    @Test
    public void testUpdateAccount() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(1L);
        accountDTO.setEmail("updated@example.com");
        accountDTO.setReferenceType("ADMIN");

        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAccount));
        when(accountValidator.getAccountAndValidateUpdate(any(), eq(true))).thenReturn(testAccount);
        when(accountRepository.save(any())).thenReturn(testAccount);

        AccountDTO result = adminService.updateAccount(accountDTO);

        assertNotNull(result);
        verify(accountRepository).save(any());
    }

    @Test
    public void testUpdatePromoter() {
        PromoterDTO promoterDTO = new PromoterDTO();
        promoterDTO.setId(1L);
        promoterDTO.setEmail("promoter@example.com");

        Account promoterAccount = new Account();
        promoterAccount.setId(1L);
        promoterAccount.setReferenceType(Account.Type.PROMOTER);

        PromoterInfo promoterInfo = new PromoterInfo();
        promoterInfo.setIdPromoter(1L);

        when(accountValidator.getPromoterAndValidateUpdate(any()))
            .thenReturn(Pair.of(promoterAccount, promoterInfo));
        when(accountRepository.save(any())).thenReturn(promoterAccount);
        when(promoterInfoRepository.save(any())).thenReturn(promoterInfo);

        PromoterDTO result = adminService.updatePromoter(promoterDTO);

        assertNotNull(result);
        verify(accountRepository).save(any());
        verify(promoterInfoRepository).save(any());
    }

    @Test
    public void testDelete() {
        Account accountToDelete = new Account();
        accountToDelete.setId(2L);
        
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountToDelete));
        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAccount));

        adminService.delete(2L);

        verify(accountRepository).deleteById(2L);
    }

    @Test
    public void testDelete_SelfDelete() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAccount));

        assertThrows(IllegalArgumentException.class, () -> adminService.delete(1L));
    }
} 