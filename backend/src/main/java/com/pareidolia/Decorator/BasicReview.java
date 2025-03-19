package com.pareidolia.Decorator;

import com.pareidolia.dto.ReviewDTO;

// (agisce come ConcreteComponent) implementa l'interfaccia e definisce il comportamento base.

public class BasicReview implements ReviewComponent {
	private ReviewDTO review;

	public BasicReview(ReviewDTO review) {
		this.review = review;
	}

	@Override
	public ReviewDTO apply() {
		return review;
	}
}