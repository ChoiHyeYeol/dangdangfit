package com.inside.ddf.dto.frontend;

import java.time.LocalDate;

import lombok.Data;

@Data
public class GetMealInDTO {

	LocalDate date;
	int time;
}
