package com.pareidolia.controller.consumer;


import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.service.consumer.ConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/consumer/consumer")
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
