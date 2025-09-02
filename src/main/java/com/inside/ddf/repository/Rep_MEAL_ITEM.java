package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.inside.ddf.entity.TB_MEAL_ITEM;
import com.inside.ddf.entity.TB_MEAL_PLAN;

public interface Rep_MEAL_ITEM extends JpaRepository<TB_MEAL_ITEM, Integer>{

	@Transactional
	public long deleteAllByMeal(TB_MEAL_PLAN meal);
	public List<TB_MEAL_ITEM> findAllByMeal(TB_MEAL_PLAN meal);
}
