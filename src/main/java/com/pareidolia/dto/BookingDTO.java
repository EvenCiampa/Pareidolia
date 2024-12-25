package com.pareidolia.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO implements Serializable {
	private Long id;
	private EventDTO event;
	private ConsumerDTO consumer;
}
