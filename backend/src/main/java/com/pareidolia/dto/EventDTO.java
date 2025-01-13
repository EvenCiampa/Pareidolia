package com.pareidolia.dto;

import com.pareidolia.entity.Event;
import lombok.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO implements Serializable {
	public Long id;
	public String title;
	public String description;
	public String place;
	public LocalDate date;
	public LocalTime time;
	public Duration duration;
	public List<PromoterDTO> promoters;
	public Long maxNumberOfParticipants;
	public Event.EventState state;
}