package com.pareidolia.configuration.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(exclude = "timestamp")
public class ErrorResponse implements Serializable {
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", shape = JsonFormat.Shape.STRING)
	public final LocalDateTime timestamp;
	public final Integer status;
	public final String message;

	public ErrorResponse(Integer status, String message) {
		this.timestamp = LocalDateTime.now();
		this.status = status;
		this.message = message;
	}
}