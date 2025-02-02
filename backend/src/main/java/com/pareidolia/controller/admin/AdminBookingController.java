package com.pareidolia.controller.admin;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.service.admin.AdminBookingService;
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
@SecurityRequirement(name = "JWT_Admin")
@PreAuthorize("hasAnyAuthority('ADMIN')")
@RequestMapping(path = "/admin/booking")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Admin", description = "The Admin APIs")
public class AdminBookingController {

	private final AdminBookingService adminBookingService;

	// display: ritorna una prenotazione
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BookingDTO getBooking(@PathVariable("id") Long id) {
		return adminBookingService.getBooking(id);
	}

	//ritorna lista di prenotazioni
	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<BookingDTO> getBookings(
		@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminBookingService.getBookings(idEvent, page, size);
	}

	// delete: elimina una prenotazione
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminBookingService.delete(id);
	}
	//update non servirebbe a nulla
}
