package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_RECIPE_STEP;

public interface Rep_RECIPE_STEP extends JpaRepository<TB_RECIPE_STEP, Integer>{

	List<TB_RECIPE_STEP> findAllByRecipe_RcpId(String rcp_id);
}
