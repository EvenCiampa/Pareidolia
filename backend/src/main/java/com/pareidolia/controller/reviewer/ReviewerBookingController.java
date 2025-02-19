package com.pareidolia.controller.reviewer;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.service.reviewer.ReviewerBookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "JWT_Reviewer")
@PreAuthorize("hasAnyAuthority('REVIEWER')")
@RequestMapping(path = "/reviewer/booking")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Reviewer", description = "The Reviewer APIs")
public class ReviewerBookingController {

	private final ReviewerBookingService reviewerBookingService;

	// display: ritorna una prenotazione
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BookingDTO getBooking(@PathVariable("id") Long id) {
		return reviewerBookingService.getBooking(id);
	}

	//ritorna lista di prenotazioni
	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<BookingDTO> getBookings(
		@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return reviewerBookingService.getBookings(idEvent, page, size);
	}
}
