package com.pareidolia.decorator.review;

import com.pareidolia.dto.ReviewDTO;

//  interfaccia ReviewComponent che dichiara il metodo che tutte le concrete components e i decorator utilizzeranno.

public interface ReviewComponent {
	ReviewDTO apply();
}
