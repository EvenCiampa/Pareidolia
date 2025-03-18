package com.pareidolia.service.promoter;

import com.pareidolia.dto.PromoterDTO;
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
public class PromoterReviewService {
	private final PromoterService promoterService;
	private final ReviewValidator reviewValidator;
	private final EventRepository eventRepository;
	private final ReviewRepository reviewRepository;
	private final AccountRepository accountRepository;

	/**
	 * Recupera le recensioni associate a un evento specifico, paginate e ordinate.
	 * @param idEvent L'ID dell'evento per cui recuperare le recensioni.
	 * @return Page<ReviewDTO> Una pagina contenente le recensioni sotto forma di DTO.
	 * @throws IllegalArgumentException Se l'ID dell'evento non è valido o se l'evento non è ancora concluso.
	 */
	public Page<ReviewDTO> getEventReviews(Long idEvent, Integer page, Integer size) {
		if (idEvent == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		// Verifica la presenza dell'evento e che sia finito
		if (eventRepository.findById(idEvent).isEmpty()) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		reviewValidator.validateEventIsOver(idEvent);

		// ricerca messaggi relativi a un evento e paginali
		Page<Review> reviews = reviewRepository.findAllByIdEvent(idEvent,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		);
		return reviews.map(review -> ReviewMapper.entityToDTO(review, accountRepository));
	}

	/**
	 * Crea una nuova recensione per un evento, assicurandosi che l'evento sia concluso e che i campi della recensione siano validi.
	 * @param reviewDTO DTO della recensione da creare.
	 * @return ReviewDTO Il DTO della recensione appena creata.
	 * @throws IllegalArgumentException Se il conto dell'utente non è valido, se l'ID del consumatore non corrisponde all'ID del promotore autenticato, o se i dati della recensione non sono validi.
	 */
	public ReviewDTO create(ReviewDTO reviewDTO) {
		PromoterDTO promoterDTO = promoterService.getData();

		Account account = accountRepository.findById(promoterDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		reviewDTO.setId(null);
		if (reviewDTO.getIdConsumer() == null) {
			reviewDTO.setIdConsumer(promoterDTO.getId());
		} else if (!reviewDTO.getIdConsumer().equals(promoterDTO.getId())) {
			throw new IllegalArgumentException("Invalid id consumer");
		}

		reviewValidator.validateEventIsOver(reviewDTO.getIdEvent());
		reviewValidator.validateNewReviewFields(reviewDTO);

		Review newReview = ReviewMapper.dtoToEntity(reviewDTO);
		newReview.setAccount(account);

		newReview = reviewRepository.save(newReview);

		eventRepository.updateAverageScore(reviewDTO.getIdEvent());

		return ReviewMapper.entityToDTO(newReview, accountRepository);
	}
}
