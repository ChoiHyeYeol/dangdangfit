package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.code.CategoryMenu;
import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.entity.TB_RECIPE;
import com.inside.ddf.entity.TB_USER;

public interface Rep_RECIPE extends JpaRepository<TB_RECIPE, String>{

	public List<TB_RECIPE> findAllByCategoryTime(CategoryTime categoryTime);
	public List<TB_RECIPE> findAllByCategoryMenu(CategoryMenu categoryMenu);
	public List<TB_RECIPE> findAllByUser(TB_USER user);
	
}
