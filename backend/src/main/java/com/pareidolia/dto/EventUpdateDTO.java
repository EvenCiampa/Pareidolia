package com.pareidolia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class EventUpdateDTO implements Serializable {
	public Long id;
	public String title;
	public String description;
	public String image;
	public String place;
	@JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
	public LocalDate date;
	@JsonFormat(pattern = "HH:mm:ss.SSS'Z'", shape = JsonFormat.Shape.STRING)
	public LocalTime time;
	public Duration duration;
	public List<String> promoterEmails;
	public Long maxNumberOfParticipants;
	public Long currentParticipants;
	public String state;
	public Double score;
}