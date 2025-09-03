package com.inside.ddf.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inside.ddf.code.CategoryMenu;
import com.inside.ddf.code.CategoryTime;
import com.inside.ddf.entity.TB_RECIPE;
import com.inside.ddf.entity.TB_USER;

public interface Rep_RECIPE extends JpaRepository<TB_RECIPE, String>{

	public List<TB_RECIPE> findAllByCategoryTime(CategoryTime categoryTime);
	public List<TB_RECIPE> findAllByCategoryMenu(CategoryMenu categoryMenu);
	public List<TB_RECIPE> findAllByUser(TB_USER user);
	
	 @Query("""
			    select r from TB_RECIPE r
			    where (:q is null or 
			           lower(r.rcpNm) like lower(concat('%', :q, '%'))
			           or lower(r.descTxt) like lower(concat('%', :q, '%'))
			          )
			  """)
	  Page<TB_RECIPE> searchSimple(@Param("q") String q, Pageable pageable);
	
}
