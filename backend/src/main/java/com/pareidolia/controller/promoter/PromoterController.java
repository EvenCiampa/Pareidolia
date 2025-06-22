package com.pareidolia.controller.promoter;

import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.service.promoter.PromoterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping(path = "/promoter")
@SecurityRequirement(name = "JWT_Promoter")
@PreAuthorize("hasAnyAuthority('PROMOTER')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Promoter", description = "The Promoter APIs")
public class PromoterController {

	private final PromoterService promoterService;

	@GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO getData() {
		return promoterService.getData();
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO update(@RequestBody PromoterDTO promoterDTO) {
		return promoterService.update(promoterDTO);
	}

	@PostMapping(value = "/update/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
		return promoterService.updatePassword(passwordUpdateDTO);
	}

	@PostMapping(value = "/update/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public PromoterDTO updateImage(@RequestParam("image") MultipartFile file) {
		return promoterService.updateImage(file);
	}

	@DeleteMapping(value = "/image")
	public PromoterDTO deleteImage() {
		return promoterService.deleteImage();
	}
}
