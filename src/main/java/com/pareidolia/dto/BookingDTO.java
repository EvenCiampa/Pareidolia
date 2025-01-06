package com.pareidolia.dto;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO implements Serializable {
	public Long id;
	public PublishedEventDTO event;
	public ConsumerDTO consumer;
}
