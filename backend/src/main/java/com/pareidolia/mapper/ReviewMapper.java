package com.pareidolia.mapper;

import com.pareidolia.decorator.review.*;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Review;
import com.pareidolia.repository.AccountRepository;

public class ReviewMapper {
	public static ReviewDTO entityToDTO(Review entity, AccountRepository accountRepository) {
		if (entity == null) return null;
		ReviewDTO dto = new ReviewDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setScore(entity.getScore());
		dto.setIdConsumer(entity.getIdConsumer());
		dto.setIdEvent(entity.getIdEvent());
		dto.setAccountName(entity.getAccount().getName() + " " + entity.getAccount().getSurname());
		dto.setAccountReferenceType(entity.getAccount().getReferenceType().name());
		dto.setCreationTime(entity.getCreationTime());
		dto.setAnonymous(entity.isAnonymous());
		dto.setTag(entity.getTag());

		// Creazione dell'istanza di ReviewComponent tramite la classe concreta BasicReview
		ReviewComponent review = new BasicReview(dto);
		// Apply HighlightDecorator in base al Account.Type
		review = new HighlightDecorator(review, accountRepository);
		// Apply AnonymousDecorator in base alla flag isAnonymous
		review = new AnonymousDecorator(review);
		// Apply TaggedReview in base al tag fornito
		review = new TaggedReview(review);

		return review.apply();
	}

	public static Review dtoToEntity(ReviewDTO dto) {
		if (dto == null) return null;
		Review entity = new Review();
		entity.setId(dto.getId());
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setScore(dto.getScore());
		entity.setIdConsumer(dto.getIdConsumer());
		entity.setIdEvent(dto.getIdEvent());
		entity.setAnonymous(dto.isAnonymous());
		entity.setTag(dto.getTag());
		return entity;
	}
}
