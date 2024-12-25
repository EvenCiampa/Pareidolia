package com.pareidolia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventDraftDTO implements Serializable {
	private Long id;
	private String title;
	private String description;
	private String place;
	private LocalDate date;
	private LocalTime time;
	private Duration duration;
	private List<PromoterDTO> promoters;
	private Long maxNumberOfParticipants;
}