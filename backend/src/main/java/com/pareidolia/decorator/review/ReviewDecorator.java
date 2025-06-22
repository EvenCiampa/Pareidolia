package com.pareidolia.decorator.review;

//  implementa l'interfaccia e contiene un riferimento all'oggetto ReviewComponent che avvolge, delegando a esso tutte le operazioni.

import com.pareidolia.dto.ReviewDTO;

public abstract class ReviewDecorator implements ReviewComponent {
	protected ReviewComponent decoratedReview;

	public ReviewDecorator(ReviewComponent decoratedReview) {
		this.decoratedReview = decoratedReview;
	}

	public ReviewDTO apply() {
		return decoratedReview.apply();
	}
}
