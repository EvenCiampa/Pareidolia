package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PublishedEventDTO extends EventDTO implements Serializable {
	public Double score;
}