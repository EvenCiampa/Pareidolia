package com.pareidolia.service.promoter;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.validator.EventDraftValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.util.Pair;
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
public class PromoterEventServiceTest {

    @Autowired
    private PromoterEventService promoterEventService;

    @MockitoBean
    private PromoterService promoterService;

    @MockitoBean
    private EventRepository eventRepository;

    @MockitoBean
    private EventDraftValidator eventDraftValidator;

    @MockitoBean
    private EventPromoterAssociationRepository eventPromoterAssociationRepository;

    private Event testEvent;
    private PromoterDTO testPromoterDTO;
    private EventPromoterAssociation testAssociation;

    @BeforeEach
    public void setup() {
        testEvent = new Event();
        testEvent.setId(1L);

        testPromoterDTO = new PromoterDTO();
        testPromoterDTO.setId(1L);

        testAssociation = new EventPromoterAssociation();
        testAssociation.setIdEvent(1L);
        testAssociation.setIdPromoter(1L);

        when(promoterService.getData()).thenReturn(testPromoterDTO);
    }

    @Test
    public void testGetEventDraft() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventPromoterAssociationRepository.findByIdEventAndIdPromoter(1L, 1L))
            .thenReturn(Optional.of(testAssociation));
        when(eventPromoterAssociationRepository.findPromotersByIdEvent(1L))
            .thenReturn(Collections.singletonList(Pair.of(new Account(), new PromoterInfo())));

        EventDTO result = promoterEventService.getEventDraft(1L);

        assertNotNull(result);
        verify(eventRepository).findById(1L);
        verify(eventPromoterAssociationRepository).findByIdEventAndIdPromoter(1L, 1L);
    }

    @Test
    public void testCreate() {
        EventDTO createDTO = new EventDTO();
        when(eventRepository.save(any())).thenReturn(testEvent);
        when(eventPromoterAssociationRepository.save(any())).thenReturn(testAssociation);
        when(eventPromoterAssociationRepository.findPromotersByIdEvent(any()))
            .thenReturn(Collections.singletonList(Pair.of(new Account(), new PromoterInfo())));

        EventDTO result = promoterEventService.create(createDTO);

        assertNotNull(result);
        verify(eventDraftValidator).createEventDraftValidator(createDTO);
        verify(eventRepository).save(any());
        verify(eventPromoterAssociationRepository).save(any());
    }

    @Test
    public void testSubmitForReview() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventPromoterAssociationRepository.findByIdEventAndIdPromoter(1L, 1L))
            .thenReturn(Optional.of(testAssociation));
        when(eventRepository.save(any())).thenReturn(testEvent);
        when(eventPromoterAssociationRepository.findPromotersByIdEvent(any()))
            .thenReturn(Collections.singletonList(Pair.of(new Account(), new PromoterInfo())));

        EventDTO result = promoterEventService.submitForReview(1L);

        assertNotNull(result);
        verify(eventRepository).save(any());
    }
} 