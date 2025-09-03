package com.inside.ddf.dto.frontend;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class SelectMealDTO {
	LocalDate date;
	String NickName;
	int dDay;
	List<List<String>> breakfastMeal;
	List<List<String>> lunchMeal;
	List<List<String>> dinnerMeal;
	List<List<String>> DessertMeal;
}
