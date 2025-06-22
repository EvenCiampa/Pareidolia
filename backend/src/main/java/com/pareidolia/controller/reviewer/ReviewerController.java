package com.pareidolia.controller.reviewer;

import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.service.reviewer.ReviewerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(path = "/reviewer")
@SecurityRequirement(name = "JWT_Reviewer")
@PreAuthorize("hasAnyAuthority('REVIEWER')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Reviewer", description = "The Reviewer APIs")
public class ReviewerController {
	private final ReviewerService reviewerService;

	//display miei dati
	@GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ReviewerDTO getData() {
		return reviewerService.getData();
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO update(@RequestBody ReviewerDTO reviewerDTO) {
		return reviewerService.update(reviewerDTO);
	}

	@PostMapping(value = "/update/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
		return reviewerService.updatePassword(passwordUpdateDTO);
	}
}



