package com.pareidolia.decorator.review;

// (agisce come uno dei ConcreteDecorators) estende ReviewDecorator e aggiunge comportamenti specifici.

import com.pareidolia.dto.ReviewDTO;

public class TaggedReview extends ReviewDecorator {
	public TaggedReview(ReviewComponent decoratedReview) {
		super(decoratedReview);
	}

	@Override
	public ReviewDTO apply() {
		ReviewDTO review = super.apply();
		if (review.getTag() != null && !review.getTag().isEmpty()) {
			review.setDescription(review.getDescription() + " [Tag: " + review.getTag() + "]");
		}
		return review;
	}
}
