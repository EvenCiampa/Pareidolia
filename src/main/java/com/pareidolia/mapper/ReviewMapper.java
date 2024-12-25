package com.pareidolia.mapper;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Review;

public class ReviewMapper {
	public static ReviewDTO entityToDTO(Review entity) {
		if (entity == null) return null;
		ReviewDTO dto = new ReviewDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setScore(entity.getScore());
		dto.setIdConsumer(entity.getIdConsumer());
		dto.setIdEvent(entity.getIdEvent());
		return dto;
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
		return entity;
	}
}
