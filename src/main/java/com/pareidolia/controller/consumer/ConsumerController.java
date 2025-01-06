package com.pareidolia.controller.consumer;


import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.service.consumer.ConsumerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/consumer")
@SecurityRequirement(name = "JWT_Consumer")
@Tag(name = "Consumer", description = "The Consumer APIs")
public class ConsumerController {

	private final ConsumerService consumerService;

	@GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConsumerDTO getData() {
		return consumerService.getData();
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ConsumerDTO update(@RequestBody ConsumerDTO consumerDTO) {
		return consumerService.update(consumerDTO);
	}
}
