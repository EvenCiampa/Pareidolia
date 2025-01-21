package com.pareidolia.service.generic;

import com.pareidolia.dto.PublishedEventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PublicServiceTest {

    @Autowired
    private PublicService publicService;

    @MockitoBean
    private EventRepository eventRepository;

    @MockitoBean
    private PromoterInfoRepository promoterInfoRepository;

    @MockitoBean
    private EventPromoterAssociationRepository eventPromoterAssociationRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    private Event testEvent;
    private Account testAccount;
    private PromoterInfo testPromoterInfo;

    @BeforeEach
    public void setup() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setState(Event.EventState.PUBLISHED);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setEmail("test@example.com");

        testPromoterInfo = new PromoterInfo();
        testPromoterInfo.setIdPromoter(1L);
    }

    @Test
    public void testGetEvent_Success() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventPromoterAssociationRepository.findPromotersByIdEvent(1L))
            .thenReturn(List.of(Pair.of(testAccount, testPromoterInfo)));

        // Act
        PublishedEventDTO result = publicService.getEvent(1L);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetEvent_NotFound() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> publicService.getEvent(1L));
    }

    @Test
    public void testGetEvents() {
        // Arrange
        List<Event> events = List.of(testEvent);
        Page<Event> eventPage = new PageImpl<>(events);
        when(eventRepository.findAllByState(eq(Event.EventState.PUBLISHED), any(PageRequest.class)))
            .thenReturn(eventPage);
        when(eventPromoterAssociationRepository.findPromotersByIdEvent(any()))
            .thenReturn(new ArrayList<>());

        // Act
        Page<PublishedEventDTO> result = publicService.getEvents(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    public void testGetPromoter_Success() {
        // Arrange
        when(promoterInfoRepository.findByIdPromoter(1L)).thenReturn(Optional.of(testPromoterInfo));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        PromoterDTO result = publicService.getPromoter(1L);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetPromoter_NotFound() {
        // Arrange
        when(promoterInfoRepository.findByIdPromoter(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> publicService.getPromoter(1L));
    }

    @Test
    public void testGetPromoters() {
        // Arrange
        List<PromoterInfo> promoters = List.of(testPromoterInfo);
        Page<PromoterInfo> promoterPage = new PageImpl<>(promoters);
        when(promoterInfoRepository.findAll(any(PageRequest.class))).thenReturn(promoterPage);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        Page<PromoterDTO> result = publicService.getPromoters(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    public void testGetPromoterEvents() {
        // Arrange
        List<Event> events = List.of(testEvent);
        Page<Event> eventPage = new PageImpl<>(events);
        when(eventRepository.findAllByStateAndPromoterId(
            eq(Event.EventState.PUBLISHED), 
            eq(1L), 
            any(PageRequest.class)
        )).thenReturn(eventPage);
        when(eventPromoterAssociationRepository.findPromotersByIdEvent(any()))
            .thenReturn(new ArrayList<>());

        // Act
        Page<PublishedEventDTO> result = publicService.getPromoterEvents(1L, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
} 