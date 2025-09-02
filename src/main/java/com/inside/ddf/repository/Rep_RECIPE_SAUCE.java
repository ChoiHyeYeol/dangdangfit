package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inside.ddf.entity.TB_RECIPE_SAUCE;

public interface Rep_RECIPE_SAUCE extends JpaRepository<TB_RECIPE_SAUCE, Integer>{

	List<TB_RECIPE_SAUCE> findAllByRecipe_RcpId(String rcp_id);
}
