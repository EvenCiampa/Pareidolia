package com.pareidolia.decorator.review;

import com.pareidolia.dto.ReviewDTO;

// (agisce come uno dei ConcreteDecorators) estende ReviewDecorator e aggiunge comportamenti specifici.
public class AnonymousDecorator extends ReviewDecorator {
	public AnonymousDecorator(ReviewComponent decoratedReview) {
		super(decoratedReview);
	}

	@Override
	public ReviewDTO apply() {
		ReviewDTO review = super.apply();
		if (review.isAnonymous()) {
			review.setAccountName("Anonimo");
			review.setIdConsumer(null);
		}
		return review;
	}
}
