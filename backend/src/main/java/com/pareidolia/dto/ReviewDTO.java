package com.pareidolia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class ReviewDTO implements Serializable {
	public Long id;
	public String title;
	public String description;
	public Long score;
	public Long idConsumer;
	public Long idEvent;
	public String accountName;
	public String accountReferenceType;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", shape = JsonFormat.Shape.STRING)
	public LocalDateTime creationTime;
	private boolean isAnonymous;
	private String tag;
}
