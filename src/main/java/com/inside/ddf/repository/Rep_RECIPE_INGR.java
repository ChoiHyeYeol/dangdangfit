package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_RECIPE_INGR;

public interface Rep_RECIPE_INGR extends JpaRepository<TB_RECIPE_INGR, Integer>{

	List<TB_RECIPE_INGR> findAllByRecipe_RcpId(String rcp_id);
}
