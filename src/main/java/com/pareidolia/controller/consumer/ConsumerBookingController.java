package com.pareidolia.controller.consumer;


import com.pareidolia.dto.BookingDTO;
import com.pareidolia.service.consumer.ConsumerBookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/consumer/booking")
@SecurityRequirement(name = "JWT_Consumer")
@Tag(name = "Consumer", description = "The Consumer APIs")
public class ConsumerBookingController {

	private final ConsumerBookingService consumerBookingService;

	//ritorna una prenotazione passando id del booking
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BookingDTO getBooking(@PathVariable("id") Long id) {
		return consumerBookingService.getBooking(id);
	}

	//ritorna lista di prenotazioni
	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<BookingDTO> getBookings(@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return consumerBookingService.getBookings(page, size);
	}

	// crea una nuova prenotazione, passo id evento, l'id utente lo ricavo con getData in ConsumerService
	@PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public BookingDTO create(@PathVariable("id") Long id) {
		return consumerBookingService.create(id);
	}

	//elimina una prenotazione
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		consumerBookingService.delete(id);
	}

}
