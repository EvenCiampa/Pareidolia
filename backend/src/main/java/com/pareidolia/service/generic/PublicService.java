package com.pareidolia.service.generic;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.repository.*;
import com.pareidolia.repository.model.EventWithInfo;
import com.pareidolia.state.PublishedState;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PublicService {

	private final EventRepository eventRepository;
	private final BookingRepository bookingRepository;
	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;
	@Value("${app.download.dir}")
	private String downloadDir;
	@Value("${app.download.allowed-extensions}")
	private String allowedExtensions;
	@Value("${app.download.max-age}")
	private String cacheMaxAge;

	/**
	 * Recupera un evento pubblicato in base all'ID specificato.
	 */
	public EventDTO getEvent(Long id) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
		if (!Objects.equals(event.getState().getStateName(), PublishedState.name)) {
			throw new IllegalArgumentException("Event not found");
		}
		List<Pair<Account, PromoterInfo>> promoters = findPromotersByEventId(id); // Trova i promotori associati all'evento
		return EventMapper.entityToDTO(event, null, bookingRepository.countByIdEvent(id), promoters);
	}

	/**
	 * Recupera una pagina di eventi pubblicati, ordinati per ID decrescente.
	 */
	public Page<EventDTO> getEvents(Integer page, Integer size) {
		return eventRepository.findAllByState(
			PublishedState.name,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		).map(event -> {
			List<Pair<Account, PromoterInfo>> promoters = findPromotersByEventId(event.getEvent().getId());
			return EventMapper.entityToDTO(event.getEvent(), null, event.getCurrentParticipants(), promoters);
		});
	}

	/**
	 * Trova i promotori associati a un evento specifico.
	 * @param eventId L'ID dell'evento.
	 * @return List<Pair < Account, PromoterInfo>> Lista di coppie contenenti le informazioni degli account e dei promotori.
	 */
	public List<Pair<Account, PromoterInfo>> findPromotersByEventId(Long eventId) {
		return eventPromoterAssociationRepository.findPromotersByIdEvent(eventId);
	}

	/**
	 * Recupera le informazioni di un promotore specifico in base all'ID fornito.
	 */
	public PromoterDTO getPromoter(Long id) {
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(id).orElseThrow(() -> new IllegalArgumentException("Promoter not found"));
		Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	/**
	 * Recupera una pagina di promotori, ordinati per ID decrescente.
	 */
	public Page<PromoterDTO> getPromoters(Integer page, Integer size) {
		return promoterInfoRepository.findAll(
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		).map(promoterInfo -> {
			Account account = accountRepository.findById(promoterInfo.getIdPromoter()).orElseThrow(() -> new IllegalArgumentException("Account not found"));
			return AccountMapper.entityToPromoterDTO(account, promoterInfo);
		});
	}

	/**
	 * Recupera una pagina di eventi pubblicati associati a un promotore specifico.
	 * @param idPromoter L'ID del promotore.
	 */
	public Page<EventDTO> getPromoterEvents(Long idPromoter, Integer page, Integer size) {
		Page<EventWithInfo> events = eventRepository.findAllByStateAndPromoterId(PublishedState.name, idPromoter,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		);
		return events.map(event -> EventMapper.entityToDTO(event.getEvent(), null, event.getCurrentParticipants(), findPromotersByEventId(event.getEvent().getId())));
	}

	/**
	 * Serve un'immagine da un percorso di archiviazione basato su un nome di file immagine specificato.
	 * @param imageName Il nome del file immagine da recuperare.
	 */
	public ResponseEntity<Resource> getImage(String imageName) {
		if (!isValidImageFileName(imageName)) {
			return ResponseEntity.badRequest().build();
		}

		try {
			Path imagePath = Paths.get(downloadDir, imageName);

			if (!Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
				return ResponseEntity.notFound().build();
			}

			String contentType = Files.probeContentType(imagePath);
			if (contentType == null || !contentType.startsWith("image/")) {
				return ResponseEntity.badRequest().build();
			}

			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(imagePath));

			// Return response with proper headers
			return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.contentLength(resource.contentLength())
				.header("Cache-Control", "public, max-age=" + cacheMaxAge)
				.body(resource);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Valida il nome del file immagine per garantire che sia sicuro e conforme alle estensioni consentite.
	 * @param fileName Il nome del file da validare.
	 * @return boolean True se il nome del file è valido, altrimenti False.
	 */
	private boolean isValidImageFileName(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return false;
		}

		// Only allow alphanumeric characters, hyphens, underscores and one dot for extension
		if (!fileName.matches("^[a-zA-Z0-9\\-_]+(\\.[a-zA-Z0-9]+)?$")) {
			return false;
		}

		// Check file extension
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		return Arrays.asList(allowedExtensions.split(",")).contains(extension);
	}
}
