package com.pareidolia.controller.promoter;


import com.pareidolia.dto.BookingDTO;
import com.pareidolia.service.promoter.PromoterBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/promoter/booking")
public class PromoterBookingController {

	private final PromoterBookingService promoterBookingService;

	//ritorna una prenotazione
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BookingDTO getBooking(@PathVariable("id") Long id) {
		return promoterBookingService.getBooking(id);
	}

	//ritorna lista di prenotazioni
	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<BookingDTO> getBookings(
		@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return promoterBookingService.getBookings(idEvent, page, size);
	}

	//elimina una prenotazione
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		promoterBookingService.delete(id);
	}

}
