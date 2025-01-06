package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO implements Serializable {
	public Long id;
	public String title;
	public String description;
	public Long score;
	public Long idConsumer;
	public Long idEvent;
}
