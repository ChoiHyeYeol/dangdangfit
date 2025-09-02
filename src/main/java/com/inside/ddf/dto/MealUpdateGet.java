package com.inside.ddf.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MealUpdateGet {
	LocalDate date;
	int oneTime;
}
