package com.event.ticketing.user_service.dto;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {
	private String name;

	private Boolean active;
}
