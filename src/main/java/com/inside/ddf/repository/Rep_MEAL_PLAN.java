package com.inside.ddf.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.entity.TB_MEAL_PLAN;
import com.inside.ddf.entity.TB_USER;

public interface Rep_MEAL_PLAN extends JpaRepository<TB_MEAL_PLAN, Integer>{

	public TB_MEAL_PLAN findByUserAndMealTypeAndMealDate(TB_USER user, CategoryTime mealType, LocalDate mealDate);
}
