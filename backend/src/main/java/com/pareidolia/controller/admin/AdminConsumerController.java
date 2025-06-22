package com.pareidolia.controller.admin;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.service.admin.AdminConsumerService;
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
@RequestMapping(path = "/admin/consumer")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Admin", description = "The Admin APIs")
public class AdminConsumerController {

	private final AdminConsumerService adminConsumerService;

	//display
	@GetMapping(value = "/{id}/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConsumerDTO getData(@PathVariable("id") Long idConsumer) {
		return adminConsumerService.getData(idConsumer);
	}

	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ConsumerDTO> getConsumers(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminConsumerService.getConsumers(page, size);
	}

	//update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ConsumerDTO update(@RequestBody ConsumerDTO consumerDTO) {
		return adminConsumerService.update(consumerDTO);
	}

	// add inutile

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminConsumerService.delete(id);
	}
}
