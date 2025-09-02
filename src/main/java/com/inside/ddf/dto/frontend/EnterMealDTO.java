package com.inside.ddf.dto.frontend;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class EnterMealDTO {

	LocalDate date;
	String NickName;
	int dDay;
	List<List<String>> mainMeal;
	List<List<String>> DessertMeal;
	
}
