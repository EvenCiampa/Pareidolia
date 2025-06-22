package com.pareidolia.service.admin;

import com.pareidolia.decorator.review.*;
import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Review;
import com.pareidolia.mapper.ReviewMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import com.pareidolia.validator.ReviewValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminReviewService {
	private final AdminService adminService;
	private final ReviewValidator reviewValidator;
	private final EventRepository eventRepository;
	private final ReviewRepository reviewRepository;
	private final AccountRepository accountRepository;

	/**
	 * Recupera tutte le recensioni associate a un evento specifico, paginandole.
	 * @param idEvent L'ID dell'evento per cui recuperare le recensioni.
	 * @return Page<ReviewDTO> Una pagina contenente le recensioni sotto forma di DTO.
	 */
	public Page<ReviewDTO> getEventReviews(Long idEvent, Integer page, Integer size) {
		if (idEvent == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}

		if (eventRepository.findById(idEvent).isEmpty()) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		reviewValidator.validateEventIsOver(idEvent);

		Page<Review> reviews = reviewRepository.findAllByIdEvent(idEvent,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
				Math.max(10, Optional.ofNullable(size).orElse(10)),
				Sort.by(Sort.Order.desc("id")))
		);
		return reviews.map(review -> ReviewMapper.entityToDTO(review, accountRepository));
	}

	/**
	 * Crea una nuova recensione per un evento, associando la recensione all'account dell'amministratore.
	 * @param reviewDTO DTO della recensione contenente i dati per la creazione.
	 * @return ReviewDTO Il DTO della recensione appena creata.
	 */
	public ReviewDTO create(ReviewDTO reviewDTO) {
		AdminDTO adminDTO = adminService.getData();

		Account account = accountRepository.findById(adminDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		reviewDTO.setId(null);
		if (reviewDTO.getIdConsumer() == null) {
			reviewDTO.setIdConsumer(adminDTO.getId());
		} else if (!reviewDTO.getIdConsumer().equals(adminDTO.getId())) {
			throw new IllegalArgumentException("Invalid id consumer");
		}

		reviewValidator.validateEventIsOver(reviewDTO.getIdEvent());
		reviewValidator.validateNewReviewFields(reviewDTO);

		// Applica i decoratori alla recensione
		ReviewComponent review = new BasicReview(reviewDTO);

		// Decora per l'anonimato se richiesto
		review = new AnonymousDecorator(review);

		// Decora per evidenziare i promoter
		review = new HighlightDecorator(review, accountRepository);

		// Decora per i tag se presenti
		review = new TaggedReview(review);

		// Applica tutte le decorazioni
		ReviewDTO decoratedReviewDTO = review.apply();

		// Converti e salva
		Review newReview = ReviewMapper.dtoToEntity(decoratedReviewDTO);
		newReview.setAccount(account);
		newReview = reviewRepository.save(newReview);

		eventRepository.updateAverageScore(reviewDTO.getIdEvent());

		return ReviewMapper.entityToDTO(newReview, accountRepository);
	}

	/**
	 * Elimina una recensione specifica basata sull'ID fornito e aggiorna il punteggio medio delle recensioni dell'evento associato.
	 * @param id L'ID della recensione da eliminare.
	 */
	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Review not found"));
		Long eventId = review.getIdEvent();

		reviewRepository.deleteById(id);
		eventRepository.updateAverageScore(eventId);
	}
}
