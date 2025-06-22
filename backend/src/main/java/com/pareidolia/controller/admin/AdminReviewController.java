package com.pareidolia.controller.admin;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.service.admin.AdminReviewService;
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
@RequestMapping(path = "/admin/review")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Admin", description = "The Admin APIs")
public class AdminReviewController {

	private final AdminReviewService adminReviewService;

	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ReviewDTO> getEventReviews(@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminReviewService.getEventReviews(idEvent, page, size);
	}

	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReviewDTO create(@RequestBody ReviewDTO createReviewDTO) {
		return adminReviewService.create(createReviewDTO);
	}

	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminReviewService.delete(id);
	}

}
