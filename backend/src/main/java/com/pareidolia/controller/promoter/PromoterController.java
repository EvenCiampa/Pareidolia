package com.pareidolia.controller.promoter;


import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.service.promoter.PromoterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/promoter")
@SecurityRequirement(name = "JWT_Promoter")
@Tag(name = "Promoter", description = "The Promoter APIs")
public class PromoterController {

	private final PromoterService promoterService;

	@GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO getData() {
		return promoterService.getData();
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO update(@RequestBody PromoterDTO promoterDTO) {
		return promoterService.update(promoterDTO);
	}
}
